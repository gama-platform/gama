/*******************************************************************************************************
 *
 * NewGamaOperatorWizard.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and
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
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
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
 * A single-page wizard that generates a GAMA Operator class annotated with {@code @operator}.
 *
 * <p>
 * The user provides:
 * </p>
 * <ul>
 * <li>The target GAMA plugin project</li>
 * <li>The Java package for the new class</li>
 * <li>The Java class name (e.g. {@code MyOperators})</li>
 * <li>The GAML operator keyword (e.g. {@code my_op})</li>
 * </ul>
 *
 * <p>
 * The generated class contains a single static method annotated with {@code @operator} as a boilerplate starting
 * point. Methods must be {@code static} so that GAMA's annotation processor can register them.
 * </p>
 *
 * @author GAMA Development Team
 * @since 2026
 */
public class NewGamaOperatorWizard extends Wizard implements INewWizard {

	/** The single wizard page. */
	private OperatorPage page;

	/**
	 * Constructs the wizard with a descriptive title.
	 */
	public NewGamaOperatorWizard() {
		setWindowTitle("New GAMA Operator");
		setNeedsProgressMonitor(true);
	}

	/** {@inheritDoc} */
	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection selection) {}

	/** {@inheritDoc} */
	@Override
	public void addPages() {
		page = new OperatorPage();
		addPage(page);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Generates the operator Java source file in the selected project.
	 * </p>
	 *
	 * @return {@code true} on success
	 */
	@Override
	public boolean performFinish() {
		final String projectName = page.getProjectName();
		final String packageName = page.getPackageName();
		final String className = page.getClassName();
		final String operatorName = page.getOperatorName();

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			@Override
			protected void execute(final IProgressMonitor monitor)
					throws CoreException, InvocationTargetException, InterruptedException {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				String packagePath = "src/" + packageName.replace('.', '/');
				String filePath = packagePath + "/" + className + ".java";
				createFolders(project, packagePath, monitor);
				IFile file = project.getFile(filePath);
				String source = buildOperatorSource(packageName, className, operatorName);
				file.create(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)), true, monitor);
			}
		};
		try {
			getContainer().run(true, true, op);
		} catch (InvocationTargetException | InterruptedException e) {
			org.eclipse.jface.dialogs.MessageDialog.openError(getShell(), "Error creating GAMA Operator",
					e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Generates the Java source for an operator class.
	 *
	 * @param packageName
	 *            the Java package
	 * @param className
	 *            the simple class name
	 * @param operatorName
	 *            the GAML operator keyword
	 * @return the complete Java source as a string
	 */
	private String buildOperatorSource(final String packageName, final String className, final String operatorName) {
		return "package " + packageName + ";\n\n"
				+ "import gama.annotations.doc;\n"
				+ "import gama.annotations.operator;\n"
				+ "import gama.core.runtime.IScope;\n\n"
				+ "/**\n"
				+ " * Provides the GAML {@code " + operatorName + "} operator.\n"
				+ " */\n"
				+ "public class " + className + " {\n\n"
				+ "    /**\n"
				+ "     * Example operator stub. Rename and adjust the signature as needed.\n"
				+ "     * Operator methods must be {@code static}.\n"
				+ "     *\n"
				+ "     * @param scope the current simulation scope\n"
				+ "     * @return a result value\n"
				+ "     */\n"
				+ "    @operator (\n"
				+ "            value = \"" + operatorName + "\",\n"
				+ "            can_be_const = false,\n"
				+ "            doc = @doc (\"TODO: describe what this operator computes\"))\n"
				+ "    public static Object " + toLowerCamel(operatorName) + "(final IScope scope) {\n"
				+ "        // TODO: implement\n"
				+ "        return null;\n"
				+ "    }\n\n"
				+ "}\n";
	}

	/**
	 * Converts a snake_case GAML name to a lowerCamelCase Java identifier.
	 *
	 * @param name
	 *            a snake_case string (e.g. {@code my_operator})
	 * @return the lowerCamelCase equivalent (e.g. {@code myOperator})
	 */
	private String toLowerCamel(final String name) {
		StringBuilder sb = new StringBuilder();
		boolean upper = false;
		for (char c : name.toCharArray()) {
			if (c == '_') {
				upper = true;
			} else {
				sb.append(upper ? Character.toUpperCase(c) : c);
				upper = false;
			}
		}
		return sb.toString();
	}

	/**
	 * Creates all intermediate folders along the given path inside the project.
	 *
	 * @param project
	 *            the target project
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
		IFolder current = null;
		for (String part : parts) {
			current = (current == null) ? project.getFolder(part) : current.getFolder(part);
			if (!current.exists()) { current.create(true, true, monitor); }
		}
	}

	// =========================================================================
	// Inner page
	// =========================================================================

	/**
	 * The sole wizard page for the New GAMA Operator wizard. Collects the target project, package, class name,
	 * and GAML operator keyword.
	 *
	 * @author GAMA Development Team
	 * @since 2026
	 */
	private static class OperatorPage extends WizardPage {

		/** Drop-down of available open PDE projects. */
		private Combo projectCombo;

		/** Text field for the Java package name. */
		private Text packageText;

		/** Text field for the Java class name. */
		private Text classNameText;

		/** Text field for the GAML operator keyword. */
		private Text operatorNameText;

		/**
		 * Constructs the page.
		 */
		OperatorPage() {
			super("operatorPage");
			setTitle("New GAMA Operator");
			setDescription("Enter the details for the new GAMA Operator class.");
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Builds the two-column grid with all required fields.
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
					if (p.isOpen() && p.hasNature("org.eclipse.pde.PluginNature")) { projectCombo.add(p.getName()); }
				} catch (CoreException e) { /* skip */ }
			}
			if (projectCombo.getItemCount() > 0) { projectCombo.select(0); }

			packageText = createField(container, "Java package:", "gama.extension.myplugin");
			classNameText = createField(container, "Class name:", "MyOperators");
			operatorNameText = createField(container, "GAML operator name:", "my_operator");

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
		 *            the default value
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
		 * Returns the GAML operator keyword entered by the user.
		 *
		 * @return the operator name as it will appear in GAML
		 */
		public String getOperatorName() { return operatorNameText.getText().trim(); }
	}

}
