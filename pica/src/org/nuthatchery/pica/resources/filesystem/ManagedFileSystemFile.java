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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.nuthatchery.pica.resources.IManagedFile;
import org.nuthatchery.pica.resources.IManagedResource;
import org.nuthatchery.pica.resources.IResourceManager;
import org.nuthatchery.pica.resources.internal.AbstractManagedResource;
import org.rascalmpl.parser.gtd.io.InputConverter;
import org.rascalmpl.uri.URIUtil;

public class ManagedFileSystemFile extends AbstractManagedResource implements IManagedFile {
	protected final File file;
	protected final IResourceManager manager;


	public ManagedFileSystemFile(IResourceManager manager, File file) throws URISyntaxException {
		super(URIUtil.createFile(file.getPath()));
		this.manager = manager;
		this.file = file;
	}


	@Override
	public char[] getContentsCharArray() throws IOException {
		InputStream stream = getContentsStream();
		try {
			char[] cs = InputConverter.toChar(stream, Charset.forName("UTF-8"));
			return cs;
		}
		finally {
			stream.close();
		}
	}


	@Override
	public InputStream getContentsStream() throws IOException {
		return new FileInputStream(file);
	}


	@Override
	public String getContentsString() throws IOException {
		InputStream stream = getContentsStream();
		try {
			String string = new String(InputConverter.toChar(stream, Charset.forName("UTF-8")));
			return string;
		}
		finally {
			stream.close();
		}
	}


	@Override
	public long getModificationStamp() {
		return file.lastModified();
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
