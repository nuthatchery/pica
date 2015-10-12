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
package org.nuthatchery.pica.memo;

import java.util.Map;

import org.eclipse.imp.pdb.facts.IAnnotatable;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IWithKeywordParameters;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.rascalmpl.debug.IRascalMonitor;
import org.rascalmpl.interpreter.IEvaluator;
import org.rascalmpl.interpreter.env.Environment;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.result.Result;

public class CallableMemo extends Result<IValue> implements ICallableValue {
	private final ICallableValue callable;
	private final MemoContext memoContext;


	public CallableMemo(ICallableValue fun, MemoContext ctx) {
		super(fun.getType(), null, fun.getEval());

		fun.getEval().getStdErr().println(fun.getClass().getCanonicalName());
		callable = fun;
		memoContext = ctx;
	}


	@Override
	public <T, E extends Throwable> T accept(IValueVisitor<T, E> v) throws E {
		return callable.accept(v);
	}


	/*@Override
	public Result<IValue> call(IRascalMonitor monitor, Type[] argTypes, IValue[] argValues, Map<String, Result<IValue>> keyArgValues) {
		return memoContext.callWithMemo(monitor, callable, argTypes, argValues, keyArgValues);
	}


	@Override
	public Result<IValue> call(Type[] argTypes, IValue[] argValues, Map<String, Result<IValue>> keyArgValues) {
		return memoContext.callWithMemo(new NullRascalMonitor(), callable, argTypes, argValues, keyArgValues);
	}*/

	@Override
	public IAnnotatable<? extends IValue> asAnnotatable() {
		return callable.asAnnotatable();
	}


	@Override
	public IWithKeywordParameters<? extends IValue> asWithKeywordParameters() {
		return null;
	}


	@Override
	public Result<IValue> call(IRascalMonitor monitor, Type[] argTypes, IValue[] argValues, Map<String, IValue> keyArgValues) {
		return memoContext.callWithMemo(monitor, callable, argTypes, argValues, keyArgValues);
	}


	@Override
	public ICallableValue cloneInto(Environment env) {
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean equals(Object other) {
		if(other instanceof CallableMemo) {
			return callable.equals(((CallableMemo) other).callable);
		}
		else {
			return callable.equals(other);
		}
	}


	@Override
	public int getArity() {
		return callable.getArity();
	}


	@Override
	public IEvaluator<Result<IValue>> getEval() {
		return callable.getEval();
	}


	public MemoContext getMemoContext() {
		return memoContext;
	}


	@Override
	public Type getType() {
		return callable.getType();
	}


	@Override
	public IValue getValue() {
		return this;
	}


	@Override
	public int hashCode() {
		return callable.hashCode();
	}


	@Override
	public boolean hasKeywordArguments() {
		return false;
	}


	@Override
	public boolean hasVarArgs() {
		return callable.hasVarArgs();
	}


	@Override
	public boolean isAnnotatable() {
		return callable.isAnnotatable();
	}


	@Override
	public boolean isEqual(IValue other) {
		return callable.isEqual(other);
	}


	@Override
	public boolean isStatic() {
		return callable.isStatic();
	}


	@Override
	public boolean mayHaveKeywordParameters() {
		return false;
	}


	@Override
	public String toString() {
		return callable.toString();
	}

}
