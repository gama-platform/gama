/**
 * 
 */
package gama.core.util.color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gama.api.data.objects.IColor;
import gama.api.data.objects.IMap;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.utils.color.GamaColor;
import gama.core.util.map.GamaMap;

/**
 * The Class GamaScale.
 */
@SuppressWarnings ("unchecked")
public class GamaScale extends GamaMap<Double, IColor> {

	/**
	 * Instantiates a new gama scale.
	 *
	 * @param scope
	 *            the scope
	 * @param values
	 *            the values
	 */
	public GamaScale(final IScope scope, final IMap<Double, IColor> values) {
		super(values.size(), Types.FLOAT, Types.COLOR);
		sort(values);
	}

	/**
	 * Sort.
	 *
	 * @param values
	 *            the values
	 */
	void sort(final Map<Double, IColor> values) {
		List<Map.Entry<Double, GamaColor>> entries = new ArrayList(values.entrySet());
		Collections.sort(entries, Comparator.comparing(Entry<Double, GamaColor>::getKey));
		for (Map.Entry<Double, GamaColor> entry : entries) { put(entry.getKey(), entry.getValue()); }
	}

}