/*******************************************************************************************************
 *
 * ITestAgent.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.tests;

import gama.api.kernel.simulation.IExperimentAgent;

/**
 *
 */
public interface ITestAgent extends IExperimentAgent, WithTestSummary<TestExperimentSummary> {

}
