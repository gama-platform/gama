/*******************************************************************************************************
 *
 * ISymbolSerializer.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.serialization;

import gama.api.additions.registries.ArtefactProtoRegistry;
import gama.api.compilation.descriptions.IDescription;
import gama.api.constants.IKeyword;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.utils.GamlProperties;
import gama.api.utils.StringUtils;

/**
 *
 */
public interface ISymbolSerializer extends IKeyword {

	/**
	 * Method serialize()
	 *
	 * @see gama.gaml.descriptions.IDescriptionSerializer#serializeToGaml(gama.api.compilation.descriptions.IDescription)
	 */
	default String serialize(final IDescription symbolDescription, final boolean includingBuiltIn) {
		if (symbolDescription.isBuiltIn() && !includingBuiltIn) return "";
		final StringBuilder sb = new StringBuilder();
		serialize(symbolDescription, sb, includingBuiltIn);
		return sb.toString();
	}

	/**
	 * Serialize.
	 *
	 * @param symbolDescription
	 *            the symbol description
	 * @param sb
	 *            the sb
	 * @param includingBuiltIn
	 *            the including built in
	 */
	default void serialize(final IDescription symbolDescription, final StringBuilder sb,
			final boolean includingBuiltIn) {
		serializeKeyword(symbolDescription, sb, includingBuiltIn);
		serializeFacets(symbolDescription, sb, includingBuiltIn);
		serializeChildren(symbolDescription, sb, includingBuiltIn);
	}

	/**
	 * Serialize children.
	 *
	 * @param symbolDescription
	 *            the symbol description
	 * @param sb
	 *            the sb
	 * @param includingBuiltIn
	 *            the including built in
	 */
	default void serializeChildren(final IDescription symbolDescription, final StringBuilder sb,
			final boolean includingBuiltIn) {

		final StringBuilder childBuilder = new StringBuilder();
		symbolDescription.visitChildren(desc -> {
			serializeChild(desc, childBuilder, includingBuiltIn);
			return true;
		});
		if (childBuilder.length() == 0) {
			sb.append(';');
		} else {
			sb.append(' ').append('{').append(StringUtils.LN);
			sb.append(childBuilder);
			sb.append('}').append(StringUtils.LN);
		}

	}

	/**
	 * Serialize child.
	 *
	 * @param s
	 *            the s
	 * @param sb
	 *            the sb
	 * @param includingBuiltIn
	 *            the including built in
	 */
	default void serializeChild(final IDescription s, final StringBuilder sb, final boolean includingBuiltIn) {
		final String gaml = s.serializeToGaml(false);
		if (gaml != null && gaml.length() > 0) {
			sb.append(StringUtils.TAB).append(s.serializeToGaml(includingBuiltIn)).append(StringUtils.LN);
		}
	}

	/**
	 * Serialize no recursion.
	 *
	 * @param sb
	 *            the sb
	 * @param symbolDescription
	 *            the symbol description
	 * @param includingBuiltIn
	 *            the including built in
	 */
	default void serializeNoRecursion(final StringBuilder sb, final IDescription symbolDescription,
			final boolean includingBuiltIn) {
		serializeKeyword(symbolDescription, sb, includingBuiltIn);
		serializeFacets(symbolDescription, sb, includingBuiltIn);
	}

	/**
	 * @param symbolDescription
	 * @param sb
	 * @param includingBuiltIn
	 */
	default void serializeFacets(final IDescription symbolDescription, final StringBuilder sb,
			final boolean includingBuiltIn) {

		final String omit = ArtefactProtoRegistry.getOmissibleFacetForSymbol(symbolDescription.getKeyword());
		final String expr = serializeFacetValue(symbolDescription, omit, includingBuiltIn);
		if (expr != null) { sb.append(expr).append(" "); }
		symbolDescription.visitFacets((key, b) -> {

			if (key.equals(omit)) return true;
			final String expr1 = serializeFacetValue(symbolDescription, key, includingBuiltIn);
			if (expr1 != null) {
				sb.append(serializeFacetKey(symbolDescription, key, includingBuiltIn)).append(expr1).append(" ");
			}

			return true;
		});

	}

	/**
	 * Serialize facet key.
	 *
	 * @param symbolDescription
	 *            the symbol description
	 * @param key
	 *            the key
	 * @param includingBuiltIn
	 *            the including built in
	 * @return the string
	 */
	default String serializeFacetKey(final IDescription symbolDescription, final String key,
			final boolean includingBuiltIn) {
		return key + ": ";
	}

	/**
	 * Serialize facet value.
	 *
	 * @param symbolDescription
	 *            the symbol description
	 * @param key
	 *            the key
	 * @param includingBuiltIn
	 *            the including built in
	 * @return the string
	 */
	default String serializeFacetValue(final IDescription symbolDescription, final String key,
			final boolean includingBuiltIn) {
		if (ArtefactProtoRegistry.NON_SERIALIZABLE_FACETS.contains(key)) return null;
		final IExpressionDescription ed = symbolDescription.getFacet(key);
		if (ed == null) return null;
		String exprString = ed.serializeToGaml(includingBuiltIn);
		if (exprString.startsWith(INTERNAL)) return null;
		if (ed.isLabel()) {
			final boolean isId = symbolDescription.getMeta().isId(key);
			if (!isId) { exprString = StringUtils.toGamlString(exprString); }
		}
		return exprString;

	}

	/**
	 * @param symbolDescription
	 * @param sb
	 * @param includingBuiltIn
	 */
	default void serializeKeyword(final IDescription symbolDescription, final StringBuilder sb,
			final boolean includingBuiltIn) {
		sb.append(symbolDescription.getKeyword()).append(' ');
	}

	/**
	 * Collect meta information.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param desc
	 *            the desc
	 * @param plugins
	 *            the plugins
	 * @date 27 déc. 2023
	 */
	default void collectMetaInformation(final IDescription desc, final GamlProperties plugins) {
		collectMetaInformationInSymbol(desc, plugins);
		collectMetaInformationInFacets(desc, plugins);
		collectMetaInformationInChildren(desc, plugins);
		desc.getGamlType().collectMetaInformation(plugins);
	}

	/**
	 * Collect meta information in symbol.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param desc
	 *            the desc
	 * @param plugins
	 *            the plugins
	 * @date 27 déc. 2023
	 */
	default void collectMetaInformationInSymbol(final IDescription desc, final GamlProperties plugins) {
		plugins.put(GamlProperties.PLUGINS, desc.getDefiningPlugin());
		plugins.put(GamlProperties.STATEMENTS, desc.getKeyword());
	}

	/**
	 * @param desc
	 * @param plugins
	 */
	default void collectMetaInformationInFacets(final IDescription desc, final GamlProperties plugins) {
		desc.visitFacets((key, exp) -> {
			collectMetaInformationInFacetValue(desc, key, plugins);
			return true;
		});
	}

	/**
	 * @param desc
	 * @param key
	 * @param plugins
	 */
	default void collectMetaInformationInFacetValue(final IDescription desc, final String key,
			final GamlProperties plugins) {
		// final IExpressionDescription ed = desc.getFacet(key);
		// if (ed == null) return;
		IExpression e = desc.getFacetExpr(key);
		if (e != null) { e.collectMetaInformation(plugins); }
	}

	/**
	 * @param desc
	 * @param plugins
	 */
	default void collectMetaInformationInChildren(final IDescription desc, final GamlProperties plugins) {
		desc.visitChildren(s -> {
			s.collectMetaInformation(plugins);
			return true;
		});

	}

}