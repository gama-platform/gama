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
package gaml.compiler.gaml.descriptions;

import static gama.annotations.constants.IKeyword.ACTION;
import static gama.annotations.constants.IKeyword.WITH;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.constants.IKeyword;
import gama.api.additions.registries.ArtefactRegistry;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.constants.IGamlIssue;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.symbols.Facets;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;

/**
 * The Class StatementWithChildrenDescription.
 */
public class DoDescription extends StatementDescription {

	/** The action. */
	IActionDescription action;

	/** The declaration context. */
	ITypeDescription lookupContext;

	/** The actual target species. */
	ITypeDescription actualTargetSpecies;

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
	public DoDescription(final String keyword, final IDescription superDesc, final boolean hasArgs,
			final EObject source, final Facets facets, final Arguments alreadyComputedArgs) {
		super(keyword, superDesc, hasArgs, source, facets, alreadyComputedArgs);
		setIf(Flag.IsSuperInvocation, IKeyword.INVOKE.equals(keyword));
	}

	@Override
	protected IExpression createVarWithTypes(final String facetName) {
		compileTypeProviderFacets();
		return getEnclosingDescription() instanceof StatementWithChildrenDescription sc
				? sc.addTemp(this, null, getLitteral(facetName), getGamlType()) : null;
	}

	@Override
	public IType<?> getGamlType() {
		IActionDescription a = getAction();
		return a == null ? Types.NO_TYPE : a.getGamlType();
	}

	@Override
	public DoDescription copy(final IDescription into) {
		final DoDescription desc = new DoDescription(getKeyword(), into, false, element, getFacetsCopy(),
				passedArgs == null ? null : passedArgs.cleanCopy());
		desc.originName = getOriginName();
		return desc;
	}

	@Override
	protected Arguments createArgs() {
		if (!hasFacets() || !hasFacetsNotIn(ArtefactRegistry.getDoFacets())) {
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
			if (!ArtefactRegistry.getDoFacets().contains(facet)) { args.put(facet, b); }
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
		final IActionDescription executer = getAction();
		if (executer != null) { executer.verifyArgs(this, passedArgs); }
		return passedArgs;
	}

	/**
	 * Checks if is super invocation.
	 *
	 * @return true, if is super invocation
	 */
	@Override
	public boolean isSuperInvocation() { return isSet(Flag.IsSuperInvocation); }

	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	private IActionDescription getAction() {
		if (action == null) {
			final String actionName = getLitteral(ACTION);
			if (actionName == null) return null;
			actualTargetSpecies = getDescriptionDeclaringAction(actionName, isSuperInvocation());
			if (actualTargetSpecies != null) { action = actualTargetSpecies.getAction(actionName); }
		}
		return action;
	}

	/**
	 * Gets the description that declares an action with the given name. Searches up the enclosing description
	 * hierarchy.
	 *
	 * @param aName
	 *            the action name to find
	 * @param superInvocation
	 *            whether to check super types
	 * @return the description that declares the action, or null if not found
	 */
	@Override
	public ITypeDescription getDescriptionDeclaringAction(final String aName, final boolean superInvocation) {
		IExpressionDescription target = getFacet(IKeyword.SYNTHETIC_DO_TARGET);
		if (target == null) {
			lookupContext = getSpeciesContext();
			return super.getDescriptionDeclaringAction(aName, superInvocation);
		}
		// Handle the case where target is not null (e.g., target-specific action)
		IExpression agent = target.compile(this);
		if (agent == null) return null;
		lookupContext = agent.getGamlType().getDenotedSpecies();
		if (lookupContext != null) return lookupContext.getDescriptionDeclaringAction(aName, superInvocation);
		return null;
	}

	/**
	 * Gets the declaration context name.
	 *
	 * @return the declaration context name
	 */
	private String getLookupContextName() {
		return lookupContext == null ? getSpeciesContext().getName() : lookupContext.getName();
	}

	/**
	 * Gets the actual species name.
	 *
	 * @return the actual species name
	 */
	private String getActualSpeciesName() {
		return actualTargetSpecies == null ? getSpeciesContext().getName() : actualTargetSpecies.getName();
	}

	@Override
	public IDescription validate() {
		IDescription result = super.validate();
		if (result == null) return null;
		IActionDescription a = getAction();
		if (a == null) {
			String actionName = getLitteral(ACTION);
			error("Action " + actionName + " does not exist in " + getLookupContextName(), IGamlIssue.UNKNOWN_ACTION,
					ACTION, actionName, getLookupContextName());
			return null;
		}
		if (a instanceof PrimitiveDescription pd) {
			final String dep = pd.getDeprecated();
			if (dep != null) { warning("Action " + action + " is deprecated: " + dep, IGamlIssue.DEPRECATED, ACTION); }
		}
		setFacetExprDescription(IKeyword.SYNTHETIC_DO_TARGET_SPECIES,
				GAML.getExpressionDescriptionFactory().createLabel(getActualSpeciesName()));
		return result;
	}

	@Override
	public boolean isInvocation() { return true; }

}