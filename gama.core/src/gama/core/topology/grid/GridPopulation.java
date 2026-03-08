/*******************************************************************************************************
 *
 * GridPopulation.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.topology.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;

import gama.api.GAMA;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.ITypeDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.IVariable;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IContainerType;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IGridAgent;
import gama.api.kernel.agent.IMacroAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.species.ISpecies;
import gama.api.kernel.topology.IGrid;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.IExecutable;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.matrix.IMatrix;
import gama.api.types.misc.IContainer;
import gama.api.types.topology.ITopology;
import gama.api.utils.benchmark.StopWatch;
import gama.api.utils.json.IJson;
import gama.core.agent.GamlAgent;
import gama.core.population.GamaPopulation;
import gama.core.population.PopulationNotifier;
import gama.core.util.json.JsonObject;
import gama.dev.DEBUG;
import one.util.streamex.StreamEx;

/**
 * Class GridPopulation.
 *
 * @author drogoul
 * @since 14 mai 2013
 *
 */
@SuppressWarnings ("unchecked")
public class GridPopulation implements IPopulation.Grid {

	static {
		DEBUG.OFF();
	}

	/**
	 * Notifier
	 */
	private final PopulationNotifier notifier = new PopulationNotifier();

	/** The grid. */
	GamaSpatialMatrix grid;
	/**
	 * The agent hosting this population which is considered as the direct macro-agent.
	 */
	protected IMacroAgent host;
	/**
	 * The object describing how the agents of this population are spatially organized
	 */
	protected ITopology topology;

	/** The species. */
	protected final ISpecies species;

	/** The updatable vars. */
	protected final IVariable[] orderedVars, updatableVars;

	/** The type. */
	protected final IContainerType<?> type;

	/** The hash code. */
	private final int hashCode;

	/** The is step overriden. */
	private final boolean isInitOverriden, isStepOverriden;

	/** The ordered var names. */
	public final LinkedHashSet<String> orderedVarNames = new LinkedHashSet<>();

	/**
	 * Instantiates a new grid population.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param host
	 *            the host
	 * @param species
	 *            the species.
	 * @date 28 août 2023
	 */
	private GridPopulation(final IMacroAgent host, final ISpecies species) {

		this.host = host;
		this.species = species;
		final ITypeDescription ecd = species.getDescription();
		orderedVars = GamaPopulation.orderAttributes(this, ecd, Predicates.alwaysTrue(),
				IVariableDescription.INIT_DEPENDENCIES_FACETS);
		for (IVariable v : orderedVars) { orderedVarNames.add(v.getName()); }
		updatableVars = GamaPopulation.orderAttributes(this, ecd, IVariableDescription::isUpdatable,
				IVariableDescription.UPDATE_DEPENDENCIES_FACETS);
		this.type = Types.LIST.of(ecd.getModelDescription().getTypeNamed(species.getName()));

		/*
		 * PATRICK TAILLANDIER: the problem of having the host here is that depending on the simulation the hashcode
		 * will be different... and this hashcode is very important for the manipultion of GamaMap thus, having two
		 * different hashcodes depending on the simulation makes ensure the repication of simulation So I remove the
		 * host for the moment.
		 */
		/*
		 * AD: Reverting this as different populations in different hosts should not have the same hash code ! See
		 * discussion in https://github.com/gama-platform/gama/issues/3339
		 */
		hashCode = Objects.hash(getSpecies(), getHost());
		final boolean[] result = { false, false };
		species.getDescription().visitChildren(d -> {
			if (d instanceof IActionDescription && !d.isBuiltIn()) {
				final String name = d.getName();
				if (ISpecies.initActionName.equals(name)) {
					result[0] = true;
				} else if (ISpecies.stepActionName.equals(name)) { result[1] = true; }
			}
			return true;
		});
		isInitOverriden = result[0];
		isStepOverriden = result[1];

	}

	/**
	 * Instantiates a new grid population.
	 *
	 * @param t
	 *            the t
	 * @param host
	 *            the host
	 * @param species
	 *            the species
	 * @param gamaSpatialMatrix
	 *            TODO
	 */
	public GridPopulation(final GamaSpatialMatrix gamaSpatialMatrix, final ITopology t, final IMacroAgent host,
			final ISpecies species) {
		this(host, species);
		grid = gamaSpatialMatrix;
		topology = t;
	}

	/**
	 * Stream.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the stream
	 * @date 21 sept. 2023
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public Stream<IGridAgent> stream() {
		Stream s = StreamEx.of(getGrid().matrix);
		return s;
	}

	/**
	 * Stream.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return the stream ex
	 * @date 21 sept. 2023
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public StreamEx<IGridAgent> stream(final IScope scope) {
		StreamEx s = StreamEx.of(getGrid().matrix);
		return s;
	}

	/**
	 * Creates the agents.
	 *
	 * @param scope
	 *            the scope
	 * @param number
	 *            the number
	 * @param initialValues
	 *            the initial values
	 * @param isRestored
	 *            the is restored
	 * @param toBeScheduled
	 *            the to be scheduled
	 * @param sequence
	 *            the sequence
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public IList<IGridAgent> createAgents(final IScope scope, final int number,
			final List<? extends Map<String, Object>> initialValues, final boolean isRestored,
			final boolean toBeScheduled, final IStatement sequence) throws GamaRuntimeException {

		createAgents(scope, null);
		for (final Map attr : initialValues) {
			final IAgent agt = getAgent((Integer) attr.get("grid_x"), (Integer) attr.get("grid_y"));
			if (agt != null) { agt.setAttributes(attr); }
		}
		return (IList) getAgents(scope);
	}

	@Override
	public IList<IGridAgent> createAgents(final IScope scope, final IContainer<?, ? extends IShape> geometries) {
		for (int i = 0; i < getGrid().actualNumberOfCells; i++) {
			final IShape s = getGrid().matrix[i];
			final Class javaBase = species.getDescription().getJavaBase();

			final boolean usesRegularAgents = GamlAgent.class.isAssignableFrom(javaBase);
			if (s != null) {
				final IAgent g = usesRegularAgents ? new GamlGridAgent(this, i) : new MinimalGridAgent(this, i);
				getGrid().matrix[i] = g;
			}
		}

		for (final IVariable var : orderedVars) {

			for (int i = 0; i < getGrid().actualNumberOfCells; i++) {
				final IAgent a = (IAgent) getGrid().matrix[i];
				if (a != null) { var.initializeWith(scope, a, null); }
			}
		}

		for (int i = 0; i < getGrid().actualNumberOfCells; i++) {
			final IAgent a = (IAgent) getGrid().matrix[i];
			if (a != null) { a.schedule(scope); }
		}
		notifier.notifyAgentsAdded(scope, this, (IList) getAgents(scope));
		return null;

	}

	/**
	 * Serialize.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param includingBuiltIn
	 *            the including built in
	 * @return the string
	 * @date 21 sept. 2023
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return getName();
	}

	/**
	 * Step agents.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 * @date 28 août 2023
	 */
	// @Override
	protected boolean stepAgents(final IScope scope) {
		return GamaExecutorService.step(scope, getGrid().matrix, getSpecies());
	}

	/**
	 * Gets the nb cols.
	 *
	 * @return the nb cols
	 */
	public int getNbCols() { return getGrid().numCols; }

	/**
	 * Gets the nb rows.
	 *
	 * @return the nb rows
	 */
	public int getNbRows() { return getGrid().numRows; }

	/**
	 * Gets the agent.
	 *
	 * @param col
	 *            the col
	 * @param row
	 *            the row
	 * @return the agent
	 */
	public IAgent getAgent(final Integer col, final Integer row) {
		if (col >= getNbCols() || col < 0 || row >= getNbRows() || row < 0) return null;
		final IShape s = getGrid().get(null, col, row);
		return s == null ? null : s.getAgent();
	}

	/**
	 * Gets the grid value.
	 *
	 * @param col
	 *            the col
	 * @param row
	 *            the row
	 * @return the grid value
	 */
	public Double getGridValue(final Integer col, final Integer row) {
		if (col >= getNbCols() || col < 0 || row >= getNbRows() || row < 0) return 0.0;
		return getGrid().getGridValue(col, row);
	}

	@Override
	public IGridAgent getAgent(final Integer index) {
		if (index >= size() || index < 0) return null;
		final IShape s = getGrid().matrix[index];
		return (IGridAgent) (s == null ? null : s.getAgent());
	}

	@Override
	public IGridAgent getOrCreateAgent(final IScope scope, final Integer index) {
		return getAgent(index);
	}

	@Override
	public GridTopology getTopology() { return (GridTopology) topology; }

	@Override
	public void initializeFor(final IScope scope) throws GamaRuntimeException {
		topology.initialize(scope, this);
	}

	@Override
	public IGridAgent getAgent(final IScope scope, final IPoint coord) {
		return (IGridAgent) getGrid().getAgentAt(coord);
	}

	@Override
	public void killMembers() throws GamaRuntimeException {
		for (final IShape a : getGrid().matrix) { if (a != null) { a.dispose(); } }
	}

	@Override
	public synchronized IGridAgent[] toArray() {
		return Arrays.copyOf(getGrid().matrix, getGrid().matrix.length, IGridAgent[].class);
	}

	/**
	 * Dispose.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 21 sept. 2023
	 */
	@Override
	public void dispose() {
		isDisposing = true;
		killMembers();
		clear();
		if (topology != null) {
			topology.dispose();
			topology = null;
		}
	}

	/**
	 * Size.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the int
	 * @date 21 sept. 2023
	 */
	@Override
	public int size() {
		return getGrid().actualNumberOfCells;
	}

	/**
	 * Gets the from indices list.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param indices
	 *            the indices
	 * @return the from indices list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @date 21 sept. 2023
	 */
	@Override
	public IGridAgent getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		if (indices == null) return null;
		final int n = indices.length(scope);
		if (n == 0) return null;
		final int x = Cast.asInt(scope, indices.get(scope, 0));
		if (n == 1) return getAgent(Cast.asInt(scope, x));
		final int y = Cast.asInt(scope, indices.get(scope, 1));
		final IShape s = getGrid().get(scope, x, y);
		if (s == null) return null;
		return (IGridAgent) s.getAgent();
	}

	/**
	 * Gets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param index
	 *            the index
	 * @return the i grid agent
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @date 21 sept. 2023
	 */
	@Override
	public IGridAgent get(final IScope scope, final Integer index) throws GamaRuntimeException {
		// WARNING False if the matrix is not dense
		return (IGridAgent) getGrid().matrix[index];
	}

	/**
	 * First value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return the i grid agent
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @date 21 sept. 2023
	 */
	@Override
	public IGridAgent firstValue(final IScope scope) throws GamaRuntimeException {
		return (IGridAgent) getGrid()._first(scope);
	}

	/**
	 * Last value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return the i grid agent
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @date 21 sept. 2023
	 */
	@Override
	public IGridAgent lastValue(final IScope scope) throws GamaRuntimeException {
		return (IGridAgent) getGrid()._last(scope);
	}

	/**
	 * Length.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return the int
	 * @date 21 sept. 2023
	 */
	@Override
	public int length(final IScope scope) {
		return getGrid().actualNumberOfCells;
	}

	/**
	 * Any value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return the i grid agent
	 * @date 21 sept. 2023
	 */
	@Override
	public IGridAgent anyValue(final IScope scope) {
		return (IGridAgent) getGrid().anyValue(scope);
	}

	/**
	 * Iterator.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the iterator
	 * @date 21 sept. 2023
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public Iterator<IGridAgent> iterator() {
		Iterator i = Iterators.forArray(getGrid().getMatrix());
		return i;
	}

	/**
	 * Contains key.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @return true, if successful
	 * @date 21 sept. 2023
	 */
	@Override
	public boolean containsKey(final IScope scope, final Object o) {
		if (o instanceof Integer i) return IPopulation.Grid.super.containsKey(scope, i);
		if (o instanceof IPoint) return getGrid().containsKey(scope, o);
		return false;
	}

	/**
	 * Iterable.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return the java.lang. iterable
	 * @date 21 sept. 2023
	 */
	@Override
	public java.lang.Iterable<IGridAgent> iterable(final IScope scope) {
		return listValue(scope, Types.NO_TYPE, false); // TODO Types.AGENT
		// ??
	}

	/**
	 * Checks if is empty.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is empty
	 * @date 21 sept. 2023
	 */
	@Override
	public boolean isEmpty() { return getGrid()._isEmpty(null); }

	/**
	 * Checks if is empty.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return true, if is empty
	 * @date 21 sept. 2023
	 */
	@Override
	public boolean isEmpty(final IScope scope) {
		return getGrid()._isEmpty(scope);
	}

	/**
	 * List value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param contentsType
	 *            the contents type
	 * @param copy
	 *            the copy
	 * @return the i list
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @date 21 sept. 2023
	 */
	@Override
	public IList<IGridAgent> listValue(final IScope scope, final IType contentsType, final boolean copy)
			throws GamaRuntimeException {
		return getGrid()._listValue(scope, contentsType, false);
	}

	/**
	 * Matrix value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param contentsType
	 *            the contents type
	 * @param copy
	 *            the copy
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @date 21 sept. 2023
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public IMatrix matrixValue(final IScope scope, final IType contentsType, final boolean copy)
			throws GamaRuntimeException {
		if (contentsType == null || contentsType.id() == IType.NONE || contentsType.getSpeciesName() != null
				&& contentsType.getSpeciesName().equals(getSpecies().getName()))
			return getGrid();
		return getGrid().matrixValue(scope, contentsType, copy);
	}

	/**
	 * Matrix value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param type
	 *            the type
	 * @param size
	 *            the size
	 * @param copy
	 *            the copy
	 * @return the i matrix
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @date 21 sept. 2023
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public IMatrix matrixValue(final IScope scope, final IType type, final IPoint size, final boolean copy)
			throws GamaRuntimeException {
		if (type == null || type.id() == IType.NONE
				|| type.getSpeciesName() != null && type.getSpeciesName().equals(getSpecies().getName()))
			return getGrid();
		return getGrid().matrixValue(scope, type, copy);
	}

	/**
	 * Sets the grid.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param matrix
	 *            the new grid
	 * @date 28 août 2023
	 */
	@Override
	public void setGrid(final IGrid matrix) {
		// DEBUG.OUT("Setting a new GamaSpatialMatrix. Are the display data different ? "
		// + !Arrays.equals(grid.getDisplayData(), matrix.getDisplayData()));
		// DEBUG.OUT("Are the grid values equal in both grids ? " + Arrays.equals(grid.gridValue,
		// matrix.getGridValue()));
		// System.arraycopy(matrix.getDisplayData(), 0, grid.supportImagePixels, 0, grid.supportImagePixels.length);
		// just for debug purposes
		grid = (GamaSpatialMatrix) matrix;
		getTopology().setPlaces(matrix);

	}

	/**
	 * Compare to.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param o
	 *            the o
	 * @return the int
	 * @date 21 sept. 2023
	 */
	@Override
	public int compareTo(final IPopulation<IGridAgent> o) {
		return species == o.getSpecies() ? 0 : 1;
	}

	/**
	 * Gets the gaml type.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the gaml type
	 * @date 21 sept. 2023
	 */
	@Override
	public IContainerType<?> getGamlType() { return type; }

	/**
	 * Contains.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param o
	 *            the o
	 * @return true, if successful
	 * @date 21 sept. 2023
	 */
	@Override
	public boolean contains(final Object o) {
		return o instanceof IGridAgent ga && ga.getPopulation() == this;
	}

	/**
	 * To array.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param <T>
	 *            the generic type
	 * @param a
	 *            the a
	 * @return the t[]
	 * @date 21 sept. 2023
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public <T> T[] toArray(final T[] a) {
		return (T[]) getGrid().matrix;
	}

	/**
	 * Adds the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param e
	 *            the e
	 * @return true, if successful
	 * @date 21 sept. 2023
	 */
	@Override
	public boolean add(final IGridAgent e) {
		return false;
	}

	/**
	 * Removes the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param o
	 *            the o
	 * @return true, if successful
	 * @date 21 sept. 2023
	 */
	@Override
	public boolean remove(final Object o) {
		return false;
	}

	/**
	 * Contains all.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param c
	 *            the c
	 * @return true, if successful
	 * @date 21 sept. 2023
	 */
	@Override
	public boolean containsAll(final Collection<?> c) {
		for (Object o : c)
			if (!contains(o)) return false;
		return true;
	}

	/**
	 * Adds the all.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param c
	 *            the c
	 * @return true, if successful
	 * @date 21 sept. 2023
	 */
	@Override
	public boolean addAll(final Collection<? extends IGridAgent> c) {
		return false;
	}

	/**
	 * Adds the all.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param index
	 *            the index
	 * @param c
	 *            the c
	 * @return true, if successful
	 * @date 21 sept. 2023
	 */
	@Override
	public boolean addAll(final int index, final Collection<? extends IGridAgent> c) {
		return false;
	}

	/**
	 * Removes the all.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param c
	 *            the c
	 * @return true, if successful
	 * @date 21 sept. 2023
	 */
	@Override
	public boolean removeAll(final Collection<?> c) {
		return false;
	}

	/**
	 * Retain all.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param c
	 *            the c
	 * @return true, if successful
	 * @date 21 sept. 2023
	 */
	@Override
	public boolean retainAll(final Collection<?> c) {
		return false;
	}

	/**
	 * Clear.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 21 sept. 2023
	 */
	@Override
	public void clear() {}

	/**
	 * Gets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param index
	 *            the index
	 * @return the i grid agent
	 * @date 21 sept. 2023
	 */
	@Override
	public IGridAgent get(final int index) {
		return (IGridAgent) getGrid().matrix[index];
	}

	/**
	 * Sets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param index
	 *            the index
	 * @param element
	 *            the element
	 * @return the i grid agent
	 * @date 21 sept. 2023
	 */
	@Override
	public IGridAgent set(final int index, final IGridAgent element) {
		getGrid().matrix[index] = element;
		return element;
	}

	/**
	 * Adds the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param index
	 *            the index
	 * @param element
	 *            the element
	 * @date 21 sept. 2023
	 */
	@Override
	public void add(final int index, final IGridAgent element) {}

	/**
	 * Removes the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param index
	 *            the index
	 * @return the i grid agent
	 * @date 21 sept. 2023
	 */
	@Override
	public IGridAgent remove(final int index) {
		return null;
	}

	/**
	 * Index of.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param o
	 *            the o
	 * @return the int
	 * @date 21 sept. 2023
	 */
	@Override
	public int indexOf(final Object o) {
		for (int i = 0; i < getGrid().actualNumberOfCells; i++) { if (getGrid().matrix[i] == o) return i; }
		return -1;
	}

	/**
	 * Last index of.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param o
	 *            the o
	 * @return the int
	 * @date 21 sept. 2023
	 */
	@Override
	public int lastIndexOf(final Object o) {
		return indexOf(o);
	}

	/**
	 * List iterator.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the list iterator
	 * @date 21 sept. 2023
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public ListIterator<IGridAgent> listIterator() {
		ListIterator it = Arrays.asList(getGrid().matrix).listIterator(0);
		return it;
	}

	/**
	 * List iterator.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param index
	 *            the index
	 * @return the list iterator
	 * @date 21 sept. 2023
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public ListIterator<IGridAgent> listIterator(final int index) {
		ListIterator it = Arrays.asList(getGrid().matrix).listIterator(index);
		return it;
	}

	/**
	 * Sub list.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param fromIndex
	 *            the from index
	 * @param toIndex
	 *            the to index
	 * @return the list
	 * @date 21 sept. 2023
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public List<IGridAgent> subList(final int fromIndex, final int toIndex) {
		List l = Arrays.asList(getGrid().matrix).subList(fromIndex, toIndex);
		return l;
	}

	/**
	 * Inits the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @date 21 sept. 2023
	 */
	@Override
	public boolean init(final IScope scope) throws GamaRuntimeException {
		return true;
	}

	/**
	 * Step.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @date 21 sept. 2023
	 */
	@Override
	public boolean step(final IScope scope) throws GamaRuntimeException {
		final IExpression frequencyExp = species.getFrequency();
		if (frequencyExp != null) {
			final int frequency = Cast.asInt(scope, frequencyExp.value(scope));
			final int step = scope.getClock().getCycle();
			if (frequency == 0 || step % frequency != 0) return true;
		}
		getSpecies().getArchitecture().preStep(scope, this);
		return stepAgents(scope);

	}

	/**
	 * Gets the populations.
	 *
	 * @param scope
	 *            the scope
	 * @return the populations
	 */
	@Override
	public Collection<? extends IPopulation<? extends IAgent>> getPopulations(final IScope scope) {
		return Collections.singleton(this);
	}

	/**
	 * Checks for agent list.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if successful
	 * @date 21 sept. 2023
	 */
	@Override
	public boolean hasAgentList() {
		return true;
	}

	/**
	 * Gets the agents.
	 *
	 * @param scope
	 *            the scope
	 * @return the agents
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public IContainer<?, ? extends IAgent> getAgents(final IScope scope) {
		IContainer c = GamaListFactory.create(scope, getGamlType().getContentType(), getGrid().matrix);
		return c;
	}

	/**
	 * Accept.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param a
	 *            the a
	 * @return true, if successful
	 * @date 21 sept. 2023
	 */
	@Override
	public boolean accept(final IScope scope, final IShape source, final IShape a) {
		final IAgent agent = a.getAgent();
		if (agent == null || agent.getPopulation() != this || agent.dead()) return false;
		final IAgent as = source.getAgent();
		if (agent == as) return false;
		return true;
	}

	/**
	 * Filter.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param source
	 *            the source
	 * @param results
	 *            the results
	 * @date 21 sept. 2023
	 */
	@Override
	public void filter(final IScope scope, final IShape source, final Collection<? extends IShape> results) {
		final IAgent sourceAgent = source == null ? null : source.getAgent();
		results.remove(sourceAgent);
		final Predicate<IShape> toRemove = each -> {
			final IAgent a = each.getAgent();
			return a == null || a.dead()
					|| a.getPopulation() != this
							&& (a.getPopulation().getGamlType().getContentType() != this.getGamlType().getContentType()
									|| !this.contains(a));
		};
		results.removeIf(toRemove);
	}

	@Override
	public void createVariablesFor(final IScope scope, final IGridAgent agent) throws GamaRuntimeException {
		for (final IVariable var : orderedVars) { var.initializeWith(scope, agent, null); }
	}

	@Override
	public boolean hasVar(final String n) {
		return species.getVar(n) != null;
	}

	/** The current agent index. */
	protected int currentAgentIndex;

	/** The is disposing. */
	private boolean isDisposing = false;

	@Override
	public IGridAgent createAgentAt(final IScope s, final int index, final Map<String, Object> initialValues,
			final boolean isRestored, final boolean toBeScheduled) throws GamaRuntimeException {

		final List<Map<String, Object>> mapInitialValues = new ArrayList<>();
		mapInitialValues.add(initialValues);

		// TODO : think to another solution... it is ugly
		final int tempIndexAgt = currentAgentIndex;

		currentAgentIndex = index;
		final IList<IGridAgent> listAgt = createAgents(s, 1, mapInitialValues, isRestored, toBeScheduled, null);
		currentAgentIndex = tempIndexAgt;

		return listAgt.firstValue(s);
	}

	@Override
	public String getName() { return species.getName(); }

	@Override
	public boolean hasAspect(final String default1) {
		return species.hasAspect(default1);
	}

	@Override
	public IExecutable getAspect(final String default1) {
		return species.getAspect(default1);
	}

	@Override
	public Collection<String> getAspectNames() { return species.getAspectNames(); }

	@Override
	public ISpecies getSpecies() { return species; }

	@Override
	public IVariable getVar(final String s) {
		return species.getVar(s);
	}

	@Override
	public boolean hasUpdatableVariables() {
		return updatableVars.length > 0;
	}

	@Override
	public IMacroAgent getHost() { return host; }

	@Override
	public void setHost(final IMacroAgent agt) { host = agt; }

	@Override
	public void addListener(final Listener listener) {
		notifier.addListener(listener);
	}

	@Override
	public void removeListener(final Listener listener) {
		notifier.removeListener(listener);
	}

	@Override
	public void updateVariables(final IScope scope, final IAgent a) {
		for (final IVariable v : updatableVars) {
			try (StopWatch w = GAMA.benchmark(scope, v)) {
				scope.setCurrentSymbol(v);
				scope.setAgentVarValue(a, v.getName(), v.getUpdatedValue(scope));
			}
		}
		scope.setCurrentSymbol(null);

	}

	@Override
	public String toString() {
		return "Population of " + species.getName();
	}

	@Override
	public boolean isInitOverriden() { return isInitOverriden; }

	@Override
	public boolean isStepOverriden() { return isStepOverriden; }

	@Override
	public final int hashCode() {
		return hashCode;
	}

	@Override
	public final boolean equals(final Object o) {
		return o == this;
	}

	/**
	 * Removes the all occurrences of value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 * @date 21 sept. 2023
	 */
	@Override
	public void removeAllOccurrencesOfValue(final IScope scope, final Object value) {}

	/**
	 * Removes the values.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param values
	 *            the values
	 * @date 21 sept. 2023
	 */
	@Override
	public void removeValues(final IScope scope, final IContainer values) {}

	/**
	 * Removes the value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 * @date 21 sept. 2023
	 */
	@Override
	public void removeValue(final IScope scope, final Object value) {}

	/**
	 * Adds the values.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param values
	 *            the values
	 * @date 21 sept. 2023
	 */
	@Override
	public void addValues(final IScope scope, final IContainer values) {}

	/**
	 * Adds the value at index.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param index
	 *            the index
	 * @param value
	 *            the value
	 * @date 21 sept. 2023
	 */
	@Override
	public void addValueAtIndex(final IScope scope, final Object index, final IGridAgent value) {}

	/**
	 * Adds the value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param value
	 *            the value
	 * @date 21 sept. 2023
	 */
	@Override
	public void addValue(final IScope scope, final IGridAgent value) {}

	@Override
	public JsonObject serializeToJson(final IJson json) {
		return (JsonObject) IPopulation.Grid.super.serializeToJson(json).add("cols", json.valueOf(getNbCols()))
				.add("rows", json.valueOf(getNbRows()));
	}

	@Override
	public boolean isDisposing() { // TODO Auto-generated method stub
		return isDisposing;
	}

	/**
	 * Gets the grid.
	 *
	 * @return the grid
	 */
	@Override
	public GamaSpatialMatrix getGrid() { return grid; }

}