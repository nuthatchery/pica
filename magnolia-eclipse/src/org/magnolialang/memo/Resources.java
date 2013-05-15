package org.magnolialang.memo;

import org.eclipse.imp.pdb.facts.IExternalValue;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.ExternalType;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.magnolialang.resources.IResourceManager;

public class Resources implements IExternalValue {
	public static final Type ResourceType = new ExternalType() {

		@Override
		protected Type glbWithExternal(Type type) {
			if(type == this)
				return this;
			else
				return TypeFactory.getInstance().voidType();
		}


		@Override
		protected boolean isSubtypeOfExternal(Type type) {
			return false;
		}


		@Override
		protected Type lubWithExternal(Type type) {
			if(type == this)
				return this;
			else
				return TypeFactory.getInstance().valueType();
		}
	};
	private final IResourceManager manager;


	public Resources(IResourceManager manager) {
		this.manager = manager;
	}


	@Override
	public <T> T accept(IValueVisitor<T> v) throws VisitorException {
		return null;
	}


	public IResourceManager getManager() {
		return manager;
	}


	@Override
	public Type getType() {
		return ResourceType;
	}


	@Override
	public boolean isEqual(IValue other) {
		return other == this;
	}


	@Override
	public String toString() {
		return manager.toString();
	}
}
