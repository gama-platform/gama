/*******************************************************************************************************
 *
 * IDescriptionValidator.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.validation.IValidator;
import gama.api.gaml.expressions.IExpression;

/**
 * Class IDescriptionValidator. This interface is intended to be used for individual validation of symbols. An instance
 * is typically known by a SymbolProto and called after the core of the validation has made its job.
 *
 * @author drogoul
 * @since 13 sept. 2013
 *
 */
@SuppressWarnings ({ "rawtypes" })
public interface IDescriptionValidator<T extends IDescription> extends IValidator {

	/** The reserved. */
	/**
	 * Called at the end of the validation process. The enclosing description, the children and the facets of the
	 * description have all been already validated (and their expressions compiled), so everything is accessible here to
	 * make a finer validation with respect to the specificites of the symbol. This interface is not supposed to change
	 * the description unless it is absolutely necessary. It is supposed to attach warnings and errors to the
	 * description instead. Alternatively, developers may want to override validate(IDescription, EObject,
	 * IExpression[]), which allows to veto the validation by returning false
	 *
	 * @param description
	 */
	void validate(T description);

	/**
	 * In that particular implementation, arguments will always be empty. Returning false will veto the validation
	 * process
	 */
	@SuppressWarnings ("unchecked")
	@Override
	default boolean validate(final IDescription description, final EObject emfContext, final IExpression... arguments) {
		validate((T) description);
		return true;
	}

	/**
	 * Swap.
	 *
	 * @param desc
	 *            the desc
	 * @param oldFacet
	 *            the old
	 * @param newFacet
	 *            the next
	 */
	default void swap(final IDescription desc, final String oldFacet, final String newFacet) {
		if (desc.hasFacet(oldFacet)) {
			desc.setFacetExprDescription(newFacet, desc.getFacet(oldFacet));
			desc.removeFacets(oldFacet);
		}
	}

	/**
	 * The Class NullValidator.
	 */
	public static class NullValidator implements IDescriptionValidator {

		/**
		 * Verifies that the name is valid (non reserved, non type and non species)
		 *
		 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.gaml.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription cd) {}
	}

}
