/*******************************************************************************************************
 *
 * SpeciesAttributeExpression.java, in gaml.compiler, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.expressions;

import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.descriptions.IVarDescriptionUser;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.types.Types;
import gama.api.kernel.species.SpeciesAttributeValues;
import gama.api.runtime.scope.IScope;
import gama.api.utils.GamlProperties;
import gama.api.utils.collections.ICollector;

/** A typed attribute snapshot, and a checked species-wide assignment on writes. */
public class SpeciesAttributeExpression extends VariableExpression {

	public static final class Assignment extends SpeciesAttributeExpression implements IVarExpression.SpeciesAttribute {

		public Assignment(final IExpression species, final IVarExpression attribute) {
			super(species, attribute);
		}
	}

	private final IExpression species;
	private final IVarExpression attribute;
	private final boolean spatial;

	public SpeciesAttributeExpression(final IExpression species, final IVarExpression attribute) {
		super(attribute.getName(), (species.getGamlType().isGridSpecies() ? Types.MATRIX : Types.LIST)
				.of(attribute.getGamlType()), attribute.isNotModifiable(),
				attribute instanceof IVarExpression.Agent agent ? agent.getDefinitionDescription() : null);
		this.species = species;
		this.attribute = attribute;
		spatial = species.getGamlType().isGridSpecies();
	}

	@Override
	public Object _value(final IScope scope) {
		return SpeciesAttributeValues.read(scope, species.value(scope), name, attribute.getGamlType(), spatial);
	}

	@Override
	public void setVal(final IScope scope, final Object value, final boolean create) {
		SpeciesAttributeValues.write(scope, species.value(scope), name, value, spatial);
	}

	@Override
	public IExpression getOwner() { return species; }

	@Override
	public IGamlDocumentation getDocumentation() { return attribute.getDocumentation(); }

	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		species.collectMetaInformation(meta);
		attribute.collectMetaInformation(meta);
	}

	@Override
	public void collectUsedVarsOf(final ITypeDescription species,
			final ICollector<IVarDescriptionUser> processed, final ICollector<IVariableDescription> result) {
		if (processed.contains(this)) return;
		processed.add(this);
		this.species.collectUsedVarsOf(species, processed, result);
		attribute.collectUsedVarsOf(species, processed, result);
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		final StringBuilder result = new StringBuilder();
		parenthesize(result, species);
		return result.append('[').append(name).append(']').toString();
	}
}
