/*******************************************************************************************************
 *
 * serializer.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import gama.api.compilation.serialization.ISymbolSerializer;

/**
 * Declares a custom serializer for GAML symbols (statements, variable declarations, species, experiments, etc.).
 * 
 * <p>
 * This annotation allows developers to override the default serialization behavior for specific symbol types by
 * providing a custom serializer implementation. The custom serializer will be invoked instead of the standard
 * serializer whenever the annotated symbol needs to be serialized.
 * </p>
 * 
 * <h2>Purpose</h2>
 * <p>
 * Custom serializers are useful when:
 * </p>
 * <ul>
 *   <li>The default serialization format is not suitable for a specific symbol type</li>
 *   <li>Additional context or metadata needs to be included during serialization</li>
 *   <li>Performance optimizations are needed for frequently serialized symbols</li>
 *   <li>Special handling is required for complex nested structures</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Example 1: Custom serializer for a statement</h3>
 * <pre>{@code
 * @serializer(MyCustomStatementSerializer.class)
 * @symbol(name = "my_statement", kind = ISymbolKind.SINGLE_STATEMENT, ...)
 * public class MyCustomStatement extends AbstractStatement {
 *     // Statement implementation
 * }
 * 
 * // The serializer implementation
 * public class MyCustomStatementSerializer implements ISymbolSerializer {
 *     public String serialize(ISymbol symbol, boolean includingBuiltIn) {
 *         MyCustomStatement stmt = (MyCustomStatement) symbol;
 *         // Custom serialization logic
 *         return ...;
 *     }
 * }
 * }</pre>
 * 
 * <h3>Example 2: Custom serializer for a species</h3>
 * <pre>{@code
 * @serializer(SpeciesSerializer.class)
 * @species("custom_agent")
 * public class CustomAgent extends AbstractSpecies {
 *     // Species implementation
 * }
 * }</pre>
 * 
 * <h3>Example 3: Custom serializer for a variable declaration</h3>
 * <pre>{@code
 * @serializer(CustomVarSerializer.class)
 * public class CustomVariableStatement extends AbstractVarStatement {
 *     // Variable declaration implementation
 * }
 * }</pre>
 * 
 * <h2>Implementation Guidelines</h2>
 * 
 * <h3>Serializer Requirements</h3>
 * <p>
 * Custom serializer classes must:
 * </p>
 * <ul>
 *   <li>Implement the {@link gama.api.compilation.serialization.ISymbolSerializer} interface</li>
 *   <li>Provide a public no-argument constructor</li>
 *   <li>Be stateless or thread-safe (serializers may be reused)</li>
 *   <li>Handle null values and edge cases gracefully</li>
 * </ul>
 * 
 * <h3>Serializer Methods</h3>
 * <p>
 * The {@link gama.api.compilation.serialization.ISymbolSerializer} interface typically requires:
 * </p>
 * <ul>
 *   <li>{@code String serialize(ISymbol symbol, boolean includingBuiltIn)} - Main serialization method</li>
 *   <li>Return a valid GAML code representation of the symbol</li>
 *   <li>The {@code includingBuiltIn} parameter controls whether built-in declarations should be included</li>
 * </ul>
 * 
 * <h3>Best Practices</h3>
 * <ul>
 *   <li>Ensure the serialized output is valid GAML code that can be parsed back</li>
 *   <li>Preserve formatting and comments when possible for better readability</li>
 *   <li>Use indentation consistent with the GAML code style</li>
 *   <li>Handle facets (symbol parameters) in the correct order</li>
 *   <li>Consider performance for symbols that are serialized frequently</li>
 *   <li>Test serialization round-trips (serialize then parse) to verify correctness</li>
 * </ul>
 * 
 * <h2>Inheritance</h2>
 * <p>
 * This annotation is inherited, meaning that subclasses of an annotated class will use the same serializer unless they
 * specify their own {@code @serializer} annotation.
 * </p>
 * 
 * <h2>Processing</h2>
 * <p>
 * The annotation is processed at runtime during platform initialization. The serializer class is instantiated and
 * registered with the serialization system for the annotated symbol type.
 * </p>
 * 
 * @author drogoul
 * @since 11 nov. 2014
 * @see gama.api.compilation.serialization.ISymbolSerializer
 * @see gama.gaml.descriptions.IDescription
 */

@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.TYPE)
@Inherited
public @interface serializer {

	/**
	 * The custom serializer class to use for this symbol type.
	 * 
	 * <p>
	 * Specifies the class that implements {@link gama.api.compilation.serialization.ISymbolSerializer} and will handle
	 * serialization for the annotated symbol. The class must have a public no-argument constructor.
	 * </p>
	 * 
	 * @return the serializer class that extends {@link gama.api.compilation.serialization.ISymbolSerializer}
	 */
	Class<? extends ISymbolSerializer> value();
}