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
 * The compilation package provides the infrastructure for compiling GAML models into executable
 * simulation specifications.
 * 
 * <p>This package contains all the interfaces and classes necessary for parsing, validating,
 * and transforming GAML source code into runtime-ready model descriptions. It implements the
 * compilation pipeline that converts textual models into structured, executable representations.</p>
 * 
 * <h2>Compilation Pipeline</h2>
 * 
 * <p>The GAML compilation process follows these stages:</p>
 * <ol>
 *   <li><strong>Parsing:</strong> GAML source → Abstract Syntax Tree (AST)</li>
 *   <li><strong>Syntactic Analysis:</strong> AST → Syntactic Elements</li>
 *   <li><strong>Description Building:</strong> Syntactic Elements → Model Descriptions</li>
 *   <li><strong>Semantic Validation:</strong> Validate descriptions and resolve references</li>
 *   <li><strong>Code Generation:</strong> Descriptions → Executable Model</li>
 * </ol>
 * 
 * <h2>Sub-packages</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.compilation.ast} - Abstract Syntax Tree (AST) representations and syntactic elements</li>
 *   <li>{@link gama.api.compilation.descriptions} - Model descriptions (species, actions, variables, etc.)</li>
 *   <li>{@link gama.api.compilation.validation} - Validation contexts, error handling, and model builders</li>
 *   <li>{@link gama.api.compilation.factories} - Factories for creating language constructs and descriptions</li>
 *   <li>{@link gama.api.compilation.prototypes} - Prototypes and signatures for operators and actions</li>
 *   <li>{@link gama.api.compilation.documentation} - Documentation extraction and management</li>
 *   <li>{@link gama.api.compilation.serialization} - Model serialization and deserialization</li>
 * </ul>
 * 
 * <h2>Key Interfaces</h2>
 * 
 * <h3>Model Management:</h3>
 * <ul>
 *   <li>{@link gama.api.compilation.IModelsManager} - Interface for managing model compilation and lifecycle</li>
 *   <li>{@link gama.api.compilation.validation.IGamlModelBuilder} - Interface for building models from sources</li>
 * </ul>
 * 
 * <h3>Compilation Support:</h3>
 * <ul>
 *   <li>{@link gama.api.compilation.IVarAndActionSupport} - Support for variables and actions in descriptions</li>
 * </ul>
 * 
 * <h3>Error Handling:</h3>
 * <ul>
 *   <li>{@link gama.api.compilation.GamlCompilationError} - Represents compilation errors with location and severity</li>
 * </ul>
 * 
 * <h2>Core Concepts</h2>
 * 
 * <h3>AST (Abstract Syntax Tree):</h3>
 * <p>The AST package provides syntactic elements that represent the structure of GAML code
 * as parsed from source files. These elements preserve source location information for
 * error reporting.</p>
 * 
 * <h3>Descriptions:</h3>
 * <p>Descriptions are semantic representations of model elements. They include:</p>
 * <ul>
 *   <li><strong>Model Description:</strong> Complete model specification</li>
 *   <li><strong>Species Description:</strong> Agent type definitions</li>
 *   <li><strong>Action Description:</strong> Behavior definitions</li>
 *   <li><strong>Variable Description:</strong> Attribute specifications</li>
 *   <li><strong>Statement Description:</strong> Executable statement specifications</li>
 * </ul>
 * 
 * <h3>Validation:</h3>
 * <p>The validation system checks models for:</p>
 * <ul>
 *   <li>Syntactic correctness</li>
 *   <li>Semantic consistency (type checking, reference resolution)</li>
 *   <li>Constraint satisfaction</li>
 *   <li>Best practice compliance</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <h3>Compiling a Model:</h3>
 * <pre>{@code
 * // Get the models manager
 * IModelsManager modelsManager = GAMA.getModelManager();
 * 
 * // Compile a model file
 * List<GamlCompilationError> errors = new ArrayList<>();
 * IModel model = modelsManager.compile(modelFile, errors);
 * 
 * if (model == null) {
 *     // Handle compilation errors
 *     for (GamlCompilationError error : errors) {
 *         System.err.println(error.toString());
 *     }
 * } else {
 *     // Model compiled successfully
 *     IExperimentPlan experiment = model.getExperiment("my_experiment");
 * }
 * }</pre>
 * 
 * <h3>Accessing Model Descriptions:</h3>
 * <pre>{@code
 * IModelDescription modelDesc = model.getDescription();
 * 
 * // Access species
 * ISpeciesDescription speciesDesc = modelDesc.getSpeciesDescription("my_species");
 * 
 * // Access attributes
 * for (IVariableDescription var : speciesDesc.getVariables()) {
 *     System.out.println(var.getName() + ": " + var.getType());
 * }
 * 
 * // Access actions
 * for (IActionDescription action : speciesDesc.getActions()) {
 *     System.out.println(action.getName());
 * }
 * }</pre>
 * 
 * <h2>Extension Points</h2>
 * 
 * <p>The compilation system can be extended by:</p>
 * <ul>
 *   <li>Providing custom symbol factories for new GAML constructs</li>
 *   <li>Implementing custom validators for specialized checks</li>
 *   <li>Contributing documentation providers for custom elements</li>
 *   <li>Registering serializers for custom types</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>Compilation is generally not thread-safe and should be performed sequentially or
 * with appropriate synchronization. However, once compiled, model descriptions are
 * immutable and can be safely shared across threads.</p>
 * 
 * <h2>Performance Considerations</h2>
 * 
 * <p>Model compilation can be resource-intensive for large models. The system includes:</p>
 * <ul>
 *   <li>Caching mechanisms for repeated compilations</li>
 *   <li>Lazy evaluation of certain description properties</li>
 *   <li>Efficient error collection and reporting</li>
 * </ul>
 * 
 * @author Alexis Drogoul
 * @author The GAMA Development Team
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see gama.api.compilation.IModelsManager
 * @see gama.api.compilation.GamlCompilationError
 * @see gama.api.compilation.ast
 * @see gama.api.compilation.descriptions
 * @see gama.api.compilation.validation
 */
package gama.api.compilation;
