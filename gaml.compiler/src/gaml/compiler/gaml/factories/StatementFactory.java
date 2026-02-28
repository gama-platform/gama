/*******************************************************************************************************
 *
 * StatementFactory.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.factories;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.support.ISymbolKind;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.compilation.factories.ISymbolDescriptionFactory;
import gama.api.constants.IKeyword;
import gama.api.gaml.symbols.Facets;
import gaml.compiler.gaml.descriptions.ActionDescription;
import gaml.compiler.gaml.descriptions.DoDescription;
import gaml.compiler.gaml.descriptions.PrimitiveDescription;
import gaml.compiler.gaml.descriptions.StatementDescription;
import gaml.compiler.gaml.descriptions.StatementRemoteWithChildrenDescription;
import gaml.compiler.gaml.descriptions.StatementWithChildrenDescription;

/**
 * Written by drogoul Modified on 8 févr. 2010
 *
 * @todo Description
 *
 */
public class StatementFactory implements ISymbolDescriptionFactory {

	/** The instance. */
	private static StatementFactory INSTANCE;

	/**
	 * Gets the single instance of StatementFactory.
	 *
	 * @return single instance of StatementFactory
	 */
	public static StatementFactory getInstance() {
		if (INSTANCE == null) { INSTANCE = new StatementFactory(); }
		return INSTANCE;
	}

	/**
	 * Instantiates a new statement factory.
	 */
	private StatementFactory() {}

	/**
	 * Builds the description.
	 *
	 * @param keyword
	 *            the keyword
	 * @param facets
	 *            the facets
	 * @param element
	 *            the element
	 * @param children
	 *            the children
	 * @param enclosing
	 *            the enclosing
	 * @param artefact
	 *            the artefact
	 * @return the statement description
	 */
	@Override
	public IStatementDescription buildDescription(final String keyword, final Facets facets, final EObject element,
			final Iterable<IDescription> children, final IDescription enclosing, final IArtefact.Symbol proto) {
		if (proto.isPrimitive()) return new PrimitiveDescription(enclosing, element, children, facets, null);
		if (IKeyword.ACTION.equals(keyword))
			return new ActionDescription(keyword, enclosing, children, element, facets);
		if (IKeyword.DO.equals(keyword) || IKeyword.INVOKE.equals(keyword))
			return new DoDescription(keyword, enclosing, children, proto.hasArgs(), element, facets, null);
		if (proto.hasSequence() && children != null) {
			if (proto.isRemoteContext()) return new StatementRemoteWithChildrenDescription(keyword, enclosing, children,
					proto.hasArgs(), element, facets, null);
			return new StatementWithChildrenDescription(keyword, enclosing, children, proto.hasArgs(), element, facets,
					null);
		}
		return new StatementDescription(keyword, enclosing, proto.hasArgs(), element, facets, null);
	}

	@Override
	public ISymbolKind[] getKinds() {
		return new ISymbolKind[] { ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.SINGLE_STATEMENT, ISymbolKind.BEHAVIOR,
				ISymbolKind.ACTION, ISymbolKind.LAYER, ISymbolKind.BATCH_METHOD, ISymbolKind.OUTPUT };
	}

}
