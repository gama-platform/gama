/*******************************************************************************************************
 *
 * GamaDataframeType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.dataframe.GamaDataframe;
import gama.api.types.dataframe.GamaDataframeFactory;
import gama.api.types.dataframe.IDataframe;

/**
 * Type representing tabular data (dataframes) in GAML.
 *
 * <p>
 * A dataframe stores tabular data with named columns. It is a container type keyed by column name (String) and
 * containing Object values. Dataframes can be created from CSV, Excel, or JSON files, or constructed programmatically.
 * </p>
 *
 * @author GAMA Team
 * @see IDataframe
 * @see GamaDataframe
 * @see GamaDataframeFactory
 */
@type (
		name = IKeyword.DATAFRAME,
		id = IType.DATAFRAME,
		wraps = { IDataframe.class, GamaDataframe.class },
		kind = ISymbolKind.CONTAINER,
		concept = { IConcept.TYPE, IConcept.CONTAINER, IConcept.DATAFRAME },
		doc = @doc ("Tabular data with named columns. Dataframes can be loaded from CSV, Excel or JSON files."))
public class GamaDataframeType extends GamaContainerType<IDataframe> {

	/**
	 * Constructs a new dataframe type with the specified types manager.
	 *
	 * @param typesManager
	 *            the types manager that owns this type
	 */
	public GamaDataframeType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@Override
	@doc ("Casts the operand into a dataframe. If already a dataframe, returns it (or a copy). Lists of lists and maps can be converted.")
	public IDataframe cast(final IScope scope, final Object obj, final Object param, final IType<?> keyType,
			final IType<?> contentType, final boolean copy) throws GamaRuntimeException {
		return GamaDataframeFactory.castToDataframe(scope, obj, copy);
	}

	@Override
	public IType<?> getKeyType() {
		return Types.STRING;
	}

	@Override
	public IType<?> contentsTypeIfCasting(final IExpression exp) {
		return Types.NO_TYPE;
	}

	@Override
	public IType<?> keyTypeIfCasting(final IExpression exp) {
		return Types.STRING;
	}

	@Override
	public boolean canCastToConst() {
		return false;
	}
}
