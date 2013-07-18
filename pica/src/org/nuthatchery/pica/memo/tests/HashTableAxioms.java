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
package org.nuthatchery.pica.memo.tests;

import static org.junit.Assert.*;

import org.nuthatchery.pica.memo.SoftHashTable;

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
		if(o1.equals(o2) && o2.equals(o3)) {
			assertEquals(o1, o3);
		}
	}


	public static void equalsHashAxiom(Object o1, Object o2) {
		if(o1.equals(o2)) {
			assertEquals(o1.hashCode(), o2.hashCode());
		}
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
		else {
			putGetContainsAxiom1(table, key1, value);
		}
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
