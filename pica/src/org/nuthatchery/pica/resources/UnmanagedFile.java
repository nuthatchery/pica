/**************************************************************************
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
 * * Tero Hasu
 * 
 *************************************************************************/
package org.nuthatchery.pica.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.eclipse.imp.pdb.facts.IAnnotatable;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.rascalmpl.parser.gtd.io.InputConverter;
import org.rascalmpl.uri.URIUtil;

public class UnmanagedFile implements IManagedFile {
	protected final File file;

	protected final URI uri;


	public UnmanagedFile(File file) throws URISyntaxException {
		this.file = file;
		this.uri = URIUtil.createFile(file.getPath());
	}


	@Override
	public <T, E extends Throwable> T accept(IValueVisitor<T, E> v) throws E {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public IAnnotatable<? extends IValue> asAnnotatable() {
		return null;
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
	public Type getType() {
		return IManagedResource.ResourceType;
	}


	@Override
	public URI getURI() {
		return uri;
	}


	@Override
	public boolean isAnnotatable() {
		return false;
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
	public boolean isEqual(IValue other) {
		// TODO Auto-generated method stub
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


	/**
	 * Does nothing as the file contents are not cached. If the underlying file
	 * changes, then that change will automatically be reflected here.
	 */
	@Override
	public void onResourceChanged() {
	}


	@Override
	public boolean setContents(String contents) throws IOException {
		return false;
	}

}
