package org.magnolialang.util.syntax.generators;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.io.PBFWriter;
import org.magnolialang.Config;
import org.magnolialang.rascal.IGrammarListener;
import org.magnolialang.rascal.RascalInterpreter;
import org.magnolialang.terms.TermFactory;
import org.rascalmpl.parser.gtd.IGTD;

public class GrammarInfoGenerator implements IGrammarListener {

	@Override
	public int getRequires() {
		return IGrammarListener.REQUIRE_GRAMMAR;
	}

	@Override
	public Job getJob(final String name, final String moduleName,
			final URI uri, final IConstructor grammar, Class<IGTD> parser,
			PrintWriter out) {
		try {
			long lastMod = Config.getResolverRegistry().lastModified(uri);
			URI infoFile = new URI(uri.getScheme(), uri.getHost(), getFileName(
					uri, "Info.pbf"), null);

			URI astFile = new URI(uri.getScheme(), uri.getHost(), getFileName(
					uri, "AST.rsc"), null);
			if(Config.getResolverRegistry().lastModified(infoFile) >= lastMod
					&& Config.getResolverRegistry().lastModified(astFile) >= lastMod)
				return null;
		}
		catch(IOException e1) {
		}
		catch(URISyntaxException e) {
		}

		return new Job("Generating Grammar info") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(name, IProgressMonitor.UNKNOWN);

				ITuple infoTuple = (ITuple) RascalInterpreter
						.getInstance()
						.call("grammar2info",
								"import org::magnolialang::util::syntax::generators::GrammarInfoGenerator;",
								grammar);

				try {
					saveGrammarInfo(infoTuple.get(0));
					saveAstModule(infoTuple.get(1));
				}
				finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}

			private void saveGrammarInfo(IValue info) {
				String outFile = getFileName(uri, "Info.pbf");

				try {
					OutputStream stream = new FileOutputStream(outFile);
					new PBFWriter().write(info, stream, TermFactory.ts);
					stream.close();
				}
				catch(FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch(IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			private void saveAstModule(IValue str) {
				String astModule = "module " + moduleName + "AST\n"
						+ ((IString) str).getValue();

				String outFile = getFileName(uri, "AST.rsc");

				try {
					PrintStream stream = new PrintStream(outFile, "UTF-8");
					stream.print(astModule);
					stream.close();
				}
				catch(FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch(UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		};

	}

	String getFileName(URI uri, String suffix) {
		return uri.getPath().substring(0, uri.getPath().lastIndexOf('.'))
				+ suffix;
	}

}
