/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umd.cs.findbugs.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation indicating that a FindBugs warning is desired.
 * 
 * See http://code.google.com/p/findbugs/wiki/FindbugsTestCases
 * 
 * @author David Hovemeyer
 */
@Retention(RetentionPolicy.CLASS)
public @interface DesireWarning {
	/** Maximum value for user visible ranks (least relevant) */
	public static final int VISIBLE_RANK_MAX = 20;
	/** Minimum value for user visible ranks (most relevant) */
	public static final int VISIBLE_RANK_MIN = 1;


	/** Want a warning at this priority or higher */
	public Confidence confidence() default Confidence.LOW;


	/** Expect a warning at least this scary */
	public int rank() default VISIBLE_RANK_MAX;


	/**
	 * The value indicates the bug code (e.g., NP) or bug pattern (e.g.,
	 * IL_INFINITE_LOOP) of the desired warning
	 */
	public String value();

}
