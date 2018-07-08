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

/**
 * Exception used for when the implementation detects that it has a bug. This is
 * a separate class of exceptions from static errors or run-time exceptions.
 */
public class ParserLoadError extends RuntimeException {
	private static final long serialVersionUID = -8394680881089520976L;


	public ParserLoadError(final String message) {
		super(message);
	}


	public ParserLoadError(final String message, final Throwable cause) {
		super(message, cause);
	}

}
