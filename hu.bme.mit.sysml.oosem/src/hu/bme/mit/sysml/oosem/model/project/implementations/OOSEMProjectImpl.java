package hu.bme.mit.sysml.oosem.model.project.implementations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.omg.sysml.lang.sysml.Element;
import org.omg.sysml.lang.sysml.Feature;
import org.omg.sysml.lang.sysml.LibraryPackage;
import org.omg.sysml.lang.sysml.MetadataUsage;
import org.omg.sysml.lang.sysml.Namespace;
import org.omg.sysml.lang.sysml.OccurrenceDefinition;
import org.omg.sysml.lang.sysml.Type;
import org.omg.sysml.util.FeatureUtil;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.model.elements.OOSEMFeature;
import hu.bme.mit.sysml.oosem.model.project.interfaces.OOSEMProject;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils.OOSEMBlockType;
import hu.bme.mit.sysml.oosem.util.ProjectUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

public class OOSEMProjectImpl implements OOSEMProject {
	
	public class OOSEMProjectImplData {
		public final Map<EObject, OOSEMBlock> blockCatalog = new HashMap<>();
		public final Map<EObject, OOSEMFeature> featureCatalog = new HashMap<>();
		public final Set<OOSEMBlock> specifications = new HashSet<OOSEMBlock>();
		public final Set<OOSEMBlock> designs = new HashSet<OOSEMBlock>();
		public final Set<OOSEMBlock> integrations = new HashSet<OOSEMBlock>();
		public BlockFamilyStructures specificationsWithDesigns;
		public BlockFamilyStructures designsWithIntegrations;

		public OOSEMProjectImplData() {}
	}

	public OOSEMProjectImpl(IProject project, IProgressMonitor monitor) {
		this.project = project;
		var processor = new ProjectProcessor(project, data, monitor);
		processor.process();
		var validator = new ProjectValidator(data);
		validator.validate();
	}

	@Override
	public Set<OOSEMBlock> getSpecifications() { return data.specifications; }

	@Override
	public Set<OOSEMBlock> getDesigns() { return data.designs; }

	@Override
	public Set<OOSEMBlock> getIntegrations() { return data.integrations; }

	@Override
	public BlockFamilyStructures getSpecificationsWithTheirDesigns() { return data.specificationsWithDesigns; }

	@Override
	public BlockFamilyStructures getDesignsWithTheirIntegrations() { return data.designsWithIntegrations; }
	
	@Override
	public IProject getProject() { return project; }
	
	public OOSEMProjectImplData getOOSEMMProjectData() { return data; }
	
	@Override
	public List<EObject> getPossibleImplementationsOfSpecification(OOSEMBlock block){
		var res = new ArrayList<EObject>();
		if(block.getObject() instanceof Type d && OOSEMUtils.getOOSEMBlockType(d) == OOSEMBlockType.SPECIFICATION) {
			var defs = OOSEMUtils.getOOSEMDefinitionsToUsage(d);
			if(defs.size() > 0) {
				var def = defs.get(0);
				if(def instanceof OccurrenceDefinition spec) {
					var designs = data.specificationsWithDesigns.getBlocksWithFamily().get(spec).stream().map(p -> p.getObject()).collect(Collectors.toList());
					if(designs == null || designs.isEmpty()){
						return res;
					} else {
						res.addAll(designs);
						for(var design : designs) {
							var integrations = data.designsWithIntegrations.getBlocksWithFamily().get(design);
							if(integrations != null) res.addAll(data.designsWithIntegrations.getBlocksWithFamily().get(design).stream().map(p -> p.getObject()).collect(Collectors.toList()));
						}
					}
				}
			}
		}
		return res;
	}

	
	private final IProject project;
	private OOSEMProjectImplData data = new OOSEMProjectImplData();
}