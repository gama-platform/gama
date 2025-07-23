package gama.gaml.types;

import org.apache.commons.lang3.NotImplementedException;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaData;
import gama.gaml.descriptions.DataDescription;

@type(
		name = IKeyword.DATA_TYPE,
		id = IType.COMPOSED,
		wraps = { GamaData.class },
		kind = ISymbolKind.DATA,
		concept = { IConcept.TYPE},
		doc = @doc("A data type in GAML, it can be composed of various elements such as dates, colors, numbers, etc.")
)
public class GamaDataType extends GamaType<GamaData> {

	
	private DataDescription data;
	
	public GamaDataType() {
		this(null, IKeyword.DATA_TYPE, IType.COMPOSED, GamaData.class);
	}
	
	public GamaDataType(final DataDescription data, final String name, final int dataId, final Class<GamaData> base) {
		this.data = data;
		this.name = name;
		id = dataId;
		support = base;
		if (data != null) { setDefiningPlugin(data.getDefiningPlugin()); }
	}
	
	@Override
	public DataDescription getData() {
		return data;
	}
	
	@Override
	public String getDataName() {
		return name;
	}
	
	@Override
	public boolean isDataType() { return true;}
	
	@Override
	public GamaData getDefault() {
		throw new NotImplementedException("getDefault is not implemented yet.");
	}

	@Override
	public boolean canCastToConst() {
		throw new NotImplementedException("canCastToConst is not implemented yet.");
	}

	@Override
	public GamaData cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
		throw new NotImplementedException("Casting to GamaData is not implemented yet.");
	}

}
