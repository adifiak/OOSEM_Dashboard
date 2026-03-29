package hu.bme.mit.sysml.oosem.views.listeners;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.omg.sysml.lang.sysml.Namespace;

import hu.bme.mit.sysml.oosem.model.elements.OOSEMElement;
import hu.bme.mit.sysml.oosem.model.project.interfaces.OOSEMProject;

public class ShowValidationResultsMouseTracctListener extends MouseTrackAdapter {
	public ShowValidationResultsMouseTracctListener(Tree tree, OOSEMProject oosemProject) {
		this.tree = tree;
	}
	
	@Override
	public void mouseHover(MouseEvent e) {
		TreeItem item = tree.getItem(new org.eclipse.swt.graphics.Point(e.x, e.y));
		if (item == null) {
			tree.setToolTipText(null);
			return;
		} else {
			var data = item.getData();
			if (data != null && data instanceof OOSEMElement block) {
				var errors = ((OOSEMElement)data).getValidationErrors();
				var warnings = ((OOSEMElement)data).getValidationWarnings();
				if (!errors.isEmpty()) {
					var toolTip = "Errors for " + ((Namespace)block.getObject()).getName() + ":";
					for (var err : errors) {
						toolTip = toolTip + "\n - " + err;
					}
					tree.setToolTipText(toolTip);
					return;
				} else if (!warnings.isEmpty()) {
					var toolTip = "Warnings for " + ((Namespace)block.getObject()).getName() + ":";
					for (var war : warnings) {
						toolTip = toolTip + "\n - " + war;
					}
					tree.setToolTipText(toolTip);
					return;
				}
			}
		}
		tree.setToolTipText(null);
	}
	
	private Tree tree;
}
