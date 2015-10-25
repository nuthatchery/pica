/**************************************************************************
 * Copyright (c) 2010-2012 Anya Helene Bagge
 * Copyright (c) 2010-2012 University of Bergen
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
package org.nuthatchery.pica.terms;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.rascalmpl.value.IConstructor;
import org.rascalmpl.values.uptr.ITree;
import org.rascalmpl.values.uptr.TreeAdapter;

public class SortNameMatcher implements INodePredicate {
	private final Set<String> sort;


	public SortNameMatcher(final String... sort) {
		this.sort = new HashSet<String>();
		Collections.addAll(this.sort, sort);
	}


	@Override
	public boolean match(final IConstructor tree) {
		// TODO: or do we need to check that tree has a sortname?
		if(tree instanceof ITree) {
			String sortName = TreeAdapter.getSortName((ITree) tree);
			return sort.contains(sortName);
		}
		else
			return false;
	}

}
