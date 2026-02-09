/*******************************************************************************************************
 *
 * GamaBoolType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import java.io.File;

import gama.api.constants.IKeyword;
import gama.api.data.objects.IContainer;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.utils.files.IGamaFile;
import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;

/**
 * Written by drogoul Modified on 1 ao�t 2010
 *
 * @todo Description
 *
 */
@SuppressWarnings ("unchecked")
@type (
		name = IKeyword.BOOL,
		id = IType.BOOL,
		wraps = { Boolean.class, boolean.class },
		kind = ISymbolKind.Variable.REGULAR,
		doc = { @doc ("Represents boolean values, either true or false") },
		concept = { IConcept.TYPE, IConcept.LOGICAL, IConcept.CONDITION })
public class GamaBoolType extends GamaType<Boolean> {

	@Override
	@doc ("Casts parameter into a bool. false if the parameter is nil, equal to zero, empty or dead, depending on its type")
	public Boolean cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return staticCast(scope, obj, param, copy);
	}

	/**
	 * Static cast.
	 *
	 * @param scope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @param param
	 *            the param
	 * @param copy
	 *            the copy
	 * @return the boolean
	 */
	@SuppressWarnings ("rawtypes")
	public static Boolean staticCast(final IScope scope, final Object obj, final Object param, final boolean copy) {
		return switch (obj) {
			case null -> false;
			case Boolean b -> b;
			case IAgent a -> !a.dead();
			case IGamaFile f -> f.exists(scope);
			case IContainer c -> !c.isEmpty(scope);
			case File f -> f.exists();
			case Integer i -> i != 0;
			case Double d -> d != 0d;
			case String s -> "true".equals(s);
			default -> false;
		};
	}

	@Override
	public Boolean getDefault() { return false; }

	@Override
	public boolean canCastToConst() {
		return true;
	}

}
