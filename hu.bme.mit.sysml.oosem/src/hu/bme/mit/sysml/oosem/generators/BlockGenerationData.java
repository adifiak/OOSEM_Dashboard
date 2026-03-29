package hu.bme.mit.sysml.oosem.generators;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.model.elements.OOSEMFeature;
import hu.bme.mit.sysml.oosem.model.project.interfaces.OOSEMProject;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils.OOSEMBlockType;
import hu.bme.mit.sysml.oosem.util.OpenInFileUtils;

public class BlockGenerationData {
	public BlockGenerationData(OOSEMProject project, OOSEMBlock subject, OOSEMBlockType targetType) {
		this.project = project;
		this.subject = subject;
		this.targetType = targetType;
		this.blockName = subject.getName();
		path = OpenInFileUtils.getFileForEObject(subject.getObject()).getParent().getFullPath().toString();
		propertyRefinementData = new RefinementData(subject.getProperties());
		subsystemRefinementData = new RefinementData(subject.getSubsystems());
	}
	
	public String getBlockName() {
		return blockName;
	}
	public void setBlockName(String blockName) {
		this.blockName = blockName;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	public OOSEMBlock getSubject() {
		return subject;
	}
	
	public OOSEMBlockType getTagetType() {
		return targetType;
	}

	public OOSEMProject getProject() {
		return project;
	}
	
	public void registerPropertyRefinement(RefinementData.RefinementConfiguration config) {
		propertyRefinementData.registerRefinement(config);
	}
	
	public void registerSubsystemRefinement(RefinementData.RefinementConfiguration config) {
		subsystemRefinementData.registerRefinement(config);
	}
	
	public RefinementData getPropertyRefinementConfigs() {
		return propertyRefinementData;
	}
	
	public RefinementData getSubsystemRefinementConfigs() {
		return subsystemRefinementData;
	}

	private final OOSEMBlock subject;
	private String blockName = "";
	private final OOSEMBlockType targetType;
	private String path = "";
	private final OOSEMProject project;
	
	private final RefinementData propertyRefinementData;
	private final RefinementData subsystemRefinementData;
	
	public class RefinementData {
		public RefinementData(Set<OOSEMFeature> originalFeatures) {
			for(var of : originalFeatures) {
				configs.add(new RefinementConfiguration(of, null, ""));
			}
		}
		
		public void registerRefinement(RefinementConfiguration config) {
			if(configs.remove(config))
				configs.add(config);
		}
		
		public Set<RefinementConfiguration> getConfigurations() {
			return configs;
		}
		
		private final Set<RefinementConfiguration> configs = new HashSet<>();
		
		public static class RefinementConfiguration {
			public RefinementConfiguration(OOSEMFeature refinedFeature, OOSEMBlock type, String name) {
				this.refinedFeature = refinedFeature;
				this.type = type;
				this.name = name;
			}
			
			public OOSEMFeature getRefinedFeature() {
				return refinedFeature;
			}
			public OOSEMBlock getType() {
				return type;
			}
			public String getName() {
				return name;
			}
			public boolean requiresIntegration() {
				return !name.isEmpty() || type != null;
			}
			
			@Override
			public int hashCode() {
				return Objects.hash(refinedFeature);
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (!(obj instanceof RefinementConfiguration))
					return false;
				RefinementConfiguration other = (RefinementConfiguration) obj;
				return Objects.equals(refinedFeature, other.refinedFeature);
			}

			private final OOSEMFeature refinedFeature;
			private final OOSEMBlock type;
			private final String name;
		}
	}
}
