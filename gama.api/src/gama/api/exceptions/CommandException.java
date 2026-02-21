/**
 * Exception thrown when a command execution fails in the GAMA server/headless context.
 * <p>
 * This exception encapsulates a {@link CommandResponse} object that contains detailed
 * information about the failed command, including the experiment ID and error details.
 * It is primarily used in headless or server mode to communicate command execution failures
 * back to the client.
 * </p>
 */
package gama.api.exceptions;

import gama.api.utils.server.CommandResponse;

/**
 * Exception representing a failed command execution in GAMA.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 déc. 2023
 */
public class CommandException extends GamaRuntimeException {

	/** The response. */
	final CommandResponse response;

	/**
	 * Instantiates a new command exception.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param response
	 *            the response
	 * @date 5 déc. 2023
	 */
	public CommandException(final CommandResponse response) {
		super(null, response.exp_id, false);
		this.response = response;
	}

	/**
	 * Gets the response.
	 *
	 * @return the response
	 */
	public CommandResponse getResponse() { return response; }

}