/*******************************************************************************************************
 *
 * GamlClass.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.species;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.model.GamlModelSpecies;
import gama.core.metamodel.agent.GamlObject;
import gama.core.metamodel.agent.IObject;
import gama.core.runtime.IScope;
import gama.core.util.GamaMapFactory;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.gaml.compilation.GAML;
import gama.gaml.compilation.IDescriptionValidator;
import gama.gaml.compilation.ISymbol;
import gama.gaml.compilation.Symbol;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.ClassDescription;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.TypeDescription;
import gama.gaml.species.GamlClass.ClassValidator;
import gama.gaml.statements.ActionStatement;
import gama.gaml.statements.IStatement.WithArgs;
import gama.gaml.types.IContainerType;
import gama.gaml.types.IType;
import gama.gaml.variables.IVariable;

/**
 * The {@code GamlClass} serves as a base class for species-related classes in the GAMA platform. It encapsulates shared
 * variables, actions, and a reference to a parent class.
 */
@symbol (
		name = { IKeyword.CLASS },
		kind = ISymbolKind.SPECIES,
		with_sequence = true,
		concept = { IConcept.SPECIES })
@inside (
		kinds = { ISymbolKind.MODEL })
@facets (
		value = {

				@facet (
						name = IKeyword.NAME,
						type = IType.ID,
						optional = false,
						doc = @doc ("the identifier of the class, which must be unique in the model. It is used to refer to the class in the model, and to create instances of it.")),
				@facet (
						name = IKeyword.PARENT,
						type = IType.CLASS,
						optional = true,
						doc = @doc ("the parent class (inheritance)")),
				@facet (
						name = IKeyword.VIRTUAL,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("whether the class is virtual (cannot be instantiated, but only used as a parent) (false by default)")) },
		omissible = IKeyword.NAME)
@doc (
		value = "The class statement allows modelers to define new classes in the model. A class is a template for creating objects, and can be used to define the attributes and actions of objects in the model. Classes can inherit from other classes")
@validator (ClassValidator.class)
public class GamlClass extends Symbol implements IClass {

	/** A map containing the variables defined in this class. */
	protected final Map<String, IVariable> variables = GamaMapFactory.createOrdered();

	/** A map containing the actions defined in this class. */
	protected final Map<String, ActionStatement> actions = GamaMapFactory.createOrdered();

	/** The parent class of this class, if any. */
	protected IClass parentClass;

	/** The parent species. */
	protected ISpecies macroSpecies;

	/**
	 * The Class ClassValidator.
	 */
	public static class ClassValidator implements IDescriptionValidator<IDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.gaml.compilation.IDescriptionValidator#validate(gama.gaml.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription desc) {
			final ClassDescription sd = (ClassDescription) desc;
			/** The name. */
			final String name = sd.getName();
			if (GAML.isUnaryOperator(name)) {
				sd.error("The name '" + name + "' cannot be used for naming this " + sd.getKeyword()
						+ ", as the derived casting operator (" + name
						+ "(...)) would conflict with an existing unary operator");
			}

		}

	}

	/**
	 * @param desc
	 */
	public GamlClass(final IDescription desc) {
		super(desc);
		setName(description.getName());
	}

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
	 * @param parentClass
	 *            the parent class to set
	 */
	public void setParentClass(final GamlClass parentClass) { this.parentClass = parentClass; }

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
	@Override
	public Collection<ActionStatement> getActions() { return actions.values(); }

	/**
	 * Gets the var.
	 *
	 * @param n
	 *            the n
	 * @return the var
	 */
	@Override
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
	@Override
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
	@Override
	public WithArgs getAction(final String name) {
		return actions.get(name);
	}

	/**
	 * Adds a variable to this class.
	 *
	 * @param name
	 *            the name of the variable
	 * @param variable
	 *            the variable to add
	 */
	public void addVariable(final String name, final IVariable variable) {
		variables.put(name, variable);
	}

	/**
	 * Adds an action to this class.
	 *
	 * @param name
	 *            the name of the action
	 * @param action
	 *            the action to add
	 */
	public void addAction(final String name, final ActionStatement action) {
		actions.put(name, action);
	}

	/**
	 * Serialize to json. This method serializes the class to a JSON object
	 *
	 * @param json
	 *            the json
	 * @return the json value
	 */
	@Override
	public JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(), IKeyword.NAME, getName());
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
		macroSpecies = null;
		parentClass = null;
	}

	@Override
	public IContainerType<?> getGamlType() {
		// TODO: Difficult to understand if this is correct, but it seems to be the way to get the type of a class

		return (IContainerType<?>) getDescription().getTypeNamed(getName());
	}

	@Override
	public Collection<String> getVarNames() { return getDescription().getAttributeNames(); }

	@Override
	public Collection<IVariable> getVars() { return variables.values(); }

	@Override
	public String getParentName() { return getDescription().getParentName(); }

	/**
	 * Checks if this species extends another species.
	 *
	 * @param s
	 *            the species to check against
	 * @return {@code true} if this species extends the given species, {@code false} otherwise
	 */
	@Override
	public boolean extendsSpecies(final IClass s) {
		final IClass parent = getParentSpecies();
		if (parent == null) return false;
		if (parent == s) return true;
		return parent.extendsSpecies(s);
	}

	@Override
	public TypeDescription getDescription() { return (TypeDescription) description; }

	@Override
	public IClass getParentSpecies() {
		if (parentClass == null) {
			final String name = getDescription().getParentName();
			GamlModelSpecies currentMacroSpec = (GamlModelSpecies) this.getMacroSpecies(); // model species
			parentClass = currentMacroSpec.getClass(name);
		}
		return parentClass;
	}

	@Override
	public IList<? extends IClass> getSubSpecies(final IScope scope) {
		return null;
	}

	@Override
	public void setMacroSpecies(final GamlModelSpecies model) { macroSpecies = model; }

	/**
	 * Gets the parent species.
	 *
	 * @return the parent species
	 */
	public ISpecies getMacroSpecies() { return macroSpecies; }

	@Override
	public List<? extends IClass> getSelfWithParents() {
		final List<IClass> retVal = new ArrayList<>();
		retVal.add(this);
		IClass currentParent = this.getParentSpecies();
		while (currentParent != null) {
			retVal.add(currentParent);
			currentParent = currentParent.getParentSpecies();
		}

		return retVal;
	}

	@Override
	public IObject createInstance(final IScope scope, final IMap<String, Object> args) {
		return new GamlObject(scope, this, args);
	}

	@Override
	public Object getVarValue(final IScope scope, final String s, final GamlObject gamlObject) {
		IVariable var = getVar(s);
		if (var != null) return var.value(scope, gamlObject);
		return null;
	}

	@Override
	public void setVarValue(final IScope scope, final String s, final Object v, final GamlObject gamlObject) {
		IVariable var = getVar(s);
		if (var != null) { var.setVal(scope, gamlObject, v); }
	}
}