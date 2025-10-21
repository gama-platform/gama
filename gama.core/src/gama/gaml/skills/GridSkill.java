/*******************************************************************************************************
 *
 * GridSkill.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.skills;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ITypeProvider;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.topology.grid.IGrid;
import gama.core.runtime.IScope;
import gama.core.util.GamaColor;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.gaml.operators.Cast;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * Written by drogoul Modified on 24 juin 2010
 *
 * @todo Description
 *
 */

/**
 * The Class GridSkill.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 28 août 2023
 */
@vars ({ @variable (
		name = IKeyword.COLOR,
		type = IType.COLOR,
		init = "#white",
		doc = { @doc ("Represents the color of the cell, used by default to represent the grid on displays") }),
		@variable (
				name = IKeyword.NEIGHBORS,
				type = IType.LIST,
				of = ITypeProvider.OWNER_TYPE,
				doc = { @doc ("Represents the neighbor at distance 1 of the cell") }),
		@variable (
				name = IKeyword.GRID_VALUE,
				type = IType.FLOAT,
				doc = { @doc ("Represents a floating point value (automatically set when the grid is initialized from a grid file, and used by default to represent the elevation of the cell when displaying it on a display)") }),
		@variable (
				name = IKeyword.BANDS,
				type = IType.LIST,
				of = IType.FLOAT,
				doc = { @doc ("Represents the values of the different bands of the cell (list of floating point value automatically set when the grid is initialized from a grid file)") }),
		@variable (
				name = IKeyword.GRID_X,
				type = IType.INT,
				constant = true,
				doc = { @doc ("Returns the 0-based index of the column of the cell in the grid") }),
		@variable (
				name = IKeyword.GRID_Y,
				type = IType.INT,
				constant = true,
				doc = { @doc ("Returns the 0-based index of the row of the cell in the grid") }) })
@skill (
		name = GridSkill.SKILL_NAME,
		concept = { IConcept.GRID, IConcept.SKILL },
		internal = true)
public class GridSkill extends GamlSkill {

	/** The Constant SKILL_NAME. */
	public static final String SKILL_NAME = "grid";

	/**
	 * Gets the grid.
	 *
	 * @param agent
	 *            the agent
	 * @return the grid
	 */
	protected final IGrid getGrid(final IAgent agent) {
		return (IGrid) agent.getPopulation().getTopology().getPlaces();
	}

	/**
	 * Gets the x.
	 *
	 * @param agent
	 *            the agent
	 * @return the x
	 */
	@getter ("grid_x")
	public final int getX(final IAgent agent) {
		return getGrid(agent).getX(agent);
	}

	/**
	 * Gets the value.
	 *
	 * @param agent
	 *            the agent
	 * @return the value
	 */
	@getter (
			value = "grid_value",
			initializer = true)
	public final double getValue(final IAgent agent) {
		return getGrid(agent).getValue(agent.getIndex());
	}

	/**
	 * Gets the bands.
	 *
	 * @param agent
	 *            the agent
	 * @return the bands
	 */
	@getter (
			value = "bands",
			initializer = true)
	public final IList<Double> getBands(final IScope scope, final IAgent agent) {
		if (getGrid(agent).getBandsNumber(scope) == 1) {
			final IList<Double> bd = GamaListFactory.create(null, Types.FLOAT);
			bd.add(getGrid(agent).getValue(agent.getIndex()));
			return bd;
		}
		return GamaListFactory.create(scope, Types.FLOAT, getGrid(agent).getBand(scope, agent.getIndex()));
	}

	/**
	 * Sets the bands.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param agent
	 *            the agent
	 * @return the i list
	 * @date 28 août 2023
	 */
	@setter (
			value = "bands")
	public final void setBands(final IAgent agent, final IList<Double> bands) {}

	/**
	 * Gets the y.
	 *
	 * @param agent
	 *            the agent
	 * @return the y
	 */
	@getter ("grid_y")
	public final int getY(final IAgent agent) {
		return getGrid(agent).getY(agent);
	}

	/**
	 * Sets the X.
	 *
	 * @param agent
	 *            the agent
	 * @param i
	 *            the i
	 */
	@setter ("grid_x")
	public final void setX(final IAgent agent, final Integer i) {}

	/**
	 * Sets the value.
	 *
	 * @param agent
	 *            the agent
	 * @param d
	 *            the d
	 */
	@setter ("grid_value")
	public final void setValue(final IAgent agent, final Double d) {
		getGrid(agent).setValue(agent.getIndex(), d);
	}

	/**
	 * Sets the Y.
	 *
	 * @param agent
	 *            the agent
	 * @param i
	 *            the i
	 */
	@setter ("grid_y")
	public final void setY(final IAgent agent, final Integer i) {}

	/**
	 * Gets the color.
	 *
	 * @param agent
	 *            the agent
	 * @return the color
	 */
	@getter (
			value = "color",
			initializer = true)
	public GamaColor getColor(final IAgent agent) {
		if (getGrid(agent).isHexagon()) return (GamaColor) agent.getAttribute(IKeyword.COLOR);
		return GamaColor.get(getGrid(agent).supportImagePixels()[agent.getIndex()]);
	}

	/**
	 * Sets the color.
	 *
	 * @param agent
	 *            the agent
	 * @param color
	 *            the color
	 */
	@setter ("color")
	public void setColor(final IAgent agent, final GamaColor color) {
		if (getGrid(agent).isHexagon()) {
			agent.setAttribute(IKeyword.COLOR, color);
		} else {
			getGrid(agent).supportImagePixels()[agent.getIndex()] = color.getRGB();
		}
	}

	/**
	 * Gets the neighbors.
	 *
	 * @param scope
	 *            the scope
	 * @param agent
	 *            the agent
	 * @return the neighbors
	 */
	@getter (
			value = IKeyword.NEIGHBORS,
			initializer = true)
	public IList<IAgent> getNeighbors(final IScope scope, final IAgent agent) {
		return Cast.asList(scope, getGrid(agent).getNeighborhood().getNeighborsIn(scope, agent.getIndex(), 1));
	}

}
