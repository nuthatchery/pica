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
package org.nuthatchery.pica.resources.internal.facts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.io.BinaryValueReader;
import org.eclipse.imp.pdb.facts.io.BinaryValueWriter;
import org.eclipse.imp.pdb.facts.type.Type;
import org.nuthatchery.pica.errors.UnexpectedFactTypeError;
import org.nuthatchery.pica.resources.internal.storage.IStoreUnit;
import org.nuthatchery.pica.resources.internal.storage.StoreUnit;
import org.nuthatchery.pica.resources.storage.IStorage;
import org.nuthatchery.pica.terms.TermFactory;
import org.nuthatchery.pica.util.ISignature;

public class ValueFact<T extends IValue> extends Fact<T> {

	private final Type type;


	public ValueFact(String name, IStorage storage, Type type) {
		super(name, storage);
		this.type = type;
	}


	public ValueFact(String name, Type type) {
		super(name);
		this.type = type;
	}


	@Override
	public T setValue(T newValue, ISignature newSignature) {
		if(type != null && newValue != null && !newValue.getType().isSubtypeOf(type)) {
			throw new UnexpectedFactTypeError(factName, type, newValue.getType());
		}
		return super.setValue(newValue, newSignature);
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


	static class ValueStoreUnit<T extends IValue> extends StoreUnit<T> {
		private IValue val;


		public ValueStoreUnit() {
			super();
			this.val = null;
		}


		public ValueStoreUnit(T val, ISignature signature) {
			super(signature);
			this.val = val;
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

	}
}