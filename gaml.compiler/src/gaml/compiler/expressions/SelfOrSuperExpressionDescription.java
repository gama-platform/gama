/*******************************************************************************************************
 *
 * SelfOrSuperExpressionDescription.java, in gaml.compiler, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.expressions;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.constants.IGamlIssue;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.expressions.IVarExpression;
import gama.api.gaml.types.IType;
import gaml.compiler.descriptions.BasicExpressionDescription;

/**
 * A pure-Java {@link IExpressionDescription} that represents an implicit {@code self} or {@code super} target in a
 * {@code do} statement where no explicit target has been provided.
 *
 * <p>
 * This class deliberately avoids creating any synthetic EMF {@link EObject} nodes (in particular a
 * {@link gaml.compiler.gaml.ReservedLiteral} manufactured via
 * {@link gaml.compiler.gaml.GamlFactory#createReservedLiteral()}). Such synthetic nodes are detached from any parse
 * tree resource or container, which means:
 * </p>
 * <ul>
 * <li>Cross-reference resolution and serialisation tools that navigate {@code eContainer()} or {@code eResource()} will
 * receive {@code null} in unexpected places.</li>
 * <li>Storing the node as the {@code target} of an {@link gaml.compiler.descriptions.EcoreBasedExpressionDescription}
 * routes the compilation through the full EMF dispatch machinery ({@link gaml.compiler.gaml.util.GamlSwitch}) for what
 * is effectively a two-value flag.</li>
 * <li>It silently violates the "no live parse-tree mutation" invariant that was enforced for the
 * {@link FacetListExpressionDescription} case.</li>
 * </ul>
 *
 * <p>
 * Instead, {@link #compile(IDescription)} replicates the {@code returnSelfOrSuper} logic directly in pure Java: it
 * obtains the type context from the compilation context, resolves the appropriate {@link IType}, and delegates to
 * {@link GAML#getExpressionFactory()}.{@code createVar} — exactly what the EMF path ultimately did, but without the
 * intermediate detached {@link EObject}.
 * </p>
 *
 * <p>
 * The {@link #getTarget()} method always returns {@code null}: there is no meaningful EMF source node for a synthetic
 * implicit receiver. Call-sites that require an EMF anchor for error reporting should use the enclosing statement's
 * element instead.
 * </p>
 *
 * @author drogoul
 * @since 2026-03
 * @see FacetListExpressionDescription
 * @see gaml.compiler.factories.ExpressionDescriptionFactory#createSelfOrSuper(boolean)
 */
public class SelfOrSuperExpressionDescription extends BasicExpressionDescription {

	/**
	 * {@code true} if this description represents the {@code self} keyword; {@code false} for {@code super}.
	 * <p>
	 * Used during {@link #compile(IDescription)} to select the correct keyword string and {@link IVarExpression} scope
	 * constant.
	 * </p>
	 */
	private final boolean isSelf;

	/**
	 * Constructs a new description for an implicit {@code self} or {@code super} target.
	 *
	 * @param isSelf
	 *            {@code true} to represent the {@code self} keyword; {@code false} to represent {@code super}
	 */
	public SelfOrSuperExpressionDescription(final boolean isSelf) {
		super((IExpression) null); // no pre-compiled expression, no EObject target
		this.isSelf = isSelf;
	}

	/**
	 * Returns whether this description represents {@code self} (as opposed to {@code super}).
	 *
	 * @return {@code true} if this description represents {@code self}; {@code false} for {@code super}
	 */
	public boolean isSelf() { return isSelf; }

	/**
	 * Compiles the implicit {@code self} or {@code super} reference into an {@link IExpression} without touching the
	 * EMF AST.
	 *
	 * <p>
	 * The logic mirrors {@code GamlExpressionCompiler.returnSelfOrSuper}: the type context is obtained from the
	 * compilation {@code context} via {@link IDescription#getTypeContext()}, the appropriate {@link IType} is resolved
	 * (the parent type for {@code super}, the own type for {@code self}), and a variable expression is created via
	 * {@link GAML#getExpressionFactory()}.{@code createVar}.
	 * </p>
	 *
	 * <p>
	 * The compiled expression is cached in the inherited {@link BasicExpressionDescription#expression} field so that
	 * subsequent invocations return the same instance without re-compiling.
	 * </p>
	 *
	 * @param context
	 *            the {@link IDescription} providing the type context for compilation; must not be {@code null}
	 * @return the compiled {@link IExpression} for {@code self} or {@code super}, or {@code null} if the type context
	 *         cannot be determined
	 */
	@Override
	public IExpression compile(final IDescription context) {
		if (expression != null) return expression;
		final ITypeDescription typeContext = context.getTypeContext();
		if (typeContext == null) {
			context.error("Unable to determine the type context of " + (isSelf ? "self" : "super"), IGamlIssue.GENERAL,
					(EObject) null);
			return null;
		}
		if (isSelf) {
			final IType<?> type = typeContext.getGamlType();
			final String name = "self";
			expression = GAML.getExpressionFactory().createVar(name, type, true, IVarExpression.Category.SELF, null);
			return expression;
		}
		ITypeDescription superType = typeContext.getParent();
		if (superType == null) {
			context.error("Unable to determine the super type of " + typeContext.getName(), IGamlIssue.GENERAL,
					(EObject) null);
			return null;
		}
		final IType<?> type = superType.getGamlType();
		final String name = "super";
		expression = GAML.getExpressionFactory().createVar(name, type, true, IVarExpression.Category.SUPER, null);
		return expression;
	}

	/**
	 * Returns {@code null} — there is no EMF parse-tree node backing this synthetic description.
	 *
	 * <p>
	 * Call-sites that need an EMF anchor for error or hover markers should use the element of the enclosing statement
	 * instead.
	 * </p>
	 *
	 * @return {@code null}
	 */
	@Override
	public EObject getTarget() { return null; }

	/**
	 * No-op: this description has no EMF target to set.
	 *
	 * @param newTarget
	 *            ignored
	 */
	@Override
	public void setTarget(final EObject newTarget) { /* intentionally empty — no EMF backing */ }

	/**
	 * Returns a clean copy of this description, preserving the {@link #isSelf} flag.
	 * <p>
	 * The cached {@link #expression} is intentionally <em>not</em> copied so that the clone can be compiled
	 * independently in a different context if needed.
	 * </p>
	 *
	 * @return a new {@link SelfOrSuperExpressionDescription} with the same {@link #isSelf} flag and no cached
	 *         expression
	 */
	@Override
	public IExpressionDescription cleanCopy() {
		return new SelfOrSuperExpressionDescription(isSelf);
	}

	/**
	 * Returns the GAML keyword represented by this description: either {@code "self"} or {@code "super"}.
	 *
	 * @return {@code "self"} or {@code "super"}
	 */
	@Override
	public String toOwnString() {
		return isSelf ? "self" : "super";
	}
}