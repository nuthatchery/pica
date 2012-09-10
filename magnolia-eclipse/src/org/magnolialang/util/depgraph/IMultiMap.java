package org.magnolialang.util.depgraph;

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


	public IMultiMap<K, V> clone();
}
