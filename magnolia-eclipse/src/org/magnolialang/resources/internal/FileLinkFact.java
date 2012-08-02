package org.magnolialang.resources.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.magnolialang.resources.ILanguage;
import org.magnolialang.resources.IManagedFile;
import org.magnolialang.tasks.IFact;
import org.magnolialang.tasks.Transaction;
import org.magnolialang.tasks.facts.AbstractFact;

public class FileLinkFact extends AbstractFact<IValue> implements IManagedFile {
	private final IManagedFile	link;


	public FileLinkFact(IManagedFile link, Type key, IValue name) {
		super(Transaction.makeKey(key, name), link.getURI().toString(), null);
		this.link = link;
		link.registerListener(this);
	}


	@Override
	public URI getURI() {
		return link.getURI();
	}


	@Override
	public boolean isFile() {
		return link.isFile();
	}


	@Override
	public boolean isFolder() {
		return link.isFolder();
	}


	@Override
	public ILanguage getLanguage() {
		return link.getLanguage();
	}


	@Override
	public boolean isValid() {
		return link.isValid();
	}


	@Override
	public IValue getValue() {
		return link.getValue();
	}


	@Override
	public boolean setValue(IValue val) {
		return link.setValue(val);
	}


	@Override
	public boolean updateFrom(IFact<IValue> fact) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void changed(IFact<?> f, Change c, Object moreInfo) {
		switch(c) {
		case CHANGED:
			notifyChanged();
			break;
		case INVALIDATED:
			notifyInvalidated();
			break;
		case AVAILABLE:
			notifyAvailable();
			break;
		case REMOVED:
			remove();
			break;
		case EXPIRED:
			break;
		case MOVED_TO:
			break;
		default:
			throw new IllegalArgumentException("Illegal change kind " + c);
		}
	}


	@Override
	public InputStream getContentsStream() throws IOException {
		return link.getContentsStream();
	}


	@Override
	public String getContentsString() throws IOException {
		return link.getContentsString();
	}


	@Override
	public boolean setContents(String contents) throws IOException {
		return link.setContents(contents);
	}


	@Override
	public char[] getContentsCharArray() throws IOException {
		return link.getContentsCharArray();
	}


	@Override
	public IPath getPath() {
		return link.getPath();
		// link.getFullPath().makeRelativeTo(link.getProject().getFullPath());
	}


	@Override
	public IPath getFullPath() {
		return link.getFullPath();
	}


	@Override
	public IProject getProject() {
		return link.getProject();
	}

}
