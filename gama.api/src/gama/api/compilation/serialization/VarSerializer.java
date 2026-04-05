/*******************************************************************************************************
 *
 * VarSerializer.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.serialization;

import gama.api.compilation.descriptions.IDescription;
import gama.api.utils.GamlProperties;

/**
 * Serializer for GAML variable and parameter declarations.
 * 
 * <p>
 * The {@code VarSerializer} implements {@link ISymbolSerializer} to handle the serialization of variable
 * declarations (including attributes, parameters, and constants) in GAML models. It provides specialized handling
 * for type keywords, initialization syntax, and facet filtering specific to variables.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * This serializer enables:
 * </p>
 * <ul>
 *   <li><strong>Variable Declaration:</strong> Convert variable descriptions back to GAML syntax</li>
 *   <li><strong>Parameter Serialization:</strong> Handle action and experiment parameters</li>
 *   <li><strong>Type Inference:</strong> Use actual type instead of generic "var" when available</li>
 *   <li><strong>Initialization Syntax:</strong> Use arrow notation ({@code <-}) for init facet</li>
 *   <li><strong>Facet Filtering:</strong> Suppress redundant or internal facets</li>
 * </ul>
 * 
 * <h2>Variable Types</h2>
 * 
 * <p>
 * This serializer handles different kinds of variable declarations:
 * </p>
 * <ul>
 *   <li><strong>Attributes:</strong> Species attributes (e.g., {@code int counter <- 0;})</li>
 *   <li><strong>Parameters:</strong> Action/experiment parameters (e.g., {@code parameter "Speed" var: speed;})</li>
 *   <li><strong>Constants:</strong> Immutable values (e.g., {@code const float PI <- 3.14159;})</li>
 *   <li><strong>Temporary Variables:</strong> Loop variables, let declarations</li>
 * </ul>
 * 
 * <h2>Keyword Serialization</h2>
 * 
 * <h3>Type-Based Keywords</h3>
 * 
 * <p>
 * For non-parameter declarations, the type is used as the keyword instead of the generic declaration keyword:
 * </p>
 * <pre>{@code
 * // Instead of: var counter <- 0;
 * int counter <- 0;
 * 
 * // Instead of: var location <- {0,0};
 * point location <- {0,0};
 * }</pre>
 * 
 * <h3>Parameter Keyword</h3>
 * 
 * <p>
 * Parameter declarations retain the "parameter" keyword:
 * </p>
 * <pre>{@code
 * parameter "Initial population" var: nb_agents min: 1 max: 1000;
 * }</pre>
 * 
 * <h2>Facet Handling</h2>
 * 
 * <h3>Suppressed Facets</h3>
 * 
 * <p>
 * The following facets are not serialized because they're redundant or internal:
 * </p>
 * <ul>
 *   <li><strong>type:</strong> Already expressed through the keyword</li>
 *   <li><strong>of:</strong> Container element type (included in type like {@code list<int>})</li>
 *   <li><strong>index:</strong> Map index type (included in type like {@code map<string,int>})</li>
 *   <li><strong>const: false:</strong> Default mutability (only {@code const: true} is shown)</li>
 * </ul>
 * 
 * <h3>Special Facet Keys</h3>
 * 
 * <p>
 * The {@code init} facet uses arrow notation instead of standard facet syntax:
 * </p>
 * <pre>{@code
 * // Instead of: int counter init: 0;
 * int counter <- 0;
 * }</pre>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Basic Variable Serialization</h3>
 * <pre>{@code
 * IDescription varDesc = ...; // int counter <- 0
 * VarSerializer serializer = new VarSerializer();
 * 
 * String gamlCode = serializer.serialize(varDesc, false);
 * // Result: "int counter <- 0;"
 * }</pre>
 * 
 * <h3>Parameter Serialization</h3>
 * <pre>{@code
 * IDescription paramDesc = ...; // parameter description
 * String paramCode = serializer.serialize(paramDesc, false);
 * // Result: "parameter \"Population\" var: nb_agents min: 1 max: 1000;"
 * }</pre>
 * 
 * <h3>Constant Serialization</h3>
 * <pre>{@code
 * IDescription constDesc = ...; // const float PI <- 3.14159
 * String constCode = serializer.serialize(constDesc, false);
 * // Result: "const float PI <- 3.14159;"
 * }</pre>
 * 
 * <h2>Metadata Collection</h2>
 * 
 * <p>
 * This serializer collects minimal metadata:
 * </p>
 * <ul>
 *   <li><strong>Plugin Information:</strong> The plugin that defines the variable's keyword</li>
 * </ul>
 * 
 * <p>
 * Note: The statement keyword metadata collection is commented out in the current implementation.
 * </p>
 * 
 * <h2>Type Resolution</h2>
 * 
 * <p>
 * If the type is {@code unknown}, the original keyword is preserved rather than attempting to infer a type.
 * This prevents incorrect serialization of variables with unresolved types.
 * </p>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see ISymbolSerializer
 * @see IDescription
 */
public class VarSerializer implements ISymbolSerializer {

	/**
	 * Collects metadata about the variable declaration for dependency tracking.
	 * 
	 * <p>
	 * This method extracts the plugin that defines the variable's keyword (e.g., the core plugin for basic types,
	 * or extension plugins for specialized types). This information is used for dependency resolution and plugin
	 * requirement validation.
	 * </p>
	 * 
	 * <p>
	 * Note: Statement keyword collection is currently commented out but could be enabled to track which variable
	 * declaration keywords are used in a model.
	 * </p>
	 * 
	 * @param desc
	 *            the variable description to analyze
	 * @param plugins
	 *            the properties object to populate with metadata
	 */
	@Override
	public void collectMetaInformationInSymbol(final IDescription desc, final GamlProperties plugins) {
		plugins.put(GamlProperties.PLUGINS, desc.getDefiningPlugin());
		// plugins.put(GamlProperties.STATEMENTS, desc.keyword);
	}

	/**
	 * Serializes the keyword for a variable declaration, using the type as the keyword when appropriate.
	 * 
	 * <p>
	 * This method implements type-based keyword selection for variable declarations. Instead of using generic
	 * keywords like "var", it uses the actual type (int, float, string, etc.) to produce more readable and
	 * idiomatic GAML code.
	 * </p>
	 * 
	 * <p>
	 * Behavior:
	 * </p>
	 * <ul>
	 *   <li><strong>Parameter:</strong> Use "parameter" keyword as-is</li>
	 *   <li><strong>Typed Variable:</strong> Use type name (e.g., "int", "point", "list&lt;agent&gt;")</li>
	 *   <li><strong>Unknown Type:</strong> Fall back to original keyword</li>
	 * </ul>
	 * 
	 * <p>
	 * Examples:
	 * </p>
	 * <pre>
	 * int counter          // keyword="var" becomes "int"
	 * point location       // keyword="var" becomes "point"
	 * parameter "Speed"    // keyword="parameter" stays "parameter"
	 * </pre>
	 * 
	 * @param desc
	 *            the variable description to serialize
	 * @param sb
	 *            the string builder to append output to
	 * @param includingBuiltIn
	 *            whether to include built-in elements (passed to type serialization)
	 */
	@Override
	public void serializeKeyword(final IDescription desc, final StringBuilder sb, final boolean includingBuiltIn) {
		String k = desc.getKeyword(); // desc.getFacets().getLabel(IKeyword.KEYWORD);
		if (!PARAMETER.equals(k)) {
			final String type = desc.getGamlType().serializeToGaml(false);
			if (!UNKNOWN.equals(type)) { k = type; }
		}
		sb.append(k).append(' ');
	}

	/**
	 * Serializes facet values with filtering for redundant or internal facets.
	 * 
	 * <p>
	 * This method suppresses facets that are either redundant (already expressed through other means) or represent
	 * default values that don't need to be explicitly stated.
	 * </p>
	 * 
	 * <p>
	 * Suppression rules:
	 * </p>
	 * <ul>
	 *   <li><strong>type:</strong> Suppressed because type is expressed through the keyword</li>
	 *   <li><strong>of:</strong> Suppressed because it's part of the type (e.g., list&lt;int&gt;)</li>
	 *   <li><strong>index:</strong> Suppressed because it's part of the type (e.g., map&lt;string,int&gt;)</li>
	 *   <li><strong>const: false:</strong> Suppressed because false is the default (only true needs to be shown)</li>
	 * </ul>
	 * 
	 * <p>
	 * Examples of suppression:
	 * </p>
	 * <pre>{@code
	 * // Input: var counter type: int init: 0 const: false
	 * // Output: int counter <- 0
	 * 
	 * // Input: var agents type: list of: agent
	 * // Output: list<agent> agents
	 * 
	 * // Input: var data type: map of: int index: string init: map([])
	 * // Output: map<string,int> data <- map([])
	 * 
	 * // Input: var PI type: float init: 3.14 const: true
	 * // Output: const float PI <- 3.14
	 * }</pre>
	 * 
	 * @param s
	 *            the description being serialized
	 * @param key
	 *            the facet key to serialize
	 * @param includingBuiltIn
	 *            whether to include built-in elements
	 * @return the serialized facet value, or null if the facet should be suppressed
	 */
	@Override
	public String serializeFacetValue(final IDescription s, final String key, final boolean includingBuiltIn) {
		if (TYPE.equals(key) || OF.equals(key) || INDEX.equals(key)) return null;
		if (CONST.equals(key) && s.hasFacet(CONST) && FALSE.equals(s.getFacet(key).serializeToGaml(includingBuiltIn)))
			return null;
		return ISymbolSerializer.super.serializeFacetValue(s, key, includingBuiltIn);
	}

	/**
	 * Serializes facet keys with special handling for the init facet.
	 * 
	 * <p>
	 * This method provides the idiomatic GAML arrow notation ({@code <-}) for variable initialization instead of
	 * the standard {@code init:} facet syntax. This makes variable declarations more concise and readable.
	 * </p>
	 * 
	 * <p>
	 * Examples:
	 * </p>
	 * <pre>
	 * int counter <- 0       // Not: int counter init: 0
	 * point loc <- {0,0}     // Not: point loc init: {0,0}
	 * </pre>
	 * 
	 * <p>
	 * All other facets use the standard {@code key: value} format.
	 * </p>
	 * 
	 * @param s
	 *            the description being serialized (unused in this implementation)
	 * @param key
	 *            the facet key to serialize
	 * @param includingBuiltIn
	 *            whether to include built-in elements (unused in this implementation)
	 * @return the facet key string: "<- " for init, "key: " for all others
	 */
	@Override
	public String serializeFacetKey(final IDescription s, final String key, final boolean includingBuiltIn) {
		if (INIT.equals(key)) return "<- ";
		return ISymbolSerializer.super.serializeFacetKey(s, key, includingBuiltIn);
	}

}