package hu.bme.mit.sysml.oosem.views;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import hu.bme.mit.sysml.oosem.model.project.interfaces.OOSEMProject;

public abstract class OOSEMView extends TabItem{

	public OOSEMView(TabFolder parent, int style) {
		super(parent, style);
		// TODO Auto-generated constructor stub
	}
	
	public void setProject(OOSEMProject project) {
		this.project = project;
		refresh();
	}
	
	abstract public void refresh();
	
	private OOSEMProject project;
}
