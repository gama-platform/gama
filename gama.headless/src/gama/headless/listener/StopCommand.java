/*******************************************************************************************************
 *
 * StopCommand.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
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
import gama.core.runtime.server.IGamaServer;
import gama.core.runtime.server.ISocketCommand;
import gama.core.runtime.server.MessageType;
import gama.core.util.IMap;

/**
 * The Class StopCommand.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 15 oct. 2023
 */
public class StopCommand implements ISocketCommand {

	@Override
	public CommandResponse execute(final IGamaServer server, final WebSocket socket,
			final IMap<String, Object> map) {
		IExperimentPlan plan;
		try {
			plan = server.retrieveExperimentPlan(socket, map);
		} catch (CommandException e) {
			return e.getResponse();
		}
		if (plan.getController().processPause(true)) {
			plan.getController().dispose();
			return new CommandResponse(MessageType.CommandExecutedSuccessfully, "", map, false);
		}
		return new CommandResponse(MessageType.UnableToExecuteRequest, "Controller is full", map, false);

	}
}
