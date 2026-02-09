/**
 * 
 */
package gama.api.exceptions;

import gama.api.utils.server.CommandResponse;

/**
 * The Class CommandException.
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