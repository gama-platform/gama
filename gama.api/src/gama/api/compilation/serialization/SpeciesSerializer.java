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
 * Serializer for GAML species descriptions.
 * 
 * <p>
 * The {@code SpeciesSerializer} implements {@link ISymbolSerializer} to handle the serialization of species
 * declarations in GAML models. It provides specialized handling for skills and metadata collection specific to
 * agent species.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * This serializer enables:
 * </p>
 * <ul>
 *   <li><strong>Species Serialization:</strong> Convert species descriptions back to GAML code</li>
 *   <li><strong>Skill Handling:</strong> Properly serialize the skills facet as a collection</li>
 *   <li><strong>Metadata Extraction:</strong> Collect plugin dependencies and skill requirements</li>
 *   <li><strong>Base Implementation:</strong> Serves as parent for {@link ModelSerializer}</li>
 * </ul>
 * 
 * <h2>Special Facet Handling</h2>
 * 
 * <h3>Skills Facet</h3>
 * 
 * <p>
 * The {@code skills} facet receives special treatment because it represents a collection of skill names. Instead of
 * serializing as a single expression, it extracts all skill names from the expression description and formats them
 * as a list.
 * </p>
 * 
 * <p>
 * Example:
 * </p>
 * <pre>{@code
 * // Input description has skills facet
 * species my_agent skills: [moving, visible] { ... }
 * 
 * // Serialized output
 * "species my_agent skills: [moving, visible] { ... }"
 * }</pre>
 * 
 * <h2>Metadata Collection</h2>
 * 
 * <p>
 * This serializer collects two types of metadata:
 * </p>
 * <ul>
 *   <li><strong>Plugin Information:</strong> The plugin that defines this species</li>
 *   <li><strong>Skill Names:</strong> All skills used by the species (for dependency tracking)</li>
 * </ul>
 * 
 * <p>
 * Metadata is stored in a {@link GamlProperties} object for later use in dependency analysis or documentation
 * generation.
 * </p>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * // Serialize a species description
 * ISpeciesDescription species = ...; // obtained from model
 * SpeciesSerializer serializer = new SpeciesSerializer();
 * 
 * String gamlCode = serializer.serialize(species, false);
 * // Result: "species my_agent parent: agent skills: [moving] { ... }"
 * 
 * // Collect metadata
 * GamlProperties metadata = new GamlProperties();
 * serializer.collectMetaInformation(species, metadata);
 * 
 * Set<String> plugins = metadata.get(GamlProperties.PLUGINS);
 * Set<String> skills = metadata.get(GamlProperties.SKILLS);
 * }</pre>
 * 
 * <h2>Inheritance</h2>
 * 
 * <p>
 * This class serves as the base serializer for:
 * </p>
 * <ul>
 *   <li>{@link ModelSerializer} - Adds model-specific serialization logic</li>
 * </ul>
 * 
 * <h2>Method Overrides</h2>
 * 
 * <p>
 * Key methods overridden from {@link ISymbolSerializer}:
 * </p>
 * <ul>
 *   <li>{@link #serializeFacetValue(IDescription, String, boolean)} - Special handling for skills facet</li>
 *   <li>{@link #collectMetaInformationInSymbol(IDescription, GamlProperties)} - Collect plugin and skill info</li>
 *   <li>{@link #collectMetaInformationInFacetValue(IDescription, String, GamlProperties)} - Collect expression metadata</li>
 * </ul>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see ISymbolSerializer
 * @see ISpeciesDescription
 * @see ModelSerializer
 */
public class SpeciesSerializer implements ISymbolSerializer {

	/**
	 * Serializes facet values with special handling for the skills facet.
	 * 
	 * <p>
	 * This method provides custom serialization for the {@code skills} facet, extracting all skill names from the
	 * expression description and formatting them as a collection. All other facets are delegated to the default
	 * implementation.
	 * </p>
	 * 
	 * <p>
	 * For skills facet:
	 * </p>
	 * <ul>
	 *   <li>Extracts skill names from the expression description</li>
	 *   <li>Returns them formatted as a list (e.g., "[moving, visible]")</li>
	 *   <li>Handles null expressions gracefully</li>
	 * </ul>
	 * 
	 * @param s
	 *            the description being serialized
	 * @param key
	 *            the facet key to serialize
	 * @param includingBuiltIn
	 *            whether to include built-in elements
	 * @return the serialized facet value, or null if not applicable
	 */
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
	 * Collects metadata about the species symbol for dependency tracking.
	 * 
	 * <p>
	 * This method extracts two key pieces of information about the species:
	 * </p>
	 * <ol>
	 *   <li><strong>Defining Plugin:</strong> The plugin that provides this species definition</li>
	 *   <li><strong>Skills:</strong> All skills used by this species</li>
	 * </ol>
	 * 
	 * <p>
	 * The collected metadata is used for:
	 * </p>
	 * <ul>
	 *   <li>Dependency resolution when loading models</li>
	 *   <li>Plugin requirement validation</li>
	 *   <li>Documentation generation</li>
	 *   <li>Feature analysis</li>
	 * </ul>
	 * 
	 * @param desc
	 *            the species description to analyze
	 * @param plugins
	 *            the properties object to populate with metadata
	 */
	@Override
	public void collectMetaInformationInSymbol(final IDescription desc, final GamlProperties plugins) {
		plugins.put(GamlProperties.PLUGINS, desc.getDefiningPlugin());
		plugins.put(GamlProperties.SKILLS, ((ISpeciesDescription) desc).getSkillsNames());
	}

	/**
	 * Collects metadata from expressions in facet values.
	 * 
	 * <p>
	 * This method extracts metadata from the compiled expression associated with a facet. It delegates to the
	 * expression's own {@code collectMetaInformation} method to gather information about operators, functions,
	 * types, and other language features used in the expression.
	 * </p>
	 * 
	 * <p>
	 * This enables tracking of:
	 * </p>
	 * <ul>
	 *   <li>Operators and functions used in facet expressions</li>
	 *   <li>Types referenced in the expression</li>
	 *   <li>Plugin dependencies from expression elements</li>
	 * </ul>
	 * 
	 * @param desc
	 *            the description containing the facet
	 * @param key
	 *            the facet key whose value to analyze
	 * @param plugins
	 *            the properties object to populate with metadata
	 */
	@Override
	public void collectMetaInformationInFacetValue(final IDescription desc, final String key,
			final GamlProperties plugins) {
		IExpression e = desc.getFacetExpr(key);
		if (e != null) { e.collectMetaInformation(plugins); }
	}

}