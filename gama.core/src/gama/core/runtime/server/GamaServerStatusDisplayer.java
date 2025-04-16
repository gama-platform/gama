/*******************************************************************************************************
 *
 * GamaServerStatusDisplayer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime.server;

import gama.core.common.interfaces.IStatusDisplayer;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;

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
		return scope != null && scope.getServerConfiguration().status();
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
		if (!canSendMessage(scope.getExperiment())) {}
		sendMessage(scope.getExperiment(), "{" + "\"message\": \"" + string + "\"" + "}",
				MessageType.SimulationStatusInform);
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
		if (!canSendMessage(scope.getExperiment())) return;
		sendMessage(scope.getExperiment(), "{" + "\"message\": \"" + error.getMessage() + "\"" + "}",
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
		if (!canSendMessage(GAMA.getExperimentAgent())) return;
		sendMessage(GAMA.getExperimentAgent(), json.object("message", null, "icon", "overlays/status.clock").toString(),
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
		if (!canSendMessage(scope.getExperiment())) return;
		sendMessage(scope.getExperiment(), json.object("message", message, "icon", icon).toString(),
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