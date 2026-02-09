/*******************************************************************************************************
 *
 * IndividualTestSummary.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.tests;

import gama.api.gaml.statements.IStatement;
import gama.api.utils.StringUtils;

/**
 * A summary of a test statement
 *
 * @author drogoul
 *
 */
public class IndividualTestSummary extends CompoundSummary<AssertionSummary, IStatement.Test> {

	/**
	 * Instantiates a new individual test summary.
	 *
	 * @param test
	 *            the test
	 */
	public IndividualTestSummary(final IStatement.Test test) {
		super(test);
	}

	@Override
	public int countTestsWith(final TestState state) {
		return getState() == state ? 1 : 0;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	protected void printFooter(final StringBuilder sb) {
		sb.append(StringUtils.LN);
	}

	@Override
	protected void printHeader(final StringBuilder sb) {
		sb.append(StringUtils.LN);
	}

}