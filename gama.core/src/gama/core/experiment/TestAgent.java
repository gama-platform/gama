/*******************************************************************************************************
 *
 * TestAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.experiment;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.emf.common.util.URI;

import gama.annotations.doc;
import gama.annotations.experiment;
import gama.annotations.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.IStatement;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.runtime.scope.IScope;
import gama.api.utils.tests.TestExperimentSummary;
import gama.api.utils.tests.WithTestSummary;
import gama.dev.DEBUG;

/**
 * The Class TestAgent.
 */
@experiment (IKeyword.TEST)
@doc ("Experiments supporting the collection of success or failure of tests. Can be used in GUI or headless")
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class TestAgent extends ExperimentAgent implements IExperimentAgent.Test {

	/** The summary. */
	// int failedModels = 0;
	TestExperimentSummary summary;

	/**
	 * Instantiates a new test agent.
	 *
	 * @param p
	 *            the p
	 * @param index
	 *            the index
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public TestAgent(final IPopulation p, final int index) throws GamaRuntimeException {
		super(p, index);
	}

	@Override
	public boolean init(final IScope scope) {
		super.init(scope);
		final TestExperimentSummary summary = getSummary();
		summary.reset();
		if (!summary.isEmpty()) { scope.getGui().openTestView(scope, false); }
		return true;
	}

	@Override
	public boolean step(final IScope scope) {
		DEBUG.LOG("Agent " + this + " doing step " + getClock().getCycle());
		super.step(scope);
		return true;
	}

	@Override
	public void schedule(final IScope scope) {
		scheduled = true;
		// scheduled = true;
		// The experiment agent is scheduled in the global scheduler
		// getSpecies().getController().schedule(this);
	}

	/**
	 * Gets the summary.
	 *
	 * @return the summary
	 */
	@Override
	public TestExperimentSummary getSummary() {
		if (summary == null) { summary = new TestExperimentSummary(this); }
		return summary;
	}

	/**
	 * Gets the title for summary.
	 *
	 * @return the title for summary
	 */
	@Override
	public String getTitleForSummary() {
		final String mn = getSpecies().getDescription().getModelDescription().getModelFilePath();
		final String modelName = mn.substring(mn.lastIndexOf('/') + 1).replace(".experiment", "").replace(".gaml", "");
		return getSpecies().getName() + " in " + modelName;
	}

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	@Override
	public URI getURI() { return getModel().getURI(); }

	/**
	 * Gets the sub elements.
	 *
	 * @return the sub elements
	 */
	@Override
	public Collection<? extends WithTestSummary> getSubElements() {
		final List tests = getModel().getAllTests();
		final Consumer<IStatement> filter = t -> { if (t instanceof WithTestSummary ts) { tests.add(ts); } };
		getSpecies().getBehaviors().forEach(filter);
		return tests;
	}

	@Override
	public boolean isGUI() { return false; }

	@Override
	public void closeSimulations(final boolean andLeaveExperimentPerspective) {
		// We interrupt the simulation scope directly (as it cannot be
		// interrupted by the global scheduler)
		if (getSimulation() != null) { getSimulation().getScope().setDisposeStatus(); }
	}

	@Override
	public void displayTestResults() {
		getScope().getGui().openTestView(getScope(), false);
		getScope().getGui().displayTestsResults(getScope(), getSummary());
		getScope().getGui().endTestDisplay();
	}

}
