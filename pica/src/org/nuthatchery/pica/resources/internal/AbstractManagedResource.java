/**************************************************************************
 * Copyright (c) 2011-2012 Anya Helene Bagge
 * Copyright (c) 2011-2012 University of Bergen
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
package org.nuthatchery.pica.resources.internal;

import java.net.URI;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.nuthatchery.pica.resources.IManagedResource;

public abstract class AbstractManagedResource implements IManagedResource {
	protected final URI uri;


	protected AbstractManagedResource(URI uri) {
		this.uri = uri;
	}


	@Override
	public <T, E extends Throwable> T accept(IValueVisitor<T, E> v) throws E {
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
