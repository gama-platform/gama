/*******************************************************************************************************
 *
 * SyntacticModelElement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.ast;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.ast.ISyntacticElement;
import gama.api.constants.IKeyword;
import gama.api.gaml.symbols.Facets;

/**
 * Represents a complete GAML model in the Abstract Syntax Tree.
 * 
 * <p>A model element is the root of the AST for a GAML file and contains all top-level
 * declarations including species, experiments, global variables, and functions. It serves
 * as the entry point for compilation and semantic analysis.</p>
 * 
 * <p><strong>Key Responsibilities:</strong></p>
 * <ul>
 *   <li><strong>Root Container:</strong> Contains all species, experiments, and global definitions</li>
 *   <li><strong>Path Management:</strong> Maintains the file path for resource resolution and imports</li>
 *   <li><strong>Pragma Support:</strong> Stores compiler directives and annotations</li>
 *   <li><strong>Tree Compaction:</strong> Provides optimization to reduce memory footprint</li>
 *   <li><strong>Experiment Access:</strong> Specialized traversal for experiment definitions</li>
 * </ul>
 * 
 * <p><strong>Model Structure:</strong></p>
 * <pre>{@code
 * model MyModel {               // SyntacticModelElement (this class)
 *   global {                    // Global section (SyntacticComposedElement)
 *     int population <- 100;    // Global variables
 *   }
 *   species MySpecies { ... }   // Species definitions
 *   grid MyGrid { ... }         // Grid definitions
 *   experiment MyExp { ... }    // Experiment definitions
 * }
 * }</pre>
 * 
 * <p><strong>Path Handling:</strong></p>
 * <p>The path field stores the directory containing the model file, with a trailing slash.
 * This is used to resolve relative imports and file references within the model. The path
 * is normalized to ensure consistent file separator usage.</p>
 * 
 * <p><strong>Pragmas:</strong></p>
 * <p>Models can contain pragma annotations that affect compilation behavior. These are
 * stored as a map and can include directives for warnings, optimization, or experimental
 * features.</p>
 * 
 * <p><strong>Memory Optimization:</strong></p>
 * <p>The {@link #compactModel()} method recursively removes empty facet collections
 * throughout the tree, significantly reducing memory usage after AST construction.</p>
 * 
 * <p><strong>Thread Safety:</strong> NOT thread-safe. Should only be accessed during
 * compilation phases from a single thread.</p>
 *
 * @author drogoul
 * @since 12 avr. 2014
 * @see SyntacticTopLevelElement
 * @see SyntacticExperimentModelElement
 * @see SyntacticFactory#create
 */
public class SyntacticModelElement extends SyntacticTopLevelElement {

	/**
	 * The file system path to the directory containing this model file.
	 * 
	 * <p>The path is normalized to end with a "/" for consistent path operations.
	 * Used for resolving relative imports and file references within the model.</p>
	 * 
	 * <p>May be null for synthetic models created programmatically (e.g., for
	 * compilation of standalone blocks in testing or REPL scenarios).</p>
	 */
	final private String path;

	/**
	 * Constructs a new syntactic model element representing a complete GAML model.
	 * 
	 * <p>The path is normalized to ensure it ends with "/" for consistent path operations.
	 * This normalization simplifies relative path resolution for imports and file references.</p>
	 * 
	 * <p><strong>Path Normalization:</strong> If the provided path doesn't end with
	 * {@link File#pathSeparator}, a "/" is appended. Note: This uses "/" instead of
	 * File.separator for cross-platform compatibility as GAML uses forward slashes.</p>
	 * 
	 * <p><strong>Null Path Handling:</strong> A null path indicates a synthetic or
	 * temporary model (e.g., for REPL, testing, or compilation of code blocks without
	 * a file context). Such models cannot resolve relative imports.</p>
	 *
	 * @param keyword   the GAML keyword, typically {@link IKeyword#MODEL} or
	 *                  {@link ISyntacticFactory#SYNTHETIC_MODEL}
	 * @param facets    the initial facets for this model, typically including name and pragmas
	 * @param statement the underlying EMF EObject from the parsed model
	 * @param path      the directory path containing the model file, or null for synthetic models.
	 *                  Will be normalized to end with "/"
	 */
	public SyntacticModelElement(final String keyword, final Facets facets, final EObject statement,
			final String path) {
		super(keyword, facets, statement);
		if (path != null) {
			final String p = path;
			// Normalize path to end with "/" for consistent operations
			this.path = p.endsWith(File.pathSeparator) ? p : p + "/";
		} else {
			// Case of ill resources (compilation of blocks)
			this.path = null;
		}
	}

	/**
	 * Checks if this element represents a species definition.
	 * 
	 * <p>Models are top-level containers and are not species themselves, even though
	 * they extend structural elements. This override clarifies that distinction.</p>
	 *
	 * @return false, as models are not species
	 */
	@Override
	public boolean isSpecies() { return false; }

	/**
	 * Visits all experiment children of this model.
	 * 
	 * <p>Experiments are top-level constructs defined within models. This method
	 * applies the {@link ISyntacticElement#EXPERIMENT_FILTER} to visit only
	 * experiment definitions.</p>
	 * 
	 * <p>This is essential for separating experiment processing from model compilation,
	 * as experiments have different validation and execution semantics.</p>
	 *
	 * @param visitor the visitor to apply to each experiment child
	 */
	@Override
	public void visitExperiments(final SyntacticVisitor visitor) {
		visitAllChildren(visitor, EXPERIMENT_FILTER);
	}

	/**
	 * Static visitor instance used for compacting the entire model tree.
	 * 
	 * <p>This visitor calls {@link ISyntacticElement#compact()} on each element
	 * to remove empty facet collections and reduce memory usage.</p>
	 */
	static SyntacticVisitor compacter = ISyntacticElement::compact;

	/**
	 * Compacts the entire model tree by removing empty facet collections.
	 * 
	 * <p>This optimization should be called after AST construction is complete and
	 * before semantic analysis begins. It recursively visits all elements in the tree
	 * and removes empty facet collections, significantly reducing memory footprint.</p>
	 * 
	 * <p><strong>Performance Impact:</strong> Can reduce memory usage by 10-20% for
	 * large models by eliminating overhead from empty collections.</p>
	 * 
	 * <p><strong>When to Call:</strong> After parsing is complete but before
	 * validation/compilation starts.</p>
	 */
	public void compactModel() {
		this.visitThisAndAllChildrenRecursively(compacter);
	}

	/**
	 * Returns the file system path to the directory containing this model.
	 * 
	 * <p>The path always ends with "/" for consistent path operations. This is used
	 * for resolving relative imports and file references within the model.</p>
	 *
	 * @return the normalized directory path ending with "/", or null for synthetic models
	 */
	public String getPath() { return path; }

	/**
	 * Returns the pragma directives defined in this model.
	 * 
	 * <p>Pragmas are compiler directives that affect compilation behavior. They are
	 * stored in the {@link IKeyword#PRAGMA} facet as a map from pragma name to
	 * a list of pragma values.</p>
	 * 
	 * <p><strong>Common Pragmas:</strong></p>
	 * <ul>
	 *   <li><strong>no_warning:</strong> Suppresses specific warning types</li>
	 *   <li><strong>no_info:</strong> Suppresses informational messages</li>
	 *   <li><strong>no_experiment_info:</strong> Hides experiment information</li>
	 * </ul>
	 * 
	 * <p><strong>Example GAML:</strong></p>
	 * <pre>{@code
	 * model MyModel {
	 *   #pragma no_warning "unused_variable"
	 *   // ... model content
	 * }
	 * }</pre>
	 *
	 * @return a map from pragma name to list of values, or null if no pragmas defined
	 */
	@Override
	@SuppressWarnings ("unchecked")
	public Map<String, List<String>> getPragmas() {
		if (hasFacet(IKeyword.PRAGMA))
			return (Map<String, List<String>>) this.getExpressionAt(IKeyword.PRAGMA).getExpression().getConstValue();
		return null;
	}

}
