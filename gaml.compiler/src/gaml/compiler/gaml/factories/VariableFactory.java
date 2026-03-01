/*******************************************************************************************************
 *
 * VariableFactory.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.factories;

import static gama.api.constants.IKeyword.ON_CHANGE;
import static gama.api.constants.IKeyword.VAR;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.support.ISymbolKind;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.factories.ISymbolDescriptionFactory;
import gama.api.constants.IKeyword;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.symbols.Facets;
import gaml.compiler.gaml.descriptions.VariableDescription;

/**
 * Written by drogoul Modified on 26 nov. 2008
 *
 * @todo Description
 */
public class VariableFactory implements ISymbolDescriptionFactory {

	/** The instance. */
	private static VariableFactory INSTANCE = new VariableFactory();

	/**
	 * Gets the single instance of VariableFactory.
	 *
	 * @return single instance of VariableFactory
	 */
	public static VariableFactory getInstance() { return INSTANCE; }

	/**
	 * Instantiates a new variable factory.
	 */
	private VariableFactory() {}

	@Override
	public IDescription buildDescription(final String keyword, final Facets facets, final EObject element,
			final Iterable<IDescription> children, final IDescription enclosing, final IArtefact.Symbol artefact) {
		if (IKeyword.PARAMETER.equals(keyword)) {

			final Map<String, IArtefact.Facet> possibleFacets = artefact.getPossibleFacets();
			// We copy the relevant facets from the targeted var of the
			// parameter
			IVariableDescription targetedVar = enclosing.getModelDescription().getAttribute(facets.getLabel(VAR));
			if (targetedVar == null && enclosing instanceof IExperimentDescription exp) {
				targetedVar = exp.getAttribute(facets.getLabel(VAR));
			}
			if (targetedVar != null) {
				for (final String key : possibleFacets.keySet()) {
					if (ON_CHANGE.equals(key)) { continue; }
					final IExpressionDescription expr = targetedVar.getFacet(key);
					if (expr != null) {
						IExpressionDescription copy = expr.cleanCopy();
						facets.putIfAbsent(key, copy);
					}
				}

			}
		}
		return new VariableDescription(keyword, enclosing, element, facets);
	}

	@Override
	public ISymbolKind[] getKinds() {
		return new ISymbolKind[] { ISymbolKind.CONTAINER, ISymbolKind.NUMBER, ISymbolKind.REGULAR,
				ISymbolKind.PARAMETER };
	}

}
