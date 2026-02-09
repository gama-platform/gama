/*******************************************************************************************************
 *
 * GamaMapType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaMapFactory;
import gama.api.data.objects.IMap;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;

/**
 * The Class GamaMapType.
 */
@type (
		name = IKeyword.MAP,
		id = IType.MAP,
		wraps = { IMap.class },
		kind = ISymbolKind.Variable.CONTAINER,
		concept = { IConcept.TYPE, IConcept.CONTAINER, IConcept.MAP },
		doc = @doc ("Represents lists of pairs key::value, where each key is unique in the map. Maps are ordered by the insertion order of elements"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaMapType extends GamaContainerType<IMap> {

	@Override
	@doc ("Casts the operand into a map. In case of an agent, returns its attributes. In case of a string, tries to parse JSON contents and returns a corresponding map.")
	public IMap cast(final IScope scope, final Object obj, final Object param, final IType keyType,
			final IType contentType, final boolean copy) throws GamaRuntimeException {
		return GamaMapFactory.createFrom(scope, obj, keyType, contentType, copy);
	}

	@Override
	public int getNumberOfParameters() { return 2; }

	@Override
	public IType keyTypeIfCasting(final IExpression exp) {
		final IType itemType = exp.getGamlType();
		if (itemType.isAgentType()) return Types.STRING;
		switch (itemType.id()) {
			case STRING:
				return Types.STRING;
			case PAIR:
			case MAP:
				return itemType.getKeyType();
			case MATRIX:
				return itemType.getContentType();
			case GRAPH:
				return Types.PAIR;
			case LIST:
				if (itemType.getContentType().id() == IType.PAIR) return itemType.getContentType().getKeyType();
				return itemType.getContentType();
		}
		return itemType;
	}

	@Override
	public IType contentsTypeIfCasting(final IExpression exp) {
		final IType itemType = exp.getGamlType();
		if (itemType.isAgentType()) return Types.NO_TYPE;
		switch (itemType.id()) {
			case STRING:
				return Types.NO_TYPE;
			case LIST:
				if (itemType.getContentType().id() == IType.PAIR) return itemType.getContentType().getContentType();
				return itemType.getContentType();
			case PAIR:
			case GRAPH:
			case MAP:
			case MATRIX:
				return itemType.getContentType();

		}
		return itemType;
	}

	@Override
	public boolean canCastToConst() {
		return true;
	}
}
