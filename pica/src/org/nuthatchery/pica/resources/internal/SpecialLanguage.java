package org.nuthatchery.pica.resources.internal;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.resources.ICompiler;
import org.nuthatchery.pica.resources.ILanguage;

public class SpecialLanguage implements ILanguage {
	public static final ILanguage INSTANCE = new SpecialLanguage();


	private SpecialLanguage() {
	}


	@Override
	public ICompiler getCompiler() {
		throw new UnsupportedOperationException();
	}


	@Override
	public Collection<String> getExtensions() {
		return Collections.EMPTY_SET;
	}


	@Override
	public String getFileName(String modName) {
		return modName;
	}


	@Override
	public String getId() {
		return "pica-special";
	}


	@Override
	public String getModuleName(String fileName) {
		return fileName;
	}


	@Override
	public String getName() {
		return "pica-special";
	}


	@Override
	public IConstructor getNameAST(String name) {
		throw new UnsupportedOperationException();
	}


	@Override
	public String getNameString(IConstructor nameAST) {
		throw new UnsupportedOperationException();
	}


	@Override
	public String getPreferredExtension() {
		return "";
	}


	@Override
	@Nullable
	public String getStoreExtension() {
		return "";
	}


	@Override
	public boolean hasExtension(String ext) {
		return false;
	}

}
