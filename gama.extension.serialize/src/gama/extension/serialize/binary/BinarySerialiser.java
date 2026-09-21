/*******************************************************************************************************
 *
 * BinarySerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import org.locationtech.jts.geom.CoordinateSequenceFactory;

import gama.api.constants.ISerialisationConstants;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.AgentReference;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.object.IClass;
import gama.api.kernel.object.IObject;
import gama.api.kernel.serialization.SerialisedAgent;
import gama.api.kernel.serialization.SerialisedGrid;
import gama.api.kernel.serialization.SerialisedPopulation;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.types.font.IFont;
import gama.api.types.geometry.IPoint;
import gama.api.types.geometry.IShape;
import gama.api.types.graph.IGraph;
import gama.api.types.list.IList;
import gama.api.types.map.IMap;
import gama.api.utils.geometry.GamaCoordinateSequence;
import gama.api.utils.geometry.GamaGeometryFactory;
import gama.api.utils.geometry.UniqueCoordinateSequence;
import gama.core.topology.graph.GamaSpatialGraph;
import gama.core.util.messaging.GamaMailbox;
import gama.core.util.messaging.GamaMessage;
import gama.core.util.path.GamaSpatialPath;

import org.eclipse.serializer.Serializer;
import org.eclipse.serializer.SerializerFoundation;

/**
 * The Class BinarySerialiser. Provides common initialisation for Eclipse Serializer configurations and coordinates binary
 * serialisation and deserialisation of GAMA objects and agents.
 *
 * <p>
 * Each supported GAMA type is handled by a dedicated {@link EclipseIndividualSerialiser} subclass registered via
 * {@link #registerSerialisers()}. This class is not thread-safe and must not be shared across
 * simulations.
 * </p>
 */
public class BinarySerialiser implements ISerialisationConstants {

	/**
	 * The underlying Eclipse Serializer configuration holding all registered serialisers and configuration state.
	 */
	SerializerFoundation<?> foundation;

	/**
	 * Flag indicating whether the serialiser is currently inside an agent serialisation. Used by
	 * {@link IAgentSerialiser} to detect nesting and write references instead of full agents.
	 */
	boolean inAgent;

	/**
	 * The current GAMA simulation scope, set before each (de)serialisation operation and cleared afterwards.
	 */
	IScope scope;

	/**
	 * Constructs a new {@code BinarySerialiser} and initialises its configuration with all registered type
	 * serialisers.
	 */
	public BinarySerialiser() {
		foundation = SerializerFoundation.New();
		initConfiguration();
	}

	/**
	 * Restores the state of a live agent from a previously serialised byte array. The agent's attributes and inner
	 * populations are replaced by the stored values.
	 *
	 * @param sim
	 *            the target agent whose state will be restored
	 * @param input
	 *            the byte array produced by a prior serialisation of this agent
	 */
	public void restoreAgentFromBytes(final IAgent sim, final byte[] input) {
		scope = sim.getScope();
		try {
			Serializer<byte[]> serializer = Serializer.Bytes(foundation);
			SerialisedAgent sa = (SerialisedAgent) serializer.deserialize(input);
			sa.restoreAs(scope, sim);
		} catch (Exception e) {
			throw GamaRuntimeException.create(e, scope);
		} finally {
			scope = null;
		}
	}

	/**
	 * Serialises an object (or agent) to a byte array. If the object is an {@link IAgent}, it is first wrapped in a
	 * {@link SerialisedAgent}.
	 *
	 * @param newScope
	 *            the current GAMA simulation scope
	 * @param obj
	 *            the object to serialise
	 * @return the serialised byte array
	 */
	public byte[] saveObjectToBytes(final IScope newScope, final Object obj) {
		inAgent = false;
		Serializer<byte[]> serializer = Serializer.Bytes(foundation);
		return serializer.serialize(obj instanceof IAgent a ? SerialisedAgent.of(a, true) : obj);
	}

	/**
	 * Deserialises an object from a byte array. If the deserialised result is a {@link SerialisedAgent}, the
	 * corresponding live agent is recreated in the given scope.
	 *
	 * @param newScope
	 *            the current GAMA simulation scope
	 * @param input
	 *            the byte array to deserialise
	 * @return the deserialised object, or a recreated agent if the data represents a {@link SerialisedAgent}
	 */
	public Object createObjectFromBytes(final IScope newScope, final byte[] input) {
		try {
			scope = newScope;
			Serializer<byte[]> serializer = Serializer.Bytes(foundation);
			Object o = serializer.deserialize(input);
			if (o instanceof SerialisedAgent sa) return sa.recreateIn(scope);
			return o;
		} catch (Exception e) {
			throw GamaRuntimeException.create(e, scope);
		} finally {
			scope = null;
		}
	}

	/**
	 * Registers all individual type serialisers with the given Eclipse Serializer configuration. Each GAMA type handled by this
	 * serialiser has its own dedicated {@link EclipseIndividualSerialiser} subclass instantiated here.
	 */
	@SuppressWarnings ("rawtypes")
	protected void registerSerialisers() {
		register(IPoint.class, new IPointSerialiser());
		register(IShape.class, new IShapeSerialiser());
		register(IObject.class, new IObjectSerialiser());
		register(IAgent.class, new IAgentSerialiser());
		register(IType.class, new ITypeSerialiser());
		register(IScope.class, new IScopeSerialiser());
		register(GamaMessage.class, new GamaMessageSerialiser());
		register(ISpecies.class, new ISpeciesSerialiser());
		register(IClass.class, new IClassSerialiser());
		register(AgentReference.class, new AgentReferenceSerialiser());
		register(SerialisedAgent.class, new SerialisedAgentSerialiser());
		register(SerialisedPopulation.class, new SerialisedPopulationSerialiser());
		register(SerialisedGrid.class, new SerialisedGridSerialiser());
		register(GamaGeometryFactory.class, new GamaGeometryFactorySerialiser());
		register(IFont.class, new IFontSerialiser());
		register(IMap.class, new IMapSerialiser());
		register(IList.class, new IListSerialiser());
		register(GamaSpatialGraph.class, new GamaSpatialGraphSerialiser());
		register(GamaSpatialPath.class, new GamaSpatialPathSerialiser());
		register(IGraph.class, new IGraphSerialiser());
		register(CoordinateSequenceFactory.class, new CoordinateSequenceFactorySerialiser());
		register(UniqueCoordinateSequence.class, new UniqueCoordinateSequenceSerialiser());
		register(GamaCoordinateSequence.class, new GamaCoordinateSequenceSerialiser());
		register(IColor.class, new IColorSerialiser());
		register(GamaMailbox.class, new IGamaMailBoxSerialiser());
	}

	/**
	 * Registers a single type serialiser with the given configuration.
	 *
	 * @param <T>
	 *            the type handled by the serialiser
	 * @param clazz
	 *            the class of the type to register
	 * @param ser
	 *            the serialiser to use for instances of {@code clazz}
	 */
	public <T> void register(final Class<T> clazz, final EclipseIndividualSerialiser<T> ser) {
		ser.setBinarySerialiser(this);
		foundation.registerCustomTypeHandler(clazz, ser);
	}

	/**
	 * Initialises the given configuration by registering all type serialisers.
	 *
	 * @return the initialised configuration
	 */
	public SerializerFoundation<?> initConfiguration() {
		registerSerialisers();
		return foundation;
	}

}
