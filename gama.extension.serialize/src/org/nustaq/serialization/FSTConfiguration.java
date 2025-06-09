/*******************************************************************************************************
 *
 * FSTConfiguration.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.3).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package org.nustaq.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.nustaq.serialization.coders.FSTStreamDecoder;
import org.nustaq.serialization.coders.FSTStreamEncoder;
import org.nustaq.serialization.serializers.FSTArrayListSerializer;
import org.nustaq.serialization.serializers.FSTBigIntegerSerializer;
import org.nustaq.serialization.serializers.FSTBigNumberSerializers;
import org.nustaq.serialization.serializers.FSTBitSetSerializer;
import org.nustaq.serialization.serializers.FSTClassSerializer;
import org.nustaq.serialization.serializers.FSTCollectionSerializer;
import org.nustaq.serialization.serializers.FSTDateSerializer;
import org.nustaq.serialization.serializers.FSTEnumSetSerializer;
import org.nustaq.serialization.serializers.FSTMapSerializer;
import org.nustaq.serialization.serializers.FSTStringBufferSerializer;
import org.nustaq.serialization.serializers.FSTStringBuilderSerializer;
import org.nustaq.serialization.serializers.FSTStringSerializer;
import org.nustaq.serialization.serializers.FSTThrowableSerializer;
import org.nustaq.serialization.serializers.FSTTimestampSerializer;
import org.nustaq.serialization.util.DefaultFSTInt2ObjectMapFactory;
import org.nustaq.serialization.util.FSTInt2ObjectMapFactory;
import org.nustaq.serialization.util.FSTUtil;

import gama.core.util.ByteArrayZipper;
import gama.dev.DEBUG;
import gama.gaml.compilation.kernel.GamaClassLoader;

/**
 * Created with IntelliJ IDEA. User: ruedi Date: 18.11.12 Time: 20:41
 *
 * Holds a serialization configuration/metadata. Reuse this class !!! construction is very expensive. (just keep static
 * instances around or use thread locals)
 *
 */
public class FSTConfiguration {

	/** The stream coder factory. */
	private final StreamCoderFactory streamCoderFactory = new FSTDefaultStreamCoderFactory(this);

	/** The name. */
	private String name;

	/** The serialization info registry. */
	private final FSTClazzInfoRegistry serializationInfoRegistry = new FSTClazzInfoRegistry();

	/** The cached objects. */
	private final HashMap<Class, List<SoftReference>> cachedObjects = new HashMap<>(97);

	/** The int to object map factory. */
	FSTInt2ObjectMapFactory intToObjectMapFactory = new DefaultFSTInt2ObjectMapFactory();

	/** The class registry. */
	private final FSTClazzNameRegistry classRegistry = new FSTClazzNameRegistry(null);

	/** The class loader. */
	private final ClassLoader classLoader = GamaClassLoader.getInstance();

	/** The instantiator. */
	private final FSTClassInstantiator instantiator = new FSTDefaultClassInstantiator();

	/**
	 * Gets the int to object map factory.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the int to object map factory
	 * @date 30 sept. 2023
	 */
	public FSTInt2ObjectMapFactory getIntToObjectMapFactory() { return intToObjectMapFactory; }

	/**
	 * The Class FieldKey.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 30 sept. 2023
	 */
	// cache fieldinfo. This can be shared with derived FSTConfigurations in order to reduce footprint
	static class FieldKey {

		/** The clazz. */
		Class<?> clazz;

		/** The field name. */
		String fieldName;

		/**
		 * Instantiates a new field key.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @param clazz
		 *            the clazz
		 * @param fieldName
		 *            the field name
		 * @date 30 sept. 2023
		 */
		public FieldKey(final Class<?> clazz, final String fieldName) {
			this.clazz = clazz;
			this.fieldName = fieldName;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) { return true; }
			if (o == null || getClass() != o.getClass()) { return false; }

			FieldKey fieldKey = (FieldKey) o;

			if (!clazz.equals(fieldKey.clazz)) { return false; }
			return fieldName.equals(fieldKey.fieldName);

		}

		@Override
		public int hashCode() {
			int result = clazz.hashCode();
			result = 31 * result + fieldName.hashCode();
			return result;
		}
	}

	/** The field info cache. */
	final ConcurrentHashMap<FieldKey, FSTClazzInfo.FSTFieldInfo> fieldInfoCache;

	/**
	 * the standard FSTConfiguration. - safe (no unsafe r/w) - platform independent byte order - moderate compression
	 *
	 * note that if you are just read/write from/to byte arrays, its faster to use DefaultCoder.
	 *
	 * This should be used most of the time.
	 *
	 * @return
	 */
	public static FSTConfiguration createDefaultConfiguration() {
		FSTConfiguration conf = new FSTConfiguration();

		conf.registerIntToObjectMapFactory(new DefaultFSTInt2ObjectMapFactory());
		conf.addDefaultClazzes();
		// serializers
		FSTSerializerRegistry reg = conf.getCLInfoRegistry().getSerializerRegistry();
		reg.putSerializer(Class.class, new FSTClassSerializer(), false);
		reg.putSerializer(String.class, new FSTStringSerializer(), false);
		reg.putSerializer(Byte.class, new FSTBigNumberSerializers.FSTByteSerializer(), false);
		reg.putSerializer(Character.class, new FSTBigNumberSerializers.FSTCharSerializer(), false);
		reg.putSerializer(Short.class, new FSTBigNumberSerializers.FSTShortSerializer(), false);
		reg.putSerializer(Float.class, new FSTBigNumberSerializers.FSTFloatSerializer(), false);
		reg.putSerializer(Double.class, new FSTBigNumberSerializers.FSTDoubleSerializer(), false);
		reg.putSerializer(Date.class, new FSTDateSerializer(), false);
		reg.putSerializer(StringBuffer.class, new FSTStringBufferSerializer(), true);
		reg.putSerializer(StringBuilder.class, new FSTStringBuilderSerializer(), true);
		reg.putSerializer(EnumSet.class, new FSTEnumSetSerializer(), true);

		// for most cases don't register for subclasses as in many cases we'd like to fallback to JDK implementation
		// (e.g. TreeMap) in order to guarantee complete serialization
		reg.putSerializer(ArrayList.class, new FSTArrayListSerializer(), false);
		reg.putSerializer(Vector.class, new FSTCollectionSerializer(), true);
		reg.putSerializer(LinkedList.class, new FSTCollectionSerializer(), false); // subclass should register manually
		reg.putSerializer(HashSet.class, new FSTCollectionSerializer(), false); // subclass should register manually
		reg.putSerializer(HashMap.class, new FSTMapSerializer(), false); // subclass should register manually
		reg.putSerializer(LinkedHashMap.class, new FSTMapSerializer(), false); // subclass should register manually
		reg.putSerializer(Hashtable.class, new FSTMapSerializer(), true);
		reg.putSerializer(ConcurrentHashMap.class, new FSTMapSerializer(), true);
		// reg.putSerializer(FSTStruct.class, new FSTStructSerializer(), true);
		reg.putSerializer(Throwable.class, new FSTThrowableSerializer(), true);

		reg.putSerializer(BitSet.class, new FSTBitSetSerializer(), true);
		reg.putSerializer(Timestamp.class, new FSTTimestampSerializer(), true);

		// serializers for classes failing in fst JDK emulation (e.g. Android<=>JDK)
		reg.putSerializer(BigInteger.class, new FSTBigIntegerSerializer(), true);

		return conf;

	}

	/**
	 * Register int to object map factory.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param intToObjectMapFactory
	 *            the int to object map factory
	 * @date 30 sept. 2023
	 */
	void registerIntToObjectMapFactory(final FSTInt2ObjectMapFactory intToObjectMapFactory) {
		if (intToObjectMapFactory != null) { this.intToObjectMapFactory = intToObjectMapFactory; }
	}

	/**
	 * register a custom serializer for a given class or the class and all of its subclasses. Serializers must be
	 * configured identical on read/write side and should be set before actually making use of the Configuration.
	 *
	 * @param clazz
	 * @param ser
	 * @param alsoForAllSubclasses
	 */
	public void registerSerializer(final Class<?> clazz, final FSTObjectSerializer ser,
			final boolean alsoForAllSubclasses) {
		serializationInfoRegistry.getSerializerRegistry().putSerializer(clazz, ser, alsoForAllSubclasses);
	}

	/**
	 * Gets the instantiator.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param clazz
	 *            the clazz
	 * @return the instantiator
	 * @date 30 sept. 2023
	 */
	FSTClassInstantiator getInstantiator(final Class<?> clazz) {
		return instantiator;
	}

	/**
	 * Instantiates a new FST configuration.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param sharedFieldInfos
	 *            the shared field infos
	 * @date 30 sept. 2023
	 */
	FSTConfiguration() {
		this.fieldInfoCache = new ConcurrentHashMap<>();
	}

	/**
	 * reuse heavy weight objects. If a FSTStream is closed, objects are returned and can be reused by new stream
	 * instances. the objects are held in soft references, so there should be no memory issues. FIXME: point of
	 * contention !
	 *
	 * @param cached
	 */
	public void returnObject(final Object cached) {
		try {
			while (!cacheLock.compareAndSet(false, true)) {
				// empty
			}
			List<SoftReference> li = cachedObjects.get(cached.getClass());
			if (li == null) {
				li = new ArrayList<>();
				cachedObjects.put(cached.getClass(), li);
			}
			if (li.size() < 5) { li.add(new SoftReference<>(cached)); }
		} finally {
			cacheLock.set(false);
		}
	}

	/** The cache lock. */
	AtomicBoolean cacheLock = new AtomicBoolean(false);

	/**
	 * Gets the cached object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param cl
	 *            the cl
	 * @return the cached object
	 * @date 30 sept. 2023
	 */
	public Object getCachedObject(final Class<?> cl) {
		try {
			while (!cacheLock.compareAndSet(false, true)) {
				// empty
			}
			List<SoftReference> li = cachedObjects.get(cl);
			if (li == null) { return null; }
			for (int i = li.size() - 1; i >= 0; i--) {
				SoftReference<?> softReference = li.get(i);
				Object res = softReference.get();
				li.remove(i);
				if (res != null) { return res; }
			}
		} finally {
			cacheLock.set(false);
		}
		return null;
	}

	/**
	 * Checks if is force serializable.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is force serializable
	 * @date 30 sept. 2023
	 */
	public boolean isForceSerializable() { return true; }

	/**
	 * Checks if is share references.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is share references
	 * @date 30 sept. 2023
	 */
	public boolean isShareReferences() { return true; }

	/**
	 *
	 * Preregister a class (use at init time). This avoids having to write class names. Its a very simple and effective
	 * optimization (frequently > 2 times faster for small objects).
	 *
	 * Read and write side need to have classes preregistered in the exact same order.
	 *
	 * The list does not have to be complete. Just add your most frequently serialized classes here to get significant
	 * gains in speed and smaller serialized representation size.
	 *
	 */
	public void registerClass(final Class<?>... c) {
		for (Class<?> element : c) {
			classRegistry.registerClass(element, this);
			try {
				Class<?> ac = Class.forName("[L" + element.getName() + ";");
				classRegistry.registerClass(ac, this);
			} catch (ClassNotFoundException e) {
				// silent
			}
		}
	}

	/**
	 * Adds the default clazzes.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 30 sept. 2023
	 */
	void addDefaultClazzes() {
		classRegistry.registerClass(String.class, this);
		classRegistry.registerClass(Byte.class, this);
		classRegistry.registerClass(Short.class, this);
		classRegistry.registerClass(Integer.class, this);
		classRegistry.registerClass(Long.class, this);
		classRegistry.registerClass(Float.class, this);
		classRegistry.registerClass(Double.class, this);
		classRegistry.registerClass(BigDecimal.class, this);
		classRegistry.registerClass(BigInteger.class, this);
		classRegistry.registerClass(Character.class, this);
		classRegistry.registerClass(Boolean.class, this);
		classRegistry.registerClass(TreeMap.class, this);
		classRegistry.registerClass(HashMap.class, this);
		classRegistry.registerClass(ArrayList.class, this);
		classRegistry.registerClass(ConcurrentHashMap.class, this);
		classRegistry.registerClass(URL.class, this);
		classRegistry.registerClass(Date.class, this);
		classRegistry.registerClass(java.sql.Date.class, this);
		classRegistry.registerClass(SimpleDateFormat.class, this);
		classRegistry.registerClass(TreeSet.class, this);
		classRegistry.registerClass(LinkedList.class, this);
		classRegistry.registerClass(SimpleTimeZone.class, this);
		classRegistry.registerClass(GregorianCalendar.class, this);
		classRegistry.registerClass(Vector.class, this);
		classRegistry.registerClass(Hashtable.class, this);
		classRegistry.registerClass(BitSet.class, this);
		classRegistry.registerClass(Timestamp.class, this);
		classRegistry.registerClass(Locale.class, this);

		classRegistry.registerClass(StringBuffer.class, this);
		classRegistry.registerClass(StringBuilder.class, this);

		classRegistry.registerClass(Object.class, this);
		classRegistry.registerClass(Object[].class, this);
		classRegistry.registerClass(Object[][].class, this);
		classRegistry.registerClass(Object[][][].class, this);

		classRegistry.registerClass(byte[].class, this);
		classRegistry.registerClass(byte[][].class, this);

		classRegistry.registerClass(char[].class, this);
		classRegistry.registerClass(char[][].class, this);

		classRegistry.registerClass(short[].class, this);
		classRegistry.registerClass(short[][].class, this);

		classRegistry.registerClass(int[].class, this);
		classRegistry.registerClass(int[][].class, this);

		classRegistry.registerClass(float[].class, this);
		classRegistry.registerClass(float[][].class, this);

		classRegistry.registerClass(double[].class, this);
		classRegistry.registerClass(double[][].class, this);

		classRegistry.registerClass(long[].class, this);
		classRegistry.registerClass(long[][].class, this);

	}

	/**
	 * Gets the class registry.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the class registry
	 * @date 30 sept. 2023
	 */
	public FSTClazzNameRegistry getClassRegistry() { return classRegistry; }

	/**
	 * Gets the CL info registry.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the CL info registry
	 * @date 30 sept. 2023
	 */
	public FSTClazzInfoRegistry getCLInfoRegistry() { return serializationInfoRegistry; }

	/**
	 * Gets the class loader.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the class loader
	 * @date 30 sept. 2023
	 */
	public ClassLoader getClassLoader() { return classLoader; }

	/**
	 * Gets the class info.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param type
	 *            the type
	 * @return the class info
	 * @date 30 sept. 2023
	 */
	public FSTClazzInfo getClassInfo(final Class<?> type) {
		return serializationInfoRegistry.getCLInfo(type, this);
	}

	/**
	 * take the given array as input. the array is NOT copied.
	 *
	 * WARNING: the input streams takes over ownership and might overwrite content of this array in subsequent IO
	 * operations.
	 *
	 * @param arr
	 * @return
	 */
	public FSTObjectInput getObjectInput(final byte arr[]) {

		return getObjectInput(arr, arr.length);
	}

	/**
	 * take the given array as input. the array is NOT copied.
	 *
	 * WARNING: the input streams takes over ownership and might overwrite content of this array in subsequent IO
	 * operations.
	 *
	 * @param arr
	 * @param len
	 * @return
	 */
	public FSTObjectInput getObjectInput(final byte arr[], final int len) {
		FSTObjectInput fstObjectInput = getIn();
		try {
			fstObjectInput.resetForReuseUseArray(arr, len);
			return fstObjectInput;
		} catch (IOException e) {
			FSTUtil.<RuntimeException> rethrow(e);
		}
		return null;
	}

	/**
	 * Gets the in.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the in
	 * @date 30 sept. 2023
	 */
	@SuppressWarnings ("resource")
	protected FSTObjectInput getIn() {
		FSTObjectInput fstObjectInput = streamCoderFactory.getInput().get();
		if (fstObjectInput != null && fstObjectInput.isClosed()) { fstObjectInput = null; }
		if (fstObjectInput == null) {
			streamCoderFactory.getInput().set(new FSTObjectInput(this));
			return getIn();
		}
		fstObjectInput.conf = this;
		fstObjectInput.getCodec().setConf(this);
		return fstObjectInput;
	}

	/**
	 * Gets the out.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the out
	 * @date 30 sept. 2023
	 */
	@SuppressWarnings ("resource")
	protected FSTObjectOutput getOut() {
		FSTObjectOutput fstOut = streamCoderFactory.getOutput().get();
		if (fstOut == null || fstOut.closed) {
			streamCoderFactory.getOutput().set(new FSTObjectOutput(this));
			return getOut();
		}
		fstOut.conf = this;
		fstOut.getCodec().setConf(this);
		return fstOut;
	}

	/**
	 * utility for thread safety and reuse. Do not close the resulting stream. However you should close the given
	 * OutputStream 'out'
	 *
	 * @param out
	 *            - can be null (temp bytearrays stream is created then)
	 * @return
	 */
	public FSTObjectOutput getObjectOutput(final OutputStream out) {
		FSTObjectOutput fstObjectOutput = getOut();
		fstObjectOutput.resetForReUse(out);
		return fstObjectOutput;
	}

	/**
	 * @return a recycled outputstream reusing its last recently used byte[] buffer
	 */
	public FSTObjectOutput getObjectOutput() { return getObjectOutput((OutputStream) null); }

	/**
	 * ignores all serialization related interfaces (Serializable, Externalizable) and serializes all classes using the
	 * default scheme. Warning: this is a special mode of operation which fail serializing/deserializing many standard
	 * JDK classes.
	 *
	 * @param ignoreSerialInterfaces
	 */
	public void setStructMode(final boolean ignoreSerialInterfaces) {
		serializationInfoRegistry.setStructMode(ignoreSerialInterfaces);
	}

	/**
	 * special for structs
	 *
	 * @return
	 */
	public boolean isStructMode() { return serializationInfoRegistry.isStructMode(); }

	/**
	 * Gets the clazz info.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param rowClass
	 *            the row class
	 * @return the clazz info
	 * @date 30 sept. 2023
	 */
	public FSTClazzInfo getClazzInfo(final Class<?> rowClass) {
		return getCLInfoRegistry().getCLInfo(rowClass, this);
	}

	/**
	 * Checks if is cross platform.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return true, if is cross platform
	 * @date 30 sept. 2023
	 */
	public boolean isCrossPlatform() { return false; }

	/**
	 * A factory for creating StreamCoder objects.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 30 sept. 2023
	 */
	public interface StreamCoderFactory {

		/**
		 * Creates a new StreamCoder object.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @return the FST encoder
		 * @date 30 sept. 2023
		 */
		FSTEncoder createStreamEncoder();

		/**
		 * Creates a new StreamCoder object.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @return the FST decoder
		 * @date 30 sept. 2023
		 */
		FSTDecoder createStreamDecoder();

		/**
		 * Gets the input.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @return the input
		 * @date 30 sept. 2023
		 */
		ThreadLocal<FSTObjectInput> getInput();

		/**
		 * Gets the output.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @return the output
		 * @date 30 sept. 2023
		 */
		ThreadLocal<FSTObjectOutput> getOutput();
	}

	/**
	 * Creates the stream encoder.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the FST encoder
	 * @date 30 sept. 2023
	 */
	public FSTEncoder createStreamEncoder() {
		return streamCoderFactory.createStreamEncoder();
	}

	/**
	 * Creates the stream decoder.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the FST decoder
	 * @date 30 sept. 2023
	 */
	public FSTDecoder createStreamDecoder() {
		return streamCoderFactory.createStreamDecoder();
	}

	/**
	 * convenience
	 */
	public Object asObject(final byte b[]) {
		try {
			final byte[] input = ByteArrayZipper.unzip(b);
			return getObjectInput(input).readObject();
		} catch (Exception e) {
			DEBUG.LOG("unable to decode:" + new String(b, 0, 0, Math.min(b.length, 100)));
			FSTUtil.<RuntimeException> rethrow(e);
		}
		return null;
	}

	/**
	 * convenience. (object must be serializable)
	 */
	public byte[] asByteArray(final Object object) {
		FSTObjectOutput objectOutput = getObjectOutput();
		try {
			objectOutput.writeObject(object);
			return ByteArrayZipper.zip(objectOutput.getCopyOfWrittenBuffer());
		} catch (IOException e) {
			FSTUtil.<RuntimeException> rethrow(e);
		}
		return null;
	}

	@Override
	public String toString() {
		return "FSTConfiguration{" + "name='" + name + '\'' + '}';
	}

	/**
	 * A factory for creating FSTDefaultStreamCoder objects.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 30 sept. 2023
	 */
	protected static class FSTDefaultStreamCoderFactory implements FSTConfiguration.StreamCoderFactory {

		/** The fst configuration. */
		private final FSTConfiguration fstConfiguration;

		/**
		 * Instantiates a new FST default stream coder factory.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @param fstConfiguration
		 *            the fst configuration
		 * @date 30 sept. 2023
		 */
		public FSTDefaultStreamCoderFactory(final FSTConfiguration fstConfiguration) {
			this.fstConfiguration = fstConfiguration;
		}

		@Override
		public FSTEncoder createStreamEncoder() {
			return new FSTStreamEncoder(fstConfiguration);
		}

		@Override
		public FSTDecoder createStreamDecoder() {
			return new FSTStreamDecoder(fstConfiguration);
		}

		/** The input. */
		static ThreadLocal<FSTObjectInput> input = new ThreadLocal<>();

		/** The output. */
		static ThreadLocal<FSTObjectOutput> output = new ThreadLocal<>();

		@Override
		public ThreadLocal<FSTObjectInput> getInput() { return input; }

		@Override
		public ThreadLocal<FSTObjectOutput> getOutput() { return output; }

	}

}
