package hu.bme.mit.kerml.atomizer.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ISetSelectionTarget;

public class OpenInFileUtils {
	public static void openEditorForEObject(EObject eObject) {
		IFile file = getFileForEObject(eObject);
		if (file == null)
			return;

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		try {
			IEditorPart editor = IDE.openEditor(page, file);

	        if (editor instanceof ISetSelectionTarget) {
	            ((ISetSelectionTarget) editor).selectReveal(new StructuredSelection(eObject));
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
