/*******************************************************************************************************
 *
 * StreamCommand.java, in gama.extension.streaming, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.streaming;

import java.util.Map;

import org.java_websocket.WebSocket;

import gama.api.exceptions.CommandException;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.ui.IOutput;
import gama.api.utils.server.CommandResponse;
import gama.api.utils.server.GamaServerMessage;
import gama.api.utils.server.IGamaServer;
import gama.api.utils.server.ISocketCommand;
import gama.api.utils.server.MessageType;
import gama.api.utils.server.ReceivedMessage;
import gama.core.outputs.LayeredDisplayOutput;

/**
 * The {@code stream} gama-server command. Lets a client subscribe to (or unsubscribe from) the live image stream of a
 * display of a running experiment, on both the headless and the GUI servers.
 *
 * <p>
 * Expected JSON parameters:
 * <ul>
 * <li>{@code display} (required): the original name of the display to stream</li>
 * <li>{@code exp_id} (optional): the id of the experiment to stream from</li>
 * <li>{@code width}, {@code height} (optional, default 500): the resolution of the frames</li>
 * <li>{@code frame_rate} (optional, default 1): number of simulation cycles between two frames</li>
 * <li>{@code enabled} (optional, default true): {@code false} to stop streaming the display</li>
 * </ul>
 *
 * Frames are then pushed asynchronously as {@link MessageType#SimulationImage} messages by {@link StreamingService}.
 */
public class StreamCommand implements ISocketCommand {

	/** The display parameter. */
	private static final String DISPLAY = "display";

	/** The width parameter. */
	private static final String WIDTH = "width";

	/** The height parameter. */
	private static final String HEIGHT = "height";

	/** The frame rate parameter. */
	private static final String FRAME_RATE = "frame_rate";

	/** The enabled parameter. */
	private static final String ENABLED = "enabled";

	@Override
	public GamaServerMessage execute(final IGamaServer server, final WebSocket socket, final ReceivedMessage map) {

		final Object display = map.get(DISPLAY);
		if (display == null) return new CommandResponse(MessageType.MalformedRequest,
				"For " + map.get("type") + ", mandatory parameter is: '" + DISPLAY + "'", map, false);
		final String displayName = display.toString();

		// We resolve the experiment for both runtimes (GUI returns the active experiment, headless the one bound to the
		// socket / exp_id).
		final IExperimentSpecies plan;
		try {
			plan = server.retrieveExperimentPlan(socket, map);
		} catch (final CommandException e) {
			return e.getResponse();
		}

		final String expId = map.get(EXP_ID) != null ? map.get(EXP_ID).toString()
				: Integer.toHexString(System.identityHashCode(socket));

		final boolean enabled = map.get(ENABLED) == null || Boolean.parseBoolean("" + map.get(ENABLED));
		if (!enabled) {
			StreamingService.getInstance().unsubscribe(expId, displayName);
			return new CommandResponse(MessageType.CommandExecutedSuccessfully, "", map, false);
		}

		// We validate that the requested display exists and is a graphical display.
		final ISimulationAgent sim = plan.getCurrentSimulation();
		if (sim == null) return new CommandResponse(MessageType.UnableToExecuteRequest,
				"No running simulation to stream from", map, false);
		final IOutput output = sim.getOutputManager().getOutputWithOriginalName(displayName);
		if (!(output instanceof LayeredDisplayOutput)) return new CommandResponse(MessageType.UnableToExecuteRequest,
				"'" + displayName + "' is not a display of this simulation", map, false);

		final int width = intValue(map, WIDTH, 500);
		final int height = intValue(map, HEIGHT, 500);
		final int frameRate = intValue(map, FRAME_RATE, 1);

		StreamingService.getInstance().subscribe(socket, plan, expId,
				new StreamSubscription(displayName, width, height, frameRate));
		return new CommandResponse(MessageType.CommandExecutedSuccessfully, "", map, false);
	}

	/**
	 * Reads an integer parameter from the request, tolerating numbers parsed as {@link Number} or as strings.
	 *
	 * @param map
	 *            the request
	 * @param key
	 *            the parameter name
	 * @param defaultValue
	 *            the value to use if the parameter is absent or unreadable
	 * @return the integer value
	 */
	private static int intValue(final Map<String, Object> map, final String key, final int defaultValue) {
		final Object value = map.get(key);
		if (value == null) return defaultValue;
		if (value instanceof Number n) return n.intValue();
		try {
			return Integer.parseInt(value.toString());
		} catch (final NumberFormatException e) {
			return defaultValue;
		}
	}

}
