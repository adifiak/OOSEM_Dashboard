package hu.bme.mit.sysml.oosem.views.listeners;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import hu.bme.mit.sysml.oosem.jobs.RefreshViewJob;
import hu.bme.mit.sysml.oosem.views.OOSEMModelTreeView;

public class ProjectBuildFinishedListener implements IResourceChangeListener {

    public ProjectBuildFinishedListener(OOSEMModelTreeView view, IProject project) {
        this.view = view;
    	this.project = project;
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_BUILD); // register listener
    }

    public void unregister() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (event.getType() != IResourceChangeEvent.POST_BUILD) return;

        IResourceDelta delta = event.getDelta();
        if (delta == null) return;

        try {
            delta.accept(d -> {
                if (d.getResource() instanceof IProject p && p.equals(project)) {
                    onProjectBuildFinished();
                    return false; // stop visiting children
                }
                return true;
            });
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    protected void onProjectBuildFinished() {
    	RefreshViewJob.createRevreshJob(view).schedule();
    }
    
    private final IProject project;
    private final OOSEMModelTreeView view;
}
