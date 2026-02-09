/*******************************************************************************************************
 *
 * IDocManager.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.documentation;

import java.io.IOException;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.utils.files.CompressionUtils;

/**
 * The Interface IDocManager.
 */
// Internal interface instantiated by XText
public interface IDocManager {

	/** The null. */
	IDocManager NULL = new NullImpl();

	/**
	 * The Class DocumentationNode.
	 */
	record DocumentationNode(String title, byte[] doc) implements IGamlDescription {

		/**
		 * Instantiates a new documentation node.
		 *
		 * @param desc
		 *            the desc
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		public DocumentationNode(final IGamlDescription desc) {
			this(desc.getTitle(), CompressionUtils.zip(desc.getDocumentation().toString().getBytes()));
		}

		/**
		 * Gets the documentation.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @return the documentation
		 * @date 30 déc. 2023
		 */
		@Override
		public IGamlDocumentation getDocumentation() {
			return new GamlConstantDocumentation(new String(CompressionUtils.unzip(doc)));

		}

		/**
		 * Gets the title.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @return the title
		 * @date 30 déc. 2023
		 */
		@Override
		public String getTitle() { return title; }

	}

	/**
	 * The Class NullImpl.
	 */
	public static class NullImpl implements IDocManager {

		/**
		 * Do document.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @param description
		 *            the description
		 * @date 31 déc. 2023
		 */
		@Override
		public void doDocument(final URI uri, final IModelDescription description,
				final Map<EObject, IGamlDescription> additionalExpressions) {}

		@Override
		public IGamlDescription getGamlDocumentation(final EObject o) {
			return null;
		}

		/**
		 * Sets the gaml documentation.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @param object
		 *            the object
		 * @param description
		 *            the description
		 * @param replace
		 *            the replace
		 * @param force
		 *            the force
		 * @date 29 déc. 2023
		 */
		@Override
		public void setGamlDocumentation(final URI openResource, final EObject object,
				final IGamlDescription description) {}

		@Override
		public void invalidate(final URI key) {}

	}

	/**
	 * Document. Should be called after validation. Validates both the statements (from ModelDescription) and the
	 * expressions (Map)
	 *
	 * @param description
	 *            the description
	 * @param additionalExpressions
	 */
	void doDocument(URI resource, IModelDescription description, Map<EObject, IGamlDescription> additionalExpressions);

	/**
	 * Gets the gaml documentation.
	 *
	 * @param o
	 *            the o
	 * @return the gaml documentation
	 */
	IGamlDescription getGamlDocumentation(EObject o);

	/**
	 * Sets the gaml documentation.
	 *
	 * @param object
	 *            the object
	 * @param description
	 *            the description
	 * @param replace
	 *            the replace
	 * @param force
	 *            the force
	 */
	void setGamlDocumentation(URI openResource, final EObject object, final IGamlDescription description);

	/**
	 * Invalidate.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param key
	 *            the key
	 * @date 29 déc. 2023
	 */
	void invalidate(URI key);

}