package org.nuthatchery.pica.errors;

import org.eclipse.core.resources.IMarker;

public enum Severity {
	WARNING(IMarker.SEVERITY_WARNING), ERROR(IMarker.SEVERITY_ERROR), DEFAULT(IMarker.SEVERITY_ERROR), INFO(IMarker.SEVERITY_INFO), NOTHING(-1);
	private int value;


	private Severity(int value) {
		this.value = value;
	}


	public int getValue() {
		return value;
	}
}
