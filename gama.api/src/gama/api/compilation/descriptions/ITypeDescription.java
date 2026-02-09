/*******************************************************************************************************
 *
 * ITypeDescription.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.descriptions;

import java.util.Collection;

import com.google.common.base.Function;

import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.kernel.skill.ISkill;

/**
 *
 */
public interface ITypeDescription extends IDescription {

	/** The to class. */
	Function<ITypeDescription, Class<? extends ISkill>> TO_CLASS = ITypeDescription::getJavaBase;

	/**
	 * @param name
	 * @return
	 */
	IVariableDescription getAttribute(String name);

	/**
	 * @return
	 */
	Collection<String> getAttributeNames();

	/**
	 * @param result
	 */
	void documentAttributes(IGamlDocumentation result);

	/**
	 * @return
	 */
	ITypeDescription getParent();

	/**
	 * @param varName
	 * @param b
	 * @return
	 */
	boolean hasAction(String varName, boolean b);

	/**
	 * @return
	 */
	Class getJavaBase();

	/**
	 * @param action
	 */
	IDescription addChild(IDescription action);

	/**
	 * @param varVisitor
	 */
	boolean visitOwnAttributes(DescriptionVisitor<IDescription> varVisitor);

	/**
	 * @param actionVisitor
	 */
	boolean visitOwnActions(DescriptionVisitor<IDescription> actionVisitor);

	/**
	 * Visit all attributes.
	 *
	 * @param visitor
	 *            the visitor
	 * @return true, if successful
	 */
	boolean visitAllAttributes(final DescriptionVisitor<IDescription> visitor);

	/**
	 * @return
	 */
	Iterable<IActionDescription> getActions();

	/**
	 * Gets the own actions.
	 *
	 * @return the own actions
	 */
	Iterable<IActionDescription> getOwnActions();

	/**
	 * @return
	 */
	Iterable<IVariableDescription> getAttributes();

	/**
	 * Gets the own attributes.
	 *
	 * @return the own attributes
	 */
	Iterable<IVariableDescription> getOwnAttributes();

	/**
	 * @return
	 */
	Collection<String> getActionNames();

	/**
	 * Sets the parent.
	 *
	 * @param parent
	 *            the new parent
	 */
	void setParent(final ITypeDescription parent);

	/**
	 * Adds the children.
	 *
	 * @param children
	 *            the children
	 */
	void addChildren(final Iterable<? extends IDescription> children);
}
