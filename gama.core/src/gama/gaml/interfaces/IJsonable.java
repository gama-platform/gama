/*******************************************************************************************************
 *
 * IJsonable.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.interfaces;

import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;

/**
 * The interface IJSonable. Represents objects that can represent themselves in terms of JSON descriptions
 * (serialization).
 *
 * @author A. Drogoul
 *
 *
 */
public interface IJsonable {

	/**
	 * Returns the serialization in JSON of this object. The context of serialization is passed through "json". This
	 * method should never be called directly (use Json.valueOf() instead);
	 *
	 * @return a string that can be reinterpreted to reproduce the object
	 */
	JsonValue serializeToJson(Json json);

}
