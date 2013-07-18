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
package org.nuthatchery.pica.resources.eclipse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.nuthatchery.pica.EclipsePicaInfra;
import org.nuthatchery.pica.resources.IManagedFile;
import org.nuthatchery.pica.resources.IManagedResource;
import org.nuthatchery.pica.resources.IResourceManager;
import org.nuthatchery.pica.resources.internal.AbstractManagedResource;
import org.rascalmpl.parser.gtd.io.InputConverter;

public class ManagedEclipseFile extends AbstractManagedResource implements IManagedFile {
	protected final IFile resource;
	protected final IResourceManager manager;


	public ManagedEclipseFile(IResourceManager manager, IFile resource) {
		super(EclipsePicaInfra.constructProjectURI(resource.getProject(), resource.getProjectRelativePath()));
		this.manager = manager;
		this.resource = resource;
	}


	@Override
	public char[] getContentsCharArray() throws IOException {
		InputStream stream = getContentsStream();
		try {
			char[] cs = InputConverter.toChar(stream, Charset.forName(resource.getCharset()));
			return cs;
		}
		catch(CoreException e) {
			throw new IOException(e);
		}
		finally {
			stream.close();
		}
	}


	@Override
	public InputStream getContentsStream() throws IOException {
		try {
			return resource.getContents();
		}
		catch(CoreException e) {
			throw new IOException(e);
		}
	}


	@Override
	public String getContentsString() throws IOException {
		InputStream stream = getContentsStream();
		try {
			String string = new String(InputConverter.toChar(stream, Charset.forName(resource.getCharset())));
			return string;
		}
		catch(CoreException e) {
			throw new IOException(e);
		}
		finally {
			stream.close();
		}
	}


	@Override
	public long getModificationStamp() {
		return resource.getModificationStamp();
	}


	@Override
	public IManagedResource getParent() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean isCodeUnit() {
		return false;
	}


	@Override
	public boolean isContainer() {
		return false;
	}


	@Override
	public boolean isFile() {
		return true;
	}


	@Override
	public boolean isProject() {
		return false;
	}


	@Override
	public void onResourceChanged() {
	}


	@Override
	public boolean setContents(String contents) throws IOException {
		return false;
	}

}