package hu.bme.mit.sysml.oosem.wizards.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

public abstract class BlockGenerationPage extends WizardPage{
	protected BlockGenerationPage(String pageName) {
		super(pageName);
	}

	public abstract void refreshDataFromUI();

	@Override
	public void createControl(Composite parent) {}

}
