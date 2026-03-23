/*******************************************************************************************************
 *
 * TestExperimentSummary.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.tests;

import gama.api.utils.StringUtils;

/**
 * A summary of a whole test experiment
 *
 * @author drogoul
 *
 */
public class TestExperimentSummary extends CompoundSummary<IndividualTestSummary, ITestAgent> {

	/**
	 * Instantiates a new test experiment summary.
	 *
	 * @param testAgent
	 *            the test agent
	 */
	public TestExperimentSummary(final ITestAgent testAgent) {
		super(testAgent);
	}

	@Override
	protected void printFooter(final StringBuilder sb) {
		sb.append(StringUtils.LN);
		sb.append("----------------------------------------------------------------");
	}

	@Override
	protected void printHeader(final StringBuilder sb) {
		sb.append("----------------------------------------------------------------");
		sb.append(StringUtils.LN);
	}
}
