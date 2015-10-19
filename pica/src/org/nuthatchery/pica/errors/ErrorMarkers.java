/**************************************************************************
 * Copyright (c) 2010-2012 Anya Helene Bagge
 * Copyright (c) 2010-2012 University of Bergen
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
package org.nuthatchery.pica.errors;

import java.util.HashMap;
import java.util.Map;


public final class ErrorMarkers {
	public static final String PREFIX = "magnolia_eclipse.";
	public static final String TYPE_SYNTAX = PREFIX + "syntaxError";
	public static final String TYPE_COMPILATION_ERROR = PREFIX + "compilationError";
	public static final String TYPE_LOADER = PREFIX + "loadError";
	public static final String TYPE_IMPORT = PREFIX + "importError";
	public static final String TYPE_NAMERES = PREFIX + "nameresError";
	public static final String TYPE_TYPECHECK = PREFIX + "typecheckError";
	public static final String TYPE_AMBIGUITY = PREFIX + "ambiguityMarker";
	public static final String TYPE_BACKEND = PREFIX + "backendError";
	public static final String TYPE_DEFAULT = TYPE_COMPILATION_ERROR;
	public static final Map<String, Integer> SEVERITY_MAP;
	public static final String SEVERITY_WARNING = "warning";
	public static final String SEVERITY_ERROR = "error";
	public static final String SEVERITY_INFO = "info";
	public static final Map<Integer, Integer> SEVERITY_INT_MAP;;


	static {
		SEVERITY_MAP = new HashMap<String, Integer>();
		SEVERITY_MAP.put("warning", Severity.WARNING.getValue());// WARNING
		SEVERITY_MAP.put("error", Severity.ERROR.getValue());// ERROR
		SEVERITY_MAP.put("info", Severity.INFO.getValue());// INFO
	}


	// map to the numbers that eclipse is using
	static {
		SEVERITY_INT_MAP = new HashMap<Integer, Integer>();
		SEVERITY_INT_MAP.put(2, Severity.WARNING.getValue());// WARNING
		SEVERITY_INT_MAP.put(3, Severity.ERROR.getValue());// ERROR
		SEVERITY_INT_MAP.put(4, Severity.ERROR.getValue());// ERROR
		SEVERITY_INT_MAP.put(1, Severity.INFO.getValue());// INFO
		SEVERITY_INT_MAP.put(0, Severity.INFO.getValue());// INFO
	}


	public static int getSeverity(final String severity) {
		return getSeverity(severity, Severity.ERROR.getValue());
	}


	public static int getSeverity(final String severity, final int dflt) {
		if(SEVERITY_MAP.containsKey(severity)) {
			return SEVERITY_MAP.get(severity);
		}
		else {
			return dflt;
		}
	}


	private ErrorMarkers() {

	}

}
