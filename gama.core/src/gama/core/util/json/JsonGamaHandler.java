/*******************************************************************************************************
 *
 * JsonGamaHandler.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.json;

import static gama.api.utils.prefs.GamaPreferences.External.JSON_INFINITY;
import static gama.api.utils.prefs.GamaPreferences.External.JSON_NAN;

import gama.annotations.constants.IKeyword;
import gama.api.gaml.types.Types;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonArray;
import gama.api.utils.json.IJsonValue;
import gama.api.utils.prefs.GamaPreferences;

/**
 * The Class DefaultHandler.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 29 oct. 2023
 */
class JsonGamaHandler extends JsonHandler<IJsonArray, JsonObject> implements IJsonConstants {

	/**
	 * Instantiates a new default handler.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param json
	 *            the json
	 * @date 1 nov. 2023
	 */
	JsonGamaHandler(final IJson json) {
		this.json = json;
	}

	/** The json. */
	protected IJson json;

	/** The value. */
	protected IJsonValue value;

	@Override
	public IJsonArray startArray() {
		return new JsonArray(json);
	}

	@Override
	public JsonObject startObject() {
		return new JsonObject(json);
	}

	@Override
	public void endNull() {
		value = IJsonConstants.NULL;
	}

	@Override
	public void endBoolean(final boolean bool) {
		value = bool ? IJsonConstants.TRUE : IJsonConstants.FALSE;
	}

	@Override
	public void endString(final String string) {
		value = switch (string) {
			case "Infinity" -> JSON_INFINITY.getValue() ? POSITIVE_INFINITY : new JsonString(string);
			case "-Infinity" -> JSON_INFINITY.getValue() ? NEGATIVE_INFINITY : new JsonString(string);
			case "NaN" -> JSON_NAN.getValue() ? IJsonConstants.NAN : new JsonString(string);
			default -> new JsonString(string);
		};

	}

	@Override
	public void endNumber(final String string, final boolean isStructurallyFloat) {
		boolean isFloat = isStructurallyFloat;
		boolean isString = false;
		if (!isFloat) {
			try {
				Integer.parseInt(string);
			} catch (NumberFormatException e) {
				// see issues #3945 and #16
				if (GamaPreferences.External.JSON_INT_OVERFLOW.getValue()) {
					isFloat = true;
				} else {
					isString = true;
				}
			}
		}
		value = isFloat ? new JsonFloat(string) : isString ? new JsonString(string) : new JsonInt(string);
	}

	/**
	 * End array.
	 *
	 * @param array
	 *            the array
	 */
	@Override
	public void endArray(final IJsonArray array) {
		value = array;
	}

	@Override
	public void endObject(final JsonObject object) {
		if (object.contains(IJson.Labels.GAML_SPECIES_LABEL)) {
			value = new JsonGamlAgent(object.remove(IJson.Labels.GAML_SPECIES_LABEL).asString(),
					object.remove(IKeyword.INDEX).asInt(), object, json);
		} else if (object.contains(IJson.Labels.AGENT_REFERENCE_LABEL)) {
			value = new JsonReferenceObject(object.get(IJson.Labels.AGENT_REFERENCE_LABEL).asString(), json);
		} else if (object.contains(IJson.Labels.CONTENTS_WITH_REFERENCES_LABEL)) {
			value = new JsonGamaContentsObject(object.get(IJson.Labels.CONTENTS_WITH_REFERENCES_LABEL),
					object.get(IJson.Labels.REFERENCE_TABLE_LABEL).asObject(), json);
		} else {
			value = object;
		}
	}

	/**
	 * End gaml object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param object
	 *            the object
	 * @date 4 nov. 2023
	 */
	@Override
	public void endGamlObject(final String type, final JsonObject object) {
		if (Types.GEOMETRY.getName().equals(type)) {
			value = new JsonGeometryObject(object, json);
		} else {
			value = new JsonGamlObject(type, object, json);
		}
	}

	@Override
	public void endArrayValue(final IJsonArray array) {
		array.add(value);
	}

	@Override
	public void endObjectValue(final JsonObject object, final String name) {
		object.add(name, value);
	}

	/**
	 * Gets the value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the value
	 * @date 29 oct. 2023
	 */
	IJsonValue getValue() { return value; }

}