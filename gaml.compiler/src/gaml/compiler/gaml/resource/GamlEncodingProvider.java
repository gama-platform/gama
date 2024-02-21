/*******************************************************************************************************
 *
 * GamlEncodingProvider.java, in gaml.compiler.gaml, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.compiler.gaml.resource;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.parser.IEncodingProvider;

/**
 * Created by drogoul, 16 déc. 2013
 * 
 */

/**
 * Class GamlEncodingProvider.
 * 
 * @author drogoul
 * @since 16 déc. 2013
 * 
 */
public class GamlEncodingProvider implements IEncodingProvider {

	/**
	 * Method getEncoding()
	 * @see org.eclipse.xtext.parser.IEncodingProvider#getEncoding(org.eclipse.emf.common.util.URI)
	 */
	@Override
	public String getEncoding(final URI uri) {
		return "UTF-8";
	}

}
