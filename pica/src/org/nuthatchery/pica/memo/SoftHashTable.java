/**************************************************************************
 * Copyright (c) 2009 Centrum Wiskunde en Informatica (CWI)
 * Copyright (c) 2012 Anya Helene Bagge
 * Copyright (c) 2012 University of Bergen
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * 
 * See the file COPYRIGHT for more information.
 * 
 * Contributors:
 * * Anya Helene Bagge
 * + Arnold Lankamp
 * 
 *************************************************************************/
package org.nuthatchery.pica.memo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.imp.pdb.facts.IValue;

/**
 * A specialized version of the ShareableHashMap, specifically meant for storing
 * values.
 * 
 * @author Arnold Lankamp
 */
public final class SoftHashTable<K, V> {
	private final static int INITIAL_LOG_SIZE = 4;

	private int modSize;
	private int hashMask;

	private Entry<K, V>[] data;

	private int threshold;

	private int load;

	private int currentHashCode;


	@SuppressWarnings("unchecked")
	public SoftHashTable() {
		super();

		modSize = INITIAL_LOG_SIZE;
		int tableSize = 1 << modSize;
		hashMask = tableSize - 1;
		data = new Entry[tableSize];

		threshold = tableSize;

		load = 0;

		currentHashCode = 0;
	}


	public SoftHashTable(SoftHashTable<K, V> table) {
		super();

		modSize = table.modSize;
		int tableSize = 1 << modSize;
		hashMask = tableSize - 1;
		data = table.data.clone();

		threshold = tableSize;

		load = table.load;

		currentHashCode = table.currentHashCode;
	}


	@SuppressWarnings("unchecked")
	public void clear() {
		modSize = INITIAL_LOG_SIZE;
		int tableSize = 1 << modSize;
		hashMask = tableSize - 1;
		data = new Entry[tableSize];

		threshold = tableSize;

		load = 0;

		currentHashCode = 0;
	}


	public boolean contains(Object key) {
		return get(key) != null;
	}


	public boolean containsKey(K key) {
		int hash = key.hashCode();
		int position = hash & hashMask;

		Entry<K, V> entry = data[position];
		while(entry != null) {
			if(hash == entry.hash) {
				if(key instanceof IValue && entry.key instanceof IValue && ((IValue) key).isEqual((IValue) entry.key))
					return true;
				else if(key.equals(entry.key))
					return true;
			}

			entry = entry.next;
		}

		return false;
	}


	public Iterator<Map.Entry<K, V>> entryIterator() {
		return new EntryIterator<K, V>(this);
	}


	public Set<Map.Entry<K, V>> entrySet() {
		HashSet<Map.Entry<K, V>> entrySet = new HashSet<Map.Entry<K, V>>();

		Iterator<Map.Entry<K, V>> entriesIterator = entryIterator();
		while(entriesIterator.hasNext()) {
			entrySet.add(entriesIterator.next());
		}

		return entrySet;
	}


	@Override
	@SuppressWarnings("rawtypes")
	public boolean equals(Object o) {
		if(o == null)
			return false;

		if(o.getClass() == getClass()) {
			SoftHashTable other = (SoftHashTable) o;

			if(other.currentHashCode != currentHashCode)
				return false;
			if(other.size() != size())
				return false;

			if(isEmpty())
				return true; // No need to check if the maps are empty.

			@SuppressWarnings("unchecked")
			Iterator<Map.Entry> otherIterator = other.entryIterator();
			while(otherIterator.hasNext()) {
				Map.Entry entry = otherIterator.next();
				Object otherValue = entry.getValue();
				V thisValue = get(entry.getKey());

				if(otherValue != thisValue && (thisValue == null || !thisValue.equals(otherValue)))
					return false;
			}
			return true;
		}

		return false;
	}


	public V get(Object key) {
		int hash = key.hashCode();
		int position = hash & hashMask;

		Entry<K, V> entry = data[position];
		while(entry != null) {
			if(hash == entry.hash) {
				if(key instanceof IValue && entry.key instanceof IValue && ((IValue) key).isEqual((IValue) entry.key))
					return entry.value;
				else if(key.equals(entry.key))
					return entry.value;
			}

			entry = entry.next;
		}

		return null;
	}


	@Override
	public int hashCode() {
		return currentHashCode;
	}


	public boolean isEmpty() {
		return load == 0;
	}


	public Set<K> keySet() {
		HashSet<K> keysSet = new HashSet<K>();

		Iterator<K> keysIterator = keysIterator();
		while(keysIterator.hasNext()) {
			keysSet.add(keysIterator.next());
		}

		return keysSet;
	}


	public Iterator<K> keysIterator() {
		return new KeysIterator<K, V>(this);
	}


	/**
	 * Insert a new element into the table.
	 * 
	 * Note that while put() is guaranteed to change any of the other elements,
	 * it
	 * may remove elements that have been garbage collected.
	 * 
	 * So, for any k2 != k1, if v2 = tbl.get(k2), then after tbl.put(k1, v1),
	 * tbl.get(k2) is either v2 or null.
	 * 
	 * 
	 * @param key
	 * @param value
	 * @return The old element at K, or null
	 */
	public V put(K key, V value) {
		ensureCapacity();

		int hash = key.hashCode();
		int position = hash & hashMask;

		Entry<K, V> currentStartEntry = data[position];
		// Check if the key is already in here.
		if(currentStartEntry != null) {
			Entry<K, V> entry = currentStartEntry;
			do {
				if(hash == entry.hash) {
					if(key instanceof IValue && entry.key instanceof IValue && ((IValue) key).isEqual((IValue) entry.key) || key.equals(entry.key)) {
						replaceValue(position, entry, value);

						return entry.value; // Return the old value.
					}
				}

				entry = entry.next;
			}
			while(entry != null);
		}

		data[position] = new Entry<K, V>(hash, key, value, currentStartEntry); // Insert the new entry.

		load++;

		currentHashCode ^= hash; // Update the current hashcode of this map.

		return null;
	}


	@SuppressWarnings("unchecked")
	public void putAll(Map<? extends K, ? extends V> otherMap) {
		Set<Map.Entry<K, V>> entrySet = (Set<Map.Entry<K, V>>) (Set<?>) otherMap.entrySet(); // Generics stink.
		Iterator<Map.Entry<K, V>> entrySetIterator = entrySet.iterator();
		while(entrySetIterator.hasNext()) {
			Map.Entry<K, V> next = entrySetIterator.next();
			put(next.getKey(), next.getValue());
		}
	}


	public V remove(Object key) {
		int hash = key.hashCode();
		int position = hash & hashMask;

		Entry<K, V> currentStartEntry = data[position];
		if(currentStartEntry != null) {
			Entry<K, V> entry = currentStartEntry;
			do {
				if(hash == entry.hash) {
					if(key instanceof IValue && entry.key instanceof IValue && ((IValue) key).isEqual((IValue) entry.key) || key.equals(entry.key)) {
						Entry<K, V> e = data[position];

						data[position] = entry.next;
						// Reconstruct the other entries (if necessary).
						while(e != entry) {
							data[position] = new Entry<K, V>(e.hash, e.key, e.value, data[position]);

							e = e.next;
						}

						load--;

						currentHashCode ^= hash; // Update the current hashcode of this map.

						return entry.value; // Return the value.
					}
				}
				entry = entry.next;
			}
			while(entry != null);
		}

		return null; // Not found.
	}


	/**
	 * Note that the size is somewhat arbitrary, since unreferenced elements can
	 * be pruned at any time.
	 * 
	 * In particular, the size may not grow when an element is added – even in a
	 * single-threaded
	 * environment – since put() might prune some of the elements.
	 * 
	 * @return An indication of the number of elements in the table
	 */
	public int size() {
		return load;
	}


	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append('{');
		for(Entry<K, V> element : data) {
			buffer.append('[');
			Entry<K, V> e = element;
			if(e != null) {
				buffer.append(e);

				e = e.next;

				while(e != null) {
					buffer.append(',');
					buffer.append(e);

					e = e.next;
				}
			}
			buffer.append(']');
		}
		buffer.append('}');

		return buffer.toString();
	}


	public Collection<V> values() {
		HashSet<V> valuesSet = new HashSet<V>();

		Iterator<V> valuesIterator = valuesIterator();
		while(valuesIterator.hasNext()) {
			valuesSet.add(valuesIterator.next());
		}

		return valuesSet;
	}


	public Iterator<V> valuesIterator() {
		return new ValuesIterator<K, V>(this);
	}


	private void ensureCapacity() {
		if(load > threshold) {
			rehash();
		}
	}


	@SuppressWarnings("unused")
	private V getTruelyEqual(K key) {
		int hash = key.hashCode();
		int position = hash & hashMask;

		Entry<K, V> entry = data[position];
		while(entry != null) {
			if(hash == entry.hash && key.equals(entry.key))
				return entry.value;

			entry = entry.next;
		}

		return null;
	}


	@SuppressWarnings("unchecked")
	private void rehash() {
		modSize++;
		int tableSize = 1 << modSize;
		hashMask = tableSize - 1;
		Entry<K, V>[] newData = new Entry[tableSize];

		threshold = tableSize;

		Entry<K, V>[] oldData = data;
		for(int i = oldData.length - 1; i >= 0; i--) {
			Entry<K, V> entry = oldData[i];

			if(entry != null) {
				// Determine the last unchanged entry chain.
				Entry<K, V> lastUnchangedEntryChain = entry;
				int newLastUnchangedEntryChainIndex = entry.hash & hashMask;

				Entry<K, V> e = entry.next;
				while(e != null) {
					int newIndex = e.hash & hashMask;
					if(newIndex != newLastUnchangedEntryChainIndex) {
						lastUnchangedEntryChain = e;
						newLastUnchangedEntryChainIndex = newIndex;
					}

					e = e.next;
				}

				newData[newLastUnchangedEntryChainIndex] = lastUnchangedEntryChain;

				// Reconstruct the other entries (if necessary).
				while(entry != lastUnchangedEntryChain) {
					int hash = entry.hash;
					int position = hash & hashMask;
					newData[position] = new Entry<K, V>(hash, entry.key, entry.value, newData[position]);

					entry = entry.next;
				}
			}
		}

		data = newData;
	}


	private void replaceValue(int position, Entry<K, V> entry, V newValue) {
		Entry<K, V> e = data[position];

		// Reconstruct the updated entry.
		data[position] = new Entry<K, V>(entry.hash, entry.key, newValue, entry.next);

		// Reconstruct the other entries (if necessary).
		while(e != entry) {
			data[position] = new Entry<K, V>(e.hash, e.key, e.value, data[position]);

			e = e.next;
		}
	}


	private static class Entry<K, V> implements Map.Entry<K, V> {
		public final int hash;
		public final K key;
		public final V value;

		public final Entry<K, V> next;


		public Entry(int hash, K key, V value, Entry<K, V> next) {
			super();

			this.hash = hash;
			this.key = key;
			this.value = value;

			this.next = next;
		}


		@Override
		public K getKey() {
			return key;
		}


		@Override
		public V getValue() {
			return value;
		}


		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException("The setting of values is not supported by this map implementation.");
		}


		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();

			buffer.append('<');
			buffer.append(key);
			buffer.append(':');
			buffer.append(value);
			buffer.append('>');

			return buffer.toString();
		}
	}


	private static class EntryIterator<K, V> implements Iterator<Map.Entry<K, V>> {
		private final Entry<K, V>[] data;

		private Entry<K, V> current;
		private int index;


		public EntryIterator(SoftHashTable<K, V> softHashMap) {
			super();

			data = softHashMap.data;

			index = data.length - 1;
			current = new Entry<K, V>(0, null, null, data[index]);
			locateNext();
		}


		@Override
		public boolean hasNext() {
			return current != null;
		}


		@Override
		public Entry<K, V> next() {
			if(!hasNext())
				throw new UnsupportedOperationException("There are no more elements in this iterator.");

			Entry<K, V> entry = current;
			locateNext();

			return entry;
		}


		@Override
		public void remove() {
			throw new UnsupportedOperationException("This iterator doesn't support removal.");
		}


		private void locateNext() {
			Entry<K, V> next = current.next;
			if(next != null) {
				current = next;
				return;
			}

			for(int i = index - 1; i >= 0; i--) {
				Entry<K, V> entry = data[i];
				if(entry != null) {
					current = entry;
					index = i;
					return;
				}
			}

			current = null;
			index = 0;
		}
	}


	private static class KeysIterator<K, V> implements Iterator<K> {
		private final EntryIterator<K, V> entryIterator;


		public KeysIterator(SoftHashTable<K, V> softHashMap) {
			super();

			entryIterator = new EntryIterator<K, V>(softHashMap);
		}


		@Override
		public boolean hasNext() {
			return entryIterator.hasNext();
		}


		@Override
		public K next() {
			return entryIterator.next().key;
		}


		@Override
		public void remove() {
			throw new UnsupportedOperationException("This iterator doesn't support removal.");
		}
	}


	private static class ValuesIterator<K, V> implements Iterator<V> {
		private final EntryIterator<K, V> entryIterator;


		public ValuesIterator(SoftHashTable<K, V> softHashMap) {
			super();

			entryIterator = new EntryIterator<K, V>(softHashMap);
		}


		@Override
		public boolean hasNext() {
			return entryIterator.hasNext();
		}


		@Override
		public V next() {
			return entryIterator.next().value;
		}


		@Override
		public void remove() {
			throw new UnsupportedOperationException("This iterator doesn't support removal.");
		}
	}
}
