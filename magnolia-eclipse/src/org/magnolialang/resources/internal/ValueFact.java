package org.magnolialang.resources.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.io.BinaryValueReader;
import org.eclipse.imp.pdb.facts.io.BinaryValueWriter;
import org.eclipse.imp.pdb.facts.type.Type;
import org.magnolialang.errors.UnexpectedFactTypeError;
import org.magnolialang.resources.storage.IStorage;
import org.magnolialang.terms.TermFactory;
import org.magnolialang.util.ISignature;

public class ValueFact<T extends IValue> extends Fact<T> {

	private final Type	type;


	public ValueFact(String name, Type type) {
		super(name);
		this.type = type;
	}


	public ValueFact(String name, IStorage storage, Type type) {
		super(name, storage);
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
		return storage.get(factName, new ValueStoreUnit<T>());
	}


	@Override
	protected void saveHelper(T val) {
		ValueStoreUnit<T> unit = new ValueStoreUnit<T>(val, signature);
		storage.put(factName, unit);
	}


	@Override
	public T setValue(T newValue, ISignature newSignature) {
		if(type != null && newValue != null && !newValue.getType().isSubtypeOf(type))
			throw new UnexpectedFactTypeError(factName, type, newValue.getType());
		return super.setValue(newValue, newSignature);
	}


	static class ValueStoreUnit<T extends IValue> extends StoreUnit<T> {
		private IValue	val;


		public ValueStoreUnit() {
			super();
			this.val = null;
		}


		public ValueStoreUnit(T val, ISignature signature) {
			super(signature);
			this.val = val;
		}


		@Override
		public void setData(byte[] bytes) {
			BinaryValueReader reader = new BinaryValueReader();
			try {
				val = reader.read(TermFactory.vf, TermFactory.ts, null, new ByteArrayInputStream(bytes));
			}
			catch(IOException e) {
				// TODO: Values reader is buggy, so ignore exceptions for now
				// e.printStackTrace();
			}
		}


		@Override
		public byte[] getData() {
			BinaryValueWriter writer = new BinaryValueWriter();
			ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
			try {
				writer.write(val, stream);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			return stream.toByteArray();
		}


		@SuppressWarnings("unchecked")
		@Override
		public T getValue() {
			return (T) val;
		}

	}
}
