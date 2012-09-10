package org.magnolialang.util;

public interface IWritableDepGraph<T> extends IDepGraph<T> {

	/**
	 * Add a new dependency to the graph.
	 * 
	 * @param dependent
	 * @param dependency
	 */
	void add(T dependent, T dependency);


	/**
	 * Add a new element to the graph.
	 * 
	 * @param element
	 */
	void add(T element);


	@Override
	IWritableDepGraph<T> clone();

}
