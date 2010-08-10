package org.magnolialang.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;

import org.eclipse.imp.pdb.facts.*;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.magnolialang.errors.ImplementationError;
import org.rascalmpl.parser.MappingsCache;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.uptr.Factory;
import org.rascalmpl.values.uptr.ProductionAdapter;
import org.rascalmpl.values.uptr.SymbolAdapter;
import org.rascalmpl.values.uptr.visitors.IdentityTreeVisitor;

public final class TreeAdapter {

	private TreeAdapter() {
		super();
	}

	public static boolean isAppl(final IConstructor tree) {
		return tree.getConstructorType() == Factory.Tree_Appl;
	}

	public static boolean isAmb(final IConstructor tree) {
		return tree.getConstructorType() == Factory.Tree_Amb;
	}

	public static boolean isChar(final IConstructor tree) {
		return tree.getConstructorType() == Factory.Tree_Char;
	}

	public static boolean isCycle(final IConstructor tree) {
		return tree.getConstructorType() == Factory.Tree_Cycle;
	}

	public static boolean isComment(final IConstructor tree) {
		final IConstructor treeProd = getProduction(tree);
		if(treeProd != null) {
			final String treeProdCategory = ProductionAdapter
					.getCategory(treeProd);
			if(treeProdCategory != null && treeProdCategory.equals("Comment"))
				return true;
		}
		return false;
	}

	public static IConstructor getProduction(final IConstructor tree) {
		return (IConstructor) tree.get("prod");
	}

	public static boolean hasSortName(final IConstructor tree) {
		return ProductionAdapter.hasSortName(getProduction(tree));
	}

	public static String getSortName(final IConstructor tree)
			throws FactTypeUseException {
		return ProductionAdapter.getSortName(getProduction(tree));
	}

	public static String getConstructorName(final IConstructor tree) {
		return ProductionAdapter.getConstructorName(getProduction(tree));
	}

	public static boolean isProduction(final IConstructor tree,
			final String sortName, final String consName) {
		final IConstructor prod = getProduction(tree);
		return ProductionAdapter.getSortName(prod).equals(sortName)
				&& ProductionAdapter.getConstructorName(prod).equals(consName);
	}

	public static boolean isLexToCf(final IConstructor tree) {
		return isAppl(tree) ? ProductionAdapter.isLexToCf(getProduction(tree))
				: false;
	}

	public static boolean isContextFree(final IConstructor tree) {
		return isAppl(tree) ? ProductionAdapter
				.isContextFree(getProduction(tree)) : false;
	}

	public static boolean isList(final IConstructor tree) {
		return isAppl(tree) ? ProductionAdapter.isList(getProduction(tree))
				: false;
	}

	public static IList getArgs(final IConstructor tree) {
		if(isAppl(tree))
			return (IList) tree.get("args");

		throw new ImplementationError("Node has no args");
	}

	public static boolean isLiteral(final IConstructor tree) {
		return isAppl(tree) ? ProductionAdapter.isLiteral(getProduction(tree))
				: false;
	}

	public static IList getListASTArgs(final IConstructor tree) {
		if(!isContextFree(tree) || !isList(tree))
			throw new ImplementationError(
					"This is not a context-free list production: " + tree);
		final IList children = getArgs(tree);
		final IListWriter writer = Factory.Args.writer(ValueFactoryFactory
				.getValueFactory());

		for(int i = 0; i < children.length(); i++) {
			final IValue kid = children.get(i);
			writer.append(kid);
			// skip layout and/or separators
			i += isSeparatedList(tree) ? 3 : 1;
		}
		return writer.done();
	}

	public static boolean isLexical(final IConstructor tree) {
		return isAppl(tree) ? ProductionAdapter.isLexical(getProduction(tree))
				: false;
	}

	public static boolean isCfOptLayout(final IConstructor tree) {
		return isAppl(tree) ? ProductionAdapter
				.isCfOptLayout(getProduction(tree)) : false;
	}

	public static boolean isLayout(final IConstructor tree) {
		return isAppl(tree) ? ProductionAdapter.isLayout(getProduction(tree))
				: false;
	}

	private static boolean isSeparatedList(final IConstructor tree) {
		return isAppl(tree) ? isList(tree)
				&& ProductionAdapter.isSeparatedList(getProduction(tree))
				: false;
	}

	public static IList getASTArgs(final IConstructor tree) {
		if(getSortName(tree).equals("<START>"))
			return getArgs(tree).delete(0).delete(1);

		if(!isContextFree(tree))
			throw new ImplementationError(
					"This is not a context-free production: " + tree);

		final IList children = getArgs(tree);
		final IListWriter writer = Factory.Args.writer(ValueFactoryFactory
				.getValueFactory());

		for(int i = 0; i < children.length(); i++) {
			final IConstructor kid = (IConstructor) children.get(i);
			if(!isLiteral(kid) && !isCILiteral(kid))
				writer.append(kid);
			// skip layout
			i++;
		}
		return writer.done();
	}

	public static boolean isCILiteral(final IConstructor tree) {
		return isAppl(tree) ? ProductionAdapter
				.isCILiteral(getProduction(tree)) : false;
	}

	public static ISet getAlternatives(final IConstructor tree) {
		if(isAmb(tree))
			return (ISet) tree.get("alternatives");

		throw new ImplementationError("Node has no alternatives");
	}

	public static ISourceLocation getLocation(final IConstructor tree) {
		return (ISourceLocation) tree.getAnnotation(Factory.Location);
	}

	public static int getCharacter(final IConstructor tree) {
		return ((IInteger) tree.get("character")).intValue();
	}

	protected static class PositionAnnotator {
		private final IConstructor tree;
		private final MappingsCache<PositionNode, IConstructor> cache;
		private final boolean windowsOS = System.getProperty("os.name")
				.toLowerCase().indexOf("win") != -1;
		private boolean inLayout = false;
		private final boolean labelLayout = false;

		public PositionAnnotator(final IConstructor tree) {
			super();

			this.tree = tree;
			cache = new MappingsCache<PositionNode, IConstructor>();
		}

		public IConstructor addPositionInformation(final URI location) {
			Factory.getInstance(); // make sure everything is declared
			try {
				return addPosInfo(tree, location, new Position()); // Fix
				// filename
				// so URI's
				// work.
			}
			catch(final MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}

		private IConstructor addPosInfo(IConstructor tree, final URI location,
				Position cur) throws MalformedURLException {
			final IValueFactory factory = ValueFactoryFactory.getValueFactory();

			final int startLine = cur.line;
			final int startCol = cur.col;
			final int startOffset = cur.offset;
			final PositionNode positionNode = new PositionNode(tree, cur.offset);
			IConstructor result = cache.get(positionNode);

			if(result != null) {
				final ISourceLocation loc = getLocation(result);
				cur.col = loc.getEndColumn();
				cur.line = loc.getEndLine();
				cur.offset += loc.getLength();
				return result;
			}

			if(isChar(tree)) {
				cur.offset++;
				final char character = (char) getCharacter(tree);

				if(character == '\r') {
					cur.col++;
					cur.sawCR = true;
				}
				else if(character == '\n') {
					cur.col = 0;
					cur.line++;

					// Workaround to funny stuff in Eclipse editors
					if(windowsOS && !cur.sawCR)
						cur.offset++;
					cur.sawCR = false;
				}
				else {
					cur.col++;
					cur.sawCR = false;
				}
				return tree;
			}

			if(isAppl(tree)) {
				boolean outermostLayout = false;
				final IList args = getArgs(tree);

				if(isLayout(tree)) {
					inLayout = true;
					outermostLayout = true;
				}

				final IListWriter newArgs = factory.listWriter(Factory.Tree);
				for(final IValue arg : args)
					newArgs.append(addPosInfo((IConstructor) arg, location, cur));
				tree = tree.set("args", newArgs.done());

				if(!labelLayout && outermostLayout) {
					inLayout = false;
					if(!isComment(tree))
						return tree;
				}
				else if(!labelLayout && inLayout)
					if(!isComment(tree))
						return tree;
			}
			else if(isAmb(tree)) {
				final ISet alts = getAlternatives(tree);
				final ISetWriter newAlts = ValueFactoryFactory
						.getValueFactory().setWriter(Factory.Tree);
				final Position save = cur;
				Position newPos = save;
				final ISetWriter cycles = ValueFactoryFactory.getValueFactory()
						.setWriter(Factory.Tree);

				for(final IValue arg : alts) {
					cur = save.clone();

					final IValue newArg = addPosInfo((IConstructor) arg,
							location, cur);

					if(cur.offset != save.offset) {
						newPos = cur;
						newAlts.insert(newArg);
					}
					else if(newPos.offset == save.offset)
						cycles.insert(arg);
					else
						newAlts.insert(newArg);
				}

				cur = save;
				cur.col = newPos.col;
				cur.line = newPos.line;
				cur.offset = newPos.offset;
				cur.sawCR = newPos.sawCR;

				for(final IValue arg : cycles.done()) {
					final IValue newArg = addPosInfo((IConstructor) arg,
							location, cur);
					newAlts.insert(newArg);
				}

				tree = tree.set("alternatives", newAlts.done());
			}
			else if(!isCycle(tree))
				throw new ImplementationError("unhandled tree: " + tree + "\n");

			final ISourceLocation loc = factory.sourceLocation(location,
					startOffset, cur.offset - startOffset, startLine, cur.line,
					startCol, cur.col);
			result = tree.setAnnotation(Factory.Location, loc);

			cache.putUnsafe(positionNode, result);

			return result;
		}

		private static class Position {
			public int col = 0;
			public int line = 1;
			public int offset = 0;
			public boolean sawCR = false;

			@Override
			public Position clone() {
				final Position tmp = new Position();
				tmp.col = col;
				tmp.line = line;
				tmp.offset = offset;
				tmp.sawCR = sawCR;
				return tmp;
			}

			@Override
			public String toString() {
				return "offset: " + offset + ", line: " + line + ", col:" + col
						+ ", sawCR:" + sawCR;

			}
		}

		private static class PositionNode {
			private final IConstructor tree;
			private final int offset;

			public PositionNode(final IConstructor tree, final int offset) {
				super();

				this.tree = tree;
				this.offset = offset;
			}

			@Override
			public int hashCode() {
				return offset << 32 ^ tree.hashCode();
			}

			@Override
			public boolean equals(final Object o) {
				if(o.getClass() != getClass())
					return false;

				final PositionNode other = (PositionNode) o;

				return offset == other.offset && tree == other.tree; // NOTE:
				// trees
				// are
				// shared,
				// so
				// they
				// are
				// pointer
				// equal.
			}
		}
	}

	private static class Unparser extends IdentityTreeVisitor {
		private final OutputStream fStream;

		public Unparser(final OutputStream stream) {
			fStream = stream;
		}

		@Override
		public IConstructor visitTreeAmb(final IConstructor arg)
				throws VisitorException {
			((ISet) arg.get("alternatives")).iterator().next().accept(this);
			return arg;
		}

		@Override
		public IConstructor visitTreeChar(final IConstructor arg)
				throws VisitorException {
			try {
				fStream.write(((IInteger) arg.get("character")).intValue());
				return arg;
			}
			catch(final IOException e) {
				throw new VisitorException(e);
			}
		}

		@Override
		public IConstructor visitTreeAppl(final IConstructor arg)
				throws VisitorException {
			final IList children = (IList) arg.get("args");
			for(final IValue child : children)
				child.accept(this);
			return arg;
		}

	}

	public static IConstructor locateLexical(final IConstructor tree,
			final int offset) {
		final ISourceLocation l = TreeAdapter.getLocation(tree);

		if(l == null)
			throw new IllegalArgumentException(
					"locate assumes position information on the tree");

		if(TreeAdapter.isLexToCf(tree)) {
			if(l.getOffset() <= offset
					&& offset < l.getOffset() + l.getLength())
				return tree;

			return null;
		}

		if(TreeAdapter.isAmb(tree))
			return null;

		if(TreeAdapter.isAppl(tree)) {
			final IList children = TreeAdapter.getASTArgs(tree);

			for(final IValue child : children) {
				final ISourceLocation childLoc = TreeAdapter
						.getLocation((IConstructor) child);

				if(childLoc == null)
					continue;

				if(childLoc.getOffset() <= offset
						&& offset < childLoc.getOffset() + childLoc.getLength()) {
					final IConstructor result = locateLexical(
							(IConstructor) child, offset);

					if(result != null)
						return result;
					break;
				}
			}

			if(l.getOffset() <= offset
					&& l.getOffset() + l.getLength() >= offset)
				return tree;
		}

		return null;
	}

	public static IConstructor locateAnnotatedTree(final IConstructor tree,
			final String label, final int offset) {
		final ISourceLocation l = TreeAdapter.getLocation(tree);

		if(l == null)
			throw new IllegalArgumentException(
					"locate assumes position information on the tree");

		if(TreeAdapter.isLexToCf(tree)) {
			if(l.getOffset() <= offset
					&& offset < l.getOffset() + l.getLength())
				if(tree.hasAnnotation(label))
					return tree;

			// we're at a leaf, but not in position (so zoom out)
			return null;
		}

		if(TreeAdapter.isAmb(tree)) {
			if(tree.hasAnnotation(label))
				return tree;

			return null;
		}

		if(TreeAdapter.isAppl(tree)) {
			final IList children = TreeAdapter.getASTArgs(tree);

			for(final IValue child : children) {
				final ISourceLocation childLoc = TreeAdapter
						.getLocation((IConstructor) child);

				if(childLoc == null)
					continue;

				if(childLoc.getOffset() <= offset
						&& offset < childLoc.getOffset() + childLoc.getLength()) {
					final IConstructor result = locateAnnotatedTree(
							(IConstructor) child, label, offset);

					if(result != null)
						return result;
				}
			}

			if(l.getOffset() <= offset
					&& l.getOffset() + l.getLength() >= offset) {
				if(tree.hasAnnotation(label))
					return tree;

				// in scope, but no annotation, so zoom out
				return null;
			}
		}

		return null;
	}

	public static void unparse(final IConstructor tree,
			final OutputStream stream) throws IOException, FactTypeUseException {
		try {
			if(tree.getConstructorType() == Factory.ParseTree_Top)
				tree.get("top").accept(new Unparser(stream));
			else if(tree.getType() == Factory.Tree)
				tree.accept(new Unparser(stream));
			else
				throw new ImplementationError("Can not unparse this "
						+ tree.getType());
		}
		catch(final VisitorException e) {
			final Throwable cause = e.getCause();

			if(cause instanceof IOException)
				throw (IOException) cause;

			throw new ImplementationError("Unexpected error in unparse: "
					+ e.getMessage());
		}
	}

	public static String yield(final IConstructor tree)
			throws FactTypeUseException {
		try {
			final ByteArrayOutputStream stream = new ByteArrayOutputStream();
			unparse(tree, stream);
			return stream.toString();
		}
		catch(final IOException e) {
			throw new ImplementationError("Method yield failed", e);
		}
	}

	public static boolean isContextFreeInjectionOrSingleton(
			final IConstructor tree) {
		final IConstructor prod = getProduction(tree);
		if(isAppl(tree)) {
			if(!ProductionAdapter.isList(prod)
					&& ProductionAdapter.getLhs(prod).length() == 1) {
				IConstructor rhs = ProductionAdapter.getRhs(prod);
				if(SymbolAdapter.isCf(rhs)) {
					rhs = SymbolAdapter.getSymbol(rhs);
					if(SymbolAdapter.isSort(rhs))
						return true;
				}
			}
		}
		else if(isList(tree)
				&& SymbolAdapter.isCf(ProductionAdapter.getRhs(prod)))
			if(getArgs(tree).length() == 1)
				return true;
		return false;
	}

	public static boolean isAmbiguousList(final IConstructor tree) {
		if(isAmb(tree)) {
			final IConstructor first = (IConstructor) getAlternatives(tree)
					.iterator().next();
			if(isList(first))
				return true;
		}
		return false;
	}

	public static boolean isNonEmptyStarList(final IConstructor tree) {
		if(isAppl(tree)) {
			final IConstructor prod = getProduction(tree);

			if(ProductionAdapter.isList(prod)) {
				IConstructor sym = ProductionAdapter.getRhs(prod);

				if(SymbolAdapter.isCf(sym) || SymbolAdapter.isLex(sym))
					sym = SymbolAdapter.getSymbol(sym);

				if(SymbolAdapter.isIterStar(sym)
						|| SymbolAdapter.isIterStarSep(sym))
					return getArgs(tree).length() > 0;
			}
		}
		return false;
	}

	public static boolean isPlusList(final IConstructor tree) {
		if(isAppl(tree)) {
			final IConstructor prod = getProduction(tree);

			if(ProductionAdapter.isList(prod)) {
				IConstructor sym = ProductionAdapter.getRhs(prod);

				if(SymbolAdapter.isCf(sym) || SymbolAdapter.isLex(sym))
					sym = SymbolAdapter.getSymbol(sym);

				if(SymbolAdapter.isIterPlus(sym)
						|| SymbolAdapter.isIterPlusSep(sym))
					return true;
			}
		}
		return false;

	}

	public static boolean isCFList(final IConstructor tree) {
		return isAppl(tree)
				&& isContextFree(tree)
				&& (SymbolAdapter.isPlusList(ProductionAdapter
						.getRhs(getProduction(tree))) || SymbolAdapter
						.isStarList(ProductionAdapter
								.getRhs(getProduction(tree))));
	}

	/**
	 * @return true if the tree does not have any characters, it's just an empty
	 *         derivation
	 */
	public static boolean isEpsilon(final IConstructor tree) {
		if(isAppl(tree)) {
			for(final IValue arg : getArgs(tree)) {
				final boolean argResult = isEpsilon((IConstructor) arg);

				if(!argResult)
					return false;
			}

			return true;
		}

		if(isAmb(tree))
			return isEpsilon((IConstructor) getAlternatives(tree).iterator()
					.next());

		if(isCycle(tree))
			return true;

		// is a character
		return false;
	}

	public static boolean hasPreferAttribute(final IConstructor tree) {
		return ProductionAdapter.hasPreferAttribute(getProduction(tree));
	}

	public static boolean hasAvoidAttribute(final IConstructor tree) {
		return ProductionAdapter.hasAvoidAttribute(getProduction(tree));
	}

	public static IList searchCategory(final IConstructor tree,
			final String category) {
		final IListWriter writer = Factory.Args.writer(ValueFactoryFactory
				.getValueFactory());
		if(isAppl(tree)) {
			final String s = ProductionAdapter.getCategory(getProduction(tree));
			if(s == category)
				writer.append(tree);
			else {
				final IList z = getArgs(tree);
				for(final IValue q : z) {
					if(!(q instanceof IConstructor))
						continue;
					final IList p = searchCategory((IConstructor) q, category);
					writer.appendAll(p);
				}
			}
		}
		return writer.done();
	}
}
