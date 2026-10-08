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
 * The annotations package provides annotations for configuring model dependencies and serialization.
 * 
 * <p>This package contains Java annotations used to declare metadata about GAML models, including
 * their dependencies on other models or plugins, custom serializers, and validators.</p>
 * 
 * <h2>Available Annotations</h2>
 * 
 * <ul>
 *   <li>{@link gama.api.annotations.depends_on} - Declares dependencies on other models or plugins
 *       <p>Used to specify that a model requires certain plugins or other models to function properly.</p>
 *   </li>
 *   <li>{@link gama.api.annotations.serializer} - Specifies a custom serializer for a type
 *       <p>Used to register custom serialization logic for specific data types.</p>
 *   </li>
 *   <li>{@link gama.api.annotations.validator} - Specifies a custom validator
 *       <p>Used to register custom validation logic for model elements.</p>
 *   </li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Declaring Dependencies:</h3>
 * <pre>{@code
 * @depends_on(value = {"gama.extension.physics", "my.custom.plugin"})
 * public class MyModelClass {
 *     // Model implementation
 * }
 * }</pre>
 * 
 * <h3>Custom Serializer:</h3>
 * <pre>{@code
 * @serializer(MyCustomSerializer.class)
 * public class MyCustomType {
 *     // Type implementation
 * }
 * }</pre>
 * 
 * <h3>Custom Validator:</h3>
 * <pre>{@code
 * @validator(MyCustomValidator.class)
 * public class MyStatement {
 *     // Statement implementation
 * }
 * }</pre>
 * 
 * <h2>Processing</h2>
 * 
 * <p>These annotations are processed during model compilation and platform initialization to:</p>
 * <ul>
 *   <li>Ensure all required dependencies are available</li>
 *   <li>Register custom serializers with the serialization system</li>
 *   <li>Register custom validators with the validation framework</li>
 * </ul>
 * 
 * @author The GAMA Development Team
 * @version 2025-03
 * 
 * @see gama.api.annotations.depends_on
 * @see gama.api.annotations.serializer
 * @see gama.api.annotations.validator
 */
package gama.api.annotations;
