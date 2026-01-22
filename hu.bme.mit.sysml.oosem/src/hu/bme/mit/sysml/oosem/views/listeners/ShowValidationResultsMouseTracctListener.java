package hu.bme.mit.sysml.oosem.views.listeners;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.omg.sysml.lang.sysml.Type;

import hu.bme.mit.sysml.oosem.model.OOSEMProject;

public class ShowValidationResultsMouseTracctListener extends MouseTrackAdapter {
	public ShowValidationResultsMouseTracctListener(Tree tree, OOSEMProject oosemProject) {
		this.tree = tree;
		this.oosemProject = oosemProject;
	}
	
	@Override
	public void mouseHover(MouseEvent e) {
		TreeItem item = tree.getItem(new org.eclipse.swt.graphics.Point(e.x, e.y));
		if (item == null) {
			tree.setToolTipText(null);
			return;
		} else {
			var data = item.getData();
			if (data != null && data instanceof Type t) {
				var errors = oosemProject.getValidationErrors().get(data);
				var warnings = oosemProject.getValidationWarnings().get(data);
				if (errors != null) {
					var toolTip = "Errors for " + t.getName() + ":";
					for (var err : errors) {
						toolTip = toolTip + "\n - " + err;
					}
					tree.setToolTipText(toolTip);
					return;
				} else if (warnings != null) {
					var toolTip = "Warnings for " + t.getName() + ":";
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
	private OOSEMProject oosemProject;
}
