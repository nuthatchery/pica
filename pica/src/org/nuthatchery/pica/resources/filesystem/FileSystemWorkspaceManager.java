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
 * it under the terms of the GNU Lesser General Public License as published by
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
package org.nuthatchery.pica.resources.filesystem;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.nuthatchery.pica.ConsolePicaInfra;
import org.nuthatchery.pica.Pica;
import org.nuthatchery.pica.resources.IResourceManager;
import org.nuthatchery.pica.resources.IWorkspaceManager;

public final class FileSystemWorkspaceManager implements IWorkspaceManager {
	private static FileSystemWorkspaceManager instance;

	private final Map<String, FileSystemProjectManager> projects = new HashMap<String, FileSystemProjectManager>();

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


	@Override
	public synchronized IResourceManager getManager(String project) {
		return projects.get(project);
	}


	@Override
	public URI getURI(String path) throws URISyntaxException {
		IPath p = new Path(path);
		String project = p.segment(0);
		p = p.removeFirstSegments(1);
		return Pica.get().constructProjectURI(project, p);
	}


	@Override
	public boolean hasURI(URI uri) {
		if(uri.getScheme().equals("project")) {
			return true;
		}

		IFile file = Pica.get().getFileHandle(uri);
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


	private void initialize() {
	}


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
				throw new CoreException(new Status(IStatus.ERROR, null, "Path already exists, and is not a folder: " + member.getFullPath()));
			}
		}
		return parent;

	}

}
