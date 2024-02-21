/*******************************************************************************************************
 *
 * MinimalGridAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.topology.grid;

import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.agent.AbstractAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShape;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.util.GamaColor;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.gaml.operators.Cast;
import gama.gaml.types.Types;

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
		geometry = population.grid.matrix[index].getGeometry();
	}

	@Override
	public GamaColor getColor() {
		if (population.grid.isHexagon) return (GamaColor) getAttribute(IKeyword.COLOR);
		return GamaColor.get(population.grid.supportImagePixels[getIndex()]);
	}

	@Override
	public void setColor(final GamaColor color) {
		if (population.grid.isHexagon) {
			setAttribute(IKeyword.COLOR, color);
		} else {
			population.grid.supportImagePixels[getIndex()] = color.getRGB();
		}
	}

	@Override
	public void setGeometricalType(final Type t) {}

	@Override
	public final int getX() {
		if (population.grid.isHexagon()) return population.grid.getX(getGeometry());
		return (int) (getLocation().getX() / population.grid.cellWidth);
	}

	@Override
	public final int getY() {
		if (population.grid.isHexagon()) return population.grid.getY(getGeometry());
		return (int) (getLocation().getY() / population.grid.cellHeight);
	}

	@Override
	public double getValue() {
		if (population.grid.gridValue != null) return population.grid.gridValue[getIndex()];
		return 0d;
	}

	@Override
	public void setValue(final double d) {
		if (population.grid.gridValue != null) { population.grid.gridValue[getIndex()] = d; }
	}

	@Override
	public IPopulation<?> getPopulation() { return population; }

	@Override
	public IShape getGeometry(final IScope scope) {
		return geometry;
	}

	@Override
	public IList<IAgent> getNeighbors(final IScope scope) {
		return Cast.asList(scope, population.grid.getNeighborhood().getNeighborsIn(scope, getIndex(), 1));
	}

	/**
	 * Method getPoints()
	 *
	 * @see gama.core.metamodel.shape.IShape#getPoints()
	 */
	@Override
	public IList<GamaPoint> getPoints() { return geometry.getPoints(); }

	@Override
	public void setDepth(final double depth) {

	}

	/**
	 * Method getArea()
	 *
	 * @see gama.core.metamodel.shape.IShape#getArea()
	 */
	@Override
	public Double getArea() { return geometry.getArea(); }

	/**
	 * Method getVolume()
	 *
	 * @see gama.core.metamodel.shape.IShape#getVolume()
	 */
	@Override
	public Double getVolume() { return geometry.getVolume(); }

	/**
	 * Method getPerimeter()
	 *
	 * @see gama.core.metamodel.shape.IShape#getPerimeter()
	 */
	@Override
	public double getPerimeter() { return geometry.getPerimeter(); }

	/**
	 * Method getHoles()
	 *
	 * @see gama.core.metamodel.shape.IShape#getHoles()
	 */
	@Override
	public IList<GamaShape> getHoles() { return geometry.getHoles(); }

	/**
	 * Method getCentroid()
	 *
	 * @see gama.core.metamodel.shape.IShape#getCentroid()
	 */
	@Override
	public GamaPoint getCentroid() { return geometry.getCentroid(); }

	/**
	 * Method getExteriorRing()
	 *
	 * @see gama.core.metamodel.shape.IShape#getExteriorRing(gama.core.runtime.IScope)
	 */
	@Override
	public GamaShape getExteriorRing(final IScope scope) {
		return geometry.getExteriorRing(scope);
	}

	/**
	 * Method getWidth()
	 *
	 * @see gama.core.metamodel.shape.IShape#getWidth()
	 */
	@Override
	public Double getWidth() { return geometry.getWidth(); }

	/**
	 * Method getHeight()
	 *
	 * @see gama.core.metamodel.shape.IShape#getHeight()
	 */
	@Override
	public Double getHeight() { return geometry.getHeight(); }

	/**
	 * Method getDepth()
	 *
	 * @see gama.core.metamodel.shape.IShape#getDepth()
	 */
	@Override
	public Double getDepth() { return geometry.getDepth(); }

	/**
	 * Method getGeometricEnvelope()
	 *
	 * @see gama.core.metamodel.shape.IShape#getGeometricEnvelope()
	 */
	@Override
	public GamaShape getGeometricEnvelope() { return geometry.getGeometricEnvelope(); }

	@Override
	public IList<? extends IShape> getGeometries() { return geometry.getGeometries(); }

	/**
	 * Method isMultiple()
	 *
	 * @see gama.core.metamodel.shape.IShape#isMultiple()
	 */
	@Override
	public boolean isMultiple() { return geometry.isMultiple(); }

	@Override
	public IList<Double> getBands() {
		if (population.grid.nbBands == 1) {
			final IList<Double> bd = GamaListFactory.create(null, Types.FLOAT);
			bd.add(getValue());
			return bd;
		}
		return population.grid.bands.get(getIndex());
	}

}