package org.magnolialang.util;

import java.util.Set;

public interface IDepGraph<T> {
	Set<T> getDepends(T element);


	Set<T> getDependents(T element);


	Set<T> getTransitiveDepends(T element);


	Set<T> getTransitiveDependents(T element);


	Set<T> getElements();


	void add(T from, T to);


	Iterable<T> topological();


	boolean hasCycles();


	void add(T node);
}
