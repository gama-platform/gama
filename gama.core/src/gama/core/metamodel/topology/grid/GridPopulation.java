/*******************************************************************************************************
 *
 * GridPopulation.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.topology.grid;

import static gama.core.common.interfaces.IKeyword.CELL_HEIGHT;
import static gama.core.common.interfaces.IKeyword.CELL_WIDTH;
import static gama.core.common.interfaces.IKeyword.FILE;
import static gama.core.common.interfaces.IKeyword.FILES;
import static gama.core.common.interfaces.IKeyword.NEIGHBORS;
import static gama.core.common.interfaces.IKeyword.WIDTH;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import gama.core.common.geometry.Envelope3D;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.GamlAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.agent.IMacroAgent;
import gama.core.metamodel.agent.MinimalAgent;
import gama.core.metamodel.population.AbstractPopulation;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.GamaExecutorService;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IContainer;
import gama.core.util.IList;
import gama.core.util.file.GamaGridFile;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonObject;
import gama.core.util.matrix.IMatrix;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.RemoteSequence;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import gama.gaml.variables.IVariable;

/**
 * Class GridPopulation.
 *
 * @author drogoul
 * @since 14 mai 2013
 *
 */
@SuppressWarnings ("unchecked")
public class GridPopulation extends AbstractPopulation<IAgent> implements IPopulation<IAgent> {

	/** The agentsContainer. */
	GamaSpatialMatrix agentsContainer;

	/**
	 * Builds the agentsContainer topology.
	 *
	 * @param scope
	 *            the scope
	 * @param species
	 *            the species
	 * @param host
	 *            the host
	 * @return the i topology
	 */
	@Override
	public void computeTopology(final IScope scope) {
		IExpression exp = species.getFacet(WIDTH);
		final Envelope3D env = scope.getSimulation().getGeometry().getEnvelope();
		final int rows = exp == null
				? species.hasFacet(CELL_WIDTH)
						? (int) (env.getWidth() / Cast.asFloat(scope, species.getFacet(CELL_WIDTH).value(scope))) : 100
				: Cast.asInt(scope, exp.value(scope));
		exp = species.getFacet(IKeyword.HEIGHT);
		final int columns = exp == null ? species.hasFacet(CELL_HEIGHT)
				? (int) (env.getHeight() / Cast.asFloat(scope, species.getFacet(CELL_HEIGHT).value(scope))) : 100
				: Cast.asInt(scope, exp.value(scope));

		final boolean isTorus = host.getTopology().isTorus();
		exp = species.getFacet("use_individual_shapes");
		final boolean useIndividualShapes = exp == null || Cast.asBool(scope, exp.value(scope));
		exp = species.getFacet("use_neighbors_cache");
		final boolean useNeighborsCache = exp == null || Cast.asBool(scope, exp.value(scope));
		exp = species.getFacet("horizontal_orientation");
		final boolean horizontalOrientation = exp == null || Cast.asBool(scope, exp.value(scope));

		exp = species.getFacet("optimizer");
		final String optimizer = exp == null ? "" : Cast.asString(scope, exp.value(scope));

		exp = species.getFacet(NEIGHBORS);
		final boolean usesVN = exp == null || Cast.asInt(scope, exp.value(scope)) == 4;
		final boolean isHexagon = exp != null && Cast.asInt(scope, exp.value(scope)) == 6;
		exp = species.getFacet(FILES);
		IList<GamaGridFile> files = null;
		if (exp != null) { files = Cast.asList(scope, exp.value(scope)); }
		if (files != null && !files.isEmpty()) {
			topology = new GridTopology(scope, host, files, isTorus, usesVN, useIndividualShapes, useNeighborsCache,
					optimizer);
		} else {
			exp = species.getFacet(FILE);
			final GamaGridFile file = (GamaGridFile) (exp != null ? exp.value(scope) : null);
			if (file == null) {
				topology = new GridTopology(scope, host, rows, columns, isTorus, usesVN, isHexagon,
						horizontalOrientation, useIndividualShapes, useNeighborsCache, optimizer);
			} else {
				topology = new GridTopology(scope, host, file, isTorus, usesVN, useIndividualShapes, useNeighborsCache,
						optimizer);
			}

		}
		agentsContainer = (GamaSpatialMatrix) topology.getPlaces();
	}

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

	@SuppressWarnings ("unchecked")
	@Override
	public IList<IAgent> createAgents(final IScope scope, final int number,
			final List<? extends Map<String, Object>> initialValues, final boolean isRestored,
			final boolean toBeScheduled, final RemoteSequence sequence) throws GamaRuntimeException {

		createAgents(scope, null);
		for (final Map attr : initialValues) {
			final IAgent agt = getAgent((Integer) attr.get("grid_x"), (Integer) attr.get("grid_y"));
			if (agt != null) { agt.setAttributes(attr); }
		}
		return (IList) getAgents(scope);
	}

	@Override
	public IList<IAgent> createAgents(final IScope scope, final IContainer<?, ? extends IShape> geometries) {
		for (int i = 0; i < agentsContainer.actualNumberOfCells; i++) {
			final IShape s = agentsContainer.matrix[i];
			final Class javaBase = species.getDescription().getJavaBase();
			final boolean usesRegularAgents = GamlAgent.class.isAssignableFrom(javaBase);
			if (s != null) {
				final IAgent g = usesRegularAgents ? new GamlAgent(this, i, s) : new MinimalAgent(this, i, s);
				agentsContainer.matrix[i] = g;
			}
		}

		for (final IVariable var : orderedVars) {
			for (int i = 0; i < agentsContainer.actualNumberOfCells; i++) {
				final IAgent a = (IAgent) agentsContainer.matrix[i];
				if (a != null) { var.initializeWith(scope, a, null); }
			}
		}

		for (int i = 0; i < agentsContainer.actualNumberOfCells; i++) {
			final IAgent a = (IAgent) agentsContainer.matrix[i];
			if (a != null) { a.schedule(scope); }
		}
		fireAgentsAdded(scope, (IList) getAgents(scope));
		return null;

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
		return GamaExecutorService.step(scope, agentsContainer.matrix, getSpecies());
	}

	/**
	 * Gets the nb cols.
	 *
	 * @return the nb cols
	 */
	public int getNbCols() { return agentsContainer.numCols; }

	/**
	 * Gets the nb rows.
	 *
	 * @return the nb rows
	 */
	public int getNbRows() { return agentsContainer.numRows; }

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
		final IShape s = agentsContainer.get(null, col, row);
		return s == null ? null : s.getAgent();
	}

	/**
	 * Gets the agentsContainer value.
	 *
	 * @param col
	 *            the col
	 * @param row
	 *            the row
	 * @return the agentsContainer value
	 */
	public Double getGridValue(final Integer col, final Integer row) {
		if (col >= getNbCols() || col < 0 || row >= getNbRows() || row < 0) return 0.0;
		return agentsContainer.getGridValue(col, row);
	}

	@Override
	public IAgent getAgent(final Integer index) {
		if (index >= size() || index < 0) return null;
		final IShape s = agentsContainer.matrix[index];
		return s == null ? null : s.getAgent();
	}

	@Override
	public boolean isGrid() { return true; }

	@Override
	public GridTopology getTopology() { return (GridTopology) topology; }

	@Override
	public IAgent getAgent(final IScope scope, final GamaPoint coord) {
		return agentsContainer.getAgentAt(coord);
	}

	@Override
	public synchronized IAgent[] toArray() {
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
		return agentsContainer.actualNumberOfCells;
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
		final IShape s = agentsContainer.get(scope, x, y);
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
	 * @return the i agentsContainer agent
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @date 21 sept. 2023
	 */
	@Override
	public IAgent get(final IScope scope, final Integer index) throws GamaRuntimeException {
		// WARNING False if the matrix is not dense
		return (IAgent) agentsContainer.matrix[index];
	}

	/**
	 * First value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return the i agentsContainer agent
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @date 21 sept. 2023
	 */
	@Override
	public IAgent firstValue(final IScope scope) throws GamaRuntimeException {
		return (IAgent) agentsContainer._first(scope);
	}

	/**
	 * Last value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return the i agentsContainer agent
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 * @date 21 sept. 2023
	 */
	@Override
	public IAgent lastValue(final IScope scope) throws GamaRuntimeException {
		return (IAgent) agentsContainer._last(scope);
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
		return agentsContainer.actualNumberOfCells;
	}

	/**
	 * Any value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @return the i agentsContainer agent
	 * @date 21 sept. 2023
	 */
	@Override
	public IAgent anyValue(final IScope scope) {
		return (IAgent) agentsContainer.anyValue(scope);
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
		if (o instanceof Integer i) return IPopulation.super.containsKey(scope, i);
		if (o instanceof GamaPoint) return agentsContainer.containsKey(scope, o);
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
	public boolean isEmpty() { return agentsContainer._isEmpty(null); }

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
		return agentsContainer._isEmpty(scope);
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
		return agentsContainer._listValue(scope, contentsType, false);
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
			return agentsContainer;
		return agentsContainer.matrixValue(scope, contentsType, copy);
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
	public IMatrix matrixValue(final IScope scope, final IType type, final GamaPoint size, final boolean copy)
			throws GamaRuntimeException {
		if (type == null || type.id() == IType.NONE
				|| type.getSpeciesName() != null && type.getSpeciesName().equals(getSpecies().getName()))
			return agentsContainer;
		return agentsContainer.matrixValue(scope, type, copy);
	}

	/**
	 * Sets the agentsContainer.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param matrix
	 *            the new agentsContainer
	 * @date 28 août 2023
	 */
	public void setGrid(final IGrid matrix) {
		// DEBUG.OUT("Setting a new GamaSpatialMatrix. Are the display data different ? "
		// + !Arrays.equals(agentsContainer.getDisplayData(), matrix.getDisplayData()));
		// DEBUG.OUT("Are the agentsContainer values equal in both grids ? " + Arrays.equals(agentsContainer.gridValue,
		// matrix.getGridValue()));
		// System.arraycopy(matrix.getDisplayData(), 0, agentsContainer.supportImagePixels, 0,
		// agentsContainer.supportImagePixels.length);
		// just for debug purposes
		agentsContainer = (GamaSpatialMatrix) matrix;
		getTopology().setPlaces(matrix);

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
		return (T[]) agentsContainer.matrix;
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
	 * @return the i agentsContainer agent
	 * @date 21 sept. 2023
	 */
	@Override
	public IAgent get(final int index) {
		return (IAgent) agentsContainer.matrix[index];
	}

	/**
	 * Sets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param index
	 *            the index
	 * @param element
	 *            the element
	 * @return the i agentsContainer agent
	 * @date 21 sept. 2023
	 */
	@Override
	public IAgent set(final int index, final IAgent element) {
		agentsContainer.matrix[index] = element;
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
	 * @return the i agentsContainer agent
	 * @date 21 sept. 2023
	 */
	@Override
	public IAgent remove(final int index) {
		return null;
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
	public JsonObject serializeToJson(final Json json) {
		return (JsonObject) IPopulation.super.serializeToJson(json).add("cols", json.valueOf(getNbCols())).add("rows",
				json.valueOf(getNbRows()));
	}

	@Override
	public List<IAgent> internalListOfAgents(final boolean makeCopy, final boolean onlyLiving) {
		List l = !makeCopy ? Arrays.asList(agentsContainer.matrix) : Lists.newArrayList(agentsContainer.matrix);
		return l;
	}

}
