/*******************************************************************************************************
 *
 * ClassDescription.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.descriptions;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.descriptions.IClassDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.symbols.Facets;
import gama.api.kernel.object.IObject;

/**
 *
 */
public class ClassDescription extends TypeDescription implements IClassDescription {

	/**
	 * Instantiates a new class description.
	 *
	 * @param keyword
	 *            the keyword
	 * @param clazz
	 *            the clazz
	 * @param macroDesc
	 *            the macro desc
	 * @param parent
	 *            the parent
	 * @param cp
	 *            the cp
	 * @param source
	 *            the source
	 * @param facets
	 *            the facets
	 * @param plugin
	 *            the plugin
	 */
	public ClassDescription(final String keyword, final Class clazz, final IDescription macroDesc,
			final ITypeDescription parent, final Iterable<? extends IDescription> cp, final EObject source,
			final Facets facets, final String plugin) {
		super(keyword, clazz, macroDesc, parent, cp, source, facets, plugin);
	}

	@Override
	public boolean isBuiltIn() { return false; }

	@Override
	public String getTitle() { return IKeyword.CLASS + " " + getName(); }

	@Override
	public ClassDescription getParent() { return (ClassDescription) super.getParent(); }

	@Override
	public Class<? extends IObject> getJavaBase() { return IObject.class; }

	/**
	 * Document this.
	 *
	 * @param sb
	 *            the sb
	 */
	@Override
	public void documentThis(final IGamlDocumentation sb) {
		final String parentName = getParent() == null ? "nil" : getParent().getName();
		final String hostName = getEnclosingDescription() == null ? null : getEnclosingDescription().getName();
		sb.append("<b>Subclass of:</b> ").append(parentName).append("<br>");
		if (hostName != null) { sb.append("<b>Class defined in:</b> ").append(hostName).append("<br>"); }
		documentAttributes(sb);
		documentActions(sb);
	}

}