/*******************************************************************************************************
 *
 * MinimalGridAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.topology.grid;

import gama.annotations.constants.IKeyword;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IGridAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.core.agent.AbstractAgent;

/**
 * The Class MinimalGridAgent.
 */
public class MinimalGridAgent extends AbstractAgent implements IGridAgent {

	/** The geometry. */
	private final IShape geometry;

	/** The population. */
	private final GridPopulation population;

	/**
	 * Instantiates a new minimal grid agent.
	 *
	 * @param index
	 *            the index
	 * @param gridPopulation
	 *            TODO
	 */
	public MinimalGridAgent(final GridPopulation gridPopulation, final int index) {
		super(index);
		population = gridPopulation;
		geometry = population.getGrid().matrix[index].getGeometry();
	}

	@Override
	public IColor getColor() {
		if (population.getGrid().isHexagon) return (IColor) getAttribute(IKeyword.COLOR);
		return GamaColorFactory.get(population.getGrid().supportImagePixels[getIndex()]);
	}

	@Override
	public void setColor(final IColor color) {
		if (population.getGrid().isHexagon) {
			setAttribute(IKeyword.COLOR, color);
		} else {
			population.getGrid().supportImagePixels[getIndex()] = color.getRGB();
		}
	}

	@Override
	public void setGeometricalType(final Type t) {}

	@Override
	public final int getX() {
		if (population.getGrid().isHexagon()) return population.getGrid().getX(getGeometry());
		return (int) (getLocation().getX() / population.getGrid().cellWidth);
	}

	@Override
	public final int getY() {
		if (population.getGrid().isHexagon()) return population.getGrid().getY(getGeometry());
		return (int) (getLocation().getY() / population.getGrid().cellHeight);
	}

	@Override
	public double getValue() {
		if (population.getGrid().gridValue != null) return population.getGrid().gridValue[getIndex()];
		return 0d;
	}

	@Override
	public void setValue(final double d) {
		if (population.getGrid().gridValue != null) { population.getGrid().gridValue[getIndex()] = d; }
	}

	@Override
	public IPopulation<?> getPopulation() { return population; }

	@Override
	public IShape getGeometry(final IScope scope) {
		return geometry;
	}

	@Override
	public IList<IAgent> getNeighbors(final IScope scope) {
		return GamaListFactory.castToList(scope,
				population.getGrid().getNeighborhood().getNeighborsIn(scope, getIndex(), 1));
	}

	/**
	 * Method getPoints()
	 *
	 * @see gama.api.types.geometry.IShape#getPoints()
	 */
	@Override
	public IList<IPoint> getPoints() { return geometry.getPoints(); }

	@Override
	public void setDepth(final double depth) {

	}

	/**
	 * Method getArea()
	 *
	 * @see gama.api.types.geometry.IShape#getArea()
	 */
	@Override
	public Double getArea() { return geometry.getArea(); }

	/**
	 * Method getVolume()
	 *
	 * @see gama.api.types.geometry.IShape#getVolume()
	 */
	@Override
	public Double getVolume() { return geometry.getVolume(); }

	/**
	 * Method getPerimeter()
	 *
	 * @see gama.api.types.geometry.IShape#getPerimeter()
	 */
	@Override
	public double getPerimeter() { return geometry.getPerimeter(); }

	/**
	 * Method getHoles()
	 *
	 * @see gama.api.types.geometry.IShape#getHoles()
	 */
	@Override
	public IList<IShape> getHoles() { return geometry.getHoles(); }

	/**
	 * Method getCentroid()
	 *
	 * @see gama.api.types.geometry.IShape#getCentroid()
	 */
	@Override
	public IPoint getCentroid() { return geometry.getCentroid(); }

	/**
	 * Method getExteriorRing()
	 *
	 * @see gama.api.types.geometry.IShape#getExteriorRing(gama.api.runtime.scope.IScope)
	 */
	@Override
	public IShape getExteriorRing(final IScope scope) {
		return geometry.getExteriorRing(scope);
	}

	/**
	 * Method getWidth()
	 *
	 * @see gama.api.types.geometry.IShape#getWidth()
	 */
	@Override
	public Double getWidth() { return geometry.getWidth(); }

	/**
	 * Method getHeight()
	 *
	 * @see gama.api.types.geometry.IShape#getHeight()
	 */
	@Override
	public Double getHeight() { return geometry.getHeight(); }

	/**
	 * Method getDepth()
	 *
	 * @see gama.api.types.geometry.IShape#getDepth()
	 */
	@Override
	public Double getDepth() { return geometry.getDepth(); }

	/**
	 * Method getGeometricEnvelope()
	 *
	 * @see gama.api.types.geometry.IShape#getGeometricEnvelope()
	 */
	@Override
	public IShape getGeometricEnvelope() { return geometry.getGeometricEnvelope(); }

	@Override
	public IList<? extends IShape> getGeometries() { return geometry.getGeometries(); }

	/**
	 * Method isMultiple()
	 *
	 * @see gama.api.types.geometry.IShape#isMultiple()
	 */
	@Override
	public boolean isMultiple() { return geometry.isMultiple(); }

	@Override
	public IList<Double> getBands() {
		if (population.getGrid().nbBands == 1) {
			final IList<Double> bd = GamaListFactory.create(null, Types.FLOAT);
			bd.add(getValue());
			return bd;
		}
		return population.getGrid().bands.get(getIndex());
	}

}