package org.magnolialang.resources;

import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.pdb.facts.IValue;
import org.magnolialang.eclipse.MagnoliaPlugin;
import org.magnolialang.errors.ImplementationError;
import org.magnolialang.magnolia.Magnolia;
import org.magnolialang.resources.internal.FileFact;
import org.magnolialang.resources.internal.ProjectManager;
import org.magnolialang.terms.TermAdapter;
import org.rascalmpl.tasks.IDependencyListener.Change;
import org.rascalmpl.tasks.INameFormatter;
import org.rascalmpl.tasks.Transaction;

@edu.umd.cs.findbugs.annotations.SuppressWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
public final class ResourceManager implements IResourceChangeListener, IResourceManager {
	private static ResourceManager					instance;
	private static Map<String, IModuleManager>		projects		= new HashMap<String, IModuleManager>();
	private Transaction								tr;
	private final Map<IPath, IManagedResource>		resources		= new HashMap<IPath, IManagedResource>();
	private final Map<IProject, Set<IPath>>			projectContents	= new HashMap<IProject, Set<IPath>>();
	private final List<IManagedResourceListener>	listeners		= new ArrayList<IManagedResourceListener>();
	private final List<IProject>					closingProjects	= new ArrayList<IProject>();
	private final static boolean					debug			= false;


	private ResourceManager() {
		initializeTransaction();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		initialize();
	}


	private void initialize() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects(0);
		for(IProject proj : projects) {
			if(proj.isOpen()) {
				addProjectFiles(proj);
				openProject(proj);
			}
		}

	}


	public static synchronized ResourceManager getInstance() {
		if(instance == null)
			instance = new ResourceManager();
		return instance;
	}


	public static synchronized IModuleManager getManager(IProject project) {
		getInstance();
		return projects.get(project.getName());
	}


	public static synchronized IModuleManager getManager(String project) {
		getInstance();
		return projects.get(project);
	}


	public Set<IPath> getProjectPaths(IProject project) {
		return projectContents.get(project);
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
		IPath path = resource.getFullPath();
		if(resource.getType() == IResource.PROJECT) {
			closeProject((IProject) resource);
		}
		if(resources.containsKey(path)) {
			for(IManagedResourceListener l : listeners)
				l.resourceRemoved(path);
			if(debug)
				System.err.println("RESOURCE REMOVED: " + path);
			resources.get(path).remove();
			resources.remove(path);
			Set<IPath> set = projectContents.get(resource.getProject());
			if(set != null)
				set.remove(path);
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
		if(resources.containsKey(path)) {
			IManagedResource resource = resources.get(path);
			if((flags & IResourceDelta.CONTENT) != 0 || (flags & IResourceDelta.ENCODING) != 0) {
				if(debug)
					System.err.println("RESOURCE CHANGED: " + path);
				resource.changed(null, Change.CHANGED, null);
			}

		}
	}


	private void addProjectFiles(IProject project) {
		final Set<IPath> paths = new HashSet<IPath>();
		try {
			project.accept(new IResourceVisitor() {
				@Override
				public boolean visit(IResource resource) throws CoreException {
					if(resource.getType() == IResource.FILE) {
						IPath path = addResource(resource);
						if(path != null)
							paths.add(path);
					}
					return true;
				}

			});

			projectContents.put(project, paths);
		}
		catch(CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void openProject(IProject project) {
		// TODO: check nature
		Set<IPath> paths = projectContents.get(project);
		if(paths == null)
			paths = new HashSet<IPath>();
		projectContents.put(project, paths);
		projects.put(project.getName(), new ProjectManager(this, project, paths));

	}


	private void closeProject(IProject project) {
		// TODO: check nature
		IResourceManager manager = projects.get(project.getName());
		if(manager != null)
			manager.dispose();
		projectContents.remove(project);

	}


	protected IPath addResource(IResource resource) {
		IPath path = resource.getFullPath();
		if(resources.containsKey(path)) {
			System.err.println("Spurious resource add: " + path);
		}
		else {
			int type = resource.getType();
			IManagedResource res = null;
			if(type == IResource.FILE) {
				ILanguage lang = LanguageRegistry.getLanguageForFile((IFile) resource);
				if(lang != null) {
					res = new FileFact(this, (IFile) resource, lang);
					Set<IPath> set = projectContents.get(resource.getProject());
					if(set != null)
						set.add(path);
				}
			}
			else if(type == IResource.PROJECT) {
				openProject((IProject) resource);
			}

			if(res != null) {
				if(debug)
					System.err.println("RESOURCE ADDED:   " + path);
				resources.put(path, res);

				for(IManagedResourceListener l : listeners)
					l.resourceAdded(path);
				return path;
			}
		}

		return null;
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
		}, stderr);
	}


	public void dataInvariant() {
		for(Map.Entry<IProject, Set<IPath>> entry : projectContents.entrySet()) {
			for(IPath p : entry.getValue()) {
				if(resources.get(p) == null)
					throw new ImplementationError("Data invariant check failed: " + p + " is member of project " + entry.getKey() + ", but is not in list of resources");
			}
		}
	}


	@Override
	public IManagedResource find(IPath path) {
		return resources.get(path);
	}


	@Override
	public IManagedResource find(IProject project, IPath path) {
		return resources.get(project.getFullPath().append(path));
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
