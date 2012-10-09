package org.magnolialang.resources;

import java.util.List;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.magnolialang.resources.internal.GenericFact;
import org.magnolialang.resources.internal.GenericListFact;
import org.magnolialang.resources.internal.IFact;
import org.magnolialang.resources.internal.ValueFact;
import org.magnolialang.resources.storage.IStorage;

public class FactFactory {
	private final IStorage	storage;


	public FactFactory(IStorage storage) {
		this.storage = storage;
	}


	public static FactFactory getFactory(IStorage storage) {
		return new FactFactory(storage);
	}


	public <T> IFact<T> makeFact(String name, ISerializer<T> io) {
		return new GenericFact<T>(name, storage, io);
	}


	public <T> IFact<List<T>> makeListFact(String name, ISerializer<T> io) {
		return new GenericListFact<T>(name, storage, io);
	}


	public IFact<IValue> makeIValueFact(String name, Type type) {
		return new ValueFact<IValue>(name, storage, type);
	}


	public IFact<IValue> makeIValueFact(String name) {
		return new ValueFact<IValue>(name, storage, null);
	}


	public IFact<IRelation> makeIRelationFact(String name) {
		return new ValueFact<IRelation>(name, storage, null);
	}


	public IFact<IConstructor> makeIConstructorFact(String name, Type type) {
		return new ValueFact<IConstructor>(name, storage, type);
	}


	public IFact<IConstructor> makeIConstructorFact(String name) {
		return new ValueFact<IConstructor>(name, storage, null);
	}

}
