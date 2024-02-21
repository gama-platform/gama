/*******************************************************************************************************
 *
 * GamaServerMessager.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime.server;

import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.util.file.json.Json;
import gama.dev.DEBUG;

/**
 * The Class GamaServerMessager.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 2 nov. 2023
 */
public abstract class GamaServerMessager {

	/** The json. */
	Json json = Json.getNew();

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
	public void sendMessage(final IExperimentAgent exp, final Object m, final GamaServerMessage.Type type) {
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
			socket.send(
					json.valueOf(new GamaServerMessage(type, m, scope.getServerConfiguration().expId())).toString());

		} catch (Exception ex) {
			ex.printStackTrace();
			DEBUG.OUT("Unable to send message:" + m);
			DEBUG.OUT(ex.toString());
		}
	}

}