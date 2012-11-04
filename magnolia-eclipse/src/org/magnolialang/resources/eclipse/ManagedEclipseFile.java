package org.magnolialang.resources.eclipse;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.magnolialang.eclipse.MagnoliaPlugin;
import org.magnolialang.resources.IManagedFile;
import org.magnolialang.resources.IManagedResource;
import org.magnolialang.resources.IResourceManager;
import org.magnolialang.resources.internal.AbstractManagedResource;
import org.rascalmpl.parser.gtd.io.InputConverter;

public class ManagedEclipseFile extends AbstractManagedResource implements IManagedFile {
	protected final IFile resource;
	protected final IResourceManager manager;


	public ManagedEclipseFile(IResourceManager manager, IFile resource) {
		super(MagnoliaPlugin.constructProjectURI(resource.getProject(), resource.getProjectRelativePath()));
		this.manager = manager;
		this.resource = resource;
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
		try {
			return resource.getContents();
		}
		catch(CoreException e) {
			throw new IOException(e);
		}
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
		return resource.getModificationStamp();
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
