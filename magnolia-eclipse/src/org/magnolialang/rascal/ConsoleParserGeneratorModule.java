package org.magnolialang.rascal;

import java.net.URI;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.magnolialang.errors.ImplementationError;
import org.magnolialang.infra.ConsoleInfra;
import org.magnolialang.infra.Infra;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.IRascalMonitor;
import org.rascalmpl.interpreter.utils.JavaBridge;
import org.rascalmpl.parser.gtd.IGTD;

public class ConsoleParserGeneratorModule extends AbstractParserGeneratorModule {

	public ConsoleParserGeneratorModule(String moduleName) {
		super(moduleName);
	}


	@Override
	public void clearParserFiles() {
		// TODO Auto-generated method stub

	}


	@Override
	public synchronized IGTD<IConstructor, IConstructor, ISourceLocation> getParser() {
		//System.err.println("ConsoleInfra.getParser for " + moduleName);
		if(parserClass == null) {
			System.err.println("generating parser for " + moduleName);
			String parserName = moduleName.replaceAll("::", ".");
			String normName = parserName.replaceAll("\\.", "_");
			final Evaluator evaluator = Infra.getEvaluatorFactory().makeEvaluator();
			final IValueFactory vf = evaluator.getValueFactory();
			final JavaBridge bridge = new JavaBridge(evaluator.getClassLoaders(), vf);
			final IRascalMonitor rm = ConsoleInfra.getInfra().getMonitor();
			evaluator.doImport(rm, "lang::rascal::grammar::ParserGenerator");
			evaluator.doImport(rm, "lang::rascal::grammar::definition::Modules");
			evaluator.doImport(rm, moduleName);
			URI uri = evaluator.getHeap().getModuleURI(moduleName);
			IMap prodmap = evaluator.getCurrentModuleEnvironment().getSyntaxDefinition();
			grammar = (IConstructor) evaluator.call(rm, "modules2grammar", vf.string(moduleName), prodmap);
			IString classString = (IString) evaluator.call(rm, "newGenerate", vf.string(parserPackageName), vf.string(normName), grammar);
			parserClass = bridge.compileJava(uri, parserPackageName + "." + normName, classString.getValue());
		}
		// We cannot store IGTD objects in our cache, as SGTDBF.parse may only be invoked _once_ per instance.
		// Hence we are storing classes, and creating a new instance every time.
		// If we stored IGTD objects we could only parse _one_ Magnolia module per runtime.
		try {
			return parserClass.newInstance();
		}
		catch(Exception e) {
			throw new ImplementationError("failed to create parser for " + moduleName, e);
		}
	}

}
