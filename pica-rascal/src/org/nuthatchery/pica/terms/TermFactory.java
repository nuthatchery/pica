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

import org.rascalmpl.value.IConstructor;
import org.rascalmpl.value.IList;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.IValueFactory;
import org.rascalmpl.value.type.Type;
import org.rascalmpl.value.type.TypeFactory;
import org.rascalmpl.value.type.TypeStore;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.rascalmpl.values.ValueFactoryFactory;

@NonNullByDefault
@SuppressWarnings("null")
public final class TermFactory {
	public static final TypeStore ts = new TypeStore(org.rascalmpl.values.uptr.RascalValueFactory.getStore());/// , org.rascalmpl.values.errors..Factory.getStore(), org.rascalmpl.values.locations.Factory.getStore());
	public static final TypeFactory tf = TypeFactory.getInstance();
	public static final IValueFactory vf = ValueFactoryFactory.getValueFactory();
	public static final Type Type_XaToken = tf.abstractDataType(ts, "XaToken");
	public static final Type Type_AST = tf.abstractDataType(ts, "AST");
	public static final Type Cons_Leaf = tf.constructor(ts, Type_AST, "leaf", tf.stringType(), "strVal");
	public static final Type Cons_Seq = tf.constructor(ts, Type_AST, "seq", tf.listType(Type_AST), "args");
	public static final Type Cons_Var = tf.constructor(ts, Type_AST, "var", tf.stringType(), "name");
	public static final Type Cons_Token = tf.constructor(ts, Type_XaToken, "token", tf.stringType(), "chars");
	public static final Type Cons_Space = tf.constructor(ts, Type_XaToken, "space", tf.stringType(), "chars");
	public static final Type Cons_Comment = tf.constructor(ts, Type_XaToken, "comment", tf.stringType(), "chars");
	public static final Type Cons_Child = tf.constructor(ts, Type_XaToken, "child", tf.integerType(), "index");
	public static final Type Cons_CtxChild = tf.constructor(ts, Type_XaToken, "ctxchild", tf.integerType(), "index", tf.valueType(), "context");
	public static final Type Cons_Sep = tf.constructor(ts, Type_XaToken, "sep", Type_XaToken, "tok", tf.listType(Type_XaToken), "separator");


	public static IConstructor child(final int index) {
		return vf.constructor(Cons_Child, vf.integer(index));
	}


	public static IConstructor child(final int index, final IValue context) {
		return vf.constructor(Cons_CtxChild, vf.integer(index), context);
	}


	public static IConstructor comment(final String chars) {
		return vf.constructor(Cons_Comment, vf.string(chars));
	}


	public static IConstructor cons(final String name, final IValue... args) {
		return vf.constructor(consType(name, args), args);
	}


	public static Type consType(final String name, final int numChildren) {
		Object[] childTypes = new Object[numChildren * 2];
		for(int i = 0; i < numChildren; i++) {
			childTypes[i * 2] = Type_AST;
			childTypes[i * 2 + 1] = "arg" + i;
		}
		Type consType = tf.constructor(ts, Type_AST, name, childTypes);
		return consType;
	}


	public static Type consType(final String name, final IValue[] args) {
		Object[] childTypes = new Object[args.length * 2];
		for(int i = 0; i < args.length; i++) {
			childTypes[i * 2] = Type_AST;
			childTypes[i * 2 + 1] = "arg" + i;
		}
		Type consType = tf.constructor(ts, Type_AST, name, childTypes);
		return consType;
	}


	public static IConstructor leaf(final String strVal) {
		return vf.constructor(Cons_Leaf, vf.string(strVal));
	}


	public static IConstructor sep(final IConstructor tok, final String chars) {
		return vf.constructor(Cons_Sep, tok, vf.string(chars));
	}


	public static IConstructor seq(final IList args) {
		return vf.constructor(Cons_Seq, args);
	}


	public static IConstructor seq(final IValue... args) {
		return vf.constructor(Cons_Seq, vf.list(args));
	}


	public static IConstructor space(final String chars) {
		return vf.constructor(Cons_Space, vf.string(chars));
	}


	public static IConstructor token(final String chars) {
		return vf.constructor(Cons_Token, vf.string(chars));
	}


	private TermFactory() {

	}
}
