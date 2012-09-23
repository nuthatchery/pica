package org.magnolialang.resources;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.magnolialang.resources.eclipse.EclipseWorkspaceManager;
import org.magnolialang.util.Pair;

public final class WorkspaceManager {
	private static EclipseWorkspaceManager	manager;


	public static synchronized IWorkspaceManager getInstance() {
		if(manager == null)
			manager = EclipseWorkspaceManager.getInstance();
		return manager;
	}


	public static synchronized IResourceManager getManager(IProject project) {
		getInstance();
		return manager.getManager(project);
	}


	public static synchronized IResourceManager getManager(String project) {
		getInstance();
		return manager.getManager(project);
	}


	public static IPath getPath(URI uri) {
		if(uri.getScheme().equals("project"))
			return new Path("/" + uri.getAuthority() + "/" + uri.getPath());
		else
			return null;
	}


	public static Pair<Set<IManagedResource>, Set<IPath>> getResourcesForDelta(IResourceDelta delta) {
		getInstance();
		final Set<IManagedResource> changed = new HashSet<IManagedResource>();
		final Set<IPath> removed = new HashSet<IPath>();

		try {
			delta.accept(new IResourceDeltaVisitor() {
				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					if(delta != null && delta.getResource() instanceof IFile)
						switch(delta.getKind()) {
						case IResourceDelta.ADDED: {
							IManagedResource resource = manager.findResource(delta.getResource());
							if(resource != null)
								changed.add(resource);
							break;
						}
						case IResourceDelta.CHANGED: {
							IManagedResource resource = manager.findResource(delta.getResource());
							if(resource != null)
								changed.add(resource);
							break;
						}
						case IResourceDelta.REMOVED:
							removed.add(delta.getResource().getFullPath());
							break;
						default:
							throw new UnsupportedOperationException("Resource change on " + delta.getFullPath() + ": " + delta.getKind());
						}
					return true;
				}
			});
		}
		catch(CoreException e) {
			e.printStackTrace();
		}

		return new Pair<Set<IManagedResource>, Set<IPath>>(changed, removed);
	}


	public static synchronized void stopInstance() {
		if(manager != null)
			manager.stop();
	}


	private WorkspaceManager() {
	}
}
