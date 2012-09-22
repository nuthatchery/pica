package org.magnolialang.memo.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.magnolialang.memo.SoftHashTable;

public class HashTableAxioms {

	/** reflexivity: a = a */
	public static void equalsAxiom(Object o) {
		assertEquals(o, o);
	}


	/** symmetry: a = b <=> b = a */
	public static void equalsAxiom(Object o1, Object o2) {
		assertEquals(o1.equals(o2), o2.equals(o1));
	}


	/** transitivity: a = b && b = c => a = c */
	public static void equalsAxiom(Object o1, Object o2, Object o3) {
		if(o1.equals(o2) && o2.equals(o3))
			assertEquals(o1, o3);
	}


	public static void equalsHashAxiom(Object o1, Object o2) {
		if(o1.equals(o2))
			assertEquals(o1.hashCode(), o2.hashCode());
	}


	public static <K, V> void putGetContainsAxiom1(SoftHashTable<K, V> table, K key, V value) {
		table.put(key, value);
		assertTrue(table.containsKey(key));
		assertEquals(value, table.get(key));
	}


	public static <K, V> void putGetContainsAxiom2(SoftHashTable<K, V> table, K key1, K key2, V value) {
		if(!key1.equals(key2)) {
			V value2 = table.get(key2);
			boolean contains2 = table.containsKey(key2);
			table.put(key1, value);
			assertTrue(table.containsKey(key1));
			assertEquals(contains2, table.containsKey(key2));
			assertEquals(value2, table.get(key2));
		}
		else
			putGetContainsAxiom1(table, key1, value);
	}


	/**
	 * Not valid for weak tables unless references to all keys are held.
	 */
	public static <K, V> void putSizeAxiom(SoftHashTable<K, V> table, K key, V value) {
		int size = table.size();

		if(table.containsKey(key)) {
			table.put(key, value);
			assertEquals(size, table.size());
		}
		else {
			table.put(key, value);
			assertEquals(size + 1, table.size());
		}
	}


	/**
	 * Not valid for weak tables unless references to all keys are held.
	 */
	public static <K, V> void removeAxiom(SoftHashTable<K, V> table, K key) {
		int size = table.size();

		if(table.containsKey(key)) {
			table.remove(key);
			assertEquals(size - 1, table.size());
		}
		else {
			table.remove(key);
			assertEquals(size, table.size());
		}
		assertNull(table.get(key));
	}

}
