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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.errors.Severity;
import org.nuthatchery.pica.resources.IWorkspaceConfig;
import org.rascalmpl.uri.BadURIException;

public abstract class AbstractPicaInfra implements IPica {
	protected final IWorkspaceConfig config;
	protected final PrintWriter err;
	protected boolean errIsSystemErr = false;


	public AbstractPicaInfra(IWorkspaceConfig config) {
		this.config = config;
		Pica.set(this);
		PrintWriter writer;
		try {
			writer = new PrintWriter(Files.newBufferedWriter(Paths.get("/tmp/magnolia.log"), Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
		}
		catch(IOException e) {
			writer = new PrintWriter(System.err);
			errIsSystemErr = true;
		}
		err = writer;

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
	public void logException(@Nullable String msg, @Nullable Throwable t) {
		err.println("error: " + (t != null ? t.toString() : "no exception") + " (" + (msg != null ? msg : "no details") + ")");
		if(t != null) {
			t.printStackTrace(err);
		}
		err.flush();
	}


	@Override
	public void logMessage(String msg) {
		logMessage(msg, Severity.DEFAULT);
	}


	@Override
	public void logMessage(String msg, Severity severity) {
		err.println(severity.toString() + ": " + msg);
		err.flush();
	}


	@Override
	public void println(String msg) {
		err.println(msg);
		err.flush();
		if(!errIsSystemErr) {
			if(msg.length() > 256) {
				System.err.println(msg.substring(0, 256) + "...");
			}
			else {
				System.err.println(msg);
			}
		}
	}

}
