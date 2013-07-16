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
package org.magnolialang.util.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.magnolialang.util.depgraph.IWritableDepGraph;
import org.magnolialang.util.depgraph.UnsyncedDepGraph;

public class DepGraphGenerator {
	public static final Random random = new Random();


	public static <T> void doRandomAddOp(IWritableDepGraph<T> graph) {
		ArrayList<T> list = new ArrayList<T>(graph.getElements());
		T e = list.remove(random.nextInt(list.size()));
		graph.add(e, list.get(random.nextInt(list.size())));
	}


	public static <T> IWritableDepGraph<T> genDepGraph(Collection<T> elements, int numOps, boolean avoidCycles) {
		UnsyncedDepGraph<T> graph = new UnsyncedDepGraph<T>();

		for(T e : elements) {
			graph.add(e);
		}

		if(elements.isEmpty()) {
			return graph;
		}

		ArrayList<T> list = new ArrayList<T>(elements);
		for(int i = 0; i < numOps; i++) {
			int a = random.nextInt(list.size());
			int b = random.nextInt(list.size());
			if(a != b) {
				if(avoidCycles) {
					graph.add(list.get(Math.min(a, b)), list.get(Math.max(a, b)));
				}
				else {
					graph.add(list.get(a), list.get(b));
				}
			}
		}
		return graph;
	}
}
