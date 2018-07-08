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
package org.nuthatchery.pica.util.depgraph;

import java.util.Collection;
import java.util.Set;

public interface IDepGraph<T> {
	void add(T element, Collection<? extends T> depends);


	/**
	 * Only the graph itself is copied, the element references remain the same.
	 *
	 * @return A copy of the dependency graph.
	 */
	IDepGraph<T> copy();


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
	 * @return All elements in the dependency graph
	 */
	Set<T> getElements();


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
	 * @return True if the dependecy graph has cycles.
	 */
	boolean hasCycles();


	/**
	 * @return A topologically ordered iteration over the dependency graph
	 */
	Iterable<T> topological();


	/**
	 * @return A work queue suitable for concurrent iteration over the
	 *         dependency graph
	 */
	ITopologicalWorkQueue<T> topologicalWorkQueue();
}
