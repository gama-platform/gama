/*******************************************************************************************************
 *
 * IGamlResourceInfoProvider.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import org.eclipse.emf.common.util.URI;

import gama.gaml.compilation.ast.ISyntacticElement;

/**
 * The Interface IGamlResourceInfoProvider.
 */
public interface IGamlResourceInfoProvider {

	/**
	 * Gets the info.
	 *
	 * @param uri
	 *            the uri
	 * @return the info
	 */
	default GamlFileInfo getInfo(final URI uri) {
		return getInfo(uri, 0l);
	}

	/**
	 * Gets the info.
	 *
	 * @param uri
	 *            the uri
	 * @param stamp
	 *            the stamp
	 * @return the info
	 */
	GamlFileInfo getInfo(final URI uri, final long stamp);

	/**
	 * Gets the contents.
	 *
	 * @param uri
	 *            the uri
	 * @return the contents
	 */
	ISyntacticElement getContents(URI uri);

}
