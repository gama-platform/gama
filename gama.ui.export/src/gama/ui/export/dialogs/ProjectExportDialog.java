package gama.ui.export.dialogs;

import java.nio.file.Path;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gama.export.ProjectArchiveExporter;

/** SWT's native FileDialog has no portable custom-checkbox API. */
public final class ProjectExportDialog extends TitleAreaDialog {

	private final ProjectArchiveExporter.Plan plan;
	private Text destination;
	private Button links;
	private Button plugins;
	private Path output;
	private boolean resolveLinks;
	private boolean includePlugins;

	public ProjectExportDialog(final Shell shell, final ProjectArchiveExporter.Plan plan) {
		super(shell);
		this.plan = plan;
	}

	@Override protected Control createDialogArea(final Composite parent) {
		setTitle("Export GAMA project");
		setMessage("Save the complete project '" + plan.project().getName() + "' as a .gamp archive.");
		final var area = (Composite) super.createDialogArea(parent);
		final var content = new Composite(area, SWT.NONE);
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		content.setLayout(new GridLayout(2, false));
		destination = new Text(content, SWT.BORDER);
		final var layout = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layout.widthHint = 440;
		destination.setLayoutData(layout);
		destination.setText(Path.of(System.getProperty("user.home"), plan.project().getName() + ".gamp").toString());
		final var browse = new Button(content, SWT.PUSH);
		browse.setText("Browse...");
		browse.addListener(SWT.Selection, e -> {
			final var dialog = new FileDialog(getShell(), SWT.SAVE);
			dialog.setText("Export GAMA project");
			dialog.setFilterExtensions(new String[] { "*.gamp" });
			dialog.setFileName(plan.project().getName() + ".gamp");
			dialog.setOverwrite(true);
			final String selected = dialog.open();
			if (selected != null) destination.setText(selected);
		});
		links = new Button(content, SWT.CHECK);
		links.setText("Resolve and export linked resources");
		links.setToolTipText("Include linked files and folders as ordinary project resources. Otherwise only their link definitions are kept.");
		links.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		if (plan.hasExtraPlugins()) {
			plugins = new Button(content, SWT.CHECK);
			plugins.setText("Export required GAMA plugins");
			plugins.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		}
		if (!plan.warnings().isEmpty()) setMessage(String.join("\n", plan.warnings()), org.eclipse.jface.dialogs.IMessageProvider.WARNING);
		return area;
	}

	@Override protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Save", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override protected void okPressed() {
		try {
			String name = destination.getText().trim();
			if (name.isEmpty()) { setErrorMessage("Choose an archive destination."); return; }
			if (!name.toLowerCase(java.util.Locale.ROOT).endsWith(".gamp")) name += ".gamp";
			output = Path.of(name).toAbsolutePath().normalize();
			if (java.nio.file.Files.exists(output) && !org.eclipse.jface.dialogs.MessageDialog.openQuestion(getShell(),
					"Replace archive?", "Replace the existing archive?\n" + output)) return;
			resolveLinks = links.getSelection();
			includePlugins = plugins != null && plugins.getSelection();
			super.okPressed();
		} catch (final java.nio.file.InvalidPathException e) { setErrorMessage(e.getMessage()); }
	}

	public Path output() { return output; }
	public boolean resolveLinks() { return resolveLinks; }
	public boolean includePlugins() { return includePlugins; }
}
