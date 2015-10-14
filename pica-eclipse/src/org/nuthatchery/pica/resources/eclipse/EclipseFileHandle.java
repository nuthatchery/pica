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
package org.nuthatchery.pica.resources.eclipse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.eclipse.EclipsePicaInfra;
import org.nuthatchery.pica.resources.handles.IFileHandle;
import org.nuthatchery.pica.resources.handles.IFolderHandle;
import org.nuthatchery.pica.resources.handles.IResourceHandle;
import org.nuthatchery.pica.resources.managed.IManagedFile;
import org.nuthatchery.pica.resources.managed.IManagedResource;
import org.nuthatchery.pica.util.NullnessHelper;
import org.rascalmpl.parser.gtd.io.InputConverter;

public class EclipseFileHandle implements IFileHandle {
	private final IFile resource;
	private final URI uri;


	public EclipseFileHandle(URI uri, IFile resource) {
		this.uri = uri;
		this.resource = resource;
	}


	@Override
	public void clearFlags(int flags) {
		// TODO Auto-generated method stub

	}


	@Override
	public void create(int flags) throws IOException {
		// TODO Auto-generated method stub

	}


	@Override
	public boolean exists() {
		return resource.exists();
	}


	@Override
	public char[] getContentsCharArray() throws IOException {
		InputStream stream = getContentsStream();
		try {
			char[] cs = NullnessHelper.checkNonNull(InputConverter.toChar(stream, Charset.forName(getFile().getCharset())));
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
			return NullnessHelper.checkNonNull(getFile().getContents());
		}
		catch(CoreException e) {
			throw new IOException(e);
		}
	}


	@Override
	public String getContentsString() throws IOException {
		InputStream stream = getContentsStream();
		try {
			String string = new String(InputConverter.toChar(stream, Charset.forName(getFile().getCharset())));
			return string;
		}
		catch(CoreException e) {
			throw new IOException(e);
		}
		finally {
			stream.close();
		}
	}


	protected IFile getFile() {
		return resource;
	}


	@Override
	public int getLength() throws IOException {
		return getContentsCharArray().length;
	}


	@Override
	public long getModificationStamp() {
		return resource.getModificationStamp();
	}


	@Override
	@Nullable
	public IFolderHandle getParent() {
		IContainer parent = resource.getParent();
		// TODO FIXME  wrap parent into IFolderHandle
		return null;
	}


	@Override
	public URI getURI() {
		return uri;
	}


	@Override
	public boolean isDerived() {
		return resource.isDerived();
	}


	@Override
	public boolean isFile() {
		return true;
	}


	@Override
	public boolean isFolder() {
		return false;
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
	public boolean setContents(String contents) throws IOException {
		InputStream stream = new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
		try {
			resource.setContents(stream, true, true, null); // TODO / FIXME null should be a monitor
		}
		catch(CoreException e) {
			throw new IOException(e); // TODO maybe check the cause of CoreException, and throw it if it is already an IOException - or otherwise handle it in some way
		}
		return false;
	}


	@Override
	public void setFlags(int flags) {
		// TODO Auto-generated method stub

	}

}
