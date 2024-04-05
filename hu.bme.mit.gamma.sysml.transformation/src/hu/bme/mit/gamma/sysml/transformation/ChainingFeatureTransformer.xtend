package hu.bme.mit.gamma.sysml.transformation

import hu.bme.mit.gamma.expression.model.Expression
import hu.bme.mit.gamma.statechart.composite.AsynchronousAdapter
import hu.bme.mit.gamma.sysml.transformation.util.FeatureHierarchy
import java.util.List
import java.util.function.Function
import org.omg.sysml.lang.sysml.FeatureChainExpression
import org.omg.sysml.lang.sysml.FeatureReferenceExpression

import static extension hu.bme.mit.gamma.statechart.derivedfeatures.StatechartModelDerivedFeatures.*
import static extension hu.bme.mit.gamma.sysml.transformation.util.SysMLModelDerivedFeatures.*

class ChainingFeatureTransformer extends CompositeElementTransformer
			implements Function<org.omg.sysml.lang.sysml.Expression, Expression> {
	
	private new(CompositeTraceability traceability) {
		super(traceability)
	}
	
	static def of(CompositeTraceability traceability) {
		return new ExpressionTransformer(
			new ChainingFeatureTransformer(traceability))
	}
		
	//
	
	override apply(org.omg.sysml.lang.sysml.Expression expression) {
		return expression.transformExpression
	}
	
	//
	
	def dispatch Expression transformExpression(org.omg.sysml.lang.sysml.Expression expression) {
		throw new IllegalArgumentException("Not supported expression: " + expression)
	}
	
	def dispatch Expression transformExpression(FeatureReferenceExpression expression) {
		val referent = expression.referent
		if (referent instanceof FeatureChainExpression) {
			return referent.transformExpression;
		} else {
			return null
		}
	}
	
	def dispatch Expression transformExpression(FeatureChainExpression expression) {
		val features = expression.features
		val lastFeature = features.last
		
		val usages = newArrayList
		usages += features
		usages -= lastFeature
		val usageHierarchy = new FeatureHierarchy(usages)
		
		val statechartTraceability = traceability.getTraceability(usageHierarchy)
		val expressionTransformer = FeatureTransformer.of(statechartTraceability)
		///
		val featureTransformer = expressionTransformer.getFeatureHandler as FeatureTransformer
		///
		
		val clonedUsageHierarchy = usageHierarchy.clone
		val gammaReverseInstances = newArrayList
		while (!clonedUsageHierarchy.empty) {
			if (traceability.contains(clonedUsageHierarchy)) {
				// In the case of composite systems, the first usage is not "mapped"
				gammaReverseInstances += traceability.get(clonedUsageHierarchy)
			}
			clonedUsageHierarchy.removeLast
		}
		val gammaInstances = gammaReverseInstances.reverse
		
		val gammaInstanceReferences = gammaInstances
				.map[it.createInstanceReference]
		///
		val gammaFeature = featureTransformer.transformFeature(lastFeature)
		///
		
		val gammaExpressions = <Expression>newArrayList
		gammaExpressions += gammaInstanceReferences
		gammaExpressions += gammaFeature
		
		val gammaInstanceStateExpession = gammaExpressions.chainAndExtendReferences
		return gammaInstanceStateExpession
	}
	
	///// Should be reworked in the next SysML release - deprecated
	
	private def getFeatures(List<? extends FeatureReferenceExpression> expressions) {
		val features = newArrayList
		
		for (expression : expressions) {
			features += expression.features
		}
		
		return features
	}
	
	private def getFeatures(FeatureReferenceExpression expression) {
		val features = newArrayList
		
		val feature = expression.referent
		val feautureChain = feature.chainingFeature
		if (feautureChain.empty) {
			features += feature
		}
		else {
			features += feautureChain
		}
		
		return features
	}
	
	/////
	
	protected def chainAndExtendReferences(List<? extends Expression> gammaOperands) {
		val gammaDeclarationReference = gammaOperands.chainReferences
		val gammaInstance = gammaDeclarationReference.instance
		
		val gammaLastInstanceReference = gammaInstance.lastInstanceReference
		val gammaLastInstance = gammaLastInstanceReference.componentInstance
		val gammaLastInstanceType = gammaLastInstance.derivedType
		
		if (gammaLastInstanceType instanceof AsynchronousAdapter) {
			val wrappedInstance = gammaLastInstanceType.wrappedComponent
			// In this transformation, the adapters wrap a statechart
			gammaLastInstanceReference.child = wrappedInstance.createInstanceReference
		}
		
		return gammaDeclarationReference
	}
	
}