package hu.bme.mit.gamma.sysml.transformation

import hu.bme.mit.gamma.expression.model.TypeDefinition
import org.omg.sysml.lang.sysml.DataType
import org.omg.sysml.lang.sysml.EnumerationDefinition
import org.omg.sysml.lang.sysml.Function
import org.omg.sysml.lang.sysml.Type

import static extension hu.bme.mit.gamma.sysml.transformation.util.SysMLModelDerivedFeatures.*

class TypeTransformer extends AtomicElementTransformer {
	
	protected final TypeDeclarationTransformer typeDeclarationTransformer
	
	new(StatechartTraceability traceability) {
		super(traceability)
		this.typeDeclarationTransformer = new TypeDeclarationTransformer(traceability)
	}
	
	def dispatch transformType(Type type) {
		throw new IllegalArgumentException("Not supported type: " + type)
	}
	
	def dispatch transformType(Function type) {
		return type.createType
	}
	
	def dispatch transformType(DataType type) {
		return type.createType
	}
	
	protected def TypeDefinition createType(Type type) {
		val name = type.commonTypeName // Based on SysMLModelDerivedFeatures.getCommonTypeName
		switch (name) {
			case "Boolean": {
				return createBooleanTypeDefinition
			}
			case "Integer": {
				return createIntegerTypeDefinition
			}
			case "Rational": {
				return createRationalTypeDefinition
			}
			case "Real": {
				return createDecimalTypeDefinition
			}
			default: 
				throw new IllegalArgumentException("Not supported type: " + name)
		}
	}
	
	def dispatch transformType(EnumerationDefinition type) {
		val typeDeclaration =
		if (traceability.containsType(type)) {
			// Already mapped
			traceability.getType(type)
		}
		else {
			// Transforming
			typeDeclarationTransformer.transformTypeDeclaration(type)
		}
		val typeReference = typeDeclaration.createTypeReference
		return typeReference
	}
	
}