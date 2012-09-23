package org.magnolialang.resources.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.magnolialang.eclipse.MagnoliaPlugin;
import org.magnolialang.errors.ErrorMarkers;
import org.magnolialang.errors.ImplementationError;
import org.magnolialang.magnolia.resources.MagnoliaPackage;
import org.magnolialang.nullness.Nullable;
import org.magnolialang.resources.ILanguage;
import org.magnolialang.resources.IManagedPackage;
import org.magnolialang.resources.IManagedResource;
import org.magnolialang.resources.IResourceManager;
import org.magnolialang.resources.IWorkspaceManager;
import org.magnolialang.resources.LanguageRegistry;
import org.magnolialang.resources.WorkspaceManager;
import org.magnolialang.util.depgraph.IDepGraph;
import org.magnolialang.util.depgraph.IWritableDepGraph;
import org.magnolialang.util.depgraph.UnsyncedDepGraph;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.interpreter.IRascalMonitor;

public final class ProjectManager implements IResourceManager {
	private final Object								changeLock		= new Object();
	private final IWorkspaceManager						manager;
	private volatile IResources							resources		= null;
	private final IProject								project;
	private final IPath									basePath;
	private static final String							MODULE_LANG_SEP	= "%";
	private static final String							OUT_FOLDER		= "cxx";

	private static final boolean						debug			= true;

	private final IPath									srcPath;

	private final IPath									outPath;

	private final List<EclipseWorkspaceManager.Change>	changeQueue		= new ArrayList<EclipseWorkspaceManager.Change>();

	private final Job									initJob;


	public ProjectManager(IWorkspaceManager manager, IProject project) throws CoreException {

		this.manager = manager;
		this.project = project;
		this.basePath = project.getFullPath();
		srcPath = null;
		outPath = project.getFolder(OUT_FOLDER).getFullPath();
		System.err.println("New projectmanager: basepath=" + basePath);
		addAllResources();

		initJob = new Job("Computing dependencies for " + project.getName()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IRascalMonitor rm = new RascalMonitor(monitor);
				processChanges(rm);
				dataInvariant();
				return Status.OK_STATUS;
			}
		};
		initJob.setRule(project);
		initJob.schedule();
		// depGraphCheckerJob.schedule(DEP_GRAPH_CHECKER_JOB_DELAY);
	}


	@Override
	public <T> T accept(IValueVisitor<T> v) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void addMarker(String message, ISourceLocation loc) {
		addMarker(message, loc, ErrorMarkers.TYPE_DEFAULT, ErrorMarkers.SEVERITY_DEFAULT_NUMBER);
	}


	@Override
	public void addMarker(String message, ISourceLocation loc, int severity) {
		addMarker(message, loc, ErrorMarkers.TYPE_DEFAULT, severity);
	}


	@Override
	public void addMarker(String message, ISourceLocation loc, String markerType) {
		addMarker(message, loc, markerType, ErrorMarkers.SEVERITY_DEFAULT_NUMBER);
	}


	private void ensureInit() {
		if(resources == null)
			try {
				initJob.join();
			}
			catch(InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if(resources == null)
			throw new ImplementationError("Project manager for " + project.getName() + " not initialized");
	}


	@Override
	public void addMarker(String message, ISourceLocation loc, String markerType, int severity) {
		ensureInit();

		IManagedResource pkg;
		if(loc == null)
			throw new ImplementationError("Missing location on marker add: " + message);

		URI uri = loc.getURI();

		pkg = resources.getResource(uri);

		if(pkg instanceof IManagedPackage)
			((IManagedPackage) pkg).addMarker(message, loc, markerType, severity);
		else
			throw new ImplementationError(message + "\nat location " + loc + " (pkg not found)");
	}


	@Override
	public Iterable<IManagedResource> allFiles() {
		ensureInit();
		return resources.allResources();
	}


	@Override
	public Collection<IManagedPackage> allPackages(final ILanguage language) {
		ensureInit();
		List<IManagedPackage> list = new ArrayList<IManagedPackage>();
		for(IManagedResource res : resources.allResources())
			if(res instanceof IManagedPackage && ((IManagedPackage) res).getLanguage().equals(language))
				list.add((IManagedPackage) res);
		return list;
	}


	@Override
	public void dispose() {
		resources = null;
		dataInvariant();
	}


	@Override
	@Nullable
	public IManagedPackage findPackage(ILanguage language, IConstructor moduleId) {
		ensureInit();
		return resources.getPackage(language.getId() + MODULE_LANG_SEP + language.getNameString(moduleId));
	}


	@Override
	@Nullable
	public IManagedPackage findPackage(ILanguage language, String moduleName) {
		ensureInit();
		return resources.getPackage(language.getId() + MODULE_LANG_SEP + moduleName);
	}


	@Override
	public IManagedPackage findPackage(URI uri) {
		ensureInit();
		IManagedResource resource = resources.getResource(uri);
		if(resource instanceof IManagedPackage)
			return (IManagedPackage) resource;
		else
			return null;
	}


	@Override
	public IManagedResource findResource(String path) {
		ensureInit();
		URI uri = MagnoliaPlugin.constructProjectURI(project, new Path(path));
		System.err.println("find: uri: " + uri);
		IManagedResource resource = findResource(uri);

		System.err.println("find: pkg: " + resource);
		return resource;
	}


	@Override
	public IManagedResource findResource(URI uri) {
		ensureInit();
		// see if we already track the URI
		IManagedResource res = resources.getResource(uri);
		if(res != null)
			return res;

		String scheme = uri.getScheme();

		// check if it is a project URI
		if(scheme.equals("project")) {
			if(uri.getAuthority().equals(project.getName()))
				return null; // we should already have found it if we were tracking it
			else {
				IResourceManager mng = WorkspaceManager.getManager(uri.getAuthority());
				if(mng != null)
					return mng.findResource(uri);
				else
					return null;
			}
		}
		else if(scheme.equals("magnolia"))
			return null; // not handled yet
		// see if we can find it using Eclipse's pkg system
		try {
			IFileStore store = EFS.getStore(uri);
			return findResource(uri, store);
		}
		catch(CoreException e) {

		}

		// give up
		return null;
	}


	@Override
	public Collection<IManagedResource> getChildren(IRascalMonitor rm) {
		ensureInit();
		return new ArrayList<IManagedResource>(resources.allResources());
	}


	@Override
	public long getModificationStamp() {
		return project.getModificationStamp(); // unlocked access ok
	}


	@Override
	public IPath getOutFolder() {
		return outPath;
	}


	/**
	 * Wait for any pending updates to the dependency graph and return a copy of
	 * the dependency graph.
	 * 
	 * Note that, unless the workspace is locked, new changes may be pending
	 * when this method returns.
	 * 
	 * @param rm
	 */
	@Override
	public IDepGraph<IManagedPackage> getPackageDependencyGraph(IRascalMonitor rm) {
		ensureInit();
		IDepGraph<IManagedPackage> depGraph = resources.getDepGraph();
		if(depGraph != null)
			return depGraph;

		// if not found, wait for processChanges() to finish if it is running
		synchronized(changeLock) {
			depGraph = resources.getDepGraph();
			return depGraph;
		}

	}


	@Override
	public Set<IManagedPackage> getPackageTransitiveDependents(IManagedPackage pkg, IRascalMonitor rm) {
		ensureInit();
		Set<IManagedPackage> dependents = resources.getDepGraph().getTransitiveDependents(pkg);
		if(dependents != null)
			return dependents;
		else
			return Collections.EMPTY_SET;
	}


	@Override
	public IManagedResource getParent() {
		return null;
	}


	@Override
	public IWorkspaceManager getResourceManager() {
		return manager;
	}


	@Override
	public IPath getSrcFolder() {
		if(srcPath == null) {
			IResource src = project.findMember("src");
			if(src != null && src.getType() == IResource.FOLDER)
				return src.getFullPath();
			else
				return basePath;
		}
		else
			return srcPath;
	}


	@Override
	public Type getType() {
		return IManagedResource.ResourceType;
	}


	@Override
	public URI getURI() {
		return MagnoliaPlugin.constructProjectURI(project, new Path("/"));
	}


	@Override
	public URI getURI(String path) throws URISyntaxException {
		return MagnoliaPlugin.constructProjectURI(project, new Path(path));
	}


	@Override
	public boolean isCodeUnit() {
		return false;
	}


	@Override
	public boolean isContainer() {
		return true;
	}


	@Override
	public boolean isEqual(IValue other) {
		return this == other;
	}


	@Override
	public boolean isFile() {
		return false;
	}


	@Override
	public boolean isProject() {
		return true;
	}


	@Override
	public void onResourceChanged() {
	}


	public void printDepGraph() {
		ensureInit();
		IDepGraph<IManagedPackage> depGraph = resources.getDepGraph();
		System.err.flush();
		System.out.flush();
		System.out.println("DEPENDENCY GRAPH FOR PROJECT " + project.getName());
		for(IManagedPackage pkg : depGraph.topological()) {
			System.out.print("\t  " + pkg.getName() + " <- ");
			for(IManagedPackage dep : depGraph.getDependents(pkg))
				System.out.print(dep.getName() + " ");
			System.out.println();
		}
	}


	public void queueChanges(List<EclipseWorkspaceManager.Change> list) {
		synchronized(changeQueue) {
			changeQueue.addAll(list);
		}
	}


	@Override
	public boolean processChanges(IRascalMonitor rm) {
		synchronized(changeLock) {
			IResources oldResources;
			IDepGraph<IManagedPackage> depGraph;
			oldResources = resources;
			if(oldResources == null)
				oldResources = new Resources();
			depGraph = oldResources.getDepGraph();

			List<EclipseWorkspaceManager.Change> changes;
			synchronized(changeQueue) {
				changes = new ArrayList<EclipseWorkspaceManager.Change>(changeQueue);
				changeQueue.clear();
			}
			if(changes.isEmpty())
				return false;

			rm.startJob("Processing workspace changes for " + project.getName(), 10, changes.size() * 2 + oldResources.numPackages() * 10);
			IWritableResources rs = null;
			for(EclipseWorkspaceManager.Change change : changes) {
				rm.event(2);
				switch(change.kind) {
				case ADDED:
					if(rs == null)
						rs = oldResources.createNewVersion();
					resourceAdded(change.resource, rs, depGraph);
					break;
				case CHANGED:
					resourceChanged(change.uri, rs != null ? rs : oldResources, depGraph);
					break;
				case REMOVED:
					if(rs == null)
						rs = oldResources.createNewVersion();
					resourceRemoved(change.uri, rs, depGraph);
					break;
				}
			}

			if(rs != null)
				resources = rs;

			rm.todo(resources.numPackages() * 10);
			IDepGraph<IManagedPackage> graph = constructDepGraph(resources, rm);
			resources.setDepGraph(graph);
			assert resources.hasDepGraph();

			return true;
		}
	}


	private IDepGraph<IManagedPackage> constructDepGraph(IResources rs, IRascalMonitor rm) {
		IWritableDepGraph<IManagedPackage> graph = new UnsyncedDepGraph<IManagedPackage>();

		for(IManagedResource res : rs.allResources()) {
			if(res instanceof IManagedPackage) {
				IManagedPackage pkg = (IManagedPackage) res;
				rm.event("Checking dependencies for " + pkg.getName(), 10);
				graph.add(pkg);
				for(IManagedPackage p : pkg.getDepends(rm))
					graph.add(pkg, p);
			}
		}

		return graph;
	}


	@Override
	public void refresh() {
		ensureInit();
		System.err.println("REFRESH DONE!");
		dataInvariant();
	}


	@Override
	public void stop() {
	}


	private void addAllResources() throws CoreException {
		final List<EclipseWorkspaceManager.Change> changes = new ArrayList<EclipseWorkspaceManager.Change>();
		project.accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) {
				if(resource.getType() == IResource.FILE)
					changes.add(new EclipseWorkspaceManager.Change(null, resource, EclipseWorkspaceManager.Change.Kind.ADDED));
				return true;
			}

		});
		queueChanges(changes);
	}


	private void dataInvariant() {
	}


	private IManagedResource findResource(URI uri, IFileStore store) {
		IResource[] rs;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IManagedResource res = null;
		if(store.fetchInfo().isDirectory())
			rs = root.findContainersForLocationURI(uri);
		else
			rs = root.findFilesForLocationURI(uri);
		// search in *this* project first
		for(IResource r : rs) {
			if(r.getProject().equals(project))
				res = findResource(MagnoliaPlugin.constructProjectURI(project, r.getProjectRelativePath()));
			if(res != null)
				return res;
		}
		for(IResource r : rs) {
			res = findResource(MagnoliaPlugin.constructProjectURI(r.getProject(), r.getProjectRelativePath()));
			if(res != null)
				return res;
		}
		return null;
	}


	private void resourceAdded(IResource resource, IWritableResources rs, IDepGraph<IManagedPackage> depGraph) {
//		IResource pkg = project.findMember(path);
		if(resource instanceof IFile) {
			URI uri = MagnoliaPlugin.constructProjectURI(project, resource.getProjectRelativePath());
			if(rs.getResource(uri) != null)
				resourceRemoved(uri, rs, depGraph);

			ILanguage language = LanguageRegistry.getLanguageForFile(uri);
			if(language != null) {
				IPath srcRelativePath = resource.getFullPath();
				srcRelativePath = srcRelativePath.makeRelativeTo(getSrcFolder());
				String modName = language.getModuleName(srcRelativePath.toString());
				IConstructor modId = language.getNameAST(modName);
				MagnoliaPackage pkg = new MagnoliaPackage(this, resource, modId, language);
				rs.addPackage(uri, language.getId() + MODULE_LANG_SEP + modName, pkg);
			}
			else {
				ManagedFile file = new ManagedFile(this, resource);
				rs.addResource(uri, file);
			}
		}
	}


	/**
	 * Called by the EclipseWorkspaceManager whenever a pkg has been changed
	 * (i.e., the file contents have changed)
	 * 
	 * @param uri
	 *            A full, workspace-relative path
	 */
	private void resourceChanged(URI uri, IResources rs, IDepGraph<IManagedPackage> depGraph) {
		if(debug)
			System.err.println("PROJECT CHANGED: " + uri);

		IManagedResource resource = rs.getResource(uri);
		if(resource != null) {
			resource.onResourceChanged();

			if(resource instanceof IManagedPackage && depGraph != null) {
				IManagedPackage pkg = (IManagedPackage) resource;
				for(IManagedPackage dep : depGraph.getTransitiveDependents(pkg))
					dep.onDependencyChanged();
			}
		}

	}


	private void resourceRemoved(URI uri, IWritableResources rs, IDepGraph<IManagedPackage> depGraph) {
		if(debug)
			System.err.println("PROJECT REMOVED: " + uri);
		IManagedResource removed = rs.removeResource(uri);
		// removed.dispose();

		if(removed instanceof IManagedPackage && depGraph != null) {
			for(IManagedPackage dep : depGraph.getTransitiveDependents((IManagedPackage) removed))
				dep.onDependencyChanged();
		}
	}


	/**
	 * @param resource
	 * @return The resource associated with the Eclipse resource handle, or null
	 *         if not found
	 */
	IManagedResource findResource(IResource resource) {
		if(!project.equals(resource.getProject()))
			throw new IllegalArgumentException("Resource must belong to this project (" + project.getName() + ")");
		return findResource(MagnoliaPlugin.constructProjectURI(resource.getProject(), resource.getProjectRelativePath()));
	}
}
