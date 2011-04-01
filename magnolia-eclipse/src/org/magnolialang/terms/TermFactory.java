package org.magnolialang.terms;

import org.eclipse.imp.pdb.facts.*;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.pdb.facts.type.TypeStore;
import org.rascalmpl.values.ValueFactoryFactory;

public final class TermFactory {
	public static final TypeStore ts = new TypeStore(
			org.rascalmpl.values.uptr.Factory.getStore(),
			org.rascalmpl.values.errors.Factory.getStore(),
			org.rascalmpl.values.locations.Factory.getStore());
	public static final TypeFactory tf = TypeFactory.getInstance();
	public static final IValueFactory vf = ValueFactoryFactory
			.getValueFactory();
	public static final Type Type_XaToken = tf.abstractDataType(ts, "XaToken");
	public static final Type Type_AST = tf.abstractDataType(ts, "AST");
	public static final Type Type_ErrorMark = tf.abstractDataType(ts,
			"ErrorMark");
	public static final Type Cons_Leaf = tf.constructor(ts, Type_AST, "leaf",
			tf.stringType(), "strVal");
	public static final Type Cons_Seq = tf.constructor(ts, Type_AST, "seq",
			tf.listType(Type_AST), "args");
	public static final Type Cons_Var = tf.constructor(ts, Type_AST, "var",
			tf.stringType(), "name");
	public static final Type Cons_Token = tf.constructor(ts, Type_XaToken,
			"token", tf.stringType(), "chars");
	public static final Type Cons_Space = tf.constructor(ts, Type_XaToken,
			"space", tf.stringType(), "chars");
	public static final Type Cons_Comment = tf.constructor(ts, Type_XaToken,
			"comment", tf.stringType(), "chars");
	public static final Type Cons_Child = tf.constructor(ts, Type_XaToken,
			"child", tf.integerType(), "index");
	public static final Type Cons_CtxChild = tf.constructor(ts, Type_XaToken,
			"ctxchild", tf.integerType(), "index", tf.valueType(), "context");
	public static final Type Cons_Sep = tf.constructor(ts, Type_XaToken, "sep",
			Type_XaToken, "tok", tf.listType(Type_XaToken), "separator");
	public static final Type Cons_Mark = tf.constructor(ts, Type_ErrorMark,
			"mark", tf.stringType(), "severity", tf.stringType(), "message",
			tf.listType(tf.sourceLocationType()), "locs");

	public static IConstructor cons(final String name, final String sort,
			final IValue... args) {
		final Object[] childTypes = new Object[args.length * 2];
		for(int i = 0; i < args.length; i++) {
			childTypes[i * 2] = Type_AST;
			childTypes[i * 2 + 1] = "arg" + i;
		}
		final Type consType = tf.constructor(ts, Type_AST, name, childTypes);
		final IConstructor cons = vf.constructor(consType, args);
		return cons;
	}

	public static IConstructor leaf(final String strVal) {
		return vf.constructor(Cons_Leaf, vf.string(strVal));
	}

	public static IConstructor seq(final IList args) {
		return vf.constructor(Cons_Seq, args);
	}

	public static IConstructor seq(final IValue... args) {
		return vf.constructor(Cons_Seq, vf.list(args));
	}

	public static IConstructor token(final String chars) {
		return vf.constructor(Cons_Token, vf.string(chars));
	}

	public static IConstructor space(final String chars) {
		return vf.constructor(Cons_Space, vf.string(chars));
	}

	public static IConstructor comment(final String chars) {
		return vf.constructor(Cons_Comment, vf.string(chars));
	}

	public static IConstructor child(final int index) {
		return vf.constructor(Cons_Child, vf.integer(index));
	}

	public static IConstructor child(final int index, final IValue context) {
		return vf.constructor(Cons_CtxChild, vf.integer(index), context);
	}

	public static IConstructor sep(final IConstructor tok, final String chars) {
		return vf.constructor(Cons_Sep, tok, vf.string(chars));
	}

	public static IConstructor mark(final String message,
			final String severity, final ISourceLocation loc) {
		IList locs;
		if(loc != null)
			locs = vf.list(loc);
		else
			locs = vf.list(tf.sourceLocationType());

		return vf.constructor(Cons_Mark, vf.string(severity),
				vf.string(message), locs);
	}

	private TermFactory() {

	}
}
