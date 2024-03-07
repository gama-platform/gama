/*******************************************************************************************************
 *
 * IDiagramOpener.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor;

/**
 * The class IDiagramOpener.
 *
 * @author drogoul
 * @since 28 févr. 2024
 *
 */
public interface IDiagramOpener {

	/**
	 * Open.
	 */
	void open(GamlEditor editor);

	/**
	 * Close.
	 */
	void close(GamlEditor editor);

}
