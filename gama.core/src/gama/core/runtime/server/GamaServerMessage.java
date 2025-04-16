/*******************************************************************************************************
 *
 * GamaServerMessage.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime.server;

import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonObject;
import gama.core.util.file.json.JsonValue;
import gama.gaml.interfaces.IJsonable;

/**
 * The Class GamaServerMessage.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 15 oct. 2023
 */
public class GamaServerMessage implements IJsonable {

	/** The type. */
	public final MessageType type;

	/** The content. */
	public final Object content;

	/**
	 * The exp_id in case the message is linked to a running experiment
	 */
	public final String exp_id;

	/**
	 * Instantiates a new gama server message.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param t
	 *            the t
	 * @param content
	 *            the content
	 * @param exp_id
	 *            the exp_id in case the message is linked to a running experiment
	 * @date 15 oct. 2023
	 */
	public GamaServerMessage(final MessageType t, final Object content, final String exp_id) {
		this.type = t;
		this.content = content;
		this.exp_id = exp_id;
	}

	/**
	 * Instantiates a new gama server message.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param t
	 *            the t
	 * @param content
	 *            the content
	 * @date 15 oct. 2023
	 */
	public GamaServerMessage(final MessageType t, final Object content) {
		this(t, content, null);
	}

	/**
	 * Serialize to json.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the string
	 * @date 28 oct. 2023
	 */
	@Override
	public JsonValue serializeToJson(final Json json) {
		JsonObject o = (JsonObject) json.object().add("type", type).add("content", content);
		if (exp_id != null) { o.add(ISocketCommand.EXP_ID, exp_id); }
		return o;
	}

}
