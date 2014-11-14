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
package org.nuthatchery.pica.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A simple mutable singleton class
 * 
 * @param <T>
 *            Type of element
 */
@NonNullByDefault
public class Mutable<T> extends NullableMutable<T> {

	public Mutable(T element) {
		super(element);
	}


	/**
	 * @return Get the element
	 */
	@Override
	public T get() {
		return NullnessHelper.assertNonNull(element);
	}


	/**
	 * Set the element
	 */
	@Override
	public void set(T element) {
		this.element = element;
	}
}
