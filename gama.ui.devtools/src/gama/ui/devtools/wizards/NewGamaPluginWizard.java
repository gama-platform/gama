/*******************************************************************************************************
 *
 * NewGamaPluginWizard.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import gama.ui.devtools.GamaDevToolsActivator;
import gama.ui.devtools.preferences.GamaDevToolsPreferencePage;

/**
 * A multi-page wizard that creates a complete GAMA plugin project from scratch.
 *
 * <p>
 * The wizard collects the following information:
 * </p>
 * <ul>
 * <li>Plugin symbolic name (e.g. {@code gama.extension.myextension})</li>
 * <li>Human-readable plugin name</li>
 * <li>Vendor name (defaults to the preference stored in {@link GamaDevToolsPreferencePage#PREF_DEFAULT_VENDOR})</li>
 * <li>Base Java package (defaults to {@link GamaDevToolsPreferencePage#PREF_DEFAULT_PACKAGE})</li>
 * <li>Short description</li>
 * </ul>
 *
 * <p>
 * On completion, the wizard creates:
 * </p>
 * <ul>
 * <li>An Eclipse PDE plugin project with {@code PluginNature} and {@code JavaNature}</li>
 * <li>{@code META-INF/MANIFEST.MF} wired to {@code gama.annotations}, {@code gama.core}, and {@code gama.dev}</li>
 * <li>{@code plugin.xml}, {@code build.properties}, {@code pom.xml}</li>
 * <li>Source folders: {@code src/}, {@code models/}, {@code tests/}, {@code tutorials/}, {@code recipes/}</li>
 * <li>{@code .factorypath} pointing to {@code gama.processor.jar}</li>
 * <li>A sibling feature project ({@code <pluginId>.feature}) with a minimal {@code feature.xml}</li>
 * </ul>
 *
 * @author GAMA Development Team
 * @since 2026
 */
public class NewGamaPluginWizard extends Wizard implements INewWizard {

	/** The single wizard page that collects plugin metadata. */
	private PluginInfoPage infoPage;

	/**
	 * Constructs the wizard with a descriptive window title.
	 */
	public NewGamaPluginWizard() {
		setWindowTitle("New GAMA Plugin");
		setNeedsProgressMonitor(true);
	}

	/** {@inheritDoc} */
	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection selection) {}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Adds {@link PluginInfoPage} as the sole page of this wizard.
	 * </p>
	 */
	@Override
	public void addPages() {
		infoPage = new PluginInfoPage();
		addPage(infoPage);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Runs a {@link WorkspaceModifyOperation} that creates the plugin project and the companion feature project.
	 * Errors are reported as an error dialog.
	 * </p>
	 *
	 * @return {@code true} if the projects were created successfully, {@code false} otherwise
	 */
	@Override
	public boolean performFinish() {
		final String pluginId = infoPage.getPluginId();
		final String pluginName = infoPage.getPluginName();
		final String vendor = infoPage.getVendor();
		final String basePackage = infoPage.getBasePackage();
		final String description = infoPage.getDescription();
		final String processorJar = GamaDevToolsActivator.getInstance().getPreferenceStore()
				.getString(GamaDevToolsPreferencePage.PREF_PROCESSOR_JAR_PATH);

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			@Override
			protected void execute(final IProgressMonitor monitor)
					throws CoreException, InvocationTargetException, InterruptedException {
				SubMonitor sub = SubMonitor.convert(monitor, "Creating GAMA plugin " + pluginId, 20);
				createPluginProject(pluginId, pluginName, vendor, basePackage, description, processorJar, sub.split(15));
				createFeatureProject(pluginId, pluginName, vendor, sub.split(5));
			}
		};
		try {
			getContainer().run(true, true, op);
		} catch (InvocationTargetException | InterruptedException e) {
			org.eclipse.jface.dialogs.MessageDialog.openError(getShell(), "Error creating GAMA plugin",
					e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
			return false;
		}
		return true;
	}

	// -------------------------------------------------------------------------
	// Internal helpers
	// -------------------------------------------------------------------------

	/**
	 * Creates the main plugin project with all required files and folders.
	 *
	 * @param pluginId
	 *            the OSGi symbolic name of the plugin (e.g. {@code gama.extension.myextension})
	 * @param pluginName
	 *            the human-readable bundle name
	 * @param vendor
	 *            the bundle vendor string
	 * @param basePackage
	 *            the root Java package (dot-separated)
	 * @param description
	 *            a short description of the plugin
	 * @param processorJar
	 *            the path to {@code gama.processor.jar} for the {@code .factorypath}
	 * @param monitor
	 *            the progress monitor
	 * @throws CoreException
	 *             if any resource creation fails
	 */
	private void createPluginProject(final String pluginId, final String pluginName, final String vendor,
			final String basePackage, final String description, final String processorJar,
			final IProgressMonitor monitor) throws CoreException {
		SubMonitor sub = SubMonitor.convert(monitor, 15);
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProject project = ws.getRoot().getProject(pluginId);

		IProjectDescription desc = ws.newProjectDescription(pluginId);
		desc.setNatureIds(new String[] { "org.eclipse.pde.PluginNature", "org.eclipse.jdt.core.javanature" });
		project.create(desc, sub.split(1));
		project.open(sub.split(1));

		// Source folders
		for (String folder : new String[] { "src", "models", "tests", "tutorials", "recipes" }) {
			createFolder(project, folder, sub.split(1));
		}
		// Source package skeleton
		String packagePath = "src/" + basePackage.replace('.', '/');
		createFolder(project, packagePath, sub.split(1));

		writeFile(project, "META-INF/MANIFEST.MF", buildManifest(pluginId, pluginName, vendor, basePackage),
				sub.split(1));
		writeFile(project, "plugin.xml", buildPluginXml(), sub.split(1));
		writeFile(project, "build.properties", buildBuildProperties(), sub.split(1));
		writeFile(project, "pom.xml", buildPomXml(pluginId), sub.split(1));
		writeFile(project, ".classpath", buildClasspath(), sub.split(1));
		writeFile(project, ".project", buildDotProject(pluginId), sub.split(1));
		if (processorJar != null && !processorJar.isBlank()) {
			writeFile(project, ".factorypath", buildFactoryPath(processorJar), sub.split(1));
		}
		writeFile(project, "about.ini", buildAboutIni(pluginName, description), sub.split(1));
	}

	/**
	 * Creates the companion feature project for the given plugin.
	 *
	 * @param pluginId
	 *            the plugin's OSGi symbolic name; the feature id will be {@code <pluginId>.feature}
	 * @param pluginName
	 *            the human-readable name used in the feature label
	 * @param vendor
	 *            the provider name for the feature
	 * @param monitor
	 *            the progress monitor
	 * @throws CoreException
	 *             if any resource creation fails
	 */
	private void createFeatureProject(final String pluginId, final String pluginName, final String vendor,
			final IProgressMonitor monitor) throws CoreException {
		SubMonitor sub = SubMonitor.convert(monitor, 5);
		String featureId = pluginId + ".feature";
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProject featureProject = ws.getRoot().getProject(featureId);

		IProjectDescription desc = ws.newProjectDescription(featureId);
		desc.setNatureIds(new String[] { "org.eclipse.pde.FeatureNature" });
		featureProject.create(desc, sub.split(1));
		featureProject.open(sub.split(1));

		writeFile(featureProject, "feature.xml", buildFeatureXml(featureId, pluginName, vendor, pluginId),
				sub.split(1));
		writeFile(featureProject, "build.properties", "bin.includes = feature.xml\n", sub.split(1));
		writeFile(featureProject, "pom.xml", buildFeaturePomXml(featureId), sub.split(1));
	}

	// -------------------------------------------------------------------------
	// Template builders
	// -------------------------------------------------------------------------

	/**
	 * Builds the content of {@code META-INF/MANIFEST.MF} for the new plugin.
	 *
	 * @param pluginId
	 *            the OSGi symbolic name
	 * @param pluginName
	 *            the human-readable bundle name
	 * @param vendor
	 *            the bundle vendor
	 * @param basePackage
	 *            the root Java package to export
	 * @return the MANIFEST.MF content as a string
	 */
	private String buildManifest(final String pluginId, final String pluginName, final String vendor,
			final String basePackage) {
		return "Manifest-Version: 1.0\n"
				+ "Bundle-ManifestVersion: 2\n"
				+ "Bundle-Name: " + pluginName + "\n"
				+ "Bundle-SymbolicName: " + pluginId + ";singleton:=true\n"
				+ "Bundle-Version: 0.0.0.qualifier\n"
				+ "Bundle-Vendor: " + vendor + "\n"
				+ "Bundle-RequiredExecutionEnvironment: JavaSE-25\n"
				+ "Bundle-ActivationPolicy: lazy\n"
				+ "Automatic-Module-Name: " + pluginId + "\n"
				+ "Require-Bundle: gama.core,\n"
				+ " gama.annotations,\n"
				+ " gama.dev\n"
				+ "Export-Package: " + basePackage + "\n";
	}

	/**
	 * Builds a minimal {@code plugin.xml} skeleton.
	 *
	 * @return the plugin.xml content
	 */
	private String buildPluginXml() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<?eclipse version=\"3.4\"?>\n"
				+ "<plugin>\n\n"
				+ "   <!-- Add your extension point contributions here -->\n\n"
				+ "</plugin>\n";
	}

	/**
	 * Builds the {@code build.properties} content.
	 *
	 * @return the build.properties content
	 */
	private String buildBuildProperties() {
		return "source.. = src/\n"
				+ "output.. = bin/\n"
				+ "jre.compilation.profile = JavaSE-25\n"
				+ "bin.includes = META-INF/,\\\n"
				+ "               .,\\\n"
				+ "               plugin.xml,\\\n"
				+ "               models/,\\\n"
				+ "               tests/,\\\n"
				+ "               tutorials/,\\\n"
				+ "               recipes/\n"
				+ "javacDefaultEncoding.. = UTF-8\n";
	}

	/**
	 * Builds the {@code pom.xml} content for a plugin project.
	 *
	 * @param artifactId
	 *            the Maven artifact id (same as the plugin symbolic name)
	 * @return the pom.xml content
	 */
	private String buildPomXml(final String artifactId) {
		return "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
				+ "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 "
				+ "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
				+ "  <modelVersion>4.0.0</modelVersion>\n"
				+ "  <parent>\n"
				+ "    <groupId>org.gama</groupId>\n"
				+ "    <artifactId>gama.parent</artifactId>\n"
				+ "    <version>0.0.0-SNAPSHOT</version>\n"
				+ "    <relativePath>../gama.parent/</relativePath>\n"
				+ "  </parent>\n"
				+ "  <artifactId>" + artifactId + "</artifactId>\n"
				+ "  <packaging>eclipse-plugin</packaging>\n"
				+ "</project>\n";
	}

	/**
	 * Builds the {@code pom.xml} content for the companion feature project.
	 *
	 * @param artifactId
	 *            the Maven artifact id of the feature
	 * @return the pom.xml content
	 */
	private String buildFeaturePomXml(final String artifactId) {
		return "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
				+ "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 "
				+ "http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
				+ "  <modelVersion>4.0.0</modelVersion>\n"
				+ "  <parent>\n"
				+ "    <groupId>org.gama</groupId>\n"
				+ "    <artifactId>gama.parent</artifactId>\n"
				+ "    <version>0.0.0-SNAPSHOT</version>\n"
				+ "    <relativePath>../gama.parent/</relativePath>\n"
				+ "  </parent>\n"
				+ "  <artifactId>" + artifactId + "</artifactId>\n"
				+ "  <packaging>eclipse-feature</packaging>\n"
				+ "</project>\n";
	}

	/**
	 * Builds the {@code .classpath} content for the new plugin.
	 *
	 * @return the .classpath content
	 */
	private String buildClasspath() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<classpath>\n"
				+ "\t<classpathentry kind=\"src\" path=\"src\"/>\n"
				+ "\t<classpathentry kind=\"con\" "
				+ "path=\"org.eclipse.jdt.launching.JRE_CONTAINER/"
				+ "org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-25\">\n"
				+ "\t\t<attributes>\n"
				+ "\t\t\t<attribute name=\"module\" value=\"true\"/>\n"
				+ "\t\t</attributes>\n"
				+ "\t</classpathentry>\n"
				+ "\t<classpathentry kind=\"con\" path=\"org.eclipse.pde.core.requiredPlugins\"/>\n"
				+ "\t<classpathentry kind=\"output\" path=\"bin\"/>\n"
				+ "</classpath>\n";
	}

	/**
	 * Builds the {@code .project} descriptor for the new plugin.
	 *
	 * @param pluginId
	 *            the project name
	 * @return the .project XML content
	 */
	private String buildDotProject(final String pluginId) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<projectDescription>\n"
				+ "\t<name>" + pluginId + "</name>\n"
				+ "\t<comment></comment>\n"
				+ "\t<projects>\n"
				+ "\t</projects>\n"
				+ "\t<buildSpec>\n"
				+ "\t\t<buildCommand>\n"
				+ "\t\t\t<name>org.eclipse.jdt.core.javabuilder</name>\n"
				+ "\t\t\t<arguments/>\n"
				+ "\t\t</buildCommand>\n"
				+ "\t\t<buildCommand>\n"
				+ "\t\t\t<name>org.eclipse.pde.ManifestBuilder</name>\n"
				+ "\t\t\t<arguments/>\n"
				+ "\t\t</buildCommand>\n"
				+ "\t\t<buildCommand>\n"
				+ "\t\t\t<name>org.eclipse.pde.SchemaBuilder</name>\n"
				+ "\t\t\t<arguments/>\n"
				+ "\t\t</buildCommand>\n"
				+ "\t</buildSpec>\n"
				+ "\t<natures>\n"
				+ "\t\t<nature>org.eclipse.pde.PluginNature</nature>\n"
				+ "\t\t<nature>org.eclipse.jdt.core.javanature</nature>\n"
				+ "\t</natures>\n"
				+ "</projectDescription>\n";
	}

	/**
	 * Builds the {@code .factorypath} content pointing to {@code gama.processor.jar}.
	 *
	 * @param processorJarPath
	 *            the path to {@code gama.processor.jar} (workspace-relative or absolute)
	 * @return the .factorypath XML content
	 */
	private String buildFactoryPath(final String processorJarPath) {
		return "<factorypath>\n"
				+ "   <factorypathentry kind=\"EXTJAR\" id=\"" + processorJarPath + "\""
				+ " enabled=\"true\" runInBatchMode=\"false\"/>\n"
				+ "</factorypath>\n";
	}

	/**
	 * Builds the {@code feature.xml} content for the companion feature project.
	 *
	 * @param featureId
	 *            the feature symbolic id
	 * @param pluginName
	 *            the human-readable label for the feature
	 * @param vendor
	 *            the feature provider name
	 * @param pluginId
	 *            the id of the plugin included in this feature
	 * @return the feature.xml content
	 */
	private String buildFeatureXml(final String featureId, final String pluginName, final String vendor,
			final String pluginId) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<feature\n"
				+ "      id=\"" + featureId + "\"\n"
				+ "      label=\"" + pluginName + "\"\n"
				+ "      version=\"0.0.0.qualifier\"\n"
				+ "      provider-name=\"" + vendor + "\">\n\n"
				+ "   <description url=\"http://gama-platform.org\">\n"
				+ "      [Enter feature description here.]\n"
				+ "   </description>\n\n"
				+ "   <copyright>\n"
				+ "      [Enter copyright description here.]\n"
				+ "   </copyright>\n\n"
				+ "   <license url=\"http://www.example.com/license\">\n"
				+ "      [Enter license description here.]\n"
				+ "   </license>\n\n"
				+ "   <url>\n"
				+ "      <update label=\"GAMA 0.0.0 Update Site\" url=\"https://updates.gama-platform.org/0.0.0\"/>\n"
				+ "   </url>\n\n"
				+ "   <plugin\n"
				+ "         id=\"" + pluginId + "\"\n"
				+ "         version=\"0.0.0\"/>\n\n"
				+ "</feature>\n";
	}

	/**
	 * Builds the {@code about.ini} content.
	 *
	 * @param pluginName
	 *            the bundle name shown in the about dialog
	 * @param description
	 *            the short description of the plugin
	 * @return the about.ini content
	 */
	private String buildAboutIni(final String pluginName, final String description) {
		return "aboutText=" + pluginName + "\n"
				+ description + "\n\n"
				+ "Visit https://github.com/gama-platform/gama for more information.\n";
	}

	// -------------------------------------------------------------------------
	// Resource helpers
	// -------------------------------------------------------------------------

	/**
	 * Creates a folder (and all intermediate folders) inside the given project.
	 *
	 * @param project
	 *            the parent project
	 * @param path
	 *            the forward-slash-separated folder path relative to the project root
	 * @param monitor
	 *            the progress monitor
	 * @throws CoreException
	 *             if folder creation fails
	 */
	private void createFolder(final IProject project, final String path, final IProgressMonitor monitor)
			throws CoreException {
		String[] parts = path.split("/");
		IFolder current = null;
		for (String part : parts) {
			current = (current == null) ? project.getFolder(part) : current.getFolder(part);
			if (!current.exists()) { current.create(true, true, monitor); }
		}
	}

	/**
	 * Writes a text file with UTF-8 encoding to the given project, creating intermediate folders as needed.
	 *
	 * @param project
	 *            the target project
	 * @param relativePath
	 *            the path of the file relative to the project root (forward slashes)
	 * @param content
	 *            the textual content to write
	 * @param monitor
	 *            the progress monitor
	 * @throws CoreException
	 *             if file creation fails
	 */
	private void writeFile(final IProject project, final String relativePath, final String content,
			final IProgressMonitor monitor) throws CoreException {
		// Ensure parent directories exist
		int lastSlash = relativePath.lastIndexOf('/');
		if (lastSlash > 0) { createFolder(project, relativePath.substring(0, lastSlash), monitor); }
		IFile file = project.getFile(relativePath);
		InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
		if (file.exists()) {
			file.setContents(stream, true, false, monitor);
		} else {
			file.create(stream, true, monitor);
		}
	}

	// =========================================================================
	// Inner page class
	// =========================================================================

	/**
	 * The single wizard page for the New GAMA Plugin wizard. Collects plugin id, name, vendor, base package,
	 * and description via simple text fields.
	 *
	 * @author GAMA Development Team
	 * @since 2026
	 */
	private static class PluginInfoPage extends WizardPage {

		/** Text field for the OSGi symbolic name / project name. */
		private Text pluginIdText;

		/** Text field for the human-readable bundle name. */
		private Text pluginNameText;

		/** Text field for the vendor / provider name. */
		private Text vendorText;

		/** Text field for the root Java package. */
		private Text basePackageText;

		/** Text field for the short description. */
		private Text descriptionText;

		/**
		 * Constructs the page with its title and description.
		 */
		PluginInfoPage() {
			super("pluginInfoPage");
			setTitle("New GAMA Plugin");
			setDescription("Enter the details for your new GAMA plugin project.");
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Builds a two-column grid layout with label/text pairs for each field.
		 * Default values are read from the {@link GamaDevToolsPreferencePage} preference store.
		 * </p>
		 *
		 * @param parent
		 *            the parent composite provided by the wizard framework
		 */
		@Override
		public void createControl(final Composite parent) {
			Composite container = new Composite(parent, SWT.NONE);
			container.setLayout(new GridLayout(2, false));

			String defaultVendor = GamaDevToolsActivator.getInstance().getPreferenceStore()
					.getString(GamaDevToolsPreferencePage.PREF_DEFAULT_VENDOR);
			if (defaultVendor == null || defaultVendor.isBlank()) { defaultVendor = "UMMISCO"; }
			String defaultPackage = GamaDevToolsActivator.getInstance().getPreferenceStore()
					.getString(GamaDevToolsPreferencePage.PREF_DEFAULT_PACKAGE);
			if (defaultPackage == null || defaultPackage.isBlank()) { defaultPackage = "gama"; }

			pluginIdText = createLabeledText(container, "Plugin symbolic name:", "gama.extension.myplugin");
			pluginNameText = createLabeledText(container, "Plugin name:", "My GAMA Extension");
			vendorText = createLabeledText(container, "Vendor:", defaultVendor);
			basePackageText = createLabeledText(container, "Base Java package:", defaultPackage);
			descriptionText = createLabeledText(container, "Description:", "");

			setControl(container);
			setPageComplete(true);
		}

		/**
		 * Creates a label and a single-line text field in the given two-column grid composite.
		 *
		 * @param parent
		 *            the parent two-column grid composite
		 * @param labelText
		 *            the label to display
		 * @param defaultValue
		 *            the initial value of the text field
		 * @return the created {@link Text} widget
		 */
		private Text createLabeledText(final Composite parent, final String labelText, final String defaultValue) {
			new Label(parent, SWT.NONE).setText(labelText);
			Text text = new Text(parent, SWT.BORDER);
			text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			text.setText(defaultValue);
			return text;
		}

		/**
		 * Returns the plugin symbolic name entered by the user.
		 *
		 * @return the OSGi symbolic name (e.g. {@code gama.extension.myplugin})
		 */
		public String getPluginId() { return pluginIdText.getText().trim(); }

		/**
		 * Returns the human-readable plugin name entered by the user.
		 *
		 * @return the bundle name
		 */
		public String getPluginName() { return pluginNameText.getText().trim(); }

		/**
		 * Returns the vendor name entered by the user.
		 *
		 * @return the bundle vendor
		 */
		public String getVendor() { return vendorText.getText().trim(); }

		/**
		 * Returns the base Java package entered by the user.
		 *
		 * @return the root Java package (dot-separated)
		 */
		public String getBasePackage() { return basePackageText.getText().trim(); }

		/**
		 * Returns the short description entered by the user.
		 *
		 * @return the plugin description
		 */
		public String getDescription() { return descriptionText.getText().trim(); }
	}

}
