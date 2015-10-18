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
package org.nuthatchery.pica.resources.handles.filesystem;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.resources.handles.AbstractResourceHandle;
import org.nuthatchery.pica.resources.handles.IFolderHandle;
import org.nuthatchery.pica.resources.handles.IResourceHandle;

public abstract class AbstractFSResourceHandle extends AbstractResourceHandle<Path> implements IResourceHandle {
	public static IResourceHandle makeHandle(Path path) {
		if(Files.isDirectory(path)) {
			return new FSFolderHandle(path);
		}
		else if(Files.isRegularFile(path)) {
			return new FSFileHandle(path);
		}
		else {
			throw new IllegalArgumentException("Not a file or directory");
		}
	}


	protected AbstractFSResourceHandle(Path path) {
		super(path);
	}


	@Override
	public boolean exists() {
		return Files.exists(resource);
	}


	@Override
	public long getModificationStamp() {
		try {
			FileTime fileTime;
			fileTime = Files.getLastModifiedTime(resource);
			return fileTime.toMillis();
		}
		catch(IOException e) {
			return 0L;
		}
	}


	@Override
	@Nullable
	public IFolderHandle getParent() {
		Path path = resource.getParent();
		if(path != null) {
			IResourceHandle handle = makeHandle(path);
			assert handle instanceof IFolderHandle;
			return (IFolderHandle) handle;
		}
		else {
			return null;
		}
	}


	@Override
	public URI getURI() {
		return resource.toUri();
	}


	@Override
	public boolean isDerived() {
		return false; // derived files not supported
	}


	@Override
	public boolean isHidden() {
		try {
			return Files.isHidden(resource);
		}
		catch(IOException e) {
			return false;
		}
	}


	@Override
	public boolean isReadable() {
		return Files.isReadable(resource);
	}


	@Override
	public boolean isWritable() {
		return Files.isWritable(resource);
	}


	@Override
	public boolean setDerived(boolean derived) throws IOException {
		return false;
	}


	@Override
	public boolean setHidden(boolean hidden) throws IOException {
		return false;
	}


	@Override
	public boolean setReadable(boolean readable) throws IOException {
		return resource.toFile().setReadable(readable, false);
	}


	@Override
	public boolean setWritable(boolean writable) throws IOException {
		if(writable) {
			return resource.toFile().setWritable(writable, true);
		}
		else {
			return resource.toFile().setWritable(writable, false);
		}

	}

}
