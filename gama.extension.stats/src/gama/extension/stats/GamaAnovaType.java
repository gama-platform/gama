/*******************************************************************************************************
 *
 * GamaAnovaType.java, in gama.extension.stats, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.stats;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.runtime.scope.IScope;
import gama.api.types.misc.IValue;

/**
 * The Class GamaAnovaType.
 */
@type (
		name = "anova",
		id = IType.ANOVA,
		wraps = { GamaAnova.class, GamaMultiAnova.class },
		kind = ISymbolKind.REGULAR,
		concept = { IConcept.TYPE },
		doc = { @doc (
				value = "Type of variables that enables to perform an ANOVA test") })
public class GamaAnovaType extends GamaType<IValue> {

	/**
	 * @param typesManager
	 */
	public GamaAnovaType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	@doc ("Returns the argument if it is an anova, otherwise nil")
	public IValue cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof GamaAnova || obj instanceof GamaMultiAnova) return (IValue) obj;
		return null;
	}

	@Override
	public IValue getDefault() { return null; }

}
