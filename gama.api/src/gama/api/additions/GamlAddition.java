/*******************************************************************************************************
 *
 * GamlAddition.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions;

import java.lang.reflect.AnnotatedElement;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.usage;
import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;

/**
 * The Class GamlAddition. Foundation for different subclasses that represent GAML artifacts (experiment, display, etc.).
 * This abstract class provides core functionality for managing GAML language additions, including name management,
 * plugin association, documentation generation, and Java annotation support.
 * 
 * <p>Each GamlAddition is backed by an AnnotatedElement (typically a Java class or method) and can automatically
 * generate documentation from the @doc annotation. Subclasses define specific types of GAML artifacts.</p>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 2 janv. 2024
 */
public abstract class GamlAddition implements IGamlDescription {

	/**
	 * The name of this GAML addition and the plugin that defines it.
	 * The name serves as the identifier for this GAML artifact.
	 * The plugin indicates which GAMA plugin contributes this addition.
	 */
	protected final String name, plugin;

	/**
	 * The cached documentation for this GAML addition. Lazily generated from the @doc annotation
	 * on the support element. Once created, it is reused for subsequent calls to getDocumentation().
	 */
	protected IGamlDocumentation documentation;

	/**
	 * The Java AnnotatedElement (typically a class or method) that provides the backing implementation
	 * for this GAML addition. Used to extract annotations such as @doc for documentation generation.
	 */
	protected final AnnotatedElement support;

	/**
	 * Instantiates a new GAML addition with the specified name, backing Java element, and defining plugin.
	 * This constructor initializes all core fields that identify and support this GAML artifact.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param name
	 *            the name of this GAML addition, which serves as its identifier
	 * @param support
	 *            the Java AnnotatedElement (class or method) providing the implementation
	 * @param plugin
	 *            the name of the plugin that contributes this GAML addition
	 * @date 2 janv. 2024
	 */
	public GamlAddition(final String name, final AnnotatedElement support, final String plugin) {
		this.name = name;
		this.plugin = plugin;
		this.support = support;
	}

	@Override
	public abstract String getTitle();

	/**
	 * Gets the @doc annotation from the support element if present. This annotation contains
	 * documentation metadata including description, usages, examples, and deprecation notices
	 * that are used to generate the GAML documentation for this addition.
	 *
	 * @return the @doc annotation from the support element, or null if the support is null
	 *         or the annotation is not present
	 */
	public doc getDocAnnotation() {
		return support != null && support.isAnnotationPresent(doc.class) ? support.getAnnotation(doc.class) : null;
	}

	/**
	 * Gets or creates the documentation for this GAML addition. This method lazily generates
	 * documentation from the @doc annotation on the support element. The documentation includes
	 * the main description, usage examples, code samples, and deprecation notices.
	 * 
	 * <p>The generated documentation is cached, so subsequent calls return the same instance
	 * without re-parsing the annotation.</p>
	 *
	 * @return the documentation object containing formatted information about this GAML addition,
	 *         or an empty documentation object if no @doc annotation is present
	 */
	@Override
	public IGamlDocumentation getDocumentation() {
		if (documentation == null) {
			final doc d = getDocAnnotation();
			if (d == null) {
				documentation = IGamlDocumentation.EMPTY_DOC;
			} else {
				documentation = new GamlRegularDocumentation(new StringBuilder(200));
				String s = d.value();
				if (s != null && !s.isEmpty()) { documentation.append(s).append("<br/>"); }
				usage[] usages = d.usages();
				for (usage u : usages) {
					documentation.append(u.value()).append("<br/><pre>");
					for (example e : u.examples()) {
						s = e.value();
						if (s != null && !s.isEmpty()) { documentation.append("<t/>&#x09;").append(s).append("<br/>"); }
					}
					documentation.append("</pre>");

				}
				s = d.deprecated();
				if (s != null && !s.isEmpty()) {
					documentation.append("<b>Deprecated</b>: ").append("<i>").append(s).append("</i><br/>");
				}
			}
		}
		return documentation;
	}

	/**
	 * Gets the name of the plugin that defines and contributes this GAML addition.
	 * This information is useful for tracking the origin of GAML language extensions
	 * and for managing plugin dependencies.
	 *
	 * @return the symbolic name of the plugin that defines this GAML addition
	 * @see gama.api.compilation.descriptions.IGamlDescription#getDefiningPlugin()
	 */
	@Override
	public String getDefiningPlugin() { return plugin; }

	/**
	 * Gets the name of this GAML addition, which serves as its unique identifier
	 * within the GAML language.
	 *
	 * @return the name of this GAML addition
	 * @see gama.api.utils.interfaces.INamed#getName()
	 */
	@Override
	public String getName() { return name; }

	/**
	 * Sets the name of this GAML addition. This implementation is a no-op because the name
	 * is declared as final and cannot be changed after construction. The name is immutable
	 * to maintain consistency in the GAML language definition.
	 *
	 * @param newName
	 *            the new name (ignored in this implementation)
	 * @see gama.api.utils.interfaces.INamed#setName(java.lang.String)
	 */
	@Override
	public void setName(final String newName) {}

	/**
	 * Serializes this GAML addition to its GAML representation. This base implementation
	 * simply returns the name of the addition. Subclasses may override this to provide
	 * more detailed serialization formats.
	 *
	 * @param includingBuiltIn
	 *            if true, includes built-in elements in the serialization (parameter not used in this base implementation)
	 * @return the GAML string representation, which is the name of this addition
	 * @see gama.api.utils.interfaces.IGamlable#serializeToGaml(boolean)
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return getName();
	}

	/**
	 * Gets the Java AnnotatedElement that provides the backing implementation for this GAML addition.
	 * This is typically a Java class or method that has been annotated to extend the GAML language.
	 *
	 * @return the AnnotatedElement (class or method) that supports this GAML addition
	 */
	public AnnotatedElement getJavaBase() { return support; }

}
