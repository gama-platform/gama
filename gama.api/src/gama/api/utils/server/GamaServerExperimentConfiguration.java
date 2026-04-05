/*******************************************************************************************************
 *
 * GamaServerExperimentConfiguration.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.server;

import org.java_websocket.WebSocket;

/**
 * The GamaServerExperimentConfiguration.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 3 nov. 2023
 */
public record GamaServerExperimentConfiguration(WebSocket socket, String expId, boolean hasConsole, boolean hasStatus,
		boolean hasDialog, boolean hasRuntime) implements IServerConfiguration {

	/**
	 * Clones the current config with an exp id.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param experimentID
	 *            the experiment ID
	 * @return the gama server experiment configuration
	 * @date 3 nov. 2023
	 */
	@Override
	public GamaServerExperimentConfiguration withExpId(final String experimentID) {
		return new GamaServerExperimentConfiguration(socket, experimentID, hasConsole, hasStatus, hasDialog,
				hasRuntime);
	}

	/**
	 * Clones the current config with a websocket
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param experimentID
	 *            the experiment ID
	 * @return the gama server experiment configuration
	 * @date 3 nov. 2023
	 */
	@Override
	public GamaServerExperimentConfiguration withSocket(final WebSocket s) {
		return new GamaServerExperimentConfiguration(s, expId, hasConsole, hasStatus, hasDialog, hasRuntime);
	}
}
