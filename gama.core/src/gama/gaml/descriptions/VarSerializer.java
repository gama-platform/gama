/*******************************************************************************************************
 *
 * VarSerializer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.descriptions;

import gama.annotations.precompiler.GamlProperties;

/**
 * The Class VarSerializer.
 */
public class VarSerializer extends SymbolSerializer {

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
		// plugins.put(GamlProperties.STATEMENTS, desc.keyword);
	}

	/**
	 * Serialize keyword.
	 *
	 * @param desc
	 *            the desc
	 * @param sb
	 *            the sb
	 * @param includingBuiltIn
	 *            the including built in
	 */
	@Override
	protected void serializeKeyword(final IDescription desc, final StringBuilder sb, final boolean includingBuiltIn) {
		String k = desc.getKeyword(); // desc.getFacets().getLabel(IKeyword.KEYWORD);
		if (!PARAMETER.equals(k)) {
			final String type = desc.getGamlType().serializeToGaml(false);
			if (!UNKNOWN.equals(type)) { k = type; }
		}
		sb.append(k).append(' ');
	}

	@Override
	protected String serializeFacetValue(final IDescription s, final String key, final boolean includingBuiltIn) {
		if (TYPE.equals(key) || OF.equals(key) || INDEX.equals(key)) return null;
		if (CONST.equals(key) && s.hasFacet(CONST) && FALSE.equals(s.getFacet(key).serializeToGaml(includingBuiltIn)))
			return null;
		return super.serializeFacetValue(s, key, includingBuiltIn);
	}

	@Override
	protected String serializeFacetKey(final IDescription s, final String key, final boolean includingBuiltIn) {
		if (INIT.equals(key)) return "<- ";
		return super.serializeFacetKey(s, key, includingBuiltIn);
	}

}