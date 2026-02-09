/*******************************************************************************************************
 *
 * AbstractProto.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.prototypes;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collections;

import gama.annotations.doc;
import gama.annotations.usage;
import gama.api.additions.GamlAddition;
import gama.api.compilation.prototypes.IArtefactProto;

/**
 * Class AbstractProto.
 *
 * @author drogoul
 * @since 17 déc. 2014
 *
 */
public abstract class AbstractProto extends GamlAddition implements IArtefactProto {

	/** The deprecated. */
	protected String deprecated;

	/**
	 * Instantiates a new abstract proto.
	 *
	 * @param name
	 *            the name
	 * @param support
	 *            the support
	 * @param plugin
	 *            the plugin
	 */
	protected AbstractProto(final String name, final AnnotatedElement support, final String plugin) {
		super(name, support, plugin);
	}

	/**
	 * Gets the deprecated.
	 *
	 * @return the deprecated
	 */
	@Override
	public String getDeprecated() {
		if (deprecated != null) return deprecated.isEmpty() ? null : deprecated;
		final doc d = getDocAnnotation();
		if (d == null) return null;
		deprecated = d.deprecated();
		if (deprecated.isEmpty()) return null;
		return deprecated;
	}

	/**
	 * Gets the main doc.
	 *
	 * @return the main doc
	 */
	@Override
	public String getMainDoc() {
		final doc d = getDocAnnotation();
		if (d == null) return null;
		final String s = d.value();
		if (s.isEmpty()) return null;
		return s;
	}

	/**
	 * Method getTitle()
	 *
	 * @see gama.api.compilation.descriptions.IGamlDescription#getTitle()
	 */
	@Override
	public String getTitle() { return ""; }

	/**
	 * Gets the usages.
	 *
	 * @return the usages
	 */
	@Override
	public Iterable<usage> getUsages() {
		final doc d = getDocAnnotation();
		if (d != null) {
			final usage[] tt = d.usages();
			if (tt.length > 0) return Arrays.asList(tt);
		}
		return Collections.EMPTY_LIST;
	}

}
