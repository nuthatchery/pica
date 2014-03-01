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
package org.nuthatchery.pica.resources.storage;

import java.io.IOException;

import org.eclipse.jdt.annotation.Nullable;

public interface IStorage {

	void declare(String key);


	/**
	 * Get an entry.
	 * 
	 * This may access the disk, and possibly throw an IOException.
	 * 
	 * @param key
	 * @param storable
	 *            A storable value into which the result is written, if any.
	 *            Untouched if the return value is null.
	 * 
	 * @return the stored value (same as 'storable'), or null if the file or
	 *         entry is not found
	 * @throws IOException
	 *             if a read error occurs, but not if the file/entry is not
	 *             found
	 */
	@Nullable
	<T extends IStorableValue> T get(String key, T storable) throws IOException;


	/**
	 * Put an entry.
	 * 
	 * Will store the value and return immediately. The data will only be stored
	 * on a call to save() or sync().
	 * 
	 * @param key
	 * @param value
	 */
	void put(String key, IStorableValue value);


	void save() throws IOException;


	/** TODO: WTF is this supposed to do? */
	@Deprecated
	@Nullable
	IStorage subStorage(String name);
}
