/**************************************************************************
 * Copyright (c) 2010-2012 Anya Helene Bagge
 * Copyright (c) 2010-2012 University of Bergen
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
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
package org.magnolialang.terms.skins;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;
import org.magnolialang.nullness.Nullable;

public interface ILanguageSkin {
	@Nullable
	IList getConcrete(IConstructor cons, @Nullable IValue context);


	@Nullable
	IList getConcrete(String cons, int arity, @Nullable IValue context);


	@Nullable
	IConstructor getListSep(String sort, @Nullable IValue context);


	boolean isVertical(IConstructor cons, @Nullable IValue context);


	boolean isVertical(String cons, int arity, @Nullable IValue context);
}
