package org.magnolialang.util.syntax.generators;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.pdb.facts.ISet;
import org.magnolialang.Config;
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
		try {
			long lastMod = Config.getResolverRegistry().lastModified(uri);
			URI outFile = new URI(uri.getScheme(), uri.getHost(), uri.getPath()
					.substring(0, uri.getPath().lastIndexOf('.')) + "AST.rsc",
					null);
			if(Config.getResolverRegistry().lastModified(outFile) >= lastMod)
				return null;
		}
		catch(IOException e1) {
		}
		catch(URISyntaxException e) {
		}

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
