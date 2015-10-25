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
package org.nuthatchery.pica.rascal;

import org.rascalmpl.value.IValue;
import org.nuthatchery.pica.tasks.ITaskMonitor;

public interface ICompiler {
	IValue call(ITaskMonitor rm, String funName, IValue... args);


	IValue call(String funName, IValue... args);


	/**
	 * Wait until the entire compiler is loaded.
	 */
	void ensureInit();


	/**
	 * @return Name of the language this compiler compiles
	 */
	String getLanguage();


	/**
	 * Reload the compiler. May complete asynchroniously
	 */
	void refresh();

}
