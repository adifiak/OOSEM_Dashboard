package hu.bme.mit.sysml.oosem.views.listeners;

import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;

public class OOSEMTreeViewerListener implements ITreeViewerListener {
	public OOSEMTreeViewerListener(ScrolledComposite scrolledComposite, Composite container, TreeViewer treeViewer) {
		this.scrolledComposite = scrolledComposite;
		this.container = container;
		this.treeViewer = treeViewer;
	}

	@Override
	public void treeExpanded(TreeExpansionEvent e) {
		asyncRelayout();
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent e) {
		asyncRelayout();
	}

	private void asyncRelayout() {
		treeViewer.getTree().getDisplay().asyncExec(() -> {
			if (!treeViewer.getTree().isDisposed()) {
				treeViewer.getTree().getParent().layout(true, true);
			}
		});
		scrolledComposite.getDisplay().asyncExec(() -> {
			if (!scrolledComposite.getDisplay().isDisposed()) {
				scrolledComposite.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				scrolledComposite.layout(true, true);
			}
		});
	}

	private ScrolledComposite scrolledComposite;
	private Composite container;
	private TreeViewer treeViewer;
}
