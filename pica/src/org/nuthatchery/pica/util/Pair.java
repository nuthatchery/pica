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
 * A simple Pair class
 * 
 * @param <T1>
 *            Type of first element
 * @param <T2>
 *            Type of second element
 */
@NonNullByDefault
public class Pair<T1, T2> extends NullablePair<T1, T2> {

	public Pair(T1 first, T2 second) {
		super(first, second);
	}


	/**
	 * @return First element of pair
	 */
	@Override
	public T1 getFirst() {
		return NullnessHelper.assertNonNull(first);
	}


	/**
	 * @return Second element of pair
	 */
	@Override
	public T2 getSecond() {
		return NullnessHelper.assertNonNull(second);
	}
}
