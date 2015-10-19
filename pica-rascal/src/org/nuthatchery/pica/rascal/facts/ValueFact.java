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
package org.nuthatchery.pica.rascal.facts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.io.BinaryValueReader;
import org.eclipse.imp.pdb.facts.io.BinaryValueWriter;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.rascal.errors.UnexpectedFactTypeError;
import org.nuthatchery.pica.resources.internal.facts.Fact;
import org.nuthatchery.pica.resources.storage.IStorage;
import org.nuthatchery.pica.resources.storage.IStoreUnit;
import org.nuthatchery.pica.resources.storage.StoreUnit;
import org.nuthatchery.pica.terms.TermFactory;
import org.nuthatchery.pica.util.ISignature;

public class ValueFact<T extends IValue> extends Fact<T> {
	static class ValueStoreUnit<T extends IValue> extends StoreUnit<T> {
		@Nullable
		private IValue val;


		public ValueStoreUnit() {
			this(null, null);
		}


		public ValueStoreUnit(@Nullable T val, @Nullable ISignature signature) {
			super(signature);
			this.val = val;
		}


		@Override
		@Nullable
		public byte[] getData() {
			if(val != null) {
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
			else
				return null;
		}


		@SuppressWarnings("unchecked")
		@Override
		@Nullable
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


	@Nullable
	private final Type type;


	public ValueFact(String name, IStorage storage, @Nullable Type type) {
		super(name, storage);
		this.type = type;
	}


	public ValueFact(String name, @Nullable Type type) {
		super(name);
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
	@Nullable
	protected IStoreUnit<T> loadHelper(IStorage s) throws IOException {
		return s.get(factName, new ValueStoreUnit<T>());
	}


	@Override
	protected void saveHelper(T val, IStorage s) {
		ValueStoreUnit<T> unit = new ValueStoreUnit<T>(val, signature);
		s.put(factName, unit);
	}


	@Override
	@Nullable
	public T setValue(@Nullable T newValue, @Nullable ISignature newSignature) {
		Type t = type;
		if(t != null && newValue != null && !newValue.getType().isSubtypeOf(t)) {
			throw new UnexpectedFactTypeError(factName, t, newValue.getType());
		}
		return super.setValue(newValue, newSignature);
	}
}
