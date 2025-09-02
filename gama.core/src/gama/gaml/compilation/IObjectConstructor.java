/*******************************************************************************************************
 *
 * IObjectConstructor.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.compilation;

import gama.core.metamodel.agent.IObject;
import gama.gaml.species.IClass;

/**
 *
 */
public interface IObjectConstructor {

	/** The default. */
	IObjectConstructor DEFAULT = new IObjectConstructor() {
		@Override
		public <T extends IObject> T createOneObject(final IClass clazz) {
			// TODO Just here for implementation convenience - should be removed asap and replaced by a proper factory
			return null;
		}
	};

	/**
	 * Creates the one agent.
	 *
	 * @param <T>
	 *            the generic type
	 * @param manager
	 *            the manager
	 * @param index
	 *            the index
	 * @return the t
	 */
	<T extends IObject> T createOneObject(IClass clazz);

}
