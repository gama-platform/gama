/*******************************************************************************************************
 *
 * GamaBoolType.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.types;

import java.io.File;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IContainer;
import gama.core.util.file.GamaFile;

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
			case GamaFile f -> f.exists(scope);
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
