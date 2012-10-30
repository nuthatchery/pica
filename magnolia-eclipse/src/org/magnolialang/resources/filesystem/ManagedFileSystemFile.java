package org.magnolialang.resources.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.magnolialang.resources.IManagedFile;
import org.magnolialang.resources.IManagedResource;
import org.magnolialang.resources.IResourceManager;
import org.magnolialang.resources.internal.AbstractManagedResource;
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
			char[] cs = InputConverter.toChar(stream);
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
			String string = new String(InputConverter.toChar(stream));
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
