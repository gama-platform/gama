/*******************************************************************************************************
 *
 * AbstractSpecies.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.species;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import gama.api.GAMA;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.factories.IExpressionFactory;
import gama.api.constants.IKeyword;
import gama.api.data.factories.GamaListFactory;
import gama.api.data.factories.GamaMapFactory;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IMatrix;
import gama.api.data.objects.IPoint;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.statements.IStatement.WithArgs;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.symbols.IVariable;
import gama.api.gaml.symbols.Symbol;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IGraphAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.skill.IArchitecture;
import gama.api.kernel.skill.ISkill;
import gama.api.kernel.species.IModelSpecies;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IExperimentDisplayable;
import gama.dev.DEBUG;
import one.util.streamex.StreamEx;

/**
 * Written by drogoul Modified on 29 d�c. 2010
 *
 * @todo Description
 *
 */

/**
 * The Class AbstractSpecies.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 3 oct. 2023
 */

/**
 * The Class AbstractSpecies.
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public abstract class AbstractSpecies extends Symbol implements ISpecies {

	static {
		DEBUG.OFF();
	}

	/** The is graph. */
	protected final boolean isGrid, isGraph;

	/** The micro species. */
	protected final Map<String, ISpecies> microSpecies = GamaMapFactory.createOrdered();

	/** The variables. */
	private final Map<String, IVariable> variables = GamaMapFactory.createOrdered();

	/** The aspects. */
	private final Map<String, IStatement.Aspect> aspects = GamaMapFactory.createOrdered();

	/** The actions. */
	private final Map<String, IStatement.Action> actions = GamaMapFactory.createOrdered();

	/** The user commands. */
	private final Map<String, IStatement.UserCommand> userCommands = GamaMapFactory.createOrdered();

	/** The behaviors. */
	private final List<IStatement> behaviors = new ArrayList<>();

	/** The parent species. */
	protected ISpecies macroSpecies, parentSpecies;

	/** The control. */
	final IArchitecture control;

	/**
	 * Instantiates a new abstract species.
	 *
	 * @param description
	 *            the description
	 */
	public AbstractSpecies(final IDescription description) {
		super(description);
		setName(description.getName());
		isGrid = IKeyword.GRID.equals(getKeyword());
		isGraph = IGraphAgent.class.isAssignableFrom(((ISpeciesDescription) description).getJavaBase());
		control = (IArchitecture) getDescription().getControl().getInstance();
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return getName();
	}

	@Override
	public Collection<IStatement> getBehaviors() { return behaviors; }

	@Override
	public java.lang.Iterable<? extends IAgent> iterable(final IScope scope) {
		return getPopulation(scope).iterable(scope);
	}

	/**
	 * Adds the temporary action.
	 *
	 * @param action
	 *            the action
	 */
	@Override
	public void addTemporaryAction(final IStatement action) {
		if (action instanceof IStatement.Action as) { actions.put(action.getName(), as); }
	}

	@Override
	public void removeTemporaryAction() {
		actions.remove(IExpressionFactory.TEMPORARY_ACTION_NAME);
		getDescription().removeAction(IExpressionFactory.TEMPORARY_ACTION_NAME);
	}

	@Override
	public IPopulation<IAgent> getPopulation(final IScope scope) {
		final IAgent a = scope.getAgent();
		IPopulation result = null;
		if (a != null) {
			// AD 19/09/13 Patch to allow experiments to gain access to the
			// simulation populations
			result = a.getPopulationFor(this);
		}
		return result;
	}

	@Override
	public IList<IAgent> listValue(final IScope scope, final IType contentsType, final boolean copy)
			throws GamaRuntimeException {
		// return getPopulation(scope).listValue(scope, contentsType);
		// hqnghi 16/04/14
		IPopulation pop = getPopulation(scope);
		if (pop == null) { pop = scope.getSimulation().getPopulationFor(contentsType.getName()); }
		// AD 20/01/16 : Explicitly passes true in order to obtain a copy of the
		// population
		return pop.listValue(scope, contentsType, true);
		// end-hqnghi
	}

	@Override
	public String stringValue(final IScope scope) {
		return name;
	}

	@Override
	public IMap<?, ?> mapValue(final IScope scope, final IType keyType, final IType contentsType, final boolean copy)
			throws GamaRuntimeException {
		final IList<IAgent> agents = listValue(scope, contentsType, false);
		// Default behavior : Returns a map containing the names of agents as
		// keys and the agents themselves as values
		final IMap result = GamaMapFactory.create(Types.STRING, scope.getType(getName()));
		for (final IAgent agent : agents.iterable(scope)) { result.put(agent.getName(), agent); }
		return result;
	}

	@Override
	public boolean isGrid() { return isGrid; }

	@Override
	public boolean isGraph() { return isGraph; }

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public ISpecies copy(final IScope scope) {
		return this;
		// Species are immutable
	}

	@Override
	public IList<ISpecies> getMicroSpecies() {
		final IList<ISpecies> retVal = GamaListFactory.create(Types.SPECIES);
		retVal.addAll(microSpecies.values());
		final ISpecies parentSpecies = this.getParentSpecies();
		if (parentSpecies != null) { retVal.addAll(parentSpecies.getMicroSpecies()); }
		return retVal;
	}

	@Override
	public IList<ISpecies> getSubSpecies(final IScope scope) {
		final IList<ISpecies> subspecies = GamaListFactory.create(Types.SPECIES);
		final IModelSpecies model = scope.getModel();
		for (final ISpecies s : model.getAllSpecies().values()) {
			if (s.getParentSpecies() == this) { subspecies.add(s); }
		}
		return subspecies;
	}

	@Override
	public Collection<String> getMicroSpeciesNames() { return microSpecies.keySet(); }

	/**
	 * Returns a micro-species with the specified name or null otherwise.
	 *
	 * @param microSpeciesName
	 * @return a species or null
	 */
	@Override
	public ISpecies getMicroSpecies(final String microSpeciesName) {
		final ISpecies retVal = microSpecies.get(microSpeciesName);
		if (retVal != null) return retVal;
		final ISpecies parentSpecies = this.getParentSpecies();
		if (parentSpecies != null) return parentSpecies.getMicroSpecies(microSpeciesName);
		return null;
	}

	@Override
	public boolean containMicroSpecies(final ISpecies species) {
		final ISpecies parentSpecies = this.getParentSpecies();
		return microSpecies.containsValue(species)
				|| (parentSpecies != null ? parentSpecies.containMicroSpecies(species) : false);
	}

	@Override
	public boolean hasMicroSpecies() {
		final ISpecies parentSpecies = this.getParentSpecies();
		return !microSpecies.isEmpty() || (parentSpecies != null ? parentSpecies.hasMicroSpecies() : false);
	}

	@Override
	public ISpeciesDescription getDescription() { return (ISpeciesDescription) description; }

	@Override
	public boolean isPeer(final ISpecies other) {
		return other != null && other.getMacroSpecies().equals(this.getMacroSpecies());
	}

	@Override
	public List<ISpecies> getSelfWithParents() {
		final List<ISpecies> retVal = new ArrayList<>();
		retVal.add(this);
		ISpecies currentParent = this.getParentSpecies();
		while (currentParent != null) {
			retVal.add(currentParent);
			currentParent = currentParent.getParentSpecies();
		}

		return retVal;
	}

	@Override
	public ISpecies getParentSpecies() {
		if (parentSpecies == null) {
			final ITypeDescription parentSpecDesc = getDescription().getParent();
			// Takes care of invalid species (see Issue 711)
			if (parentSpecDesc == null || parentSpecDesc == getDescription()) return null;
			ISpecies currentMacroSpec = this.getMacroSpecies();
			while (currentMacroSpec != null && parentSpecies == null) {
				parentSpecies = currentMacroSpec.getMicroSpecies(parentSpecDesc.getName());
				currentMacroSpec = currentMacroSpec.getMacroSpecies();
			}
		}
		return parentSpecies;
	}

	@Override
	public boolean extendsSpecies(final ISpecies s) {
		final ISpecies parent = getParentSpecies();
		if (parent == null) return false;
		if (parent == s) return true;
		return parent.extendsSpecies(s);
	}

	@Override
	public String getParentName() { return getDescription().getParentName(); }

	@Override
	public IArchitecture getArchitecture() { return control; }

	@Override
	public IVariable getVar(final String n) {
		return variables.get(n);
	}

	@Override
	public boolean hasVar(final String name) {
		return variables.containsKey(name);
	}

	@Override
	public Collection<String> getVarNames() { return getDescription().getAttributeNames(); }

	@Override
	public Collection<IVariable> getVars() { return variables.values(); }

	@Override
	public Collection<? extends IExperimentDisplayable> getUserCommands() { return userCommands.values(); }

	@Override
	public WithArgs getAction(final String name) {
		return actions.get(name);
	}

	@Override
	public Collection<? extends IStatement> getActions() { return actions.values(); }

	@Override
	public boolean hasAspect(final String n) {
		return aspects.containsKey(n);
	}

	@Override
	public IExecutable getAspect(final String n) {
		return aspects.get(n);
	}

	@Override
	public Collection<? extends IExecutable> getAspects() { return aspects.values(); }

	@Override
	public IList<String> getAspectNames() { return GamaListFactory.wrap(Types.STRING, aspects.keySet()); }

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {
		// First we verify the control architecture
		if (control == null)
			throw GamaRuntimeException.error("The control of species " + description.getName() + " cannot be computed",
					GAMA.getRuntimeScope());
		// Then we classify the children in their categories
		for (final ISymbol s : children) {
			if (s instanceof ISpecies oneMicroSpecies) {
				oneMicroSpecies.setMacroSpecies(this);
				microSpecies.put(oneMicroSpecies.getName(), oneMicroSpecies);
			} else if (s instanceof IVariable) {
				DEBUG.OUT("Adding " + s.getName() + " to " + this);
				variables.put(s.getName(), (IVariable) s);
			} else
				switch (s) {
					case IStatement.Aspect as -> aspects.put(s.getName(), as);
					case IStatement.Action ac -> {
						s.setEnclosing(this);
						actions.put(s.getName(), ac);
					}
					case IStatement.UserCommand uc -> userCommands.put(s.getName(), uc);
					case null, default -> {
						if (s instanceof IStatement) {
							behaviors.add((IStatement) s); // reflexes, states or tasks
						}
					}
				}
		}
		control.setChildren(behaviors);
		behaviors.forEach(b -> b.setEnclosing(this));
		variables.forEach((n, v) -> v.setEnclosing(this));
		control.verifyBehaviors(this);
	}

	@Override
	public void dispose() {
		super.dispose();
		for (final IVariable v : variables.values()) { v.dispose(); }
		variables.clear();
		for (final IStatement.Aspect ac : aspects.values()) { ac.dispose(); }
		aspects.clear();
		for (final IStatement.Action ac : actions.values()) { ac.dispose(); }
		actions.clear();
		for (final IStatement c : behaviors) { c.dispose(); }
		behaviors.clear();
		macroSpecies = null;
		parentSpecies = null;
		// TODO dispose micro_species first???
		microSpecies.clear();
	}

	// TODO review this
	// this is the "original" macro-species???
	@Override
	public ISpecies getMacroSpecies() { return macroSpecies; }

	@Override
	public void setMacroSpecies(final ISpecies macroSpecies) { this.macroSpecies = macroSpecies; }

	/*
	 * Equation (Huynh Quang Nghi)
	 */

	@Override
	public <T extends IStatement> T getStatement(final Class<T> clazz, final String valueOfFacetName) {
		for (final IStatement s : behaviors) {
			final boolean instance = clazz.isAssignableFrom(s.getClass());
			if (instance) {
				if (valueOfFacetName == null) return (T) s;
				final String t = s.getDescription().getName();
				if (t != null) {
					final boolean named = t.equals(valueOfFacetName);
					if (named) return (T) s;
				}
			}
		}
		return null;
	}

	/*
	 * end-of Equation
	 */

	@Override
	public Boolean implementsSkill(final String skill) {
		return getDescription().implementsSkill(skill);
	}

	@Override
	public IAgent get(final IScope scope, final Integer index) throws GamaRuntimeException {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? null : pop.get(scope, index);
	}

	@Override
	public boolean contains(final IScope scope, final Object o) throws GamaRuntimeException {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? false : pop.contains(scope, o);
	}

	@Override
	public IAgent firstValue(final IScope scope) throws GamaRuntimeException {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? null : pop.firstValue(scope);
	}

	@Override
	public IAgent lastValue(final IScope scope) throws GamaRuntimeException {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? null : pop.lastValue(scope);
	}

	@Override
	public int length(final IScope scope) {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? 0 : pop.length(scope);
	}

	@Override
	public boolean isEmpty(final IScope scope) {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? true : pop.isEmpty(scope);
	}

	@Override
	public IContainer<Integer, ? extends IAgent> reverse(final IScope scope) throws GamaRuntimeException {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? null : pop.reverse(scope);
	}

	@Override
	public IAgent anyValue(final IScope scope) {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? null : pop.anyValue(scope);
	}

	@Override
	public IMatrix<? extends IAgent> matrixValue(final IScope scope, final IType contentsType, final boolean copy)
			throws GamaRuntimeException {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? null : pop.matrixValue(scope, contentsType, copy);
	}

	@Override
	public IMatrix<? extends IAgent> matrixValue(final IScope scope, final IType contentsType,
			final IPoint preferredSize, final boolean copy) throws GamaRuntimeException {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? null : pop.matrixValue(scope, contentsType, preferredSize, copy);
	}

	@Override
	public IAgent getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? null : pop.getFromIndicesList(scope, indices);
	}

	@Override
	public boolean isMirror() { return getDescription().isMirror(); }

	@Override
	public Collection<? extends IPopulation<? extends IAgent>> getPopulations(final IScope scope) {
		final IPopulation<? extends IAgent> pop = getPopulation(scope);
		return pop == null ? Collections.EMPTY_LIST : Collections.singleton(pop);
	}

	/**
	 * Gets the skill instance for.
	 *
	 * @param skillClass
	 *            the skill class
	 * @return the skill instance for
	 */
	@Override
	public ISkill getSkillInstanceFor(final Class skillClass) {
		if (skillClass == null) return null;
		if (skillClass.isAssignableFrom(control.getClass())) return control;
		return getSkillInstanceFor(getDescription(), skillClass);
	}

	/**
	 * Gets the skill instance for.
	 *
	 * @param sd
	 *            the sd
	 * @param skillClass
	 *            the skill class
	 * @return the skill instance for
	 */
	private ISkill getSkillInstanceFor(final ISpeciesDescription sd, final Class skillClass) {
		for (final ISkillDescription sk : sd.getSkills()) {
			if (skillClass.isAssignableFrom(sk.getJavaBase())) return sk.getInstance();
		}
		if (sd.getParent() != null && sd.getParent() != sd) return getSkillInstanceFor(sd.getParent(), skillClass);
		return null;
	}

	@Override
	public IList<String> getSubSpeciesNames(final IScope scope) {
		return StreamEx.of(getSubSpecies(scope)).map(ISpecies::getName)
				.toCollection(GamaListFactory.getSupplier(Types.STRING));
	}

	@Override
	public IList<String> getAttributeNames(final IScope scope) {
		return GamaListFactory.create(scope, Types.STRING, getVarNames());
	}

	@Override
	public IList<String> getActionNames(final IScope scope) {
		return GamaListFactory.create(scope, Types.STRING, StreamEx.of(getActions()).map(IStatement::getName).toList());
	}

}