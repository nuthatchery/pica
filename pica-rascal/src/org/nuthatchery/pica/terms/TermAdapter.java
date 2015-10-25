/**************************************************************************
 * Copyright (c) 2010-2013 Anya Helene Bagge
 * Copyright (c) 2010-2013 University of Bergen
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

import static org.nuthatchery.pica.terms.TermFactory.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.rascalmpl.value.IAnnotatable;
import org.rascalmpl.value.IConstructor;
import org.rascalmpl.value.IInteger;
import org.rascalmpl.value.IList;
import org.rascalmpl.value.INode;
import org.rascalmpl.value.ISet;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.value.IString;
import org.rascalmpl.value.ITuple;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.type.Type;
import org.rascalmpl.value.visitors.NullVisitor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.rascal.RascalUtil;
import org.nuthatchery.pica.resources.regions.ICodeRegion;
import org.nuthatchery.pica.util.Pair;

class IConstructorIterableWrapper implements Iterable<IConstructor> {
	private final Iterable<IValue> iterable;


	public IConstructorIterableWrapper(final Iterable<IValue> iterable) {
		this.iterable = iterable;
	}


	@Override
	public Iterator<IConstructor> iterator() {
		return new IConstructorIteratorWrapper(iterable);
	}
}

class IConstructorIteratorWrapper implements Iterator<IConstructor> {
	private final Iterator<IValue> iterator;
	private IConstructor next = null;


	public IConstructorIteratorWrapper(final Iterable<IValue> iterable) {
		iterator = iterable.iterator();
		next = null;
		while(iterator.hasNext()) {
			IValue val = iterator.next();
			if(val instanceof IConstructor) {
				next = (IConstructor) val;
				break;
			}
		}
	}


	@Override
	public boolean hasNext() {
		return next != null;
	}


	@Override
	public IConstructor next() {
		IConstructor result = next;
		next = null;
		while(iterator.hasNext()) {
			IValue val = iterator.next();
			if(val instanceof IConstructor) {
				next = (IConstructor) val;
				break;
			}
		}
		return result;
	}


	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}

/**
 * @author anya
 *
 */
@NonNullByDefault
public final class TermAdapter {
	@SuppressWarnings("null")
	public static final ISourceLocation LOC_UNKNOWN = TermFactory.vf.sourceLocation("unknown://");
	@SuppressWarnings("null")
	private static Pattern quoteChars = Pattern.compile("([\\\"])");


	/**
	 * The number of children of a constructor or list.
	 *
	 * @param tree
	 * @return Constructor arity, list length, or 0.
	 */
	public static int arity(final IConstructor tree) {
		if(isSeq(tree)) {
			return ((IList) tree.get("args")).length();
		}
		else if(isCons(tree)) {
			return tree.arity();
		}
		else {
			return 0;
		}
	}


	public static ICodeRegion<URI> codeRegionOf(IConstructor tree) {
		return RascalUtil.toCodeRegion(locOf(tree));
	}


	/**
	 * Get the child of a term.
	 *
	 * Works on both constructors and lists. Does nothing on leaves.
	 *
	 * With multiple arguments, follows the path to the desired element.
	 *
	 * @param tree
	 *            A term
	 * @param arg
	 *            Child to select
	 * @param args
	 *            For selecting descendants of the child
	 * @return The selected child/descendant
	 */
	@SuppressWarnings("null")
	public static IConstructor getArg(IConstructor tree, int arg, int... args) {
		for(int i = -1; i < args.length; i++) {
			if(i >= 0) {
				arg = args[i];
			}
			if(isSeq(tree)) {
				tree = (IConstructor) ((IList) tree.get("args")).get(arg);
			}
			else if(isLeaf(tree) || isVar(tree)) {
				return tree;
			}
			else {
				tree = (IConstructor) tree.get(arg);
			}
		}
		return tree;
	}


	/**
	 * Get the children of a constructor or list
	 *
	 * @param tree
	 *            A term
	 * @return Iterator over the children
	 * @requires hasChildren(tree)
	 */
	public static Iterable<IConstructor> getChildren(final IConstructor tree) {
		assert hasChildren(tree);

		if(isSeq(tree)) {
			return new IConstructorIterableWrapper((IList) tree.get("args"));
		}
		else {
			return new IConstructorIterableWrapper(tree.getChildren());
		}

	}


	/**
	 * Get the children of a constructor or list
	 *
	 * @param tree
	 *            A term
	 * @return List of children
	 * @requires isSeq(tree)
	 */
	public static IList getChildrenList(final IConstructor tree) {
		assert isSeq(tree);

		if(isSeq(tree)) {
			return (IList) tree.get("args");
		}
		else {
			throw new UnsupportedOperationException();
		}

	}


	/**
	 * @param tree
	 *            A term
	 * @return Location annotation of the term, or null
	 */
	@Nullable
	public static ISourceLocation getLocation(IValue tree) {
		if(tree instanceof IConstructor) {
			return (ISourceLocation) tree.asAnnotatable().getAnnotation("loc");
		}
		else {
			return null;
		}
	}


	/**
	 * @param tree
	 *            A term
	 * @return Constructor name of term
	 */
	@SuppressWarnings("null")
	public static String getName(final IConstructor tree) {
		if(isVar(tree)) {
			return ((IString) tree.get("name")).getValue();
		}
		else {
			return tree.getName();
		}
	}


	@SuppressWarnings("unused")
	@Nullable
	public static String getSort(final IConstructor tree) {
		return null; // (IString) tree.get("sort");
	}


	/**
	 * @param tree
	 *            A leaf term
	 * @return String value of leaf or ""
	 */
	@SuppressWarnings("null")
	public static String getString(final IConstructor tree) {
		if(isLeaf(tree)) {
			return ((IString) tree.get("strVal")).getValue();
		}
		else {
			return "";
		}
	}


	/**
	 * @param tree
	 *            A term
	 * @return True if term has children
	 */
	public static boolean hasChildren(final @Nullable IConstructor tree) {
		return isSeq(tree) || isCons(tree);
	}


	/**
	 * @param tree
	 *            A term
	 * @return True if term is a constructor
	 */
	public static boolean isCons(final @Nullable IConstructor tree) {
		if(tree == null) {
			return false;
		}
		final Type constype = tree.getConstructorType();
		return constype != Cons_Seq && constype != Cons_Leaf && constype != Cons_Var;
	}


	/**
	 * @param tree
	 *            A term
	 * @param name
	 *            A name
	 * @return True if term is a constructor with the given name
	 */
	public static boolean isCons(final @Nullable IConstructor tree, final @Nullable String name) {
		return tree != null && tree.getName().equals(name);
	}


	/**
	 * @param tree
	 *            A term
	 * @param name
	 *            A name
	 * @param arity
	 *            A term arity
	 * @return True if term is a constructor with the given name and arity
	 */
	public static boolean isCons(final @Nullable IConstructor tree, final @Nullable String name, final int arity) {
		return tree != null && tree.getName().equals(name) && tree.arity() == arity;
	}


	/**
	 * @param tree
	 *            A value
	 * @return True if value is a constructor term
	 */
	public static boolean isCons(final @Nullable IValue tree) {
		return tree instanceof IConstructor && isCons((IConstructor) tree);
	}


	/**
	 * @param tree
	 *            A value
	 * @param name
	 *            A name
	 * @return True if value is a constructor term with the given name
	 */
	public static boolean isCons(final @Nullable IValue tree, final @Nullable String name) {
		return tree instanceof IConstructor && isCons((IConstructor) tree, name);
	}


	/**
	 * @param tree
	 *            A value
	 * @param name
	 *            A name
	 * @param arity
	 *            A term arity
	 * @return True if value is a constructor term with the given name and arity
	 */
	public static boolean isCons(final @Nullable IValue tree, final @Nullable String name, final int arity) {
		return tree instanceof IConstructor && isCons((IConstructor) tree, name, arity);
	}


	/**
	 * @param tree
	 *            A term
	 * @return True if term is a leaf
	 */
	public static boolean isLeaf(final @Nullable IConstructor tree) {
		return tree != null && tree.getConstructorType() == Cons_Leaf;
	}


	/**
	 * @param tree
	 *            A term
	 * @param chars
	 *            A string
	 * @return True if term is a leaf consisting of the given string
	 */
	public static boolean isLeaf(final @Nullable IConstructor tree, final @Nullable String chars) {
		return tree != null && tree.getConstructorType() == Cons_Leaf && ((IString) tree.get("strVal")).getValue().equals(chars);
	}


	/**
	 * @param tree
	 *            A value
	 * @return True if value is a leaf
	 */
	public static boolean isLeaf(final @Nullable IValue tree) {
		return tree instanceof IConstructor && isLeaf((IConstructor) tree);
	}


	/**
	 * @param tree
	 *            A value
	 * @param chars
	 *            A string
	 * @return True if value is a leaf consisting of the given string
	 */
	public static boolean isLeaf(final @Nullable IValue tree, final @Nullable String chars) {
		return tree instanceof IConstructor && isLeaf((IConstructor) tree, chars);
	}


	/**
	 * @param tree
	 *            A term
	 * @return True if term is a list
	 */
	public static boolean isSeq(final @Nullable IConstructor tree) {
		return tree != null && tree.getConstructorType() == Cons_Seq;
	}


	/**
	 * @param tree
	 *            A value
	 * @return True if value is a list term
	 */
	public static boolean isSeq(final @Nullable IValue tree) {
		return tree instanceof IConstructor && isSeq((IConstructor) tree);
	}


	/**
	 * @param tree
	 *            A term
	 * @return True if term is a variable
	 */
	public static boolean isVar(final @Nullable IConstructor tree) {
		return tree != null && tree.getConstructorType() == Cons_Var;
	}


	/**
	 * @param tree
	 *            A term
	 * @param name
	 *            A name
	 * @return True if term is a variable with the given name
	 */
	public static boolean isVar(final @Nullable IConstructor tree, final @Nullable String name) {
		return tree != null && tree.getConstructorType() == Cons_Var && ((IString) tree.get("name")).getValue().equals(name);
	}


	/**
	 * @param tree
	 *            A value
	 * @return True if value is a variable term
	 */
	public static boolean isVar(final @Nullable IValue tree) {
		return tree instanceof IConstructor && isVar((IConstructor) tree);
	}


	/**
	 * @param tree
	 *            A value
	 * @param name
	 *            A name
	 * @return True if value is a variable term with the given name
	 */
	public static boolean isVar(final @Nullable IValue tree, final @Nullable String name) {
		return tree instanceof IConstructor && isVar((IConstructor) tree, name);
	}


	/**
	 * @param input
	 *            A string representation of a term
	 * @return The string as a term
	 */
	public static IConstructor loadTerm(String input) {
		return new TermLoader(input).parseTerm();
	}


	/**
	 * @param tree
	 *            A term
	 * @return Location of term, or {@link #LOC_UNKNOWN}
	 */
	public static ISourceLocation locOf(IConstructor tree) {
		IAnnotatable<? extends IConstructor> atree = tree.asAnnotatable();
		if(atree.hasAnnotation("loc")) {
			return (ISourceLocation) atree.getAnnotation("loc");
		}
		else {
			return LOC_UNKNOWN;
		}
	}


	@SuppressWarnings("null")
	@Nullable
	public static Pair<IValue, IValue> oneDiff(IConstructor a, IConstructor b) {
		if(a == b) {
			return null;
		}

		if(!a.getConstructorType().equivalent(b.getConstructorType()) || a.arity() != b.arity()) {
			return new Pair<IValue, IValue>(a, b);
		}

		for(int i = 0; i < a.arity(); i++) {
			IValue ca = a.get(i);
			IValue cb = b.get(i);
			if(ca instanceof IConstructor && cb instanceof IConstructor) {
				Pair<IValue, IValue> p = oneDiff((IConstructor) a.get(i), (IConstructor) b.get(i));
				if(p != null) {
					return p;
				}
			}
			else if(!ca.isEqual(cb)) {
				return new Pair<>(ca, cb);
			}
		}
		return null;
	}


	@Nullable
	public static Pair<IValue, Pair<HashMap<String, IValue>, HashMap<String, IValue>>> oneDiffWithAnnos(IValue a, IValue b) {
		if(a == b) {
			return null;
		}
		if(!a.isEqual(b)) {
			throw new IllegalArgumentException("Arguments should be isEqual()");
		}

		if(a.isAnnotatable() && b.isAnnotatable()) {
			Map<String, IValue> annosa = a.asAnnotatable().getAnnotations();
			Map<String, IValue> annosb = b.asAnnotatable().getAnnotations();

			if(!annosa.equals(annosb)) {
				HashMap<String, IValue> mapA = new HashMap<>(annosa);
				HashMap<String, IValue> mapB = new HashMap<>(annosb);
				for(Entry<String, IValue> e : annosb.entrySet()) {
					if(annosa.containsKey(e.getKey()) && annosa.get(e.getKey()).equals(annosb.get(e.getKey()))) {
						mapA.remove(e.getKey());
						mapB.remove(e.getKey());
					}
				}
				return new Pair<>(a, new Pair<>(mapA, mapB));

			}
		}

		if(a instanceof IConstructor && b instanceof IConstructor) {

			for(int i = 0; i < ((INode) a).arity(); i++) {
				@SuppressWarnings("null")
				Pair<IValue, Pair<HashMap<String, IValue>, HashMap<String, IValue>>> p = oneDiffWithAnnos(((IConstructor) a).get(i), ((IConstructor) b).get(i));
				if(p != null) {
					return p;
				}
			}
		}
		else if(a instanceof IList && b instanceof IList) {

			for(int i = 0; i < ((IList) a).length(); i++) {
				@SuppressWarnings("null")
				Pair<IValue, Pair<HashMap<String, IValue>, HashMap<String, IValue>>> p = oneDiffWithAnnos(((IList) a).get(i), ((IList) b).get(i));
				if(p != null) {
					return p;
				}
			}
		}

		if(!a.equals(b)) {
			System.err.println("  A: " + a);
			System.err.println("  B: " + b);
		}
		return null;
	}


	@SuppressWarnings("null")
	public static IConstructor preserveAnnos(IConstructor tree, IConstructor annoSource) {
		return tree.asAnnotatable().setAnnotations(annoSource.asAnnotatable().getAnnotations());
	}


	/*
	 * public static boolean isGround(IConstructor tree) { try { return
	 * tree.accept(new NullVisitor<Boolean>() { public Boolean
	 * visitConstructor(IConstructor c) { if(isLeaf(c)) return true; else
	 * if(isVar(c)) return false; for(IValue child : (IList) c.get("args"))
	 * if(!isGround(child)) return false; return true; }}); } catch
	 * (VisitorException e) { return false; } }
	 *
	 * public static IList vars(IConstructor tree) {
	 *
	 * final IListWriter lw = vf.listWriter(Type_AST);
	 *
	 * try { tree.accept(new IdentityVisitor() { public IValue
	 * visitConstructor(IConstructor c) throws VisitorException { if(isVar(c))
	 * lw.append(c); else if(isCons(c) || isSeq(c)) for(IValue child : (IList)
	 * c.get("args")) child.accept(this); return c; }}); } catch
	 * (VisitorException e) { throw new ImplementationError("Visitor error", e);
	 * }
	 *
	 * return lw.done(); }
	 */
	@SuppressWarnings("null")
	public static String yield(final IValue tree) {
		if(!tree.getType().isSubtypeOf(Type_AST)) {
			return tree.toString();
		}

		return tree.accept(new NullVisitor<String, RuntimeException>() {
			@Override
			public String visitConstructor(final @Nullable IConstructor c) {
				final IList concrete = (IList) c.asAnnotatable().getAnnotation("concrete");
				final StringBuilder result = new StringBuilder(1024);
				if(concrete == null || concrete.length() == 0) {
					if(isLeaf(c)) {
						return getString(c);
					}
					else if(isVar(c)) {
						return "<" + getName(c) + ">";
					}
					else {
						for(final IConstructor child : getChildren(c)) {
							result.append(child.accept(this));
						}
						return result.toString();
					}
				}

				for(final IValue token : concrete) {
					final Type type = ((IConstructor) token).getConstructorType();
					if(type.equivalent(Cons_Token) || type.equivalent(Cons_Space) || type.equivalent(Cons_Comment)) {
						result.append(((IString) ((IConstructor) token).get("chars")).getValue());
					}
					else {
						final int index = ((IInteger) ((IConstructor) token).get("index")).intValue();
						result.append(getArg(c, index).accept(this));
					}
				}
				return result.toString();
			}

		});
	}


	/**
	 * @param tree
	 *            A term
	 * @return The term as a string
	 */
	@SuppressWarnings("null")
	public static String yieldTerm(@Nullable IValue tree) {
		if(tree != null) {
			StringBuilder result = new StringBuilder(1024);
			yieldTerm(tree, false, result);
			return result.toString();
		}
		else {
			return "";
		}
	}


	/**
	 * @param tree
	 *            A term
	 * @param withAnnos
	 *            True if annotations should appear in the string
	 * @return The term as a string
	 */
	@SuppressWarnings("null")
	public static String yieldTerm(@Nullable IValue tree, boolean withAnnos) {
		if(tree != null) {
			StringBuilder result = new StringBuilder(1024);
			yieldTerm(tree, withAnnos, result);
			return result.toString();
		}
		else {
			return "";
		}
	}


	@SuppressWarnings("null")
	private static void yieldTerm(IValue tree, boolean withAnnos, StringBuilder output) {
		if(tree instanceof IConstructor) {
			final IConstructor c = (IConstructor) tree;

			final Type constype = c.getConstructorType();

			if(constype == Cons_Seq) {
				yieldTerm(c.get("args"), withAnnos, output);
			}
			else if(constype == Cons_Leaf) {
				output.append('\"');
				output.append(quoteChars.matcher(((IString) c.get("strVal")).getValue()).replaceAll("\\\\$1"));
				output.append('\"');
			}
			else if(constype == Cons_Var) {
				output.append(((IString) c.get("name")).getValue());
			}
			else {
				output.append(c.getName());
				output.append('(');
				yieldTermList(c, withAnnos, output);
				output.append(')');
			}

		}
		else if(tree instanceof IList) {
			output.append('[');
			yieldTermList((IList) tree, withAnnos, output);
			output.append(']');
		}
		else if(tree instanceof ISet) {
			output.append('{');
			yieldTermList((ISet) tree, withAnnos, output);
			output.append('}');
		}
		else if(tree instanceof ITuple) {
			output.append('<');
			yieldTermList((ITuple) tree, withAnnos, output);
			output.append('>');
		}
		else {
			output.append(tree.toString());
		}
	}


	@SuppressWarnings("null")
	private static void yieldTermList(Iterable<IValue> list, boolean withAnnos, StringBuilder output) {
		boolean first = true;

		for(final IValue child : list) {
			if(!first) {
				output.append(", ");
			}
			yieldTerm(child, withAnnos, output);
			first = false;
		}
	}


	private TermAdapter() {

	}

}
