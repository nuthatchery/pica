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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.resources.ISerializer;
import org.nuthatchery.pica.resources.storage.IStorage;
import org.nuthatchery.pica.resources.storage.IStoreUnit;
import org.nuthatchery.pica.resources.storage.StoreUnit;
import org.nuthatchery.pica.util.ISignature;

public class GenericListFact<T> extends Fact<List<T>> {
	static class GenericListStoreUnit<T> extends StoreUnit<List<T>> {
		@Nullable
		private final List<T> val;
		private final ISerializer<T> io;


		public GenericListStoreUnit(ISerializer<T> io) {
			this(null, null, io);
		}


		public GenericListStoreUnit(@Nullable List<T> val, @Nullable ISignature signature, ISerializer<T> io) {
			super(signature);
			this.val = val;
			this.io = io;
		}


		@Override
		@Nullable
		public byte[] getData() {
			List<T> tmpVal = val;
			if(tmpVal != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				int size = tmpVal.size();
				stream.write(size & 0xff);
				stream.write(size >>> 8 & 0xff);
				stream.write(size >>> 16 & 0xff);
				stream.write(size >>> 24 & 0xff);
				for(T v : tmpVal) {
					assert v != null;
					try {
						io.write(v, stream);
					}
					catch(IOException e) {
						e.printStackTrace();
					}
				}
				return stream.toByteArray();
			}
			else {
				return null;
			}
		}


		@Override
		@Nullable
		public List<T> getValue() {
			return val;
		}


		@Override
		public void setData(byte[] bytes) {
			ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
			int size = stream.read();
			size = size << 8 | stream.read();
			size = size << 8 | stream.read();
			size = size << 8 | stream.read();
			/*val = new ArrayList<T>();
			for(int i = 0; i < size; i++)
				try {
					val.add(io.read(stream));
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			 */
		}
	}


	private final ISerializer<T> io;


	public GenericListFact(String name, IStorage storage, ISerializer<T> io) {
		super(name, storage);
		this.io = io;
	}


	@Override
	@Nullable
	protected IStoreUnit<List<T>> loadHelper(IStorage s) throws IOException {
		return s.get(factName, new GenericListStoreUnit<T>(io));
	}


	@Override
	protected void saveHelper(List<T> val, IStorage s) {
		GenericListStoreUnit<T> unit = new GenericListStoreUnit<T>(val, signature, io);
		s.put(factName, unit);
	}

}
