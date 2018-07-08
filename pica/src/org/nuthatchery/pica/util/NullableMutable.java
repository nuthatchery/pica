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

import org.eclipse.jdt.annotation.Nullable;

/**
 * A simple mutable singleton class
 *
 * @param <T>
 *            Type of element
 */

public class NullableMutable<T> {


	@Nullable
	protected T element;


	public NullableMutable(@Nullable T element) {
		this.element = element;
	}


	@Override
	public boolean equals(@Nullable Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		NullableMutable<?> other = (NullableMutable<?>) obj;
		T e = element;
		if(e == null) {
			if(other.element != null) {
				return false;
			}
		}
		else if(!e.equals(other.element)) {
			return false;
		}
		return true;
	}


	/**
	 * @return The element
	 */
	@Nullable
	public T get() {
		return element;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		final Object e = element;
		if(e != null) {
			result = prime * result + (element == null ? 0 : e.hashCode());
		}
		else {
			// TODO handle null value
		}
		return result;
	}


	public void set(@Nullable T element) {
		this.element = element;
	}

}
