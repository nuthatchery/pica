package org.magnolialang.resources.internal;

import static org.magnolialang.terms.TermFactory.vf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.magnolialang.errors.CompilationError;
import org.magnolialang.resources.ILanguage;
import org.magnolialang.resources.IManagedFile;
import org.magnolialang.resources.ResourceManager;
import org.rascalmpl.parser.gtd.io.InputConverter;
import org.rascalmpl.tasks.IFact;

public class FileFact extends ManagedResource implements IManagedFile {

	private final ILanguage lang;

	public FileFact(ResourceManager manager, IFile resource, ILanguage lang) {
		super(manager, resource);
		this.lang = lang;
	}

	@Override
	public IValue getValue() {
		try {
			return vf.string(getContentsString());
		}
		catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean setValue(IValue val) throws CompilationError {
		if(val instanceof IString) {
			try {
				return setContents(((IString) val).getValue());
			}
			catch(IOException e) {
				throw new CompilationError("Failed to set file contents", e);
			}
		}
		else
			throw new CompilationError(
					"Failed to set file contents: expected an IString, but got "
							+ val.getType().getName());
	}

	@Override
	public void setDepends(Collection<IFact<IValue>> deps) {
		throw new UnsupportedOperationException("setDepends");
	}

	@Override
	public boolean updateFrom(IFact<IValue> fact) {
		throw new UnsupportedOperationException("updateFrom");
	}

	@Override
	public void changed(IFact<?> f, Change c, Object moreInfo) {
		if(f == null && c == Change.CHANGED) {
			notifyChanged();
		}
		else
			throw new IllegalArgumentException("f=" + f + ", c=" + c);
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
	public ILanguage getLanguage() {
		return lang;
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
		return InputConverter.toChar(stream).toString();
	}

	@Override
	public char[] getContentsCharArray() throws IOException {
		InputStream stream = getContentsStream();
		return InputConverter.toChar(stream);
	}

}
