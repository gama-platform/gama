/*******************************************************************************************************
 *
 * SyntacticAttributeElement.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.compilation.ast;

import org.eclipse.emf.ecore.EObject;

/**
 * The Class SyntacticAttributeElement.
 */
public class SyntacticAttributeElement extends SyntacticSingleElement {

	/**
	 * The name.
	 */
	final String name;

	/**
	 * Instantiates a new syntactic attribute element.
	 *
	 * @param keyword
	 *            the keyword
	 * @param name
	 *            the name
	 * @param statement
	 *            the statement
	 */
	public SyntacticAttributeElement(final String keyword, final String name, final EObject statement) {
		super(keyword, null, statement);
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gama.gaml.compilation.ast.AbstractSyntacticElement#toString()
	 */
	@Override
	public String toString() {
		return "Attribute " + getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gama.gaml.compilation.ast.AbstractSyntacticElement#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

}
