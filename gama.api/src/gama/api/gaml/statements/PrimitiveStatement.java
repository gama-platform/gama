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
 * Implementation of primitive actions - actions defined in Java rather than GAML.
 * 
 * <p>
 * Primitive actions are built-in operations provided by GAMA's core or plugins. Unlike regular actions defined in
 * GAML, primitives are implemented in Java for performance and access to internal APIs. They are typically defined in
 * skills and can be invoked like regular actions.
 * </p>
 * 
 * <h2>Key Differences from Regular Actions</h2>
 * <ul>
 * <li>Implemented in Java using {@link IGamaHelper} instead of GAML statements</li>
 * <li>Can access low-level GAMA APIs not available in GAML</li>
 * <li>Generally faster due to native execution</li>
 * <li>Defined using Java annotations in skill classes</li>
 * <li>Don't require compilation validation (use NullValidator)</li>
 * </ul>
 * 
 * <h2>Example Java Implementation</h2>
 * <pre>
 * {@code
 * @action(name = "wander", args = {
 *     @arg(name = "amplitude", type = IType.INT, optional = true)
 * })
 * public Object primWander(IScope scope) {
 *     // Java implementation
 *     return null;
 * }
 * }
 * </pre>
 * 
 * <h2>Example GAML Usage</h2>
 * <pre>
 * {@code
 * species animal skills: [moving] {
 *     reflex move {
 *         // 'wander' is a primitive action from the moving skill
 *         do wander amplitude: 120;
 *     }
 * }
 * }
 * </pre>
 * 
 * <h2>Skill Association</h2>
 * <p>
 * Primitives are associated with skills. When executed, the primitive finds the appropriate skill instance from the
 * species hierarchy and uses it as the execution context.
 * </p>
 * 
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @see ActionStatement
 * @see ISkill
 * @see IGamaHelper
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

	/** The skill instance that provides this primitive action (null if it's the agent itself). */
	private ISkill skill = null;

	/** The Java helper that implements the primitive's logic. */
	private final IGamaHelper helper;

	/**
	 * Constructs a new primitive statement.
	 * 
	 * <p>
	 * The helper is extracted from the description and will be used to execute the primitive's Java code.
	 * </p>
	 *
	 * @param desc
	 *            the primitive description
	 */
	public PrimitiveStatement(final IDescription desc) {
		super(desc);
		helper = getDescription().getHelper();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return the action description with helper information
	 */
	@Override
	public IActionDescription getDescription() { return (IActionDescription) description; }

	/**
	 * Executes the primitive by invoking its Java helper.
	 * 
	 * <p>
	 * Arguments are pushed onto the scope stack, then the helper is invoked with either the agent or the skill as
	 * context depending on whether a skill provides this primitive.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @return the result of the helper execution
	 * @throws GamaRuntimeException
	 *             if execution fails
	 */
	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		scope.stackArguments(actualArgs.get());
		final IAgent agent = scope.getAgent();
		return helper.run(scope, agent, skill == null ? agent : skill);
	}

	/**
	 * Sets the runtime arguments without complementing with formal arguments.
	 * 
	 * <p>
	 * Primitives handle their arguments directly via the helper, so no complementing is needed.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope
	 * @param args
	 *            the runtime arguments
	 */
	@Override
	public void setRuntimeArgs(final IScope scope, final Arguments args) {
		actualArgs.set(args);
	}

	/**
	 * Identifies and caches the skill instance that provides this primitive.
	 * 
	 * <p>
	 * When the enclosing symbol (species) is set, this method searches for the skill class that defines this primitive
	 * and caches its instance for use during execution.
	 * </p>
	 *
	 * @param enclosing
	 *            the enclosing species
	 */
	@Override
	public void setEnclosing(final ISymbol enclosing) {
		if (enclosing instanceof ISpecies spec) { skill = spec.getSkillInstanceFor(helper.getSkillClass()); }
	}

	/**
	 * Disposes of this primitive statement and clears the skill reference.
	 */
	@Override
	public void dispose() {
		skill = null;
		super.dispose();
	}

}
