package org.magnolialang.resources.eclipse;

import java.io.IOException;
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
import org.eclipse.core.resources.IFolder;
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
import org.magnolialang.infra.Infra;
import org.magnolialang.magnolia.Magnolia;
import org.magnolialang.magnolia.resources.EclipseMagnoliaPackage;
import org.magnolialang.nullness.Nullable;
import org.magnolialang.resources.ILanguage;
import org.magnolialang.resources.IManagedPackage;
import org.magnolialang.resources.IManagedResource;
import org.magnolialang.resources.IResourceManager;
import org.magnolialang.resources.IWorkspaceManager;
import org.magnolialang.resources.LanguageRegistry;
import org.magnolialang.resources.internal.IResources;
import org.magnolialang.resources.internal.IWritableResources;
import org.magnolialang.resources.internal.Resources;
import org.magnolialang.resources.storage.IStorage;
import org.magnolialang.util.depgraph.IDepGraph;
import org.magnolialang.util.depgraph.IWritableDepGraph;
import org.magnolialang.util.depgraph.UnsyncedDepGraph;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.interpreter.IRascalMonitor;
import org.rascalmpl.uri.URIUtil;

public final class EclipseProjectManager implements IResourceManager {
	/**
	 * This lock should be acquired before starting any work leading to a new
	 * version of the resources. It must be held while updating the resources
	 * field.
	 */
	private final Object changeLock = new Object();
	/**
	 * The parent workspace manager.
	 */
	private final IWorkspaceManager manager;
	/**
	 * The IResources object stored here must be *immutable*, except that its
	 * dependency graph may be replaced.
	 * 
	 * The resources can be access without locking, but changeLock must be held
	 * before switching to a new version.
	 * 
	 */
	private volatile IResources resources = null;
	/**
	 * The project we're managing.
	 */
	private final IProject project;
	/**
	 * The full workspace-relative path to the project.
	 */
	private final IPath basePath;
	/**
	 * A character that can't occuring in package names (used to fake a
	 * namespace for each language).
	 */
	private static final String LANG_SEP = "%";
	/**
	 * The default output folder.
	 * 
	 */
	private static final String OUT_FOLDER = "cxx";
	/**
	 * The default source folder.
	 */
	private static final String SRC_FOLDER = "src";
	/**
	 * The default store folder.
	 */
	private static final String STORE_FOLDER = "cache";

	/**
	 * Control debug printing
	 */
	private static final boolean debug = false;

	/**
	 * The src folder of the project (workspace-relative).
	 */
	private final IPath srcPath;

	/**
	 * The output folder of the project (workspace-relative).
	 */
	private final IPath outPath;

	/**
	 * The store folder of the project (workspace-relative).
	 */
	private final IPath storePath;

	/**
	 * A list of changes that are not yet reflected in 'resources'.
	 */
	private final List<Change> changeQueue = new ArrayList<Change>();

	/**
	 * A job which processes the initial set of resource changes.
	 */
	private final Job initJob;
	private final Job storeSaveJob;


	public EclipseProjectManager(IWorkspaceManager manager, IProject project) throws CoreException {
		this.manager = manager;
		this.project = project;
		this.basePath = project.getFullPath();
		IFolder srcFolder = project.getFolder(SRC_FOLDER);
		try {
			srcFolder.create(IResource.DERIVED, true, null);
		}
		catch(CoreException e) {
			// ignore
		}
		srcPath = srcFolder.getFullPath();
		IFolder outFolder = project.getFolder(OUT_FOLDER);
		try {
			outFolder.create(IResource.DERIVED, true, null);
		}
		catch(CoreException e) {
			// ignore
		}
		outPath = outFolder.getFullPath();

		IFolder storeFolder = project.getFolder(STORE_FOLDER);
		try {
			storeFolder.create(IResource.DERIVED | IResource.HIDDEN, true, null);
		}
		catch(CoreException e) {
			// ignore
		}
		storePath = storeFolder.getFullPath();

		queueAllResources();

		initJob = new Job("Computing dependencies for " + project.getName()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				// System.err.println("Scheduling rule: " + getRule());
				IRascalMonitor rm = new RascalMonitor(monitor);
				long t0 = System.currentTimeMillis();
				Magnolia.getInstance().getCompiler().ensureInit();
				System.err.println(getName() + ": initialised in " + (System.currentTimeMillis() - t0) + "ms");
				t0 = System.currentTimeMillis();
				processChanges(rm);
				System.err.println(getName() + ": done in " + (System.currentTimeMillis() - t0) + "ms");
				dataInvariant();
				return Status.OK_STATUS;
			}
		};

		storeSaveJob = new Job("Saving data for " + project.getName()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					ensureInit();
				}
				catch(ImplementationError e) {
					return Status.CANCEL_STATUS;
				}

				Collection<IManagedResource> rs = resources.allResources();
				monitor.beginTask(getName(), rs.size());
				try {
					for(IManagedResource r : rs) {
						if(r instanceof IManagedPackage) {
							IManagedPackage pkg = (IManagedPackage) r;
							IStorage storage = pkg.getStorage();
							if(storage != null) {
								try {
									storage.save();
								}
								catch(IOException e) {
									e.printStackTrace();
								}
							}
						}
						if(monitor.isCanceled()) {
							schedule(5000);
							return Status.CANCEL_STATUS;
						}
						monitor.worked(1);
					}
				}
				finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}

		};
		// initJob.setRule(project.getFolder(SRC_FOLDER));
		initJob.schedule();

		storeSaveJob.setRule(project.getFolder(STORE_FOLDER));
		storeSaveJob.schedule(10000);
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


	@Override
	public void addMarker(String message, ISourceLocation loc, String markerType, int severity) {
		ensureInit();

		IManagedResource pkg;
		if(loc == null) {
			throw new ImplementationError("Missing location on marker add: " + message);
		}

		URI uri = loc.getURI();

		pkg = resources.getResource(uri);

		if(pkg instanceof IManagedPackage) {
			((IManagedPackage) pkg).addMarker(message, loc, markerType, severity);
		}
		else {
			throw new ImplementationError(message + "\nat location " + loc + " (pkg not found)");
		}
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
		for(IManagedResource res : resources.allResources()) {
			if(res instanceof IManagedPackage && ((IManagedPackage) res).getLanguage().equals(language)) {
				list.add((IManagedPackage) res);
			}
		}
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
		return resources.getPackage(language.getId() + LANG_SEP + language.getNameString(moduleId));
	}


	@Override
	@Nullable
	public IManagedPackage findPackage(ILanguage language, String moduleName) {
		ensureInit();
		return resources.getPackage(language.getId() + LANG_SEP + moduleName);
	}


	@Override
	public IManagedPackage findPackage(URI uri) {
		ensureInit();
		IManagedResource resource = resources.getResource(uri);
		if(resource instanceof IManagedPackage) {
			return (IManagedPackage) resource;
		}
		else {
			return null;
		}
	}


	@Override
	public IManagedResource findResource(String path) {
		ensureInit();
		URI uri = MagnoliaPlugin.constructProjectURI(project, new Path(path));
		IManagedResource resource = findResource(uri);

		return resource;
	}


	@Override
	public IManagedResource findResource(URI uri) {
		ensureInit();
		// see if we already track the URI
		IManagedResource res = resources.getResource(uri);
		if(res != null) {
			return res;
		}

		String scheme = uri.getScheme();

		// check if it is a project URI
		if(scheme.equals("project")) {
			if(uri.getAuthority().equals(project.getName())) {
				return null; // we should already have found it if we were tracking it
			}
			else {
				IResourceManager mng = Infra.getResourceManager(uri.getAuthority());
				if(mng != null) {
					return mng.findResource(uri);
				}
				else {
					return null;
				}
			}
		}
		else if(scheme.equals("magnolia")) {
			return null; // not handled yet
		}
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
		if(depGraph != null) {
			return depGraph;
		}

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
		if(dependents != null) {
			return dependents;
		}
		else {
			return Collections.EMPTY_SET;
		}
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
			IResource src = project.findMember(SRC_FOLDER);
			if(src != null && src.getType() == IResource.FOLDER) {
				return src.getFullPath();
			}
			else {
				return basePath;
			}
		}
		else {
			return srcPath;
		}
	}


	@Override
	public IStorage getStorage(URI uri) {
		return null;
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


	public URI makeOutputURI(URI sourceURI, String fileNameExtension) {
		IPath path = new Path(sourceURI.getPath());
		path = path.removeFileExtension().addFileExtension(fileNameExtension);
		try {
			URIUtil.changePath(sourceURI, path.toString());
		}
		catch(URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
			for(IManagedPackage dep : depGraph.getDependents(pkg)) {
				System.out.print(dep.getName() + " ");
			}
			System.out.println();
		}
	}


	@Override
	public boolean processChanges(IRascalMonitor rm) {
		synchronized(changeLock) {
			IResources oldResources;
			IDepGraph<IManagedPackage> depGraph;
			oldResources = resources;
			if(oldResources == null) {
				oldResources = new Resources();
			}
			depGraph = oldResources.getDepGraph();

			List<Change> changes;
			synchronized(changeQueue) {
				changes = new ArrayList<Change>(changeQueue);
				changeQueue.clear();
			}
			/*
			  System.err.println("Changes: ");
			for(Change c : changes) {
				System.err.println("  " + c.kind.name() + " " + (c.uri != null ? c.uri : c.resource));
			}
			 */
			if(changes.isEmpty()) {
				return false;
			}

			rm.startJob("Processing workspace changes for " + project.getName(), 10, changes.size() * 2 + oldResources.numPackages() * 10);
			IWritableResources rs = null;
			for(Change change : changes) {
				rm.event(2);
				switch(change.kind) {
				case ADDED:
					if(rs == null) {
						rs = oldResources.createNewVersion();
					}
					resourceAdded(change.resource, rs, depGraph);
					break;
				case CHANGED:
					resourceChanged(change.uri, rs != null ? rs : oldResources, depGraph);
					break;
				case REMOVED:
					if(rs == null) {
						rs = oldResources.createNewVersion();
					}
					resourceRemoved(change.uri, rs, depGraph);
					break;
				}
			}

			if(rs != null) {
				resources = rs;
			}

			rm.todo(resources.numPackages() * 10);
			IDepGraph<IManagedPackage> graph = constructDepGraph(resources, rm);
			resources.setDepGraph(graph);
			assert resources.hasDepGraph();

			storeSaveJob.schedule(5000);
			return true;
		}
	}


	public void queueChanges(List<Change> list) {
		synchronized(changeQueue) {
			changeQueue.addAll(list);
		}
	}


	@Override
	public void refresh() {
		synchronized(changeLock) {
			try {
				queueAllResources();
				resources = null;
				initJob.schedule();
			}
			catch(CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.err.println("REFRESH DONE!");
		dataInvariant();
	}


	@Override
	public void stop() {
	}


	private IDepGraph<IManagedPackage> constructDepGraph(IResources rs, IRascalMonitor rm) {
		IWritableDepGraph<IManagedPackage> graph = new UnsyncedDepGraph<IManagedPackage>();

		for(IManagedResource res : rs.allResources()) {
			if(res instanceof IManagedPackage) {
				IManagedPackage pkg = (IManagedPackage) res;
				rm.event("Checking dependencies for " + pkg.getName(), 10);
				graph.add(pkg);
				try {
					for(IManagedPackage p : pkg.getDepends(rm)) {
						graph.add(pkg, p);
					}
				}
				catch(NullPointerException e) {
					e.printStackTrace();
				}
			}
		}

		return graph;
	}


	private void dataInvariant() {
	}


	private void ensureInit() {
		if(resources == null) {
			try {
				initJob.join();
			}
			catch(InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(resources == null) {
			throw new ImplementationError("Project manager for " + project.getName() + " not initialized");
		}
	}


	private IManagedResource findResource(URI uri, IFileStore store) {
		IResource[] rs;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IManagedResource res = null;
		if(store.fetchInfo().isDirectory()) {
			rs = root.findContainersForLocationURI(uri);
		}
		else {
			rs = root.findFilesForLocationURI(uri);
		}
		// search in *this* project first
		for(IResource r : rs) {
			if(r.getProject().equals(project)) {
				res = findResource(MagnoliaPlugin.constructProjectURI(project, r.getProjectRelativePath()));
			}
			if(res != null) {
				return res;
			}
		}
		for(IResource r : rs) {
			res = findResource(MagnoliaPlugin.constructProjectURI(r.getProject(), r.getProjectRelativePath()));
			if(res != null) {
				return res;
			}
		}
		return null;
	}


	private void queueAllResources() throws CoreException {
		final List<Change> changes = new ArrayList<Change>();
		project.accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) {
				if(resource.getType() == IResource.FILE) {
					changes.add(new Change(null, resource, Change.Kind.ADDED));
				}
				return true;
			}

		});
		queueChanges(changes);
	}


	private void resourceAdded(IResource resource, IWritableResources rs, IDepGraph<IManagedPackage> depGraph) {
		if(debug) {
			System.err.println("PROJECT ADDED: " + resource.getFullPath());
		}
		if(resource instanceof IFile) {
			URI uri = MagnoliaPlugin.constructProjectURI(project, resource.getProjectRelativePath());
			if(rs.getResource(uri) != null) {
				resourceRemoved(uri, rs, depGraph);
			}

			ILanguage language = LanguageRegistry.getLanguageForFile(uri);
			if(language != null) {
				IPath srcRelativePath = resource.getFullPath();
				srcRelativePath = srcRelativePath.makeRelativeTo(getSrcFolder());
				String modName = language.getModuleName(srcRelativePath.toString());
				IConstructor modId = language.getNameAST(modName);
				String ext = language.getStoreExtension();
				IStorage store = null;
				if(ext != null) {
					IPath outPath = storePath.append(srcRelativePath).removeFileExtension().addFileExtension(ext);
					IFile outFile = project.getWorkspace().getRoot().getFile(outPath);
					store = new EclipseStorage(outFile);
				}
				IManagedPackage pkg = new EclipseMagnoliaPackage(this, (IFile) resource, store, modId, language);
				rs.addPackage(uri, language.getId() + LANG_SEP + modName, pkg);
			}
			else {
				ManagedEclipseFile file = new ManagedEclipseFile(this, (IFile) resource);
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
		if(debug) {
			System.err.println("PROJECT CHANGED: " + uri);
		}

		IManagedResource resource = rs.getResource(uri);
		if(resource != null) {
			resource.onResourceChanged();

			if(resource instanceof IManagedPackage && depGraph != null) {
				IManagedPackage pkg = (IManagedPackage) resource;
				for(IManagedPackage dep : depGraph.getTransitiveDependents(pkg)) {
					dep.onDependencyChanged();
				}
			}
		}

	}


	private void resourceRemoved(URI uri, IWritableResources rs, IDepGraph<IManagedPackage> depGraph) {
		if(debug) {
			System.err.println("PROJECT REMOVED: " + uri);
		}
		IManagedResource removed = rs.removeResource(uri);
		// removed.dispose();

		if(removed instanceof IManagedPackage && depGraph != null) {
			for(IManagedPackage dep : depGraph.getTransitiveDependents((IManagedPackage) removed)) {
				dep.onDependencyChanged();
			}
		}
	}


	/**
	 * @param resource
	 * @return The resource associated with the Eclipse resource handle, or null
	 *         if not found
	 */
	IManagedResource findResource(IResource resource) {
		if(!project.equals(resource.getProject())) {
			throw new IllegalArgumentException("Resource must belong to this project (" + project.getName() + ")");
		}
		return findResource(MagnoliaPlugin.constructProjectURI(resource.getProject(), resource.getProjectRelativePath()));
	}

}
