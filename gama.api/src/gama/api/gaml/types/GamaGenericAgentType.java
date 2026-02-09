/*******************************************************************************************************
 *
 * GamaGenericAgentType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;

/**
 * The "generic" agent type.
 *
 * Written by drogoul Modified on 1 ao�t 2010
 *
 * @todo Description
 * @modified 08 juin 2012
 *
 */
@type (
		name = IKeyword.AGENT,
		id = IType.AGENT,
		wraps = { IAgent.class },
		kind = ISymbolKind.Variable.REGULAR,
		concept = { IConcept.TYPE, IConcept.SPECIES },
		doc = @doc ("The basic and default type of agents in GAML"))
public class GamaGenericAgentType extends GamaAgentType {

	/**
	 * Instantiates a new gama generic agent type.
	 */
	public GamaGenericAgentType() {
		super(null, IKeyword.AGENT, IType.AGENT, IAgent.class);
	}

	/**
	 * Sets the species.
	 *
	 * @param sd
	 *            the new species
	 */
	public void setSpecies(final ISpeciesDescription sd) { species = sd; }

	@Override
	public IAgent cast(final IScope scope, final Object obj, final Object param, final IType<?> keyType,
			final IType<?> contentsType, final boolean copy) throws GamaRuntimeException {
		return cast(scope, obj, param, copy);
	}

	@Override
	@doc ("Returns an agent if the argument is already an agent, otherwise returns null")
	public IAgent cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj == null) return getDefault();
		if (obj instanceof IAgent a) return a;
		return getDefault();
	}

	@Override
	public IGamlDocumentation getDocumentation() {
		return new GamlConstantDocumentation("Generic type of all agents in a model");
	}

	@Override
	public boolean isSuperTypeOf(final IType<?> type) {
		return type != this && type instanceof GamaAgentType;
	}

}
