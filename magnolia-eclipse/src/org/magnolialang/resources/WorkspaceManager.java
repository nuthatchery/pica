package org.magnolialang.resources;

import java.io.PrintWriter;
import java.net.URI;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.pdb.facts.IValue;
import org.magnolialang.eclipse.MagnoliaPlugin;
import org.magnolialang.magnolia.Magnolia;
import org.magnolialang.resources.internal.ProjectManager;
import org.magnolialang.tasks.INameFormatter;
import org.magnolialang.tasks.Transaction;
import org.magnolialang.terms.TermAdapter;

@edu.umd.cs.findbugs.annotations.SuppressWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
public final class WorkspaceManager implements IResourceChangeListener, IWorkspaceManager {
	private static WorkspaceManager					instance;
	private static Map<String, ProjectManager>		projects		= new HashMap<String, ProjectManager>();
	private Transaction								tr;
	private final List<IManagedResourceListener>	listeners		= new ArrayList<IManagedResourceListener>();
	private final List<IProject>					closingProjects	= new ArrayList<IProject>();
	private final static boolean					debug			= false;


	private WorkspaceManager() {
		initializeTransaction();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		initialize();
	}


	private void initialize() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects(0);
		for(IProject proj : projects) {
			if(proj.isOpen()) {
				openProject(proj);
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
				// TODO Auto-generated catch block
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

		}
		// System.err.println("FINISHED PROCESSING RESOURCE CHANGE EVENT");
		dataInvariant();
	}


	protected void removeResource(IResource resource) {
		if(resource.getType() == IResource.PROJECT) {
			closeProject((IProject) resource);
		}
		else {
			IPath path = resource.getFullPath();
			IProject project = resource.getProject();
			if(project != null) {
				ProjectManager manager = projects.get(project.getName());
				if(manager != null)
					manager.onResourceRemoved(resource.getFullPath());
			}
			for(IManagedResourceListener l : listeners)
				l.resourceRemoved(path);
		}
	}


	private void resourceChanged(IResourceDelta delta) {
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
			if(project != null) {
				ProjectManager manager = projects.get(project.getName());
				if(manager != null)
					manager.onResourceChanged(resource.getFullPath());
			}
			for(IManagedResourceListener l : listeners)
				l.resourceChanged(path);
		}

	}


	private void openProject(IProject project) {
		// TODO: check nature
		projects.put(project.getName(), new ProjectManager(this, project));

	}


	private void closeProject(IProject project) {
		// TODO: check nature
		IWorkspaceManager manager = projects.remove(project.getName());
		if(manager != null)
			manager.dispose();
	}


	protected IPath addResource(IResource resource) {
		if(resource.getType() == IResource.PROJECT) {
			openProject((IProject) resource);
			return null;
		}
		else {
			IPath path = resource.getFullPath();
			IProject project = resource.getProject();
			if(project != null) {
				ProjectManager manager = projects.get(project.getName());
				if(manager != null)
					manager.onResourceAdded(resource);
			}
			for(IManagedResourceListener l : listeners)
				l.resourceAdded(path);
			return path;
		}
	}


	@Override
	public Transaction getTransaction() {
		return tr;
	}


	private void initializeTransaction() {
		PrintWriter stderr = new PrintWriter(System.err);
		// new PrintWriter(RuntimePlugin.getInstance().getConsoleStream());
		if(tr != null)
			tr.abandon();
		tr = new Transaction(new INameFormatter() {
			@Override
			public String format(IValue name) {
				if(TermAdapter.isCons(name, "QName") || TermAdapter.isCons(name, "Name"))
					return Magnolia.yieldName(name);

				return name.toString();
			}
		}, stderr, null);
	}


	public void dataInvariant() {
	}


	@Override
	public IManagedResource find(IPath path) {
		String projectName = path.segment(0);
		if(projectName != null) {
			IResourceManager manager = projects.get(projectName);
			if(manager != null)
				return manager.find(path);
		}
		return null;
	}


	@Override
	public IManagedResource find(IProject project, IPath path) {
		IResourceManager manager = projects.get(project.getName());
		if(manager != null)
			return manager.find(path);
		return null;
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
	public IPath getPath(URI uri) {
		if(uri.getScheme().equals("project")) {
			return new Path(uri.getHost()).append(uri.getPath());
		}

		IFile file = MagnoliaPlugin.getFileHandle(uri);
		if(file != null)
			return file.getFullPath();

		throw new IllegalArgumentException("URI not handled, or path is outside the workspace: " + uri);
	}


	@Override
	public boolean hasPath(URI uri) {
		if(uri.getScheme().equals("project"))
			return true;

		IFile file = MagnoliaPlugin.getFileHandle(uri);
		return file != null;
	}


	@Override
	public IPath getPath(String path) {
		IPath p = new Path(path);
		if(p.isAbsolute())
			return p;

		return ResourcesPlugin.getWorkspace().getRoot().getFullPath().append(p);
	}

}
