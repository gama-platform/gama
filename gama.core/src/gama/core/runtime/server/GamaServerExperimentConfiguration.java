/*******************************************************************************************************
 *
 * GamaServerExperimentConfiguration.java, in gama.core, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime.server;

import org.java_websocket.WebSocket;

/**
 * The GamaServerExperimentConfiguration.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 3 nov. 2023
 */
public record GamaServerExperimentConfiguration(WebSocket socket, String expId, boolean console, boolean status,
		boolean dialog, boolean runtime) {

	/** The null. */
	public static final GamaServerExperimentConfiguration NULL =
			new GamaServerExperimentConfiguration(null, "", false, false, false, false);

	/** The gui. */
	public static final GamaServerExperimentConfiguration GUI =
			new GamaServerExperimentConfiguration(null, "", true, true, true, true);

	/**
	 * Clones the current config with an exp id.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param experimentID
	 *            the experiment ID
	 * @return the gama server experiment configuration
	 * @date 3 nov. 2023
	 */
	public GamaServerExperimentConfiguration withExpId(final String experimentID) {
		return new GamaServerExperimentConfiguration(socket, experimentID, console, status, dialog, runtime);
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
	public GamaServerExperimentConfiguration withSocket(final WebSocket s) {
		return new GamaServerExperimentConfiguration(s, expId, console, status, dialog, runtime);
	}
}
