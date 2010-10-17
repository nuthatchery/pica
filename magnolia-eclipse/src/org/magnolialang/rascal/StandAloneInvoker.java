package org.magnolialang.rascal;

import sglr.LegacySGLRInvoker;
import sglr.SGLRInvoker;

public class StandAloneInvoker {

	public static RascalInterpreter getInterpreter() {
		String rascalPath = System.getenv("FRAGMENT");
		String metaxaPath = System.getenv("METAXA");
		String baseBinaryPath = rascalPath + "/installed/bin";
		String baseLibraryPath = rascalPath + "/installed/lib";

		System.setProperty("rascal.parsetable.default.file", rascalPath
				+ "/installed/share/rascal-grammar/rascal.tbl");
		System.setProperty("rascal.parsetable.header.file", rascalPath
				+ "/installed/share/rascal-grammar/rascal-header.tbl");
		System.setProperty("rascal.rascal2table.command", rascalPath
				+ "/installed/bin/rascal2table");
		System.setProperty("rascal.rascal2table.dir", rascalPath
				+ "/installed/bin");
		System.setProperty("rascal.sdf.library.dir", rascalPath
				+ "/installed/share/sdf-library/library");
		System.setProperty("rascal.parsetable.cache.dir", System
				.getProperty("java.io.tmpdir"));
		System.setProperty("rascal.path", metaxaPath + "/src");
		SGLRInvoker.setBaseLibraryPath(baseLibraryPath);
		LegacySGLRInvoker.setBaseBinaryPath(baseBinaryPath);

		return RascalInterpreter.getInstance();
	}

	private StandAloneInvoker() {

	}
}
