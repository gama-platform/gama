package gama.extension.serialize.fory;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;

import gama.api.additions.GamaClassLoader;
import gama.api.kernel.serialization.BinarySerialisation;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.date.GamaDateFactory;
import gama.api.types.geometry.GamaPointFactory;
import gama.extension.serialize.SerializeActivator;

/** Standalone regression checks for the sole binary backend and its activation. */
public final class ForySerializationTest {

	public static final class Value {
		public String label;
		public Object first;
		public Object second;
	}

	public static void main(final String[] args) {
		final ClassLoader loader = ForySerializationTest.class.getClassLoader();
		final Bundle bundle = (Bundle) Proxy.newProxyInstance(Bundle.class.getClassLoader(),
				new Class<?>[] { Bundle.class }, (proxy, method, arguments) -> switch (method.getName()) {
					case "getSymbolicName" -> "fory-serialization-test";
					case "loadClass" -> loader.loadClass((String) arguments[0]);
					default -> throw new UnsupportedOperationException(method.getName());
				});
		GamaClassLoader.getInstance().addBundle(bundle);
		new SerializeActivator().initialize(null);
		check(BinarySerialisation.createNewBinarySerializer() instanceof ForyBinarySerialiser,
				"Activation must install Fory without selecting a backend");

		final List<Object> values = new ArrayList<>();
		values.add(null);
		values.add("recorded value");
		values.add(List.of(1, 2, 3));
		values.add(new HashMap<>(Map.of("amount", 42, "label", "changed")));
		values.add(GamaPointFactory.create(1, 2, 3));
		values.add(GamaColorFactory.createWithRGBA(12, 34, 56, 78));
		values.add(GamaDateFactory.createFromTemporal(LocalDateTime.of(2026, 10, 26, 12, 34)));
		for (final Object value : values) {
			final byte[] bytes = BinarySerialisation.saveToBytes(null, value);
			check(java.util.Objects.equals(value, BinarySerialisation.createFromBytes(null, bytes)),
					"Binary round trip failed for " + value);
			final String string = BinarySerialisation.saveToString(null, value);
			check(java.util.Objects.equals(value, BinarySerialisation.createFromString(null, string)),
					"Binary string round trip failed for " + value);
		}

		final Value value = new Value();
		value.label = "recorded value";
		value.first = value;
		value.second = value;
		for (int i = 0; i < 20; i++) {
			final Value restored = (Value) BinarySerialisation.createFromBytes(null,
					BinarySerialisation.saveToBytes(null, value));
			check(value.label.equals(restored.label), "String fields must survive recording");
			check(restored.first == restored && restored.second == restored,
					"Shared and cyclic references must survive recording");
		}
	}

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}
}
