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

import gama.annotations.constants.IKeyword;
import gama.api.additions.registries.ArtefactRegistry;
import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.utils.GamlProperties;
import gama.api.utils.StringUtils;

/**
 * Interface for serializing GAML symbols back to source code.
 * 
 * <p>
 * This interface defines the contract for converting semantic descriptions ({@link IDescription}) back into
 * syntactically valid GAML source code. Serializers are used for code generation, model transformation,
 * refactoring, and saving programmatically created models.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * Symbol serializers enable:
 * </p>
 * <ul>
 *   <li><strong>Code Generation:</strong> Converting descriptions back to GAML source text</li>
 *   <li><strong>Model Transformation:</strong> Supporting refactoring and model manipulation</li>
 *   <li><strong>Persistence:</strong> Saving programmatically created models to files</li>
 *   <li><strong>Documentation:</strong> Generating code examples in help text</li>
 *   <li><strong>Metadata Collection:</strong> Extracting plugin and statement dependencies</li>
 * </ul>
 * 
 * <h2>Serialization Process</h2>
 * 
 * <p>
 * Serialization follows a structured process:
 * </p>
 * <ol>
 *   <li><strong>Keyword:</strong> Output the symbol's keyword (species, action, if, etc.)</li>
 *   <li><strong>Facets:</strong> Serialize all facets (properties/parameters)</li>
 *   <li><strong>Children:</strong> Recursively serialize nested elements</li>
 * </ol>
 * 
 * <h2>Specialized Serializers</h2>
 * 
 * <p>
 * Different symbol types may use specialized serializers:
 * </p>
 * <ul>
 *   <li>{@link ModelSerializer} - For model descriptions</li>
 *   <li>{@link SpeciesSerializer} - For species descriptions</li>
 *   <li>{@link StatementSerializer} - For statements</li>
 *   <li>{@link VarSerializer} - For variable declarations</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <h3>Serializing a Description:</h3>
 * <pre>{@code
 * IDescription desc = ...; // species, action, or statement description
 * ISymbolSerializer serializer = desc.getSerializer();
 * 
 * // Serialize to string
 * String gamlCode = serializer.serialize(desc, false);
 * // Result: "species my_agent parent: agent { ... }"
 * 
 * // Include built-in elements
 * String fullCode = serializer.serialize(desc, true);
 * }</pre>
 * 
 * <h3>Building GAML Incrementally:</h3>
 * <pre>{@code
 * StringBuilder sb = new StringBuilder();
 * ISymbolSerializer serializer = new StatementSerializer();
 * 
 * // Serialize multiple descriptions
 * for (IDescription child : model.getChildren()) {
 *     serializer.serialize(child, sb, false);
 *     sb.append("\n");
 * }
 * }</pre>
 * 
 * <h3>Collecting Metadata:</h3>
 * <pre>{@code
 * GamlProperties metadata = new GamlProperties();
 * serializer.collectMetaInformation(desc, metadata);
 * 
 * // Extract plugin dependencies
 * Set<String> plugins = metadata.get(GamlProperties.PLUGINS);
 * 
 * // Extract used statements
 * Set<String> statements = metadata.get(GamlProperties.STATEMENTS);
 * }</pre>
 * 
 * <h2>Built-in Elements</h2>
 * 
 * <p>
 * The {@code includingBuiltIn} parameter controls whether platform-provided elements are serialized:
 * </p>
 * <ul>
 *   <li><strong>true:</strong> Include built-in attributes, actions, and species</li>
 *   <li><strong>false:</strong> Only user-defined elements (typical for saving models)</li>
 * </ul>
 * 
 * <h2>Facet Serialization</h2>
 * 
 * <p>
 * Facets are serialized with special handling:
 * </p>
 * <ul>
 *   <li><strong>Omissible Facets:</strong> Can appear without the key name</li>
 *   <li><strong>Label Facets:</strong> May need quoting depending on ID status</li>
 *   <li><strong>Non-Serializable:</strong> Some internal facets are skipped</li>
 * </ul>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see IDescription
 * @see ModelSerializer
 * @see SpeciesSerializer
 * @see StatementSerializer
 * @see VarSerializer
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

		final String omit = ArtefactRegistry.getOmissibleFacetForSymbol(symbolDescription.getKeyword());
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
		if (ArtefactRegistry.NON_SERIALIZABLE_FACETS.contains(key)) return null;
		final IExpressionDescription ed = symbolDescription.getFacet(key);
		if (ed == null) return null;
		String exprString = ed.serializeToGaml(includingBuiltIn);
		if (exprString.startsWith(INTERNAL)) return null;
		if (ed.isLabel()) {
			final boolean isId = symbolDescription.getArtefact().isId(key);
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