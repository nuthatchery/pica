package org.magnolialang.memo;

import java.util.Map;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.rascalmpl.interpreter.IEvaluator;
import org.rascalmpl.interpreter.IRascalMonitor;
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
	public <T> T accept(IValueVisitor<T> v) throws VisitorException {
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
	public Result<IValue> call(IRascalMonitor monitor, Type[] argTypes, IValue[] argValues, Map<String, IValue> keyArgValues) {
		return memoContext.callWithMemo(monitor, callable, argTypes, argValues, keyArgValues);
	}


	@Override
	public boolean equals(Object other) {
		if(other instanceof CallableMemo)
			return callable.equals(((CallableMemo) other).callable);
		else
			return callable.equals(other);
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
	public boolean hasKeywordArgs() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean hasVarArgs() {
		return callable.hasVarArgs();
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
	public String toString() {
		return callable.toString();
	}

}
