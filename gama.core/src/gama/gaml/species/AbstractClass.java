/*******************************************************************************************************
 *
 * AbstractClass.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.species;

import java.util.Collection;
import java.util.Map;

import gama.core.util.GamaMapFactory;
import gama.gaml.compilation.ISymbol;
import gama.gaml.compilation.Symbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.statements.ActionStatement;
import gama.gaml.statements.IStatement.WithArgs;
import gama.gaml.variables.IVariable;

/**
 * The {@code AbstractClass} serves as a base class for species-related classes in the GAMA platform.
 * It encapsulates shared variables, actions, and a reference to a parent class.
 */
public abstract class AbstractClass extends Symbol {

	/**
	 * @param desc
	 */
	public AbstractClass(final IDescription desc) {
		super(desc);
		setName(description.getName());
	}

	/** A map containing the variables defined in this class. */
	protected final Map<String, IVariable> variables = GamaMapFactory.createOrdered();

	/** A map containing the actions defined in this class. */
	protected final Map<String, ActionStatement> actions = GamaMapFactory.createOrdered();

	/** The parent class of this class, if any. */
	protected AbstractClass parentClass;

	/**
	 * Retrieves the parent class of this class.
	 *
	 * @return the parent class, or {@code null} if none exists
	 */
	public AbstractClass getParentClass() { return parentClass; }

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {
		// Then we classify the children in their categories
		for (final ISymbol s : children) {
			if (s instanceof IVariable) {
				variables.put(s.getName(), (IVariable) s);
			} else if (s instanceof ActionStatement) {
				s.setEnclosing(this);
				actions.put(s.getName(), (ActionStatement) s);
			}
		}
		variables.forEach((n, v) -> v.setEnclosing(this));
	}

	/**
	 * Sets the parent class of this class.
	 *
	 * @param parentClass the parent class to set
	 */
	public void setParentClass(final AbstractClass parentClass) { this.parentClass = parentClass; }

	/**
	 * Retrieves the variables defined in this class.
	 *
	 * @return a map of variable names to their definitions
	 */
	public Map<String, IVariable> getVariables() { return variables; }

	/**
	 * Retrieves the actions defined in this class.
	 *
	 * @return a map of action names to their definitions
	 */
	public Collection<ActionStatement> getActions() { return actions.values(); }

	/**
	 * Gets the var.
	 *
	 * @param n
	 *            the n
	 * @return the var
	 */
	public IVariable getVar(final String n) {
		return variables.get(n);
	}

	/**
	 * Checks for var.
	 *
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	public boolean hasVar(final String name) {
		return variables.containsKey(name);
	}

	/**
	 * Gets the action.
	 *
	 * @param name
	 *            the name
	 * @return the action
	 */
	public WithArgs getAction(final String name) {
		return actions.get(name);
	}

	/**
	 * Adds a variable to this class.
	 *
	 * @param name the name of the variable
	 * @param variable the variable to add
	 */
	public void addVariable(final String name, final IVariable variable) {
		variables.put(name, variable);
	}

	/**
	 * Adds an action to this class.
	 *
	 * @param name the name of the action
	 * @param action the action to add
	 */
	public void addAction(final String name, final ActionStatement action) {
		actions.put(name, action);
	}

	/**
	 *
	 */
	@Override
	public void dispose() {
		// Dispose of resources if necessary
		for (IVariable variable : variables.values()) { variable.dispose(); }
		for (ActionStatement action : actions.values()) { action.dispose(); }
		variables.clear();
		actions.clear();
		parentClass = null;
	}
}