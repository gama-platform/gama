/*******************************************************************************************************
 *
 * GamaServerStatusDisplayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.server;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.runtime.SimulationContext;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.ui.IStatusDisplayer;

/**
 * The Class GamaServerStatusDisplayer.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 2 nov. 2023
 */
public final class GamaServerStatusDisplayer extends GamaServerMessager implements IStatusDisplayer {

	/**
	 * Can send message.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param exp
	 *            the exp
	 * @return true, if successful
	 * @date 2 nov. 2023
	 */
	@Override
	public boolean canSendMessage(final IExperimentAgent exp) {
		if (exp == null) return false;
		var scope = exp.getScope();
		return scope != null && scope.getServerConfiguration() != null && scope.getServerConfiguration().hasStatus();
	}

	private static IExperimentAgent currentExperiment() {
		final var simulation = SimulationContext.current();
		return simulation == null ? GAMA.getExperimentAgent() : simulation.getExperiment();
	}

	@Override
	public void informStatus(final String message, final String icon) {
		final var exp = currentExperiment();
		if (exp != null) { informStatus(exp.getScope(), message, icon); }
	}

	@Override
	public void errorStatus(final GamaRuntimeException error) {
		final var scope = error.getScope();
		if (scope != null) { errorStatus(scope, error); }
	}

	@Override
	public void beginTask(final String name, final String icon) { informStatus(name, icon); }

	@Override
	public void endTask(final String name, final String icon) { informStatus(name, icon); }

	@Override
	public void setTaskCompletion(final String name, final Double completion) {
		informStatus(name + " [" + completion + "]", null);
	}

	@Override
	public void setStatus(final String message, final String icon, final IColor color) {
		final var exp = currentExperiment();
		if (exp == null) return;
		forwardMessage(exp.getScope(), GAMA.getJsonEncoder().object("message", message, "icon", icon, "color", color)
				.toString(), MessageType.SimulationStatusInform);
	}

	/**
	 * Inform status.
	 *
	 * @param scope
	 *            the scope
	 * @param string
	 *            the string
	 */
	public void informStatus(final IScope scope, final String string) {
		informStatus(scope, string, null);
	}

	/**
	 * Error status.
	 *
	 * @param scope
	 *            the scope
	 * @param error
	 *            the error
	 */
	public void errorStatus(final IScope scope, final Exception error) {
		forwardMessage(scope, GAMA.getJsonEncoder().object("message", error.getMessage()).toString(),
				MessageType.SimulationStatusError);
	}

	/**
	 * Sets the status.
	 *
	 * @param scope
	 *            the scope
	 * @param msg
	 *            the msg
	 * @param icon
	 *            the icon
	 * @param color
	 *            the color
	 */
	// @Override
	// public void setStatus(final IScope scope, final String msg, final GamaColor color) {
	// if (!canSendMessage(scope.getExperiment())) return;
	// sendMessage(scope.getExperiment(), json.object("message", msg, "color", color).toString(),
	// MessageType.SimulationStatus);
	// }

	/**
	 * Update experiment status.
	 */
	@Override
	public void updateExperimentStatus() {
		final var experiment = currentExperiment();
		if (!canSendMessage(experiment)) return;
		sendMessage(experiment,
				GAMA.getJsonEncoder().object("message", null, "icon", "overlays/status.clock").toString(),
				MessageType.SimulationStatusInform);
	}

	/**
	 * Inform status.
	 *
	 * @param scope
	 *            the scope
	 * @param message
	 *            the message
	 * @param icon
	 *            the icon
	 */
	public void informStatus(final IScope scope, final String message, final String icon) {
		forwardMessage(scope, GAMA.getJsonEncoder().object("message", message, "icon", icon).toString(),
				MessageType.SimulationStatusInform);
	}

	/**
	 * Sets the status.
	 *
	 * @param scope
	 *            the scope
	 * @param msg
	 *            the msg
	 * @param icon
	 *            the icon
	 */
	// @Override
	// public void setStatus(final IScope scope, final String msg, final String icon) {
	// if (!canSendMessage(scope.getExperiment())) return;
	// sendMessage(scope.getExperiment(), json.object("message", msg, "icon", icon).toString(),
	// MessageType.SimulationStatus);
	//
	// }

	/**
	 * Neutral status.
	 *
	 * @param scope
	 *            the scope
	 * @param string
	 *            the string
	 */
	// @Override
	// public void neutralStatus(final IScope scope, final String string) {
	// if (!canSendMessage(scope.getExperiment())) return;
	// sendMessage(scope.getExperiment(), json.object("message", string).toString(),
	// MessageType.SimulationStatusNeutral);
	// }
}