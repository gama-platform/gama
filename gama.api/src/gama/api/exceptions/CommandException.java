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

	/** The command response containing error details and experiment context. */
	final CommandResponse response;

	/**
	 * Constructs a new command exception with the given response.
	 * <p>
	 * This exception is typically thrown when a command sent to the GAMA server or headless
	 * execution environment fails. The response object contains detailed information about
	 * the failure, including the experiment ID and error details.
	 * </p>
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param response
	 *            the command response containing error details, must not be null
	 * @date 5 déc. 2023
	 */
	public CommandException(final CommandResponse response) {
		super(null, response.exp_id, false);
		this.response = response;
	}

	/**
	 * Gets the command response containing the error details.
	 * <p>
	 * The response object includes information about the failed command such as
	 * the experiment ID, error message, and any additional context needed for
	 * error handling and reporting.
	 * </p>
	 *
	 * @return the command response object, never null
	 */
	public CommandResponse getResponse() { return response; }

}