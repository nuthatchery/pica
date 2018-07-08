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
package org.nuthatchery.pica.util.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.nuthatchery.pica.util.depgraph.IDepGraph;
import org.nuthatchery.pica.util.depgraph.IWritableDepGraph;

public class DepGraphAxioms {
	public static <T> void addDep1(IWritableDepGraph<T> graph, T from, T to) {
		graph = graph.copy();
		graph.add(from, to);
		assertTrue(graph.getDepends(from).contains(to));
		assertTrue(graph.getDependents(to).contains(from));
		assertTrue(graph.getTransitiveDepends(from).contains(to));
		assertTrue(graph.getTransitiveDependents(to).contains(from));
	}


	public static <T> void addDep2(IWritableDepGraph<T> graph, T elt) {
		graph = graph.copy();
		graph.add(elt);
		assertNotNull(graph.getDepends(elt));
		assertNotNull(graph.getDependents(elt));
		assertNotNull(graph.getTransitiveDepends(elt));
		assertNotNull(graph.getTransitiveDependents(elt));
	}


	public static <T> void dependsHasDependent(IDepGraph<T> graph, T node) {
		Set<T> depends = graph.getDepends(node);
		for(T d : depends) {
			assertTrue(graph.getDependents(d).contains(node));
		}
	}


	public static <T> void runTests(IDepGraph<T> graph) {
		for(T e : graph.getElements()) {
			dependsHasDependent(graph, e);
		}
		topologicalContainsAll(graph);
		topologicalIsTopological(graph);
	}


	public static <T> void topologicalContainsAll(IDepGraph<T> graph) {
		Set<T> elements = graph.getElements();
		int i = 0;
		for(T e : graph.topological()) {
			assertTrue(elements.contains(e));
			i++;
		}
		assertEquals(elements.size(), i);
	}


	public static <T> void topologicalIsTopological(IDepGraph<T> graph) {
		Set<T> seen = new HashSet<T>();
		for(T e : graph.topological()) {
			seen.add(e);
			for(T x : graph.getDepends(e)) {
				assertTrue(seen.contains(x));
			}
		}
	}
}
