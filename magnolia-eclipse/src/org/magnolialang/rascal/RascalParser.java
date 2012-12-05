package org.magnolialang.rascal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.magnolialang.infra.Infra;
import org.rascalmpl.parser.gtd.IGTD;
import org.rascalmpl.parser.gtd.result.action.IActionExecutor;

/**
 * This class provides utility methods for accessing the Rascal parser
 * generator.
 * 
 * The class keeps a cache of generated parsers, and will regenerate parsers on
 * the fly if the grammar file is updated.
 * 
 * @author anya
 * 
 */
public final class RascalParser {
	private static final Map<String, IParserGeneratorModule> modules = new HashMap<String, IParserGeneratorModule>();
	private static Collection<IGrammarListener> listeners = new ArrayList<IGrammarListener>();


	public static void addGrammarListener(IGrammarListener listener) {
		listeners.add(listener);
	}


	/**
	 * Clear stored parser and associated files. Will force a regeneration of
	 * parser on next call to getParser().
	 * 
	 * This is needed if the Rascal runtime has changed and the generated parser
	 * is no longer compatible.
	 * 
	 * @param moduleName
	 *            Rascal module name for the grammar
	 */
	public static void clearParser(String moduleName) {
		getParserModule(moduleName).clearParserFiles();
	}


	/**
	 * Return an ActionExecutor for the given grammar module and parser,
	 * suitable for calling the parser's parse() method.
	 * 
	 * @param moduleName
	 *            Rascal module name for the grammar
	 * @param parser
	 *            A parser instance, returned by getParser()
	 * @return An ActionExecutor
	 */
	public static IActionExecutor<IConstructor> getActionExecutor(String moduleName, IGTD<IConstructor, IConstructor, ISourceLocation> parser) {
		return getParserModule(moduleName).getActionExecutor();
	}


	/**
	 * Get the set of productions from a grammar.
	 * 
	 * getParser() must be called first.
	 * 
	 * @param moduleName
	 *            Rascal module name for the grammar
	 * @return The set of grammar productions
	 */
	public static IConstructor getGrammar(String moduleName) {
		return getParserModule(moduleName).getGrammar();
	}


	public static Collection<IGrammarListener> getGrammarListeners(int require) {
		Collection<IGrammarListener> ls = new ArrayList<IGrammarListener>();
		for(IGrammarListener l : listeners)
			if(l.getRequires() == require) {
				ls.add(l);
			}
		return ls;
	}


	/**
	 * Get the URI of the file containing the grammar.
	 * 
	 * getParser() must be called first.
	 * 
	 * @param moduleName
	 *            Rascal module name for the grammar
	 * @return URI of the grammar, or null if a parser hasn't been generated
	 *         yet.
	 */
	public static URI getGrammarURI(String moduleName) {
		return getParserModule(moduleName).getModuleURI();
	}


	/**
	 * Return a parser for the given grammar. The grammar module must be in the
	 * Rascal interpreter's search path.
	 * 
	 * This method *may* take a long time, if the parser needs to be generated.
	 * 
	 * @param moduleName
	 *            Rascal module name for the grammar
	 * @return A new parser instance
	 */
	public static IGTD<IConstructor, IConstructor, ISourceLocation> getParser(String moduleName) {
		return getParserModule(moduleName).getParser();
	}


	private static synchronized IParserGeneratorModule getParserModule(String moduleName) {
		IParserGeneratorModule mod = modules.get(moduleName);
		if(mod == null) {
			mod = Infra.get().getParserGeneratorModule(moduleName);
			modules.put(moduleName, mod);
		}
		return mod;
	}


	public static void refresh() {
		modules.clear();
	}


	public static void removeGrammarListener(IGrammarListener listener) {
		listeners.remove(listener);
	}


	private RascalParser() {

	}

}
