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
package org.nuthatchery.pica.resources.managed;

import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.tasks.ITaskMonitor;

public interface IManagedContainer extends IManagedResource {
	/**
	 * Find a given child of a package
	 *
	 * @param childId
	 *            The fully-qualified ID of the child
	 * @param rm
	 *            A monitor
	 * @return The child, or null if not found
	 */
	@Nullable
	IManagedResource getChild(Object childId, ITaskMonitor rm);


	Collection<? extends IManagedResource> getChildren(ITaskMonitor rm);

}
