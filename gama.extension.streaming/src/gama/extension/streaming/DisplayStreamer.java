/*******************************************************************************************************
 *
 * DisplayStreamer.java, in gama.extension.streaming, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.streaming;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.java_websocket.WebSocket;

import gama.api.GAMA;
import gama.api.utils.server.GamaServerMessage;
import gama.api.utils.server.MessageType;
import gama.core.outputs.LayeredDisplayOutput;
import gama.dev.DEBUG;

/**
 * Captures a single frame of a display and sends it to a client over the gama-server WebSocket as a JSON
 * {@link MessageType#SimulationImage} message carrying a base64-encoded PNG.
 *
 * <p>
 * The frame is rendered through {@link LayeredDisplayOutput#getImage(int, int)}, which works identically for the
 * headless ({@code ImageDisplaySurface}) and on-screen (Java2D / OpenGL) backends — each backend already marshals the
 * pixel read-back to its own thread.
 */
public class DisplayStreamer {

	static {
		DEBUG.OFF();
	}

	/**
	 * Renders the given display at the subscription's resolution and sends it to the client as a
	 * {@code SimulationImage} message. Failures (closed socket, missing surface, encoding error) are swallowed so the
	 * pump can keep going.
	 *
	 * @param socket
	 *            the client WebSocket
	 * @param expId
	 *            the id of the running experiment (used to route the message client-side, may be null)
	 * @param output
	 *            the display output to capture
	 * @param sub
	 *            the subscription describing resolution and target display
	 * @param cycle
	 *            the simulation cycle this frame corresponds to
	 */
	public static void streamFrame(final WebSocket socket, final String expId, final LayeredDisplayOutput output,
			final StreamSubscription sub, final int cycle) {
		if (socket == null || socket.isClosed() || socket.isClosing()) return;
		try {
			final BufferedImage image = output.getImage(sub.width, sub.height);
			if (image == null) return;
			final String base64 = toBase64Png(image);

			final Map<String, Object> content = new LinkedHashMap<>();
			content.put("display", sub.display);
			content.put("cycle", cycle);
			content.put("width", image.getWidth());
			content.put("height", image.getHeight());
			content.put("mime", "image/png");
			content.put("data", base64);

			final String message = GAMA.getJsonEncoder()
					.valueOf(new GamaServerMessage(MessageType.SimulationImage, content, expId)).toString();
			if (!socket.isClosed() && !socket.isClosing()) { socket.send(message); }
		} catch (final Exception e) {
			DEBUG.ERR("Unable to stream a frame of display '" + sub.display + "'", e);
		}
	}

	/**
	 * Encodes a {@link BufferedImage} into a base64 PNG string.
	 *
	 * @param image
	 *            the image to encode
	 * @return the base64-encoded PNG
	 * @throws IOException
	 *             if the PNG encoding fails
	 */
	private static String toBase64Png(final BufferedImage image) throws IOException {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			ImageIO.write(image, "png", os);
			return Base64.getEncoder().encodeToString(os.toByteArray());
		}
	}

}
