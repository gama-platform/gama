/*******************************************************************************************************
 *
 * AbstractGamaClassWizard.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDE;

import gama.ui.devtools.util.GamaProjects;
import gama.ui.devtools.util.ResourceUtils;

/**
 * Base class of the wizards that create Java classes contributing to GAML (skills, operators, actions, types) in an
 * existing GAMA plugin.
 */
public abstract class AbstractGamaClassWizard extends Wizard implements INewWizard {

	/** Valid GAML identifiers. */
	static final Pattern GAML_ID = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

	/** The page. */
	protected ClassPage page;

	/** The initial selection. */
	protected IStructuredSelection selection;

	@Override
	public void init(final IWorkbench workbench, final IStructuredSelection sel) {
		this.selection = sel;
		setNeedsProgressMonitor(true);
		setWindowTitle(title());
	}

	@Override
	public void addPages() {
		page = new ClassPage();
		addPage(page);
	}

	/** The title of the wizard. */
	protected abstract String title();

	/** A description of what is generated. */
	protected abstract String description();

	/** The default class name. */
	protected abstract String defaultClassName();

	/** The suffix removed from the class name to compute the default GAML name (e.g. "Skill"). */
	protected abstract String suffix();

	/** The label of the GAML name field (e.g. "Skill name"). */
	protected abstract String gamlNameLabel();

	/**
	 * Generates the files (simple file names in the package folder -> contents). The first one is opened in an
	 * editor.
	 */
	protected abstract Map<String, String> generate(String pkg, String className, String gamlName);

	@Override
	public boolean performFinish() {
		IProject project = page.project();
		String pkg = page.pkg.getText().strip();
		Map<String, String> files = generate(pkg, page.className.getText().strip(), page.gamlName.getText().strip());
		Map<String, IFile> created = new LinkedHashMap<>();
		try {
			getContainer().run(true, false, new WorkspaceModifyOperation() {
				@Override
				protected void execute(final org.eclipse.core.runtime.IProgressMonitor monitor)
						throws org.eclipse.core.runtime.CoreException {
					String folder = "src/" + pkg.replace('.', '/') + "/";
					for (Map.Entry<String, String> e : files.entrySet()) {
						created.put(e.getKey(), ResourceUtils.write(project, folder + e.getKey(), e.getValue(), monitor));
					}
				}
			});
		} catch (InvocationTargetException e) {
			page.setErrorMessage("Error: " + e.getCause());
			ResourceUtils.log(e.getCause());
			return false;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
		IWorkbenchPage wp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		created.values().stream().findFirst().ifPresent(f -> {
			try {
				IDE.openEditor(wp, f);
			} catch (PartInitException e) {
				ResourceUtils.log(e);
			}
		});
		return true;
	}

	/**
	 * The page of the class wizards: target plugin, package, class name and GAML name.
	 */
	protected class ClassPage extends WizardPage {

		/** The widgets. */
		Combo projects;
		Text pkg, className, gamlName;
		private List<IProject> gamaProjects;
		private boolean gamlNameEdited;

		ClassPage() {
			super("class");
			setTitle(title());
			setDescription(description());
		}

		IProject project() {
			int i = projects.getSelectionIndex();
			return i < 0 ? null : gamaProjects.get(i);
		}

		@Override
		public void createControl(final Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			c.setLayout(new GridLayout(2, false));
			gamaProjects = GamaProjects.all();
			projects = new Combo(c, SWT.READ_ONLY);
			label(c, "GAMA plugin:", projects);
			gamaProjects.forEach(p -> projects.add(p.getName()));
			pkg = new Text(c, SWT.BORDER);
			label(c, "Package:", pkg);
			className = new Text(c, SWT.BORDER);
			label(c, "Class name:", className);
			gamlName = new Text(c, SWT.BORDER);
			label(c, gamlNameLabel() + ":", gamlName);
			initFromSelection();
			className.setText(defaultClassName());
			gamlName.setText(computeGamlName());
			projects.addListener(SWT.Selection, e -> {
				IProject p = project();
				if (p != null && pkg.getText().isBlank()) { pkg.setText(p.getName()); }
				validate();
			});
			pkg.addListener(SWT.Modify, e -> validate());
			className.addListener(SWT.Modify, e -> {
				if (!gamlNameEdited) { gamlName.setText(computeGamlName()); }
				validate();
			});
			gamlName.addListener(SWT.Modify, e -> {
				gamlNameEdited = !gamlName.getText().equals(computeGamlName());
				validate();
			});
			setControl(c);
			validate();
		}

		private String computeGamlName() {
			return gama.ui.devtools.templates.CodeTemplates.gamlNameFor(className.getText().strip(), suffix());
		}

		private void label(final Composite c, final String text, final org.eclipse.swt.widgets.Control control) {
			Label l = new Label(c, SWT.NONE);
			l.setText(text);
			l.moveAbove(control);
			control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}

		private void initFromSelection() {
			if (selection == null || selection.isEmpty()) return;
			Object o = selection.getFirstElement();
			IJavaElement je = Adapters.adapt(o, IJavaElement.class);
			if (je != null) {
				IJavaElement frag = je.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
				if (frag != null && !frag.getElementName().isEmpty()) { pkg.setText(frag.getElementName()); }
			}
			IProject p = GamaProjects.firstProject(selection);
			if (p != null) {
				int i = gamaProjects.indexOf(p);
				if (i >= 0) {
					projects.select(i);
					if (pkg.getText().isBlank()) { pkg.setText(p.getName()); }
				}
			}
		}

		private void validate() {
			setErrorMessage(null);
			setPageComplete(false);
			IProject p = project();
			if (p == null) {
				setErrorMessage(gamaProjects.isEmpty() ? "There is no GAMA plugin in the workspace"
						: "Choose the GAMA plugin in which to create the class");
				return;
			}
			IStatus s = JavaConventions.validatePackageName(pkg.getText().strip(), JavaCore.VERSION_21,
					JavaCore.VERSION_21);
			if (s.getSeverity() == IStatus.ERROR) {
				setErrorMessage("Package: " + s.getMessage());
				return;
			}
			s = JavaConventions.validateJavaTypeName(className.getText().strip(), JavaCore.VERSION_21,
					JavaCore.VERSION_21, null);
			if (s.getSeverity() == IStatus.ERROR) {
				setErrorMessage("Class name: " + s.getMessage());
				return;
			}
			if (!GAML_ID.matcher(gamlName.getText().strip()).matches()) {
				setErrorMessage(gamlNameLabel() + " must be a valid GAML identifier");
				return;
			}
			String folder = "src/" + pkg.getText().strip().replace('.', '/') + "/";
			for (String f : generate(pkg.getText().strip(), className.getText().strip(), gamlName.getText().strip())
					.keySet()) {
				if (p.getFile(folder + f).exists()) {
					setErrorMessage(folder + f + " already exists");
					return;
				}
			}
			setPageComplete(true);
		}
	}

}
