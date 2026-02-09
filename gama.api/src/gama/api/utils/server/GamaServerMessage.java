/*******************************************************************************************************
 *
 * GamaServerMessage.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.server;

import gama.api.data.json.IJson;
import gama.api.data.json.IJsonObject;
import gama.api.data.json.IJsonValue;
import gama.api.data.json.IJsonable;

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
	public IJsonValue serializeToJson(final IJson json) {
		IJsonObject o = json.object().add("type", type).add("content", content);
		if (exp_id != null) { o.add(ISocketCommand.EXP_ID, exp_id); }
		return o;
	}

}
