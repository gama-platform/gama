/*******************************************************************************************************
 *
 * LoadCommand.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.listener;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.java_websocket.WebSocket;

import gama.api.exceptions.CommandException;
import gama.api.exceptions.GamaCompilationFailedException;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.utils.server.CommandResponse;
import gama.api.utils.server.GamaWebSocketServer;
import gama.api.utils.server.IGamaServer;
import gama.api.utils.server.ISocketCommand;
import gama.api.utils.server.MessageType;
import gama.api.utils.server.ReceivedMessage;
import gama.api.utils.server.ServerLoadRequest;
import gama.dev.DEBUG;
import gama.headless.core.GamaHeadlessException;
import gama.headless.server.GamaServerExperimentJob;

/**
 * The Class LoadCommand.
 * Specialized for calls in headless server, strips out GUI dependencies
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 15 oct. 2023
 */
public class HeadlessLoadCommand implements ISocketCommand {
	@Override
	public CommandResponse execute(final IGamaServer server, final WebSocket socket, final ReceivedMessage map) {
		try {
			return launchGamlSimulation(server, socket, ServerLoadRequest.parse(socket, map), map);
		} catch (CommandException e) {
			return e.getResponse();
		} catch (GamaCompilationFailedException compError) {
			DEBUG.OUT(compError);
			return new CommandResponse(MessageType.UnableToExecuteRequest, compError.toJsonString(), map, true);
		} catch (Exception e) {
			DEBUG.OUT(e);
			return new CommandResponse(MessageType.UnableToExecuteRequest, e, map, false);
		}
	}

	/**
	 * Launch gaml simulation.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param gamaWebSocketServer
	 *            the gama web socket server
	 * @param socket
	 *            the socket
	 * @param request
	 *            the validated load options
	 * @param map
	 *            the map
	 * @return the command response
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws GamaHeadlessException
	 *             the gama headless exception
	 * @date 15 oct. 2023
	 */
	public CommandResponse launchGamlSimulation(final IGamaServer gamaWebSocketServer, final WebSocket socket,
			final ServerLoadRequest request, final Map<String, Object> map)
			throws IOException, GamaCompilationFailedException {

		final String socketId = map.get("socket_id") != null ? map.get("socket_id").toString()
				: GamaWebSocketServer.getSocketId(socket);
		final File ff = request.model();
		final String argExperimentName = request.experiment();
		final var config = request.configuration();
		final String end = request.until();
		GamaServerExperimentJob selectedJob = new GamaServerExperimentJob(ff.getAbsoluteFile().toString(),
				argExperimentName, socket, request.parameters(), end, config.hasConsole(), config.hasStatus(),
				config.hasDialog(), config.hasRuntime());
		selectedJob.load();
		// we check if the experiment is present in the file
		if (selectedJob.simulator.getModel().getExperiment(argExperimentName) == null)
			return new CommandResponse(MessageType.UnableToExecuteRequest,
					"'" + argExperimentName + "' is not an experiment present in '" + ff.getAbsolutePath() + "'", map,
					false);

		if (selectedJob.controller.processOpen(true)) {
			gamaWebSocketServer.addExperiment(socketId, selectedJob.getExperimentID(),
					selectedJob.controller.getExperiment());
			gamaWebSocketServer.execute(selectedJob.controller.executionThread);
			return new CommandResponse(MessageType.CommandExecutedSuccessfully, selectedJob.getExperimentID(), map,
					false);
		}
		// A runtime error raised by the init does not propagate, so it is reported here rather than as a bare failure.
		final GamaRuntimeException error = selectedJob.controller.consumeLastRuntimeError();
		if (error != null) return new CommandResponse(MessageType.RuntimeError, error, map, false);
		return new CommandResponse(MessageType.UnableToExecuteRequest, selectedJob.getExperimentID(), map, false);

	}

}
