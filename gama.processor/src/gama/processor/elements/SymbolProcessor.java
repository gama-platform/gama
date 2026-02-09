/*******************************************************************************************************
 *
 * SymbolProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor.elements;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;

/**
 * The SymbolProcessor is responsible for processing {@code @symbol} annotations during the annotation processing phase.
 * 
 * <p>Symbols in GAMA represent language constructs that can appear in GAML models - these include statements,
 * declarations, and other syntactic elements that form the building blocks of GAML code. Each symbol defines
 * its behavior, accepted facets, containment rules, and execution semantics.
 * 
 * <p>This processor handles the complex task of:
 * <ul>
 * <li><strong>Symbol Registration:</strong> Registering symbols with the GAMA language system</li>
 * <li><strong>Facet Processing:</strong> Handling the facets (parameters) that each symbol accepts</li>
 * <li><strong>Containment Rules:</strong> Processing {@code @inside} annotations that define where symbols can appear</li>
 * <li><strong>Validation:</strong> Ensuring symbols extend the proper base classes and have valid configurations</li>
 * <li><strong>Constants Generation:</strong> Generating constant definitions for facet values</li>
 * </ul>
 * 
 * <h3>Symbol Structure:</h3>
 * <p>A typical symbol definition includes:
 * <ul>
 * <li>One or more names (keywords) for the symbol</li>
 * <li>The symbol kind (statement, declaration, etc.)</li>
 * <li>Behavioral flags (breakable, continuable, remote context, etc.)</li>
 * <li>Containment rules defining where the symbol can appear</li>
 * <li>Facet definitions specifying the parameters the symbol accepts</li>
 * </ul>
 * 
 * <h3>Example usage:</h3>
 * <pre>{@code
 * @symbol(
 *     name = {"write", "output"}, 
 *     kind = ISymbolKind.SINGLE_STATEMENT,
 *     with_args = true
 * )
 * @inside(symbols = {"experiment", "species", "action"})
 * @facets(value = {
 *     @facet(name = "message", type = IType.STRING, optional = false),
 *     @facet(name = "color", type = IType.COLOR, optional = true)
 * })
 * public class WriteStatement extends AbstractStatement {
 *     // Implementation
 * }
 * }</pre>
 * 
 * @author GAMA Development Team
 * @since 1.0
 * @see symbol
 * @see facets
 * @see inside
 * @see ElementProcessor
 */
public class SymbolProcessor extends ElementProcessor<symbol> {

	/**
	 * Creates the element code for a symbol annotation.
	 * 
	 * <p>This method generates the runtime registration code for a GAMA symbol. The process involves:
	 * <ol>
	 * <li>Extracting the symbol name (from annotation or class name)</li>
	 * <li>Processing behavioral flags (breakable, continuable, etc.)</li>
	 * <li>Handling containment rules from {@code @inside} annotations</li>
	 * <li>Processing facet definitions from {@code @facets} annotations</li>
	 * <li>Generating constructor reference for symbol instantiation</li>
	 * <li>Creating constant definitions for facet values</li>
	 * </ol>
	 * 
	 * <p>The generated code registers the symbol with all its metadata, enabling the GAMA
	 * parser to recognize and properly handle the symbol in GAML models.
	 * 
	 * @param sb the StringBuilder to append the generated registration code to
	 * @param e the class element annotated with @symbol
	 * @param symbol the symbol annotation containing the symbol's metadata
	 */
	@Override
	public void createElement(final StringBuilder sb, final Element e, final symbol symbol) {
		final String clazz = rawNameOf(e.asType());
		final String name = symbol.name().length == 0 ? e.getSimpleName().toString() : symbol.name()[0];
		verifyDoc(e, "symbol " + name, symbol);
		final StringBuilder constants = new StringBuilder();

		sb.append(in).append("_symbol(");
		toArrayOfStrings(symbol.name(), sb)
			.append(',').append(toClassObject(clazz))
			.append(',').append(symbol.kind())
			.append(',').append(toBoolean(symbol.breakable()))
			.append(',').append(toBoolean(symbol.continuable()))
			.append(',').append(toBoolean(symbol.remote_context()))
			.append(',').append(toBoolean(symbol.with_args()))
			.append(',').append(toBoolean(symbol.with_scope()))
			.append(',').append(toBoolean(symbol.with_sequence()))
			.append(',').append(toBoolean(symbol.unique_in_context()))
			.append(',').append(toBoolean(symbol.unique_name()))
			.append(',');
		final inside inside = e.getAnnotation(inside.class);
		if (inside != null) {
			toArrayOfStrings(inside.symbols(), sb).append(',');
			toArrayOfInts(inside.kinds(), sb).append(',');
		} else {
			toArrayOfStrings(null, sb).append(',');
			toArrayOfInts(null, sb).append(',');
		}
		
		final facets facets = e.getAnnotation(facets.class);
		final String omissible = processFacets(sb, e, facets, constants);
		
		sb.append(',').append(toJavaString(omissible)).append(',').append("(x)->new ").append(clazz).append("(x)");
		sb.append(");");
		
		if (constants.length() > 0) {
			constants.setLength(constants.length() - 1);
			sb.append(ln).append("_constants(").append(constants).append(");");
		}
	}
	
	/**
	 * Processes facets annotation and builds facets string.
	 * 
	 * <p>This method handles the {@code @facets} annotation which defines the parameters
	 * that a symbol accepts. It processes each facet definition and generates the appropriate
	 * runtime code for facet validation and handling.
	 * 
	 * <p>The method performs validation to ensure:
	 * <ul>
	 * <li>No duplicate facet names within the same symbol</li>
	 * <li>Proper documentation for each facet</li>
	 * <li>Correct type specifications</li>
	 * </ul>
	 * 
	 * <p>Facets with constant values are collected separately and added to the constants
	 * StringBuilder for later processing.
	 * 
	 * @param sb the StringBuilder to append the facets code to
	 * @param e the symbol element (for error reporting)
	 * @param facets the facets annotation containing all facet definitions
	 * @param constants the StringBuilder for collecting constant definitions
	 * @return the omissible facet name, or empty string if none specified
	 */
	private String processFacets(final StringBuilder sb, final Element e, final facets facets, 
			final StringBuilder constants) {
		if (facets == null) {
			sb.append("null");
			return "";
		}
		
		final String omissible = facets.omissible();
		sb.append("P(");
		
		// Use local set for temp to improve memory usage
		final Set<String> temp = new HashSet<>(facets.value().length);
		
		for (int i = 0; i < facets.value().length; i++) {
			final facet child = facets.value()[i];
			final String fName = child.name();
			if (!temp.add(fName)) {
				context.emitError("Facet '" + fName + "' is declared twice", e);
				continue;
			}
			
			if (i > 0) { sb.append(','); }
			
			processFacet(sb, e, child, constants);
		}
		sb.append(')');
		return omissible;
	}
	
	/**
	 * Processes a single facet definition.
	 * 
	 * <p>This method generates the registration code for an individual facet within a symbol.
	 * It handles all facet properties including:
	 * <ul>
	 * <li>Name and type specification</li>
	 * <li>Content type and indexing information</li>
	 * <li>Valid values constraints</li>
	 * <li>Optional and internal flags</li>
	 * <li>Remote context handling</li>
	 * <li>Documentation validation</li>
	 * </ul>
	 * 
	 * @param sb the StringBuilder to append the facet code to
	 * @param e the symbol element (for error reporting and documentation validation)
	 * @param child the individual facet annotation to process
	 * @param constants the StringBuilder for collecting constant definitions from facet values
	 */
	private void processFacet(final StringBuilder sb, final Element e, final facet child, 
			final StringBuilder constants) {
		final String fName = child.name();
		sb.append("_facet(").append(toJavaString(fName)).append(',');
		
		toArrayOfInts(child.type(), sb)
			.append(',').append(child.of())
			.append(',').append(child.index())
			.append(',');
		
		final String[] values = child.values();
		if (values != null && values.length > 0) { 
			toArrayOfStrings(values, constants).append(','); 
		}
		
		toArrayOfStrings(values, sb)
			.append(',').append(toBoolean(child.optional()))
			.append(',').append(toBoolean(child.internal()))
			.append(',').append(toBoolean(child.remote_context()));
		
		verifyDoc(e, "facet " + child.name(), child);
		sb.append(')');
	}

	/**
	 * Returns the annotation class that this processor handles.
	 * 
	 * @return the {@link symbol} annotation class
	 */
	@Override
	protected Class<symbol> getAnnotationClass() { return symbol.class; }

	/**
	 * Validates that a symbol element meets the requirements for symbol processing.
	 * 
	 * <p>This method performs two key validations:
	 * <ol>
	 * <li><strong>Inheritance Check:</strong> Verifies that the symbol class extends the required
	 *     ISymbol interface, ensuring it can participate in the GAMA symbol system</li>
	 * <li><strong>Annotation Check:</strong> Checks for the presence of {@code @inside} annotations
	 *     which define containment rules (though this is not mandatory)</li>
	 * </ol>
	 * 
	 * <p>Symbols must implement the ISymbol interface to be properly integrated into
	 * the GAMA language processing system and provide the required symbol behavior.
	 * 
	 * @param e the element to validate (should be a class annotated with @symbol)
	 * @return {@code true} if the element passes validation, {@code false} otherwise
	 */
	@Override
	protected boolean validateElement(final Element e) {
		boolean result = assertClassExtends(true, (TypeElement) e, context.getISymbol());
		result &= assertAnnotationPresent(false, e, inside.class);
		return result;
	}

}
