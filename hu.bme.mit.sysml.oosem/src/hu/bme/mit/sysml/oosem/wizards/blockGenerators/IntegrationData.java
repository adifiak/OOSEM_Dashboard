package hu.bme.mit.sysml.oosem.wizards.blockGenerators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.omg.sysml.lang.sysml.OccurrenceDefinition;

import hu.bme.mit.sysml.oosem.model.project.interfaces.OOSEMProject;
import hu.bme.mit.sysml.oosem.wizards.blockGenerators.IntegrationPage.OOSEMIntegrationConfig;

public class IntegrationData {
	public OccurrenceDefinition subjectDesign;
	public OOSEMProject project;
	public List<OOSEMIntegrationConfig> configs;
	public Map<EObject, String> featureNames = new HashMap<>();
}
