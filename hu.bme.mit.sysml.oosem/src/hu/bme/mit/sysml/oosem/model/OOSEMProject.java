package hu.bme.mit.sysml.oosem.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.omg.sysml.lang.sysml.OccurrenceDefinition;
import org.omg.sysml.lang.sysml.Type;

import hu.bme.mit.sysml.oosem.model.OOSEMModelLoader.BlockFamilyStructures;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils.OOSEMBlockType;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.EObject;

public class OOSEMProject {
	public OOSEMProject(IProject project, Set<EObject> specifications, Set<EObject> designs, Set<EObject> integrations, BlockFamilyStructures specificationsWithDesigns, BlockFamilyStructures designsWithIntegrations, Map<EObject, Set<String>> validationErrors, Map<EObject, Set<String>> validationWarnings) {
		this.project = project;
		this.specifications = specifications;
		this.designs = designs;
		this.integrations = integrations;
		this.specificationsWithDesigns = specificationsWithDesigns;
		this.designsWithIntegrations = designsWithIntegrations;
		this.validationErrors = validationErrors;
		this.validationWarnings = validationWarnings;
	}

	public Set<EObject> getSpecifications() {
		return specifications;
	}

	public Set<EObject> getDesigns() {
		return designs;
	}

	public Set<EObject> getIntegrations() {
		return integrations;
	}

	public BlockFamilyStructures getSpecificationsWithTheirDesigns() {
		return specificationsWithDesigns;
	}

	public BlockFamilyStructures getDesignsWithTheirIntegrations() {
		return designsWithIntegrations;
	}
	
	public Map<EObject, Set<String>> getValidationErrors() {
		return validationErrors;
	}
	
	public Map<EObject, Set<String>> getValidationWarnings() {
		return validationWarnings;
	}
	
	public IProject getProject() {
		return project;
	}
	
	public List<EObject> getPossibleImplementationsOfSpecification(EObject o){
		var res = new ArrayList<EObject>();
		if(o instanceof Type d && OOSEMUtils.getOOSEMBlockType(d) == OOSEMBlockType.SPECIFICATION) {
			var defs = OOSEMUtils.getOOSEMDefinitionsToUsage(d);
			if(defs.size() > 0) {
				var def = defs.get(0);
				if(def instanceof OccurrenceDefinition spec) {
					var designs = specificationsWithDesigns.getBlocksWithFamily().get(spec);
					if(designs.isEmpty()){
						return res;
					} else {
						res.addAll(designs);
						for(var design : designs) {
							var integrations = designsWithIntegrations.getBlocksWithFamily().get(design);
							if(integrations != null) res.addAll(designsWithIntegrations.getBlocksWithFamily().get(design));
						}
					}
				}
			}
		}
		return res;
	}

	private IProject project;
	private Set<EObject> specifications;
	private Set<EObject> designs;
	private Set<EObject> integrations;
	private BlockFamilyStructures specificationsWithDesigns;
	private BlockFamilyStructures designsWithIntegrations;
	private Map<EObject, Set<String>> validationErrors;
	private Map<EObject, Set<String>> validationWarnings;
}
