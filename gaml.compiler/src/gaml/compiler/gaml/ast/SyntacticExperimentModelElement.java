/**
 * 
 */
package gaml.compiler.gaml.ast;

import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.ast.ISyntacticElement;
import gama.api.constants.IKeyword;

/**
 * The Class SyntacticExperimentModelElement.
 */
public class SyntacticExperimentModelElement extends SyntacticModelElement {

	/**
	 * Instantiates a new syntactic experiment model element.
	 *
	 * @param keyword
	 *            the keyword
	 * @param root
	 *            the root
	 * @param path
	 *            the path
	 */
	public SyntacticExperimentModelElement(final String keyword, final EObject root, final String path) {
		super(keyword, null, root, path);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gama.gaml.compilation.ast.SyntacticComposedElement#addChild(gama.gaml.compilation.ast.ISyntacticElement)
	 */
	@Override
	public void addChild(final ISyntacticElement e) {
		super.addChild(e);
		setFacet(IKeyword.NAME, e.getExpressionAt(IKeyword.NAME));
	}

	/**
	 * Gets the experiment.
	 *
	 * @return the experiment
	 */
	public SyntacticExperimentElement getExperiment() { return (SyntacticExperimentElement) children[0]; }
}