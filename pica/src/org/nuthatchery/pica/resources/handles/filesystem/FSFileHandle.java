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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.nuthatchery.pica.resources.handles.IFileHandle;
import org.nuthatchery.pica.resources.handles.IFolderHandle;
import org.nuthatchery.pica.resources.handles.PicaOpenOption;
import org.nuthatchery.pica.tasks.ITaskMonitor;
import org.nuthatchery.pica.tasks.TaskId;

public class FSFileHandle extends AbstractFSResourceHandle implements IFileHandle {


	public FSFileHandle(Path path) {
		super(path);
	}


	@Override
	public void create(ITaskMonitor tm, OpenOption... flags) throws IOException {
		tm.begin(new TaskId("FSFileHandle.create", "Create " + resource, resource));
		List<OpenOption> options = new ArrayList<>();

		boolean makeParents = false;
		for(OpenOption o : flags) {
			if(o instanceof StandardOpenOption) {
				options.add(o);
			}
			else if(o == PicaOpenOption.CREATE_PARENTS) {
				makeParents = true;
			}
		}

		if(makeParents) {
			IFolderHandle parent = getParent();
			if(!parent.exists()) {
				parent.create(tm.subMonitor(), PicaOpenOption.CREATE_PARENTS);
			}
		}

		if(!exists()) {
			try (OutputStream stream = Files.newOutputStream(resource, (OpenOption[]) options.toArray())) {

			}
		}
	}


	@Override
	public char[] getContentsCharArray(ITaskMonitor tm) throws IOException {
		byte[] byteArray = getRawContentsByteArray(tm);
		CharBuffer decoded = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(byteArray));

		char[] charArray = new char[decoded.length()];
		decoded.get(charArray);
		return charArray;
	}


	@Override
	public String getContentsString(ITaskMonitor tm) throws IOException {
		byte[] byteArray = getRawContentsByteArray(tm);
		return new String(byteArray, StandardCharsets.UTF_8).intern();
	}


	@Override
	public long getLength(ITaskMonitor tm) throws IOException {
		tm.begin(new TaskId("FSFileHandle.stat", "Stat " + resource, resource));
		return Files.size(resource);
	}


	@Override
	public byte[] getRawContentsByteArray(ITaskMonitor tm) throws IOException {
		tm.begin(new TaskId("FSFileHandle.read", "Read " + resource, resource));
		byte[] bytes = Files.readAllBytes(resource);
		return bytes;
	}


	@Override
	public InputStream getRawContentsStream(ITaskMonitor tm) throws IOException {
		tm.begin(new TaskId("FSFileHandle.read", "Read " + resource, resource));
		return Files.newInputStream(resource);
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
	public boolean setContents(String contents, ITaskMonitor tm, OpenOption... flags) throws IOException {
		tm.begin(new TaskId("FSFileHandle.write", "Write " + resource, resource));


		List<OpenOption> options = new ArrayList<>();

		boolean makeParents = false;
		for(OpenOption o : flags) {
			if(o instanceof StandardOpenOption) {
				options.add(o);
			}
			else if(o == PicaOpenOption.CREATE_PARENTS) {
				makeParents = true;
			}
		}

		if(makeParents) {
			IFolderHandle parent = getParent();
			if(!parent.exists()) {
				parent.create(tm.subMonitor(), PicaOpenOption.CREATE_PARENTS);
			}
		}


		try (BufferedWriter writer = Files.newBufferedWriter(resource, (OpenOption[]) options.toArray())) {
			writer.write(contents);
		}

		return true;
	}


}
