/*******************************************************************************************************
 *
 * GamaDataType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.types;

import java.util.HashMap;
import java.util.Map;

import gama.annotations.precompiler.ITypeProvider;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaData;
import gama.gaml.descriptions.DataTypeDescription;
import gama.gaml.descriptions.OperatorProto;

// @type(
// name = IKeyword.DATA_TYPE,
// id = IType.COMPOSED,
// wraps = { GamaData.class },
// kind = ISymbolKind.DATA,
// concept = { IConcept.TYPE},
// doc = @doc("A data type in GAML, it can be composed of various elements such as dates, colors, numbers, etc.")
/**
 * The Class GamaDataType.
 */
// )
public class GamaDataType extends GamaType<GamaData> {

	/** The data. */
	private final DataTypeDescription data;

	/**
	 * Instantiates a new gama data type.
	 */
	public GamaDataType() {
		this(null, IKeyword.DATA_TYPE, IType.COMPOSED, GamaData.class);
	}

	/**
	 * Instantiates a new gama data type.
	 *
	 * @param data
	 *            the data
	 * @param name
	 *            the name
	 * @param dataId
	 *            the data id
	 * @param base
	 *            the base
	 */
	public GamaDataType(final DataTypeDescription data, final String name, final int dataId, final Class<GamaData> base) {
		this.data = data;
		this.name = name;
		id = dataId;
		support = base;
		if (data != null) { setDefiningPlugin(data.getDefiningPlugin()); }
		if (data != null) { initializeFieldGetters(); }
	}

	/**
	 * Initialize field getters.
	 */
	private void initializeFieldGetters() {
		getters = new HashMap<>();
		data.visitAllAttributes(a -> {
			OperatorProto p = new OperatorProto(name, null, (s, o) -> ((GamaData) o[0]).getFieldValue(a.getName()),
					true, true, a.getGamlType().id(), a.getGamlType().toClass(), ITypeProvider.NONE, ITypeProvider.NONE,
					ITypeProvider.NONE, new int[] {});
			getters.put(a.getName(), p);
			return true;
		});
	}

	@Override
	public DataTypeDescription getData() { return data; }

	@Override
	public String getDataName() { return name; }

	@Override
	public boolean isDataType() { return true; }

	@Override
	public GamaData getDefault() { return null; }

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public GamaData cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj == null) return null;
		if (obj instanceof GamaData gd) return gd;
		return null;
	}

	@Override
	public void setFieldGetters(final Map<String, OperatorProto> map) {}

}
