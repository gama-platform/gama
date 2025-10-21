/*******************************************************************************************************
 *
 * IClass.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.species;

import java.util.Collection;
import java.util.List;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.interfaces.ITyped;
import gama.core.kernel.model.GamlModelSpecies;
import gama.core.metamodel.agent.GamlObject;
import gama.core.metamodel.agent.IObject;
import gama.core.runtime.IScope;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.TypeDescription;
import gama.gaml.interfaces.IGamlable;
import gama.gaml.interfaces.IJsonable;
import gama.gaml.operators.Containers;
import gama.gaml.statements.ActionStatement;
import gama.gaml.statements.IStatement;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import gama.gaml.variables.IVariable;
import one.util.streamex.StreamEx;

/**
 * The {@code IClass} interface represents a class in the GAMA modeling platform. It defines the structure and behavior
 * of species, including their variables, actions and parent-child relationships.
 */

@vars ({ @variable (
		name = IClass.ACTIONS,
		type = IType.LIST,
		of = IType.STRING,
		doc = @doc ("A list of the names of the actions defined in this species/class (incl. the ones inherited from its parent)")),

		@variable (
				name = IKeyword.ATTRIBUTES,
				type = IType.LIST,
				of = IType.STRING,
				doc = @doc ("A list of the names of the attributes of this species/class (incl. the ones inherited from its parent)")),
		@variable (
				name = IKeyword.PARENT,
				type = IType.SPECIES,
				doc = @doc ("The parent (if any) of this species/class - 'model' for models, 'experiment' for experiments and 'agent' for species with no explicit parent, 'object' for classes with no explicit parent")),
		@variable (
				name = IKeyword.NAME,
				type = IType.STRING,
				doc = @doc ("The name of the species/class")),
		@variable (
				name = IClass.SUBSPECIES,
				type = IType.LIST,
				of = IType.SPECIES,
				doc = @doc ("A list of the names of children of this species/class, i.e. its direct subspecies/subclasses")) })
public interface IClass extends ISymbol, IGamlable, ITyped, IJsonable {

	/** The subspecies. */
	String SUBSPECIES = "children";

	/** The actions. */
	String ACTIONS = "actions";

	/**
	 * Checks if this species extends another species.
	 *
	 * @param s
	 *            the species to check against
	 * @return {@code true} if this species extends the given species, {@code false} otherwise
	 */
	boolean extendsSpecies(final IClass s);

	/**
	 * Returns all the direct subspecies of this species, properly typed for GAMA.
	 *
	 * @param scope
	 *            the current simulation scope
	 * @return a list of direct subspecies
	 */

	IList<? extends IClass> getSubSpecies(IScope scope);

	/**
	 * Returns the names of all direct subspecies of this species.
	 *
	 * @param scope
	 *            the current simulation scope
	 * @return a list of subspecies names
	 */
	@SuppressWarnings ("unchecked")
	@getter (SUBSPECIES)
	@doc ("Returns all the direct subspecies names of this species")
	default IList<String> getSubSpeciesNames(final IScope scope) {
		return StreamEx.of(getSubSpecies(scope)).map(IClass::getName).toCollection(Containers.listOf(Types.STRING));
	}

	/**
	 * Gets the name of this species.
	 *
	 * @return the name of the species
	 */
	@Override
	@getter (IKeyword.NAME)
	String getName();

	/**
	 * Returns the direct parent of this species. Experiments, models, and species with no explicit parents will return
	 * {@code null}.
	 *
	 * @return the parent species, or {@code null} if none exists
	 */
	@getter (IKeyword.PARENT)
	@doc ("Returns the direct parent of the species. Experiments, models and species with no explicit parents will return nil")
	IClass getParentSpecies();

	/**
	 * Returns this species along with all its parent species.
	 *
	 * @return a list of this species and its parents
	 */
	List<? extends IClass> getSelfWithParents();

	/**
	 * Retrieves an action by its name.
	 *
	 * @param name
	 *            the name of the action
	 * @return the action, or {@code null} if not found
	 */
	IStatement.WithArgs getAction(final String name);

	/**
	 * Returns the names of all actions defined in this species, including inherited ones.
	 *
	 * @param scope
	 *            the current simulation scope
	 * @return a list of action names
	 */
	@getter (ACTIONS)
	@doc ("retuns the list of actions defined in this species (incl. the ones inherited from its parent)")
	default IList<String> getActionNames(final IScope scope) {
		return GamaListFactory.create(scope, Types.STRING,
				StreamEx.of(getActions()).map(ActionStatement::getName).toList());
	}

	/**
	 * Returns all actions defined in this species.
	 *
	 * @return a collection of actions
	 */
	Collection<ActionStatement> getActions();

	/**
	 * Gets the name of the parent species.
	 *
	 * @return the name of the parent species
	 */
	String getParentName();

	/**
	 * Retrieves a variable by its name.
	 *
	 * @param n
	 *            the name of the variable
	 * @return the variable, or {@code null} if not found
	 */
	IVariable getVar(final String n);

	/**
	 * Returns the names of all variables defined in this species.
	 *
	 * @return a collection of variable names
	 */
	Collection<String> getVarNames();

	/**
	 * Returns a list of all attributes defined in this species, including inherited ones.
	 *
	 * @param scope
	 *            the current simulation scope
	 * @return a list of attribute names
	 */
	@getter (IKeyword.ATTRIBUTES)
	@doc ("retuns the list of attributes defined in this species (incl. the ones inherited from its parent)")
	default IList<String> getAttributeNames(final IScope scope) {
		return GamaListFactory.create(scope, Types.STRING, getVarNames());
	}

	/**
	 * Returns all variables defined in this species.
	 *
	 * @return a collection of variables
	 */
	Collection<IVariable> getVars();

	/**
	 * Checks if a variable with the given name exists in this species.
	 *
	 * @param name
	 *            the name of the variable
	 * @return {@code true} if the variable exists, {@code false} otherwise
	 */
	boolean hasVar(final String name);

	/**
	 * Returns the description of this species.
	 *
	 * @return the type description of the species
	 */

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	@Override
	TypeDescription getDescription();

	/**
	 * @param gamlModelSpecies
	 */
	void setMacroSpecies(GamlModelSpecies model);

	/**
	 * @param scope
	 * @param args
	 * @return
	 */
	IObject createInstance(IScope scope, IMap<String, Object> args);

	/**
	 * @param scope
	 * @param s
	 * @param gamlObject
	 * @return
	 */
	Object getVarValue(IScope scope, String s, GamlObject gamlObject);

	/**
	 * @param scope
	 * @param s
	 * @param v
	 * @param gamlObject
	 */
	void setVarValue(IScope scope, String s, Object v, GamlObject gamlObject);

}