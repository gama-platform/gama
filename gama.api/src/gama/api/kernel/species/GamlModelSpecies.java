/*******************************************************************************************************
 *
 * GamlModelSpecies.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.species;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import gama.annotations.doc;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.symbol;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.IType;
import gama.api.kernel.GamaMetaModel;
import gama.api.kernel.object.IClass;
import gama.api.types.map.GamaMapFactory;
import gama.api.ui.IOutputManager;

/**
 * Represents the top-level model species in a GAMA simulation.
 *
 * <p>
 * The model species is a special species that serves as the root container for all other species and experiments in a
 * GAML model. It represents the "world" or global context of the simulation. There is typically one instance of the
 * model species per simulation, accessible via the {@code world} keyword in GAML.
 * </p>
 *
 * <h2>Key Characteristics</h2>
 * <ul>
 * <li><b>Root Container:</b> Contains all other species as micro-species</li>
 * <li><b>Experiment Host:</b> Manages all experiment definitions associated with the model</li>
 * <li><b>File Context:</b> Provides access to model file paths and project structure</li>
 * <li><b>Species Registry:</b> Maintains a registry of all species defined in the model</li>
 * </ul>
 *
 * <h2>Model vs. Regular Species</h2>
 * <p>
 * Unlike regular species, the model species:
 * </p>
 * <ul>
 * <li>Cannot have a parent species (it's the root)</li>
 * <li>Cannot be a micro-species of another species</li>
 * <li>Has exactly one instance per simulation (the world agent)</li>
 * <li>Manages experiments that can create multiple simulations</li>
 * <li>Provides global scheduling and topology</li>
 * </ul>
 *
 * <h2>Example GAML Model</h2>
 *
 * <pre>
 * {@code
 * model prey_predator
 *
 * global {
 *     int nb_preys <- 100;
 *     int nb_predators <- 20;
 *
 *     init {
 *         create prey number: nb_preys;
 *         create predator number: nb_predators;
 *     }
 * }
 *
 * species prey { ... }
 * species predator { ... }
 *
 * experiment main type: gui {
 *     // experiment definition
 * }
 * }
 * </pre>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since GAMA 1.0
 * @see GamlSpecies
 * @see IModelSpecies
 * @see IExperimentSpecies
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
public class GamlModelSpecies extends GamlSpecies implements IModelSpecies {

	/** Map of experiments keyed by their internal name. */
	protected final Map<String, IExperimentSpecies> experiments = new HashMap<>();

	/** The classes. */
	protected final Map<String, IClass> classes = new HashMap<>();

	/** Map of experiments keyed by their title (display name). */
	protected final Map<String, IExperimentSpecies> titledExperiments = new HashMap<>();

	/** Cached map of all species in the model (includes all micro-species recursively). */
	protected Map<String, ISpecies> allSpecies;

	/**
	 * Constructs a new model species from its description.
	 *
	 * <p>
	 * The model species represents the global/world agent of a GAMA simulation and serves as the root container for all
	 * other species and experiments.
	 * </p>
	 *
	 * @param description
	 *            the model description
	 */
	public GamlModelSpecies(final IDescription description) {
		super(description);
		setName(description.getName());
	}

	@Override
	public IModelDescription getDescription() { return (IModelDescription) description; }

	@Override
	public Collection<String> getImportedPaths() { return getDescription().getAlternatePaths(); }

	@Override
	public String getWorkingPath() { return getDescription().getModelFolderPath(); }

	@Override
	public String getFilePath() { return getDescription().getModelFilePath(); }

	@Override
	public String getProjectPath() { return getDescription().getModelProjectPath(); }

	/**
	 * Adds an experiment species to this model.
	 *
	 * <p>
	 * Experiments are stored in two maps: one by internal name and one by title. This allows retrieval by either
	 * identifier. The experiment is also registered as belonging to this model.
	 * </p>
	 *
	 * @param exp
	 *            the experiment species to add
	 */
	protected void addExperiment(final IExperimentSpecies exp) {
		if (exp == null) return;
		experiments.put(exp.getName(), exp);
		titledExperiments.put(exp.getFacet(IKeyword.TITLE).literalValue(), exp);
		exp.setModel(this);
	}

	/**
	 * Gets an experiment by name or title.
	 *
	 * <p>
	 * This method provides flexible experiment retrieval by trying multiple strategies:
	 * </p>
	 * <ol>
	 * <li>Look up by internal name</li>
	 * <li>If not found, look up by title</li>
	 * <li>If not found and the string is numeric, get the n-th experiment by index</li>
	 * </ol>
	 *
	 * @param s
	 *            the experiment name, title, or numeric index
	 * @return the experiment species, or null if not found
	 */
	@Override
	public IExperimentSpecies getExperiment(final String s) {
		// First we try to get it using its "internal" name
		IExperimentSpecies e = experiments.get(s);
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
	public Iterable<IExperimentSpecies> getExperiments() { return experiments.values(); }

	@Override
	public void dispose() {
		if (isBuiltIn()) return;
		super.dispose();
		for (final IExperimentSpecies exp : experiments.values()) { exp.dispose(); }
		experiments.clear();
		titledExperiments.clear();
		if (allSpecies != null) { allSpecies.clear(); }
	}

	@Override
	public ISpecies getSpecies(final String speciesName) {
		if (speciesName == null) return null;
		if (speciesName.equals(getName())) return this;
		ISpecies sp = GamaMetaModel.getSpecies(speciesName);
		/*
		 * the original is: return getAllSpecies().get(speciesName);
		 */

		// hqnghi 11/Oct/13
		// get experiementSpecies in any model
		if (sp == null) { sp = getAllSpecies().get(speciesName); }
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

	/**
	 * Gets all species defined in this model.
	 *
	 * <p>
	 * This method builds a comprehensive map of all species by recursively traversing the species hierarchy. The
	 * traversal uses a depth-first approach starting from the model itself and descending through all micro-species.
	 * The result is cached for performance.
	 * </p>
	 *
	 * <p>
	 * The map includes:
	 * </p>
	 * <ul>
	 * <li>The model species itself (global/world)</li>
	 * <li>All direct micro-species</li>
	 * <li>All nested micro-species recursively</li>
	 * </ul>
	 *
	 * @return a map of all species keyed by name
	 */
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
					if (currentSpecies.equals(microSpec.getMacroSpecies())) { speciesStack.push(microSpec); }
				}
			}
		}
		return allSpecies;
	}

	/**
	 * Organizes children symbols, separating experiments and outputs from regular species content.
	 *
	 * <p>
	 * This override handles the special case of model species which can contain experiments and default output
	 * managers. The method:
	 * </p>
	 * <ol>
	 * <li>Separates experiments and output managers from other children</li>
	 * <li>Passes regular children (variables, actions, etc.) to the parent implementation</li>
	 * <li>Registers experiments with the model</li>
	 * <li>Distributes default output managers to all experiments</li>
	 * </ol>
	 *
	 * @param children
	 *            the child symbols to organize
	 */
	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {
		final List forExperiment = new ArrayList<>();
		final List<IExperimentSpecies> theExperiments = new ArrayList<>();

		for (final Iterator<? extends ISymbol> it = children.iterator(); it.hasNext();) {
			final ISymbol s = it.next();

			if (s instanceof IExperimentSpecies) {
				theExperiments.add((IExperimentSpecies) s);
				it.remove();
			} else if (s instanceof IOutputManager) {
				forExperiment.add(s);
				it.remove();
			}
		}
		// Add the variables, etc. to the model
		super.setChildren(children);
		// Add the experiments and the default outputs to all experiments
		for (final IExperimentSpecies exp : theExperiments) {
			addExperiment(exp);
			exp.setChildren(forExperiment);
		}
	}

	/** The is test. */
	static Predicate<IStatement> isTest = s -> (s instanceof IStatement.Test);

	@Override
	public List<IStatement.Test> getAllTests() {

		final List<IStatement.Test> tests = new ArrayList<>();
		final Consumer<IStatement> filter =
				t -> { if (t instanceof IStatement.Test) { tests.add((IStatement.Test) t); } };
		// Fix Issue #2659
		// getBehaviors().forEach(filter);
		getAllSpecies().values().forEach(s -> s.getBehaviors().forEach(filter));
		return tests;
	}

	/**
	 * Gets the class.
	 *
	 * @param className
	 *            the class name
	 * @return the class
	 */
	@Override
	public IClass getClass(final String className) {
		if (className == null) return null;
		if (IKeyword.OBJECT.equals(className)) return GamaMetaModel.getAbstractObjectClass();
		return classes.get(className);
	}

	/**
	 * Gets the class.
	 *
	 * @param speciesName
	 *            the species name
	 * @param origin
	 *            the origin
	 * @return the class
	 */
	@Override
	public IClass getClass(final String speciesName, final String origin) {
		if (speciesName == null) return null;
		for (final Map.Entry<String, ISpecies> entry : getAllSpecies().entrySet()) {
			final ISpecies mm = entry.getValue();
			if (mm instanceof GamlModelSpecies gms && origin.equals(mm.getName())) return gms.getClass(speciesName);
		}
		return getClass(speciesName);
	}

}
