/*******************************************************************************************************
 *
 * NewGamaPluginWizard.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDE;

import gama.ui.devtools.jobs.CleanAndRebuildJob;
import gama.ui.devtools.preferences.DevToolsPreferences;
import gama.ui.devtools.templates.CodeTemplates;
import gama.ui.devtools.templates.PluginSpec;
import gama.ui.devtools.templates.WorkspaceIntegration;
import gama.ui.devtools.util.GamaProjects;
import gama.ui.devtools.util.Notifier;
import gama.ui.devtools.util.ResourceUtils;

/**
 * Creates a new GAMA plugin with the standard layout ({@code src}, {@code gaml}, {@code libs}, {@code models}), the
 * annotation processor configured, a Maven pom, and optionally registers it in the GAMA build (modules of
 * {@code gama.parent}, {@code gama.feature.extensions} or a dedicated feature in the update site).
 */
public class NewGamaPluginWizard extends Wizard implements INewWizard {

	/** The ids of the projects used for the integration in the GAMA build. */
	private static final String EXTENSIONS_FEATURE = "gama.feature.extensions", P2SITE = "gama.p2site";

	/** The model sub-folders that can be created. */
	private static final List<String> MODEL_FOLDERS = List.of("Tests", "Recipes", "Library models", "Data");

	/** The page. */
	private Page page;

	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection selection) {
		setWindowTitle("New GAMA Plugin");
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		PluginSpec spec = page.spec();
		IPath location = page.location();
		boolean register = page.register.getSelection();
		List<String> warnings = new ArrayList<>();
		IProject[] created = new IProject[1];
		try {
			getContainer().run(true, false, new WorkspaceModifyOperation() {
				@Override
				protected void execute(final IProgressMonitor monitor) throws CoreException {
					SubMonitor sub = SubMonitor.convert(monitor, "Creating " + spec.id(), 4);
					created[0] = createPlugin(spec, location, sub.split(2));
					if (spec.standaloneFeature()) { createFeature(spec, location, sub.split(1)); }
					if (register) { register(spec, warnings, sub.split(1)); }
				}
			});
		} catch (InvocationTargetException e) {
			page.setErrorMessage("Error: " + e.getCause().getMessage());
			ResourceUtils.log(e.getCause());
			return false;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
		IProject project = created[0];
		openMainFile(project, spec);
		new CleanAndRebuildJob(List.of(project), false).schedule();
		Notifier.show("GAMA plugin created",
				spec.id() + " has been created and is being built."
						+ (warnings.isEmpty() ? "" : "\n" + String.join("\n", warnings)));
		return true;
	}

	/** Opens the example class (if any) or the manifest. */
	private void openMainFile(final IProject project, final PluginSpec spec) {
		IFile file = spec.exampleCode()
				? project.getFile("src/" + spec.basePackage().replace('.', '/') + "/" + exampleClass(spec) + ".java")
				: project.getFile("META-INF/MANIFEST.MF");
		try {
			IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		} catch (PartInitException e) {
			ResourceUtils.log(e);
		}
	}

	/** The name of the example operators class. */
	private static String exampleClass(final PluginSpec spec) {
		String s = CodeTemplates.javaIdentifier(spec.shortcut());
		return Character.toUpperCase(s.charAt(0)) + s.substring(1) + "Operators";
	}

	/** The name of the example operator. */
	private static String exampleOperator(final PluginSpec spec) {
		return CodeTemplates.javaIdentifier(spec.shortcut()).toLowerCase() + "_double";
	}

	/**
	 * Creates (and opens) a project at {@code parent/name}, or in the workspace if {@code parent} is null.
	 */
	private static IProject createProject(final String name, final IPath parent, final String[] natures,
			final String[] builders, final IProgressMonitor monitor) throws CoreException {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IProject project = ws.getRoot().getProject(name);
		IProjectDescription desc = ws.newProjectDescription(name);
		if (parent != null && !parent.equals(ws.getRoot().getLocation())) { desc.setLocation(parent.append(name)); }
		desc.setNatureIds(natures);
		List<ICommand> commands = new ArrayList<>();
		for (String b : builders) {
			ICommand c = desc.newCommand();
			c.setBuilderName(b);
			commands.add(c);
		}
		desc.setBuildSpec(commands.toArray(ICommand[]::new));
		project.create(desc, monitor);
		project.open(monitor);
		return project;
	}

	/** Creates the plugin project and all its files. */
	private static IProject createPlugin(final PluginSpec spec, final IPath location, final IProgressMonitor monitor)
			throws CoreException {
		SubMonitor sub = SubMonitor.convert(monitor, 20);
		IProject p = createProject(spec.id(), location, new String[] { "org.eclipse.pde.PluginNature", JavaCore.NATURE_ID },
				new String[] { JavaCore.BUILDER_ID, "org.eclipse.pde.ManifestBuilder", "org.eclipse.pde.SchemaBuilder" },
				sub.split(2));
		List<String> jars = List.of();
		ResourceUtils.write(p, ".settings/org.eclipse.jdt.apt.core.prefs", CodeTemplates.aptPrefs(), sub.split(1));
		ResourceUtils.write(p, ".settings/org.eclipse.jdt.core.prefs", CodeTemplates.jdtPrefs(), sub.split(1));
		ResourceUtils.write(p, ".settings/org.eclipse.core.resources.prefs", CodeTemplates.resourcesPrefs(),
				sub.split(1));
		ResourceUtils.write(p, ".factorypath", CodeTemplates.factoryPath(), sub.split(1));
		ResourceUtils.write(p, ".gitignore", CodeTemplates.gitignore(), sub.split(1));
		ResourceUtils.write(p, "META-INF/MANIFEST.MF", CodeTemplates.manifest(spec, jars), sub.split(1));
		ResourceUtils.write(p, "build.properties", CodeTemplates.buildProperties(spec, jars), sub.split(1));
		ResourceUtils.write(p, "plugin.xml", CodeTemplates.pluginXml(), sub.split(1));
		ResourceUtils.write(p, "pom.xml", CodeTemplates.pluginPom(spec.id()), sub.split(1));
		ResourceUtils.write(p, "README.md", CodeTemplates.readme(spec), sub.split(1));
		String pkgPath = "src/" + spec.basePackage().replace('.', '/');
		ResourceUtils.createFolder(p, pkgPath, sub.split(1));
		ResourceUtils.createFolder(p, GamaProjects.GEN_SRC_DIR, sub.split(1));
		if (spec.libs()) {
			ResourceUtils.write(p, "libs/README.md",
					"Put third-party jars here. Declare only the required runtime jars in Bundle-ClassPath, "
							+ "build.properties and .classpath. Source and Javadoc archives should be attached "
							+ "to their libraries, not added as runtime dependencies. Configuration repair does "
							+ "not register jars automatically.\n",
					sub.split(1));
		}
		for (String f : spec.modelFolders()) { ResourceUtils.createFolder(p, "models/" + f, sub.split(1)); }
		if (spec.exampleCode()) {
			String op = exampleOperator(spec);
			ResourceUtils.write(p, pkgPath + "/" + exampleClass(spec) + ".java",
					CodeTemplates.operator(spec.basePackage(), exampleClass(spec), op), sub.split(1));
			ResourceUtils.write(p, "models/Tests/" + op + "Tests.gaml", CodeTemplates.exampleTestModel(op),
					sub.split(1));
		}
		// Written last so that the classpath is valid when JDT first looks at the project
		ResourceUtils.write(p, ".classpath", CodeTemplates.classpath(jars), sub.split(1));
		return p;
	}

	/** Creates the dedicated feature project. */
	private static void createFeature(final PluginSpec spec, final IPath location, final IProgressMonitor monitor)
			throws CoreException {
		SubMonitor sub = SubMonitor.convert(monitor, 4);
		IProject f = createProject(spec.featureId(), location, new String[] { "org.eclipse.pde.FeatureNature" },
				new String[] { "org.eclipse.pde.FeatureBuilder" }, sub.split(1));
		ResourceUtils.write(f, "feature.xml", CodeTemplates.featureXml(spec), sub.split(1));
		ResourceUtils.write(f, "build.properties", CodeTemplates.featureBuildProperties(), sub.split(1));
		ResourceUtils.write(f, "pom.xml", CodeTemplates.featurePom(spec.featureId()), sub.split(1));
	}

	/** Registers the plugin (and its feature) in the GAMA build. Missing projects produce warnings. */
	private static void register(final PluginSpec spec, final List<String> warnings, final IProgressMonitor monitor)
			throws CoreException {
		SubMonitor sub = SubMonitor.convert(monitor, 3);
		IProject parent = GamaProjects.root().getProject(GamaProjects.PARENT_PROJECT);
		String pom = parent.isOpen() ? ResourceUtils.read(parent, "pom.xml") : null;
		if (pom == null) {
			warnings.add("gama.parent is not in the workspace: add the module(s) to its pom.xml manually.");
		} else {
			String updated = WorkspaceIntegration.addModule(pom, spec.id(), "gama.extension.");
			if (spec.standaloneFeature()) {
				updated = WorkspaceIntegration.addModule(updated, spec.featureId(), "gama.feature.extension");
			}
			ResourceUtils.write(parent, "pom.xml", updated, sub.split(1));
		}
		if (spec.standaloneFeature()) {
			IProject site = GamaProjects.root().getProject(P2SITE);
			String category = site.isOpen() ? ResourceUtils.read(site, "category.xml") : null;
			if (category == null) {
				warnings.add(P2SITE + " is not in the workspace: add " + spec.featureId() + " to its category.xml.");
			} else {
				ResourceUtils.write(site, "category.xml",
						WorkspaceIntegration.addFeatureToCategory(category, spec.featureId()), sub.split(1));
			}
		} else {
			IProject feature = GamaProjects.root().getProject(EXTENSIONS_FEATURE);
			String xml = feature.isOpen() ? ResourceUtils.read(feature, "feature.xml") : null;
			if (xml == null) {
				warnings.add(EXTENSIONS_FEATURE + " is not in the workspace: add " + spec.id() + " to its feature.xml.");
			} else {
				ResourceUtils.write(feature, "feature.xml", WorkspaceIntegration.addPluginToFeature(xml, spec.id()),
						sub.split(1));
			}
		}
	}

	/** The page of the wizard. */
	private class Page extends WizardPage {

		/** Widgets. */
		Text id, name, vendor, pkg, description, location;
		Button libs, example, inExtensions, standalone, register;
		List<Button> folders = new ArrayList<>();
		private boolean pkgEdited, nameEdited;
		private final Set<String> existingShortcuts = new TreeSet<>();

		Page() {
			super("plugin");
			setTitle("New GAMA Plugin");
			setDescription("Creates a plugin extending GAML, with the annotation processor configured.");
			for (IProject p : GamaProjects.all()) { existingShortcuts.add(GamaProjects.shortcut(p.getName())); }
		}

		PluginSpec spec() {
			List<String> mf = new ArrayList<>();
			for (Button b : folders) { if (b.getSelection()) { mf.add((String) b.getData()); } }
			if (example.getSelection() && !mf.contains("Tests")) { mf.add(0, "Tests"); }
			return new PluginSpec(id.getText().strip(), name.getText().strip(), vendor.getText().strip(),
					pkg.getText().strip(), description.getText().strip(), libs.getSelection(), mf,
					example.getSelection(), standalone.getSelection());
		}

		IPath location() {
			String l = location.getText().strip();
			return l.isEmpty() ? null : IPath.fromOSString(l);
		}

		@Override
		public void createControl(final Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			c.setLayout(new GridLayout(3, false));
			String prefix = DevToolsPreferences.get(DevToolsPreferences.DEFAULT_PREFIX);
			id = text(c, "Plugin id:", prefix);
			name = text(c, "Name:", "");
			vendor = text(c, "Vendor:", DevToolsPreferences.get(DevToolsPreferences.DEFAULT_VENDOR));
			pkg = text(c, "Base package:", prefix);
			description = text(c, "Description:", "");
			location = new Text(c, SWT.BORDER);
			label(c, "Location:", location, 1);
			location.setText(defaultLocation());
			Button browse = new Button(c, SWT.PUSH);
			browse.setText("Browse...");
			browse.addListener(SWT.Selection, e -> {
				DirectoryDialog d = new DirectoryDialog(getShell());
				d.setFilterPath(location.getText());
				String r = d.open();
				if (r != null) { location.setText(r); }
			});

			Group layout = group(c, "Layout");
			libs = check(layout, "libs/ folder for third-party jars", false);
			for (String f : MODEL_FOLDERS) {
				Button b = check(layout, "models/" + f, "Tests".equals(f));
				b.setData(f);
				folders.add(b);
			}
			example = check(layout, "Example operator, documented, with a test model", true);

			Group integration = group(c, "Integration in the GAMA build");
			inExtensions = new Button(integration, SWT.RADIO);
			inExtensions.setText("Add the plugin to " + EXTENSIONS_FEATURE);
			inExtensions.setSelection(true);
			standalone = new Button(integration, SWT.RADIO);
			standalone.setText("Create a dedicated feature (gama.feature.extension.<shortcut>) in the update site");
			register = check(integration, "Register the module(s) in gama.parent/pom.xml and the feature/update site",
					true);

			id.addListener(SWT.Modify, e -> {
				if (!pkgEdited) { pkg.setText(id.getText().strip()); }
				if (!nameEdited) { name.setText(defaultName()); }
				validate();
			});
			pkg.addListener(SWT.Modify, e -> {
				pkgEdited = !pkg.getText().equals(id.getText().strip());
				validate();
			});
			name.addListener(SWT.Modify, e -> {
				nameEdited = !name.getText().equals(defaultName());
				validate();
			});
			for (Text t : List.of(vendor, location)) { t.addListener(SWT.Modify, e -> validate()); }
			register.addListener(SWT.Selection, e -> validate());
			setControl(c);
			validate();
		}

		private String defaultName() {
			String s = GamaProjects.shortcut(id.getText().strip());
			return s.isEmpty() ? "" : "GAMA " + Character.toUpperCase(s.charAt(0)) + s.substring(1) + " Extension";
		}

		/** The parent folder of gama.processor (i.e. the root of the git repository), or the workspace. */
		private String defaultLocation() {
			IProject proc = GamaProjects.processor();
			IPath l = proc == null ? null : proc.getLocation();
			return l == null ? ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString()
					: l.removeLastSegments(1).toOSString();
		}

		private Group group(final Composite c, final String title) {
			Group g = new Group(c, SWT.NONE);
			g.setText(title);
			g.setLayout(new GridLayout(1, false));
			g.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
			return g;
		}

		private Button check(final Composite c, final String text, final boolean selected) {
			Button b = new Button(c, SWT.CHECK);
			b.setText(text);
			b.setSelection(selected);
			return b;
		}

		private Text text(final Composite c, final String label, final String value) {
			Text t = new Text(c, SWT.BORDER);
			label(c, label, t, 2);
			t.setText(value);
			return t;
		}

		private void label(final Composite c, final String text, final Control control, final int span) {
			Label l = new Label(c, SWT.NONE);
			l.setText(text);
			l.moveAbove(control);
			control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, span, 1));
		}

		private void validate() {
			setErrorMessage(null);
			setPageComplete(false);
			String i = id.getText().strip();
			IStatus s = JavaConventions.validatePackageName(i, JavaCore.VERSION_21, JavaCore.VERSION_21);
			if (s.getSeverity() == IStatus.ERROR) {
				setErrorMessage("Plugin id: " + s.getMessage());
				return;
			}
			if (i.endsWith(".")) {
				setErrorMessage("Enter the plugin id");
				return;
			}
			if (GamaProjects.root().getProject(i).exists()) {
				setErrorMessage("A project named " + i + " already exists");
				return;
			}
			String shortcut = GamaProjects.shortcut(i);
			if (existingShortcuts.contains(shortcut)) {
				setErrorMessage("Another GAMA plugin already ends with '." + shortcut
						+ "': the generated additions would collide (gaml.additions." + shortcut + ")");
				return;
			}
			s = JavaConventions.validatePackageName(pkg.getText().strip(), JavaCore.VERSION_21, JavaCore.VERSION_21);
			if (s.getSeverity() == IStatus.ERROR) {
				setErrorMessage("Base package: " + s.getMessage());
				return;
			}
			if (name.getText().isBlank() || vendor.getText().isBlank()) {
				setErrorMessage("Enter the name and the vendor of the plugin");
				return;
			}
			IPath l = location();
			if (l == null || !l.toFile().isDirectory()) {
				setErrorMessage("The location must be an existing folder");
				return;
			}
			if (register.getSelection() && !l.equals(IPath.fromOSString(defaultLocation()))) {
				setErrorMessage("To register this plugin, choose the GAMA repository root as its location");
				return;
			}
			if (new File(l.toFile(), i).exists()) {
				setErrorMessage(l.append(i).toOSString() + " already exists");
				return;
			}
			setPageComplete(true);
		}
	}

}
