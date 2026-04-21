package hu.bme.mit.sysml.oosem.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import org.eclipse.xtext.ui.editor.XtextEditor;

public class OpenInFileUtils {
	public static void openEditorForEObject(EObject eObject) {
		IFile file = getFileForEObject(eObject);
		if (file == null)
			return;

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		try {
			IEditorPart editor = IDE.openEditor(page, file);

			if (editor instanceof XtextEditor) {
			    XtextEditor xtextEditor = (XtextEditor) editor;

			    xtextEditor.getDocument().readOnly(resource -> {
			        EObject target = resource.getEObject(EcoreUtil.getURI(eObject).fragment());
			        INode node = NodeModelUtils.findActualNodeFor(target);
			        if (node != null) {
			            xtextEditor.selectAndReveal(node.getOffset(), node.getLength());
			        }
			        return null;
			    });
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	public static IFile getFileForEObject(EObject eObject) {
		Resource resource = eObject.eResource();
		if (resource == null)
			return null;

		URI uri = resource.getURI();
		if (uri.isPlatformResource()) {
			String platformString = uri.toPlatformString(true);
			return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(platformString));
		}

		return null;
	}
}
