package gaml.compiler.ui.wizards.species;

import java.lang.reflect.Proxy;
import java.util.Map;

import gama.api.additions.registries.GamaSkillRegistry;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.gaml.GAML;
import gama.api.gaml.types.IType;
import gama.api.types.map.GamaMapFactory;
import gama.extension.traffic.driving.DrivingSkill;
import gama.gaml.skills.MovingSkill;

/** Uses driving's actual class hierarchy without booting the extension registry. */
final class InheritedSkillFixture implements AutoCloseable {
	static final String SKILL = "wizard_driving";
	private final Map<String, ISkillDescription> skills;
	private final ISkillDescription previous;
	private final IVariableDescription speed = variable("speed", true);
	private final IVariableDescription drivingSpeed = variable("speed", false);
	private final IVariableDescription acceleration = variable("acceleration", false);
	private final IVariableDescription direct = variable("direct_attribute", false);
	private final String previousClassName;
	private final Map<Class<?>, String> classNames;

	@SuppressWarnings("unchecked")
	InheritedSkillFixture() throws Exception {
		var registryField = GamaSkillRegistry.class.getDeclaredField("skills");
		registryField.setAccessible(true);
		skills = (Map<String, ISkillDescription>) registryField.get(GamaSkillRegistry.INSTANCE);
		var namesField = GamaSkillRegistry.class.getDeclaredField("classSkillNames");
		namesField.setAccessible(true);
		classNames = (Map<Class<?>, String>) namesField.get(GamaSkillRegistry.INSTANCE);
		previousClassName = classNames.get(DrivingSkill.NewDrivingSkill.class);
		previous = skills.get(SKILL);
		var own = GamaMapFactory.<String, IVariableDescription>createUnordered();
		own.put(direct.getName(), direct);
		ISkillDescription description = (ISkillDescription) Proxy.newProxyInstance(
				ISkillDescription.class.getClassLoader(), new Class<?>[] { ISkillDescription.class },
				(proxy, method, args) -> switch (method.getName()) {
					case "getName" -> SKILL;
					case "getJavaBase" -> DrivingSkill.NewDrivingSkill.class;
					case "getOwnAttributes" -> own;
					default -> throw new UnsupportedOperationException(method.getName());
				});
		GamaSkillRegistry.INSTANCE.register(description, DrivingSkill.NewDrivingSkill.class);
		GAML.addAddition(MovingSkill.class, speed);
		GAML.addAddition(DrivingSkill.class, drivingSpeed);
		GAML.addAddition(DrivingSkill.class, acceleration);
	}

	private static IVariableDescription variable(final String name, final boolean unmodifiable) {
		IType<?> type = (IType<?>) Proxy.newProxyInstance(IType.class.getClassLoader(),
				new Class<?>[] { IType.class }, (proxy, method, args) -> {
					if ("toString".equals(method.getName())) return "float";
					throw new UnsupportedOperationException(method.getName());
				});
		return (IVariableDescription) Proxy.newProxyInstance(IVariableDescription.class.getClassLoader(),
				new Class<?>[] { IVariableDescription.class }, (proxy, method, args) -> switch (method.getName()) {
					case "getName" -> name;
					case "getGamlType" -> type;
					case "getFacet" -> null;
					case "isUnmodifiable" -> unmodifiable;
					case "hashCode" -> System.identityHashCode(proxy);
					case "equals" -> proxy == args[0];
					default -> throw new UnsupportedOperationException(method.getName());
				});
	}

	@Override
	public void close() {
		if (previous == null) skills.remove(SKILL);
		else skills.put(SKILL, previous);
		if (previousClassName == null) classNames.remove(DrivingSkill.NewDrivingSkill.class);
		else classNames.put(DrivingSkill.NewDrivingSkill.class, previousClassName);
		GAML.getAdditions(MovingSkill.class).remove(speed);
		GAML.getAdditions(DrivingSkill.class).remove(drivingSpeed);
		GAML.getAdditions(DrivingSkill.class).remove(acceleration);
	}
}
