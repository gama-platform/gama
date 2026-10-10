/*******************************************************************************************************
 *
 * ForyBinarySerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.fory;

import java.util.ArrayList;
import java.util.List;

import org.apache.fory.Fory;
import org.apache.fory.config.Config;
import org.apache.fory.config.Language;
import org.apache.fory.resolver.TypeResolver;
import org.apache.fory.serializer.Serializer;
import org.locationtech.jts.geom.CoordinateSequenceFactory;

import gama.api.constants.ISerialisationConstants;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.AgentReference;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.object.IClass;
import gama.api.kernel.object.IObject;
import gama.api.kernel.serialization.AbstractBinarySerializer;
import gama.api.kernel.serialization.IGamaObjectSerializer;
import gama.api.kernel.serialization.SerialisedAgent;
import gama.api.kernel.serialization.SerialisedGrid;
import gama.api.kernel.serialization.SerialisedPopulation;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;
import gama.api.types.date.IDate;
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
import gama.extension.serialize.binary.AgentReferenceSerialiser;
import gama.extension.serialize.binary.CoordinateSequenceFactorySerialiser;
import gama.extension.serialize.binary.GamaCoordinateSequenceSerialiser;
import gama.extension.serialize.binary.GamaGeometryFactorySerialiser;
import gama.extension.serialize.binary.GamaMessageSerialiser;
import gama.extension.serialize.binary.GamaSpatialGraphSerialiser;
import gama.extension.serialize.binary.GamaSpatialPathSerialiser;
import gama.extension.serialize.binary.IAgentSerialiser;
import gama.extension.serialize.binary.IClassSerialiser;
import gama.extension.serialize.binary.IColorSerialiser;
import gama.extension.serialize.binary.IDateSerialiser;
import gama.extension.serialize.binary.IFontSerialiser;
import gama.extension.serialize.binary.IGamaMailBoxSerialiser;
import gama.extension.serialize.binary.IGraphSerialiser;
import gama.extension.serialize.binary.IListSerialiser;
import gama.extension.serialize.binary.IMapSerialiser;
import gama.extension.serialize.binary.IObjectSerialiser;
import gama.extension.serialize.binary.IPointSerialiser;
import gama.extension.serialize.binary.IScopeSerialiser;
import gama.extension.serialize.binary.IShapeSerialiser;
import gama.extension.serialize.binary.ISpeciesSerialiser;
import gama.extension.serialize.binary.ITypeSerialiser;
import gama.extension.serialize.binary.SerialisedAgentSerialiser;
import gama.extension.serialize.binary.SerialisedGridSerialiser;
import gama.extension.serialize.binary.SerialisedPopulationSerialiser;
import gama.extension.serialize.binary.UniqueCoordinateSequenceSerialiser;

/**
 * Binary serialiser based on Apache Fory. Registers the same GAMA serialisers as the FST implementation, so that both
 * backends handle exactly the same types. Like FST, serialisers are registered for a type <em>and</em> its subtypes:
 * when several registered types match a class, the most specific one is used.
 *
 * <p>
 * This class is not thread-safe and must not be shared across simulations.
 * </p>
 */
public class ForyBinarySerialiser extends AbstractBinarySerializer implements ISerialisationConstants {

	/** A GAMA serialiser registered for a type and its subtypes. */
	private record Registration<T>(Class<T> type, IGamaObjectSerializer<T> serializer) {}

	/** The registered serialisers, in registration order. */
	private final List<Registration<?>> registrations = new ArrayList<>();

	/** The underlying Fory instance. */
	private final Fory fory;

	/**
	 * Constructs a new {@code ForyBinarySerialiser} and registers all the GAMA type serialisers.
	 */
	public ForyBinarySerialiser() {
		fory = Fory.builder().withLanguage(Language.JAVA).withClassLoader(ForyBinarySerialiser.class.getClassLoader())
				.withAsyncCompilation(true).withClassVersionCheck(false).withRefTracking(true)
				.requireClassRegistration(false).withJdkClassSerializableCheck(false).withCodegen(true)
				.withCompatible(false).build();
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
		register(IDate.class, new IDateSerialiser());
		register(GamaMailbox.class, new IGamaMailBoxSerialiser());
		fory.registerSerializerFactory(this::createSerializer);
	}

	/**
	 * Registers a GAMA serialiser for the given type and its subtypes.
	 */
	private <T> void register(final Class<T> type, final IGamaObjectSerializer<T> serializer) {
		registrations.add(new Registration<>(type, serializer));
	}

	/**
	 * Finds the serialiser registered for the most specific supertype of the given class.
	 *
	 * @return a Fory serialiser, or {@code null} if the class is not handled by any GAMA serialiser
	 */
	@SuppressWarnings ({ "unchecked", "rawtypes" })
	private Serializer<?> createSerializer(final TypeResolver resolver, final Class<?> cls) {
		Registration<?> best = null;
		for (final Registration<?> r : registrations) {
			if (r.type().isAssignableFrom(cls) && (best == null || best.type().isAssignableFrom(r.type()))) {
				best = r;
			}
		}
		if (best == null) return null;
		final Config config = fory.getConfig();
		return new ForyGamaIndividualSerialiser(config, cls, best.serializer(), getContext());
	}

	@Override
	protected Object fromByteArrayToObject(final byte[] input) {
		return fory.deserialize(input);
	}

	@Override
	protected byte[] fromObjectToByteArray(final Object obj) {
		return fory.serialize(obj);
	}

}
