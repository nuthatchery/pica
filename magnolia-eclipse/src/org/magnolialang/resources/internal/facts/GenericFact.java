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
package org.magnolialang.resources.internal.facts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.magnolialang.resources.ISerializer;
import org.magnolialang.resources.internal.storage.IStoreUnit;
import org.magnolialang.resources.internal.storage.StoreUnit;
import org.magnolialang.resources.storage.IStorage;
import org.magnolialang.util.ISignature;

public class GenericFact<T> extends Fact<T> {

	private final ISerializer<T> io;


	public GenericFact(String name, ISerializer<T> io) {
		super(name);
		this.io = io;
	}


	public GenericFact(String name, IStorage storage, ISerializer<T> io) {
		super(name, storage);
		this.io = io;
	}


	@Override
	protected IStoreUnit<T> loadHelper() throws IOException {
		return storage.get(factName, new GenericStoreUnit<T>(io));
	}


	@Override
	protected void saveHelper(T val) {
		GenericStoreUnit<T> unit = new GenericStoreUnit<T>(val, signature, io);
		storage.put(factName, unit);
	}


	static class GenericStoreUnit<T> extends StoreUnit<T> {
		private final ISerializer<T> io;
		private T val;


		public GenericStoreUnit(ISerializer<T> io) {
			super();
			this.val = null;
			this.io = io;
		}


		public GenericStoreUnit(T val, ISignature signature, ISerializer<T> io) {
			super(signature);
			this.val = val;
			this.io = io;
		}


		@Override
		public byte[] getData() {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			try {
				io.write(val, stream);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			return stream.toByteArray();
		}


		@Override
		public T getValue() {
			return val;
		}


		@Override
		public void setData(byte[] data) {
			try {
				val = io.read(new ByteArrayInputStream(data));
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

}
