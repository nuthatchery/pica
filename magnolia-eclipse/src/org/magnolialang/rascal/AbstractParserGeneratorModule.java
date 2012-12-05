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
