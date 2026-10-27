/*******************************************************************************************************
 *
 * GamaServerLoggingDraft.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.server;

import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;

/** Captures all frame paths, including direct socket sends, broadcasts, and ping/pong, also over TLS. */
final class GamaServerLoggingDraft extends Draft_6455 {

	private final GamaServerExchangeLog log;
	private volatile WebSocket socket;

	GamaServerLoggingDraft(final GamaServerExchangeLog log) {
		this.log = log;
	}

	void bind(final WebSocket connection) { socket = connection; }

	@Override
	public Draft copyInstance() { return new GamaServerLoggingDraft(log); }

	@Override
	public ByteBuffer createBinaryFrame(final Framedata frame) {
		log.frame(socket, "sent", frame);
		return super.createBinaryFrame(frame);
	}

	@Override
	public void processFrame(final WebSocketImpl connection, final Framedata frame) throws InvalidDataException {
		log.frame(connection, "received", frame);
		super.processFrame(connection, frame);
	}
}
