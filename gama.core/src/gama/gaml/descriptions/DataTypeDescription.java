/*******************************************************************************************************
 *
 * DataTypeDescription.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.descriptions;

import org.eclipse.emf.ecore.EObject;

import gama.core.util.GamaData;
import gama.gaml.statements.Facets;

/**
 * The Class DataTypeDescription.
 */
public class DataTypeDescription extends TypeDescription {

	/**
	 * Instantiates a new data type description.
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
	public DataTypeDescription(final String keyword, final Class clazz, final IDescription macroDesc,
			final TypeDescription parent, final Iterable<? extends IDescription> cp, final EObject source,
			final Facets facets, final String plugin) {
		super(keyword, clazz, macroDesc, parent, cp, source, facets, plugin);
	}

	@Override
	public boolean isBuiltIn() { return false; }

	@Override
	public String getTitle() { return "data_type " + getName(); }

	@Override
	public DataTypeDescription getParent() { return (DataTypeDescription) super.getParent(); }

	@Override
	public Class getJavaBase() { return GamaData.class; }

	@Override
	public IDescription addChild(final IDescription child) {
		super.addChild(child);
		switch (child) {
			case ActionDescription ad:
				addAction(ad);
				break;
			case VariableDescription vd:
				addOwnAttribute(vd);
				break;
			default:
		}
		return child;
	}
}