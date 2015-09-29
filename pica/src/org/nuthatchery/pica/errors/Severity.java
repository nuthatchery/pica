package org.nuthatchery.pica.errors;


public enum Severity {
	WARNING(1), ERROR(2), DEFAULT(2), INFO(0), NOTHING(-1);
	private int value;


	private Severity(int value) {
		this.value = value;
	}


	public int getValue() {
		return value;
	}
}
