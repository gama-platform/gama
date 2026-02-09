/*******************************************************************************************************
 *
 * GamaServerConsoleListener.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.server;

import gama.api.GAMA;
import gama.api.data.objects.IColor;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.ITopLevelAgent;
import gama.api.ui.IConsoleListener;
import gama.api.utils.prefs.GamaPreferences;

/**
 * The listener interface for receiving gamaServerConsole events.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @see GamaServerConsoleEvent
 * @date 2 nov. 2023
 */
public final class GamaServerConsoleListener extends GamaServerMessager implements IConsoleListener {

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
		if (!GamaPreferences.Runtime.CORE_SERVER_CONSOLE.getValue()) return false;
		var scope = exp.getScope();
		return scope != null && scope.getServerConfiguration().hasConsole();
	}

	@Override
	public void informConsole(final String s, final ITopLevelAgent root, final IColor color) {
		if (!canSendMessage(root.getExperiment())) return;
		sendMessage(root.getExperiment(), GAMA.getJsonEncoder().object("message", s, "color", color),
				MessageType.SimulationOutput);
	}

	@Override
	public void debugConsole(final int cycle, final String s, final ITopLevelAgent root, final IColor color) {
		if (!canSendMessage(root.getExperiment())) return;
		sendMessage(root.getExperiment(), GAMA.getJsonEncoder().object("cycle", cycle, "message", s, "color", color),
				MessageType.SimulationDebug);
	}
}