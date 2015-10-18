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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.nuthatchery.pica.resources.handles.IFileHandle;
import org.nuthatchery.pica.resources.handles.IFolderHandle;
import org.nuthatchery.pica.resources.handles.PicaOpenOption;
import org.nuthatchery.pica.tasks.ITaskMonitor;
import org.nuthatchery.pica.tasks.TaskId;
import org.nuthatchery.pica.tasks.eclipse.EclipseTaskMonitor;
import org.nuthatchery.pica.util.NullnessHelper;
import org.rascalmpl.parser.gtd.io.InputConverter;

public class EclipseFileHandle extends EclipseResourceHandle implements IFileHandle {

	protected EclipseFileHandle(IFile resource) {
		super(resource);
	}


	protected void create(int updateFlags, boolean ignoreExisting, ITaskMonitor tm) throws CoreException {
		try (ByteArrayInputStream stream = new ByteArrayInputStream(new byte[0])) {
			if(!ignoreExisting || !resource.exists())
				((IFile) resource).create(stream, updateFlags, EclipseTaskMonitor.makeProgressMonitor(tm, 100));
		}
		catch(IOException e) {
			// never happens
			e.printStackTrace();
		}
	}


	@Override
	public char[] getContentsCharArray(ITaskMonitor tm) throws IOException {
		InputStream stream = getRawContentsStream(tm);
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
	public String getContentsString(ITaskMonitor tm) throws IOException {
		InputStream stream = getRawContentsStream(tm);
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
		return (IFile) resource;
	}


	@Override
	public long getLength(ITaskMonitor tm) throws IOException {
		return getContentsCharArray(tm).length;
	}


	@Override
	public byte[] getRawContentsByteArray(ITaskMonitor tm) throws IOException {
		return getContentsString(tm).getBytes(StandardCharsets.UTF_8);
	}


	@Override
	public InputStream getRawContentsStream(ITaskMonitor tm) throws IOException {
		try {
			return NullnessHelper.checkNonNull(getFile().getContents());
		}
		catch(CoreException e) {
			throw new IOException(e);
		}
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
	public boolean setContents(String contents, ITaskMonitor tm, OpenOption... options) throws IOException {
		tm.begin(new TaskId("FSFileHandle.write", "Write " + resource, resource));

		boolean makeParents = false;
		for(OpenOption o : options) {
			if(o == PicaOpenOption.CREATE_PARENTS) {
				makeParents = true;
			}
		}

		if(makeParents) {
			IFolderHandle parent = getParent();
			if(!parent.exists()) {
				parent.create(tm.subMonitor(), PicaOpenOption.CREATE_PARENTS);
			}
		}

		InputStream stream = new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
		try {
			getFile().setContents(stream, true, true, EclipseTaskMonitor.makeProgressMonitor(tm, 100)); // TODO / FIXME null should be a monitor
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


}
