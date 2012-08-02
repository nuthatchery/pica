package org.magnolialang.resources.internal;

import java.util.Collection;

import org.eclipse.core.resources.IContainer;
import org.eclipse.imp.pdb.facts.IValue;
import org.magnolialang.resources.ILanguage;
import org.magnolialang.resources.IManagedContainer;
import org.magnolialang.resources.IManagedResource;
import org.magnolialang.resources.ResourceManager;
import org.magnolialang.tasks.IFact;

public class ManagedContainer extends AbstractManagedResource implements IManagedContainer {

	public ManagedContainer(ResourceManager manager, IContainer resource) {
		super(manager, resource);
	}


	@Override
	public boolean isFile() {
		return false;
	}


	@Override
	public boolean isFolder() {
		return true;
	}


	@Override
	public void changed(IFact<?> f, Change c, Object moreInfo) {
		// TODO Auto-generated method stub

	}


	@Override
	public Collection<IManagedResource> getContents() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public IValue getValue() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean setValue(IValue val) {
		throw new UnsupportedOperationException("setValue on container");
	}


	@Override
	public boolean updateFrom(IFact<IValue> fact) {
		throw new UnsupportedOperationException("updateFrom on container");
	}


	@Override
	public ILanguage getLanguage() {
		throw new UnsupportedOperationException("getLanguage on container");
	}

}
