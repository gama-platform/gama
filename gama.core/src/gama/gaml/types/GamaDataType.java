package gama.gaml.types;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaData;

@type(
		name = IKeyword.DATA_TYPE,
		id = IType.COMPOSED,
		wraps = { GamaData.class },
		kind = ISymbolKind.DATA,
		concept = { IConcept.TYPE},
		doc = @doc("A data type in GAML, it can be composed of various elements such as dates, colors, numbers, etc.")
)
public class GamaDataType extends GamaType<GamaData> {

	@Override
	public GamaData getDefault() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canCastToConst() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public GamaData cast(IScope scope, Object obj, Object param, boolean copy) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		return null;
	}

}
