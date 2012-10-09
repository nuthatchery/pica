package org.magnolialang.resources.internal;

import java.io.IOException;
import java.lang.ref.SoftReference;

import org.magnolialang.errors.UnexpectedFactTypeError;
import org.magnolialang.resources.storage.IStorage;
import org.magnolialang.util.ISignature;
import org.magnolialang.util.Pair;

public abstract class Fact<T> implements IFact<T> {
	protected ISignature		signature		= null;
	/**
	 * Note: be sure to keep a reference to the value stored in this soft
	 * reference for as long as it is needed, since calling get() may return
	 * null immediately after the reference is set.
	 */
	private SoftReference<T>	value			= null;
	protected final IStorage	storage;
	protected final String		factName;
	private boolean				loadAttempted	= false;


	public Fact(String name) {
		this.factName = name;
		this.storage = null;
	}


	public Fact(String name, IStorage storage) {
		this.storage = storage;
		this.factName = name;
	}


	public T load() {
		if(storage != null && !loadAttempted) {
			loadAttempted = true;
			IStoreUnit<T> unit;
			try {
				unit = loadHelper();
				if(unit != null) {
					signature = unit.getSignature();
					value = new SoftReference<T>(unit.getValue());
					return unit.getValue();
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
	protected abstract IStoreUnit<T> loadHelper() throws IOException;


	protected abstract void saveHelper(T val);


	@Override
	public T dispose() {
		T result = value.get();
		value.clear();
		return result;
	}


	@Override
	public Pair<T, ISignature> getValue() {
		T t = null;

		if(value != null)
			t = value.get();

		if(t == null) {
			load();
			if(value != null)
				t = value.get();
		}

		return new Pair<T, ISignature>(t, signature);
	}


	@Override
	public T getValue(ISignature sourceSignature) {
		if(value == null || signature == null)
			load();

		if(value == null || signature == null)
			return null;
		else if(signature.equals(sourceSignature)) {
			// byte[] foo = new byte[512 * 1024 * 1024];
			T t = value.get();
			if(t == null) {
				System.err.println("Signature matches, loading " + factName + " from disk");
				t = load();
				if(!signature.equals(sourceSignature))
					return null;
			}
			else
				System.err.println("Signature matches, fact " + factName + " available");
			return t;
		}
		else
			return null;
	}


	@Override
	public T setValue(T newValue, ISignature newSignature) {
		T old = value == null ? null : value.get();
		value = new SoftReference<T>(newValue);
		signature = newSignature;
		if(storage != null) {
			saveHelper(newValue);
			loadAttempted = false;
		}
		return old;
	}

}
