/*******************************************************************************************************
 *
 * JsonGamaContentsObject.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file.json;

import gama.core.metamodel.agent.AgentReference;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.SerialisedAgent;
import gama.core.runtime.IScope;

/**
 * The Class JsonGamaContentsObject.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 6 nov. 2023
 */
public class JsonGamaContentsObject extends JsonAbstractObject {

	/**
	 * Instantiates a new json gama contents object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param json
	 *            the json
	 * @date 6 nov. 2023
	 */
	public JsonGamaContentsObject(final JsonValue contents, final JsonObject references, final Json json) {
		super(json);
		add(CONTENTS_WITH_REFERENCES_LABEL, contents).add(REFERENCE_TABLE_LABEL, references);
	}

	@Override
	public boolean isObject() { return true; }

	@Override
	public Object toGamlValue(final IScope scope) {
		JsonObject references = get(REFERENCE_TABLE_LABEL).asObject();
		recreateAgents(scope, references);
		JsonValue contents = get(CONTENTS_WITH_REFERENCES_LABEL);
		return contents.toGamlValue(scope);
	}

	/**
	 * Recreate agents.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param references
	 *            the references
	 * @date 7 nov. 2023
	 */
	private void recreateAgents(final IScope scope, final JsonObject references) {
		for (JsonObjectMember m : references) {
			IAgent a = AgentReference.of(m.getName()).getReferencedAgent(scope);
			JsonGamlAgent jga = (JsonGamlAgent) m.getValue();
			SerialisedAgent sa = jga.toGamlValue(scope);
			sa.restoreAs(scope, a);
		}
	}

}
