/*******************************************************************************************************
 *
 * package-info.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

/**
 * The validation package provides infrastructure for validating GAML models and handling compilation errors.
 * 
 * <p>This package contains interfaces and classes for model validation, error collection, and model building.
 * It implements the validation phase of the compilation pipeline.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>Model Building:</h3>
 * <ul>
 *   <li>{@link gama.api.compilation.validation.IGamlModelBuilder} - Interface for building models from sources</li>
 * </ul>
 * 
 * <h3>Validation Context:</h3>
 * <ul>
 *   <li>{@link gama.api.compilation.validation.IValidationContext} - Context for collecting validation errors</li>
 *   <li>{@link gama.api.compilation.validation.IDocumentationContext} - Context for documentation generation</li>
 * </ul>
 * 
 * <h2>Validation Process</h2>
 * 
 * <p>Model validation includes:</p>
 * <ul>
 *   <li>Syntactic validation (parsing errors)</li>
 *   <li>Semantic validation (type checking, reference resolution)</li>
 *   <li>Constraint validation (facet requirements, value ranges)</li>
 *   <li>Warning generation (best practices, deprecations)</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * IGamlModelBuilder builder = GamlModelBuilder.getInstance();
 * List<GamlCompilationError> errors = new ArrayList<>();
 * 
 * IModel model = builder.compile(modelFile, errors);
 * 
 * if (!errors.isEmpty()) {
 *     // Handle validation errors
 *     for (GamlCompilationError error : errors) {
 *         if (error.isError()) {
 *             System.err.println("Error: " + error);
 *         }
 *     }
 * }
 * }</pre>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.compilation.validation.IGamlModelBuilder
 * @see gama.api.compilation.validation.IValidationContext
 * @see gama.api.compilation.GamlCompilationError
 */
package gama.api.compilation.validation;
