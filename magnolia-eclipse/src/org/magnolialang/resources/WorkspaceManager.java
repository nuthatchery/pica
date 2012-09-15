package org.magnolialang.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.magnolialang.eclipse.MagnoliaPlugin;
import org.magnolialang.resources.internal.ProjectManager;

public final class WorkspaceManager implements IResourceChangeListener, IWorkspaceManager {
	private static WorkspaceManager					instance;
	private static Map<String, ProjectManager>		projects		= new HashMap<String, ProjectManager>();
	private final List<IManagedResourceListener>	listeners		= new ArrayList<IManagedResourceListener>();
	private final List<IProject>					closingProjects	= new ArrayList<IProject>();
	private final static boolean					debug			= false;
	private List<Change>							changeQueue		= new ArrayList<Change>();


	private WorkspaceManager() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		initialize();
	}


	private void initialize() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects(0);
		for(IProject proj : projects) {
			if(proj.isOpen()) {
				try {
					openProject(proj);
				}
				catch(CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}


	public static synchronized WorkspaceManager getInstance() {
		if(instance == null)
			instance = new WorkspaceManager();
		return instance;
	}


	public static synchronized IResourceManager getManager(IProject project) {
		getInstance();
		return projects.get(project.getName());
	}


	public static synchronized IResourceManager getManager(String project) {
		getInstance();
		return projects.get(project);
	}


	@Override
	public synchronized void resourceChanged(IResourceChangeEvent event) {
		if(event.getType() == IResourceChangeEvent.POST_CHANGE) {
			IResourceDelta delta = event.getDelta();
			// System.err.println("PROCESSING RESOURCE CHANGE EVENT");
			try {
				delta.accept(new IResourceDeltaVisitor() {
					@Override
					public boolean visit(IResourceDelta delta) throws CoreException {
						if(delta != null) {
							try {
								switch(delta.getKind()) {
								case IResourceDelta.ADDED:
									if(debug)
										System.out.println(delta.getFullPath().toString() + " ADDED");
									addResource(delta.getResource());
									break;
								case IResourceDelta.CHANGED:
									if(delta.getFlags() == IResourceDelta.MARKERS || delta.getFlags() == IResourceDelta.NO_CHANGE) {
										if(debug)
											System.out.println(delta.getFullPath().toString() + " NO CHANGE");
									}
									else {
										if(debug)
											System.out.println(delta.getFullPath().toString() + " CHANGED");
										// only if its not just the markers
										resourceChanged(delta);
									}
									break;
								case IResourceDelta.REMOVED:
									if(debug)
										System.out.println(delta.getFullPath().toString() + " REMOVED");
									removeResource(delta.getResource());
									break;
								default:
									throw new UnsupportedOperationException("Resource change on " + delta.getFullPath() + ": " + delta.getKind());
								}
							}
							catch(CoreException e) {
								MagnoliaPlugin.getInstance().logException("CoreException while processing " + delta.getFullPath(), e);
								e.printStackTrace();
							}
							catch(Throwable t) {
								MagnoliaPlugin.getInstance().logException("INTERNAL ERROR IN RESOURCE MANAGER (for " + delta.getFullPath() + ")", t);
								t.printStackTrace();
							}

						}

						return true;
					}
				});
			}
			catch(CoreException e) {
				e.printStackTrace();
			}
			for(IProject p : closingProjects) {
				try {
					if(p != null)
						closeProject(p);
				}
				catch(Throwable t) {
					MagnoliaPlugin.getInstance().logException("INTERNAL ERROR IN RESOURCE MANAGER (for " + p + ")", t);
					t.printStackTrace();
				}
			}
			closingProjects.clear();

			if(!listeners.isEmpty() && !changeQueue.isEmpty()) {
				Job job = new ChangeQueueRunner(changeQueue, listeners);
				job.schedule();
			}
			if(!changeQueue.isEmpty())
				changeQueue = new ArrayList<Change>();
		}
		// System.err.println("FINISHED PROCESSING RESOURCE CHANGE EVENT");
		dataInvariant();
	}


	private void removeResource(IResource resource) {
		if(resource.getType() == IResource.PROJECT) {
			closeProject((IProject) resource);
		}
		else {
			IProject project = resource.getProject();
			URI uri = MagnoliaPlugin.constructProjectURI(project, resource.getProjectRelativePath());
			if(project != null) {
				ProjectManager manager = projects.get(project.getName());
				if(manager != null)
					manager.onResourceRemoved(uri);
			}
			changeQueue.add(new Change(uri, Change.Kind.REMOVED));
		}
	}


	private void resourceChanged(IResourceDelta delta) throws CoreException {
		IPath path = delta.getFullPath();
		int flags = delta.getFlags();

		if((flags & (IResourceDelta.TYPE | IResourceDelta.REPLACED)) != 0) {
			removeResource(delta.getResource());
			addResource(delta.getResource());
		}

		if((flags & IResourceDelta.OPEN) != 0) {
			IProject proj = (IProject) delta.getResource();
			if(proj.isOpen())
				openProject(proj);
			else
				closingProjects.add(proj);

		}

		if((flags & IResourceDelta.LOCAL_CHANGED) != 0) {
			System.err.println("LOCAL_CHANGED: " + path);
		}
		if((flags & IResourceDelta.CONTENT) != 0 || (flags & IResourceDelta.ENCODING) != 0) {
			if(debug)
				System.err.println("RESOURCE CHANGED: " + path);
			IResource resource = delta.getResource();
			IProject project = resource.getProject();
			URI uri = MagnoliaPlugin.constructProjectURI(project, resource.getProjectRelativePath());
			if(project != null) {
				ProjectManager manager = projects.get(project.getName());
				if(manager != null)
					manager.onResourceChanged(uri);
			}
			changeQueue.add(new Change(uri, Change.Kind.CHANGED));
		}

	}


	private void openProject(IProject project) throws CoreException {
		// TODO: check nature
		projects.put(project.getName(), new ProjectManager(this, project));

	}


	private void closeProject(IProject project) {
		// TODO: check nature
		IResourceManager manager = projects.remove(project.getName());
		if(manager != null)
			manager.dispose();
	}


	private URI addResource(IResource resource) throws CoreException {
		if(resource.getType() == IResource.PROJECT) {
			openProject((IProject) resource);
			return null;
		}
		else {
			IProject project = resource.getProject();
			URI uri = MagnoliaPlugin.constructProjectURI(project, resource.getProjectRelativePath());
			if(project != null) {
				ProjectManager manager = projects.get(project.getName());
				if(manager != null)
					manager.onResourceAdded(resource);
			}
			changeQueue.add(new Change(uri, Change.Kind.ADDED));
			return uri;
		}
	}


	public void dataInvariant() {
	}


	@Override
	public void addListener(IManagedResourceListener listener) {
		listeners.add(listener);
	}


	@Override
	public void removeListener(IManagedResourceListener listener) {
		listeners.remove(listener);
	}


	@Override
	public void dispose() {
		// do nothing
	}


	@Override
	public boolean hasURI(URI uri) {
		if(uri.getScheme().equals("project"))
			return true;

		IFile file = MagnoliaPlugin.getFileHandle(uri);
		return file != null;
	}


	@Override
	public URI getURI(String path) throws URISyntaxException {
		IPath p = new Path(path);
		String project = p.segment(0);
		p = p.removeFirstSegments(1);
		return new URI("project", project, p.toString(), null);
	}


	public static IPath getPath(URI uri) {
		if(uri.getScheme().equals("project"))
			return new Path("/" + uri.getHost() + "/" + uri.getPath());
		else
			return null;
	}


	static class Change {
		enum Kind {
			ADDED, REMOVED, CHANGED
		};

		final Kind	kind;
		final URI	uri;


		Change(URI uri, Kind kind) {
			this.uri = uri;
			this.kind = kind;
		}
	}


	static class ChangeQueueRunner extends Job {
		private final List<Change>						changeQueue;
		private final List<IManagedResourceListener>	listeners;


		public ChangeQueueRunner(List<Change> changeQueue, List<IManagedResourceListener> listeners) {
			super("Do change notifications");
			this.changeQueue = changeQueue;
			this.listeners = listeners;
		}


		@Override
		protected IStatus run(IProgressMonitor monitor) {
			for(Change c : changeQueue) {
				switch(c.kind) {
				case ADDED:
					for(IManagedResourceListener l : listeners)
						l.resourceAdded(c.uri);
					break;
				case CHANGED:
					for(IManagedResourceListener l : listeners)
						l.resourceChanged(c.uri);
					break;
				case REMOVED:
					for(IManagedResourceListener l : listeners)
						l.resourceRemoved(c.uri);
					break;
				}
			}
			return Status.OK_STATUS;
		}
	}

}
