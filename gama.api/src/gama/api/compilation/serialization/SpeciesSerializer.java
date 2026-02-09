/*******************************************************************************************************
 *
 * SpeciesSerializer.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.serialization;

import java.util.Collection;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.utils.GamlProperties;

/**
 * The Class SpeciesSerializer.
 */
public class SpeciesSerializer implements ISymbolSerializer {

	@Override
	public String serializeFacetValue(final IDescription s, final String key, final boolean includingBuiltIn) {
		if (SKILLS.equals(key)) {
			final IExpressionDescription ed = s.getFacet(key);
			if (ed == null) return null;
			final Collection<String> strings = ed.getStrings(s, true);
			return strings.toString();
		}
		return ISymbolSerializer.super.serializeFacetValue(s, key, includingBuiltIn);
	}

	/**
	 * Collect meta information in symbol.
	 *
	 * @param desc
	 *            the desc
	 * @param plugins
	 *            the plugins
	 */
	@Override
	public void collectMetaInformationInSymbol(final IDescription desc, final GamlProperties plugins) {
		plugins.put(GamlProperties.PLUGINS, desc.getDefiningPlugin());
		plugins.put(GamlProperties.SKILLS, ((ISpeciesDescription) desc).getSkillsNames());
	}

	/**
	 * Collect meta information in facet value.
	 *
	 * @param desc
	 *            the desc
	 * @param key
	 *            the key
	 * @param plugins
	 *            the plugins
	 */
	@Override
	public void collectMetaInformationInFacetValue(final IDescription desc, final String key,
			final GamlProperties plugins) {
		IExpression e = desc.getFacetExpr(key);
		if (e != null) { e.collectMetaInformation(plugins); }
	}

}