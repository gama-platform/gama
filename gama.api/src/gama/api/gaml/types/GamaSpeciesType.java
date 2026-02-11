/*******************************************************************************************************
 *
 * GamaSpeciesType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.type;
import gama.annotations.usage;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulationSet;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;

/**
 * The type used for representing species objects (since they can be manipulated in a model)
 *
 * Written by drogoul Modified on 1 aout 2010
 *
 * @todo Description
 *
 */
@type (
		name = IKeyword.SPECIES,
		id = IType.SPECIES,
		wraps = { ISpecies.class },
		kind = ISymbolKind.Variable.REGULAR,
		concept = { IConcept.TYPE, IConcept.SPECIES },
		doc = @doc ("Meta-type of the species present in the GAML language"))
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class GamaSpeciesType extends GamaContainerType<ISpecies> {

	/**
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
	 */
	public GamaSpeciesType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@Override
	@doc (
			value = "casting of the operand to a species.",
			usages = { @usage ("if the operand is nil, returns nil;"),
					@usage ("if the operand is an agent, returns its species;"),
					@usage ("if the operand is a string, returns the species with this name (nil if not found);"),
					@usage ("otherwise, returns nil") },
			examples = { @example (
					value = "species(self)",
					equals = "the species of the current agent",
					isExecutable = false),
					@example (
							value = "species('node')",
							equals = "node",
							isExecutable = false),
					@example (
							value = "species([1,5,9,3])",
							equals = "nil",
							isExecutable = false),
					@example (
							value = "species(node1)",
							equals = "node",
							isExecutable = false) })
	public ISpecies cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		// TODO Add a more general cast with list of agents to find a common
		// species.
		ISpecies species =
				obj == null ? getDefault() : obj instanceof ISpecies i ? i : obj instanceof IAgent i ? i.getSpecies()
						: obj instanceof String s
								? scope.getModel() != null ? scope.getModel().getSpecies(s) : getDefault()
						: getDefault();
		if (obj instanceof IPopulationSet) { species = ((IPopulationSet) obj).getSpecies(); }
		return species;
	}

	@Override
	public ISpecies cast(final IScope scope, final Object obj, final Object param, final IType keyType,
			final IType contentType, final boolean copy) {

		final ISpecies result = cast(scope, obj, param, copy);
		if (result == null && contentType.isAgentType()) return scope.getModel().getSpecies(contentType.getName());
		return result;
	}

	// TODO Verify that we dont need to declare the other cast method

	@Override
	public ISpecies getDefault() { return null; }

	@Override
	public IType getContentType() { return Types.get(AGENT); }

	@Override
	public IType getKeyType() { return Types.INT; }

	@Override
	public boolean isDrawable() { return true; }

	@Override
	public IType contentsTypeIfCasting(final IExpression exp) {
		final IType itemType = exp.getGamlType();
		if (itemType.isAgentType()) return itemType;
		switch (exp.getGamlType().id()) {
			case SPECIES:
				return itemType.getContentType();
			case IType.STRING:
				return Types.AGENT;
		}
		return exp.getGamlType();
	}

	@Override
	public boolean canCastToConst() {
		return false;
	}

}
