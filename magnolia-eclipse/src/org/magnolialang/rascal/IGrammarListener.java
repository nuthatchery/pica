package org.magnolialang.rascal;

import java.io.PrintWriter;
import java.net.URI;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.pdb.facts.ISet;
import org.rascalmpl.parser.gtd.IGTD;

public interface IGrammarListener {
	public final int REQUIRE_GRAMMAR = 0;
	public final int REQUIRE_PARSER = 1;

	/**
	 * @return REQUIRE_GRAMMAR or REQUIRE_PARSER, depending on whether the job
	 *         requires just grammar information, or also a generated parser.
	 */
	public int getRequires();

	/**
	 * @param name
	 *            The name of the language (e.g., "Magnolia");
	 * @param moduleName
	 *            The name of the grammar module (e.g.,
	 *            "org::magnolialang::syntax::Magnolia")
	 * @param uri
	 *            The URI of the grammar module
	 * @param productions
	 *            The set of productions in the grammar
	 * @param parser
	 *            A parser class, or null if getRequires != REQUIRE_PARSER
	 * @param out
	 *            A print writer for user feedback
	 * @return An Eclipse Job to be scheduled, or null if the operation
	 *         completed synchroniously
	 */
	public Job getJob(String name, String moduleName, URI uri,
			ISet productions, Class<IGTD> parser, PrintWriter out);
}