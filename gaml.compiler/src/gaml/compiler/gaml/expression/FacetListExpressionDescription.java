/*******************************************************************************************************
 *
 * FacetListExpressionDescription.java, in gaml.compiler, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.expression;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gaml.compiler.gaml.Expression;
import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.descriptions.BasicExpressionDescription;

/**
 * A pure Java-side carrier that wraps an {@link EList}&lt;{@link Facet}&gt; as an {@link IExpressionDescription}
 * so it can be stored in a symbol's facet map and later retrieved by {@link gaml.compiler.gaml.descriptions.DoDescription}.
 *
 * <p>
 * This is used exclusively for the <em>deprecated</em> facet-based {@code do} form
 * ({@code do action arg1: val1 arg2: val2;}). Storing the facets here avoids constructing any synthetic EMF nodes
 * (such as {@link gaml.compiler.gaml.Function}/{@link gaml.compiler.gaml.Parameter}) whose containment setters
 * would reparent the live value expressions and fire change-notifications on the resource, causing ghost
 * re-validations.
 * </p>
 *
 * <p>
 * The {@link #getNameExpression()} and {@link #getFacets()} accessors return the original, unmodified EMF objects
 * from the real parse tree; they are only read — never re-parented.
 * </p>
 *
 * @author drogoul
 * @see gaml.compiler.gaml.descriptions.DoDescription#validate()
 * @see ExpressionCompilationSwitch#compileActionCallFromFacets
 */
public class FacetListExpressionDescription extends BasicExpressionDescription {

	/** The action-name expression from the live parse tree (e.g. a {@link gaml.compiler.gaml.VariableRef}). */
	private final Expression nameExpression;

	/** The raw facet list from the parsed {@code S_Do} statement — never modified. */
	private final EList<Facet> facets;

	/**
	 * Constructs a new description carrying the deprecated-form arguments.
	 *
	 * @param nameExpression
	 *            the action-name {@link Expression} node; used as the error/hover anchor and as the target for
	 *            {@link #getTarget()}
	 * @param facets
	 *            the raw facet list from the {@code S_Do} statement; must not be {@code null}
	 */
	public FacetListExpressionDescription(final Expression nameExpression, final EList<Facet> facets) {
		super(nameExpression); // sets target = nameExpression, which is in the real resource
		this.nameExpression = nameExpression;
		this.facets = facets;
	}

	/**
	 * Returns the action-name expression from the live parse tree.
	 *
	 * @return the action-name {@link Expression}; never {@code null}
	 */
	public Expression getNameExpression() { return nameExpression; }

	/**
	 * Returns the raw facet list from the parsed {@code S_Do} statement.
	 *
	 * @return the {@link EList} of {@link Facet} objects; never {@code null}
	 */
	public EList<Facet> getFacets() { return facets; }

	/**
	 * Returns the action-name expression as the EMF anchor (same as the underlying {@link #getTarget()}).
	 *
	 * @return the action-name {@link Expression}
	 */
	@Override
	public EObject getTarget() { return nameExpression; }

	/**
	 * Compiles the action call by delegating to
	 * {@link GamlExpressionCompiler#compileActionCallFromFacets(Expression, EList, IDescription)}.
	 *
	 * <p>
	 * The result is cached in {@link #expression} so that subsequent calls (e.g. from
	 * {@link gama.gaml.descriptions.SymbolDescription#validateFacets()}) return the cached
	 * {@link ActionCallOperator} without re-compiling.
	 * </p>
	 *
	 * @param context
	 *            the compilation context
	 * @return the compiled {@link ActionCallOperator}, or {@code null} on failure
	 */
	@Override
	public IExpression compile(final IDescription context) {
		if (expression == null) {
			expression = GamlExpressionCompiler.getInstance().compileActionCallFromFacets(nameExpression, facets,
					context);
		}
		return expression;
	}

	@Override
	public IExpressionDescription cleanCopy() {
		final FacetListExpressionDescription copy = new FacetListExpressionDescription(nameExpression, facets);
		copy.expression = expression;
		return copy;
	}

	@Override
	public String toOwnString() {
		return nameExpression != null ? nameExpression.toString() : ""; //$NON-NLS-1$
	}
}
