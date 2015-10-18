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
package org.nuthatchery.pica.resources.managed;

import java.util.Collection;

import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.resources.ILanguage;
import org.nuthatchery.pica.resources.IXRefInfo;
import org.nuthatchery.pica.resources.storage.IStorage;
import org.nuthatchery.pica.tasks.ITaskMonitor;
import org.nuthatchery.pica.util.ISignature;
import org.nuthatchery.pica.util.Pair;

/**
 * A code unit has a name/id, and may depend on other code units.
 *
 *
 * @author anya
 *
 * @param <Identifier>
 */
public interface IManagedCodeUnit extends IManagedResource {

	/**
	 * Find dependencies.
	 *
	 * Will return dependencies at the same level in the hierarchy. Note that
	 * this method may return an estimation of the dependencies, and that the
	 * actual dependency set may be smaller than the returned set.
	 *
	 * @param tm
	 *            A progress monitor
	 * @return All code units this code unit depends on.
	 */
	Collection<? extends IManagedCodeUnit> getDepends(ITaskMonitor tm);


	/**
	 * Get a byte array containing a hash that identifies the current source
	 * code version of this package and all its dependencies.
	 *
	 * The return value must not be modified.
	 *
	 * @param tm
	 *            A monitor
	 * @return A hash of the package and its dependencies
	 */
	ISignature getFullSignature(ITaskMonitor tm);


	/**
	 * Return a unique ID for the code unit.
	 *
	 * The ID will typically be a fully-qualified name, but it need not be
	 * suitable for human use. Uniquess is guaranteed across the language.
	 *
	 * @return The unique name/id of this code unit.
	 */
	Object getId();


	/**
	 *
	 * @param tm
	 *            A monitor
	 * @return Language-dependent indicator of what kind of code unit this is
	 *         (e.g., class, function, etc)
	 */
	String getKind(ITaskMonitor tm);


	/**
	 * @return The language associated with this code unit.
	 */
	ILanguage getLanguage();


	/**
	 * This may be just the pretty-printed result of getId(). The result is not
	 * guaranteed to be unique, but should be human-readable.
	 *
	 * @return The name/id of this code unit, as a string.
	 */
	String getName();


	/**
	 * Get a byte array containing a hash that identifies the current source
	 * code of this unit.
	 *
	 * The return value must not be modified.
	 *
	 * @param tm
	 *            A monitor
	 * @return A hash of the source code
	 */
	ISignature getSourceSignature(ITaskMonitor tm);


	/**
	 * @return The storage where this code unit may save persistent data
	 */
	@Nullable
	IStorage getStorage();


	/**
	 * @param tm
	 *            A progress monitor
	 * @return The transitive closure of getDepends().
	 */
	Collection<? extends IManagedCodeUnit> getTransitiveDepends(ITaskMonitor tm);


	@Nullable
	IXRefInfo getXRefs(ISourceLocation loc, ITaskMonitor tm);


	/**
	 * Return cross-references and hover help info for the code unit.
	 *
	 * The returned information will include xrefs from all children.
	 *
	 * @param tm
	 *            A progress monitor
	 * @return A mapping from source locations to Ref values
	 * @see org.magnolialang.magnolia.MagnoliaFacts#Type_Ref
	 */
	Collection<Pair<ISourceLocation, Object>> getXRefs(ITaskMonitor tm);


	/**
	 * @param tm
	 * @return True if this unit has dependencies which have not been resolved.
	 */
	boolean hasIncompleteDepends(ITaskMonitor tm);


	/**
	 * Called by the resource manager when a dependency of this code unit is
	 * changed.
	 *
	 * Should only do a minimal amount of work (e.g., marking cached data as
	 * invalid) and then return.
	 */
	void onDependencyChanged();

}
