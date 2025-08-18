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
import gama.annotations.precompiler.ITypeProvider;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;
import gama.core.util.GamaListFactory;
import gama.core.util.IAddressableContainer;
import gama.core.util.IList;
import gama.gaml.compilation.ISymbol;
import gama.gaml.descriptions.TypeDescription;
import gama.gaml.operators.Containers;
import gama.gaml.statements.ActionStatement;
import gama.gaml.statements.IStatement;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import gama.gaml.variables.IVariable;
import one.util.streamex.StreamEx;

/**
 * Written by drogoul Modified on 25 avr. 2010
 *
 * @todo Description
 *
 */

/**
 * The Interface IClass.
 */

/**
 * The Interface IClass.
 */
@vars ({ @variable (
		name = IClass.ACTIONS,
		type = IType.LIST,
		of = IType.STRING,
		doc = @doc ("A list of the names of the actions defined in this species")),

		@variable (
				name = IKeyword.ATTRIBUTES,
				type = IType.LIST,
				of = IType.STRING,
				doc = @doc ("A list of the names of the attributes of this species")),
		@variable (
				name = IKeyword.PARENT,
				type = IType.SPECIES,
				doc = @doc ("The parent (if any) of this species")),
		@variable (
				name = IKeyword.NAME,
				type = IType.STRING,
				doc = @doc ("The name of the species")),
		@variable (
				name = IClass.SUBSPECIES,
				type = IType.LIST,
				of = IType.SPECIES,
				doc = @doc ("A list of the names of subspecies of this species")),
		@variable (
				name = IClass.POPULATION,
				type = IType.LIST,
				of = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
				doc = @doc ("The population that corresponds to this species in an instance of its host")) })
public interface IClass extends ISymbol, IAddressableContainer<Integer, IAgent, Integer, IAgent> {

	/** The population. */
	String POPULATION = "population";

	/** The subspecies. */
	String SUBSPECIES = "subspecies";

	/** The actions. */
	String ACTIONS = "actions";

	/**
	 * Extends species.
	 *
	 * @param s
	 *            the s
	 * @return true, if successful
	 */
	boolean extendsSpecies(final IClass s);

	/**
	 * Return all the direct subspecies of this species, properly typed for GAMA
	 *
	 * @return
	 */

	IList<? extends IClass> getSubSpecies(IScope scope);

	/**
	 * Gets the sub species names.
	 *
	 * @param scope
	 *            the scope
	 * @return the sub species names
	 */
	@SuppressWarnings ("unchecked")
	@getter (SUBSPECIES)
	@doc ("Returns all the direct subspecies names of this species")
	default IList<String> getSubSpeciesNames(final IScope scope) {
		return StreamEx.of(getSubSpecies(scope)).map(IClass::getName).toCollection(Containers.listOf(Types.STRING));
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@Override
	@getter (IKeyword.NAME)
	String getName();

	/**
	 * Returns the parent species.
	 *
	 * @return
	 */
	@getter (IKeyword.PARENT)
	@doc ("Returns the direct parent of the species. Experiments, models and species with no explicit parents will return nil")
	IClass getParentSpecies();

	/**
	 * Gets the self with parents.
	 *
	 * @return the self with parents
	 */
	List<? extends IClass> getSelfWithParents();

	/**
	 * Gets the action.
	 *
	 * @param name
	 *            the name
	 * @return the action
	 */
	IStatement.WithArgs getAction(final String name);

	/**
	 * Gets the action names.
	 *
	 * @param scope
	 *            the scope
	 * @return the action names
	 */
	@getter (ACTIONS)
	@doc ("retuns the list of actions defined in this species (incl. the ones inherited from its parent)")
	default IList<String> getActionNames(final IScope scope) {
		return GamaListFactory.create(scope, Types.STRING,
				StreamEx.of(getActions()).map(ActionStatement::getName).toList());
	}

	/**
	 * Gets the actions.
	 *
	 * @return the actions
	 */
	Collection<ActionStatement> getActions();

	/**
	 * Gets the parent name.
	 *
	 * @return the parent name
	 */
	String getParentName();

	/**
	 * Gets the var.
	 *
	 * @param n
	 *            the n
	 * @return the var
	 */
	IVariable getVar(final String n);

	/**
	 * Gets the var names.
	 *
	 * @return the var names
	 */
	Collection<String> getVarNames();

	/**
	 * Similar to getVarNames(), but returns a correctly initialized IList of attribute names
	 *
	 * @param scope
	 * @return the list of all the attributes defined in this species
	 */
	@getter (IKeyword.ATTRIBUTES)
	@doc ("retuns the list of attributes defined in this species (incl. the ones inherited from its parent)")
	default IList<String> getAttributeNames(final IScope scope) {
		return GamaListFactory.create(scope, Types.STRING, getVarNames());
	}

	/**
	 * Gets the vars.
	 *
	 * @return the vars
	 */
	Collection<IVariable> getVars();

	/**
	 * Checks for var.
	 *
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	boolean hasVar(final String name);

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	@Override
	TypeDescription getDescription();

}