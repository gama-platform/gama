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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.Geometry;

import gama.api.constants.ISerialisationConstants;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.GamaType;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.AgentReference;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.serialization.ISerialisedAgent;
import gama.api.kernel.serialization.ISerialisedPopulation;
import gama.api.kernel.serialization.SerialisedAgent;
import gama.api.kernel.serialization.SerialisedGrid;
import gama.api.kernel.serialization.SerialisedPopulation;
import gama.api.kernel.species.ISpecies;
import gama.api.kernel.topology.IGrid;
import gama.api.runtime.scope.IScope;
import gama.api.types.font.GamaFontFactory;
import gama.api.types.font.IFont;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IShape;
import gama.api.types.geometry.IShape.Type;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.types.map.IMap;
import gama.api.utils.geometry.GamaGeometryFactory;
import gama.api.utils.geometry.GeometryUtils;
import gama.extension.serialize.fst.FSTBasicObjectSerializer;
import gama.extension.serialize.fst.FSTClazzInfo;
import gama.extension.serialize.fst.FSTClazzInfo.FSTFieldInfo;
import gama.extension.serialize.fst.FSTConfiguration;
import gama.extension.serialize.fst.FSTObjectInput;
import gama.extension.serialize.fst.FSTObjectOutput;

/**
 * The Class FSTImplementation. Allows to provide common initializations to FST Configurations and do the dirty work.
 * Not thread / simulation safe.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 2 août 2023
 */
public class BinarySerialiser implements ISerialisationConstants {

	/** The fst. */
	FSTConfiguration fst;

	/** The in agent. */
	boolean inAgent;

	/** The scope. */
	IScope scope;

	/**
	 * Instantiates a new gama FST serialiser.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope.
	 * @date 5 août 2023
	 */
	public BinarySerialiser() {
		fst = initConfiguration(FSTConfiguration.createDefaultConfiguration());
	}

	/**
	 * Restore simulation from.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param sim
	 *            the sim
	 * @param some
	 *            the some
	 * @date 8 août 2023
	 */
	public void restoreAgentFromBytes(final IAgent sim, final byte[] input) {
		scope = sim.getScope();
		try {
			SerialisedAgent sa = (SerialisedAgent) fst.asObject(input);
			sa.restoreAs(scope, sim);
		} catch (Exception e) {
			throw GamaRuntimeException.create(e, scope);
		} finally {
			scope = null;
		}
	}

	/**
	 * Save object to bytes.
	 *
	 * @param newScope
	 *            the scope
	 * @param obj
	 *            the obj
	 * @return the byte[]
	 */
	public byte[] saveObjectToBytes(final IScope newScope, final Object obj) {
		inAgent = false;
		return fst.asByteArray(obj instanceof IAgent a ? SerialisedAgent.of(a, true) : obj);
	}

	/**
	 * Restore object from bytes.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param input
	 *            the input
	 * @return the object
	 * @date 29 sept. 2023
	 */
	public Object createObjectFromBytes(final IScope newScope, final byte[] input) {
		try {
			scope = newScope;
			Object o = fst.asObject(input);
			if (o instanceof SerialisedAgent sa) return sa.recreateIn(scope);
			return o;
		} catch (Exception e) {
			throw GamaRuntimeException.create(e, scope);
		} finally {
			scope = null;
		}

	}

	/**
	 * Register serialisers.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 5 août 2023
	 */
	@SuppressWarnings ("rawtypes")
	protected void registerSerialisers(final FSTConfiguration conf) {

		register(conf, IShape.class, new FSTIndividualSerialiser<IShape>() {

			@Override
			protected boolean shouldRegister() {
				return false;
			}

			// TODO The inner attributes of the shape should be saved (ie the ones that do not belong to the var names
			// of the species
			@Override
			public void serialise(final FSTObjectOutput out, final IShape toWrite) throws Exception {
				Double d = toWrite.getDepth();
				IShape.Type t = toWrite.getGeometricalType();
				out.writeDouble(d == null ? 0d : d);
				out.writeInt(t.ordinal());
				out.writeObject(toWrite.getInnerGeometry());
				// out.writeObject(AgentReference.of(toWrite.getAgent()));
			}

			@Override
			public IShape deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
				double d = in.readDouble();
				IShape.Type t = IShape.Type.values()[in.readInt()];
				IShape result = GamaShapeFactory.createFrom((Geometry) in.readObject());
				// AgentReference agent = (AgentReference) in.readObject();
				// if (agent != AgentReference.NULL) { result.setAgent(agent.getReferencedAgent(scope)); }
				if (d > 0d) { result.setDepth(d); }
				if (t != Type.NULL) { result.setGeometricalType(t); }
				return result;
			}
		});

		register(conf, IAgent.class, new FSTIndividualSerialiser<IAgent>() {

			@Override
			protected boolean shouldRegister() {
				return false;
			}

			@Override
			public void serialise(final FSTObjectOutput out, final IAgent o) throws Exception {

				if (inAgent) {
					out.writeBoolean(true); // isRef
					out.writeObject(AgentReference.of(o));
				} else {
					inAgent = true;
					out.writeBoolean(false); // isRef
					out.writeObject(SerialisedAgent.of(o, true));
					inAgent = false;
				}
			}

			@Override
			public IAgent deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
				boolean isRef = in.readBoolean();
				if (isRef) {
					AgentReference ref = (AgentReference) in.readObject(AgentReference.class);
					return ref.getReferencedAgent(scope);
				}
				SerialisedAgent sa = (SerialisedAgent) in.readObject(SerialisedAgent.class);
				return sa.recreateIn(scope);
			}

		});

		register(conf, IType.class, new FSTIndividualSerialiser<IType>() {

			@Override
			public void serialise(final FSTObjectOutput out, final IType toWrite) throws Exception {
				out.writeStringUTF(toWrite.getGamlType().getName());
				if (toWrite.isCompoundType()) {
					out.writeObject(toWrite.getKeyType());
					out.writeObject(toWrite.getContentType());
				}
			}

			@Override
			public IType deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
				String name = in.readStringUTF();
				IType type = scope.getType(name);
				if (type.isCompoundType()) {
					IType key = (IType) in.readObject();
					IType content = (IType) in.readObject();
					return GamaType.from(type, key, content);
				}
				return type;
			}

		});

		register(conf, IScope.class, new FSTIndividualSerialiser<IScope>() {

			@Override
			public void serialise(final FSTObjectOutput out, final IScope toWrite) throws Exception {
				out.writeStringUTF(toWrite.getName());
			}

			@Override
			public IScope deserialise(final IScope scope1, final FSTObjectInput in) throws Exception {
				String name = in.readStringUTF();
				return scope1.copy(name);
			}

		});

		register(conf, ISpecies.class, new FSTIndividualSerialiser<ISpecies>() {

			@Override
			public void serialise(final FSTObjectOutput out, final ISpecies o) throws Exception {
				out.writeStringUTF(o.getName());
			}

			@Override
			public ISpecies deserialise(final IScope scope1, final FSTObjectInput in) throws Exception {
				String name = in.readStringUTF();
				return scope1.getModel().getSpecies(name);
			}

		});

		register(conf, AgentReference.class, new FSTIndividualSerialiser<AgentReference>() {

			@Override
			public void serialise(final FSTObjectOutput out, final AgentReference o) throws Exception {
				out.writeObject(o.species());
				out.writeObject(o.index());
			}

			@Override
			public AgentReference deserialise(final IScope scope1, final FSTObjectInput in) throws Exception {
				return AgentReference.of((String[]) in.readObject(), (Integer[]) in.readObject());
			}
		});

		register(conf, SerialisedAgent.class, new FSTIndividualSerialiser<SerialisedAgent>() {

			@Override
			public void serialise(final FSTObjectOutput out, final SerialisedAgent o) throws Exception {
				out.writeInt(o.index());
				out.writeStringUTF(o.species());
				out.writeObject(o.attributes());
				out.writeObject(o.innerPopulations());
			}

			@SuppressWarnings ("unchecked")
			@Override
			public SerialisedAgent deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
				return new SerialisedAgent(in.readInt(), in.readStringUTF(), (Map<String, Object>) in.readObject(),
						(Map<String, ISerialisedPopulation>) in.readObject());
			}
		});

		register(conf, SerialisedPopulation.class, new FSTIndividualSerialiser<SerialisedPopulation>() {

			@Override
			public void serialise(final FSTObjectOutput out, final SerialisedPopulation o) throws Exception {
				out.writeStringUTF(o.speciesName());
				out.writeObject(o.agents());
			}

			@SuppressWarnings ("unchecked")
			@Override
			public SerialisedPopulation deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
				return new SerialisedPopulation(in.readStringUTF(), (List<ISerialisedAgent>) in.readObject());
			}
		});

		register(conf, SerialisedGrid.class, new FSTIndividualSerialiser<SerialisedGrid>() {

			@Override
			public void serialise(final FSTObjectOutput out, final SerialisedGrid o) throws Exception {
				out.writeStringUTF(o.speciesName());
				out.writeObject(o.agents());
				out.writeObject(o.matrix());
			}

			@SuppressWarnings ("unchecked")
			@Override
			public SerialisedGrid deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
				return new SerialisedGrid(in.readStringUTF(), (List<ISerialisedAgent>) in.readObject(),
						(IGrid) in.readObject());
			}
		});

		register(conf, GamaGeometryFactory.class, new FSTIndividualSerialiser<GamaGeometryFactory>() {

			@Override
			public void serialise(final FSTObjectOutput out, final GamaGeometryFactory o) throws Exception {
				out.writeStringUTF("*GGF*");
			}

			@Override
			public GamaGeometryFactory deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
				in.readStringUTF();
				return GeometryUtils.getGeometryFactory();
			}
		});

		register(conf, IFont.class, new FSTIndividualSerialiser<IFont>() {

			@Override
			public void serialise(final FSTObjectOutput out, final IFont o) throws Exception {
				out.writeStringUTF(o.getName());
				out.writeInt(o.getStyle());
				out.writeInt(o.getSize());
			}

			@Override
			public IFont deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
				return GamaFontFactory.createFont(in.readStringUTF(), in.readInt(), in.readInt());
			}
		});

		register(conf, IMap.class, new FSTIndividualSerialiser<IMap>() {

			@SuppressWarnings ("unchecked")
			@Override
			public void serialise(final FSTObjectOutput out, final IMap o) throws Exception {
				out.writeObject(o.getGamlType().getKeyType());
				out.writeObject(o.getGamlType().getContentType());
				out.writeBoolean(o.isOrdered());
				out.writeInt(o.size());
				o.forEach((k, v) -> {
					try {
						out.writeObject(k);
						out.writeObject(v);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}

			@SuppressWarnings ({ "unchecked" })
			@Override
			public IMap deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
				IType k = (IType) in.readObject();
				IType c = (IType) in.readObject();
				boolean ordered = in.readBoolean();
				IMap<Object, Object> result = GamaMapFactory.create(k, c, ordered);
				int size = in.readInt();
				for (int i = 0; i < size; i++) { result.put(in.readObject(), in.readObject()); }
				return result;
			}

		});

		register(conf, IList.class, new FSTIndividualSerialiser<IList>() {

			@Override
			protected boolean shouldRegister() {
				return false;
			}

			@SuppressWarnings ({ "unchecked" })
			@Override
			public void serialise(final FSTObjectOutput out, final IList o) throws Exception {
				out.writeObject(o.getGamlType().getContentType());
				out.writeInt(o.size());
				o.forEach(v -> {
					try {
						out.writeObject(v);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}

			@Override
			public IList deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
				IType c = (IType) in.readObject();
				IList<Object> result = GamaListFactory.create(c);
				int size = in.readInt();
				for (int i = 0; i < size; i++) { result.add(in.readObject()); }
				return result;
			}

		});

		register(conf, CoordinateSequenceFactory.class, new FSTIndividualSerialiser<CoordinateSequenceFactory>() {

			@Override
			public void serialise(final FSTObjectOutput out, final CoordinateSequenceFactory o) throws Exception {
				out.writeStringUTF("*GCSF*");
			}

			@Override
			public CoordinateSequenceFactory deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
				in.readStringUTF();
				return GeometryUtils.getGeometryFactory().getCoordinateSequenceFactory();
			}
		});
	}

	/**
	 * Register.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param <T>
	 *            the generic type
	 * @param clazz
	 *            the clazz
	 * @date 5 août 2023
	 */
	public <T> void register(final FSTConfiguration conf, final Class<T> clazz, final FSTIndividualSerialiser<T> ser) {
		conf.registerSerializer(clazz, ser, true);
	}

	/**
	 * Inits the common.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param conf
	 *            the conf
	 * @return the FST configuration
	 * @date 2 août 2023
	 */
	public FSTConfiguration initConfiguration(final FSTConfiguration conf) {
		registerSerialisers(conf);
		return conf;
	}

	/**
	 * The Class GamaFSTSerialiser.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 5 août 2023
	 */
	abstract class FSTIndividualSerialiser<T> extends FSTBasicObjectSerializer {

		/**
		 * Should register.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @return true, if successful
		 * @date 21 févr. 2024
		 */
		protected boolean shouldRegister() {
			return true;
		}

		/**
		 * Instantiate.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @param objectClass
		 *            the object class
		 * @param in
		 *            the in
		 * @param serializationInfo
		 *            the serialization info
		 * @param referencee
		 *            the referencee
		 * @param streamPosition
		 *            the stream position
		 * @return the t
		 * @throws Exception
		 *             the exception
		 * @date 7 août 2023
		 */
		@SuppressWarnings ("rawtypes")
		@Override
		public final T instantiate(final Class objectClass, final FSTObjectInput in,
				final FSTClazzInfo serializationInfo, final FSTFieldInfo referencee, final int streamPosition)
				throws Exception {

			T result = deserialise(scope, in);
			if (shouldRegister()) { in.registerObject(result, streamPosition, serializationInfo, referencee); }
			return result;
		}

		/**
		 * Write object.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @param out
		 *            the out
		 * @param toWrite
		 *            the to write
		 * @param clzInfo
		 *            the clz info
		 * @param referencedBy
		 *            the referenced by
		 * @param streamPosition
		 *            the stream position
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 * @date 7 août 2023
		 */
		@SuppressWarnings ("unchecked")
		@Override
		public void writeObject(final FSTObjectOutput out, final Object toWrite, final FSTClazzInfo clzInfo,
				final FSTFieldInfo referencedBy, final int streamPosition) throws IOException {
			try {
				serialise(out, (T) toWrite);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * Serialise.
		 *
		 * @param out
		 *            the out
		 * @param toWrite
		 *            the to write
		 * @throws Exception
		 *             the exception
		 */
		public void serialise(final FSTObjectOutput out, final T toWrite) throws Exception {}

		/**
		 * Deserialise.
		 *
		 * @param scope
		 *            the scope
		 * @param in
		 *            the in
		 * @return the t
		 * @throws Exception
		 *             the exception
		 */
		abstract public T deserialise(IScope scope, FSTObjectInput in) throws Exception;

	}

}
