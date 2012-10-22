package org.magnolialang.terms;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;

public class TermLocator {
	public static final INodePredicate	TRUE	= new INodePredicate() {
													@Override
													public boolean match(IConstructor tree) {
														return true;
													}
												};
	public static final INodePredicate	IS_NAME	= new INodePredicate() {
													@Override
													public boolean match(IConstructor tree) {
														return tree.getName().endsWith("Name");
													}
												};


	public static IConstructor locate(IConstructor tree, ISourceLocation loc) {
		return locate(tree, loc, TRUE, false);
	}


	public static IConstructor locateName(IConstructor tree, ISourceLocation loc) {
		return locate(tree, loc, IS_NAME, true);
	}


	public static IConstructor locate(IConstructor tree, ISourceLocation loc, INodePredicate pred, boolean outermost) {
		ISourceLocation treeLoc = (ISourceLocation) tree.getAnnotation("loc");

		if(treeLoc != null) {
			if(isInside(loc, treeLoc)) {
				if(outermost && pred.match(tree)) {
					return tree;
				}
				else {
					if(TermAdapter.isLeaf(tree)) {
						return null;
					}
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
			else {
				return null;
			}
		}
		else {
			if(TermAdapter.isLeaf(tree)) {
				return null;
			}
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

				if(c.hasAnnotation("loc")) {
					ISourceLocation cLoc = (ISourceLocation) c.getAnnotation("loc");
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


	public static boolean hasSameURI(ISourceLocation a, ISourceLocation b) {
		if(a.getURI() == null)
			return true;
		else if(b.getURI() == null)
			return true;
		else
			return a.getURI().equals(b.getURI());
	}
}
