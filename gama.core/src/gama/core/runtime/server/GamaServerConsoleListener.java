/*******************************************************************************************************
 *
 * GamaServerConsoleListener.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.runtime.server;

import gama.core.common.interfaces.IConsoleListener;
import gama.core.common.preferences.GamaPreferences;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.util.GamaColor;

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
		if (!GamaPreferences.Runtime.CORE_SERVER_CONSOLE.getValue()) { return false; }
		var scope = exp.getScope();
		return scope != null && scope.getServerConfiguration().console();
	}

	@Override
	public void informConsole(final String s, final ITopLevelAgent root, final GamaColor color) {
		if (!canSendMessage(root.getExperiment())) { return; }
		sendMessage(root.getExperiment(), json.object("message", s, "color", color), MessageType.SimulationOutput);
	}

	@Override
	public void debugConsole(final int cycle, final String s, final ITopLevelAgent root, final GamaColor color) {
		if (!canSendMessage(root.getExperiment())) { return; }
		sendMessage(root.getExperiment(), json.object("cycle", cycle, "message", s, "color", color),
				MessageType.SimulationDebug);
	}
}