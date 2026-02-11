/*******************************************************************************************************
 *
 * GamaPairType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
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
import gama.api.data.factories.GamaPairFactory;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IPair;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;

/**
 * Written by drogoul Modified on 1 ao�t 2010
 *
 * @todo Description
 *
 */
@type (
		name = IKeyword.PAIR,
		id = IType.PAIR,
		wraps = { IPair.class },
		kind = ISymbolKind.Variable.REGULAR,
		concept = { IConcept.TYPE, IConcept.CONTAINER },
		doc = @doc ("Represents a pair of 2 arbitrary elements"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaPairType extends GamaContainerType<IPair> {

	/**
	 * @param typesManager
	 * @param varKind
	 * @param id
	 * @param name
	 * @param support
	 */
	public GamaPairType(final ITypesManager typesManager) {
		super(typesManager);
	}

	@Override
	public IPair cast(final IScope scope, final Object obj, final Object param, final IType keyType,
			final IType contentsType, final boolean copy) {
		return GamaPairFactory.toPair(scope, obj, keyType, contentsType, copy);
	}

	@Override
	public int getNumberOfParameters() { return 2; }

	@Override
	public IType keyTypeIfCasting(final IExpression exp) {
		final IType itemType = exp.getGamlType();
		switch (itemType.id()) {
			case PAIR:
				return itemType.getKeyType();
			case MAP:
				return Types.LIST.of(itemType.getKeyType());
		}
		return itemType;
	}

	@Override
	public IType contentsTypeIfCasting(final IExpression exp) {
		final IType itemType = exp.getGamlType();
		switch (itemType.id()) {
			case MAP:
				return Types.LIST.of(itemType.getContentType());
			case PAIR:
				return itemType.getContentType();

		}
		return itemType;
	}

	@Override
	public IPair getDefault() { return GamaPairFactory.createDefault(); }

	@Override
	public IType getContentType() { return Types.get(NONE); }

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	public IPair deserializeFromJson(final IScope scope, final IMap<String, Object> map2) {
		IType requested = (IType) map2.remove("requested_type");
		return GamaPairFactory.toPair(scope, map2, requested.getKeyType(), requested.getContentType(), false);
	}

}
