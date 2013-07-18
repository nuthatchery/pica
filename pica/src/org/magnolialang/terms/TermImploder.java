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
 * it under the terms of the GNU General Public License as published by
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
package org.magnolialang.terms;

import static org.magnolialang.terms.TermFactory.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IListWriter;
import org.eclipse.imp.pdb.facts.INode;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.utils.Pair;
import org.magnolialang.pica.Pica;
import org.rascalmpl.values.uptr.Factory;
import org.rascalmpl.values.uptr.ProductionAdapter;
import org.rascalmpl.values.uptr.SymbolAdapter;
import org.rascalmpl.values.uptr.TreeAdapter;

public final class TermImploder {
	private final static boolean DIAGNOSE_AMB = true;

	public static final Type Attr = tf.abstractDataType(ts, "Attr");

	public static final Type Attr_Abstract = tf.constructor(ts, Attr, "selectable");
	private static final Pattern LAYOUT_PAT = Pattern.compile("^(\\s*)(\\S.*\\S)(\\s*)$", Pattern.DOTALL);

	private static final Pattern PAT_SPACE = Pattern.compile("^([^\r\n]*)([\r\n]+)(.*)$", Pattern.DOTALL);


	private TermImploder() {

	}


	public static String getSortName(final IConstructor tree) {
		IConstructor type = ProductionAdapter.getType(tree);

		while(SymbolAdapter.isAnyList(type) || type.getConstructorType() == Factory.Symbol_Opt || type.getConstructorType() == Factory.Symbol_Alt) {
			type = SymbolAdapter.getSymbol(type);
		}

		if(SymbolAdapter.isSort(type) || SymbolAdapter.isParameterizedSort(type))
			return SymbolAdapter.getName(type);

		return "";
	}


	public static IConstructor implode(final IConstructor tree) {

		IConstructor result = null;
		IList concrete = null;

		final Type nodeType = tree.getConstructorType();
		if(nodeType == Factory.Tree_Appl) {
			final IConstructor prod = TreeAdapter.getProduction(tree);
			IList syms = null;
			if(!ProductionAdapter.isList(prod)) {
				syms = ProductionAdapter.getSymbols(prod);
			}
			final IConstructor type = ProductionAdapter.getType(prod);
			final ISet attrs = ProductionAdapter.getAttributes(prod);
			final Map<String, IValue> newAttrs = new HashMap<String, IValue>();
			boolean hasAbstract = false;
			String cons = ProductionAdapter.getConstructorName(prod);
			final String sort = getSortName(prod);

			for(final IValue attr : attrs) {
				if(attr.getType().isAbstractData() && ((IConstructor) attr).getConstructorType() == Factory.Attr_Tag) {
					final IValue value = ((IConstructor) attr).get("tag");
					if(value.getType().isNode()) {
						INode node = (INode) value;
						if(node.getName().equals("abstract")) {
							hasAbstract = true;
						}
						else if(node.arity() == 0) {
							newAttrs.put(node.getName(), vf.bool(true));
						}
						else if(node.arity() == 1) {
							newAttrs.put(node.getName(), node.get(0));
						}
						else {
							newAttrs.put(node.getName(), node);
						}
					}
				}
			}

			// String name = SymbolAdapter.getName(rhs);
			// Token: [lex] -> cf
			if(ProductionAdapter.isLexical(prod) || ProductionAdapter.isKeyword(prod)) {
				final String str = TreeAdapter.yield(tree);
				if(cons == null)
					return check(leaf(str).setAnnotation("loc", TreeAdapter.getLocation(tree)));
				else {
					result = cons(cons, check(leaf(str).setAnnotation("loc", TreeAdapter.getLocation(tree))));
				}
			}
			// Injection [cf] -> [cf], no cons
			else if(syms != null && syms.length() == 1 && cons == null) {
				if(hasAbstract)
					// TODO: fix type of tree
					return check(implode((IConstructor) TreeAdapter.getArgs(tree).get(0)));
				else
					return check(implode((IConstructor) TreeAdapter.getArgs(tree).get(0)));
			}
			else if(ProductionAdapter.hasAttribute(prod, Factory.Attribute_Bracket) && syms != null && syms.length() == 5) {
				IConstructor t = (IConstructor) TreeAdapter.getArgs(tree).get(2);
				return check(implode(t));
			}
			else if(SymbolAdapter.isStartSort(ProductionAdapter.getDefined(prod))) {
				// IConstructor prod = TreeAdapter.getProduction(pt);

				final Pair<IValue[], IList> t = visitChildren(TreeAdapter.getArgs(tree));
				assert t.first.length == 1;
				result = (IConstructor) t.first[0];
				IList innerConcrete = (IList) result.getAnnotation("concrete");
				IListWriter concreteWriter = vf.listWriter(TermFactory.Type_XaToken);
				for(IValue tok : t.second) {
					if(((IConstructor) tok).getConstructorType().equivalent(Cons_Child)) {
						concreteWriter.appendAll(innerConcrete);
					}
					else {
						concreteWriter.append(tok);
					}
				}
				concrete = concreteWriter.done();
			}
			else if(ProductionAdapter.isList(prod)) {
				final Pair<IValue[], IList> t = visitChildren(TreeAdapter.getArgs(tree));
				concrete = t.second;
				result = seq(t.first);
			}
			// Alternative: cf -> cf(alt(_,_))
			else if(type.getConstructorType() == Factory.Symbol_Alt)
				return check(implode((IConstructor) TreeAdapter.getArgs(tree).get(0)));
			else if(syms != null && syms.length() == 0 && type.getConstructorType() == Factory.Symbol_Opt)
				return check(seq().setAnnotation("loc", TreeAdapter.getLocation(tree)));
			else if(type.getConstructorType() == Factory.Symbol_Opt)
				return check(seq(implode((IConstructor) TreeAdapter.getArgs(tree).get(0))));
			else {
/*				if(ProductionAdapter.isRegular(tree))
					System.out.println("Regular");
 */				final Pair<IValue[], IList> t = visitChildren(TreeAdapter.getArgs(tree));
				concrete = t.second;
				result = cons(cons == null ? sort : cons, t.first);
			}

			if(result != null) {
				result = result.setAnnotations(newAttrs);
			}

		}
		else if(nodeType == Factory.Tree_Amb) {
			if(DIAGNOSE_AMB) {
				System.out.println("Ambiguity detected! The doctor says: ");
				IList msgs = (IList) Pica.get().getEvaluatorPool("Dr. Ambiguity", Arrays.asList("Ambiguity")).call("diagnose", tree);
				for(IValue msg : msgs) {
					System.out.println("  " + msg);
				}
			}

			return implode((IConstructor) TreeAdapter.getAlternatives(tree).iterator().next());
		}
		else
			return null;
		// throw new ImplementationError("TermImploder does not implement: "
		// + nodeType);

		if(result == null)
			return null;
		else {
			ISourceLocation loc = TreeAdapter.getLocation(tree);
			if(loc == null) {
				int arity = TermAdapter.arity(result);
				if(arity == 1) {
					loc = (ISourceLocation) TermAdapter.getArg(result, 0).getAnnotation("loc");
				}
				else if(arity > 1) {
					loc = (ISourceLocation) TermAdapter.getArg(result, 0).getAnnotation("loc");
					ISourceLocation endLoc = (ISourceLocation) TermAdapter.getArg(result, arity - 1).getAnnotation("loc");
					int beginCol = loc.getBeginColumn();
					int beginLine = loc.getBeginLine();
					int endCol = endLoc.getEndColumn();
					int endLine = endLoc.getEndLine();
					int length = (endLoc.getOffset() + endLoc.getLength()) - loc.getOffset();
					int offset = loc.getOffset();
					URI uri = loc.getURI();
					loc = vf.sourceLocation(uri, offset, length, beginLine, endLine, beginCol, endCol);
				}
			}
			if(loc != null) {
				result = result.setAnnotation("loc", loc);
			}
			if(concrete != null) {
				result = result.setAnnotation("concrete", concrete);
			}
			return check(result);
		}
	}


	/**
	 * Implode an AsFix tree to an XaTree.
	 * 
	 * @param tree
	 *            A tree in AsFix format
	 * @return An imploded XaTree
	 */
	public static IConstructor implodeTree(final IConstructor tree) {
		if(tree.getConstructorType() == Factory.Tree_Appl) {
			IConstructor prod = TreeAdapter.getProduction(tree);
			IList args = TreeAdapter.getArgs(tree);
			if(ProductionAdapter.isLexical(prod) && args.length() == 3)
				return implode((IConstructor) args.get(1));
		}
		return implode(tree);
	}


	public static Pair<IValue[], IList> visitChildren(final IList trees) throws FactTypeUseException {
		final List<IValue> ast = new ArrayList<IValue>();
		final IListWriter cst = vf.listWriter(Type_XaToken);
		int i = 0;
		for(final IValue v : trees) {
			assert v instanceof IConstructor;
			IConstructor tree = (IConstructor) v;
			while(TreeAdapter.isAmb(tree)) {
				tree = (IConstructor) TreeAdapter.getAlternatives(tree).iterator().next();
			}

			if(TreeAdapter.isLayout(tree)) {
				final String chars = TreeAdapter.yield(tree);
				if(!chars.equals("")) {
					final Matcher m = LAYOUT_PAT.matcher(chars);
					if(m.matches()) {
						splitSpaces(m.group(1), cst);
						cst.append(comment(m.group(2)));
						splitSpaces(m.group(3), cst);
					}
					else {
						splitSpaces(chars, cst);
					}
				}
			}
			else if(TreeAdapter.isLiteral(tree)) {
				cst.append(token(TreeAdapter.yield(tree)));
			}
			else {
				final IValue child = implode(tree);
				if(child != null) {
					ast.add(child);
					cst.append(child(i++));
				}
			}
		}

		return new Pair<IValue[], IList>(ast.toArray(new IValue[ast.size()]), cst.done());
	}


	private static IConstructor check(IConstructor ret) {
		// if(!ret.getType().isSubtypeOf(Type_AST))
		// throw new ImplementationError("Bad AST type: " + ret.getType());
		return ret;
	}


	private static void splitSpaces(String chars, final IListWriter cst) {
		Matcher m = PAT_SPACE.matcher(chars);
		while(m.matches()) {
			if(m.group(1).length() > 0) {
				cst.append(space(m.group(1)));
			}
			cst.append(space(m.group(2)));
			chars = m.group(3);
			m = PAT_SPACE.matcher(chars);
		}

		if(chars.length() > 0) {
			cst.append(space(chars));
		}
	}
}
