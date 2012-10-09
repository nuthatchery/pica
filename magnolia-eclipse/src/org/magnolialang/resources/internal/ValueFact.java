package org.magnolialang.resources.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.io.BinaryValueReader;
import org.eclipse.imp.pdb.facts.io.BinaryValueWriter;
import org.eclipse.imp.pdb.facts.type.Type;
import org.magnolialang.errors.UnexpectedFactTypeError;
import org.magnolialang.resources.IResourceManager;
import org.magnolialang.terms.TermFactory;
import org.magnolialang.util.ISignature;

public class ValueFact<T extends IValue> extends Fact<T> {

	private final Type	type;


	public ValueFact(String name, Type type) {
		super(name);
		this.type = type;
	}


	public ValueFact(String name, IResourceManager manager, URI uri, Type type) {
		super(name, manager, uri);
		this.type = type;
	}


	/**
	 * @return
	 * @throws IOException
	 * @throws UnexpectedFactTypeError
	 *             if an entry for this fact was found, but it was of the wrong
	 *             type
	 */
	@Override
	protected IStoreUnit<T> loadHelper() throws IOException {
		return storage.get(factName, new StoreUnit<T>());
	}


	@Override
	protected void saveHelper(T val) {
		StoreUnit<T> unit = new StoreUnit<T>(val, signature);
		storage.put(factName, unit);
	}


	@Override
	public T setValue(T newValue, ISignature newSignature) {
		if(type != null && !newValue.getType().isSubtypeOf(type))
			throw new UnexpectedFactTypeError(factName, type, newValue.getType());
		return super.setValue(newValue, newSignature);
	}


	static class StoreUnit<T extends IValue> implements IStoreUnit<T> {
		private IValue		val;
		private ISignature	signature;


		public StoreUnit() {
			this.val = null;
			this.signature = null;
		}


		public StoreUnit(T val, ISignature signature) {
			this.val = val;
			this.signature = signature;
		}


		@Override
		public void writeValue(OutputStream stream) throws IOException {
			BinaryValueWriter writer = new BinaryValueWriter();
			writer.write(val, stream);
			signature.writeTo(stream);
		}


		@Override
		public void readValue(InputStream stream) throws IOException {
			BinaryValueReader reader = new BinaryValueReader();
			val = reader.read(TermFactory.vf, TermFactory.ts, null, stream);
			signature = signature.readFrom(stream);
		}


		@SuppressWarnings("unchecked")
		@Override
		public T getValue() {
			return (T) val;
		}


		@Override
		public ISignature getSignature() {
			return signature;
		}
	}
}
