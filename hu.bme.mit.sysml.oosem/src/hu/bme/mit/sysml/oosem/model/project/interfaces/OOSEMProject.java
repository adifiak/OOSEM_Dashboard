package hu.bme.mit.sysml.oosem.model.project.interfaces;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.EObject;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMBlock;
import hu.bme.mit.sysml.oosem.model.project.implementations.BlockFamilyStructures;
import hu.bme.mit.sysml.oosem.model.project.implementations.OOSEMProjectImpl;

public interface OOSEMProject {

	Set<OOSEMBlock> getSpecifications();

	Set<OOSEMBlock> getDesigns();

	Set<OOSEMBlock> getIntegrations();

	BlockFamilyStructures getSpecificationsWithTheirDesigns();

	BlockFamilyStructures getDesignsWithTheirIntegrations();

	IProject getProject();

	List<EObject> getPossibleImplementationsOfSpecification(OOSEMBlock block);

}