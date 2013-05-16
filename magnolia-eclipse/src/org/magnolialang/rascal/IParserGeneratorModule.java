/**************************************************************************
 * Copyright (c) 2012 Tero Hasu
 * Copyright (c) 2012 University of Bergen
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
 * * Tero Hasu
 * 
 *************************************************************************/
package org.magnolialang.rascal;

import java.net.URI;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.parser.gtd.IGTD;
import org.rascalmpl.parser.gtd.result.action.IActionExecutor;

public interface IParserGeneratorModule {

	public abstract void clearParserFiles();


	public abstract IActionExecutor<IConstructor> getActionExecutor();


	public abstract IConstructor getGrammar();


	/**
	 * @return fully qualified name of the module
	 */
	public abstract String getModuleName();


	public abstract URI getModuleURI();


	/**
	 * @return unqualified name of the module
	 */
	public abstract String getName();


	public abstract IGTD<IConstructor, IConstructor, ISourceLocation> getParser();

}
