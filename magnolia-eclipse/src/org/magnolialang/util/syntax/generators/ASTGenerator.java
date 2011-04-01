package org.magnolialang.util.syntax.generators;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.pdb.facts.ISet;
import org.magnolialang.rascal.IGrammarListener;
import org.rascalmpl.parser.gtd.IGTD;

public class ASTGenerator implements IGrammarListener {

	@Override
	public int getRequires() {
		return IGrammarListener.REQUIRE_GRAMMAR;
	}

	@Override
	public Job getJob(final String name, final String moduleName,
			final URI uri, final ISet productions, Class<IGTD> parser,
			PrintWriter out) {
		return new Job("Generating AST specification") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(name, IProgressMonitor.UNKNOWN);
				String astModule = "module " + moduleName + "AST\n"
						+ ASTGen.generateASTTypes(productions);
				String outFile = uri.getPath().substring(0,
						uri.getPath().lastIndexOf('.'))
						+ "AST.rsc";
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
				finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
	}
}
