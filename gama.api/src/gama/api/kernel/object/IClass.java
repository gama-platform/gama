/*******************************************************************************************************
 *
 * IClass.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.object;

import java.util.Collection;
import java.util.List;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.constants.IKeyword;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.symbols.IVariable;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITyped;
import gama.api.gaml.types.Types;
import gama.api.kernel.species.IModelSpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.utils.json.IJsonable;
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
				type = IType.CLASS,
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
public interface IClass extends ISymbol, ITyped, IJsonable {

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
		return StreamEx.of(getSubSpecies(scope)).map(IClass::getName)
				.toCollection(GamaListFactory.getSupplier(Types.STRING));
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
	@doc ("returns the list of actions defined in this species (incl. the ones inherited from its parent)")
	default IList<String> getActionNames(final IScope scope) {
		return GamaListFactory.create(scope, Types.STRING,
				StreamEx.of(getActions()).map(IStatement.Action::getName).toList());
	}

	/**
	 * Returns all actions defined in this species.
	 *
	 * @return a collection of actions
	 */
	Collection<IStatement.Action> getActions();

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
	ITypeDescription getDescription();

	/**
	 * @param gamlModelSpecies
	 */
	void setMacroSpecies(IModelSpecies model);

	/**
	 * @param scope
	 * @param args
	 * @return
	 */
	IObject createInstance(IScope scope, IMap<String, Object> args);

}