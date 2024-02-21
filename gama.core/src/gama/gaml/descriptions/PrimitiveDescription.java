/*******************************************************************************************************
 *
 * PrimitiveDescription.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.descriptions;

import java.lang.reflect.AccessibleObject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.core.common.interfaces.IKeyword;
import gama.dev.DEBUG;
import gama.gaml.compilation.IGamaHelper;
import gama.gaml.operators.Strings;
import gama.gaml.statements.Facets;

/**
 * The Class PrimitiveDescription. Singleton throughout the simulation
 */

/**
 * The Class PrimitiveDescription.
 */
@SuppressWarnings ({ "rawtypes" })
public class PrimitiveDescription extends ActionDescription {

	static {
		DEBUG.OFF();
	}

	/** The helper. */
	private IGamaHelper helper;

	/** The method. */
	private AccessibleObject method;

	/** The plugin. */
	private String plugin;

	/** The documentation. */
	private RegularDoc documentation;

	/**
	 * Instantiates a new primitive description.
	 *
	 * @param superDesc
	 *            the super desc
	 * @param source
	 *            the source
	 * @param children
	 *            the children
	 * @param facets
	 *            the facets
	 * @param plugin
	 *            the plugin
	 */
	public PrimitiveDescription(final IDescription superDesc, final EObject source,
			final Iterable<IDescription> children, final Facets facets, final String plugin) {
		super(IKeyword.PRIMITIVE, superDesc, children, source, facets);
		// DEBUG.OUT("Primitive " + name + " created");
		this.plugin = plugin;
	}

	@Override
	public boolean visitOwnChildrenRecursively(final DescriptionVisitor visitor) {
		return true;
	}

	@Override
	public boolean visitChildren(final DescriptionVisitor visitor) {
		return true;
	}

	@Override
	public boolean visitOwnChildren(final DescriptionVisitor visitor) {
		return true;
	}

	@Override
	public String getDefiningPlugin() { return plugin; }

	@Override
	public boolean validateChildren() {
		return true;
	}

	@Override
	public Doc getDocumentation() {
		if (documentation != null) return documentation;
		String s = getBuiltInDoc();
		// Only arguments
		documentation = new RegularDoc(s);
		if (getArgNames().size() > 0) {
			Map<String, arg> argAnnotations = getArgs();
			getFormalArgs().forEach(arg -> {
				final StringBuilder sb1 = new StringBuilder(100);
				String name = arg.getName();
				sb1.append(arg.getGamlType());
				if (arg.hasFacet(DEFAULT) && arg.getFacetExpr(DEFAULT) != null) {
					sb1.append(" <i>(default: ").append(arg.getFacetExpr(DEFAULT).serializeToGaml(false))
							.append(")</i>");
				}
				arg a = argAnnotations.get(name);
				if (a != null && a.doc().length > 0) { sb1.append("; ").append(a.doc()[0].value()); }
				documentation.set("Arguments accepted: ", name, new ConstantDoc(sb1.toString()));
			});
		}
		return documentation;
	}

	/**
	 * Gets the deprecated.
	 *
	 * @return the deprecated
	 */
	public String getDeprecated() {
		final doc d = getDocAnnotation();
		if (d == null) return null;
		String deprecated = d.deprecated();
		if (deprecated.isEmpty()) return null;
		return deprecated;
	}

	@Override
	public String getBuiltInDoc() {
		final doc d = getDocAnnotation();
		String s;
		if (d == null) {
			s = "";
		} else if (d.deprecated().isEmpty()) {
			s = d.value() + Strings.LN;
		} else {
			s = d.value() + Strings.LN + Strings.LN + d.deprecated() + Strings.LN;
		}
		return s;
	}

	/**
	 * Gets the doc annotation.
	 *
	 * @return the doc annotation
	 */
	public doc getDocAnnotation() {
		doc d = null;
		if (method != null && method.isAnnotationPresent(doc.class)) {
			d = method.getAnnotation(doc.class);
		} else if (method != null && method.isAnnotationPresent(action.class)) {
			final doc[] docs = method.getAnnotation(action.class).doc();
			if (docs.length > 0) { d = docs[0]; }
		}
		return d;
	}

	/**
	 * Gets the args.
	 *
	 * @return the args
	 */
	public Map<String, arg> getArgs() {
		if (method == null || !method.isAnnotationPresent(action.class)) return Collections.EMPTY_MAP;
		action annot = method.getAnnotation(action.class);
		arg[] list = annot.args();
		if (list.length == 0) return Collections.EMPTY_MAP;
		Map<String, arg> result = new LinkedHashMap<>();
		for (arg a : list) { result.put(a.name(), a); }
		return result;
	}

	/**
	 * Gets the helper.
	 *
	 * @return the helper
	 */
	public IGamaHelper getHelper() { return helper; }

	@Override
	public PrimitiveDescription validate() {
		return this;
	}

	/**
	 * Sets the helper.
	 *
	 * @param helper
	 *            the helper
	 * @param method
	 *            the method
	 */
	public void setHelper(final IGamaHelper helper, final AccessibleObject method) {
		this.helper = helper;
		this.method = method;
	}

	@Override
	public PrimitiveDescription copy(final IDescription into) {
		// DEBUG.OUT("Primitive " + name + " copied");
		return this;
	}

	/**
	 * @param plugin
	 *            name
	 */
	@Override
	public void setDefiningPlugin(final String plugin) { this.plugin = plugin; }

	@Override
	public void dispose() {}

	@Override
	public void setEnclosingDescription(final IDescription desc) {}
}
