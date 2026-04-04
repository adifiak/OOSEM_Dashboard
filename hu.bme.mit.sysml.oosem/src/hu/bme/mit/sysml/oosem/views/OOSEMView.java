package hu.bme.mit.sysml.oosem.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import hu.bme.mit.sysml.oosem.model.project.interfaces.OOSEMProject;

public abstract class OOSEMView{

	public OOSEMView(TabFolder parent) {
		tab = new TabItem(parent, SWT.NONE);
		
		container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tab.setControl(container);
		
		GridLayout layout = new GridLayout(3, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		container.setLayout(layout);
	}
	
	public void setProject(OOSEMProject project) {
		this.project = project;
		refresh();
	}
	
	abstract public void refresh();
	
	protected void clearView() {
		Display.getDefault().syncExec(() -> {
			for (var child : container.getChildren()) {
				child.dispose();
			}
		});
	}
	
	protected OOSEMProject project;
	protected TabItem tab;
	
	protected Composite container;
}
