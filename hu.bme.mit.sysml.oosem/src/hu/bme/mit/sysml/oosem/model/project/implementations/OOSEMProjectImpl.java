package hu.bme.mit.sysml.oosem.model.project.implementations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.model.elements.OOSEMFeature;
import hu.bme.mit.sysml.oosem.model.project.interfaces.OOSEMProject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;

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

	private final IProject project;
	private OOSEMProjectImplData data = new OOSEMProjectImplData();
}