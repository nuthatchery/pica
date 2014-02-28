/**************************************************************************
 * Copyright (c) 2012 Anya Helene Bagge
 * Copyright (c) 2012 Tero Hasu
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
 * * Tero Hasu
 * 
 *************************************************************************/
package org.nuthatchery.pica;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IPath;
import org.nuthatchery.pica.errors.Severity;
import org.nuthatchery.pica.resources.IWorkspaceConfig;
import org.rascalmpl.uri.BadURIException;

public abstract class AbstractPicaInfra implements IPica {
	protected final IWorkspaceConfig config;


	public AbstractPicaInfra(IWorkspaceConfig config) {
		this.config = config;
		Pica.set(this);
	}


	@Override
	public URI constructProjectURI(String project, IPath path) {
		try {
			// making sure that spaces in 'path' are properly escaped
			path = path.makeAbsolute();
			return new URI("project", project, path.toString(), null, null);
		}
		catch(URISyntaxException usex) {
			throw new BadURIException(usex);
		}
	}


	@Override
	public IWorkspaceConfig getConfig() {
		return config;
	}


	@Override
	public void logMessage(String msg) {
		logMessage(msg, Severity.DEFAULT);
	}

}
