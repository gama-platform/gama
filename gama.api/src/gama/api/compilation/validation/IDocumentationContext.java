/*******************************************************************************************************
 *
 * IValidationContext.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.validation;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.descriptions.IModelDescription;

/**
 *
 */
public interface IDocumentationContext {

	/**
	 * Do document.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param description
	 *            the description
	 * @date 31 déc. 2023
	 */
	void doDocument(IModelDescription description);

	/**
	 * Sets the gaml documentation.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param e
	 *            the e
	 * @param d
	 *            the d
	 * @date 31 déc. 2023
	 */
	void setGamlDocumentation(EObject e, IGamlDescription d);

	/**
	 * Gets the uri.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the uri
	 * @date 10 janv. 2024
	 */
	URI getURI();

}