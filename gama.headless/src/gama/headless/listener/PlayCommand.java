/*******************************************************************************************************
 *
 * PlayCommand.java, in gama.headless, is part of the source code of the
 * GAMA modeling and simulation platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.headless.listener;

import org.java_websocket.WebSocket;

import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.runtime.server.CommandResponse;
import gama.core.runtime.server.GamaServerMessage;
import gama.core.runtime.server.GamaWebSocketServer;
import gama.core.runtime.server.ISocketCommand;
import gama.core.util.IMap;

/**
 * The Class PlayCommand.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 15 oct. 2023
 */
public class PlayCommand implements ISocketCommand {

	@Override
	public CommandResponse execute(final GamaWebSocketServer server, final WebSocket socket,
			final IMap<String, Object> map) {
		IExperimentPlan plan;
		try {
			plan = server.retrieveExperimentPlan(socket, map);
		} catch (CommandException e) {
			return e.getResponse();
		}
		final boolean sync = map.get(SYNC) != null ? Boolean.parseBoolean("" + map.get(SYNC)) : false;
		plan.getAgent().setAttribute("%%playCommand%%", map);
		if (!plan.getController().processStart(false))
			return new CommandResponse(GamaServerMessage.Type.UnableToExecuteRequest, "Controller is full", map, false);
		boolean hasEndCond = map.containsKey(UNTIL) && !map.get(UNTIL).toString().isBlank();
		if (hasEndCond && sync) return null;
		return new CommandResponse(GamaServerMessage.Type.CommandExecutedSuccessfully, "", map, false);
	}
}
