/*******************************************************************************************************
 *
 * LoadCommand.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.listener;

import java.io.File;
import java.io.IOException;

import org.java_websocket.WebSocket;

import gama.core.common.GamlFileExtension;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.server.CommandExecutor;
import gama.core.runtime.server.CommandResponse;
import gama.core.runtime.server.GamaWebSocketServer;
import gama.core.runtime.server.IGamaServer;
import gama.core.runtime.server.ISocketCommand;
import gama.core.runtime.server.MessageType;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.gaml.compilation.GamaCompilationFailedException;
import gama.headless.core.GamaHeadlessException;
import gama.headless.server.GamaServerExperimentJob;

/**
 * The Class LoadCommand.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 15 oct. 2023
 */
public class LoadCommand implements ISocketCommand {
	@Override
	public CommandResponse execute(final IGamaServer server, final WebSocket socket,
			final IMap<String, Object> map) {
		final Object model = map.get(IKeyword.MODEL);
		final Object experiment = map.get(IKeyword.EXPERIMENT);
		if (model == null || experiment == null) return new CommandResponse(MessageType.MalformedRequest,
				"For " + LOAD + ", mandatory parameters are: 'model' and 'experiment'", map, false);
		try {
			return launchGamlSimulation(server, socket, (IList) map.get(PARAMETERS),
					map.get(UNTIL) != null ? map.get(UNTIL).toString() : "", map);
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
	 * @param params
	 *            the params
	 * @param end
	 *            the end
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
			final IList params, final String end, final IMap<String, Object> map)
			throws IOException, GamaCompilationFailedException {

		final String pathToModel = map.get("model").toString();
		final String socketId = map.get("socket_id") != null ? map.get("socket_id").toString()
				: GamaWebSocketServer.getSocketId(socket);

		File ff = new File(pathToModel);

		if (!ff.exists()) {
			DEBUG.OUT(ff.getAbsolutePath() + " does not exist");
			return new CommandResponse(MessageType.UnableToExecuteRequest,
					"'" + ff.getAbsolutePath() + "' does not exist", map, false);
		}
		if (!GamlFileExtension.isGaml(ff.getAbsoluteFile().toString())) {
			DEBUG.OUT(ff.getAbsolutePath() + " is not a gaml file");
			return new CommandResponse(MessageType.UnableToExecuteRequest,
					"'" + ff.getAbsolutePath() + "' is not a gaml file", map, false);
		}

		final String argExperimentName = map.get("experiment").toString();

		var console = map.get("console") != null ? Boolean.parseBoolean("" + map.get("console")) : true;
		var status = map.get("status") != null ? Boolean.parseBoolean("" + map.get("status")) : false;
		var dialog = map.get("dialog") != null ? Boolean.parseBoolean("" + map.get("dialog")) : false;
		var runtime = map.get("runtime") != null ? Boolean.parseBoolean("" + map.get("runtime")) : true;

		// we check that the parameters are properly formed
		var parametersError = CommandExecutor.checkLoadParameters(params, map);
		if (parametersError != null) return parametersError;

		GamaServerExperimentJob selectedJob = new GamaServerExperimentJob(ff.getAbsoluteFile().toString(), argExperimentName, socket, params,
						end, console, status, dialog, runtime);
		selectedJob.load();
		// we check if the experiment is present in the file
		if (selectedJob.simulator.getModel().getExperiment(argExperimentName) == null)
			return new CommandResponse(MessageType.UnableToExecuteRequest,
					"'" + argExperimentName + "' is not an experiment present in '" + ff.getAbsolutePath() + "'", map,
					false);

		if (selectedJob.controller.processOpen(true)) {
			selectedJob.controller.getExperiment().setStopCondition(end);
			gamaWebSocketServer.addExperiment(socketId, selectedJob.getExperimentID(),
					selectedJob.controller.getExperiment());
			gamaWebSocketServer.execute(selectedJob.controller.executionThread);
			return new CommandResponse(MessageType.CommandExecutedSuccessfully, selectedJob.getExperimentID(), map,
					false);
		}
		return new CommandResponse(MessageType.UnableToExecuteRequest, selectedJob.getExperimentID(), map, false);

	}

}
