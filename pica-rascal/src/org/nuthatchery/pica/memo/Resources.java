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
package org.nuthatchery.pica.memo;

import org.rascalmpl.value.IAnnotatable;
import org.rascalmpl.value.IExternalValue;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.IWithKeywordParameters;
import org.rascalmpl.value.type.ExternalType;
import org.rascalmpl.value.type.Type;
import org.rascalmpl.value.type.TypeFactory;
import org.rascalmpl.value.visitors.IValueVisitor;
import org.nuthatchery.pica.resources.IProjectManager;

public class Resources implements IExternalValue {
	public static final Type ResourceType = new ExternalType() {

		@Override
		protected Type glbWithExternal(Type type) {
			if(type == this) {
				return this;
			}
			else {
				return TypeFactory.getInstance().voidType();
			}
		}


		@Override
		protected boolean isSubtypeOfExternal(Type type) {
			return false;
		}


		@Override
		protected Type lubWithExternal(Type type) {
			if(type == this) {
				return this;
			}
			else {
				return TypeFactory.getInstance().valueType();
			}
		}
	};
	private final IProjectManager manager;


	public Resources(IProjectManager manager) {
		this.manager = manager;
	}


	@Override
	public <T, E extends Throwable> T accept(IValueVisitor<T, E> v) throws E {
		return null;
	}


	@Override
	public IAnnotatable<? extends IValue> asAnnotatable() {
		return null;
	}


	@Override
	public IWithKeywordParameters<? extends IValue> asWithKeywordParameters() {
		return null;
	}


	public IProjectManager getManager() {
		return manager;
	}


	@Override
	public Type getType() {
		return ResourceType;
	}


	@Override
	public boolean isAnnotatable() {
		return false;
	}


	@Override
	public boolean isEqual(IValue other) {
		return other == this;
	}


	@Override
	public boolean mayHaveKeywordParameters() {
		return false;
	}


	@Override
	public String toString() {
		return manager.toString();
	}
}
