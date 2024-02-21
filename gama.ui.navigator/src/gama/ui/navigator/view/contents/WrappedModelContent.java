/*******************************************************************************************************
 *
 * WrappedModelContent.java, in gama.ui.navigator.view, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.navigator.view.contents;

import gama.gaml.compilation.ast.ISyntacticElement;

/**
 * The Class WrappedModelContent.
 */
public class WrappedModelContent extends WrappedSyntacticContent {

	/**
	 * Instantiates a new wrapped model content.
	 *
	 * @param file the file
	 * @param e the e
	 */
	public WrappedModelContent(final WrappedGamaFile file, final ISyntacticElement e) {
		super(file, e, "Contents");
	}

	@Override
	public WrappedGamaFile getFile() {
		return (WrappedGamaFile) getParent();
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

}