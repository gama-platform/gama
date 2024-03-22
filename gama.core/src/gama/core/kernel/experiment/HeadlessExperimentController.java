/*******************************************************************************************************
 *
 * HeadlessExperimentController.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.experiment;

import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;

/**
 * The Class HeadlessExperimentController.
 */
public class HeadlessExperimentController implements IExperimentController {

	/** The experiment. */
	private final IExperimentPlan experiment;

	/** The agent. */
	private ExperimentAgent agent;

	/**
	 * Instantiates a new headless experiment controller.
	 *
	 * @param experiment
	 *            the experiment.
	 */
	public HeadlessExperimentController(final IExperimentPlan experiment) {
		this.experiment = experiment;
	}

	@Override
	public IExperimentPlan getExperiment() { return experiment; }

	@Override
	public void close() {
		experiment.dispose(); // will call own dispose() later
	}

	@Override
	public void schedule(final ExperimentAgent agent) {
		this.agent = agent;
		IScope scope = agent.getScope();
		try {
			if (!scope.init(agent).passed()) { scope.setDisposeStatus(); }
		} catch (final Throwable e) {
			if (!(e instanceof GamaRuntimeException)) {
				GAMA.reportError(scope, GamaRuntimeException.create(e, scope), true);
			}
		}

	}

	@Override
	public void dispose() {
		agent = null;
	}

	@Override
	public boolean processOpen(final boolean andWait) {
		return true;
	}

	@Override
	public boolean processPause(final boolean andWait) {
		return true;
	}

	@Override
	public boolean processReload(final boolean andWait) {
		return true;
	}

	@Override
	public boolean processStep(final boolean andWait) {
		return true;
	}

	@Override
	public boolean processBack(final boolean andWait) {
		return true;
	}

	@Override
	public boolean processStartPause(final boolean andWait) {
		return true;
	}

	@Override
	public boolean processStart(final boolean andWait) {
		if (agent == null) return false;
		IScope scope = agent.getScope();
		try {
			while (scope.step(agent).passed()) {}
		} catch (final Throwable e) {
			if (!(e instanceof GamaRuntimeException)) {
				GAMA.reportError(scope, GamaRuntimeException.create(e, scope), true);
			}
			return false;
		}
		return true;
	}
}
