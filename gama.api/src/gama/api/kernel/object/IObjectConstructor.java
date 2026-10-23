/*******************************************************************************************************
 *
 * IObjectConstructor.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.object;

import gama.api.runtime.scope.IScope;

/**
 * Creates a Java-backed GAML object. GAML attributes are initialized by the caller after this factory returns.
 */
@FunctionalInterface
public interface IObjectConstructor extends IConstructor<IObject, IScope, IClass> {

	@Override
	IObject create(IScope scope, IClass clazz);
}
