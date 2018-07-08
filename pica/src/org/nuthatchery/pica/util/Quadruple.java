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
public class Quadruple<T1, T2, T3, T4> extends NullableQuadruple<T1, T2, T3, T4> {

	public Quadruple(T1 first, T2 second, T3 third, T4 fourth) {
		super(first, second, third, fourth);
	}


	/**
	 * @return First element of tuple
	 */
	@Override
	public T1 getFirst() {
		return NullnessHelper.assertNonNull(super.getFirst());
	}


	/**
	 * @return Fourth element of tuple
	 */
	@Override
	public T4 getFourth() {
		return NullnessHelper.assertNonNull(super.getFourth());
	}


	/**
	 * @return Second element of tuple
	 */
	@Override
	public T2 getSecond() {
		return NullnessHelper.assertNonNull(super.getSecond());
	}


	/**
	 * @return Third element of tuple
	 */
	@Override
	public T3 getThird() {
		return NullnessHelper.assertNonNull(super.getThird());
	}

}
