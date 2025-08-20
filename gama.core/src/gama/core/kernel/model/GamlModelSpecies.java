/*******************************************************************************************************
 *
 * GamlModelSpecies.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.experiment.IExperimentPlan;
import gama.core.outputs.AbstractOutputManager;
import gama.core.util.GamaMapFactory;
import gama.gaml.compilation.ISymbol;
import gama.gaml.compilation.kernel.GamaMetaModel;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.ModelDescription;
import gama.gaml.species.GamlSpecies;
import gama.gaml.species.IClass;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.IStatement;
import gama.gaml.statements.test.TestStatement;
import gama.gaml.types.IType;

/**
 * The Class GamlModelSpecies.
 */
@symbol (
		name = { IKeyword.MODEL },
		kind = ISymbolKind.MODEL,
		with_sequence = true,
		internal = true,
		concept = { IConcept.MODEL })
@facets (
		value = { @facet (
				name = IKeyword.VERSION,
				type = IType.ID,
				optional = true,
				doc = @doc ("The version of this model")),
				@facet (
						name = IKeyword.AUTHOR,
						type = IType.ID,
						optional = true,
						doc = @doc ("The author of the model")),
				@facet (
						name = IKeyword.PRAGMA,
						type = IType.MAP,
						index = IType.STRING,
						of = IType.LIST,
						optional = true,
						internal = true,
						doc = @doc ("For internal use only")),
				@facet (
						name = IKeyword.TORUS,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Whether the model will be based on a toroidal environment or not")),
				@facet (
						name = IKeyword.NAME,
						type = IType.ID,
						optional = false,
						doc = @doc ("The name of the model")),
				@facet (
						name = IKeyword.PARENT,
						type = IType.ID,
						optional = true,
						doc = @doc ("Whether this model inherits from another one or not (must be in the same project and folder)")),
				@facet (
						name = IKeyword.SKILLS,
						type = IType.LIST,
						optional = true,
						doc = @doc ("The list of skills attached to this model")),
				@facet (
						name = IKeyword.CONTROL,
						type = IType.SKILL,
						optional = true,
						doc = @doc ("The control architecture attached to this model")),
				@facet (
						name = IKeyword.FREQUENCY,
						type = IType.INT,
						optional = true,
						doc = @doc ("Specifies how often the model (e.g. every x cycles) will be asked to execute")),
				@facet (
						name = IKeyword.SCHEDULES,
						type = IType.CONTAINER,
						of = IType.AGENT,
						doc = @doc ("A container of agents (a species, a dynamic list, or a combination of species and containers) , which represents which agents will be actually scheduled when the population is scheduled for execution. For instance, 'species a schedules: (10 among a)' will result in a population that schedules only 10 of its own agents every cycle. 'species b schedules: []' will prevent the agents of 'b' to be scheduled. Note that the scope of agents covered here can be larger than the population, which allows to build complex scheduling controls; for instance, defining 'global schedules: [] {...} species b schedules: []; species c schedules: b + world; ' allows to simulate a model where the agents of b are scheduled first, followed by the world, without even having to create an instance of c."),
						optional = true),
				@facet (
						name = IKeyword.TOPOLOGY,
						type = IType.TOPOLOGY,
						optional = true,
						doc = @doc ("The topology of this model. Can be used to specify boundaries (although it is preferred to set the shape attribute).")) },
		omissible = IKeyword.NAME)
@doc ("A model is a species that is used to specify the 'world' of all the agents in the model. The corresponding population is hosted by experiments and accessible by the keyword 'simulations' (or 'simulation' to get the most recently created one)")
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamlModelSpecies extends GamlSpecies implements IModel {

	/** The experiments. */
	protected final Map<String, IExperimentPlan> experiments = GamaMapFactory.create();

	/** The titled experiments. */
	protected final Map<String, IExperimentPlan> titledExperiments = GamaMapFactory.create();

	/** The all species. */
	protected Map<String, ISpecies> allSpecies;

	/** The classes. */
	protected final Map<String, IClass> classes = GamaMapFactory.createOrdered();

	/**
	 * Instantiates a new gaml model species.
	 *
	 * @param description
	 *            the description
	 */
	public GamlModelSpecies(final IDescription description) {
		super(description);
		setName(description.getName());
	}

	@Override
	public ModelDescription getDescription() { return (ModelDescription) description; }

	@Override
	public Collection<String> getImportedPaths() { return getDescription().getAlternatePaths(); }

	@Override
	public String getWorkingPath() { return getDescription().getModelFolderPath(); }

	@Override
	public String getFilePath() { return getDescription().getModelFilePath(); }

	@Override
	public String getProjectPath() { return getDescription().getModelProjectPath(); }

	/**
	 * Adds the experiment.
	 *
	 * @param exp
	 *            the exp
	 */
	protected void addExperiment(final IExperimentPlan exp) {
		if (exp == null) return;
		experiments.put(exp.getName(), exp);
		titledExperiments.put(exp.getFacet(IKeyword.TITLE).literalValue(), exp);
		exp.setModel(this);
	}

	@Override
	public IExperimentPlan getExperiment(final String s) {
		// First we try to get it using its "internal" name
		IExperimentPlan e = experiments.get(s);
		if (e == null) {
			// Otherwise with its title
			e = titledExperiments.get(s);
			// Finally, if the string is an int, we try to get the n-th
			// experiment
			if (e == null && StringUtils.isNumeric(s)) {
				final int i = Integer.parseInt(s);
				final List<String> names = new ArrayList(experiments.keySet());
				if (names.size() > 0) { e = getExperiment(names.get(i)); }
			}
		}
		return e;
	}

	@Override
	public Iterable<IExperimentPlan> getExperiments() { return experiments.values(); }

	@Override
	public void dispose() {
		super.dispose();
		for (final IExperimentPlan exp : experiments.values()) { exp.dispose(); }
		experiments.clear();
		titledExperiments.clear();
		if (allSpecies != null) { allSpecies.clear(); }
	}

	@Override
	public ISpecies getSpecies(final String speciesName) {
		if (speciesName == null) return null;
		if (speciesName.equals(getName())) return this;
		if (IKeyword.MODEL.equals(speciesName)) return GamaMetaModel.INSTANCE.getAbstractModelSpecies();
		if (IKeyword.AGENT.equals(speciesName)) return GamaMetaModel.INSTANCE.getAbstractAgentSpecies();
		/*
		 * the original is: return getAllSpecies().get(speciesName);
		 */

		// hqnghi 11/Oct/13
		// get experiementSpecies in any model
		ISpecies sp = getAllSpecies().get(speciesName);
		if (sp == null) {
			sp = getExperiment(speciesName);
			if (sp == null) {
				for (final Map.Entry<String, ISpecies> entry : getAllSpecies().entrySet()) {
					final ISpecies mm = entry.getValue();
					if (mm instanceof GamlModelSpecies gms) {
						sp = gms.getExperiment(speciesName);
						if (sp != null) return sp;
					}
				}
			}
		}
		return sp;
	}

	/**
	 * Gets the class.
	 *
	 * @param className
	 *            the class name
	 * @return the class
	 */
	public IClass getClass(final String className) {
		if (className == null) return null;
		return classes.get(className);
	}

	@Override
	public ISpecies getSpecies(final String speciesName, final String origin) {
		if (speciesName == null) return null;
		if (speciesName.equals(getName())) return this;
		// hqnghi 11/Oct/13
		// get experiementSpecies in any model
		ISpecies sp = getExperiment(speciesName);
		if (sp == null) {
			for (final Map.Entry<String, ISpecies> entry : getAllSpecies().entrySet()) {
				final ISpecies mm = entry.getValue();
				if (mm instanceof GamlModelSpecies && origin.equals(mm.getName())) {
					sp = ((GamlModelSpecies) mm).getExperiment(speciesName);
					if (sp != null) return sp;
				}
			}
		}
		return getSpecies(speciesName);
	}

	@Override
	public Map<String, ISpecies> getAllSpecies() {
		if (allSpecies == null) {
			allSpecies = GamaMapFactory.create();
			final Deque<ISpecies> speciesStack = new ArrayDeque<>();
			speciesStack.push(this);
			ISpecies currentSpecies;
			while (!speciesStack.isEmpty()) {
				currentSpecies = speciesStack.pop();
				// scope.getGui().debug("GamlModelSpecies: effectively adding "
				// + currentSpecies.getName());
				allSpecies.put(currentSpecies.getName(), currentSpecies);
				final List<ISpecies> theMicroSpecies = currentSpecies.getMicroSpecies();
				for (final ISpecies microSpec : theMicroSpecies) {
					if (microSpec.getMacroSpecies().equals(currentSpecies)) { speciesStack.push(microSpec); }
				}
			}
		}
		return allSpecies;
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {
		final List forExperiment = new ArrayList<>();

		final List<IExperimentPlan> theExperiments = new ArrayList<>();

		for (final Iterator<? extends ISymbol> it = children.iterator(); it.hasNext();) {
			final ISymbol s = it.next();

			if (s instanceof IExperimentPlan) {
				theExperiments.add((IExperimentPlan) s);
				it.remove();
			} else if (s instanceof AbstractOutputManager) {
				forExperiment.add(s);
				it.remove();
			} else if (s instanceof IClass c && !(s instanceof ISpecies)) {
				classes.put(c.getName(), c);
				c.setMacroSpecies(this);
				it.remove();
			}
		}
		// Add the variables, etc. to the model
		super.setChildren(children);
		// Add the experiments and the default outputs to all experiments
		for (final IExperimentPlan exp : theExperiments) {
			addExperiment(exp);
			exp.setChildren(forExperiment);
		}
	}

	/** The is test. */
	static Predicate<IStatement> isTest = s -> (s instanceof TestStatement);

	@Override
	public List<TestStatement> getAllTests() {

		final List<TestStatement> tests = new ArrayList<>();
		final Consumer<IStatement> filter = t -> { if (t instanceof TestStatement) { tests.add((TestStatement) t); } };
		// Fix Issue #2659
		// getBehaviors().forEach(filter);
		getAllSpecies().values().forEach(s -> s.getBehaviors().forEach(filter));
		return tests;
	}

}
