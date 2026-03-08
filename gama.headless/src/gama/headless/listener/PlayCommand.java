/*******************************************************************************************************
 *
 * PlayCommand.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.listener;

import org.java_websocket.WebSocket;

import gama.api.exceptions.CommandException;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.utils.server.CommandResponse;
import gama.api.utils.server.IGamaServer;
import gama.api.utils.server.ISocketCommand;
import gama.api.utils.server.MessageType;
import gama.api.utils.server.ReceivedMessage;

/**
 * The Class PlayCommand.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 15 oct. 2023
 */
public class PlayCommand implements ISocketCommand {

	@Override
	public CommandResponse execute(final IGamaServer server, final WebSocket socket, final ReceivedMessage map) {
		IExperimentSpecies plan;
		try {
			plan = server.retrieveExperimentPlan(socket, map);
		} catch (CommandException e) {
			return e.getResponse();
		}
		final boolean sync = map.get(SYNC) != null ? Boolean.parseBoolean("" + map.get(SYNC)) : false;
		plan.getAgent().setAttribute("%%playCommand%%", map);
		if (!plan.getController().processStart(false))
			return new CommandResponse(MessageType.UnableToExecuteRequest, "Controller is full", map, false);
		boolean hasEndCond = map.containsKey(UNTIL) && !map.get(UNTIL).toString().isBlank();
		if (hasEndCond && sync) return null;
		return new CommandResponse(MessageType.CommandExecutedSuccessfully, "", map, false);
	}
}
