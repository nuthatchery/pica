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
 * it under the terms of the GNU Lesser General Public License as published by
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
package org.nuthatchery.pica.resources;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

public final class LanguageRegistry {

	private static volatile LanguageRegistry instance = new LanguageRegistry();

	private static LanguageRegistry getInstance() {
		return instance;
	}

	@Nullable
	public static ILanguage getLanguage(String lang) {
		return getInstance().languages.get(lang);
	}


	@Nullable
	public static ILanguage getLanguageForFile(Path file) {
		String extension = PathUtil.getFileExtension(file);
		if(extension == null) {
			return null;
		}
		else {
			return getInstance().extensions.get(extension);
		}
	}


	@Nullable
	public static ILanguage getLanguageForFile(URI uri) {
		String extension = PathUtil.getFileExtension(uri);
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


	public Map<String, ILanguage> extensions = new HashMap<String, ILanguage>();


	public Map<String, ILanguage> languages = new HashMap<String, ILanguage>();


	private LanguageRegistry() {
	}

}
