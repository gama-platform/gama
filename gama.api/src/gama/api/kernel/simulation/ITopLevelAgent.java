/*******************************************************************************************************
 *
 * ITopLevelAgent.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.simulation;

import gama.api.data.objects.IColor;
import gama.api.gaml.expressions.IExpression;
import gama.api.kernel.agent.IMacroAgent;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.IScopedStepable;
import gama.api.ui.IOutputManager;
import gama.api.utils.random.IRandom;

/**
 * Class ITopLevelAgent Addition (Aug 2021): explicit inheritance of IScopedStepable
 *
 * @author drogoul
 * @since 27 janv. 2016
 *
 */
public interface ITopLevelAgent extends IMacroAgent, IScopedStepable {

	/**
	 * The Interface Platform.
	 */
	interface Platform extends ITopLevelAgent, IExpression {

		/** The Constant WORKSPACE_PATH. */
		String WORKSPACE_PATH = "workspace_path";

		/** The Constant MACHINE_TIME. */
		String MACHINE_TIME = "machine_time";

		/**
		 * Dispose.
		 */
		@Override
		default void dispose() {
			IExpression.super.dispose();
		}

		/**
		 *
		 */
		void restorePrefs();

		/**
		 * @param key
		 * @param value
		 */
		void savePrefToRestore(String key, Object value);
	}

	/**
	 * Gets the clock.
	 *
	 * @return the clock
	 */
	IClock getClock();

	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	IColor getColor();

	/**
	 * Gets the random generator.
	 *
	 * @return the random generator
	 */
	IRandom getRandomGenerator();

	/**
	 * Gets the output manager.
	 *
	 * @return the output manager
	 */
	IOutputManager getOutputManager();

	/**
	 * Post end action.
	 *
	 * @param executable
	 *            the executable
	 */
	void postEndAction(IExecutable executable);

	/**
	 * Post dispose action.
	 *
	 * @param executable
	 *            the executable
	 */
	void postDisposeAction(IExecutable executable);

	/**
	 * Post one shot action.
	 *
	 * @param executable
	 *            the executable
	 */
	void postOneShotAction(IExecutable executable);

	/**
	 * Execute action.
	 *
	 * @param executable
	 *            the executable
	 */
	void executeAction(IExecutable executable);

	/**
	 * Checks if is on user hold.
	 *
	 * @return true, if is on user hold
	 */
	boolean isOnUserHold();

	/**
	 * Sets the on user hold.
	 *
	 * @param state
	 *            the new on user hold
	 */
	void setOnUserHold(boolean state);

	/**
	 * Gets the experiment.
	 *
	 * @return the experiment
	 */
	IExperimentAgent getExperiment();

	/**
	 * Gets the family name. Means either 'simulation', 'experiment' or 'platform'
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the family name
	 * @date 13 août 2023
	 */
	String getFamilyName();

	/**
	 * Checks if is platform.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is platform
	 * @date 3 sept. 2023
	 */
	default boolean isPlatform() { return false; }

	/**
	 * Checks if is experiment.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is experiment
	 * @date 3 sept. 2023
	 */
	default boolean isExperiment() { return false; }

	/**
	 * Checks if is simulation.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is simulation
	 * @date 3 sept. 2023
	 */
	default boolean isSimulation() { return false; }

}
