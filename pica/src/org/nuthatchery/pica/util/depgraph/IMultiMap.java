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
package org.nuthatchery.pica.util.depgraph;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author anya
 *
 * @param <K>
 * @param <V>
 */
public interface IMultiMap<K, V> {

	/**
	 * Remove all entires
	 */
	void clear();


	/**
	 * @param key
	 * @return true if key exists
	 * @see #isEmpty(Object)
	 */
	boolean containsKey(Object key);


	/**
	 * Only the map itself is copied, key/value references remain the same.
	 *
	 * @return A copy of this multimap.
	 */
	IMultiMap<K, V> copy();


	/**
	 * @return A set of all the entries in the map
	 */
	Set<Entry<K, Set<? extends V>>> entrySet();


	/**
	 * Get all entries for a given key.
	 *
	 * @param key
	 * @return Entries for 'key'
	 */
	Set<V> get(K key);


	/**
	 * @return true if the multimap has no entries (no keys)
	 */
	boolean isEmpty();


	/**
	 * @param key
	 * @return true if the entry for key is empty
	 */
	boolean isEmpty(K key);


	/**
	 * @return Set of keys (backed by map)
	 */
	Set<K> keySet();


	int numKeys();


	/**
	 * Add a key with no mapping.
	 *
	 * @param key
	 */
	void put(K key);


	void put(K arg0, Set<? extends V> arg1);


	/**
	 * Add a new entry.
	 *
	 * @param arg0
	 * @param arg1
	 *
	 */
	void put(K arg0, V arg1);


	/**
	 * Add all entries from the map.
	 *
	 * @param arg0
	 */
	void putAll(IMultiMap<? extends K, ? extends V> arg0);


	/**
	 * Add all entries from the map.
	 *
	 * @param arg0
	 */
	void putAll(Map<? extends K, ? extends V> arg0);


	/**
	 * Remove the key from the map.
	 *
	 * @param key
	 * @return The entries for the key
	 */
	Set<V> remove(K key);


	/**
	 * Remove a key/value mapping.
	 *
	 * The key will remain in the map, even if there are no more values
	 * associated with it.
	 *
	 * @param key
	 * @param value
	 * @return true if the mapping was in the multimap
	 */
	boolean remove(K key, V value);
}
