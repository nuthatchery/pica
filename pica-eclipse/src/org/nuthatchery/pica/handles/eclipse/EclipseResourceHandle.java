/**************************************************************************
 * Copyright (c) 2011-2012 Anya Helene Bagge
 * Copyright (c) 2011-2012 University of Bergen
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
package org.nuthatchery.pica.handles.eclipse;

import java.io.IOException;
import java.net.URI;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.eclipse.EclipsePicaInfra;
import org.nuthatchery.pica.resources.handles.AbstractResourceHandle;
import org.nuthatchery.pica.resources.handles.IFolderHandle;
import org.nuthatchery.pica.resources.handles.IResourceHandle;
import org.nuthatchery.pica.resources.handles.PicaOpenOption;
import org.nuthatchery.pica.tasks.ITaskMonitor;
import org.nuthatchery.pica.tasks.TaskId;
import org.nuthatchery.pica.tasks.eclipse.EclipseTaskMonitor;

public abstract class EclipseResourceHandle extends AbstractResourceHandle<IResource> implements IResourceHandle {
	public static IResourceHandle makeHandle(IResource resource) {
		if(resource instanceof IContainer) {
			return new EclipseFolderHandle((IContainer) resource);
		}
		else if(resource instanceof IFile) {
			return new EclipseFileHandle((IFile) resource);
		}
		else {
			throw new IllegalArgumentException("Unknown resource kind");
		}
	}


	protected EclipseResourceHandle(IResource resource) {
		super(resource);
	}


	protected abstract void create(int updateFlags, boolean ignoreExisting, ITaskMonitor tm) throws CoreException;


	@Override
	public void create(ITaskMonitor tm, OpenOption... flags) throws IOException {
		tm.begin(new TaskId("EclipseFileHandle.create", "Create " + resource, resource));

		int updateFlags = IResource.FORCE;
		boolean makeParents = false;
		boolean ignoreExisting = true;
		for(OpenOption o : flags) {
			if(o == StandardOpenOption.CREATE_NEW) {
				ignoreExisting = false;
			}
			else if(o == PicaOpenOption.CREATE_PARENTS) {
				makeParents = true;
			}
			else if(o == PicaOpenOption.DERIVED) {
				updateFlags |= IResource.DERIVED;
			}
			else if(o == PicaOpenOption.HIDDEN) {
				updateFlags |= IResource.HIDDEN;
			}

		}

		if(makeParents) {
			IFolderHandle parent = getParent();
			if(!parent.exists()) {
				parent.create(tm.subMonitor(), PicaOpenOption.CREATE_PARENTS);
			}
		}

		try {
			if(!ignoreExisting || !resource.exists())
				create(updateFlags, ignoreExisting, tm);
		}
		catch(CoreException e) {
			Throwable cause = e.getCause();
			if(cause instanceof IOException)
				throw (IOException) cause;
			else
				throw new IOException(e);
		}
	}


	@Override
	public boolean exists() {
		return resource.exists();
	}


	public IResource getEclipseResource() {
		return resource;
	}


	@Override
	public long getModificationStamp() {
		return resource.getModificationStamp();
	}


	@Override
	@Nullable
	public IFolderHandle getParent() {
		IContainer parent = resource.getParent();
		return EclipsePicaInfra.getResourceHandle(parent);
	}


	@Override
	public URI getURI() {
		return EclipsePicaInfra.constructProjectURI(resource.getProject(), resource.getProjectRelativePath());
	}


	@Override
	public boolean isDerived() {
		return resource.isDerived();
	}


	@Override
	public boolean isHidden() {
		return resource.isHidden();
	}


	@Override
	public boolean isReadable() {
		return resource.isAccessible();
	}


	@Override
	public boolean isWritable() {
		return resource.isAccessible() && !resource.isReadOnly();
	}


	@Override
	public boolean setDerived(boolean derived) throws IOException {
		try {
			resource.setDerived(derived);
		}
		catch(CoreException e) {
			Throwable cause = e.getCause();
			if(cause instanceof IOException)
				throw (IOException) cause;
			else
				throw new IOException(e);
		}
		return true;
	}


	@Override
	public boolean setHidden(boolean hidden) throws IOException {
		try {
			resource.setHidden(hidden);
		}
		catch(CoreException e) {
			Throwable cause = e.getCause();
			if(cause instanceof IOException)
				throw (IOException) cause;
			else
				throw new IOException(e);
		}
		return true;
	}


	@Override
	public boolean setReadable(boolean readable) throws IOException {
		return readable; // all files are readable
	}


	@Override
	public boolean setWritable(boolean writable) throws IOException {
		resource.setReadOnly(!writable);
		return true;
	}
}
