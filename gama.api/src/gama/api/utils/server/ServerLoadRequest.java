/*******************************************************************************************************
 *
 * ServerLoadRequest.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.server;

import java.io.File;
import java.util.Map;

import org.java_websocket.WebSocket;

import gama.api.constants.GamlFileExtension;
import gama.api.exceptions.CommandException;
import gama.api.types.list.IList;

/** Options shared by the GUI and headless implementations of the WebSocket {@code load} command. */
public record ServerLoadRequest(File model, String experiment, IList<?> parameters, String until,
		GamaServerExperimentConfiguration configuration) {

	public static ServerLoadRequest parse(final WebSocket socket, final Map<String, Object> command) {
		if (command.get("model") == null || command.get("experiment") == null)
			throw invalid(MessageType.MalformedRequest,
					"For 'load', mandatory parameters are: 'model' and 'experiment'", command);
		final File model = new File(command.get("model").toString().trim()).getAbsoluteFile();
		if (!model.exists())
			throw invalid(MessageType.UnableToExecuteRequest, "'" + model + "' does not exist", command);
		if (!GamlFileExtension.isGaml(model.toString()))
			throw invalid(MessageType.UnableToExecuteRequest, "'" + model + "' is not a gaml file", command);
		final Object parameters = command.get(ISocketCommand.PARAMETERS);
		if (parameters != null && !(parameters instanceof IList<?>))
			throw invalid(MessageType.MalformedRequest, "'parameters' must be an array of parameter objects", command);
		final IList<?> params = (IList<?>) parameters;
		final CommandResponse error = CommandExecutor.checkLoadParameters(params, command);
		if (error != null) throw new CommandException(error);
		final String experiment = command.get("experiment").toString().trim();
		final String until = command.get(ISocketCommand.UNTIL) == null ? ""
				: command.get(ISocketCommand.UNTIL).toString();
		return new ServerLoadRequest(model, experiment, params, until,
				new GamaServerExperimentConfiguration(socket, experiment, flag(command, "console", true),
						flag(command, "status", false), flag(command, "dialog", false), flag(command, "runtime", true)));
	}

	private static boolean flag(final Map<String, Object> command, final String name, final boolean defaultValue) {
		final Object value = command.get(name);
		return value == null ? defaultValue : Boolean.parseBoolean(value.toString());
	}

	private static CommandException invalid(final MessageType type, final String message,
			final Map<String, Object> command) {
		return new CommandException(new CommandResponse(type, message, command, false));
	}
}
