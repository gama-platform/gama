/*******************************************************************************************************
 *
 * FSTBinarySerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.fst;

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
 * The Class FSTBinarySerialiser. Provides common initialisation for FST configurations and coordinates binary
 * serialisation and deserialisation of GAMA objects and agents.
 *
 * <p>
 * Each supported GAMA type is handled by a dedicated {@link FSTGamaIndividualSerialiser} subclass registered via
 * {@link #registerSerialisers(FSTConfiguration)}. This class is not thread-safe and must not be shared across
 * simulations.
 * </p>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 2 août 2023
 */
public class FSTBinarySerialiser extends AbstractBinarySerializer implements ISerialisationConstants {

	/**
	 * The underlying FST configuration holding all registered serialisers and configuration state.
	 */
	FSTConfiguration fst;

	/**
	 * Constructs a new {@code FSTBinarySerialiser} and initialises its FST configuration with all registered type
	 * serialisers.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 5 août 2023
	 */
	public FSTBinarySerialiser() {
		fst = FSTConfiguration.createDefaultConfiguration();
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
	}

	/**
	 * @param class1
	 * @param iPointSerialiser
	 */
	private <T> void register(final Class<T> class1, final IGamaObjectSerializer<T> serializer) {
		final FSTGamaIndividualSerialiser<T> ser = new FSTGamaIndividualSerialiser<>(serializer);
		fst.registerSerializer(class1, ser, true);
		ser.setSerializationContext(getContext());
	}

	@Override
	protected Object fromByteArrayToObject(final byte[] input) {
		return fst.asObject(input);
	}

	@Override
	protected byte[] fromObjectToByteArray(final Object obj) {
		return fst.asByteArray(obj);
	}

}