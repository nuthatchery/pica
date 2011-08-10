package org.magnolialang.resources;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

public class LanguageRegistry {
	public Map<String, ILanguage> extensions = new HashMap<String, ILanguage>();
	public Map<String, ILanguage> languages = new HashMap<String, ILanguage>();
	private static LanguageRegistry instance;

	private LanguageRegistry() {
	}

	private static LanguageRegistry getInstance() {
		if(instance == null)
			instance = new LanguageRegistry();
		return instance;
	}

	public static ILanguage getLanguage(String lang) {
		return getInstance().languages.get(lang);
	}

	public static ILanguage getLanguageForFile(IFile file) {
		String extension = file.getFileExtension();
		if(extension != null)
			return getInstance().extensions.get(extension);
		else
			return null;
	}

	public static ILanguage getLanguageForFile(IPath file) {
		String extension = file.getFileExtension();
		if(extension != null)
			return getInstance().extensions.get(extension);
		else
			return null;
	}

	public static void registerLanguage(ILanguage lang) {
		getInstance().languages.put(lang.getId(), lang);
		for(String ext : lang.getExtensions())
			getInstance().extensions.put(ext, lang);
	}

}