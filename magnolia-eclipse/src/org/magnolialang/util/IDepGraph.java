package org.magnolialang.util;

import java.util.Set;

public interface IDepGraph<T> {
	/**
	 * Get all elements that 'element' depends on.
	 * 
	 * All elements in the result set will have 'element' in their
	 * getDependents() set.
	 * 
	 * @param element
	 * @return Dependencies
	 */
	Set<T> getDepends(T element);


	/**
	 * Get all elements that depend on 'element'.
	 * 
	 * All elements in the result set will have 'element' in their getDepends()
	 * set.
	 * 
	 * @param element
	 * @return Dependents
	 */
	Set<T> getDependents(T element);


	/**
	 * Get all elements that 'element' depends on, transitively.
	 * 
	 * I.e., all elements in the result set will either be in getDepends(), or
	 * in the getDepends() of some successor.
	 * 
	 * For any element in the result set, 'element' will be in the
	 * getTransitiveDependents() set.
	 * 
	 * @param element
	 * @return All elements that 'element' depends on, transitively
	 */
	Set<T> getTransitiveDepends(T element);


	/**
	 * Get all elements that depends on 'element', transitively.
	 * 
	 * I.e., all elements in the result set will either be in getDependents(),
	 * or in the getDependents() of some predecessor.
	 * 
	 * For any element in the result set, 'element' will be in the
	 * getTransitiveDepends() set.
	 * 
	 * @param element
	 * @return All elements that 'element' depends on, transitively
	 */
	Set<T> getTransitiveDependents(T element);


	/**
	 * @return All elements in the dependency graph
	 */
	Set<T> getElements();


	/**
	 * @return A topologically ordered iteration over the dependency graph
	 */
	Iterable<T> topological();


	/**
	 * @return True if the dependecy graph has cycles.
	 */
	boolean hasCycles();


	IDepGraph<T> clone();
}
