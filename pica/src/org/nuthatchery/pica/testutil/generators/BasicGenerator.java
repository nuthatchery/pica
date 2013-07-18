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
 * it under the terms of the GNU General Public License as published by
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
package org.nuthatchery.pica.testutil.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BasicGenerator {
	public static final Random random = new Random();
	public static final List<String> strings = Collections.unmodifiableList(Arrays.asList("", "eple", "banan", "pære", "ananas", "katt", "hund", "pipp", "kanin", "a+b", "e=mc²", "the quick brown fox jumped over the lazy dog", "#¤%&/()=", "mjau", "voff", "fhdjsk", "HJ1NJK", "\"\"\\\"\\\""));

	public static final List<Integer> ints = Collections.unmodifiableList(Arrays.asList(0, -1, 1, Integer.MAX_VALUE, Integer.MIN_VALUE, -42, 69, 12143, 32, -43));


	public static int genInt() {
		return random.nextInt();
	}


	public static List<Integer> genIntList(int length) {
		List<Integer> list = new ArrayList<Integer>(length);
		for(int i = 0; i < length; i++) {
			list.add(random.nextInt());
		}
		return list;
	}


	public static String genString() {
		int len = random.nextInt(100);
		char[] chars = new char[len];
		for(int i = 0; i < len; i++) {
			chars[i] = (char) (random.nextInt(256) - 128);
		}
		return String.valueOf(chars);
	}


	/**
	 * Regn ut a modulo b hvor resultatet skal ha samme fortegn som b.
	 * 
	 * (Tilsvarer a % b, bortsett fra fortegnet)
	 * 
	 * @param a
	 * @param b
	 * @return et tall mellom 0 (inklusiv) og b (eksklusiv)
	 */
	public static int modulo(int a, int b) {
		assert b != 0;

		if(a < 0) {
			return -(a % b);
		}
		else {
			return a % b;
		}
	}


	public static <T> T randomElement(List<T> list) {
		if(!list.isEmpty()) {
			return list.get(random.nextInt(list.size()));
		}
		else {
			return null;
		}
	}
}
