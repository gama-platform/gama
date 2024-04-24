/*******************************************************************************************************
 *
 * Experiment.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.core;

import gama.core.kernel.experiment.ExperimentPlan;
import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.kernel.experiment.ParametersSet;
import gama.core.kernel.model.IModel;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.outputs.AbstractOutputManager;
import gama.core.outputs.IOutput;
import gama.core.outputs.MonitorOutput;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IList;
import gama.gaml.compilation.GAML;
import gama.gaml.expressions.IExpression;
import gama.headless.server.GamaServerExperimentJob;

/**
 * The Class Experiment.
 */
public class Experiment implements IExperiment {

	/** The Constant DEFAULT_SEED_VALUE. */
	public static final double DEFAULT_SEED_VALUE = 0;

	/** The current experiment. */
	protected IExperimentPlan currentExperiment = null;

	/** The params. */
	protected ParametersSet params = new ParametersSet();

	/** The model. */
	final protected IModel model;

	/** The experiment name. */
	protected String experimentName = null;

	/** The seed. */
	protected double seed = DEFAULT_SEED_VALUE;

	/** The current step. */
	protected long currentStep;

	/**
	 * Instantiates a new experiment.
	 *
	 * @param mdl
	 *            the mdl
	 */
	public Experiment(final IModel mdl) {
		this.model = mdl;
	}

	@Override
	public SimulationAgent getSimulation() {
		return currentExperiment == null ? null : currentExperiment.getCurrentSimulation();
	}

	/**
	 * Gets the scope.
	 *
	 * @return the scope
	 */
	protected IScope getScope() {
		SimulationAgent sim = getSimulation();
		return sim == null ? null : sim.getScope();
	}

	@Override
	public synchronized void setup(final String expName, final double sd) {
		this.seed = sd;
		this.loadCurrentExperiment(expName);
	}

	/**
	 * Setup.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param expName
	 *            the exp name
	 * @param sd
	 *            the sd
	 * @param params
	 *            the params
	 * @param ec
	 *            the ec
	 * @date 28 oct. 2023
	 */
	@Override
	public synchronized void setup(final String expName, final double sd, final IList params, final GamaServerExperimentJob ec) {
		this.seed = sd;
		this.loadCurrentExperiment(expName, params, ec);
	}

	/**
	 * Load current experiment.
	 *
	 * @param expName
	 *            the exp name
	 */
	@SuppressWarnings("rawtypes")
	private void loadCurrentExperiment(final String expName, final IList p, final GamaServerExperimentJob ec) {
		this.experimentName = expName;
		this.currentStep = 0;

		final ExperimentPlan curExperiment = (ExperimentPlan) model.getExperiment(expName);
		curExperiment.setHeadless(true);
		curExperiment.setController(ec.controller);
		curExperiment.setParameterValues(p);
		curExperiment.open(seed);
		if (!GAMA.getControllers().contains(curExperiment.getController())) {
			GAMA.getControllers().add(curExperiment.getController());
		}
		this.currentExperiment = curExperiment;
		this.currentExperiment.setHeadless(true);
	}

	/**
	 * Load current experiment.
	 *
	 * @param expName
	 *            the exp name
	 */
	private void loadCurrentExperiment(final String expName) {
		this.experimentName = expName;
		this.currentStep = 0;
		this.currentExperiment = GAMA.addHeadlessExperiment(model, experimentName, this.params, seed);
		this.currentExperiment.setHeadless(true);
	}

	@Override
	public long step() {
		currentExperiment.getAgent().step(currentExperiment.getAgent().getScope());
		return currentStep++;

	}

	@Override
	public long backStep() {
		currentExperiment.getAgent().backward(currentExperiment.getAgent().getScope());
		return currentStep--;

	}

	@Override
	public void setParameter(final String parameterName, final Object value) {
		this.params.put(parameterName, value);
	}

	@Override
	public Object getOutput(final String parameterName) {
		final IOutput output =
				((AbstractOutputManager) getSimulation().getOutputManager()).getOutputWithOriginalName(parameterName);
		if (output == null) throw GamaRuntimeException.error("Output does not exist: " + parameterName, getScope());
		if (!(output instanceof MonitorOutput))
			throw GamaRuntimeException.error("Output " + parameterName + " is not an alphanumeric data.", getScope());
		output.update();
		return ((MonitorOutput) output).getLastValue();
	}

	@Override
	public Object getVariableOutput(final String parameterName) {
		final Object res = getSimulation().getDirectVarValue(getScope(), parameterName);
		if (res == null) throw GamaRuntimeException.error("Output unresolved: " + parameterName, getScope());
		return res;
	}

	@Override
	public void dispose() {
		GAMA.closeExperiment(currentExperiment);
		currentExperiment = null;
	}

	@Override
	public boolean isInterrupted() {
		final SimulationAgent sim = currentExperiment.getCurrentSimulation();
		if (currentExperiment.isBatch() && sim == null) return false;
		return sim == null || sim.dead() || sim.getScope().interrupted();
	}

	@Override
	public IModel getModel() { return this.model; }

	@Override
	public IExperimentPlan getExperimentPlan() { return this.currentExperiment; }

	@Override
	public IExpression compileExpression(final String expression) {
		return GAML.compileExpression(expression, this.getSimulation(), false);
	}

	@Override
	public Object evaluateExpression(final IExpression exp) {
		return exp.value(this.getSimulation().getScope());
	}

	@Override
	public Object evaluateExpression(final String exp) {
		final IExpression localExpression = compileExpression(exp);
		return evaluateExpression(localExpression);
	}

}
