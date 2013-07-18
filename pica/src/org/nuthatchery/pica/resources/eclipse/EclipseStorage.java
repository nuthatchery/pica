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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.nuthatchery.pica.resources.storage.IStorableValue;
import org.nuthatchery.pica.resources.storage.IStorage;

public class EclipseStorage implements IStorage {

	private static final long MAX_SIZE = 64 * 1024 * 1024;
	private final IFile file;
	private final Set<String> keys = new HashSet<String>();
	private final Map<String, byte[]> store = new HashMap<String, byte[]>();
	private long lastLoadStamp = 0L;
	private long lastSaveStamp = 0L;
	private long stamp = 0L;
	private static final boolean DISABLED = true;


	public EclipseStorage(IFile file) {
		this.file = file;
	}


	@Override
	public void declare(String key) {
		keys.add(key + ".data");
		keys.add(key + ".metadata");
	}


	@Override
	public <T extends IStorableValue> T get(String key, T storable) throws IOException {
		if(DISABLED) {
			return null;
		}
		byte[] data = store.get(key + ".data");
		byte[] metadata = store.get(key + ".metadata");
		if(data == null || metadata == null) {
			load();
			data = store.get(key + ".data");
			metadata = store.get(key + ".metadata");
		}

		if(data == null || metadata == null) {
			return null;
		}

		storable.setData(data);
		storable.setMetaData(metadata);
		return storable;
	}


	@Override
	public void put(String key, IStorableValue value) {
		declare(key);
		byte[] metaData = value.getMetaData();
		byte[] data = value.getData();
		synchronized(this) {
			store.put(key + ".data", data);
			store.put(key + ".metadata", metaData);
		}
		stamp++;
	}


	@Override
	public void save() throws IOException {
		if(DISABLED) {
			return;
		}
		lastSaveStamp = stamp;
		HashMap<String, byte[]> map;
		synchronized(this) {
			if(store.isEmpty()) {
				return;
			}
		}
		load();
		synchronized(this) {
			map = new HashMap<String, byte[]>(store);
			store.clear();
			lastLoadStamp = 0L;
		}
		if(map.isEmpty()) {
			return;
		}
		System.err.println("STORAGE: Saving file  " + file);
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024);
			ZipOutputStream zipStream = new ZipOutputStream(byteStream);
			for(Entry<String, byte[]> entry : map.entrySet()) {
				if(!keys.contains(entry.getKey())) {
					continue;
				}
				ZipEntry zipEntry = new ZipEntry(entry.getKey());
				byte[] data = entry.getValue();
				zipStream.putNextEntry(zipEntry);
				zipStream.write(data, 0, data.length);
				zipStream.closeEntry();
			}
			zipStream.close();
			ByteArrayInputStream inputStream = new ByteArrayInputStream(byteStream.toByteArray());
			if(file.exists()) {
				file.setContents(inputStream, false, false, null);
			}
			else {
				IContainer dir = EclipseWorkspaceManager.mkdir(file.getParent().getFullPath(), IResource.DERIVED | IResource.HIDDEN | IResource.FORCE);
				System.out.println("Dir: " + dir + " exists: " + dir.exists());
				file.create(inputStream, IResource.DERIVED | IResource.HIDDEN, null);
			}
		}
		catch(CoreException e) {
			synchronized(this) {
				store.putAll(map); // undo the clear
			}
			e.printStackTrace();
			throw new IOException("CoreException while saving file " + file, e);
		}
		catch(IllegalArgumentException e) {
//			synchronized(this) {
//				store.putAll(map); // undo the clear
//			}
//			e.printStackTrace();
			//throw new IOException("IllegalArgumentException while saving file " + file, e);
		}
	}


	@Override
	public IStorage subStorage(String name) {
		// TODO Auto-generated method stub
		return null;
	}


	private void load() throws IOException {
		try {
			if(file.exists()) {
				synchronized(this) {
					if(file.getModificationStamp() == lastLoadStamp) {
						return;
					}
				}
				System.err.println("STORAGE: Loading file " + file);
				Map<String, byte[]> newStore = new HashMap<String, byte[]>();
				//Job.getJobManager().beginRule(file, null);
				long modStamp = file.getModificationStamp();
				try {
					InputStream contents = file.getContents();

					try (ZipInputStream zipStream = new ZipInputStream(contents)) {
						ZipEntry entry = zipStream.getNextEntry();
						while(entry != null) {
							long size = entry.getSize();
							if(size < MAX_SIZE) {
								if(size >= 0) {
									byte[] bytes = new byte[(int) size];
									for(int pos = 0, read = 0; read >= 0 && pos < bytes.length; pos += read) {
										read = zipStream.read(bytes, pos, bytes.length - pos);
									}
									newStore.put(entry.getName(), bytes);
								}
								else {
									ByteArrayOutputStream bytes = new ByteArrayOutputStream();
									byte[] buffer = new byte[8192];
									while(true) {
										int read = zipStream.read(buffer, 0, 8192);
										if(read > 0) {
											bytes.write(buffer, 0, read);
										}
										else {
											break;
										}
									}
									newStore.put(entry.getName(), bytes.toByteArray());
								}
							}
							entry = zipStream.getNextEntry();
						}
					}
				}
				finally {
					//Job.getJobManager().endRule(file);
				}
				synchronized(this) {
					lastLoadStamp = modStamp;
					for(Entry<String, byte[]> e : newStore.entrySet()) {
						if(!store.containsKey(e.getKey())) {
							store.put(e.getKey(), e.getValue());
						}
					}
				}
			}
		}
		catch(CoreException e) {
			e.printStackTrace();
			throw new IOException("CoreException while loading file " + file, e);
		}
	}

}
