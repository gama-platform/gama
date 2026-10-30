package gama.ui.export.dialogs;

import java.nio.file.Path;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import gama.export.ProjectArchiveExporter;

/** SWT's native FileDialog has no portable custom-checkbox API. */
public final class ProjectExportDialog extends Dialog {

	private final ProjectArchiveExporter.Plan plan;
	private Button links;
	private Button plugins;
	private Path output;
	private boolean resolveLinks;
	private boolean includePlugins;

	public ProjectExportDialog(final Shell shell, final ProjectArchiveExporter.Plan plan) {
		super(shell);
		this.plan = plan;
	}

	@Override protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		shell.setText("Export GAMA project");
	}

	@Override protected Control createDialogArea(final Composite parent) {
		final var area = (Composite) super.createDialogArea(parent);
		final var content = new Composite(area, SWT.NONE);
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		content.setLayout(new GridLayout(1, false));
		final var description = new Label(content, SWT.WRAP);
		description.setText("Export " + plan.project().getName() + " as a self-contained .gamp archive that can be shared.");
		final var descriptionLayout = new GridData(SWT.FILL, SWT.CENTER, true, false);
		descriptionLayout.widthHint = convertWidthInCharsToPixels(60);
		description.setLayoutData(descriptionLayout);
		final var separator = new Label(content, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		links = new Button(content, SWT.CHECK);
		links.setText("Resolve and export linked resources");
		links.setToolTipText("Include linked files and folders as ordinary project resources. Otherwise only their link definitions are kept.");
		links.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (plan.hasExtraPlugins()) {
			plugins = new Button(content, SWT.CHECK);
			plugins.setText("Export required GAMA plugins");
			plugins.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		if (!plan.warnings().isEmpty()) {
			final var warnings = new Label(content, SWT.WRAP);
			warnings.setText(String.join("\n", plan.warnings()));
			final var warningLayout = new GridData(SWT.FILL, SWT.CENTER, true, false);
			warningLayout.widthHint = convertWidthInCharsToPixels(60);
			warnings.setLayoutData(warningLayout);
		}
		return area;
	}

	@Override protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Choose destination...", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override protected void okPressed() {
		final var dialog = new FileDialog(getShell(), SWT.SAVE);
		dialog.setText("Export GAMA project");
		dialog.setFilterExtensions(new String[] { "*.gamp" });
		dialog.setFilterNames(new String[] { "GAMA project (*.gamp)" });
		dialog.setFilterPath(System.getProperty("user.home"));
		dialog.setFileName(plan.project().getName() + ".gamp");
		dialog.setOverwrite(true);
		final String selected = dialog.open();
		if (selected == null) return;
		try {
			String name = selected;
			if (!name.toLowerCase(java.util.Locale.ROOT).endsWith(".gamp")) name += ".gamp";
			output = Path.of(name).toAbsolutePath().normalize();
			if (!selected.equals(name) && java.nio.file.Files.exists(output)
					&& !MessageDialog.openQuestion(getShell(),
					"Replace archive?", "Replace the existing archive?\n" + output)) return;
			resolveLinks = links.getSelection();
			includePlugins = plugins != null && plugins.getSelection();
			super.okPressed();
		} catch (final java.nio.file.InvalidPathException e) {
			MessageDialog.openError(getShell(), "Invalid archive destination", e.getMessage());
		}
	}

	public Path output() { return output; }
	public boolean resolveLinks() { return resolveLinks; }
	public boolean includePlugins() { return includePlugins; }
}
