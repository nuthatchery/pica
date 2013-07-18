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
package org.nuthatchery.pica.memo;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.rascalmpl.interpreter.IRascalMonitor;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.result.Result;

public class MemoContext {
	private final Map<ICallableValue, SoftHashTable<MemoKey, MemoResult>> cache = new WeakHashMap<ICallableValue, SoftHashTable<MemoKey, MemoResult>>();


	public Result<IValue> callWithMemo(IRascalMonitor monitor, ICallableValue fun, IValue[] argValues, Map<String, IValue> keyArgValues) {
		Type[] argTypes = new Type[argValues.length];
		for(int i = 0; i < argValues.length; i++) {
			argTypes[i] = argValues[i].getType();
		}

		return callWithMemo(monitor, fun, argTypes, argValues, keyArgValues);
	}


	public Result<IValue> callWithMemo(IRascalMonitor monitor, ICallableValue fun, Type[] argTypes, IValue[] argValues, Map<String, IValue> keyArgValues) {
		SoftHashTable<MemoKey, MemoResult> funEntry = cache.get(fun);
		MemoKey key = new MemoKey(argValues);
		if(funEntry != null) {
			MemoResult e = funEntry.get(key);
			if(e != null)
				//System.err.println("MEMO CACHE HIT! " + fun.toString());
				return e.result;
		}

		Result<IValue> result = fun.call(monitor, argTypes, argValues, keyArgValues);

		if(funEntry == null) {
			funEntry = new SoftHashTable<MemoKey, MemoResult>();
		}
		funEntry.put(key, new MemoResult(result));
		cache.put(fun, funEntry);

		return result;
	}
}

class MemoKey {
	IValue[] argValues;


	public MemoKey(IValue[] argValues) {
		this.argValues = argValues;
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
		for(int i = 0; i < argValues.length; i++) {
			if(!argValues[i].isEqual(other.argValues[i]))
				return false;
		}
		return true;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(argValues);
		return result;
	}
}

class MemoResult {
	Result<IValue> result;


	public MemoResult(Result<IValue> result) {
		this.result = result;
	}
}
