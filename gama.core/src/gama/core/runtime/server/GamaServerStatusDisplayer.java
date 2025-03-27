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
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;

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

	@Override
	public void informStatus(final String string, final String icon) {
		if (!canSendMessage(GAMA.getExperimentAgent())) return;
		sendMessage(GAMA.getExperimentAgent(), "{" + "\"message\": \"" + string + "\"" + "}",
				GamaServerMessage.Type.SimulationStatusInform);
	}

	/**
	 * Error status.
	 *
	 * @param error
	 *            the error
	 */
	@Override
	public void errorStatus(final GamaRuntimeException error) {
		if (!canSendMessage(GAMA.getExperimentAgent())) return;
		sendMessage(GAMA.getExperimentAgent(), "{" + "\"message\": \"" + error.getMessage() + "\"" + "}",
				GamaServerMessage.Type.SimulationStatusError);
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
	@Override
	public void setStatus(final String msg, final String icon, final GamaColor color) {
		if (!canSendMessage(GAMA.getExperimentAgent())) return;
		sendMessage(GAMA.getExperimentAgent(), json.object("message", msg, "color", color, "icon", icon).toString(),
				GamaServerMessage.Type.SimulationStatus);
	}

	/**
	 * Update experiment status.
	 */
	@Override
	public void updateExperimentStatus() {
		if (!canSendMessage(GAMA.getExperimentAgent())) return;
		sendMessage(GAMA.getExperimentAgent(), json.object("message", null, "icon", "overlays/status.clock").toString(),
				GamaServerMessage.Type.SimulationStatusInform);
	}

}