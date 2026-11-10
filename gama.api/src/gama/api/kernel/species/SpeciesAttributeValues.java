/*******************************************************************************************************
 *
 * SpeciesAttributeValues.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.species;

import java.util.ArrayList;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.symbols.IVariable;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.topology.IGrid;
import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.matrix.GamaMatrixFactory;
import gama.api.types.matrix.IMatrix;

/** Typed species attribute snapshots and assignments, with spatial matrices for grids. */
public final class SpeciesAttributeValues {

	private SpeciesAttributeValues() {}

	public static IPopulation<? extends IAgent> population(final IScope scope, final Object target) {
		if (target instanceof ISpecies species) {
			final IPopulation<? extends IAgent> population = species.getPopulation(scope);
			if (population != null) return population;
		}
		throw GamaRuntimeException.error("Species attribute access expects an initialized population", scope);
	}

	public static Object read(final IScope scope, final Object target, final String name,
			final IType<?> type, final boolean spatial) {
		final IPopulation<? extends IAgent> population = population(scope, target);
		if (spatial) return read(scope, gridPopulation(scope, population), name, type);
		final IList<Object> result = GamaListFactory.create(type, population.size());
		for (final IAgent agent : population) {
			result.add(agent.getDirectVarValue(scope, name));
		}
		return result;
	}

	private static IPopulation.Grid gridPopulation(final IScope scope, final IPopulation<?> population) {
		if (population instanceof IPopulation.Grid grid) return grid;
		throw GamaRuntimeException.error("Grid attribute access expects an initialized grid population", scope);
	}

	public static IMatrix<?> read(final IScope scope, final IPopulation.Grid population, final String name,
			final IType<?> type) {
		final IGrid grid = population.getGrid();
		final int cols = grid.getCols(scope);
		final int rows = grid.getRows(scope);
		final IMatrix<?> result = type.id() == IType.FLOAT ? GamaMatrixFactory.createField(scope, cols, rows)
				: GamaMatrixFactory.create(cols, rows, type);
		if (type.id() == IType.FLOAT && population.size() == cols * rows) {
			grid.getValuesInto(scope, name, result.getFieldData(scope));
			return result;
		}
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				final IShape shape = grid.get(scope, col, row);
				final IAgent cell = shape == null ? null : shape.getAgent();
				result.set(scope, col, row, cell == null ? type.createDefaultValue(scope)
						: cell.getDirectVarValue(scope, name));
			}
		}
		return result;
	}

	public static void write(final IScope scope, final Object target, final String name, final Object value,
			final boolean spatial) {
		final IPopulation<? extends IAgent> population = population(scope, target);
		final IVariable variable = population.getSpecies().getVar(name);
		if (variable == null || variable.isNotModifiable() || variable.isFunction())
			throw GamaRuntimeException.error("Species attribute '" + name + "' is not writable", scope);
		if (!spatial) {
			if (!(value instanceof IList<?> values))
				throw GamaRuntimeException.error("Species attribute '" + name + "' must be assigned a non-nil list", scope);
			final var agents = new ArrayList<>(population);
			if (values.size() != agents.size())
				throw GamaRuntimeException.error("The list for species attribute '" + name + "' must have "
						+ agents.size() + " elements", scope);
			if (!values.getGamlType().getContentType().isTranslatableInto(variable.getType()))
				throw GamaRuntimeException.error("List elements are incompatible with species attribute '" + name + "'", scope);
			final var items = new ArrayList<>(values);
			for (int i = 0; i < agents.size(); i++) {
				scope.setAgentVariableValue(agents.get(i), variable, items.get(i));
			}
			return;
		}
		if (!(value instanceof IMatrix<?> matrix))
			throw GamaRuntimeException.error("Grid attribute '" + name + "' must be assigned a non-nil matrix", scope);
		final IGrid grid = gridPopulation(scope, population).getGrid();
		final int cols = grid.getCols(scope);
		final int rows = grid.getRows(scope);
		if (matrix.getCols(scope) != cols || matrix.getRows(scope) != rows)
			throw GamaRuntimeException.error("The matrix for grid attribute '" + name + "' must have " + cols
					+ " columns and " + rows + " rows", scope);
		if (!matrix.getGamlType().getContentType().isTranslatableInto(variable.getType()))
			throw GamaRuntimeException.error("Matrix elements are incompatible with grid attribute '" + name + "'", scope);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				final IShape shape = grid.get(scope, col, row);
				if (shape != null && shape.getAgent() != null)
					scope.setAgentVariableValue(shape.getAgent(), variable, matrix.get(scope, col, row));
			}
		}
	}
}
