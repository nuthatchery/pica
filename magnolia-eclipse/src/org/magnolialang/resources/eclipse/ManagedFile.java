package org.magnolialang.resources.eclipse;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.magnolialang.resources.IManagedFile;
import org.magnolialang.resources.IManagedResource;
import org.magnolialang.resources.IResourceManager;
import org.rascalmpl.parser.gtd.io.InputConverter;

public class ManagedFile extends AbstractManagedResource implements IManagedFile {
	public ManagedFile(IResourceManager manager, IResource resource) {
		super(manager, resource);
	}


	@Override
	public char[] getContentsCharArray() throws IOException {
		InputStream stream = getContentsStream();
		char[] cs = InputConverter.toChar(stream);
		stream.close();
		return cs;
	}


	@Override
	public InputStream getContentsStream() throws IOException {
		try {
			return ((IFile) resource).getContents();
		}
		catch(CoreException e) {
			throw new IOException(e);
		}
	}


	@Override
	public String getContentsString() throws IOException {
		InputStream stream = getContentsStream();
		String string = new String(InputConverter.toChar(stream));
		stream.close();
		return string;
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
