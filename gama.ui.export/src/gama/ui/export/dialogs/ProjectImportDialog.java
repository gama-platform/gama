package gama.ui.export.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gama.export.ProjectArchive;
import gama.export.ProjectPlugins;
import gama.ui.export.ProjectArchiveImport;
import gama.ui.navigator.view.contents.UserGroups;
import gama.workspace.manager.ProjectCategory;

public final class ProjectImportDialog extends TitleAreaDialog {

	private final ProjectArchive.Info info;
	private final String suggestedGroup;
	private Combo group;
	private Button install;
	private String selectedGroup;
	private boolean installPlugins;
	private List<ProjectArchiveImport.Choice> choices = List.of();

	public ProjectImportDialog(final Shell shell, final ProjectArchive.Info info, final String suggestedGroup) {
		super(shell);
		this.info = info;
		this.suggestedGroup = suggestedGroup;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override protected Control createDialogArea(final Composite parent) {
		setTitle("Import GAMA project");
		setMessage("Import a workspace copy. Existing projects are never overwritten without confirmation.");
		final var area = (Composite) super.createDialogArea(parent);
		final var content = new Composite(area, SWT.NONE);
		content.setLayout(new GridLayout(2, false));
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final var details = new Text(content, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		final var data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		data.widthHint = 530;
		data.heightHint = 210;
		details.setLayoutData(data);
		details.setText(info.description());
		new Label(content, SWT.NONE).setText("Navigator group (optional):");
		group = new Combo(content, SWT.DROP_DOWN);
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final var groups = new java.util.TreeSet<String>();
		groups.add("");
		for (final var project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			final var path = ProjectCategory.groupOf(project.getLocation());
			if (!path.isEmpty() && UserGroups.validate(path.toPortableString()) == null) groups.add(path.toPortableString());
		}
		group.setItems(groups.toArray(String[]::new));
		group.setText(suggestedGroup);
		final var missing = ProjectPlugins.missing(info);
		if (missing.stream().anyMatch(p -> !p.artifact().isEmpty())) {
			install = new Button(content, SWT.CHECK);
			install.setText("Install included missing plugins (requires restart)");
			install.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			final var warning = new Label(content, SWT.WRAP);
			warning.setText("Plugins execute Java code with your permissions. Install only archives from trusted authors.\n"
					+ "If installation is declined, projects are imported but may not compile.");
			warning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		}
		if (!missing.isEmpty()) setMessage("Some required plugins are not installed. Review the requirements below.",
				org.eclipse.jface.dialogs.IMessageProvider.WARNING);
		return area;
	}

	@Override protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Import", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override protected void okPressed() {
		selectedGroup = group.getText().trim();
		final String error = UserGroups.validate(selectedGroup);
		if (error != null) { setErrorMessage(error); return; }
		final var result = new ArrayList<ProjectArchiveImport.Choice>();
		final var used = new java.util.HashSet<String>();
		for (final String source : info.projects()) {
			String name = source;
			boolean replace = false;
			final var existing = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			final String initialProblem = UserGroups.checkProjectName(name, selectedGroup, null);
			if (existing.exists() || initialProblem != null || used.contains(name)) {
				final int answer = new MessageDialog(getShell(), "Project already exists", null,
						"A project or destination named '" + name + "' already exists.\n"
								+ (existing.exists() ? "Replacing it keeps a backup of the original project."
										: "Choose another name to preserve existing files."),
						MessageDialog.QUESTION, existing.exists()
								? new String[] { "Cancel", "Replace existing model", "Rename and import" }
								: new String[] { "Cancel", "Rename and import" }, 0).open();
				if (answer < 1) { cancelPressed(); return; }
				if (answer == 1 && existing.exists()) replace = true;
				else {
					final var rename = new InputDialog(getShell(), "Rename and import", "Name of the imported project:",
							name + "-2", value -> used.contains(value.trim()) ? "This name is already in this import"
									: UserGroups.checkProjectName(value, selectedGroup, null));
					if (rename.open() != IDialogConstants.OK_ID) { cancelPressed(); return; }
					name = rename.getValue().trim();
				}
			}
			final String problem = UserGroups.checkProjectName(name, selectedGroup, replace ? existing : null);
			if (problem != null || !used.add(name)) {
				setErrorMessage(problem == null ? "Duplicate project name" : problem);
				return;
			}
			result.add(new ProjectArchiveImport.Choice(source, name, replace));
		}
		choices = List.copyOf(result);
		installPlugins = install != null && install.getSelection();
		super.okPressed();
	}

	public String group() { return selectedGroup; }
	public List<ProjectArchiveImport.Choice> choices() { return choices; }
	public boolean installPlugins() { return installPlugins; }
}
