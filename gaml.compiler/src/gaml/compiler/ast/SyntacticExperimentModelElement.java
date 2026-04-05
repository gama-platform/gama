/*******************************************************************************************************
 *
 * SyntacticExperimentModelElement.java, in gaml.compiler, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ast;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.ast.ISyntacticElement;

/**
 * Represents a synthetic model element that wraps a standalone experiment.
 * 
 * <p>This specialized model element is created when an experiment is loaded or executed
 * independently without a full model context. It creates a minimal model structure that
 * contains only the experiment, allowing the compilation pipeline to process it normally.</p>
 * 
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li><strong>Headless Execution:</strong> Running experiments from command line</li>
 *   <li><strong>Remote Execution:</strong> Server-side experiment execution</li>
 *   <li><strong>Testing:</strong> Unit testing individual experiments</li>
 *   <li><strong>Batch Processing:</strong> Running experiments without the full model</li>
 * </ul>
 * 
 * <p><strong>Key Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Single Child:</strong> Always contains exactly one {@link SyntacticExperimentElement}</li>
 *   <li><strong>Name Delegation:</strong> The model's name is derived from the experiment's name</li>
 *   <li><strong>Synthetic Structure:</strong> Created programmatically, not parsed from source</li>
 *   <li><strong>Path Support:</strong> Maintains file path for error reporting</li>
 * </ul>
 * 
 * <p><strong>Structure:</strong></p>
 * <pre>
 * SyntacticExperimentModelElement (keyword: "experiment_model", path: "...")
 *   └── SyntacticExperimentElement (keyword: "experiment", name: "...")
 *       └── [experiment children: parameters, outputs, etc.]
 * </pre>
 * 
 * <p><strong>Design Note:</strong> This class demonstrates the Adapter pattern, wrapping
 * an experiment to make it look like a complete model from the compiler's perspective.</p>
 * 
 * <p><strong>Thread Safety:</strong> NOT thread-safe. Should only be accessed during
 * compilation phases from a single thread.</p>
 *
 * @author drogoul
 * @since 9 sept. 2013
 * @see SyntacticModelElement
 * @see SyntacticExperimentElement
 * @see SyntacticFactory#createExperimentModel(EObject, EObject, String)
 */
public class SyntacticExperimentModelElement extends SyntacticModelElement {

	/**
	 * Constructs a new synthetic experiment model wrapping a standalone experiment.
	 * 
	 * <p>This constructor is called by {@link SyntacticFactory#createExperimentModel}
	 * to create a minimal model structure around an experiment. The facets are set to
	 * null as the model properties come from the contained experiment.</p>
	 *
	 * @param keyword the GAML keyword, should be {@link ISyntacticFactory#EXPERIMENT_MODEL}
	 * @param root    the root EObject representing the experiment model
	 * @param path    the file path for error reporting and resource location, may be null
	 */
	public SyntacticExperimentModelElement(final String keyword, final EObject root, final String path) {
		super(keyword, null, root, path);
	}

	/**
	 * Adds a child element and automatically propagates the experiment's name to this model.
	 * 
	 * <p>This override ensures that when the experiment child is added, its name becomes
	 * this model's name. This is essential because:</p>
	 * <ul>
	 *   <li>The model needs a name for identification in error messages</li>
	 *   <li>The experiment's name is the most meaningful identifier</li>
	 *   <li>It maintains consistency with user expectations</li>
	 * </ul>
	 * 
	 * <p><strong>Design Pattern:</strong> This implements the Decorator pattern by
	 * augmenting the base addChild behavior with name propagation.</p>
	 * 
	 * <p><strong>Validation Note:</strong> In normal usage, only one child (the experiment)
	 * should be added. Multiple calls would overwrite the name with the last child's name.</p>
	 *
	 * @param e the child element to add, typically a {@link SyntacticExperimentElement}
	 */
	@Override
	public void addChild(final ISyntacticElement e) {
		super.addChild(e);
		setFacet(IKeyword.NAME, e.getExpressionAt(IKeyword.NAME));
	}

	/**
	 * Returns the wrapped experiment element.
	 * 
	 * <p>This convenience method provides typed access to the single experiment child.
	 * It assumes the first (and only) child is an experiment, which should always be
	 * true for properly constructed experiment model elements.</p>
	 * 
	 * <p><strong>Precondition:</strong> {@code children[0]} must exist and be a
	 * {@link SyntacticExperimentElement}. This is guaranteed by the factory method
	 * that creates these elements.</p>
	 *
	 * @return the experiment child element
	 * @throws ArrayIndexOutOfBoundsException if no children have been added (shouldn't happen)
	 * @throws ClassCastException if the child is not an experiment (shouldn't happen)
	 */
	public SyntacticExperimentElement getExperiment() { return (SyntacticExperimentElement) children[0]; }
}