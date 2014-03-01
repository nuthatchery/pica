/**************************************************************************
 * Copyright (c) 2012 Anya Helene Bagge
 * Copyright (c) 2012 University of Bergen
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
package org.nuthatchery.pica.resources.internal.facts;

import java.io.IOException;
import java.lang.ref.SoftReference;

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.errors.UnexpectedFactTypeError;
import org.nuthatchery.pica.resources.storage.IStorage;
import org.nuthatchery.pica.resources.storage.IStoreUnit;
import org.nuthatchery.pica.util.ISignature;
import org.nuthatchery.pica.util.Pair;

public abstract class Fact<T> implements IFact<T> {
	protected ISignature signature = null;
	/**
	 * Note: be sure to keep a reference to the value stored in this soft
	 * reference for as long as it is needed, since calling get() may return
	 * null immediately after the reference is set.
	 */
	private SoftReference<T> value = null;
	protected final IStorage storage;
	protected final String factName;
	private boolean loadAttempted = false;


	public Fact(String name) {
		this.factName = name;
		this.storage = null;
	}


	public Fact(String name, @Nullable IStorage storage) {
		this.storage = storage;
		this.factName = name;
		if(storage != null) {
			storage.declare(name);
		}
	}


	@Override
	@Nullable
	public T dispose() {
		T result = value.get();
		value.clear();
		return result;
	}


	@Override
	public Pair<T, ISignature> getValue() {
		T t = null;

		if(value != null) {
			t = value.get();
		}

		if(t == null) {
			load();
			if(value != null) {
				t = value.get();
			}
		}

		return new Pair<T, ISignature>(t, signature);
	}


	@Override
	@Nullable
	public T getValue(ISignature sourceSignature) {
		if(value == null || signature == null) {
			load();
		}

		if(value == null || signature == null) {
			return null;
		}
		else if(signature.equals(sourceSignature)) {
			// byte[] foo = new byte[512 * 1024 * 1024];
			T t = value.get();
			if(t == null) {
				System.err.println("Signature matches, loading " + factName + " from disk");
				t = load();
				if(!signature.equals(sourceSignature)) {
					return null;
				}
				System.err.println("OK");
			}
			//else
			//	System.err.println("Signature matches, fact " + factName + " available");
			return t;
		}
		else {
			return null;
		}
	}


	@Override
	@Nullable
	public T setValue(@Nullable T newValue, @Nullable ISignature newSignature) {
		T old = value == null ? null : value.get();
		if(newValue != null) {
			value = new SoftReference<T>(newValue);
			signature = newSignature;
			if(storage != null) {
				saveHelper(newValue);
				loadAttempted = false;
			}
		}
		else {
			value = null;
		}
		return old;
	}


	@Nullable
	protected T load() {
		if(storage != null && !loadAttempted) {
			loadAttempted = true;
			IStoreUnit<T> unit;
			try {
				unit = loadHelper();
				if(unit != null) {
					if(unit.getValue() != null) {
						signature = unit.getSignature();
						value = new SoftReference<T>(unit.getValue());
						loadAttempted = false;
						System.err.println("Successfully loaded fact " + factName + " from storage");
						return unit.getValue();
					}
					else {
						System.err.println("Failed to load fact " + factName + " from storage");
					}
				}
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}


	/**
	 * @throws IOException
	 * @throws UnexpectedFactTypeError
	 *             if an entry for this fact was found, but it was of the wrong
	 *             type
	 */
	@Nullable
	protected abstract IStoreUnit<T> loadHelper() throws IOException;


	protected abstract void saveHelper(T val);

}
