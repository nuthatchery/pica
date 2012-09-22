package org.magnolialang.resources;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.magnolialang.nullness.Nullable;

public final class LanguageRegistry {
	@Nullable
	public static ILanguage getLanguage(String lang) {
		return getInstance().languages.get(lang);
	}
	@Nullable
	public static ILanguage getLanguageForFile(IPath file) {
		String extension = file.getFileExtension();
		if(extension == null)
			return null;
		else
			return getInstance().extensions.get(extension);
	}
	@Nullable
	public static ILanguage getLanguageForFile(URI uri) {
		String extension = new Path(uri.getPath()).getFileExtension();
		if(extension == null)
			return null;
		else
			return getInstance().extensions.get(extension);
	}


	public static void registerLanguage(ILanguage lang) {
		getInstance().languages.put(lang.getId(), lang);
		for(String ext : lang.getExtensions())
			getInstance().extensions.put(ext, lang);
	}


	private static LanguageRegistry getInstance() {
		if(instance == null)
			synchronized(LanguageRegistry.class) {
				if(instance == null)
					instance = new LanguageRegistry();
			}
		return instance;
	}


	public Map<String, ILanguage>				extensions	= new HashMap<String, ILanguage>();


	public Map<String, ILanguage>				languages	= new HashMap<String, ILanguage>();


	private static volatile LanguageRegistry	instance;


	private LanguageRegistry() {
	}

}
