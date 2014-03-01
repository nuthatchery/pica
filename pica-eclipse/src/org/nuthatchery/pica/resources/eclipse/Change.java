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
package org.nuthatchery.pica.resources.eclipse;

import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.errors.ImplementationError;

class Change {
	final Change.Kind kind;
	final URI uri;
	final IResource resource;


	Change(URI uri, IResource resource, Change.Kind kind) {
		this.uri = uri;
		this.kind = kind;
		this.resource = resource;

	}


	public IResource getResource() {
		return resource;
	}


	public URI getURI() {
		return uri;
	}


	enum Kind {
		ADDED, REMOVED, CHANGED
	}
}
