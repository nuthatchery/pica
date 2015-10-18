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
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.nuthatchery.pica.eclipse.EclipsePicaInfra;
import org.nuthatchery.pica.resources.handles.IFolderHandle;
import org.nuthatchery.pica.resources.handles.IResourceHandle;
import org.nuthatchery.pica.tasks.ITaskMonitor;
import org.nuthatchery.pica.tasks.eclipse.EclipseTaskMonitor;

public class EclipseFolderHandle extends EclipseResourceHandle implements IFolderHandle {

	protected EclipseFolderHandle(IContainer resource) {
		super(resource);
	}


	protected void create(int updateFlags, boolean ignoreExisting, ITaskMonitor tm) throws CoreException {
		if(!ignoreExisting || !resource.exists())
			((IFolder) resource).create(updateFlags, true, EclipseTaskMonitor.makeProgressMonitor(tm, 100));
	}


	@Override
	public Collection<IResourceHandle> getChildren(ITaskMonitor tm) throws IOException {
		IContainer container = (IContainer) resource;
		try {
			IResource[] members = container.members();
			Collection<IResourceHandle> children = new ArrayList<>();
			for(IResource r : members) {
				children.add(EclipsePicaInfra.getResourceHandle(r));
			}
			return children;
		}
		catch(CoreException e) {
			throw new IOException(e);
		}
	}


	@Override
	public Collection<IResourceHandle> getChildren(String glob, ITaskMonitor tm) throws IOException {
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean isFile() {
		return false;
	}


	@Override
	public boolean isFolder() {
		return true;
	}


}
