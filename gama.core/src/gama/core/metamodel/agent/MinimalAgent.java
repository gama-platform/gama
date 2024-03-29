/*******************************************************************************************************
 *
 * MinimalAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.metamodel.agent;

import java.util.Objects;
import java.util.Set;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.species;
import gama.core.common.geometry.Envelope3D;
import gama.core.common.interfaces.IKeyword;
import gama.core.metamodel.population.IPopulation;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShapeFactory;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.ITopology;
import gama.core.runtime.IScope;
import gama.core.util.graph.GamaGraph;
import gama.gaml.species.GamlSpecies;
import gama.gaml.species.ISpecies;
import gama.gaml.types.GamaGeometryType;

/**
 * The Class MinimalAgent.
 */
@species (
		name = IKeyword.AGENT,
		doc = @doc ("The species parent of all agent species"))
/**
 * A concrete implementation of AbstractAgent that declares its own population, geometry and name. Base of most of the
 * concrete subclasses of GAMA agents
 *
 * @author drogoul
 *
 */
public class MinimalAgent extends AbstractAgent {

	/** The population that this agent belongs to. */
	protected final IPopulation<? extends IAgent> population;

	/** The name. */
	protected String name;

	/** The geometry. */
	protected final IShape geometry;

	/** The hash code. */
	public final int hashCode;

	/**
	 * @param s
	 *            the population used to prototype the agent.
	 */
	public MinimalAgent(final IPopulation<? extends IAgent> s, final int index) {
		this(s, index, GamaShapeFactory.create());
	}

	/**
	 * Instantiates a new minimal agent.
	 *
	 * @param population
	 *            the population that this agent belongs to.
	 * @param index
	 *            the index
	 * @param geometry
	 *            the geometry
	 */
	protected MinimalAgent(final IPopulation<? extends IAgent> population, final int index, final IShape geometry) {
		super(index);
		this.population = population;
		this.hashCode = Objects.hash(getPopulation(), index);
		this.geometry = geometry;
		geometry.setAgent(this);
	}

	/**
	 * Instantiates a new minimal agent with a given hashcode.
	 *
	 * @param population
	 *            the population that this agent belongs to.
	 * @param index
	 *            the index
	 * @param hashcode
	 *            the hashcode
	 * @param geometry
	 *            the geometry
	 */
	public MinimalAgent(final IPopulation<? extends IAgent> population, final int index, final int hashcode,
			final IShape geometry) {
		super(index);
		this.population = population;
		this.hashCode = hashcode;
		this.geometry = geometry;
		geometry.setAgent(this);
	}

	@Override
	public IPopulation<? extends IAgent> getPopulation() { return population; }

	@Override
	public IShape getGeometry(final IScope scope) {
		return geometry;
	}

	@Override
	public/* synchronized */void setGeometry(final IScope scope, final IShape newGeometry) {
		// Addition to address Issue 817: if the new geometry is exactly the one
		// possessed by the agent, no need to change anything.
		if (newGeometry == geometry || newGeometry == null || newGeometry.getInnerGeometry() == null || dead()
				|| this.getSpecies().isGrid() && ((GamlSpecies) this.getSpecies()).belongsToAMicroModel())
			return;

		final ITopology topology = getTopology();
		final GamaPoint newGeomLocation = newGeometry.getLocation().copy(scope);

		// if the old geometry is "shared" with another agent, we create a new
		// one. otherwise, we copy it directly.
		final IAgent other = newGeometry.getAgent();
		IShape newLocalGeom;
		if (other == null) {
			newLocalGeom = newGeometry;
		} else {
			// If the agent is different, we do not copy the attributes present in the shape passed as argument (see
			// Issue #2053).
			newLocalGeom = GamaShapeFactory.createFrom(newGeometry.getInnerGeometry().copy());
			newLocalGeom.copyShapeAttributesFrom(newGeometry);
		}
		// topology.normalizeLocation(newGeomLocation, false);

		if (!newGeomLocation.equals(newLocalGeom.getLocation())) { newLocalGeom.setLocation(newGeomLocation); }

		newLocalGeom.setAgent(this);
		final Envelope3D previous = Envelope3D.of(geometry);
		geometry.setGeometry(newLocalGeom);

		topology.updateAgent(previous, this);

		// update micro-agents' locations accordingly

		// TODO DOES NOT WORK FOR THE MOMENT
		// for ( final IPopulation pop : getMicroPopulations() ) {
		// pop.hostChangesShape();
		// }

		notifyVarValueChange(IKeyword.SHAPE, newLocalGeom);
	}

	@Override
	public String getName() {
		if (name == null) { name = super.getName(); }
		if (dead()) return name + " (dead)";
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
		notifyVarValueChange(IKeyword.NAME, name);
	}

	@SuppressWarnings ("rawtypes")
	@Override
	public/* synchronized */GamaPoint setLocation(final IScope scope, final GamaPoint point) {
		if (point == null || dead() || this.getSpecies().isGrid()) return getLocation();
		GamaPoint newLocation = point.copy(scope);
		final ITopology topology = getTopology();
		if (topology == null) return getLocation();
		newLocation = topology.normalizeLocation(scope, newLocation, false);

		if (geometry == null || geometry.getInnerGeometry() == null) {
			setGeometry(GamaGeometryType.createPoint(newLocation));
		} else {
			final GamaPoint previousPoint = geometry.getLocation();
			if (newLocation.equals(previousPoint)) return newLocation;
			final Envelope3D previous = geometry.getEnvelope();
			geometry.setLocation(newLocation);
			topology.updateAgent(previous, this);

			// update micro-agents' locations accordingly
			// for ( final IPopulation pop : getMicroPopulations() ) {
			// // FIXME DOES NOT WORK FOR THE MOMENT
			// pop.hostChangesShape();
			// }
		}
		final GamaGraph graph = (GamaGraph) getAttribute("attached_graph");
		if (graph != null) {
			final Set edgesToModify = graph.edgesOf(this);
			for (final Object obj : edgesToModify) {
				if (obj instanceof IAgent) {
					final IShape ext1 = (IShape) graph.getEdgeSource(obj);
					final IShape ext2 = (IShape) graph.getEdgeTarget(obj);
					((IAgent) obj).setGeometry(GamaGeometryType.buildLine(ext1.getLocation(), ext2.getLocation()));
				}
			}

		}
		notifyVarValueChange(IKeyword.LOCATION, newLocation);
		return newLocation;

	}

	@Override
	public/* synchronized */GamaPoint getLocation(final IScope scope) {
		if (geometry == null || geometry.getInnerGeometry() == null) {
			final ITopology t = getTopology();
			final GamaPoint randomLocation = t == null ? null : t.getRandomLocation(scope);
			if (randomLocation == null) return null;
			setGeometry(GamaGeometryType.createPoint(randomLocation));
			return randomLocation;
		}
		return geometry.getLocation();
	}

	@Override
	public boolean isInstanceOf(final ISpecies s, final boolean direct) {
		// TODO and direct ?
		if (IKeyword.AGENT.equals(s.getName())) return true;
		return super.isInstanceOf(s, direct);
	}

	/**
	 * During the call to init, the agent will search for the action named _init_ and execute it. Its default
	 * implementation is provided in this class as well (equivalent to a super.init())
	 *
	 * @see GamlAgent#_init_()
	 * @see gama.core.common.interfaces.IStepable#step(gama.core.runtime.IScope)
	 * @warning This method should NOT be overriden (except for some rare occasions like in SimulationAgent). Always
	 *          override _init_(IScope) instead.
	 */
	@Override
	public boolean init(final IScope scope) {
		if (!getPopulation().isInitOverriden()) {
			_init_(scope);
		} else {
			scope.execute(getSpecies().getAction(ISpecies.initActionName), this, null);
		}
		return !scope.interrupted();
	}

	/**
	 * During the call to doStep(), the agent will search for the action named _step_ and execute it. Its default
	 * implementation is provided in this class as well (equivalent to a super.doStep());
	 *
	 * @see GamlAgent#_step_()
	 * @see gama.core.common.interfaces.IStepable#step(gama.core.runtime.IScope)
	 * @warning This method should NOT be overridden (except for some rare occasions like in SimulationAgent). Always
	 *          override _step_(IScope) instead.
	 */
	@Override
	public boolean doStep(final IScope scope) {
		if (!getPopulation().isStepOverriden()) {
			super.doStep(scope);
			return !scope.interrupted();
		}
		return scope.execute(getSpecies().getAction(ISpecies.stepActionName), this, null).passed();
	}

	/**
	 * The default init of agents consists in calling the super implementation of init() in order to realize the default
	 * init sequence
	 *
	 * @param scope
	 * @return
	 */
	@action (
			name = ISpecies.initActionName)
	public Object _init_(final IScope scope) {
		return super.init(scope);
	}

	/**
	 * The default step of agents consists in calling the super implementation of step() in order to realize the default
	 * step sequence
	 *
	 * TODO verify this sequence as _step_() is NEVER called (only from GAML if done explicitly)
	 *
	 * @param scope
	 * @return
	 */
	@action (
			name = ISpecies.stepActionName)
	public Object _step_(final IScope scope) {
		return super.step(scope);
	}

	@Override
	public final int hashCode() {
		return hashCode;
	}

}
