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
package org.nuthatchery.pica.resources;

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.util.ISignature;
import org.nuthatchery.pica.util.Pair;

public interface IFact<T> {

	/**
	 * Clear the fact's value from memory.
	 * 
	 * @return The old value, or null if it has expired or not been set.
	 */
	@Nullable
	T dispose();


	/**
	 * Get the value of this fact, if available.
	 * 
	 * This may cause the fact to be loaded from disk.
	 * 
	 * @return A pair of the value and the signature of the fact's dependencies
	 */
	Pair<T, ISignature> getValue();


	/**
	 * Get the value of this fact, if the signature matches.
	 * 
	 * This may cause the fact to be loaded from disk.
	 * 
	 * @param sourceSignature
	 *            A signature of the fact's dependencies
	 * 
	 * @return The value, if any.
	 */
	@Nullable
	T getValue(ISignature sourceSignature);


	/**
	 * Set the value of this fact.
	 * 
	 * If the fact is not connected to a store and there are no other references
	 * to the value, a subsequent getValue may return null (even immediately
	 * after setValue returns)
	 * 
	 * @param newValue
	 *            The new value
	 * @param newSignature
	 *            A signature of the fact's dependencies used to compute the
	 *            value
	 * @return The old value, if any
	 */
	@Nullable
	T setValue(@Nullable T newValue, @Nullable ISignature newSignature);

}
