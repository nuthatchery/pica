package org.magnolialang.util.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.magnolialang.util.IDepGraph;
import org.magnolialang.util.IWritableDepGraph;

public class DepGraphAxioms {
	public static <T> void runTests(IDepGraph<T> graph) {
		for(T e : graph.getElements())
			dependsHasDependent(graph, e);
		topologicalContainsAll(graph);
		topologicalIsTopological(graph);
	}


	public static <T> void addDep1(IWritableDepGraph<T> graph, T from, T to) {
		graph = graph.clone();
		graph.add(from, to);
		assertTrue(graph.getDepends(from).contains(to));
		assertTrue(graph.getDependents(to).contains(from));
		assertTrue(graph.getTransitiveDepends(from).contains(to));
		assertTrue(graph.getTransitiveDependents(to).contains(from));
	}


	public static <T> void addDep2(IWritableDepGraph<T> graph, T elt) {
		graph = graph.clone();
		graph.add(elt);
		assertNotNull(graph.getDepends(elt));
		assertNotNull(graph.getDependents(elt));
		assertNotNull(graph.getTransitiveDepends(elt));
		assertNotNull(graph.getTransitiveDependents(elt));
	}


	public static <T> void dependsHasDependent(IDepGraph<T> graph, T node) {
		Set<T> depends = graph.getDepends(node);
		for(T d : depends)
			assertTrue(graph.getDependents(d).contains(node));
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
			for(T x : graph.getDepends(e))
				assertTrue(seen.contains(x));
		}
	}
}
