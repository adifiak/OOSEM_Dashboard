package hu.bme.mit.sysml.oosem.jobs;


import java.util.Collection;

import org.eclipse.emf.ecore.EObject;

import hu.bme.mit.kerml.atomizer.jobs.OOSEMVisualizer;

public class Main {
	public void run(EObject root, Collection<EObject> roots) {
		//ObjectProcessor.executeTarget(root, roots);
		OOSEMVisualizer.processNode(root);
		
		//System.out.println(ExtentManager.getInstance().printState());
		//System.out.println(Atomizer.toKermlModel());

		//ExtentManager.getInstance().reset();
		//Atom.reset();
	}
}
