/**************************************************************************
 * Copyright (c) 2012-2013 Anya Helene Bagge
 * Copyright (c) 2012-2013 Tero Hasu
 * Copyright (c) 2012-2013 University of Bergen
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
package org.nuthatchery.pica.eclipse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.model.ISourceProject;

import org.nuthatchery.pica.AbstractPicaInfra;
import org.nuthatchery.pica.Pica;
import org.nuthatchery.pica.errors.Severity;
import org.nuthatchery.pica.resources.IWorkspaceConfig;
import org.nuthatchery.pica.resources.IWorkspaceManager;
import org.nuthatchery.pica.resources.eclipse.EclipseWorkspaceManager;
import org.rascalmpl.uri.UnsupportedSchemeException;

public final class EclipsePicaInfra extends AbstractPicaInfra {

	public EclipsePicaInfra(IWorkspaceConfig config) {
		super(config);
	}


	@Override
	public boolean areModuleFactsPreloaded() {
		return true;
	}


	/**
	 * @param uri
	 *            The URI of the desired file
	 * @return An IFile representing the URI
	 */
	@Override
	public IFile getFileHandle(URI uri) {
		IPath path = null;
		try {
			path = new Path(new File(Pica.getResolverRegistry().getResourceURI(uri)).getAbsolutePath());
		}
		catch(UnsupportedSchemeException e) {
			Pica.get().logException(e.getMessage(), e);
			e.printStackTrace();
			return null;
		}
		catch(IOException e) {
			Pica.get().logException(e.getMessage(), e);
			e.printStackTrace();
			return null;
		}
		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
	}


	@Override
	public IWorkspaceManager getWorkspaceManager() {
		return EclipseWorkspaceManager.getInstance(config);
	}


	@Override
	public void logException(String msg, Throwable t) {
		PicaActivator.getDefault().logMsg(msg, Severity.ERROR, t);
	}


	@Override
	public void logMessage(String msg, Severity severity) {
		PicaActivator.getDefault().logMsg(msg, severity, null);

	}


	public static URI constructProjectURI(IProject project, IPath path) {
		return Pica.get().constructProjectURI(project.getName(), path);
	}


	public static URI constructProjectURI(ISourceProject project, IPath path) {
		return Pica.get().constructProjectURI(project.getRawProject().getName(), path);
	}


	public static void setInfra(IWorkspaceConfig config) {
		Pica.set(new EclipsePicaInfra(config));
	}

}
