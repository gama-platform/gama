/*******************************************************************************************************
 *
 * DoDescription.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.descriptions;

import static gama.api.compilation.IInternalFacets.INTERNAL_FUNCTION;
import static gama.api.compilation.IInternalFacets.INTERNAL_TARGET;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.constants.IGamlIssue;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gaml.compiler.EGaml;
import gaml.compiler.expressions.ActionCallOperator;
import gaml.compiler.expressions.GamlExpressionCompiler;

/**
 * Description of a {@code do} / {@code invoke} / {@code .} statement.
 *
 * <p>
 * All three syntactic forms of action calls are normalised by
 * {@link gaml.compiler.parsing.GamlSyntacticConverter#processDo} into a uniform set of facets:
 * </p>
 * <ul>
 * <li>{@link IInternalFacets#INTERNAL_TARGET} – the receiver expression (always present; synthetic {@code self} /
 * {@code super} for the implicit-self forms).</li>
 * <li>{@link IInternalFacets#INTERNAL_FUNCTION} – the {@code Function} EMF node carrying action name + argument
 * list.</li>
 * <li>{@link IInternalFacets#INTERNAL_NAME} – a label with the bare action name.</li>
 * </ul>
 *
 * <p>
 * During {@link #validate()}, both facets are compiled via
 * {@link GamlExpressionCompiler#compileActionCall(EObject, EObject, IDescription)} which always produces a single
 * {@link ActionCallOperator}. That operator is then stored back on the {@code INTERNAL_FUNCTION} facet. The resulting
 * {@link ActionCallOperator} contains both the resolved action and its arguments, so
 * {@link gama.gaml.statements.DoStatement} can simply delegate to {@code function.value(scope)}.
 * </p>
 *
 * <h2>Thread safety</h2>
 * <p>
 * This class is not thread-safe by itself; it is created and used on a single compilation thread. The produced
 * {@link ActionCallOperator} is thread-safe (see that class's documentation).
 * </p>
 *
 * @author drogoul
 * @see gaml.compiler.parsing.GamlSyntacticConverter#processDo
 * @see ActionCallOperator
 * @see gama.gaml.statements.DoStatement
 */
public class DoDescription extends StatementDescription {

	/**
	 * The compile-time action description, lazily resolved in {@link #getAction()}. Used only for the post-validation
	 * deprecation check.
	 */
	IActionDescription action;

	/**
	 * The type-description in which the action lookup was performed. Set as a side-effect of
	 * {@link #getDescriptionDeclaringAction(String, boolean)} and used in error messages.
	 */
	ITypeDescription lookupContext;

	/**
	 * Creates a new {@code DoDescription}.
	 *
	 * @param keyword
	 *            the statement keyword ({@code do}, {@code invoke}, or {@code .})
	 * @param superDesc
	 *            the enclosing description
	 * @param hasArgs
	 *            whether the statement carries child arg descriptions
	 * @param source
	 *            the source EMF element
	 * @param facets
	 *            the pre-computed facets
	 * @param alreadyComputedArgs
	 *            pre-built arguments (used when copying descriptions)
	 */
	public DoDescription(final String keyword, final IDescription superDesc, final boolean hasArgs,
			final EObject source, final Facets facets, final Arguments alreadyComputedArgs) {
		super(keyword, superDesc, hasArgs, source, facets, alreadyComputedArgs);
		setIf(Flag.IsSuperInvocation, IKeyword.INVOKE.equals(keyword));
	}

	/**
	 * Returns the GAML type produced by this action call. Delegates to the resolved {@link IActionDescription}.
	 *
	 * @return the return type of the action, or {@link Types#NO_TYPE} when the action cannot be resolved
	 */
	@Override
	public IType<?> getGamlType() {
		final IActionDescription a = getAction();
		return a == null ? Types.NO_TYPE : a.getGamlType();
	}

	/**
	 * Produces a deep copy of this description rooted under a new parent.
	 *
	 * @param into
	 *            the new parent description
	 * @return a fresh {@code DoDescription} with the same keyword, source, and facets
	 */
	@Override
	public DoDescription copy(final IDescription into) {
		final DoDescription desc = new DoDescription(getKeyword(), into, false, element, getFacetsCopy(),
				passedArgs == null ? null : passedArgs.cleanCopy());
		desc.originName = getOriginName();
		return desc;
	}

	/**
	 * Returns the {@link IActionDescription} for the action being called. The lookup is performed lazily and cached.
	 *
	 * <p>
	 * As a side-effect, {@link #lookupContext} is populated so that error messages can refer to it.
	 * </p>
	 *
	 * @return the action description, or {@code null} when the action cannot be found
	 */
	private IActionDescription getAction() {
		if (action != null) return action;
		final IExpressionDescription functionFacet = getFacet(INTERNAL_FUNCTION);
		if (functionFacet == null) return null;
		// Derive the action name from the compiled expression if available, otherwise from the EMF target
		final IExpression compiled = functionFacet.getExpression();
		final String actionName;
		if (compiled instanceof ActionCallOperator aco) {
			actionName = aco.getName();
		} else {
			actionName = EGaml.getInstance().getKeyOf(functionFacet.getTarget());
		}
		if (actionName == null) return null;
		lookupContext = getDescriptionDeclaringAction(actionName, isSuperInvocation());
		if (lookupContext != null) { action = lookupContext.getAction(actionName); }
		return action;
	}

	/**
	 * Resolves the {@link ITypeDescription} that declares an action with the given name.
	 *
	 * <p>
	 * When an explicit {@link IInternalFacets#INTERNAL_TARGET} facet is present (i.e. the {@code target.action(...)}
	 * form), the target expression is compiled to determine the receiver's type, and the lookup is performed on that
	 * type. Otherwise the lookup starts from {@link IDescription#getTypeContext()}.
	 * </p>
	 *
	 * @param aName
	 *            the bare action name to look up
	 * @param superInvocation
	 *            {@code true} when this is a {@code super.action()} / {@code invoke} call
	 * @return the type description declaring the action, or {@code null} when not found
	 */
	@Override
	public ITypeDescription getDescriptionDeclaringAction(final String aName, final boolean superInvocation) {
		final IExpressionDescription target = getFacet(INTERNAL_TARGET);
		if (target == null) {
			lookupContext = getTypeContext();
			return super.getDescriptionDeclaringAction(aName, superInvocation);
		}
		final IExpression agent = target.compile(this);
		if (agent == null) return null;
		lookupContext = agent.getGamlType().getDenotedSpecies();
		if (lookupContext != null) return lookupContext.getDescriptionDeclaringAction(aName, superInvocation);
		return null;
	}

	// -------------------------------------------------------------------------
	// Helpers for error messages
	// -------------------------------------------------------------------------

	/**
	 * Returns the name of the type context used for the action lookup, for use in error messages.
	 *
	 * @return the lookup-context species name, or the enclosing type-context name when no lookup was performed yet
	 */
	private String getLookupContextName() {
		return lookupContext == null ? getTypeContext().getName() : lookupContext.getName();
	}

	// -------------------------------------------------------------------------
	// Core validation
	// -------------------------------------------------------------------------

	/**
	 * Validates this description and compiles the action call into an {@link ActionCallOperator}.
	 *
	 * <p>
	 * <b>Unified compilation path.</b> Because {@code processDo} always sets both {@code INTERNAL_TARGET} and
	 * {@code INTERNAL_FUNCTION}, we can always route through
	 * {@link GamlExpressionCompiler#compileActionCall(EObject, EObject, IDescription)} which uses the same
	 * {@link gaml.compiler.expressions.ExpressionCompilationSwitch#compileFieldAccess} logic as the
	 * {@code target.action()} expression form. The result is a single {@link ActionCallOperator} that encapsulates the
	 * target, the action description, and the compiled arguments.
	 * </p>
	 *
	 * <p>
	 * After successful compilation, the {@link ActionCallOperator} is stored back on the {@code INTERNAL_FUNCTION}
	 * facet so that {@link gama.gaml.statements.DoStatement#privateExecuteIn} can evaluate it via
	 * {@code function.value(scope)}.
	 * </p>
	 *
	 * @return this description if validation succeeded, {@code null} on fatal error
	 */
	@Override
	public IDescription validate() {
		final IExpressionDescription functionFacet = getFacet(INTERNAL_FUNCTION);
		if (functionFacet == null) {
			error("Action " + getAction() + " does not exist in " + getLookupContextName(), IGamlIssue.UNKNOWN_ACTION);
			return null;
		}
		final IExpressionDescription targetFacet = getFacet(INTERNAL_TARGET);
		if (functionFacet instanceof gaml.compiler.expressions.FacetListExpressionDescription || targetFacet == null) {
			// Deprecated facet-based form: FacetListExpressionDescription.compile() handles everything
			// (target resolution + argument compilation) without touching the EMF parse tree.
			functionFacet.compile(this);
		} else {
			// Unified path for functional and dot-notation forms.
			IExpression compiled;
			if (targetFacet.getTarget() == null) {
				// The target has no EObject backing (e.g. SelfOrSuperExpressionDescription):
				// We first verify if this is not an operator call (see
				// https://github.com/gama-platform/gama/issues/1016)
				compiled = GamlExpressionCompiler.getInstance().compile(functionFacet, this);
				if (compiled == null) {
					// compile it to an IExpression first, then use the pre-compiled-owner overload.
					final IExpression targetExpr = targetFacet.compile(this);
					compiled = GamlExpressionCompiler.getInstance().compileActionCall(targetExpr,
							functionFacet.getTarget(), this);
				}
			} else {
				compiled = GamlExpressionCompiler.getInstance().compileActionCall(targetFacet.getTarget(),
						functionFacet.getTarget(), this);
			}
			functionFacet.setExpression(compiled);
			// Pre-compile INTERNAL_TARGET so validateFacets() (called by super.validate() below)
			// finds both internal facets already compiled and never re-compiles their synthetic EObjects.
			if (targetFacet.getExpression() == null) { targetFacet.compile(this); }
		}

		final IDescription result = super.validate();
		if (result == null) return null;

		// Deprecation check for primitives
		if (getAction() instanceof PrimitiveDescription pd) {
			final String dep = pd.getDeprecated();
			if (dep != null) {
				warning("Action " + getAction().getName() + " is deprecated: " + dep, IGamlIssue.DEPRECATED);
			}
		}

		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isInvocation() { return true; }

}
