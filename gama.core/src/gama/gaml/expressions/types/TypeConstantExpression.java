/*******************************************************************************************************
 *
 * TypeConstantExpression.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.expressions.types;

import gama.annotations.precompiler.GamlProperties;
import gama.gaml.descriptions.TypeDescription;
import gama.gaml.expressions.ConstantExpression;
import gama.gaml.types.IType;

/**
 *
 */
public abstract class TypeConstantExpression extends ConstantExpression {

	/**
	 * @param val
	 * @param t
	 * @param name
	 */
	public TypeConstantExpression(final Object val, final IType<?> t) {
		super(val, t);
	}

	@Override
	public boolean isContextIndependant() { return false; }

	@Override
	public boolean isAllowedInParameters() { return true; } // verify this

	/**
	 * Method collectPlugins()
	 *
	 * @see gama.gaml.interfaces.IGamlDescription#collectPlugins(java.util.Set)
	 */
	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		final TypeDescription sd = getGamlType().getContentType().getSpecies();
		if (sd != null) {
			meta.put(GamlProperties.PLUGINS, sd.getDefiningPlugin());
			if (sd.isBuiltIn()) { meta.put(GamlProperties.SPECIES, (String) value); }
		}
	}

}
