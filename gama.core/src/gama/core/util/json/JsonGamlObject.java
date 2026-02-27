/*******************************************************************************************************
 *
 * JsonGamlObject.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.json;

import java.io.IOException;

import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.map.IMap;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonObject;

/**
 * The Class JsonGamlObject.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 3 nov. 2023
 */
public class JsonGamlObject extends JsonAbstractObject {

	/** The type. */
	final String type;

	/**
	 * Instantiates a new json gaml object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param json
	 *            the json
	 * @date 3 nov. 2023
	 */
	public JsonGamlObject(final String type, final IJson json) {
		super(json);
		this.type = type;
	}

	/**
	 * Instantiates a new json gaml object from an existing JsonObject
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param json
	 *            the json
	 * @date 3 nov. 2023
	 */
	public JsonGamlObject(final String type, final IJsonObject object, final IJson json) {
		this(type, json);
		for (IJsonObject.Member m : object) { add(m.name(), m.value()); }
	}

	@Override
	public Object toGamlValue(final IScope scope) {
		IType<?> gamlType = Types.findTypesManager(scope).decodeType(type);
		return gamlType.deserializeFromJson(scope, (IMap<String, Object>) toMap(scope));

	}

	@Override
	protected void writeMembers(final JsonWriter writer) throws IOException {
		writer.writeMemberName(IJson.Labels.GAML_TYPE_LABEL);
		writer.writeMemberSeparator();
		writer.writeString(type);
		writer.writeObjectSeparator();
		super.writeMembers(writer);
	}

	@Override
	public boolean isGamlObject() { return true; }

	@Override
	public JsonGamlObject asGamlObject() {
		return this;
	}

}
