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
import org.rascalmpl.parser.gtd.result.action.VoidActionExecutor;
import org.rascalmpl.uri.URIUtil;

public abstract class AbstractParserGeneratorModule implements IParserGeneratorModule {
	protected final String moduleName;

	protected final String name;
	protected final URI moduleURI;

	protected IConstructor grammar = null;

	protected Class<IGTD<IConstructor, IConstructor, ISourceLocation>> parserClass = null;

	protected static final String parserPackageName = "org.rascalmpl.java.parser.object";


	AbstractParserGeneratorModule(String moduleName) {
		this.moduleName = moduleName;
		this.moduleURI = URIUtil.createRascalModule(moduleName);

		if(moduleName.contains("::")) {
			this.name = moduleName.substring(moduleName.lastIndexOf(':') + 1, moduleName.length());
		}
		else {
			this.name = moduleName;
		}
	}


	/* (non-Javadoc)
	 * @see org.magnolialang.rascal.IParserGeneratorModule#getActionExecutor()
	 */
	@Override
	public IActionExecutor<IConstructor> getActionExecutor() {
		return new VoidActionExecutor<IConstructor>();
	}


	/* (non-Javadoc)
	 * @see org.magnolialang.rascal.IParserGeneratorModule#getGrammar()
	 */
	@Override
	public IConstructor getGrammar() {
		return grammar;
	}


	/* (non-Javadoc)
	 * @see org.magnolialang.rascal.IParserGeneratorModule#getModuleName()
	 */
	@Override
	public String getModuleName() {
		return moduleName;
	}


	/* (non-Javadoc)
	 * @see org.magnolialang.rascal.IParserGeneratorModule#getModuleURI()
	 */
	@Override
	public URI getModuleURI() {
		return moduleURI;
	}


	/* (non-Javadoc)
	 * @see org.magnolialang.rascal.IParserGeneratorModule#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

}
