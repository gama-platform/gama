/*******************************************************************************************************
 *
 * CommandResponse.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime.server;

import gama.core.util.IMap;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;

/**
 * The Class CommandResponse.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 15 oct. 2023
 */
public class CommandResponse extends GamaServerMessage {

	/** The command parameters. */
	public final IMap<String, Object> commandParameters;

	/** The is json. */
	protected boolean isJson = false;

	/**
	 * Instantiates a new command response.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param t
	 *            the t
	 * @param content
	 *            the content
	 * @param parameters
	 *            the parameters
	 * @param isJSON
	 *            the is JSON
	 * @date 15 oct. 2023
	 */
	public CommandResponse(final MessageType t, final Object content, final IMap<String, Object> parameters,
			final boolean isJSON) {
		super(t, content);
		this.commandParameters = parameters;
		this.isJson = isJSON;
	}

	@Override
	public JsonValue serializeToJson(final Json json) {
		var params = commandParameters.copy(null);
		params.remove("server");
		return json.object("type", type, "content", isJson ? json.parse((String) content) : content, "command", params);
	}

}
