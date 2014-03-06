/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.umd.cs.findbugs.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation indicating that a FindBugs warning is expected.
 * 
 * See http://code.google.com/p/findbugs/wiki/FindbugsTestCases
 * 
 * @author David Hovemeyer
 */
@Retention(RetentionPolicy.CLASS)
public @interface ExpectWarning {
	/** Maximum value for user visible ranks (least relevant) */
	public static final int VISIBLE_RANK_MAX = 20;
	/** Minimum value for user visible ranks (most relevant) */
	public static final int VISIBLE_RANK_MIN = 1;


	/** Want a warning at this priority or higher */
	public Confidence confidence() default Confidence.LOW;


	/** Expect at least this many warnings */
	public int num() default 1;


	/** Expect a warning at least this scary */
	public int rank() default VISIBLE_RANK_MAX;


	/**
	 * The value indicates the bug code (e.g., NP) or bug pattern (e.g.,
	 * IL_INFINITE_LOOP) of the expected warning. Can be a comma-separated list.
	 */
	public String value();
}
