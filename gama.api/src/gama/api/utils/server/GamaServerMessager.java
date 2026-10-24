/*******************************************************************************************************
 *
 * GamaServerMessager.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.server;

import gama.api.GAMA;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.runtime.scope.IScope;
import gama.dev.DEBUG;

/**
 * The Class GamaServerMessager.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 2 nov. 2023
 */
public abstract class GamaServerMessager {

	private static final GamaServerMessager FORWARDER = new GamaServerMessager() {
		@Override
		public boolean canSendMessage(final IExperimentAgent exp) { return exp != null; }
	};

	/** Mirrors an event to the experiment's client without changing local GUI behavior. */
	public static void forwardMessage(final IScope scope, final Object message, final MessageType type) {
		final IExperimentAgent exp = scope == null ? null : scope.getExperiment();
		if (exp == null) return;
		final IServerConfiguration config = exp.getScope().getServerConfiguration();
		if (config == null || config.socket() == null) return;
		final boolean enabled = switch (type) {
			case SimulationDialog, SimulationErrorDialog -> config.hasDialog();
			case SimulationError -> config.hasRuntime();
			case SimulationStatusInform, SimulationStatusError, SimulationStatusNeutral -> config.hasStatus();
			case SimulationEnded -> true;
			default -> throw new IllegalArgumentException("Unsupported forwarded event: " + type);
		};
		if (enabled) { FORWARDER.sendMessage(exp, message, type); }
	}

	/**
	 * Can send message.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param exp
	 *            the exp
	 * @return true, if successful
	 * @date 2 nov. 2023
	 */
	public abstract boolean canSendMessage(final IExperimentAgent exp);

	/**
	 * Send message.
	 *
	 * @param exp
	 *            the exp
	 * @param m
	 *            the m
	 * @param type
	 *            the type
	 */
	public void sendMessage(final IExperimentAgent exp, final Object m, final MessageType type) {
		try {
			if (exp == null) {
				DEBUG.OUT("No experiment, unable to send message: " + m);
				return;
			}
			var scope = exp.getScope();
			if (scope == null) {
				DEBUG.OUT("No scope, unable to send message: " + m);
				return;
			}
			var socket = scope.getServerConfiguration().socket();
			if (socket == null) {
				DEBUG.OUT("No socket found, maybe the client is already disconnected. Unable to send message: " + m);
				return;
			}
			if (socket.isClosed() || socket.isClosing() || !socket.isOpen()) {
				DEBUG.OUT(
						"Socket is closed or not open, maybe the client is already disconnected. Unable to send message: "
								+ m);
				return;
			}
			socket.send(GAMA.getJsonEncoder()
					.valueOf(new GamaServerMessage(type, m, scope.getServerConfiguration().expId())).toString());

		} catch (Exception ex) {
			ex.printStackTrace();
			DEBUG.OUT("Unable to send message:" + m);
			DEBUG.OUT(ex.toString());
		}
	}

}