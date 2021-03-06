/**************************************************************************
 * Copyright (c) 2011-2013 Anya Helene Bagge
 * Copyright (c) 2011-2013 Tero Hasu
 * Copyright (c) 2011-2013 University of Bergen
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. See http://www.gnu.org/licenses/
 *
 *
 * See the file COPYRIGHT for more information.
 *
 * Contributors:
 * * Anya Helene Bagge
 * * Tero Hasu
 *
 *************************************************************************/
package org.nuthatchery.pica.resources.eclipse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import io.usethesource.vallang.IAnnotatable;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IWithKeywordParameters;
import io.usethesource.vallang.type.Type;
import io.usethesource.vallang.visitors.IValueVisitor;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.Pica;
import org.nuthatchery.pica.eclipse.EclipsePicaInfra;
import org.nuthatchery.pica.errors.ImplementationError;
import org.nuthatchery.pica.errors.Severity;
import org.nuthatchery.pica.handles.eclipse.EclipseFileHandle;
import org.nuthatchery.pica.resources.ILanguage;
import org.nuthatchery.pica.resources.IProjectManager;
import org.nuthatchery.pica.resources.IWorkspaceConfig;
import org.nuthatchery.pica.resources.IWorkspaceManager;
import org.nuthatchery.pica.resources.LanguageRegistry;
import org.nuthatchery.pica.resources.handles.IFileHandle;
import org.nuthatchery.pica.resources.handles.IResourceHandle;
import org.nuthatchery.pica.resources.internal.IResources;
import org.nuthatchery.pica.resources.internal.IWritableResources;
import org.nuthatchery.pica.resources.internal.Resources;
import org.nuthatchery.pica.resources.internal.SpecialCodeUnit;
import org.nuthatchery.pica.resources.managed.IManagedCodeUnit;
import org.nuthatchery.pica.resources.managed.IManagedContainer;
import org.nuthatchery.pica.resources.managed.IManagedResource;
import org.nuthatchery.pica.resources.marks.IMark;
import org.nuthatchery.pica.resources.marks.IMarkPattern;
import org.nuthatchery.pica.resources.marks.MarkBuilder;
import org.nuthatchery.pica.resources.regions.ICodeRegion;
import org.nuthatchery.pica.resources.storage.IStorage;
import org.nuthatchery.pica.util.NullnessHelper;
import org.nuthatchery.pica.util.depgraph.IDepGraph;
import org.nuthatchery.pica.util.depgraph.IWritableDepGraph;
import org.nuthatchery.pica.util.depgraph.UnsyncedDepGraph;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.nature.WarningsToMarkers;
import org.nuthatchery.pica.tasks.ITaskMonitor;
import org.nuthatchery.pica.tasks.NullTaskMonitor;
import org.nuthatchery.pica.tasks.TaskId;
import org.nuthatchery.pica.tasks.eclipse.EclipseTaskMonitor;
import org.rascalmpl.interpreter.NullRascalMonitor;
import org.rascalmpl.uri.URIUtil;

public final class EclipseProjectManager implements IProjectManager {
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
	 * The resources can be accessed without locking, but changeLock must be
	 * held
	 * before switching to a new version.
	 *
	 */
	protected volatile IResources resources = new Resources();
	/**
	 * The project we're managing.
	 */
	private final IProject project;

	/**
	 * The full workspace-relative path to the project.
	 */
	private final IPath basePath;

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
	private final IWorkspaceConfig config;
	private volatile boolean initialized = false;
	private List<MarkChange> markQueue = new ArrayList<MarkChange>();
	private Map<IMark, IMarker> marks = new HashMap<IMark, IMarker>();


	@SuppressWarnings("null")
	public EclipseProjectManager(IWorkspaceManager manager, final IWorkspaceConfig config, IProject project) throws CoreException {
		this.manager = manager;
		this.project = project;
		this.basePath = project.getFullPath();
		this.config = config;
		IFolder srcFolder = project.getFolder(SRC_FOLDER);
		try {
			srcFolder.create(0, true, null);
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
			protected IStatus run(@Nullable IProgressMonitor monitor) {
				// System.err.println("Scheduling rule: " + getRule());
				ITaskMonitor rm;
				if(monitor != null)
					rm = new EclipseTaskMonitor(SubMonitor.convert(monitor));
				else
					rm = new NullTaskMonitor();
				initialize(rm);
				monitor.done();
				return Status.OK_STATUS;

			}
		};

		storeSaveJob = new Job("Saving data for " + project.getName()) {

			@Override
			protected IStatus run(@Nullable IProgressMonitor monitor) {
				if(monitor == null)
					throw new IllegalArgumentException();
				try {
					ensureInit();
				}
				catch(ImplementationError e) {
					return Status.CANCEL_STATUS;
				}

				Collection<IManagedCodeUnit> rs = resources.allCodeUnits();
				monitor.beginTask(getName(), rs.size());
				try {
					for(IManagedCodeUnit pkg : rs) {
						IStorage storage = pkg.getStorage();
						if(storage != null) {
							try {
								// System.err.println("Saving data for " + pkg);
								storage.save();
							}
							catch(IOException e) {
								e.printStackTrace();
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
	public void addMark(IMark mark) {
		ensureInit();
		URI uri = mark.getURI();

		IManagedResource resource = findResource(uri);
		if(resource == null) {
			throw new IllegalArgumentException("No such managed resource: " + uri);
		}

		IResourceHandle file = resource.getResource();

		if(resource.isFragment()) {
			long start = resource.getOffset();
			if(mark.hasOffsetAndLength()) {
				start = start + mark.getOffset();
			}

			mark = new MarkBuilder(mark).at(start).uri(resource.getURI()).done();
		}

		synchronized(markQueue) {
			markQueue.add(new MarkChange(Change.Kind.ADDED, mark));
		}
	}


	@Override
	public void addMark(String message, ICodeRegion<URI> loc, Severity severity, String markerSource, @Nullable URI markerContext) {
		ensureInit();

		URI uri = loc.getBase();
		assert uri != null;

		String context = markerContext == null ? null : markerContext.toString();
		IMark mark;
		if(uri.getScheme().equals("unknown") && markerContext != null) {
			mark = new MarkBuilder().message(message).uri(markerContext).severity(severity).source(markerSource).context(context).done();
		}
		else
			mark = new MarkBuilder().message(message).uri(loc.getBase()).at(loc.getOffset()).length(loc.getLength()).severity(severity).source(markerSource).context(context).done();
		addMark(mark);
	}


	@Override
	public Iterable<? extends IManagedResource> allFiles() {
		ensureInit();
		return resources.allResources();
	}


	@Override
	public Collection<? extends IManagedCodeUnit> allPackages(final ILanguage language) {
		ensureInit();
		List<IManagedCodeUnit> list = new ArrayList<IManagedCodeUnit>();
		for(IManagedCodeUnit res : resources.allCodeUnits()) {
			if(res.getLanguage().equals(language)) {
				list.add(res);
			}
		}
		return list;
	}


	@Override
	public void clearMarks(IMark... marks) {
		ensureInit();
		synchronized(markQueue) {
			for(IMark m : marks) {
				if(m != null)
					markQueue.add(new MarkChange(Change.Kind.REMOVED, m));
			}
		}

	}


	@Override
	public void clearMarks(String markerSource, @Nullable URI markerCause) {
		ensureInit();
		String context = markerCause == null ? null : markerCause.toString();
		synchronized(markQueue) {
			ListIterator<MarkChange> listIterator = markQueue.listIterator();
			while(listIterator.hasNext()) {
				MarkChange m = listIterator.next();
				if(m.getKind() == Change.Kind.ADDED) {
					if(m.getMark().getSource().equals(markerSource)) {
						if(context == null || context.equals(m.getMark().getContext())) {
							listIterator.remove();
						}
					}
				}
			}

			synchronized(marks) {
				for(IMark m : marks.keySet()) {
					if(m.getSource().equals(markerSource)) {
						if(context == null || context.equals(m.getContext())) {
							markQueue.add(new MarkChange(Change.Kind.REMOVED, m));
						}
					}
				}
			}
		}
	}


	@Override
	public void commitMarks() {
		ensureInit();

		IResources resources = this.resources;
		synchronized(markQueue) {
			synchronized(marks) {
				for(MarkChange mc : markQueue) {
					IMark mark = mc.getMark();
					switch(mc.getKind()) {
					case ADDED:
						IManagedResource resource = resources.getResource(mark.getURI());
						if(resource != null) {
							try {
								IResource res = EclipsePicaInfra.toIResource(resource.getResource());
								IMarker marker = EclipseMarks.markToMarker(res, mark);
								System.err.println("Commiting mark [" + Thread.currentThread().getId() + "]: " + mark);
								mark = EclipseMarks.linkWithMarker(mark, marker);
								marks.put(mark, marker);
							}
							catch(CoreException e) {
								e.printStackTrace();
							}
						}
						break;
					case CHANGED:
						// not used
						break;
					case REMOVED:
						IMarker marker = marks.remove(mark);
						if(marker != null && marker.exists()) {
							try {
								marker.delete();
							}
							catch(CoreException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						break;
					}
				}
				markQueue.clear();
			}
		}
	}


	private IDepGraph<IManagedCodeUnit> constructDepGraph(IResources rs, ITaskMonitor rm) {
		IWritableDepGraph<IManagedCodeUnit> graph = new UnsyncedDepGraph<IManagedCodeUnit>();
		for(IManagedCodeUnit pkg : rs.allCodeUnits()) {
			rm.subTask(new TaskId("EclipseProjectManager.constructDepGraph", "Checking dependencies for " + pkg.getName(), pkg.getName()));
			graph.add(pkg);
			try {
				for(IManagedCodeUnit p : pkg.getDepends(rm)) {
					graph.add(pkg, p);
				}
				if(pkg.hasIncompleteDepends(rm)) {
					graph.add(pkg, SpecialCodeUnit.INCOMPLETE_DEPENDS);
				}
			}
			catch(NullPointerException e) {
				e.printStackTrace();
			}
			rm.done(10);
		}

		return graph;
	}


	protected void dataInvariant() {
	}


	@Override
	public void dispose() {
		ensureInit();

		resources = new Resources();
		markQueue.clear();
		marks.clear();
		dataInvariant();
	}


	protected void ensureInit() {
		if(!initialized) {
			try {
				initJob.join();
			}
			catch(InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!initialized) {
			throw new ImplementationError("Project manager for " + project.getName() + " not initialized");
		}
	}


	@Override
	@Nullable
	public IManagedCodeUnit findCodeUnit(ILanguage language, Object moduleId) {
		ensureInit();
		return resources.getCodeUnit(language.getId() + LANG_SEP + language.getNameString(moduleId));
	}


	@Override
	@Nullable
	public IManagedCodeUnit findCodeUnit(ILanguage language, String moduleName) {
		ensureInit();
		return resources.getCodeUnit(language.getId() + LANG_SEP + moduleName);
	}


	@Override
	@Nullable
	public IManagedCodeUnit findCodeUnit(URI uri) {
		ensureInit();
		IManagedCodeUnit resource = resources.getCodeUnit(uri);
		return resource;
	}


	@Override
	public Collection<IMark> findMarks(IManagedResource resource) {
		ensureInit();

		long offset = -1;
		long length = -1;
		if(resource.isFragment()) {
			offset = resource.getOffset();
			try {
				length = resource.getLength();
			}
			catch(IOException e) {
				// ignored
			}
		}
		URI uri = resource.getResource().getURI();
		List<IMark> list = new ArrayList<IMark>();
		synchronized(marks) {
			for(IMark m : marks.keySet()) {
				if(m.getURI().equals(uri)) {
					if(offset == -1 && length == -1) {
						list.add(m);
					}
					else if(m.hasOffsetAndLength()) {
						if(m.getOffset() >= offset && m.getOffset() + m.getLength() < offset + length)
							list.add(m);
					}
				}
			}
		}
		return list;
	}


	@Override
	public Collection<IMark> findMarks(IMarkPattern searchCriteria) {
		ensureInit();

		List<IMark> list = new ArrayList<IMark>();
		synchronized(marks) {
			for(IMark m : marks.keySet()) {
				if(searchCriteria.matches(m))
					list.add(m);
			}
		}
		return list;
	}


	/**
	 * @param resource
	 * @return The resource associated with the Eclipse resource handle, or null
	 *         if not found
	 */
	@Nullable
	IManagedResource findResource(IResource resource) {
		if(!project.equals(resource.getProject())) {
			throw new IllegalArgumentException("Resource must belong to this project (" + project.getName() + ")");
		}
		return findResource(EclipsePicaInfra.constructProjectURI(resource.getProject(), resource.getProjectRelativePath()));
	}


	@Override
	@Nullable
	public IManagedResource findResource(String path) {
		ensureInit();
		URI uri = Pica.get().constructProjectURI(project.getName(), path);
		IManagedResource resource = findResource(uri);

		return resource;
	}


	@Override
	@Nullable
	public IManagedResource findResource(URI uri) {
		ensureInit();
		// see if we already track the URI
		IManagedResource res = resources.getResource(uri);
		if(res != null) {
			return res;
		}
		else {
			try {
				res = resources.getResource(NullnessHelper.assertNonNull(URIUtil.changeFragment(uri, null)));
				if(res != null)
					return res;
			}
			catch(URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		String scheme = uri.getScheme();

		// check if it is a project URI
		if(scheme.equals("project")) {
			if(uri.getAuthority().equals(project.getName())) {
				return null;// we should already have found it if we were tracking it
			}
			else {
				IProjectManager mng = Pica.getResourceManager(uri.getAuthority());
				if(mng != null) {
					return mng.findResource(uri);
				}
				else {
					return null;
				}
			}
		}
		else if(scheme.equals("magnolia")) {
			return null;// not handled yet
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


	@Nullable
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
				res = findResource(EclipsePicaInfra.constructProjectURI(project, r.getProjectRelativePath()));
			}
			if(res != null) {
				return res;
			}
		}
		for(IResource r : rs) {
			res = findResource(EclipsePicaInfra.constructProjectURI(r.getProject(), r.getProjectRelativePath()));
			if(res != null) {
				return res;
			}
		}
		return null;
	}


	@Override
	public Collection<IManagedResource> getChildren(ITaskMonitor rm) {
		ensureInit();
		return new ArrayList<IManagedResource>(resources.allResources());
	}


	@Override
	@Nullable
	public IManagedCodeUnit getCodeUnit(IResourceHandle resource) {
		ensureInit();
		return resources.getCodeUnit(resource);
	}


	@Override
	public Path getOutFolder() {
		return EclipsePicaInfra.toPath(outPath);
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
	public IDepGraph<IManagedCodeUnit> getPackageDependencyGraph(@Nullable ITaskMonitor rm) {
		ensureInit();
		IDepGraph<IManagedCodeUnit> depGraph = resources.getDepGraph();
		if(depGraph != null) {
			return depGraph;
		}

		// if not found, wait for processChanges() to finish if it is running
		synchronized(changeLock) {
			depGraph = resources.getDepGraph();
			assert depGraph != null;
			return depGraph;
		}

	}


	@Override
	public Set<IManagedCodeUnit> getPackageTransitiveDependents(IManagedCodeUnit pkg, ITaskMonitor rm) {
		ensureInit();
		Set<IManagedCodeUnit> dependents = getPackageDependencyGraph(rm).getTransitiveDependents(pkg);
		if(dependents != null) {
			return dependents;
		}
		else {
			return Collections.EMPTY_SET;
		}
	}


	@Override
	public String getProjectName() {
		return project.getName();
	}


	@Override
	public IWorkspaceManager getResourceManager() {
		return manager;
	}


	@Override
	public Path getSrcFolder() {
		if(srcPath == null) {
			IResource src = project.findMember(SRC_FOLDER);
			if(src != null && src.getType() == IResource.FOLDER) {
				return EclipsePicaInfra.toPath(src.getFullPath());
			}
			else {
				return EclipsePicaInfra.toPath(basePath);
			}
		}
		else {
			return EclipsePicaInfra.toPath(srcPath);
		}
	}


	@Override
	public IStorage getStorage(URI uri) {
		throw new UnsupportedOperationException();
	}


	@Override
	public URI getURI() {
		return Pica.get().constructProjectURI(project.getName(), "/");
	}


	@Override
	public URI getURI(String path) throws URISyntaxException {
		return Pica.get().constructProjectURI(project.getName(), path);
	}


	private void initialize(ITaskMonitor rm) {
		long t0 = System.currentTimeMillis();
		config.initCompiler();
		System.err.println("Project manager for: " + project.getName() + ": initialised in " + (System.currentTimeMillis() - t0) + "ms");
		t0 = System.currentTimeMillis();
		processChanges(rm);
		System.err.println("Project manager initialisation for: " + project.getName() + ": done in " + (System.currentTimeMillis() - t0) + "ms");
		dataInvariant();
	}


	@Nullable
	public URI makeOutputURI(URI sourceURI, String fileNameExtension) {
		IPath path = new org.eclipse.core.runtime.Path(sourceURI.getPath());
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


	public void printDepGraph() {
		ensureInit();
		IDepGraph<IManagedCodeUnit> depGraph = getPackageDependencyGraph(null);
		System.err.flush();
		System.out.flush();
		System.out.println("DEPENDENCY GRAPH FOR PROJECT " + project.getName());
		for(IManagedCodeUnit pkg : depGraph.topological()) {
			System.out.print("\t  " + pkg.getName() + " <- ");
			for(IManagedCodeUnit dep : depGraph.getDependents(pkg)) {
				System.out.print(dep.getName() + " ");
			}
			System.out.println();
		}
	}


	@Override
	public boolean processChanges(ITaskMonitor rm) {
		synchronized(changeLock) {
			IResources oldResources = resources;
			IDepGraph<IManagedCodeUnit> depGraph;

			depGraph = oldResources.getDepGraph();
			if(depGraph == null) {
				depGraph = new UnsyncedDepGraph<IManagedCodeUnit>();
			}
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
			if(changes.isEmpty() && initialized) {
				return false;
			}

			ITaskMonitor changeMonitor = rm.subMonitor("Processing workspace changes for " + project.getName(), 10);
			changeMonitor.setWorkTodo(changes.size() * 2 + oldResources.numPackages() * 10);
			IWritableResources rs = null;

			for(Change change : changes) {
				changeMonitor.done(2);
				switch(change.kind) {
				case ADDED:
					if(rs == null) {
						rs = oldResources.createNewVersion();
					}
					resourceAdded(change.resource, rs, depGraph);
					break;
				case CHANGED:
					assert oldResources != null;
					resourceChanged(change.getURI(), rs != null ? rs : oldResources, depGraph);
					break;
				case REMOVED:
					if(rs == null) {
						rs = oldResources.createNewVersion();
					}
					resourceRemoved(change.getURI(), rs, depGraph);
					break;
				}
			}

			if(rs != null) {
				resources = rs;
			}
			initialized = true;

			rm.setWorkTodo(resources.numPackages() * 10);
			IDepGraph<IManagedCodeUnit> graph = constructDepGraph(resources, rm);
			resources.setDepGraph(graph);
			assert resources.hasDepGraph();
			commitMarks();

			storeSaveJob.schedule(5000);
			return true;
		}
	}


	private void queueAllResources() throws CoreException {
		final List<Change> changes = new ArrayList<Change>();

		project.accept(new IResourceVisitor() {
			@Override
			public boolean visit(@Nullable IResource resource) {
				assert resource != null;
				if(resource.getType() == IResource.FILE) {
					changes.add(new Change(resource.getLocationURI(), resource, Change.Kind.ADDED));
				}
				return true;
			}

		});
		queueChanges(changes);
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
				resources = new Resources();
				initialized = false;
				initialize(new NullTaskMonitor());
			}
			catch(CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.err.println("REFRESH DONE!");
		dataInvariant();
	}


	private void resourceAdded(IResource resource, IWritableResources rs, IDepGraph<IManagedCodeUnit> depGraph) {
		if(debug) {
			System.err.println("PROJECT ADDED: " + resource.getFullPath());
		}
		if(resource instanceof IFile) {
			URI uri = EclipsePicaInfra.constructProjectURI(project, resource.getProjectRelativePath());
			if(rs.getResource(uri) != null) {
				resourceRemoved(uri, rs, depGraph);
			}
			Path fullPath = EclipsePicaInfra.toPath(resource.getFullPath());
			if(fullPath.startsWith(getSrcFolder())) {
				IPath srcRelativePath = EclipsePicaInfra.toIPath(getSrcFolder().relativize(fullPath));

				IFileHandle file = EclipsePicaInfra.getResourceHandle((IFile) resource);
//				rs.addResource(uri, file);

				ILanguage language = LanguageRegistry.getLanguageForFile(uri);
				if(language != null) {
					String modName = language.getModuleName(srcRelativePath.toString());
					Object modId = language.getNameAST(modName);
					String ext = language.getStoreExtension();
					IStorage store = null;
					if(ext != null) {
						IPath outPath = storePath.append(srcRelativePath).removeFileExtension().addFileExtension(ext);
						IFile outFile = project.getWorkspace().getRoot().getFile(outPath);
						store = new EclipseStorage(outFile);
					}
					IManagedCodeUnit pkg = config.makePackage(this, file, store, modId, language);
					rs.addCodeUnit(uri, language.getId() + LANG_SEP + modName, pkg, file);
				}
				else {
				}
			}

			if(depGraph != null) {
				for(IManagedCodeUnit dep : depGraph.getTransitiveDependents(SpecialCodeUnit.INCOMPLETE_DEPENDS)) {
					dep.onDependencyChanged();
				}
			}

		}
	}


	/**
	 * Called by the EclipseWorkspaceManager whenever a pkg has been changed
	 * (i.e., the file contents have changed
	 *
	 * @param uri
	 *            A full, workspace-relative path
	 */
	private void resourceChanged(URI uri, IResources rs, IDepGraph<IManagedCodeUnit> depGraph) {
		if(debug) {
			System.err.println("PROJECT CHANGED: " + uri);
		}

		IManagedResource resource = rs.getResource(uri);
		if(resource != null) {
			resource.onResourceChanged();

			if(resource instanceof IManagedCodeUnit && depGraph != null) {
				for(IManagedCodeUnit dep : depGraph.getTransitiveDependents((IManagedCodeUnit) resource)) {
					dep.onDependencyChanged();
				}
			}
		}

	}


	private void resourceRemoved(URI uri, IWritableResources rs, IDepGraph<IManagedCodeUnit> depGraph) {
		if(debug) {
			System.err.println("PROJECT REMOVED: " + uri);
		}

		IManagedResource resource = rs.getResource(uri);
		rs.removeResource(uri);
		// removed.dispose();

		if(resource instanceof IManagedCodeUnit && depGraph != null) {
			for(IManagedCodeUnit dep : depGraph.getTransitiveDependents((IManagedCodeUnit) resource)) {
				dep.onDependencyChanged();
			}
		}
	}


	@Override
	public void stop() {
	}

}
