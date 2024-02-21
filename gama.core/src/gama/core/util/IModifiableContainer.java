/*******************************************************************************************************
 *
 * IModifiableContainer.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.util;

/**
 * Class ModifiableContainer.
 * 
 * @author drogoul
 * @since 24 janv. 2014
 * 
 */
public interface IModifiableContainer<K, V, KeyToAdd, ValueToAdd> extends IContainer<K, V>,
	IContainer.Modifiable<KeyToAdd, ValueToAdd> {

}
