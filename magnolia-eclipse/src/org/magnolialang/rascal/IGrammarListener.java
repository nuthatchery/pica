package org.magnolialang.rascal;

import java.io.PrintWriter;
import java.net.URI;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.parser.gtd.IGTD;

public interface IGrammarListener {
	int REQUIRE_GRAMMAR = 0;
	int REQUIRE_PARSER = 1;


	/**
	 * @param name
	 *            The name of the language (e.g., "Magnolia");
	 * @param moduleName
	 *            The name of the grammar module (e.g.,
	 *            "org::magnolialang::syntax::Magnolia")
	 * @param uri
	 *            The URI of the grammar module
	 * @param grammar
	 *            The grammar
	 * @param parser
	 *            A parser class, or null if getRequires != REQUIRE_PARSER
	 * @param out
	 *            A print writer for user feedback
	 * @return An Eclipse Job to be scheduled, or null if the operation
	 *         completed synchroniously
	 */
	Job getJob(String name, String moduleName, URI uri, IConstructor grammar, Class<IGTD<IConstructor, IConstructor, ISourceLocation>> parser, PrintWriter out);


	/**
	 * @return REQUIRE_GRAMMAR or REQUIRE_PARSER, depending on whether the job
	 *         requires just grammar information, or also a generated parser.
	 */
	int getRequires();
}
