package hu.bme.mit.sysml.oosem.model.project.interfaces;

import java.util.Set;

import org.eclipse.core.resources.IProject;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.model.project.implementations.BlockFamilyStructures;

public interface OOSEMProject {

	Set<OOSEMBlock> getSpecifications();

	Set<OOSEMBlock> getDesigns();

	Set<OOSEMBlock> getIntegrations();

	BlockFamilyStructures getSpecificationsWithTheirDesigns();

	BlockFamilyStructures getDesignsWithTheirIntegrations();

	IProject getProject();

}