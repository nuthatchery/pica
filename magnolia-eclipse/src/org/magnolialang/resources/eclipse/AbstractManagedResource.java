package org.magnolialang.resources.eclipse;

import java.net.URI;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.magnolialang.resources.IManagedResource;

public abstract class AbstractManagedResource implements IManagedResource {
	protected final URI	uri;


	protected AbstractManagedResource(URI uri) {
		this.uri = uri;
	}


	@Override
	public <T> T accept(IValueVisitor<T> v) throws VisitorException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Type getType() {
		return IManagedResource.ResourceType;
	}


	@Override
	public URI getURI() {
		return uri;
	}


	@Override
	public boolean isEqual(IValue other) {
		return this == other;
	}


	@Override
	public String toString() {
		return uri.toString();
	}
}
