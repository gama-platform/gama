/*******************************************************************************************************
 *
 * GamaGradient.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.color;

import java.util.Map;

import gama.api.data.objects.IColor;
import gama.api.data.objects.IMap;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.color.GamaColorFactory;
import gama.core.util.map.GamaMap;

/**
 * The Class GamaGradient.
 */
@SuppressWarnings ("unchecked")
public class GamaGradient extends GamaMap<IColor, Double> {

	/**
	 * Instantiates a new gama gradient.
	 */
	public GamaGradient() {
		super(5, Types.COLOR, Types.FLOAT);
	}

	/**
	 * Sets the.
	 *
	 * @param scope
	 *            the scope
	 * @param values
	 *            the values
	 */
	public void set(final IScope scope, final IMap<Object, Object> values) {
		for (Map.Entry<Object, Object> entry : values.entrySet()) {
			this.put(GamaColorFactory.createFrom(scope, entry.getKey()), Cast.asFloat(scope, entry.getValue()));
		}
	}

}