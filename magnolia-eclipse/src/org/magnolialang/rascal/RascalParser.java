package org.magnolialang.rascal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.pdb.facts.ISet;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.asserts.ImplementationError;
import org.rascalmpl.interpreter.env.Environment;
import org.rascalmpl.parser.IParserInfo;
import org.rascalmpl.parser.ParserGenerator;
import org.rascalmpl.parser.RascalActionExecutor;
import org.rascalmpl.parser.gtd.IGTD;
import org.rascalmpl.parser.gtd.result.action.IActionExecutor;

public class RascalParser {
	private static final Map<String, ParserModule> modules = new HashMap<String, ParserModule>();

	/**
	 * Return a parser for the given grammar. The grammar module must be in the
	 * Rascal interpreter's search path.
	 * 
	 * This method *may* take a long time, if the parser needs to be generated.
	 * 
	 * @param grammarModule
	 *            Rascal module name for the grammar
	 * @return A new parser instance
	 */
	public static IGTD getParser(String grammarModule) {
		ParserModule parser = modules.get(grammarModule);
		if(parser == null) {
			parser = new ParserModule(grammarModule);
			modules.put(grammarModule, parser);
		}
		return parser.getParser();
	}

	public static IActionExecutor getActionExecutor(String grammarModule,
			IGTD parser) {
		ParserModule module = modules.get(grammarModule);
		if(module == null) {
			module = new ParserModule(grammarModule);
			modules.put(grammarModule, module);
		}
		return module.getActionExecutor(parser);
	}

}

class ParserModule {
	private static ParserGenerator parserGenerator;

	private final Evaluator evaluator;

	private final String name;

	private Class<IGTD> parser = null;

	ParserModule(String grammarModule) {
		this.name = grammarModule;
		long currentTimeMillis = System.currentTimeMillis();
		this.evaluator = StandAloneInvoker.getInterpreter().getEvaluator(
				"import " + grammarModule + ";");
		System.out.printf("loaded %s in %d millis\n", grammarModule,
				System.currentTimeMillis() - currentTimeMillis);

	}

	public IGTD getParser() {
		if(parser == null) {
			Environment env = evaluator.getCurrentModuleEnvironment();
			ISet productions = env.getProductions();

			// see if Rascal has a cached parser for this set of productions
			// TODO: any point in this in the presence of multiple evaluators?
			parser = evaluator.getHeap().getObjectParser(name, productions);

			if(parser == null) {
				String parserName = name.replaceAll("::", ".");

				ParserGenerator pg = getParserGenerator();
				long currentTimeMillis = System.currentTimeMillis();
				parser = pg.getParser(env.getLocation(), parserName,
						productions);
				System.out.printf("generated parser for %s in %d millis\n",
						name, System.currentTimeMillis() - currentTimeMillis);
				evaluator.getHeap()
						.storeObjectParser(name, productions, parser);
			}
		}
		try {
			// should we return a new instance every time?
			return parser.newInstance();
		}
		catch(InstantiationException e) {
			throw new ImplementationError(e.getMessage(), e);
		}
		catch(IllegalAccessException e) {
			throw new ImplementationError(e.getMessage(), e);
		}

	}

	public IActionExecutor getActionExecutor(IGTD parser) {
		return new RascalActionExecutor(evaluator, (IParserInfo) parser);
	}

	private ParserGenerator getParserGenerator() {
		if(parserGenerator == null) {
			long currentTimeMillis = System.currentTimeMillis();
			parserGenerator = new ParserGenerator(evaluator.getStdErr(),
					evaluator.getClassLoaders(), evaluator.getValueFactory());
			System.out.printf("loaded parser generator in %d millis\n",
					System.currentTimeMillis() - currentTimeMillis);
		}
		return parserGenerator;
	}
}
