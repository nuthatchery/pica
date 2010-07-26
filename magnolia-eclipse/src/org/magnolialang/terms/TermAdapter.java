package org.magnolialang.terms;

import static org.magnolialang.terms.TermFactory.*;

import java.util.Iterator;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IListWriter;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.visitors.NullVisitor;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.magnolialang.errors.ImplementationError;
import org.rascalmpl.values.ValueFactoryFactory;


public class TermAdapter {
	private static IValueFactory vf = ValueFactoryFactory.getValueFactory();

	public static IMap match(IValue pattern, IValue tree) {
		if(pattern instanceof IConstructor && tree instanceof IConstructor)
			return match((IConstructor)pattern, (IConstructor)tree, vf.map(Type_AST, Type_AST));
		else
			return null;
	}

	public static IMap match(IConstructor pattern, IConstructor tree) {
		return match(pattern, tree, vf.map(Type_AST, Type_AST));
	}
	public static IMap match(IConstructor pattern, IConstructor tree, IMap env) {
		if(env == null || pattern == null || tree == null)
			return null;
		else if(pattern == tree)
			return env;
		else if(isCons(pattern))
			return matchCons(pattern, tree, env);
		else if(isSeq(pattern))
			return matchSeq(pattern, tree, env);
		else if(isLeaf(pattern) && pattern.isEqual(tree))
			return env;
		else if(isVar(pattern)) {
			if(env.containsKey(pattern))
				return match((IConstructor)env.get(pattern), tree, env);
			else
				return env.put(pattern, tree);
		}

		return null;
	}

	public static IMap matchCons(IConstructor pattern, IConstructor tree, IMap env) {
		if(!pattern.get("name").equals(tree.get("name"))
				|| !pattern.get("sort").equals(tree.get("sort")))
			return null;

		IList pargs = (IList) pattern.get("args");
		IList targs = (IList) tree.get("args");
		if(pargs.length() != targs.length())
			return null;

		for(int i = 0; i < pargs.length(); i++)
			env = match((IConstructor)pargs.get(i), (IConstructor)targs.get(i), env);

		return env;
	}

	public static IMap matchSeq(IConstructor pattern, IConstructor tree, IMap env) {
		if(!pattern.get("sort").equals(tree.get("sort")))
			return null;

		IList pargs = (IList) pattern.get("args");
		IList targs = (IList) tree.get("args");
		if(pargs.length() != targs.length())
			return null;

		for(int i = 0; i < pargs.length(); i++)
			env = match((IConstructor)pargs.get(i), (IConstructor)targs.get(i), env);

		return env;
	}


	public static boolean isCons(IValue tree) {
		return tree instanceof IConstructor && isCons((IConstructor)tree);
	}

	public static boolean isCons(IConstructor tree) {
		Type constype = tree.getConstructorType();
		return constype != Cons_Seq && constype != Cons_Leaf && constype != Cons_Var;
	}

	public static boolean isCons(IValue tree, String name) {
		return tree instanceof IConstructor && isCons((IConstructor)tree, name);
	}
	public static boolean isCons(IConstructor tree, String name) {
		return tree.getName().equals(name);
	}

	public static boolean isCons(IValue tree, String name, int arity) {
		return tree instanceof IConstructor && isCons((IConstructor)tree, name, arity);
	}

	public static boolean isCons(IConstructor tree, String name, int arity) {
		return tree.getName().equals(name) && tree.arity() == arity;
	}

	public static boolean isSeq(IValue tree) {
		return tree instanceof IConstructor && isSeq((IConstructor)tree);
	}

	public static boolean isSeq(IConstructor tree) {
		return tree.getConstructorType() == Cons_Seq;
	}

	public static boolean isLeaf(IValue tree) {
		return tree instanceof IConstructor && isLeaf((IConstructor)tree);
	}

	public static boolean isLeaf(IConstructor tree) {
		return tree.getConstructorType() == Cons_Leaf;
	}

	public static boolean isLeaf(IValue tree, String chars) {
		return tree instanceof IConstructor && isLeaf((IConstructor)tree, chars);
	}

	public static boolean isLeaf(IConstructor tree, String chars) {
		return tree.getConstructorType() ==  Cons_Leaf
		&& ((IString)tree.get("strVal")).getValue().equals(chars);
	}

	public static boolean isVar(IValue tree) {
		return tree instanceof IConstructor && isVar((IConstructor)tree);
	}

	public static boolean isVar(IConstructor tree) {
		return tree.getConstructorType() == Cons_Var;
	}

	public static boolean isVar(IValue tree, String name) {
		return tree instanceof IConstructor && isVar((IConstructor)tree, name);
	}

	public static boolean isVar(IConstructor tree, String name) {
		return tree.getConstructorType() == Cons_Var
		&& ((IString)tree.get("name")).getValue().equals(name);
	}

	public static Iterable<IConstructor> getChildren(IConstructor tree) {
		if(isSeq(tree))
			return new IConstructorIterableWrapper((IList)tree.get("args"));
		else
			return new IConstructorIterableWrapper(tree.getChildren());

	}
	public static IConstructor getArg(IConstructor tree, int arg) {
		if(isSeq(tree))
			return (IConstructor) ((IList) tree.get("args")).get(arg);
		else if(isLeaf(tree) || isVar(tree))
			return tree;
		else
			return (IConstructor) tree.get(arg);
	}

	public static String getString(IConstructor tree) {
		if(isLeaf(tree))
			return ((IString)tree.get("strVal")).getValue();
		else
			return null;
	}

	public static boolean hasChildren(IConstructor tree) {
		return isSeq(tree) || isCons(tree);
	}

	/**
	 * The number of children of a constructor or list.
	 * @param tree
	 * @return Constructor arity, list length, or 0.
	 */
	public static int arity(IConstructor tree) {
		if(isSeq(tree))
			return ((IList) tree.get("args")).length();
		else if(isCons(tree))
			return tree.arity();
		else
			return 0;
	}

	public static String getName(IConstructor tree) {
		if(isVar(tree))
			return ((IString)tree.get("name")).getValue();
		else
			return tree.getName();
	}

	public static String getSort(IConstructor tree) {
		return null; // (IString) tree.get("sort");
	}
	/*
	public static boolean isGround(IConstructor tree) {
		try {
			return tree.accept(new NullVisitor<Boolean>() {
				public Boolean visitConstructor(IConstructor c) {
					if(isLeaf(c))
						return true;
					else if(isVar(c))
						return false;
					for(IValue child : (IList) c.get("args"))
						if(!isGround(child))
							return false;
					return true;
				}});
		} catch (VisitorException e) {
			return false;
		}
	}

	public static IList vars(IConstructor tree) {

		final IListWriter lw = vf.listWriter(Type_AST);

		try {
			tree.accept(new IdentityVisitor() {
				public IValue visitConstructor(IConstructor c) throws VisitorException {
					if(isVar(c))
						lw.append(c);
					else if(isCons(c) || isSeq(c))
						for(IValue child : (IList) c.get("args"))
							child.accept(this);
					return c;
				}});
		} catch (VisitorException e) {
			throw new ImplementationError("Visitor error", e);
		}

		return lw.done();
	}
	 */	
	public static String yield(IValue tree) {
		try {
			return tree.accept(new NullVisitor<String>() {
				public String visitConstructor(IConstructor c) throws VisitorException {
					IList concrete = (IList) c.getAnnotation("concrete");
					if(concrete == null || concrete.length() == 0) {
						if(isLeaf(c))
							return getString(c);
						else if(isVar(c))
							return "<" + getName(c) + ">";
						else {
							StringBuffer result = new StringBuffer();
							for(IConstructor child : getChildren(c))
								result.append(child.accept(this));
							return result.toString();
						}
					}
					//					Iterator<IConstructor> args;
					////					if(isLeaf(c) || isVar(c))
					//						args = vf.list(c.removeAnnotation("concrete")).iterator();
					//					else
					//						args = getArgs(c);

					StringBuffer result = new StringBuffer();
					for(IValue token : concrete) {
						Type type = ((IConstructor)token).getConstructorType();
						if(type == Cons_Token || type == Cons_Space || type == Cons_Comment) {
							result.append(((IString)((IConstructor)token).get("chars")).getValue());
						}
						else {
							int index = ((IInteger)((IConstructor)token).get("index")).intValue();
							result.append(getArg(c, index).accept(this));
						}
					}					
					return result.toString();
				}});
		} catch (VisitorException e) {
			return null;
		}
	}

	public static String yield(final IValue tree, final ILanguageSkin skin, final boolean fallback) {
		return yield(tree, skin, fallback, "");
	}

	public static String yield(final IValue tree, final ILanguageSkin skin, final boolean fallback, String nesting) {
		if(tree instanceof IConstructor) {
			IConstructor c = (IConstructor) tree;
			IList concrete = null;

			if(isCons(c)) {
				concrete = skin.getConcrete(getName(c), getSort(c), arity(c), null);
				if(concrete != null && skin.isVertical(getName(c), getSort(c), arity(c), null)) {
					concrete = concrete.insert(space("\n" + nesting)).append(space("\n" + nesting));
					nesting = nesting + "\t";
				}
			}
			else if(isSeq(c)) {
				IListWriter lw = vf.listWriter(Type_XaToken);
				IConstructor sep = skin.getListSep(getSort(c), null);
				if(sep != null) {
					for(int i = 0; i < arity(c); i++) {
						if(i > 0)
							lw.append(sep);
						lw.append(child(i));
					}
					concrete = lw.done();
				}
			}


			if(concrete == null && fallback)
				concrete = (IList) c.getAnnotation("concrete");

			if(concrete == null || concrete.length() == 0) {
				if(isLeaf(c))
					return ((IString)c.get("strVal")).getValue();
				else if(isVar(c))
					return "<" + ((IString)c.get("name")).getValue() + ">";
				else {
					StringBuffer result = new StringBuffer();
					for(IConstructor child : getChildren(c))
						result.append(yield(child, skin, fallback, nesting));
					return result.toString();
				}
			}

			StringBuffer result = new StringBuffer();
			for(IValue token : concrete) {
				Type type = ((IConstructor)token).getConstructorType();
				if(type == Cons_Token || type == Cons_Comment) {
					result.append(((IString)((IConstructor)token).get("chars")).getValue());
				}
				else if(type == Cons_Space)
					result.append(((IString)((IConstructor)token).get("chars")).getValue());
				else if(type == Cons_Child) {
					int index = ((IInteger)((IConstructor)token).get("index")).intValue();
					result.append(yield(getArg(c, index), skin, fallback, nesting));
				}
				else if(type == Cons_Sep) {
					IValue tok = ((IConstructor)token).get("tok");
					if(((IConstructor)tok).getConstructorType() == Cons_Child) {
						int index = ((IInteger)((IConstructor)tok).get("index")).intValue();
						result.append(yield(getArg(c, index), skin, fallback, nesting));
					}
				}
			}					
			return result.toString();
		}
		else if(tree instanceof IList) {
			StringBuffer result = new StringBuffer();
			for(IValue child : (IList)tree)
				result.append(yield(child, skin, fallback, nesting));
			return result.toString();
		}
		else {
			throw new ImplementationError("Yield not valid on type " + tree.getType());
		}

	}

}

class IConstructorIterableWrapper implements Iterable<IConstructor> {
	private Iterable<IValue> iterable;

	public IConstructorIterableWrapper(Iterable<IValue> iterable) {
		this.iterable = iterable;
	}
	public Iterator<IConstructor> iterator() {
		return new IConstructorIteratorWrapper(iterable);
	}		
}

class IConstructorIteratorWrapper implements Iterator<IConstructor> {
	private Iterator<IValue> iterator;

	public IConstructorIteratorWrapper(Iterable<IValue> iterable) {
		this.iterator = iterable.iterator();
	}

	public boolean hasNext() {
		return iterator.hasNext();
	}

	public IConstructor next() {
		try {
			return (IConstructor) iterator.next();
		}
		catch(ClassCastException e) {
			throw e;
		}
	}

	public void remove() {
		iterator.remove();
	}
}
