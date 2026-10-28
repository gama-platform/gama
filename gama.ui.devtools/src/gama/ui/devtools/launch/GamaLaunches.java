/*******************************************************************************************************
 *
 * GamaLaunches.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.launch;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.pde.launching.PDESourcePathProvider;

import gama.ui.devtools.GamaDevToolsActivator;
import gama.ui.devtools.util.GamaProjects;
import gama.ui.devtools.util.ResourceUtils;

/** Creates defaults once, and keeps explicit mappings to user-selected configurations. */
public final class GamaLaunches {

	public enum Flavor {
		GUI("GUI", "gama.product", "gama.ui.application"),
		HEADLESS("Headless", "gama.headless.product", "gama.headless"),
		TESTS("Tests", "gama.headless.product", "gama.headless");

		public final String label;
		final String productFile;
		final String bundle;

		Flavor(final String label, final String productFile, final String bundle) {
			this.label = label;
			this.productFile = productFile;
			this.bundle = bundle;
		}
	}

	private static final String GENERATED = GamaDevToolsActivator.PLUGIN_ID + ".generatedFlavor";

	private GamaLaunches() {}

	public static List<ILaunchConfiguration> configurations() throws CoreException {
		var manager = DebugPlugin.getDefault().getLaunchManager();
		return List.of(manager.getLaunchConfigurations(manager.getLaunchConfigurationType(
				IPDELauncherConstants.ECLIPSE_APPLICATION_LAUNCH_CONFIGURATION_TYPE)));
	}

	public static ILaunchConfiguration mapped(final Flavor flavor) throws CoreException {
		String memento = GamaDevToolsActivator.getInstance().getPreferenceStore().getString("launch." + flavor.name());
		if (!memento.isEmpty()) {
			ILaunchConfiguration config = DebugPlugin.getDefault().getLaunchManager().getLaunchConfiguration(memento);
			if (config.exists()) { return config; }
		}
		for (ILaunchConfiguration config : configurations()) {
			if (flavor.name().equals(config.getAttribute(GENERATED, ""))) { return config; }
		}
		return null;
	}

	public static void map(final Flavor flavor, final ILaunchConfiguration config) throws CoreException {
		GamaDevToolsActivator.getInstance().getPreferenceStore().setValue("launch." + flavor.name(), config.getMemento());
	}

	public static List<IVMInstall> jdks() {
		List<IVMInstall> result = new ArrayList<>();
		for (var type : JavaRuntime.getVMInstallTypes()) {
			for (IVMInstall vm : type.getVMInstalls()) {
				if (isJdk25(vm)) { result.add(vm); }
			}
		}
		return result;
	}

	private static boolean isJdk25(final IVMInstall vm) {
		if (!(vm instanceof IVMInstall2 versioned)) { return false; }
		String version = versioned.getJavaVersion();
		return version != null && version.matches("25(?:[.+\\-].*)?")
				&& (new java.io.File(vm.getInstallLocation(), "bin/javac").isFile()
						|| new java.io.File(vm.getInstallLocation(), "bin/javac.exe").isFile());
	}

	public static ILaunchConfiguration create(final Flavor flavor, final IVMInstall vm) throws CoreException {
		if (!isJdk25(vm)) { throw error("Choose a JDK 25 under Preferences > Java > Installed JREs."); }
		IFile file = GamaProjects.root().getFile(IPath.fromPortableString("/gama.product/" + flavor.productFile));
		if (!file.isAccessible()) { throw error("Import and open gama.product, including " + flavor.productFile + "."); }
		ProductLaunchSpec spec;
		try (InputStream input = file.getContents()) {
			spec = ProductLaunchSpec.read(input, Platform.getOS());
		} catch (CoreException e) {
			throw e;
		} catch (java.io.IOException | javax.xml.parsers.ParserConfigurationException | org.xml.sax.SAXException
				| IllegalArgumentException e) {
			throw new CoreException(ResourceUtils.error("Cannot read " + file.getFullPath(), e));
		}
		requireBundle(flavor.bundle);
		requireBundle("gama.core");
		requireBundle("gama.api");
		for (String id : spec.features()) {
			if (!GamaProjects.root().getProject(id).getFile("feature.xml").isAccessible()) {
				throw error("Import and open the feature project " + id + ", required by " + file.getName() + ".");
			}
		}
		var manager = DebugPlugin.getDefault().getLaunchManager();
		var type = manager.getLaunchConfigurationType(IPDELauncherConstants.ECLIPSE_APPLICATION_LAUNCH_CONFIGURATION_TYPE);
		// Restoring defaults creates a fresh configuration and leaves the old one available.
		ILaunchConfigurationWorkingCopy copy = type.newInstance(null,
				manager.generateLaunchConfigurationName("GAMA - " + flavor.label));
		copy.setAttribute(GENERATED, flavor.name());
		copy.setAttribute(IPDELauncherConstants.USE_PRODUCT, true);
		copy.setAttribute(IPDELauncherConstants.PRODUCT, spec.product());
		copy.setAttribute(IPDELauncherConstants.APPLICATION, spec.application());
		copy.setAttribute(IPDELauncherConstants.PRODUCT_FILE, file.getFullPath().toPortableString());
		copy.setAttribute(IPDELauncherConstants.USE_DEFAULT, false);
		copy.setAttribute(IPDELauncherConstants.AUTOMATIC_ADD, false);
		copy.setAttribute(IPDELauncherConstants.USE_CUSTOM_FEATURES, true);
		Set<String> features = new LinkedHashSet<>();
		for (String id : spec.features()) { features.add(id + ":" + IPDELauncherConstants.LOCATION_DEFAULT); }
		copy.setAttribute(IPDELauncherConstants.SELECTED_FEATURES, features);
		copy.setAttribute(IPDELauncherConstants.ROOT_FEATURES, new LinkedHashSet<>(spec.features()));
		copy.setAttribute(IPDELauncherConstants.AUTOMATIC_INCLUDE_REQUIREMENTS, true);
		copy.setAttribute(IPDELauncherConstants.AUTOMATIC_VALIDATE, true);
		copy.setAttribute(IPDELauncherConstants.INCLUDE_OPTIONAL, false);
		Set<String> startups = new LinkedHashSet<>();
		for (var startup : spec.startups()) {
			var model = PluginRegistry.findModel(startup.id());
			if (model == null) {
				throw error("The product startup bundle " + startup.id()
						+ " is unavailable. Import its project or resolve the GAMA target platform.");
			}
			startups.add(String.join(":", startup.id(), model.getPluginBase().getVersion(), "default", "true",
					startup.level().isBlank() || startup.level().equals("0") ? "default" : startup.level(),
					Boolean.toString(startup.autoStart())));
		}
		copy.setAttribute(IPDELauncherConstants.ADDITIONAL_PLUGINS, startups);
		String runtime = "${workspace_loc}/../gama-runtime-" + flavor.name().toLowerCase(java.util.Locale.ROOT);
		copy.setAttribute(IPDELauncherConstants.LOCATION, runtime);
		copy.setAttribute(IPDELauncherConstants.DOCLEAR, false);
		copy.setAttribute(IPDELauncherConstants.ASKCLEAR, false);
		copy.setAttribute(IPDELauncherConstants.CONFIG_USE_DEFAULT_AREA, true);
		copy.setAttribute(IPDELauncherConstants.CONFIG_GENERATE_DEFAULT, true);
		copy.setAttribute(IPDELauncherConstants.CONFIG_CLEAR_AREA, false);
		copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, runtime);
		copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH,
				JavaRuntime.newJREContainerPath(vm).toPortableString());
		copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER, PDESourcePathProvider.ID);
		String vmArguments = spec.vmArguments();
		for (var property : spec.properties().entrySet()) {
			vmArguments += "\n" + DebugPlugin.renderArguments(
					new String[] { "-D" + property.getKey() + "=" + property.getValue() }, null);
		}
		copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArguments);
		List<String> args = new ArrayList<>(Arrays.asList(DebugPlugin.parseArguments(spec.programArguments())));
		for (int i = 0; i < args.size();) {
			if (args.get(i).equals("-data")) {
				args.remove(i);
				if (i < args.size()) { args.remove(i); }
			} else { i++; }
		}
		String program = DebugPlugin.renderArguments(args.toArray(String[]::new), null);
		program += " -consoleLog";
		if (flavor != Flavor.GUI) { program += " -nosplash"; }
		if (flavor == Flavor.TESTS) { program += " -test"; }
		copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, program);
		copy.setMappedResources(new org.eclipse.core.resources.IResource[] { file });
		ILaunchConfiguration config = copy.doSave();
		map(flavor, config);
		return config;
	}

	public static void validate(final ILaunchConfiguration config, final Flavor flavor) throws CoreException {
		requireBundle(flavor.bundle);
		requireBundle("gama.core");
		IVMInstall vm = JavaRuntime.computeVMInstall(config);
		if (vm == null || !isJdk25(vm)) {
			throw error("The launch must use a JDK 25 (the products enable Java preview features). "
					+ "Edit its JRE tab in GAMA > Run... > Configure...");
		}
	}

	private static void requireBundle(final String id) throws CoreException {
		var model = PluginRegistry.findModel(id);
		if (model == null || model.getUnderlyingResource() == null
				|| !model.getUnderlyingResource().getProject().isOpen()) {
			throw error("Import and open the workspace plugin " + id + ", and resolve the GAMA target platform.");
		}
	}

	private static CoreException error(final String message) {
		return new CoreException(ResourceUtils.error(message, null));
	}
}
