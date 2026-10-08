/*******************************************************************************************************
 *
 * IGamlModelBuilder.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.validation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.emf.common.util.URI;

import gama.api.compilation.GamlCompilationError;
import gama.api.exceptions.GamaCompilationFailedException;
import gama.api.kernel.species.IModelSpecies;
import gama.api.utils.GamlProperties;

/**
 * Builder interface for compiling GAML source files into executable model species.
 * 
 * <p>This interface defines the contract for GAML model builders, which are responsible for
 * the complete compilation pipeline from source code to executable models. The builder handles
 * parsing, validation, type resolution, and model construction.</p>
 * 
 * <h2>Compilation Pipeline</h2>
 * 
 * <p>The typical compilation process includes:</p>
 * <ol>
 *   <li><strong>Resource Loading:</strong> Load GAML source files and dependencies</li>
 *   <li><strong>Parsing:</strong> Convert source text to EMF model (Abstract Syntax Tree)</li>
 *   <li><strong>Linking:</strong> Resolve cross-references and imports</li>
 *   <li><strong>Validation:</strong> Perform semantic validation (see {@link IValidationContext})</li>
 *   <li><strong>Type Resolution:</strong> Resolve and check types</li>
 *   <li><strong>Model Construction:</strong> Build executable {@link IModelSpecies} from descriptions</li>
 * </ol>
 * 
 * <h2>Error Handling</h2>
 * 
 * <p>Compilation errors and warnings are collected in a provided list of {@link GamlCompilationError}.
 * These errors include:</p>
 * <ul>
 *   <li><strong>Syntax Errors:</strong> Parsing failures and malformed code</li>
 *   <li><strong>Semantic Errors:</strong> Type mismatches, undefined references</li>
 *   <li><strong>Validation Errors:</strong> Constraint violations, missing required facets</li>
 *   <li><strong>Warnings:</strong> Best practice violations, deprecated usage</li>
 * </ul>
 * 
 * <h2>Input Sources</h2>
 * 
 * <p>Models can be compiled from multiple source types:</p>
 * <ul>
 *   <li><strong>File:</strong> Local filesystem {@link File} objects</li>
 *   <li><strong>URI:</strong> EMF {@link URI} for workspace or platform resources</li>
 *   <li><strong>URL:</strong> Network-accessible resources via {@link URL}</li>
 * </ul>
 * 
 * <h2>Metadata Collection</h2>
 * 
 * <p>The optional {@link GamlProperties} parameter allows collecting metadata during compilation:</p>
 * <ul>
 *   <li>Required plugin bundles (OSGi dependencies)</li>
 *   <li>Used skills, operators, and statements</li>
 *   <li>Model features and capabilities</li>
 *   <li>Import dependencies</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>Implementations should be thread-safe for concurrent compilation of multiple models.
 * Error lists and properties objects are not shared between compilations.</p>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * IGamlModelBuilder builder = ...; // Get builder instance
 * List<GamlCompilationError> errors = new ArrayList<>();
 * GamlProperties properties = new GamlProperties();
 * 
 * IModelSpecies model = builder.compile(
 *     modelFile,
 *     errors,
 *     properties
 * );
 * 
 * if (model == null || !errors.isEmpty()) {
 *     // Handle compilation errors
 *     for (GamlCompilationError error : errors) {
 *         System.err.println(error);
 *     }
 * } else {
 *     // Model compiled successfully
 *     System.out.println("Required bundles: " + properties.getBundles());
 * }
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see IModelSpecies
 * @see GamlCompilationError
 * @see GamlProperties
 * @see IValidationContext
 */
public interface IGamlModelBuilder {

	/**
	 * Loads additional URLs into the builder's resource set.
	 * 
	 * <p>This method pre-loads resources that may be referenced by models during compilation.
	 * Useful for loading libraries, plugins, or shared model components.</p>
	 *
	 * @param URLs the list of URLs to load into the resource set
	 */
	void loadURLs(final List<URL> URLs);

	/**
	 * Compiles a GAML model from an EMF URI.
	 * 
	 * <p>This is the low-level compilation method that works directly with EMF URIs.
	 * Suitable for workspace resources, platform plugins, and custom URI schemes.</p>
	 * 
	 * <p>EMF URI formats:</p>
	 * <ul>
	 *   <li>{@code platform:/resource/MyProject/models/mymodel.gaml} - Workspace resource</li>
	 *   <li>{@code platform:/plugin/my.plugin/models/mymodel.gaml} - Plugin resource</li>
	 *   <li>{@code file:/absolute/path/to/model.gaml} - File system</li>
	 * </ul>
	 *
	 * @param uri the EMF URI of the GAML source file
	 * @param errors list to collect compilation errors and warnings (will be populated, must not be null)
	 * @return the compiled model species, or null if compilation failed
	 */
	IModelSpecies compile(final URI uri, final List<GamlCompilationError> errors);

	/**
	 * Compiles a GAML model from a file with metadata collection.
	 * 
	 * <p>This is the primary compilation method for headless and application scenarios.
	 * It provides full control over error handling and metadata collection.</p>
	 * 
	 * <h3>Parameters:</h3>
	 * <ul>
	 *   <li><strong>myFile:</strong> The .gaml source file to compile</li>
	 *   <li><strong>errors:</strong> Collection for errors/warnings (pass null to ignore)</li>
	 *   <li><strong>metaProperties:</strong> Metadata collection (pass null to skip)</li>
	 * </ul>
	 * 
	 * <h3>Metadata Collected:</h3>
	 * <p>When metaProperties is provided, it will be populated with:</p>
	 * <ul>
	 *   <li>Symbolic bundle names required to run the model</li>
	 *   <li>Skills used in the model</li>
	 *   <li>Operators referenced</li>
	 *   <li>Statements used</li>
	 *   <li>Import dependencies</li>
	 * </ul>
	 *
	 * @param myFile the GAML source file to compile (must exist and be readable)
	 * @param errors list to collect compilation errors (can be null)
	 * @param metaProperties properties object to populate with metadata (can be null)
	 * @return the compiled model species, or null if compilation failed
	 * @throws IOException if the file cannot be read
	 * @throws GamaCompilationFailedException if critical compilation errors occur
	 */
	IModelSpecies compile(final File myFile, final List<GamlCompilationError> errors, final GamlProperties metaProperties)
			throws IOException, GamaCompilationFailedException;

	/**
	 * Compiles a GAML model from a URL.
	 * 
	 * <p>This method supports compiling models from network-accessible locations or
	 * other URL-based resources. Useful for remote model repositories or web-based
	 * model libraries.</p>
	 *
	 * @param url the URL of the GAML source file
	 * @param errors list to collect compilation errors and warnings (will be populated, must not be null)
	 * @return the compiled model species, or null if compilation failed
	 */
	IModelSpecies compile(final URL url, final List<GamlCompilationError> errors);

}
