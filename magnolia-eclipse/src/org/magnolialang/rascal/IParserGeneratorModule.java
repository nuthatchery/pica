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
