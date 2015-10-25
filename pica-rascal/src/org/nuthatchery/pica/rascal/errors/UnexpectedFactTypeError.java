/**************************************************************************
 * Copyright (c) 2012 Anya Helene Bagge
 * Copyright (c) 2012 University of Bergen
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
package org.nuthatchery.pica.rascal.errors;

import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.value.type.Type;
import org.nuthatchery.pica.errors.ImplementationError;
import org.nuthatchery.pica.rascal.RascalUtil;

public class UnexpectedFactTypeError extends ImplementationError {

	private static final long serialVersionUID = 4283365129892352057L;
	public final Type expected;
	public final Type got;


	public UnexpectedFactTypeError(String factName, Type expected, Type got) {
		super("Unexpected type for fact '" + factName + "': expected " + expected.toString() + ", got " + got.toString());
		this.expected = expected;
		this.got = got;
	}


	public UnexpectedFactTypeError(String factName, Type expected, Type got, ISourceLocation loc) {
		super("Unexpected type for fact '" + factName + "': expected " + expected.toString() + ", got " + got.toString(), RascalUtil.toCodeRegion(loc));
		this.expected = expected;
		this.got = got;
	}


	public UnexpectedFactTypeError(String factName, Type expected, Type got, Throwable cause) {
		super("Unexpected type for fact '" + factName + "': expected " + expected.toString() + ", got " + got.toString(), cause);
		this.expected = expected;
		this.got = got;
	}

}
