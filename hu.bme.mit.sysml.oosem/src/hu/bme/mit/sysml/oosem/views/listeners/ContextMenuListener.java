package hu.bme.mit.sysml.oosem.views.listeners;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.omg.sysml.lang.sysml.OccurrenceDefinition;

import hu.bme.mit.sysml.oosem.model.OOSEMProject;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils;
import hu.bme.mit.sysml.oosem.util.OpenInFileUtils;
import hu.bme.mit.sysml.oosem.util.OOSEMUtils.OOSEMBlockType;
import hu.bme.mit.sysml.oosem.views.listeners.ContextMenuListener.addOptionToContextMenu;
import hu.bme.mit.sysml.oosem.wizards.blockGenerators.DesignToIntegrationWizard;
import hu.bme.mit.sysml.oosem.wizards.blockGenerators.SpecificationToDesignWizard;

public class ContextMenuListener {
	@FunctionalInterface
	public interface addOptionToContextMenu {
		void add(IMenuManager manager, Object item, OOSEMProject context);
	}
	
	public static IMenuListener getContextMenuListener(List<addOptionToContextMenu> contextMenuOptions, TreeViewer treeViewer, OOSEMProject oosemProject) {
		return manager -> {
			ITreeSelection selection = treeViewer.getStructuredSelection();
			Object obj = selection.getFirstElement();
			if (obj == null || contextMenuOptions.isEmpty()) return;

			for(var opt : contextMenuOptions) {
				opt.add(manager, obj, oosemProject);
			}
		};
	}
	
	public static class MenuOptions {
		public static void addShowInEditorToMenu(IMenuManager manager, Object item, OOSEMProject context) {
			if (item instanceof EObject eobj) {
				manager.add(new Action("Open in Editor") {
					public void run() {
						OpenInFileUtils.openEditorForEObject(eobj);
					}
				});
			}
		}

		public static void addDesignWizardToMenu(IMenuManager manager, Object item, OOSEMProject context) {
			if (item instanceof OccurrenceDefinition od && OOSEMUtils.getOOSEMBlockType(od) == OOSEMBlockType.SPECIFICATION) {
				var action = new Action("Generate Design Block") {
					public void run() {
						WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(),
								new SpecificationToDesignWizard(od, context));
						dialog.open();
					}
				};
				action.setEnabled(context.passedValidation(od));
				manager.add(action);
			}
		}
		
		public static void addIntegrationWizardToMenu(IMenuManager manager, Object item, OOSEMProject context) {
			if (item instanceof OccurrenceDefinition od && OOSEMUtils.getOOSEMBlockType(od) == OOSEMBlockType.DESIGN) {
				var action = new Action("Generate Integration Block") {
					public void run() {
						WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(),
								new DesignToIntegrationWizard(od, context));
						dialog.open();
					}
				};
				action.setEnabled(context.passedValidation(od));
				manager.add(action);
			}
		}
	}
}
