/*******************************************************************************************************
 *
 * JsonReferenceObject.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
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
import gama.api.runtime.scope.IScope;
import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonObject;

/**
 * The Class JsonReferenceObject.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 nov. 2023
 */
public class JsonReferenceObject extends JsonAbstractObject implements IJsonObject.Reference {

	/** The ref. */
	final String ref;

	/**
	 * Instantiates a new json reference object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param json
	 *            the json
	 * @date 5 nov. 2023
	 */
	public JsonReferenceObject(final String ref, final IJson json) {
		super(json);
		this.ref = ref;
	}

	@Override
	public IAgent toGamlValue(final IScope scope) {
		return AgentReference.of(ref).getReferencedAgent(scope);
	}

}
