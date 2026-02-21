/*******************************************************************************************************
 *
 * ModelSerializer.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.serialization;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.utils.StringUtils;

/**
 * Serializer for GAML model descriptions.
 * 
 * <p>
 * The {@code ModelSerializer} extends {@link SpeciesSerializer} to handle the serialization of complete GAML models,
 * including the model header, global section, and all child elements (species, experiments, etc.). It generates
 * syntactically valid GAML code from a model's semantic description.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * This serializer enables:
 * </p>
 * <ul>
 *   <li><strong>Model Persistence:</strong> Save programmatically created models to .gaml files</li>
 *   <li><strong>Model Transformation:</strong> Support refactoring and code generation operations</li>
 *   <li><strong>Documentation:</strong> Generate complete model code examples</li>
 *   <li><strong>Code Formatting:</strong> Standardize model structure with proper sections</li>
 * </ul>
 * 
 * <h2>Serialization Structure</h2>
 * 
 * <p>
 * A model is serialized with the following structure:
 * </p>
 * <pre>
 * model &lt;name&gt;
 * 
 * global {
 *     // Global attributes
 *     // Global actions
 *     // Behaviors
 *     // Aspects
 * }
 * 
 * species &lt;name&gt; { ... }
 * ...
 * 
 * experiment &lt;name&gt; { ... }
 * ...
 * </pre>
 * 
 * <h2>Key Features</h2>
 * 
 * <ul>
 *   <li><strong>Model Header:</strong> Outputs "model" keyword with cleaned name (removing MODEL_SUFFIX)</li>
 *   <li><strong>Global Section:</strong> Serializes model-level attributes, actions, behaviors, and aspects</li>
 *   <li><strong>Organized Output:</strong> Groups children by type with section comments</li>
 *   <li><strong>Hierarchical:</strong> Includes all micro-species and experiments in order</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * // Serialize a model description
 * IModelDescription model = ...; // obtained from compilation
 * ModelSerializer serializer = new ModelSerializer();
 * 
 * String gamlCode = serializer.serialize(model, false);
 * // Result:
 * // model my_model
 * //
 * // global {
 * //     // Global attributes of world
 * //     int counter <- 0;
 * //     // Global actions of world
 * //     action init { ... }
 * //     ...
 * // }
 * // species my_agent { ... }
 * // experiment my_experiment { ... }
 * 
 * // Save to file
 * Files.writeString(Path.of("model.gaml"), gamlCode);
 * }</pre>
 * 
 * <h2>Section Organization</h2>
 * 
 * <p>
 * Children are serialized in a structured order:
 * </p>
 * <ol>
 *   <li><strong>Global Block:</strong>
 *     <ul>
 *       <li>Global attributes (variables)</li>
 *       <li>Global actions (methods)</li>
 *       <li>Behaviors (reflex, init, etc.)</li>
 *       <li>Aspects (visualization)</li>
 *     </ul>
 *   </li>
 *   <li><strong>Species Definitions:</strong> All micro-species from the model</li>
 *   <li><strong>Experiments:</strong> All experiment definitions</li>
 * </ol>
 * 
 * <h2>Special Handling</h2>
 * 
 * <ul>
 *   <li><strong>Name Facet:</strong> Suppressed in serialization (name appears in model header)</li>
 *   <li><strong>Comments:</strong> Section comments added to organize output</li>
 *   <li><strong>Blank Lines:</strong> Added between sections for readability</li>
 * </ul>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see SpeciesSerializer
 * @see IModelDescription
 * @see ISymbolSerializer
 */
public class ModelSerializer extends SpeciesSerializer {

	/**
	 * Serializes the model header and global keyword.
	 * 
	 * <p>
	 * Outputs the model declaration followed by the global section keyword. The model name is cleaned by removing
	 * the {@link IModelDescription#MODEL_SUFFIX} (typically "_model") to produce readable code.
	 * </p>
	 * 
	 * <p>
	 * Generated format:
	 * </p>
	 * <pre>
	 * model &lt;cleaned_name&gt;
	 * 
	 * global 
	 * </pre>
	 * 
	 * @param desc
	 *            the model description to serialize
	 * @param sb
	 *            the string builder to append output to
	 * @param includingBuiltIn
	 *            whether to include built-in elements (unused in this method)
	 */
	@Override
	public void serializeKeyword(final IDescription desc, final StringBuilder sb, final boolean includingBuiltIn) {
		sb.append("model ").append(desc.getName().replace(IModelDescription.MODEL_SUFFIX, "")).append(StringUtils.LN)
				.append(StringUtils.LN);
		sb.append("global ");
	}

	/**
	 * Serializes all children of the model in a structured, organized format.
	 * 
	 * <p>
	 * This method overrides the default behavior to organize model elements into logical sections with
	 * descriptive comments. The output follows this structure:
	 * </p>
	 * <ol>
	 *   <li><strong>Global block:</strong> Contains attributes, actions, behaviors, and aspects</li>
	 *   <li><strong>Species:</strong> All micro-species defined in the model</li>
	 *   <li><strong>Experiments:</strong> All experiment definitions</li>
	 * </ol>
	 * 
	 * <p>
	 * Each section is preceded by a comment header and separated by blank lines for readability.
	 * </p>
	 * 
	 * <p>
	 * Example output:
	 * </p>
	 * <pre>
	 * global {
	 * 
	 *     // Global attributes of world
	 *     int counter <- 0;
	 *     
	 *     // Global actions of world
	 *     action step { ... }
	 *     
	 *     // Behaviors of world
	 *     reflex update { ... }
	 *     
	 *     // Aspects of world
	 *     aspect default { ... }
	 * }
	 * 
	 * species my_agent { ... }
	 * 
	 * experiment main type: gui { ... }
	 * </pre>
	 * 
	 * @param d
	 *            the model description (cast to ISpeciesDescription internally)
	 * @param sb
	 *            the string builder to append output to
	 * @param includingBuiltIn
	 *            whether to include built-in elements in serialization
	 */
	@Override
	public void serializeChildren(final IDescription d, final StringBuilder sb, final boolean includingBuiltIn) {
		final ISpeciesDescription desc = (ISpeciesDescription) d;
		sb.append(' ').append('{').append(StringUtils.LN);
		Iterable<? extends IDescription> children = desc.getAttributes();
		sb.append(StringUtils.LN);
		sb.append("// Global attributes of ").append(desc.getName()).append(StringUtils.LN);
		for (final IDescription s : children) { serializeChild(s, sb, includingBuiltIn); }
		children = desc.getActions();
		sb.append(StringUtils.LN);
		sb.append("// Global actions of ").append(desc.getName()).append(StringUtils.LN);
		for (final IDescription s : children) { serializeChild(s, sb, includingBuiltIn); }
		children = desc.getBehaviors();
		sb.append(StringUtils.LN);
		sb.append("// Behaviors of ").append(desc.getName()).append(StringUtils.LN);
		for (final IDescription s : children) { serializeChild(s, sb, includingBuiltIn); }
		children = desc.getAspects();
		sb.append(StringUtils.LN);
		sb.append("// Aspects of ").append(desc.getName()).append(StringUtils.LN);
		for (final IDescription s : children) { serializeChild(s, sb, includingBuiltIn); }
		sb.append('}').append(StringUtils.LN);
		children = desc.getOwnMicroSpecies().values();
		for (final IDescription s : children) {
			sb.append(StringUtils.LN);
			serializeChild(s, sb, includingBuiltIn);
		}

		children = ((IModelDescription) desc).getExperiments();
		for (final IDescription s : children) {
			sb.append(StringUtils.LN);
			serializeChild(s, sb, includingBuiltIn);
		}
	}

	/**
	 * Serializes facet values, suppressing the name facet.
	 * 
	 * <p>
	 * This method prevents the model name from being serialized as a facet because it is already included in
	 * the model header declaration. All other facets are delegated to the parent {@link SpeciesSerializer}.
	 * </p>
	 * 
	 * <p>
	 * Suppressed facets:
	 * </p>
	 * <ul>
	 *   <li><strong>name:</strong> Already in "model &lt;name&gt;" header</li>
	 * </ul>
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
		if (NAME.equals(key)) return null;
		return super.serializeFacetValue(s, key, includingBuiltIn);
	}

}