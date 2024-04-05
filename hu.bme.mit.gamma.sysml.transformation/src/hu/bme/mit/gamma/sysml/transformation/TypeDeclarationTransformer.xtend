package hu.bme.mit.gamma.sysml.transformation

import org.omg.sysml.lang.sysml.EnumerationDefinition
import org.omg.sysml.lang.sysml.EnumerationUsage

import static extension hu.bme.mit.gamma.sysml.transformation.Namings.*

class TypeDeclarationTransformer extends AtomicElementTransformer {
	
	new(StatechartTraceability traceability) {
		super(traceability)
	}
	
	def dispatch transformTypeDeclaration(EnumerationDefinition enumeration) {
		val declarationName = enumeration.typeDeclarationName
		val enumeratedValues = enumeration.variantMembership
				.map[it.memberElement]
				.filter(EnumerationUsage) // FIXME enumeratedValue does not work
		
		val gammaEnumeration = createEnumerationTypeDefinition
		
		for (enumeratedValue : enumeratedValues) {
			val literalName = enumeratedValue.literalName
			val gammaLiteral = createEnumerationLiteralDefinition
			gammaLiteral.name = literalName
			
			gammaEnumeration.literals += gammaLiteral
			
			traceability.putLiteral(enumeratedValue, gammaLiteral)
		}
		
		val gammaTypeDeclaration = createTypeDeclaration
		gammaTypeDeclaration.name = declarationName
		gammaTypeDeclaration.type = gammaEnumeration
		
		traceability.putType(enumeration, gammaTypeDeclaration)
		
		return gammaTypeDeclaration
	}
	
}