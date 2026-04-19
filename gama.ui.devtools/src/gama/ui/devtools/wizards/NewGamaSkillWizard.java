/*******************************************************************************************************
 *
 * NewGamaSkillWizard.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.wizards;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * A single-page wizard that generates a GAMA Skill class annotated with {@code @skill}.
 *
 * <p>
 * The user provides:
 * </p>
 * <ul>
 * <li>The target GAMA plugin project (selected from all open projects with PDE nature)</li>
 * <li>The Java package for the new class</li>
 * <li>The Java class name (e.g. {@code MySkill})</li>
 * <li>The GAML keyword / skill name (e.g. {@code my_skill})</li>
 * </ul>
 *
 * <p>
 * The generated class extends {@code Skill}, carries the {@code @skill} annotation with the provided name,
 * and includes a stub {@code @action}-annotated method to demonstrate the pattern.
 * </p>
 *
 * @author GAMA Development Team
 * @since 2026
 */
public class NewGamaSkillWizard extends Wizard implements INewWizard {

	/** The single wizard page. */
	private SkillPage page;

	/**
	 * Constructs the wizard with a descriptive title.
	 */
	public NewGamaSkillWizard() {
		setWindowTitle("New GAMA Skill");
		setNeedsProgressMonitor(true);
	}

	/** {@inheritDoc} */
	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection selection) {}

	/** {@inheritDoc} */
	@Override
	public void addPages() {
		page = new SkillPage();
		addPage(page);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Generates the skill Java source file in the selected project under the specified package.
	 * </p>
	 *
	 * @return {@code true} on success
	 */
	@Override
	public boolean performFinish() {
		final String projectName = page.getProjectName();
		final String packageName = page.getPackageName();
		final String className = page.getClassName();
		final String skillName = page.getSkillName();

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			@Override
			protected void execute(final IProgressMonitor monitor)
					throws CoreException, InvocationTargetException, InterruptedException {
				IWorkspace ws = ResourcesPlugin.getWorkspace();
				IProject project = ws.getRoot().getProject(projectName);
				String packagePath = "src/" + packageName.replace('.', '/');
				String filePath = packagePath + "/" + className + ".java";
				String source = buildSkillSource(packageName, className, skillName);
				IFile file = project.getFile(filePath);
				if (!file.getParent().exists()) {
					createFolders(project, packagePath, monitor);
				}
				file.create(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)), true, monitor);
			}
		};
		try {
			getContainer().run(true, true, op);
		} catch (InvocationTargetException | InterruptedException e) {
			org.eclipse.jface.dialogs.MessageDialog.openError(getShell(), "Error creating GAMA Skill",
					e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Generates the Java source for a skill class.
	 *
	 * @param packageName
	 *            the Java package of the class
	 * @param className
	 *            the simple class name
	 * @param skillName
	 *            the GAML skill keyword
	 * @return the complete Java source as a string
	 */
	private String buildSkillSource(final String packageName, final String className, final String skillName) {
		return "package " + packageName + ";\n\n"
				+ "import gama.annotations.action;\n"
				+ "import gama.annotations.doc;\n"
				+ "import gama.annotations.skill;\n"
				+ "import gama.core.runtime.IScope;\n"
				+ "import gama.core.runtime.exceptions.GamaRuntimeException;\n"
				+ "import gama.gaml.skills.Skill;\n\n"
				+ "/**\n"
				+ " * A GAMA skill that provides ... to agent species.\n"
				+ " *\n"
				+ " * <p>Attach to a species with: {@code species my_agent skills: [" + skillName + "]}</p>\n"
				+ " */\n"
				+ "@skill (\n"
				+ "        name = \"" + skillName + "\",\n"
				+ "        doc = @doc (\"TODO: describe what this skill provides\"))\n"
				+ "public class " + className + " extends Skill {\n\n"
				+ "    /**\n"
				+ "     * Example action stub. Replace or add actions as needed.\n"
				+ "     *\n"
				+ "     * @param scope the current simulation scope\n"
				+ "     * @return null\n"
				+ "     * @throws GamaRuntimeException on runtime error\n"
				+ "     */\n"
				+ "    @action (\n"
				+ "            name = \"my_action\",\n"
				+ "            doc = @doc (\"TODO: describe what this action does\"))\n"
				+ "    public Object myAction(final IScope scope) throws GamaRuntimeException {\n"
				+ "        // TODO: implement\n"
				+ "        return null;\n"
				+ "    }\n\n"
				+ "}\n";
	}

	/**
	 * Creates all intermediate folders along the given slash-separated path inside the project.
	 *
	 * @param project
	 *            the project in which to create the folders
	 * @param path
	 *            the folder path relative to the project root (forward slashes)
	 * @param monitor
	 *            the progress monitor
	 * @throws CoreException
	 *             if folder creation fails
	 */
	private void createFolders(final IProject project, final String path, final IProgressMonitor monitor)
			throws CoreException {
		String[] parts = path.split("/");
		org.eclipse.core.resources.IFolder current = null;
		for (String part : parts) {
			current = (current == null) ? project.getFolder(part) : current.getFolder(part);
			if (!current.exists()) { current.create(true, true, monitor); }
		}
	}

	// =========================================================================
	// Inner page
	// =========================================================================

	/**
	 * The sole wizard page for the New GAMA Skill wizard. Collects the target project, package, class name, and
	 * GAML skill keyword.
	 *
	 * @author GAMA Development Team
	 * @since 2026
	 */
	private static class SkillPage extends WizardPage {

		/** Drop-down of available open PDE projects. */
		private Combo projectCombo;

		/** Text field for the Java package name. */
		private Text packageText;

		/** Text field for the Java class name. */
		private Text classNameText;

		/** Text field for the GAML skill keyword. */
		private Text skillNameText;

		/**
		 * Constructs the page with title and description.
		 */
		SkillPage() {
			super("skillPage");
			setTitle("New GAMA Skill");
			setDescription("Enter the details for the new GAMA Skill class.");
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Builds a two-column grid with fields for project, package, class name, and skill keyword. The project
		 * combo is pre-populated with all open projects that have the PDE plug-in nature.
		 * </p>
		 *
		 * @param parent
		 *            the parent composite
		 */
		@Override
		public void createControl(final Composite parent) {
			Composite container = new Composite(parent, SWT.NONE);
			container.setLayout(new GridLayout(2, false));

			new Label(container, SWT.NONE).setText("Target project:");
			projectCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			projectCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
				try {
					if (p.isOpen() && p.hasNature("org.eclipse.pde.PluginNature")) {
						projectCombo.add(p.getName());
					}
				} catch (CoreException e) { /* skip */ }
			}
			if (projectCombo.getItemCount() > 0) { projectCombo.select(0); }

			packageText = createField(container, "Java package:", "gama.extension.myplugin");
			classNameText = createField(container, "Class name:", "MySkill");
			skillNameText = createField(container, "GAML skill name:", "my_skill");

			setControl(container);
			setPageComplete(true);
		}

		/**
		 * Convenience method to create a label and text field row.
		 *
		 * @param parent
		 *            the two-column grid parent
		 * @param label
		 *            the label text
		 * @param def
		 *            the default field value
		 * @return the created {@link Text} widget
		 */
		private Text createField(final Composite parent, final String label, final String def) {
			new Label(parent, SWT.NONE).setText(label);
			Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			t.setText(def);
			return t;
		}

		/**
		 * Returns the name of the selected target project.
		 *
		 * @return the project name
		 */
		public String getProjectName() { return projectCombo.getText(); }

		/**
		 * Returns the Java package name entered by the user.
		 *
		 * @return the package name (dot-separated)
		 */
		public String getPackageName() { return packageText.getText().trim(); }

		/**
		 * Returns the Java class name entered by the user.
		 *
		 * @return the simple class name
		 */
		public String getClassName() { return classNameText.getText().trim(); }

		/**
		 * Returns the GAML skill keyword entered by the user.
		 *
		 * @return the skill name as it will appear in GAML
		 */
		public String getSkillName() { return skillNameText.getText().trim(); }
	}

}
