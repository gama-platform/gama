package gaml.compiler.ui.wizards.species;

import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.gaml.types.IType;
import gama.api.kernel.GamaMetaModel;

/** Isolated registry entries for standalone tests that do not boot the Eclipse platform. */
final class BuiltInParentFixture implements AutoCloseable {
	static final String PARENT = "wizard_builtin";
	static final String MODEL = "wizard_model";
	static final String EXPERIMENT = "wizard_experiment";
	static final String ATTRIBUTE = "builtin_capacity";
	private final Map<String, ISpeciesDescription> registry;
	private final Map<String, ISpeciesDescription> previous = new LinkedHashMap<>();

	@SuppressWarnings("unchecked")
	BuiltInParentFixture() throws ReflectiveOperationException {
		var instance = GamaMetaModel.class.getDeclaredField("INSTANCE");
		instance.setAccessible(true);
		var descriptions = GamaMetaModel.class.getDeclaredField("builtInSpeciesDescriptions");
		descriptions.setAccessible(true);
		registry = (Map<String, ISpeciesDescription>) descriptions.get(instance.get(null));
		add(PARENT, false, false);
		add(MODEL, true, false);
		add(EXPERIMENT, false, true);
		add(GamaMetaModel.INTERNAL_GLOBAL_SCHEDULER_SPECIES, false, false);
	}

	private void add(final String name, final boolean model, final boolean experiment) {
		IType<?> type = (IType<?>) Proxy.newProxyInstance(IType.class.getClassLoader(),
				new Class<?>[] { IType.class }, (proxy, method, args) -> {
					if ("toString".equals(method.getName())) return "int";
					throw new UnsupportedOperationException(method.getName());
				});
		IVariableDescription attribute = (IVariableDescription) Proxy.newProxyInstance(
				IVariableDescription.class.getClassLoader(), new Class<?>[] { IVariableDescription.class },
				(proxy, method, args) -> switch (method.getName()) {
					case "getName" -> ATTRIBUTE;
					case "getGamlType" -> type;
					case "getFacet" -> null;
					case "isUnmodifiable" -> true;
					default -> throw new UnsupportedOperationException(method.getName());
				});
		ISpeciesDescription description = (ISpeciesDescription) Proxy.newProxyInstance(
				ISpeciesDescription.class.getClassLoader(), new Class<?>[] { ISpeciesDescription.class },
				(proxy, method, args) -> switch (method.getName()) {
					case "getName" -> name;
					case "isModel" -> model;
					case "isExperiment" -> experiment;
					case "getAttributes" -> List.of(attribute);
					default -> throw new UnsupportedOperationException(method.getName());
				});
		previous.put(name, registry.put(name, description));
	}

	@Override
	public void close() {
		previous.forEach((name, description) -> {
			if (description == null) registry.remove(name);
			else registry.put(name, description);
		});
	}
}
