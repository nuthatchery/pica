package org.magnolialang.resources.internal;

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
	public boolean isFile() {
		return true;
	}


	@Override
	public boolean isFolder() {
		return false;
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
	public boolean setContents(String contents) throws IOException {
		return false;
	}


	@Override
	public String getContentsString() throws IOException {
		InputStream stream = getContentsStream();
		return new String(InputConverter.toChar(stream));
	}


	@Override
	public char[] getContentsCharArray() throws IOException {
		InputStream stream = getContentsStream();
		return InputConverter.toChar(stream);
	}


	@Override
	public IManagedResource getParent() {
		// TODO should return a folder
		return manager;
	}


	@Override
	public Kind getResourceKind() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void onResourceChanged() {
		// TODO Auto-generated method stub

	}

}
