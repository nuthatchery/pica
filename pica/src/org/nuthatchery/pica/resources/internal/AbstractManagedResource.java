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

import org.nuthatchery.pica.resources.handles.IResourceHandle;
import org.nuthatchery.pica.resources.managed.IManagedContainer;
import org.nuthatchery.pica.resources.managed.IManagedResource;

public abstract class AbstractManagedResource implements IManagedResource {
	protected final URI uri;
	protected final IResourceHandle resource;


	protected AbstractManagedResource(URI uri, IResourceHandle resource) {
		this.uri = uri;
		this.resource = resource;
	}


	@Override
	public IResourceHandle getResource() {
		return resource;
	}


	@Override
	public URI getURI() {
		return uri;
	}


	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("ManagedResource(");
		b.append("uri=");
		b.append(getURI());
		b.append("resource=");
		b.append(resource);

		if(isCodeUnit()) {
			b.append(", codeUnit");
		}
		if(isFragment()) {
			b.append(", fragment");
		}
		if(this instanceof IManagedContainer) {
			b.append(", container");
		}
		b.append(")");
		return b.toString();
	}

}
