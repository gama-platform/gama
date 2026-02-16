/*******************************************************************************************************
 *
 * JsonGamlAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.json;

import java.io.IOException;
import java.util.Map;

import gama.api.constants.IKeyword;
import gama.api.kernel.serialization.ISerialisedAgent;
import gama.api.kernel.serialization.SerialisedAgent;
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
public class JsonGamlAgent extends JsonAbstractObject implements IJsonObject.Agent {

	/** The type. */
	final String species;

	/** The index. */
	int index;

	/**
	 * Instantiates a new json gaml object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param json
	 *            the json
	 * @date 3 nov. 2023
	 */
	public JsonGamlAgent(final String species, final int index, final IJson json) {
		super(json);
		this.species = species;
		this.index = index;
	}

	/**
	 * Instantiates a new json gaml object from an existing JsonObject
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param json
	 *            the json
	 * @date 3 nov. 2023
	 */
	public JsonGamlAgent(final String species, final int index, final IJsonObject object, final IJson json) {
		this(species, index, json);
		for (IJsonObject.Member m : object) { add(m.name(), m.value()); }
	}

	@Override
	public ISerialisedAgent toGamlValue(final IScope scope) {
		IMap map = toMap(scope);
		return SerialisedAgent.of(index, species, (Map<String, Object>) map.get("attributes"));
	}

	@Override
	protected void writeMembers(final JsonWriter writer) throws IOException {
		writer.writeMemberName(IJson.Labels.GAML_SPECIES_LABEL);
		writer.writeMemberSeparator();
		writer.writeString(species);
		writer.writeObjectSeparator();
		writer.writeMemberName(IKeyword.INDEX);
		writer.writeMemberSeparator();
		writer.writeNumber(String.valueOf(index));
		writer.writeObjectSeparator();
		super.writeMembers(writer);
	}

	@Override
	public boolean isGamlAgent() { return true; }

	@Override
	public IJsonObject.Agent asGamlAgent() {
		return this;
	}

}
