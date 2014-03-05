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
package org.nuthatchery.pica.resources;

import java.util.Collection;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.jdt.annotation.Nullable;
import org.nuthatchery.pica.resources.storage.IStorage;
import org.nuthatchery.pica.util.ISignature;
import org.nuthatchery.pica.util.Pair;
import org.rascalmpl.interpreter.IRascalMonitor;

public interface IManagedCodeUnit extends IManagedResource {

	/**
	 * Get the raw AST of a code unit.
	 * 
	 * The returned AST will not have been annotated with types, etc.
	 * 
	 * @param rm
	 *            A progress monitor
	 * @return An AST.
	 */
	IConstructor getAST(IRascalMonitor rm);


	/**
	 * Find dependencies.
	 * 
	 * Will return dependencies at the same level in the hierarchy. Note that
	 * this method may return an estimation of the dependencies, and that the
	 * actual dependency set may be smaller than the returned set.
	 * 
	 * @param rm
	 *            A progress monitor
	 * @return All code units this code unit depends on.
	 */
	Collection<? extends IManagedCodeUnit> getDepends(IRascalMonitor rm);


	/**
	 * Return a unique ID for the code unit.
	 * 
	 * The ID will typically be a fully-qualified name, but it need not be
	 * suitable for human use. Uniquess is guaranteed across the language.
	 * 
	 * @return The unique name/id of this code unit.
	 */
	IConstructor getId();


	/**
	 * 
	 * @param rm
	 *            A monitor
	 * @return Language-dependent indicator of what kind of code unit this is
	 *         (e.g., class, function, etc)
	 */
	String getKind(IRascalMonitor rm);


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
	 * code of this package.
	 * 
	 * The return value must not be modified.
	 * 
	 * @param rm
	 *            A monitor
	 * @return A hash of the source code
	 */
	ISignature getSourceSignature(IRascalMonitor rm);


	/**
	 * @return The storage where this code unit may save persistent data
	 */
	@Nullable
	IStorage getStorage();


	/**
	 * @param rm
	 *            A progress monitor
	 * @return The transitive closure of getDepends().
	 */
	Collection<? extends IManagedCodeUnit> getTransitiveDepends(IRascalMonitor rm);


	/**
	 * Return cross-references and hover help info for the code unit.
	 * 
	 * The returned information will include xrefs from all children.
	 * 
	 * @param rm
	 *            A progress monitor
	 * @return A mapping from source locations to Ref values
	 * @see org.magnolialang.magnolia.MagnoliaFacts#Type_Ref
	 */
	Collection<Pair<ISourceLocation, IConstructor>> getXRefs(IRascalMonitor rm);


	@Nullable
	IXRefInfo getXRefs(ISourceLocation loc, IRascalMonitor rm);


	/**
	 * Called by the resource manager when a dependency of this code unit is
	 * changed.
	 * 
	 * Should only do a minimal amount of work (e.g., marking cached data as
	 * invalid) and then return.
	 */
	void onDependencyChanged();

}
