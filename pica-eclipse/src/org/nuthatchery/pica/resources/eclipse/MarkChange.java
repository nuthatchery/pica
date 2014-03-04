package org.nuthatchery.pica.resources.eclipse;

import org.nuthatchery.pica.resources.eclipse.Change.Kind;
import org.nuthatchery.pica.resources.marks.IMark;

public class MarkChange {
	private final IMark mark;
	private final Kind kind;


	public MarkChange(Kind kind, IMark mark) {
		this.mark = mark;
		this.kind = kind;
	}


	public Kind getKind() {
		return kind;
	}


	public IMark getMark() {
		return mark;
	}


	@Override
	public String toString() {
		return "MarkChange(" + kind.name() + ", " + mark.toString() + ")";
	}
}
