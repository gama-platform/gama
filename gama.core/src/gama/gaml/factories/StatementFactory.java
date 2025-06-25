/*******************************************************************************************************
 *
 * StatementFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.factories;

import org.eclipse.emf.ecore.EObject;

import gama.core.common.interfaces.IKeyword;
import gama.gaml.descriptions.ActionDescription;
import gama.gaml.descriptions.DoDescription;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.PrimitiveDescription;
import gama.gaml.descriptions.StatementDescription;
import gama.gaml.descriptions.StatementRemoteWithChildrenDescription;
import gama.gaml.descriptions.StatementWithChildrenDescription;
import gama.gaml.descriptions.SymbolProto;
import gama.gaml.statements.Facets;

/**
 * Written by drogoul Modified on 8 févr. 2010
 *
 * @todo Description
 *
 */
public class StatementFactory extends SymbolFactory implements IKeyword {

	@Override
	protected StatementDescription buildDescription(final String keyword, final Facets facets, final EObject element,
			final Iterable<IDescription> children, final IDescription enclosing, final SymbolProto proto) {
		if (proto.isPrimitive()) return new PrimitiveDescription(enclosing, element, children, facets, null);
		if (ACTION.equals(keyword)) return new ActionDescription(keyword, enclosing, children, element, facets);
		if (DO.equals(keyword) || INVOKE.equals(keyword))
			return new DoDescription(keyword, enclosing, children, proto.hasArgs(), element, facets, null);
		if (proto.hasSequence() && children != null) {
			if (proto.isRemoteContext()) return new StatementRemoteWithChildrenDescription(keyword, enclosing, children,
					proto.hasArgs(), element, facets, null);
			return new StatementWithChildrenDescription(keyword, enclosing, children, proto.hasArgs(), element, facets,
					null);
		}
		return new StatementDescription(keyword, enclosing, proto.hasArgs(), element, facets, null);
	}

}
