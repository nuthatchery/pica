/**************************************************************************
 * Copyright (c) 2012-2013 Anya Helene Bagge
 * Copyright (c) 2012-2013 University of Bergen
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

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.ISourceLocation;

public class TermLocator {
	public static final INodePredicate TRUE = new INodePredicate() {
		@Override
		public boolean match(IConstructor tree) {
			return true;
		}
	};
	public static final INodePredicate IS_NAME = new INodePredicate() {
		@Override
		public boolean match(IConstructor tree) {
			return tree.getName().endsWith("Name") || tree.getName().equals("Instance");
		}
	};


	public static boolean hasSameURI(ISourceLocation a, ISourceLocation b) {
		if(a.getURI() == null)
			return true;
		else if(b.getURI() == null)
			return true;
		else
			return a.getURI().equals(b.getURI());
	}


	/**
	 * @param a
	 * @param b
	 * @return True if the location a is inside the location b
	 */
	public static boolean isInside(ISourceLocation a, ISourceLocation b) {
		if(!hasSameURI(a, b))
			return false;
		if(a.getOffset() < b.getOffset())
			return false;

		if(a.getOffset() + a.getLength() > b.getOffset() + b.getLength())
			return false;

		return true;
	}


	public static IConstructor locate(IConstructor tree, ISourceLocation loc) {
		return locate(tree, loc, TRUE, false);
	}


	public static IConstructor locate(IConstructor tree, ISourceLocation loc, INodePredicate pred, boolean outermost) {
		ISourceLocation treeLoc = (ISourceLocation) tree.asAnnotatable().getAnnotation("loc");

		if(treeLoc != null) {
			if(isInside(loc, treeLoc)) {
				if(outermost && pred.match(tree))
					return tree;
				else {
					if(TermAdapter.isLeaf(tree))
						return null;
					for(IConstructor c : TermAdapter.getChildren(tree)) {
						IConstructor locate = locate(c, loc, pred, outermost);
						if(locate != null)
							return locate;
					}
					if(pred.match(tree))
						return tree;
					else
						return null;
				}
			}
			else
				return null;
		}
		else {
			if(TermAdapter.isLeaf(tree))
				return null;
			int start = Integer.MAX_VALUE;
			int end = Integer.MIN_VALUE;

			for(IConstructor c : TermAdapter.getChildren(tree)) {
				IConstructor locate = locate(c, loc, pred, outermost);
				if(locate != null) {
					if(outermost && pred.match(tree))
						return tree;
					else
						return locate;
				}

				if(c.asAnnotatable().hasAnnotation("loc")) {
					ISourceLocation cLoc = (ISourceLocation) c.asAnnotatable().getAnnotation("loc");
					if(hasSameURI(cLoc, loc)) {
						start = Math.min(start, cLoc.getOffset());
						end = Math.max(end, cLoc.getOffset() + cLoc.getLength());
					}
				}
			}

			if(loc.getOffset() >= start && loc.getOffset() + loc.getLength() <= end && pred.match(tree))
				return tree;
			else
				return null;
		}
	}


	public static IConstructor locateName(IConstructor tree, ISourceLocation loc) {
		return locate(tree, loc, IS_NAME, true);
	}
}
