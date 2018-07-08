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
package org.nuthatchery.pica.rascal;

import java.util.List;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.type.Type;
import org.nuthatchery.pica.rascal.facts.ValueFact;
import org.nuthatchery.pica.resources.IFact;
import org.nuthatchery.pica.resources.ISerializer;
import org.nuthatchery.pica.resources.internal.facts.GenericFact;
import org.nuthatchery.pica.resources.internal.facts.GenericListFact;
import org.nuthatchery.pica.resources.storage.IStorage;

public class ValuesFactFactory {
	private final IStorage storage;

	public ValuesFactFactory(IStorage storage) {
		this.storage = storage;
	}


	public <T> IFact<T> makeFact(String name, ISerializer<T> io) {
		return new GenericFact<T>(name, storage, io);
	}


	public IFact<IConstructor> makeIConstructorFact(String name) {
		return new ValueFact<IConstructor>(name, storage, null);
	}


	public IFact<IConstructor> makeIConstructorFact(String name, Type type) {
		return new ValueFact<IConstructor>(name, storage, type);
	}


	public IFact<ISet> makeISetFact(String name) {
		return new ValueFact<ISet>(name, storage, null);
	}


	public IFact<IValue> makeIValueFact(String name) {
		return new ValueFact<IValue>(name, storage, null);
	}


	public IFact<IValue> makeIValueFact(String name, Type type) {
		return new ValueFact<IValue>(name, storage, type);
	}


	public <T> IFact<List<T>> makeListFact(String name, ISerializer<T> io) {
		return new GenericListFact<T>(name, storage, io);
	}


	public static ValuesFactFactory getFactory(IStorage storage) {
		return new ValuesFactFactory(storage);
	}

}
