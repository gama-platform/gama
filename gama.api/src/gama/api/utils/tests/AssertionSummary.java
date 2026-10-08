/*******************************************************************************************************
 *
 * AssertionSummary.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.tests;

import java.util.Collections;
import java.util.Map;

import gama.api.utils.StringUtils;

/**
 * A summary of assert statements. Contrary to other summaries, they possess a state
 *
 * @author drogoul
 *
 */
public class AssertionSummary extends AbstractSummary<WithTestSummary> {

	/** The state. */
	private TestState state = TestState.NOT_RUN;

	/** The time stamp. */
	public final long timeStamp;

	/**
	 * Instantiates a new assertion summary.
	 *
	 * @param a
	 *            the a
	 */
	public AssertionSummary(final WithTestSummary a) {
		super(a);
		timeStamp = System.currentTimeMillis();
	}

	@Override
	public void setState(final TestState s) { state = s; }

	@Override
	public void reset() {
		super.reset();
		state = TestState.NOT_RUN;
	}

	@Override
	public TestState getState() { return state; }

	@Override
	protected void printFooter(final StringBuilder sb) {
		sb.append(StringUtils.LN);
	}

	@Override
	public Map<String, ? extends AbstractSummary<?>> getSummaries() { return Collections.EMPTY_MAP; }

	@Override
	public int countTestsWith(final TestState state) {
		return 0;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public long getTimeStamp() { return timeStamp; }

}