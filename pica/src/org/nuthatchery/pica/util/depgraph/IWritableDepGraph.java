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

public interface IWritableDepGraph<T> extends IDepGraph<T> {

	/**
	 * Add a new element to the graph.
	 * 
	 * @param element
	 */
	void add(T element);


	/**
	 * Add a new dependency to the graph.
	 * 
	 * @param dependent
	 * @param dependency
	 */
	void add(T dependent, T dependency);


	void clear();


	@Override
	IWritableDepGraph<T> copy();


	/**
	 * Remove an element from the graph.
	 * 
	 * The element will also be removed from the dependency/dependents lists of
	 * other elements.
	 * 
	 * @param element
	 */
	void remove(T element);

}
