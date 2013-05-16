/**************************************************************************
 * Copyright (c) 2012 Anya Helene Bagge
 * Copyright (c) 2012 University of Bergen
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. See http://www.gnu.org/licenses/
 * 
 * 
 * See the file COPYRIGHT for more information.
 * 
 * Contributors:
 * * Anya Helene Bagge
 * 
 *************************************************************************/
package org.magnolialang.resources.filesystem;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.magnolialang.eclipse.MagnoliaNature;
import org.magnolialang.eclipse.MagnoliaPlugin;
import org.magnolialang.resources.IManagedResource;
import org.magnolialang.resources.IResourceManager;
import org.magnolialang.resources.IWorkspaceManager;
import org.magnolialang.util.Pair;

public final class FileSystemWorkspaceManager implements IWorkspaceManager {
	private static FileSystemWorkspaceManager instance;


	public static synchronized FileSystemWorkspaceManager getInstance() {
		if(instance == null) {
			instance = new FileSystemWorkspaceManager();
		}
		return instance;
	}


	public static IPath getPath(URI uri) {
		if(uri.getScheme().equals("project")) {
			return new Path("/" + uri.getAuthority() + "/" + uri.getPath());
		}
		else {
			return null;
		}
	}


	public static Pair<Set<IManagedResource>, Set<IPath>> getResourcesForDelta(IResourceDelta delta) {
		getInstance();
		final Set<IManagedResource> changed = new HashSet<IManagedResource>();
		final Set<IPath> removed = new HashSet<IPath>();

		try {
			delta.accept(new IResourceDeltaVisitor() {
				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					if(delta != null && delta.getResource() instanceof IFile) {
						switch(delta.getKind()) {
						case IResourceDelta.ADDED: {
							IManagedResource resource = instance.findResource(delta.getResource());
							if(resource != null) {
								changed.add(resource);
							}
							break;
						}
						case IResourceDelta.CHANGED: {
							IManagedResource resource = instance.findResource(delta.getResource());
							if(resource != null) {
								changed.add(resource);
							}
							break;
						}
						case IResourceDelta.REMOVED:
							removed.add(delta.getResource().getFullPath());
							break;
						default:
							throw new UnsupportedOperationException("Resource change on " + delta.getFullPath() + ": " + delta.getKind());
						}
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


	/**
	 * Ensure that a directory and all its ancestors exist.
	 * 
	 * @param path
	 *            A workspace-relative directory path
	 * @param updateFlags
	 *            Flags, e.g., IResource.DERIVED and/or IResource.HIDDEN
	 * @return The container/folder identified by path
	 * @throws CoreException
	 */
	public static IContainer mkdir(IPath path, int updateFlags) throws CoreException {
		IContainer parent = ResourcesPlugin.getWorkspace().getRoot();
		for(String s : path.segments()) {
			IResource member = parent.findMember(s, true);
			if(member == null) {
				parent = parent.getFolder(new Path(s));
				((IFolder) parent).create(updateFlags, true, null);
			}
			else if(member instanceof IContainer) {
				parent = (IContainer) member;
				if(!parent.exists()) {
					((IFolder) parent).create(updateFlags, true, null);
				}
			}
			else {
				throw new CoreException(new Status(IStatus.ERROR, MagnoliaPlugin.PLUGIN_ID, "Path already exists, and is not a folder: " + member.getFullPath()));
			}
		}
		return parent;

	}

	private final Map<String, FileSystemProjectManager> projects = new HashMap<String, FileSystemProjectManager>();

	private final List<IProject> closingProjects = new ArrayList<IProject>();

	private final static boolean debug = false;

	private static final Object JOB_FAMILY_WORKSPACE_MANAGER = new Object();


	private FileSystemWorkspaceManager() {
		initialize();
	}


	public void dataInvariant() {
	}


	@Override
	public void dispose() {
		// do nothing
	}


	public synchronized IManagedResource findResource(IResource resource) {
		FileSystemProjectManager projectManager = projects.get(resource.getProject().getName());
		IManagedResource res = null;
		if(projectManager != null) {
			res = projectManager.findResource(resource);
			if(res != null) {
				return res;
			}
			if(resource.exists()) {
				try {
					System.err.println("FileSystemWorkspaceManager: adding missing resource " + resource.getFullPath());
					resourceAdded(resource);
					return projectManager.findResource(resource);
				}
				catch(CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}


	public synchronized IResourceManager getManager(IProject project) {
		return projects.get(project.getName());
	}


	@Override
	public synchronized IResourceManager getManager(String project) {
		return projects.get(project);
	}


	@Override
	public URI getURI(String path) throws URISyntaxException {
		IPath p = new Path(path);
		String project = p.segment(0);
		p = p.removeFirstSegments(1);
		return MagnoliaPlugin.constructProjectURI(project, p);
	}


	@Override
	public boolean hasURI(URI uri) {
		if(uri.getScheme().equals("project")) {
			return true;
		}

		IFile file = MagnoliaPlugin.getFileHandle(uri);
		return file != null;
	}


	@Override
	public synchronized void stop() {
		IJobManager jobManager = Job.getJobManager();
		jobManager.cancel(JOB_FAMILY_WORKSPACE_MANAGER);
		for(FileSystemProjectManager mgr : projects.values()) {
			mgr.stop();
		}
	}


	private void closeProject(IProject project) {
		// TODO: check nature
		IResourceManager manager = projects.remove(project.getName());
		if(manager != null) {
			manager.dispose();
		}
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


	private void openProject(IProject project) throws CoreException {
		if(project.hasNature(MagnoliaNature.NATURE_ID)) {
			projects.put(project.getName(), new FileSystemProjectManager(this, project));
		}

	}


	private void resourceAdded(IResource resource) throws CoreException {
		if(resource.getType() == IResource.PROJECT) {
			if(((IProject) resource).isOpen()) {
				openProject((IProject) resource);
			}
		}
		else {
			IProject project = resource.getProject();
			URI uri = MagnoliaPlugin.constructProjectURI(project, resource.getProjectRelativePath());
		}
	}


	private void resourceChanged(IResourceDelta delta) throws CoreException {
		IPath path = delta.getFullPath();
		int flags = delta.getFlags();

		if((flags & (IResourceDelta.TYPE | IResourceDelta.REPLACED)) != 0) {
			resourceRemoved(delta.getResource());
			resourceAdded(delta.getResource());
		}

		if((flags & IResourceDelta.OPEN) != 0) {
			IProject proj = (IProject) delta.getResource();
			if(proj.isOpen()) {
				openProject(proj);
			}
			else {
				closingProjects.add(proj);
			}

		}

		if((flags & IResourceDelta.LOCAL_CHANGED) != 0) {
			System.err.println("LOCAL_CHANGED: " + path);
		}
		if((flags & IResourceDelta.CONTENT) != 0 || (flags & IResourceDelta.ENCODING) != 0) {
			if(debug) {
				System.err.println("RESOURCE CHANGED: " + path);
			}
			IResource resource = delta.getResource();
			IProject project = resource.getProject();
			URI uri = MagnoliaPlugin.constructProjectURI(project, resource.getProjectRelativePath());

		}

	}


	private void resourceRemoved(IResource resource) {
		if(resource.getType() == IResource.PROJECT) {
			closeProject((IProject) resource);
		}
		else {
			IProject project = resource.getProject();
			URI uri = MagnoliaPlugin.constructProjectURI(project, resource.getProjectRelativePath());
		}
	}

}
