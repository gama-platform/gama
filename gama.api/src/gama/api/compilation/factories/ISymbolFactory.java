/*******************************************************************************************************
 *
 * ISymbolFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.factories;

import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.symbols.ISymbol;

/**
 * Factory interface for creating executable symbol instances from descriptions.
 * 
 * <p>
 * This functional interface defines the contract for factories that transform semantic
 * {@link IDescription} objects into executable {@link ISymbol} instances. Symbols represent
 * the runtime executable counterparts of GAML language constructs.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * ISymbolFactory enables:
 * </p>
 * <ul>
 *   <li><strong>Description to Symbol Conversion:</strong> Creating executable symbols from validated descriptions</li>
 *   <li><strong>Runtime Instantiation:</strong> Building the runtime execution tree</li>
 *   <li><strong>Custom Symbol Creation:</strong> Allowing extensions to define new GAML constructs</li>
 *   <li><strong>Encapsulation:</strong> Separating description (compile-time) from symbol (runtime) concerns</li>
 * </ul>
 * 
 * <h2>Symbol Types</h2>
 * 
 * <p>
 * Factories create various types of symbols:
 * </p>
 * <ul>
 *   <li><strong>Statements:</strong> Executable statements (if, loop, create, ask, etc.)</li>
 *   <li><strong>Species:</strong> Agent type definitions</li>
 *   <li><strong>Actions:</strong> Agent behaviors</li>
 *   <li><strong>Variables:</strong> Agent attributes</li>
 *   <li><strong>Aspects:</strong> Visual representations</li>
 * </ul>
 * 
 * <h2>Compilation Flow</h2>
 * 
 * <ol>
 *   <li><strong>Parsing:</strong> Source code → AST (ISyntacticElement)</li>
 *   <li><strong>Description Building:</strong> AST → Descriptions (IDescription)</li>
 *   <li><strong>Validation:</strong> Descriptions validated for correctness</li>
 *   <li><strong>Symbol Creation:</strong> Descriptions → Symbols (ISymbol) via this factory</li>
 *   <li><strong>Execution:</strong> Symbols executed at runtime</li>
 * </ol>
 * 
 * <h2>Usage Example</h2>
 * 
 * <h3>Implementing a Symbol Factory:</h3>
 * <pre>{@code
 * public class IfStatementFactory implements ISymbolFactory {
 *     
 *     @Override
 *     public ISymbol create(IDescription description) {
 *         // Extract facets from description
 *         IExpression condition = description.getFacetExpr("condition");
 *         
 *         // Create the executable symbol
 *         return new IfStatement(description, condition);
 *     }
 * }
 * }</pre>
 * 
 * <h3>Registering and Using:</h3>
 * <pre>{@code
 * // Registration (typically via annotations)
 * @symbol(name = "if", kind = ISymbolKind.SEQUENCE_STATEMENT, ...)
 * @factory(IfStatementFactory.class)
 * public class IfStatement extends AbstractStatement { ... }
 * 
 * // During compilation
 * IDescription ifDesc = ...; // Compiled if statement description
 * ISymbolFactory factory = ...;  // Retrieved from registry
 * ISymbol ifSymbol = factory.create(ifDesc); // Create executable symbol
 * }</pre>
 * 
 * <h2>Design Pattern</h2>
 * 
 * <p>
 * This interface implements the Factory Method pattern, where each symbol type has its own
 * factory implementation. The use of a functional interface allows for concise lambda-based
 * implementations when appropriate.
 * </p>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>
 * Implementations should be stateless and thread-safe, as they may be called concurrently
 * during parallel compilation. The created symbols, however, are typically bound to a specific
 * agent or scope and are not shared across threads.
 * </p>
 * 
 * @author drogoul
 * @since 29 août 2010
 * @version 2025-03
 * 
 * @see ISymbol
 * @see IDescription
 * @see gama.api.gaml.symbols.AbstractStatement
 */
@FunctionalInterface
public interface ISymbolFactory {

	/**
	 * Creates an executable symbol instance from the given description.
	 * 
	 * <p>
	 * This method transforms a validated description into its executable runtime counterpart.
	 * The description contains all the compile-time information (facets, children, type
	 * information) needed to construct the symbol.
	 * </p>
	 * 
	 * <p>
	 * Implementations typically:
	 * </p>
	 * <ol>
	 *   <li>Extract facet expressions from the description</li>
	 *   <li>Create child symbols recursively if needed</li>
	 *   <li>Construct the symbol instance with the extracted information</li>
	 *   <li>Perform any additional initialization required</li>
	 * </ol>
	 * 
	 * <h3>Example:</h3>
	 * <pre>{@code
	 * @Override
	 * public ISymbol create(IDescription description) {
	 *     // Extract condition expression
	 *     IExpression condition = description.getFacetExpr(IKeyword.CONDITION);
	 *     
	 *     // Create symbol with description and condition
	 *     IfStatement symbol = new IfStatement(description, condition);
	 *     
	 *     // Symbol will recursively create its children (then/else blocks)
	 *     return symbol;
	 * }
	 * }</pre>
	 * 
	 * <p>
	 * <b>Note:</b> The description should have been fully validated before this method
	 * is called. The factory can assume that all required facets are present and valid.
	 * </p>
	 *
	 * @param description the validated description to create a symbol from (never null)
	 * @return a new executable symbol instance (never null)
	 * @throws IllegalArgumentException if the description is invalid or missing required information
	 */
	ISymbol create(IDescription description);

}
