/*******************************************************************************************************
 *
 * GamaClassType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.types;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulationSet;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.expressions.IExpression;
import gama.gaml.species.IClass;
import gama.gaml.species.ISpecies;

/**
 * The type used for representing species objects (since they can be manipulated in a model)
 *
 * Written by drogoul Modified on 1 aout 2010
 *
 * @todo Description
 *
 */

/**
 * The Class GamaClassType.
 *
 * @param <T>
 *            the generic type
 */
@type (
		name = IKeyword.CLASS,
		id = IType.CLASS,
		wraps = { IClass.class },
		kind = ISymbolKind.Variable.REGULAR,
		concept = { IConcept.TYPE, IConcept.SPECIES },
		doc = @doc ("Meta-type of the classes present in the GAML language"))
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class GamaClassType<T extends IClass> extends GamaType<T> {

	@Override
	@doc (
			value = "casting of the operand to a class.",
			usages = { @usage ("if the operand is nil, returns nil;"),
					@usage ("if the operand is an object or an agent, returns its class or species;"),
					@usage ("if the operand is a string, returns the class / species with this name (nil if not found);"),
					@usage ("otherwise, returns nil") },
			examples = { @example (
					value = "class(self)",
					equals = "the species of the current agent",
					isExecutable = false),
					@example (
							value = "class([1,5,9,3])",
							equals = "nil",
							isExecutable = false) })

	public T cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		// TODO Add a more general cast with list of agents to find a common
		// species.
		T species = obj == null ? getDefault() : obj instanceof ISpecies ? (ISpecies) obj
				: obj instanceof IAgent ? ((IAgent) obj).getSpecies()
				: obj instanceof String
						? scope.getModel() != null ? scope.getModel().getSpecies((String) obj) : getDefault()
				: getDefault();
		if (obj instanceof IPopulationSet) { species = ((IPopulationSet) obj).getSpecies(); }
		return species;
	}

	@Override
	public T cast(final IScope scope, final Object obj, final Object param, final IType keyType,
			final IType contentType, final boolean copy) {

		final ISpecies result = cast(scope, obj, param, copy);
		if (result == null && contentType.isAgentType()) return scope.getModel().getSpecies(contentType.getName());
		return result;
	}

	// TODO Verify that we dont need to declare the other cast method

	@Override
	public T getDefault() { return null; }

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
