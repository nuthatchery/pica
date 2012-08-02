package org.magnolialang.memo;

import java.io.IOException;
import java.net.URI;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.magnolialang.load.ModuleParser;
import org.magnolialang.magnolia.Magnolia;
import org.magnolialang.resources.ILanguage;
import org.magnolialang.resources.IManagedFile;
import org.magnolialang.resources.IManagedResource;
import org.magnolialang.resources.IModuleManager;
import org.magnolialang.resources.LanguageRegistry;
import org.magnolialang.resources.ResourceManager;
import org.magnolialang.terms.TermFactory;
import org.magnolialang.terms.TermImploder;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;
import org.rascalmpl.values.ValueFactoryFactory;

public class RscResources {
	private final IValueFactory	vf;


	public RscResources() {
		this(ValueFactoryFactory.getValueFactory());
	}


	public RscResources(IValueFactory vf) {
		this.vf = vf;
	}


	public IValue getModuleLoc(IConstructor moduleName, IValue resources, IEvaluatorContext ctx) {
		IModuleManager manager = manager(resources, ctx);
		IManagedResource module = manager.findModule(LanguageRegistry.getLanguage("Magnolia"), moduleName);
		URI uri = module.getURI();

		return vf.sourceLocation(uri);
	}


	public IValue getModuleName(ISourceLocation moduleLoc, IValue resources, IEvaluatorContext ctx) {
		IModuleManager manager = manager(resources, ctx);
		URI uri = moduleLoc.getURI();
		ctx.getStdErr().println(uri);
		ctx.getStdErr().println(manager.getPath(uri));
		return manager.getModuleId(manager.getPath(uri));
	}


	public IValue getModuleAST(IConstructor moduleId, IValue resources, IEvaluatorContext ctx) {
		IModuleManager manager = manager(resources, ctx);
		if(moduleId.getType().isSubtypeOf(TermFactory.Type_AST)) {
			IManagedResource moduleRes = manager.findModule(LanguageRegistry.getLanguage("Magnolia"), moduleId);
			if(moduleRes instanceof IManagedFile) {
				IManagedFile moduleFile = (IManagedFile) moduleRes;
				ModuleParser parser = moduleFile.getLanguage().getParser();
				IConstructor pt;
				try {
					pt = parser.parseModule(moduleFile.getURI(), moduleFile.getContentsCharArray());
					IConstructor ast = TermImploder.implodeTree(pt);
					return ast;
				}
				catch(IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		else
			throw new org.rascalmpl.interpreter.staticErrors.UnexpectedTypeError(TermFactory.Type_AST, moduleId.getType(), ctx.getCurrentAST());
		return null;
	}


	public IValue getProjectResources(IString name, IEvaluatorContext ctx) {
		IModuleManager manager = ResourceManager.getManager(name.getValue());
		return new Resources(manager);
	}


	public void initializeMagnolia(IEvaluatorContext ctx) {
		if(LanguageRegistry.getLanguage("Magnolia") == null) {
			ILanguage magnolia = new Magnolia();
			LanguageRegistry.registerLanguage(magnolia);
		}
	}


	protected static IModuleManager manager(IValue res, IEvaluatorContext ctx) {
		if(res instanceof Resources)
			return ((Resources) res).getManager();
		else
			throw RuntimeExceptionFactory.illegalArgument(res, ctx.getCurrentAST(), ctx.getStackTrace(), "is not a resources handle");
	}

}
