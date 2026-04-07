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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.IVariable;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IMacroAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.species.ISpecies;
import gama.api.kernel.topology.IGrid;
import gama.api.runtime.GamaExecutorService;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.list.IList;
import gama.api.types.matrix.IMatrix;
import gama.api.types.misc.IContainer;
import gama.api.types.topology.GamaTopologyFactory;
import gama.api.utils.json.IJson;
import gama.core.agent.GamlAgent;
import gama.core.agent.MinimalAgent;
import gama.core.population.AbstractPopulation;
import gama.core.util.json.JsonObject;
import gama.dev.DEBUG;

/**
 * Class GridPopulation.
 *
 * @author drogoul
 * @since 14 mai 2013
 *
 */
@SuppressWarnings ("unchecked")
public class GridPopulation extends AbstractPopulation<IAgent> implements IPopulation.Grid {

	static {
		DEBUG.OFF();
	}

	/** The grid. */
	GamaSpatialMatrix agentsContainer;

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
	public GridPopulation(final IMacroAgent host, final ISpecies species) {
		super(host, species);
	}

	/**
	 * Compute topology.
	 *
	 * @param scope
	 *            the scope
	 */
	@Override
	public void computeTopology(final IScope scope) {
		topology = GamaTopologyFactory.createGrid(scope, species, host);
		agentsContainer = (GamaSpatialMatrix) topology.getPlaces();
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
	public IList<IAgent> createAgents(final IScope scope, final int number,
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
	public IList<IAgent> createAgents(final IScope scope, final IContainer<?, ? extends IShape> geometries) {
		for (int i = 0; i < getGrid().actualNumberOfCells; i++) {
			final IShape s = getGrid().matrix[i];
			final Class javaBase = species.getDescription().getJavaBase();
			final boolean usesRegularAgents = GamlAgent.class.isAssignableFrom(javaBase);
			if (s != null) {
				getGrid().matrix[i] = usesRegularAgents ? new GamlAgent(this, i, s) : new MinimalAgent(this, i, s);
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
		fireAgentsAdded(scope, (IList) getAgents(scope));
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
	@Override
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
	public IAgent getAgent(final Integer index) {
		if (index >= size() || index < 0) return null;
		final IShape s = getGrid().matrix[index];
		return s == null ? null : s.getAgent();
	}

	@Override
	public IAgent getOrCreateAgent(final IScope scope, final Integer index, final Map<String, Object> attributes) {
		return getAgent(index);
	}

	@Override
	public GridTopology getTopology() { return (GridTopology) topology; }

	@Override
	public IAgent getAgent(final IScope scope, final IPoint coord) {
		return getGrid().getAgentAt(coord);
	}

	@Override
	public void killMembers() throws GamaRuntimeException {
		for (final IShape a : getGrid().matrix) { if (a != null) { a.dispose(); } }
	}

	@Override
	public IAgent[] toArray() {
		return Arrays.copyOf(agentsContainer.matrix, agentsContainer.matrix.length, IAgent[].class);
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
	public IAgent getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		if (indices == null) return null;
		final int n = indices.length(scope);
		if (n == 0) return null;
		final int x = Cast.asInt(scope, indices.get(scope, 0));
		if (n == 1) return getAgent(Cast.asInt(scope, x));
		final int y = Cast.asInt(scope, indices.get(scope, 1));
		final IShape s = getGrid().get(scope, x, y);
		if (s == null) return null;
		return s.getAgent();
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
	public IAgent get(final IScope scope, final Integer index) throws GamaRuntimeException {
		// WARNING False if the matrix is not dense
		return (IAgent) getGrid().matrix[index];
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
	public IAgent firstValue(final IScope scope) throws GamaRuntimeException {
		return (IAgent) getGrid()._first(scope);
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
	public IAgent lastValue(final IScope scope) throws GamaRuntimeException {
		return (IAgent) getGrid()._last(scope);
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
	public IAgent anyValue(final IScope scope) {
		return (IAgent) getGrid().anyValue(scope);
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
	public Iterator<IAgent> iterator() {
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
		if (o instanceof Integer i) return super.containsKey(scope, i);
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
	public java.lang.Iterable<IAgent> iterable(final IScope scope) {
		return listValue(scope, getGamlType().getContentType(), false);
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
	public IList<IAgent> listValue(final IScope scope, final IType contentsType, final boolean copy)
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
	 * Compare to.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param o
	 *            the o
	 * @return the int
	 * @date 21 sept. 2023
	 */
	@Override
	public int compareTo(final IPopulation<IAgent> o) {
		return species == o.getSpecies() ? 0 : 1;
	}

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
		return o instanceof IAgent ga && ga.getPopulation() == this;
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
	public boolean add(final IAgent e) {
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
	 * Adds the all.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param c
	 *            the c
	 * @return true, if successful
	 * @date 21 sept. 2023
	 */
	@Override
	public boolean addAll(final Collection<? extends IAgent> c) {
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
	public boolean addAll(final int index, final Collection<? extends IAgent> c) {
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
	 * Gets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param index
	 *            the index
	 * @return the i grid agent
	 * @date 21 sept. 2023
	 */
	@Override
	public IAgent get(final int index) {
		return (IAgent) getGrid().matrix[index];
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
	public IAgent set(final int index, final IAgent element) {
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
	public void add(final int index, final IAgent element) {}

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
	public IAgent remove(final int index) {
		return null;
	}

	/** The current agent index. */
	protected int currentAgentIndex;

	@Override
	public IAgent createAgentAtIndex(final IScope s, final int index, final Map<String, Object> initialValues,
			final boolean isRestored, final boolean toBeScheduled) throws GamaRuntimeException {

		final List<Map<String, Object>> mapInitialValues = new ArrayList<>();
		mapInitialValues.add(initialValues);

		// TODO : think to another solution... it is ugly
		final int tempIndexAgt = currentAgentIndex;

		currentAgentIndex = index;
		final IList<IAgent> listAgt = createAgents(s, 1, mapInitialValues, isRestored, toBeScheduled, null);
		currentAgentIndex = tempIndexAgt;

		return listAgt.firstValue(s);
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
	public void addValueAtIndex(final IScope scope, final Object index, final IAgent value) {}

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
	public void addValue(final IScope scope, final IAgent value) {}

	@Override
	public JsonObject serializeToJson(final IJson json) {
		return (JsonObject) super.serializeToJson(json).add("cols", json.valueOf(getNbCols())).add("rows",
				json.valueOf(getNbRows()));
	}

	/**
	 * Gets the grid.
	 *
	 * @return the grid
	 */
	@Override
	public GamaSpatialMatrix getGrid() { return agentsContainer; }

	@Override
	public List<IAgent> internalListOfAgents(final boolean makeCopy, final boolean onlyLiving) {
		List l = !makeCopy ? Arrays.asList(agentsContainer.matrix) : Lists.newArrayList(agentsContainer.matrix);
		return l;
	}

	/**
	 * Sets the grid. Used in deserialisation
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
		agentsContainer = (GamaSpatialMatrix) matrix;
		getTopology().setPlaces(matrix);

	}

}