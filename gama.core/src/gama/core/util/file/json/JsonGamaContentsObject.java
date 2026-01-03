/*******************************************************************************************************
 *
 * JsonGamaContentsObject.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
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
	public JsonGamaContentsObject(final IJsonValue contents, final IJsonObject references, final IJSon json) {
		super(json);
		add(CONTENTS_WITH_REFERENCES_LABEL, contents).add(REFERENCE_TABLE_LABEL, references);
	}

	@Override
	public boolean isObject() { return true; }

	@Override
	public Object toGamlValue(final IScope scope) {
		JsonObject references = get(REFERENCE_TABLE_LABEL).asObject();
		recreateAgents(scope, references);
		IJsonValue contents = get(CONTENTS_WITH_REFERENCES_LABEL);
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
		for (IJsonObjectMember m : references) {
			IAgent a = AgentReference.of(m.name()).getReferencedAgent(scope);
			JsonGamlAgent jga = (JsonGamlAgent) m.value();
			SerialisedAgent sa = jga.toGamlValue(scope);
			sa.restoreAs(scope, a);
		}
	}

}
