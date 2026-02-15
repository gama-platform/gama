/*******************************************************************************************************
 *
 * PrimitiveStatement.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.statements;

import gama.annotations.doc;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.additions.IGamaHelper;
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator.NullValidator;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.skill.ISkill;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;

/**
 * The Class ActionCommand.
 *
 * @author drogoul
 */
@symbol (
		name = IKeyword.PRIMITIVE,
		kind = ISymbolKind.BEHAVIOR,
		with_sequence = true,
		with_args = true,
		internal = true,
		concept = { IConcept.ACTION, IConcept.SYSTEM })
@inside (
		kinds = { ISymbolKind.SPECIES, ISymbolKind.EXPERIMENT, ISymbolKind.MODEL },
		symbols = IKeyword.CHART)
@facets (
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.ID,
				optional = false,
				doc = { @doc ("The name of this primitive") }),
				@facet (
						name = IKeyword.VIRTUAL,
						type = IType.BOOL,
						optional = true,
						doc = { @doc ("Indicates if this primitive is virtual or not. A virtual primitive does not contain code and must be redefined in the species that implement the skill or extend the species that contain it") }),
				@facet (
						name = IKeyword.TYPE,
						type = IType.TYPE_ID,
						optional = true,
						doc = { @doc ("The type of the value returned by this primitive") }) },
		omissible = IKeyword.NAME)
// Necessary to avoid running the validator from ActionStatement
@validator (NullValidator.class)
@doc ("A primitve is an action written in Java (as opposed to GAML for regular actions")
@SuppressWarnings ({ "rawtypes" })
public class PrimitiveStatement extends ActionStatement {

	// Declaring a null validator because primites dont need to be checked

	/** The skill. */
	private ISkill skill = null;

	/** The helper. */
	private final IGamaHelper helper;

	/**
	 * Instantiates a new primitive statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public PrimitiveStatement(final IDescription desc) {
		super(desc);
		helper = getDescription().getHelper();
	}

	@Override
	public IActionDescription getDescription() { return (IActionDescription) description; }

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		scope.stackArguments(actualArgs.get());
		final IAgent agent = scope.getAgent();
		return helper.run(scope, agent, skill == null ? agent : skill);
	}

	@Override
	public void setRuntimeArgs(final IScope scope, final Arguments args) {
		actualArgs.set(args);
	}

	@Override
	public void setEnclosing(final ISymbol enclosing) {
		if (enclosing instanceof ISpecies spec) { skill = spec.getSkillInstanceFor(helper.getSkillClass()); }
	}

	@Override
	public void dispose() {
		skill = null;
		super.dispose();
	}

}
