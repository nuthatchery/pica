package org.magnolialang.memo;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.rascalmpl.interpreter.IRascalMonitor;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.result.Result;
import org.rascalmpl.values.ValueFactoryFactory;

public class MemoContext {
	private static IValueFactory											VF		= ValueFactoryFactory.getValueFactory();
	private static TypeFactory												TF		= TypeFactory.getInstance();
	private final Map<ICallableValue, SoftHashTable<MemoKey, MemoResult>>	cache	= new WeakHashMap<ICallableValue, SoftHashTable<MemoKey, MemoResult>>();


	public Result<IValue> callWithMemo(IRascalMonitor monitor, ICallableValue fun, IValue[] argValues) {
		Type[] argTypes = new Type[argValues.length];
		for(int i = 0; i < argValues.length; i++)
			argTypes[i] = argValues[i].getType();

		return callWithMemo(monitor, fun, argTypes, argValues);
	}


	public Result<IValue> callWithMemo(IRascalMonitor monitor, ICallableValue fun, Type[] argTypes, IValue[] argValues) {
		SoftHashTable<MemoKey, MemoResult> funEntry = cache.get(fun);
		MemoKey key = new MemoKey(argValues);
		if(funEntry != null) {
			MemoResult e = funEntry.get(key);
			if(e != null)
				return e.result;
		}

		Result<IValue> result = fun.call(monitor, argTypes, argValues);

		if(funEntry == null)
			funEntry = new SoftHashTable<MemoKey, MemoResult>();
		funEntry.put(key, new MemoResult(result));
		cache.put(fun, funEntry);

		return result;
	}
}

class MemoKey {
	IValue[]	argValues;


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(argValues);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		MemoKey other = (MemoKey) obj;
		if(argValues.length != other.argValues.length)
			return false;
		for(int i = 0; i < argValues.length; i++)
			if(!argValues[i].isEqual(other.argValues[i]))
				return false;
		return true;
	}


	public MemoKey(IValue[] argValues) {
		this.argValues = argValues;
	}
}

class MemoResult {
	Result<IValue>	result;


	public MemoResult(Result<IValue> result) {
		this.result = result;
	}
}
