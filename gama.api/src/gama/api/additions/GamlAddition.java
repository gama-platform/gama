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
 * The Class GamlAddition. Foundation for different subclasses that represent GAML artifacts (experiment, display,...)
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @param <T>
 *            the generic type
 * @date 2 janv. 2024
 */
public abstract class GamlAddition implements IGamlDescription {

	/** The plugin. */
	protected final String name, plugin;

	/** The documentation. */
	protected IGamlDocumentation documentation;

	/** The support. */
	protected final AnnotatedElement support;

	/**
	 * Instantiates a new gaml addition.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param name
	 *            the plugin.
	 * @param support
	 *            the support.
	 * @param plugin
	 *            the plugin.
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
	 * Gets the doc annotation.
	 *
	 * @return the doc annotation
	 */
	public doc getDocAnnotation() {
		return support != null && support.isAnnotationPresent(doc.class) ? support.getAnnotation(doc.class) : null;
	}

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
	 * Method getDefiningPlugin()
	 *
	 * @see gama.api.compilation.descriptions.IGamlDescription#getDefiningPlugin()
	 */
	@Override
	public String getDefiningPlugin() { return plugin; }

	/**
	 * Method getName()
	 *
	 * @see gama.api.utils.INamed#getName()
	 */
	@Override
	public String getName() { return name; }

	/**
	 * Method setName()
	 *
	 * @see gama.api.utils.INamed#setName(java.lang.String)
	 */
	@Override
	public void setName(final String newName) {}

	/**
	 * Method serialize()
	 *
	 * @see gama.api.utils.IGamlable#serializeToGaml(boolean)
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return getName();
	}

	/**
	 * Gets the support.
	 *
	 * @return the support
	 */
	public AnnotatedElement getJavaBase() { return support; }

}
