package org.nuthatchery.pica.rascal;

import java.net.URI;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.ISourceLocation;
import org.nuthatchery.pica.errors.Severity;
import org.nuthatchery.pica.resources.regions.CodeRegion;
import org.nuthatchery.pica.resources.regions.ICodeRegion;
import org.nuthatchery.pica.resources.regions.IOffsetLength;
import org.nuthatchery.pica.terms.TermFactory;

public class RascalUtil {

	public static Severity getSeverity(final IConstructor severity) {
		if(severity.getConstructorType().getName().equals("Error")) {
			return Severity.ERROR;
		}
		else if(severity.getConstructorType().getName().equals("Warning")) {
			return Severity.WARNING;
		}
		else if(severity.getConstructorType().getName().equals("Internal")) {
			return Severity.ERROR;
		}
		else if(severity.getConstructorType().getName().equals("Info")) {
			return Severity.INFO;
		}
		else {
			return Severity.ERROR;
		}
	}


	public static ICodeRegion<URI> toCodeRegion(ISourceLocation loc) {
		return new CodeRegion<URI>(loc.getURI(), loc.getOffset(), loc.getLength());
	}


	public static ISourceLocation toSourceLocation(ICodeRegion<URI> reg) {
		return TermFactory.vf.sourceLocation(reg.getBase(), (int) reg.getOffset(), (int) reg.getLength());
	}


	public static ISourceLocation toSourceLocation(URI uri, IOffsetLength ol) {
		return TermFactory.vf.sourceLocation(uri, (int) ol.getOffset(), (int) ol.getLength());
	}

}
