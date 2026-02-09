/*******************************************************************************************************
 *
 * InternalGamaPairFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util;

import gama.api.data.factories.GamaListFactory;
import gama.api.data.factories.IPairFactory;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IPair;
import gama.api.data.objects.IShape;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.core.geometry.DynamicLineString;

/**
 * A factory for creating IPair objects.
 */
public class InternalGamaPairFactory implements IPairFactory {

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param keyType
	 *            the key type
	 * @param contentsType
	 *            the contents type
	 * @param copy
	 *            the copy
	 * @return the gama pair
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	public IPair createFrom(final IScope scope, final Object obj, final IType keyType, final IType contentsType,
			final boolean copy) throws GamaRuntimeException {
		Object key, value;
		if (obj instanceof IPair p) {
			key = p.getKey();
			value = p.getValue();
		} else
		// 8/01/14: No more automatic casting between points and pairs (as
		// points can have 3 coordinates)
		if (obj instanceof IShape s && s.getInnerGeometry() instanceof DynamicLineString) {
			final DynamicLineString g = (DynamicLineString) s.getInnerGeometry();
			key = g.getSource();
			value = g.getTarget();
		} else if (obj instanceof IMap m) {
			if (m.containsKey("key") && m.containsKey("value")) {
				key = m.get("key");
				value = m.get("value");
			} else {
				key = GamaListFactory.create(scope, m.getGamlType().getKeyType(), m.keySet());
				value = GamaListFactory.create(scope, m.getGamlType().getContentType(), m.values());
			}
		} else {
			// 8/01/14 : Change of behavior: now returns a pair object::object
			key = obj;
			value = obj;
		}
		final IType kt = keyType == null || keyType == Types.NO_TYPE ? GamaType.of(key) : keyType;
		final IType ct = contentsType == null || contentsType == Types.NO_TYPE ? GamaType.of(value) : contentsType;
		return new GamaPair<>(GamaType.toType(scope, key, kt, copy), GamaType.toType(scope, value, ct, copy), kt, ct);
	}

	/**
	 * Creates a new IPair object.
	 *
	 * @return the i pair
	 */
	@Override
	public IPair createDefault() {
		return new GamaPair<>(null, null, Types.NO_TYPE, Types.NO_TYPE);
	}

	/**
	 * Creates the.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the i pair
	 */
	@Override
	public IPair create(final Object key, final Object value) {
		return new GamaPair<>(key, value, GamaType.of(key), GamaType.of(value));
	}

	/**
	 * Creates a new InternalGamaPair object.
	 *
	 * @param v1
	 *            the v 1
	 * @param v2
	 *            the v 2
	 * @param keyType
	 *            the key type
	 * @param valueType
	 *            the value type
	 * @return the i pair
	 */
	@Override
	public IPair createFrom(final Object v1, final Object v2, final IType keyType, final IType valueType) {
		return new GamaPair<>(v1, v2, keyType, valueType);
	}

}
