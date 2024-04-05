package hu.bme.mit.gamma.sysml.transformation

import hu.bme.mit.gamma.statechart.statechart.Region
import hu.bme.mit.gamma.statechart.statechart.State
import hu.bme.mit.gamma.sysml.transformation.util.FeatureHierarchy
import org.omg.sysml.lang.sysml.ActionUsage
import org.omg.sysml.lang.sysml.AttributeUsage
import org.omg.sysml.lang.sysml.DataType
import org.omg.sysml.lang.sysml.EnumerationUsage
import org.omg.sysml.lang.sysml.PortDefinition
import org.omg.sysml.lang.sysml.PortUsage
import org.omg.sysml.lang.sysml.StateUsage
import org.omg.sysml.lang.sysml.Type
import org.omg.sysml.lang.sysml.Usage

import static extension hu.bme.mit.gamma.statechart.derivedfeatures.StatechartModelDerivedFeatures.*
import static extension hu.bme.mit.gamma.sysml.transformation.util.SysMLModelDerivedFeatures.*
import static extension java.lang.Math.abs

class Namings {
	
	def static String getComponentName(FeatureHierarchy parts) '''«FOR part : parts.features SEPARATOR "_"»«part.name.toFirstUpper»«ENDFOR»'''
	def static String getInstanceName(Usage partUsage) '''«partUsage.name»'''
	
	def static String getStatechartName(FeatureHierarchy parts) '''«parts.componentName»Statechart'''
	def static String getParentRegionName(StateUsage stateUsage) '''Parent«stateUsage.name»'''
	def static String getSubegionName(StateUsage stateUsage) '''Sub«stateUsage.name»'''
	def static String getStateName(StateUsage stateUsage) '''«stateUsage.name»'''
	def static String getInitialStateName(StateUsage stateUsage) '''Initial«stateUsage.name»'''
	def static String getInitialStateName(StateUsage stateUsage, Region region) '''«stateUsage.initialStateName»Of«region.name»'''
	
	def static String getEntryNodeName(ActionUsage actionUsage) '''Entry_«actionUsage.hashCode.abs»'''
	def static String getExitNodeName(ActionUsage actionUsage) '''FinalState'''
	def static String getExitNodeName(ActionUsage actionUsage, Region region) '''«actionUsage.exitNodeName»Of«region.name»'''
	
	def static String getTimeoutName(State state) '''«state.name»_«state.containingStatechart.timeoutDeclarations.size»'''
	def static String getVariableName(AttributeUsage attributeUsage) '''«attributeUsage.name»'''
	def static String getTypeDeclarationName(DataType dataType) '''«dataType.name»'''
	def static String getLiteralName(EnumerationUsage enumerationUsage) '''«enumerationUsage.name»'''
	
	def static String getInterfaceName() '''Interface_'''
	def static String getPortName() '''Port_'''
	def static String getInterfaceName(PortDefinition port) '''«port.name»'''
	def static String getPortName(PortUsage port) '''«port.name»'''
	
	def static String getMessageQueueName(Usage partUsage) '''«partUsage.name»Queue'''
	def static String getEnvironmentMessageQueueName(Usage partUsage) '''«partUsage.name»EnvironmentQueue'''
	
	//
	private def static String getEventName(Type type) '''_«type.commonTypeName»'''
	def static String getInEventName(Type type) '''in«type.eventName»'''
	def static String getOutEventName(Type type) '''out«type.eventName»'''
	
	def static String getParameterName(Type type) '''_«type.commonTypeName»Value'''
	//
}