package org.magnolialang.resources.storage;

import java.io.IOException;

public interface IStorage {

	void save() throws IOException;


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


	/**
	 * Get an entry.
	 * 
	 * This may access the disk, and possibly throw an IOException.
	 * 
	 * @param key
	 * @param storable
	 *            The class of the expected return value; should provide a
	 *            nullary constructor
	 * @return the stored value, or null if the file or entry is not found
	 * @throws IOException
	 *             if a read error occurs, but not if the file/entry is not
	 *             found
	 */
	<T extends IStorableValue> T get(String key, T storable) throws IOException;


	IStorage subStorage(String name);
}
