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
package gama.core.util.json;

import gama.api.kernel.agent.AgentReference;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.serialization.ISerialisedAgent;
import gama.api.runtime.scope.IScope;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonObject;
import gama.api.utils.json.IJsonValue;

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
	public JsonGamaContentsObject(final IJsonValue contents, final IJsonObject references, final IJson json) {
		super(json);
		add(IJson.Labels.CONTENTS_WITH_REFERENCES_LABEL, contents).add(IJson.Labels.REFERENCE_TABLE_LABEL, references);
	}

	@Override
	public boolean isObject() { return true; }

	@Override
	public Object toGamlValue(final IScope scope) {
		IJsonObject references = get(IJson.Labels.REFERENCE_TABLE_LABEL).asObject();
		recreateAgents(scope, references);
		IJsonValue contents = get(IJson.Labels.CONTENTS_WITH_REFERENCES_LABEL);
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
	private void recreateAgents(final IScope scope, final IJsonObject references) {
		for (IJsonObject.Member m : references) {
			IAgent a = AgentReference.of(m.name()).getReferencedAgent(scope);
			IJsonObject.Agent jga = (IJsonObject.Agent) m.value();
			ISerialisedAgent sa = jga.toGamlValue(scope);
			sa.restoreAs(scope, a);
		}
	}

}
