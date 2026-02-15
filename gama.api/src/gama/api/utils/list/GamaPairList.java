/*******************************************************************************************************
 *
 * GamaPairList.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.list;

import java.util.Map;

import gama.api.data.objects.IMap;
import gama.api.data.objects.IMap.IPairList;
import gama.api.gaml.types.Types;

/**
 * The Class IPairList.
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value type
 */
public class GamaPairList<K, V> extends GamaList<Map.Entry<K, V>> implements IPairList<K, V> {

	/**
	 * Instantiates a new gama pair list.
	 *
	 * @param map
	 *            the map
	 */
	public GamaPairList(final IMap<K, V> map) {
		super(map.size(), Types.PAIR.of(map.getGamlType().getKeyType(), map.getGamlType().getContentType()));
	}

}