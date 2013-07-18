/**************************************************************************
 * Copyright (c) 2011-2012 Anya Helene Bagge
 * Copyright (c) 2011-2012 University of Bergen
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. See http://www.gnu.org/licenses/
 * 
 * 
 * See the file COPYRIGHT for more information.
 * 
 * Contributors:
 * * Anya Helene Bagge
 * 
 *************************************************************************/
package org.magnolialang.resources;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public final class LanguageRegistry {

	public Map<String, ILanguage> extensions = new HashMap<String, ILanguage>();


	public Map<String, ILanguage> languages = new HashMap<String, ILanguage>();


	private static volatile LanguageRegistry instance;


	private LanguageRegistry() {
	}


	public static ILanguage getLanguage(String lang) {
		return getInstance().languages.get(lang);
	}

	public static ILanguage getLanguageForFile(IPath file) {
		String extension = file.getFileExtension();
		if(extension == null) {
			return null;
		}
		else {
			return getInstance().extensions.get(extension);
		}
	}

	public static ILanguage getLanguageForFile(URI uri) {
		String extension = new Path(uri.getPath()).getFileExtension();
		if(extension == null) {
			return null;
		}
		else {
			return getInstance().extensions.get(extension);
		}
	}

	public static void registerLanguage(ILanguage lang) {
		getInstance().languages.put(lang.getId(), lang);
		for(String ext : lang.getExtensions()) {
			getInstance().extensions.put(ext, lang);
		}
	}


	private static LanguageRegistry getInstance() {
		if(instance == null) {
			synchronized(LanguageRegistry.class) {
				if(instance == null) {
					instance = new LanguageRegistry();
				}
			}
		}
		return instance;
	}

}
