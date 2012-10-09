package org.magnolialang.resources;

import java.net.URI;
import java.util.List;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.magnolialang.resources.internal.GenericFact;
import org.magnolialang.resources.internal.GenericListFact;
import org.magnolialang.resources.internal.IFact;
import org.magnolialang.resources.internal.ValueFact;

public class FactFactory {
	private final IResourceManager	manager;
	private final URI				uri;


	public FactFactory(IResourceManager manager, URI uri) {
		this.manager = manager;
		this.uri = uri;
	}


	public static FactFactory getFactory(IResourceManager manager, URI uri) {
		return new FactFactory(manager, uri);
	}


	public <T> IFact<T> makeFact(String name, ISerializer<T> io) {
		return new GenericFact<T>(name, manager, uri, io);
	}


	public <T> IFact<List<T>> makeListFact(String name, ISerializer<T> io) {
		return new GenericListFact<T>(name, manager, uri, io);
	}


	public IFact<IValue> makeIValueFact(String name, Type type) {
		return new ValueFact<IValue>(name, manager, uri, type);
	}


	public IFact<IValue> makeIValueFact(String name) {
		return new ValueFact<IValue>(name, manager, uri, null);
	}


	public IFact<IRelation> makeIRelationFact(String name) {
		return new ValueFact<IRelation>(name, manager, uri, null);
	}


	public IFact<IConstructor> makeIConstructorFact(String name, Type type) {
		return new ValueFact<IConstructor>(name, manager, uri, type);
	}


	public IFact<IConstructor> makeIConstructorFact(String name) {
		return new ValueFact<IConstructor>(name, manager, uri, null);
	}

}
