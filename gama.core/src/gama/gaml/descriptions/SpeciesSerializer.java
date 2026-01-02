/*******************************************************************************************************
 *
 * SpeciesSerializer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.descriptions;

import java.util.Collection;

import gama.annotations.precompiler.GamlProperties;
import gama.gaml.expressions.IExpression;

/**
 * The Class SpeciesSerializer.
 */
public class SpeciesSerializer extends SymbolSerializer {

	@Override
	protected String serializeFacetValue(final IDescription s, final String key, final boolean includingBuiltIn) {
		if (SKILLS.equals(key)) {
			final IExpressionDescription ed = s.getFacet(key);
			if (ed == null) return null;
			final Collection<String> strings = ed.getStrings(s, true);
			return strings.toString();
		}
		return super.serializeFacetValue(s, key, includingBuiltIn);
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
	protected void collectMetaInformationInSymbol(final IDescription desc, final GamlProperties plugins) {
		plugins.put(GamlProperties.PLUGINS, desc.getDefiningPlugin());
		plugins.put(GamlProperties.SKILLS, ((SpeciesDescription) desc).getSkillsNames());
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
	protected void collectMetaInformationInFacetValue(final IDescription desc, final String key,
			final GamlProperties plugins) {
		// final IExpressionDescription ed = desc.getFacet(key);
		// if (ed == null) return;
		//
		// ed.collectMetaInformation(plugins);
		IExpression e = desc.getFacetExpr(key);
		if (e != null) { e.collectMetaInformation(plugins); }
	}

	// @Override
	// protected void collectPluginsInFacetValue(final SpeciesDescription s, final String key,
	// final Set<String> plugins) {
	// if (SKILLS.equals(key)) {
	// IExpressionDescription ed = s.getFacets().get(key);
	// if (ed == null) return;
	// Set<String> strings = ed.getStrings(s, true);
	// for (String name : strings) {
	// ISkill sk = AbstractGamlAdditions.getSkillInstanceFor(name);
	// if (sk != null) { plugins.add(sk.getDefiningPlugin()); }
	// }
	// } else if (CONTROL.equals(key)) {
	// IExpressionDescription ed = s.getFacets().get(key);
	// if (ed == null) return;
	// String name = ed.getExpression().literalValue();
	// ISkill sk = AbstractGamlAdditions.getSkillInstanceFor(name);
	// if (sk != null) { plugins.add(sk.getDefiningPlugin()); }
	// } else {
	// super.collectPluginsInFacetValue(s, key, plugins);
	// }
	// }

}