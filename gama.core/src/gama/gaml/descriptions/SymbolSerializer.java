/*******************************************************************************************************
 *
 * SymbolSerializer.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.descriptions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import gama.annotations.precompiler.GamlProperties;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.util.StringUtils;
import gama.gaml.expressions.IExpression;
import gama.gaml.factories.DescriptionFactory;
import gama.gaml.operators.Strings;

/**
 * Class IDescriptionSerializer.
 *
 * @author drogoul
 * @since 10 nov. 2014
 *
 */

public class SymbolSerializer implements IKeyword {

	/**
	 * Instantiates a new symbol serializer.
	 */
	protected SymbolSerializer() {}

	/** The Constant uselessFacets. */
	public static final Set<String> uselessFacets = new HashSet<>(Arrays.asList(INTERNAL_FUNCTION, WITH));

	/**
	 * Method serialize()
	 *
	 * @see gama.gaml.descriptions.IDescriptionSerializer#serializeToGaml(gama.gaml.descriptions.IDescription)
	 */
	public final String serialize(final IDescription symbolDescription, final boolean includingBuiltIn) {
		if (symbolDescription.isBuiltIn() && !includingBuiltIn) return "";
		final StringBuilder sb = new StringBuilder();
		serialize(symbolDescription, sb, includingBuiltIn);
		return sb.toString();
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
	public final void serializeNoRecursion(final StringBuilder sb, final IDescription symbolDescription,
			final boolean includingBuiltIn) {
		serializeKeyword(symbolDescription, sb, includingBuiltIn);
		serializeFacets(symbolDescription, sb, includingBuiltIn);
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
	protected void serialize(final IDescription symbolDescription, final StringBuilder sb,
			final boolean includingBuiltIn) {
		serializeKeyword(symbolDescription, sb, includingBuiltIn);
		serializeFacets(symbolDescription, sb, includingBuiltIn);
		serializeChildren(symbolDescription, sb, includingBuiltIn);
	}

	/**
	 * Serialize keyword.
	 *
	 * @param symbolDescription
	 *            the symbol description
	 * @param sb
	 *            the sb
	 * @param includingBuiltIn
	 *            the including built in
	 */
	protected void serializeKeyword(final IDescription symbolDescription, final StringBuilder sb,
			final boolean includingBuiltIn) {
		sb.append(symbolDescription.getKeyword()).append(' ');
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
	protected void serializeChildren(final IDescription symbolDescription, final StringBuilder sb,
			final boolean includingBuiltIn) {

		final StringBuilder childBuilder = new StringBuilder();
		symbolDescription.visitChildren(desc -> {
			serializeChild(desc, childBuilder, includingBuiltIn);
			return true;
		});
		if (childBuilder.length() == 0) {
			sb.append(';');
		} else {
			sb.append(' ').append('{').append(Strings.LN);
			sb.append(childBuilder);
			sb.append('}').append(Strings.LN);
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
	protected void serializeChild(final IDescription s, final StringBuilder sb, final boolean includingBuiltIn) {
		final String gaml = s.serializeToGaml(false);
		if (gaml != null && gaml.length() > 0) {
			sb.append(Strings.indent(s.serializeToGaml(includingBuiltIn), 1)).append(Strings.LN);
		}
	}

	/**
	 * Serialize facets.
	 *
	 * @param symbolDescription
	 *            the symbol description
	 * @param sb
	 *            the sb
	 * @param includingBuiltIn
	 *            the including built in
	 */
	protected void serializeFacets(final IDescription symbolDescription, final StringBuilder sb,
			final boolean includingBuiltIn) {
		final String omit = DescriptionFactory.getOmissibleFacetForSymbol(symbolDescription.getKeyword());
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
	protected String serializeFacetKey(final IDescription symbolDescription, final String key,
			final boolean includingBuiltIn) {
		return key + ": ";
	}

	/**
	 * Return null to exclude a facet
	 *
	 * @param symbolDescription
	 * @param key
	 * @return
	 */
	protected String serializeFacetValue(final IDescription symbolDescription, final String key,
			final boolean includingBuiltIn) {
		if (uselessFacets.contains(key)) return null;
		final IExpressionDescription ed = symbolDescription.getFacet(key);
		if (ed == null) return null;
		String exprString = ed.serializeToGaml(includingBuiltIn);
		if (exprString.startsWith(INTERNAL)) return null;
		if (ed instanceof LabelExpressionDescription) {
			final boolean isId = symbolDescription.getMeta().isId(key);
			if (!isId) { exprString = StringUtils.toGamlString(exprString); }
		}
		return exprString;

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
	protected void collectMetaInformation(final IDescription desc, final GamlProperties plugins) {
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
	protected void collectMetaInformationInSymbol(final IDescription desc, final GamlProperties plugins) {
		plugins.put(GamlProperties.PLUGINS, desc.getDefiningPlugin());
		plugins.put(GamlProperties.STATEMENTS, desc.getKeyword());
	}

	/**
	 * @param desc
	 * @param plugins
	 */
	protected void collectMetaInformationInFacets(final IDescription desc, final GamlProperties plugins) {
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
	protected void collectMetaInformationInFacetValue(final IDescription desc, final String key,
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
	protected void collectMetaInformationInChildren(final IDescription desc, final GamlProperties plugins) {
		desc.visitChildren(s -> {
			s.collectMetaInformation(plugins);
			return true;
		});

	}

}
