package org.magnolialang.util;

import java.net.URI;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IInteger;
import org.magnolialang.util.TreeAdapter.PositionAnnotator;
import org.rascalmpl.values.errors.SummaryAdapter;
import org.rascalmpl.values.uptr.Factory;

public final class ParsetreeAdapter {

	private ParsetreeAdapter() {
		super();
	}

	// DENNE HER!
	public static IConstructor addPositionInformation(
			final IConstructor parseTree, final URI location) {
		if(isParseTree(parseTree)) {
			IConstructor tree = (IConstructor) parseTree.get("top");
			tree = new PositionAnnotator(tree).addPositionInformation(location);
			return parseTree.set("top", tree);
		}

		return parseTree;
	}

	public static boolean isErrorSummary(final IConstructor parseTree) {
		return parseTree.getConstructorType() == Factory.ParseTree_Summary;
	}

	public static boolean isParseTree(final IConstructor parseTree) {
		return parseTree.getConstructorType() == Factory.ParseTree_Top;
	}

	public static IConstructor getTop(final IConstructor parseTree) {
		return (IConstructor) parseTree.get("top");
	}

	public static SummaryAdapter getSummary(final IConstructor parseTree) {
		return new SummaryAdapter(parseTree);
	}

	public static boolean hasAmbiguities(final IConstructor parseTree) {
		return ((IInteger) parseTree.get("amb_cnt")).intValue() != 0;
	}
}
