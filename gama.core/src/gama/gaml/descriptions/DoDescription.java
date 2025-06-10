/*******************************************************************************************************
 *
 * DoDescription.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.descriptions;

import static gama.gaml.statements.DoStatement.DO_FACETS;

import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import gama.gaml.compilation.GAML;
import gama.gaml.expressions.IExpression;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.statements.Arguments;
import gama.gaml.statements.Facets;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class StatementWithChildrenDescription.
 */
public class DoDescription extends StatementWithChildrenDescription {

	/** The action. */
	ActionDescription action;

	/** The declaration context. */
	TypeDescription declarationContext;

	/**
	 * Instantiates a new statement representing the do
	 *
	 * @param keyword
	 *            the keyword
	 * @param superDesc
	 *            the super desc
	 * @param cp
	 *            the cp
	 * @param hasArgs
	 *            the has args
	 * @param source
	 *            the source
	 * @param facets
	 *            the facets
	 * @param alreadyComputedArgs
	 *            the already computed args
	 */
	public DoDescription(final String keyword, final IDescription superDesc, final Iterable<IDescription> cp,
			final boolean hasArgs, final EObject source, final Facets facets, final Arguments alreadyComputedArgs) {
		super(keyword, superDesc, cp, hasArgs, source, facets, alreadyComputedArgs);
		setIf(Flag.IsSuperInvocation, INVOKE.equals(keyword));
	}

	@Override
	public boolean visitChildren(final DescriptionVisitor<IDescription> visitor) {
		return true;
	}

	@Override
	public boolean visitOwnChildrenRecursively(final DescriptionVisitor<IDescription> visitor) {
		return true;
	}

	@Override
	public boolean visitOwnChildren(final DescriptionVisitor<IDescription> visitor) {
		return true;
	}

	@Override
	public Iterable<IDescription> getOwnChildren() { return Collections.EMPTY_LIST; }

	@Override
	public List<IDescription> getChildren() { return Collections.EMPTY_LIST; }

	@Override
	public IExpression addTemp(final IDescription declaration, final String facet, final String name,
			final IType<?> type) {
		return getEnclosingDescription() instanceof StatementWithChildrenDescription sc
				? sc.addTemp(declaration, null, name, getGamlType()) : null;
	}

	@Override
	protected IExpression createVarWithTypes(final String facetName) {
		compileTypeProviderFacets();
		return addTemp(this, facetName, getLitteral(facetName), getGamlType());
	}

	@Override
	public IType<?> getGamlType() {
		ActionDescription a = getAction();
		return a == null ? Types.NO_TYPE : a.getGamlType();
	}

	@Override
	public DoDescription copy(final IDescription into) {
		final DoDescription desc = new DoDescription(getKeyword(), into, null, false, element, getFacetsCopy(),
				passedArgs == null ? null : passedArgs.cleanCopy());
		desc.originName = getOriginName();
		return desc;
	}

	@Override
	protected Arguments createArgs() {
		if (!hasFacets() || !hasFacetsNotIn(DO_FACETS)) {
			if (hasFacet(WITH)) {
				try {
					return GAML.getExpressionFactory().createArgumentMap(getAction(), getFacet(WITH), this);
				} finally {
					// We remove it before validation... a bit dirty
					removeFacets(WITH);
				}
			}
			return null;
		}
		final Arguments args = new Arguments();
		visitFacets((facet, b) -> {
			if (!DO_FACETS.contains(facet)) { args.put(facet, b); }
			return true;
		});
		return args;
	}

	/**
	 * Validate passed args.
	 *
	 * @return the arguments
	 */
	@Override
	protected Arguments validatePassedArgs() {
		super.validatePassedArgs();
		final ActionDescription executer = getAction();
		if (executer != null) { executer.verifyArgs(this, passedArgs); }
		return passedArgs;
	}

	/**
	 * Checks if is super invocation.
	 *
	 * @return true, if is super invocation
	 */
	public boolean isSuperInvocation() { return isSet(Flag.IsSuperInvocation); }

	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	private ActionDescription getAction() {
		if (action == null) {
			final String actionName = getLitteral(ACTION);
			if (actionName == null) return null;
			declarationContext = (TypeDescription) getDescriptionDeclaringAction(actionName, isSuperInvocation());
			if (declarationContext != null) { action = declarationContext.getAction(actionName); }
		}
		return action;
	}

	/**
	 * Gets the declaration context name.
	 *
	 * @return the declaration context name
	 */
	private String getDeclarationContextName() {
		return declarationContext == null ? getSpeciesContext().getName() : declarationContext.getName();
	}

	@Override
	public IDescription validate() {
		IDescription result = super.validate();
		if (result == null) return null;
		ActionDescription a = getAction();
		if (a == null) {
			String actionName = getLitteral(ACTION);
			error("Action " + actionName + " does not exist in " + getDeclarationContextName(),
					IGamlIssue.UNKNOWN_ACTION, ACTION, actionName, getDeclarationContextName());
			return null;
		}
		if (a instanceof PrimitiveDescription pd) {
			final String dep = pd.getDeprecated();
			if (dep != null) { warning("Action " + action + " is deprecated: " + dep, IGamlIssue.DEPRECATED, ACTION); }
		}
		return result;
	}

	@Override
	public boolean isInvocation() { return true; }

}