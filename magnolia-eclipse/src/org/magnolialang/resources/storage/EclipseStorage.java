package org.magnolialang.resources.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class EclipseStorage implements IStorage {

	private final IFile							file;
	private final Map<String, IStorableValue>	map				= new HashMap<String, IStorableValue>();
	private long								lastLoadStamp	= 0L;
	private long								lastSaveStamp	= 0L;
	private long								stamp			= 0L;


	public EclipseStorage(IFile file) {
		this.file = file;
	}


	@Override
	public void save() throws IOException {
		lastSaveStamp = stamp;
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024);
			ZipOutputStream zipStream = new ZipOutputStream(byteStream);
			for(Entry<String, IStorableValue> entry : map.entrySet()) {
				ZipEntry zipEntry = new ZipEntry(entry.getKey());
				zipEntry.setComment(entry.getValue().getMetaData());
				zipStream.putNextEntry(zipEntry);
				byte[] data = entry.getValue().getData();
				zipStream.write(data, 0, data.length);
			}
			zipStream.close();
			ByteArrayInputStream inputStream = new ByteArrayInputStream(byteStream.toByteArray());
			if(file.exists())
				file.setContents(inputStream, false, false, null);
			else {
				file.create(inputStream, IFile.DERIVED | IFile.HIDDEN, null);
			}
		}
		catch(CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lastLoadStamp = file.getModificationStamp();
	}


	private void load() {
		lastLoadStamp = file.getModificationStamp();
	}


	@Override
	public void put(String key, IStorableValue value) {
		map.put(key, value);
		System.err.println("Storage put: " + key + " in file " + file);
		stamp++;
	}


	@Override
	public <T extends IStorableValue> T get(String key, T storable) throws IOException {

		return null;
	}


	@Override
	public IStorage subStorage(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
