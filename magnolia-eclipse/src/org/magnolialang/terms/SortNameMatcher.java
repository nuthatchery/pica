package org.magnolialang.terms;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.imp.pdb.facts.IConstructor;
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
		String sortName = TreeAdapter.getSortName(tree);
		return sort.contains(sortName);
	}

}
