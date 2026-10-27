/*******************************************************************************************************
 *
 * GamaLaunchDialog.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.launch;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jface.dialogs.IDialogConstants;
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

import gama.ui.devtools.launch.GamaLaunches.Flavor;
import gama.ui.devtools.util.ResourceUtils;

/** Associates each menu entry with an existing PDE configuration or creates a generated default. */
public final class GamaLaunchDialog extends TitleAreaDialog {

	private final Flavor initial;
	private Combo flavor;
	private Combo configuration;
	private Combo jdk;
	private List<ILaunchConfiguration> configurations;
	private List<IVMInstall> jdks;
	private ILaunchConfiguration result;

	public GamaLaunchDialog(final Shell shell, final Flavor initial) {
		super(shell);
		this.initial = initial;
	}

	public ILaunchConfiguration result() { return result; }

	public Flavor selectedFlavor() { return flavor(); }

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		setTitle("Configure GAMA Launches");
		setMessage("Reuse an existing Eclipse Application configuration, or generate a default from a GAMA product.");
		Composite content = new Composite(area, SWT.NONE);
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		content.setLayout(new GridLayout(2, false));
		flavor = combo(content, "GAMA flavor:");
		for (Flavor f : Flavor.values()) { flavor.add(f.label); }
		flavor.select(initial.ordinal());
		configuration = combo(content, "Launch configuration:");
		jdk = combo(content, "JDK for generated defaults:");
		jdks = GamaLaunches.jdks();
		for (IVMInstall vm : jdks) { jdk.add(vm.getName() + " - " + vm.getInstallLocation()); }
		if (!jdks.isEmpty()) { jdk.select(0); }
		Button edit = new Button(content, SWT.PUSH);
		edit.setText("Edit configuration...");
		edit.addListener(SWT.Selection, e -> {
			try {
				ILaunchConfiguration config = choose();
				if (config != null) {
					DebugUITools.openLaunchConfigurationPropertiesDialog(getShell(), config, "org.eclipse.debug.ui.launchGroup.run");
					refresh();
				}
			} catch (CoreException ex) { error(ex); }
		});
		Button reset = new Button(content, SWT.PUSH);
		reset.setText("Restore generated defaults...");
		reset.addListener(SWT.Selection, e -> {
			if (!MessageDialog.openConfirm(getShell(), "Restore GAMA defaults",
					"Create and select a fresh configuration from the current product? "
					+ "Your existing configuration will be kept, not overwritten.")) { return; }
			try {
				result = generate();
				refresh();
			} catch (CoreException ex) { error(ex); }
		});
		Label explanation = new Label(content, SWT.WRAP);
		explanation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		explanation.setText("Missing JDK 25? Add it under Preferences > Java > Installed JREs.\n"
				+ "Generated launches use separate workspaces. Existing configurations are never overwritten.");
		flavor.addListener(SWT.Selection, e -> refresh());
		configuration.addListener(SWT.Selection, e -> jdk.setEnabled(configuration.getSelectionIndex() == 0));
		refresh();
		return area;
	}

	private Combo combo(final Composite parent, final String label) {
		new Label(parent, SWT.NONE).setText(label);
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return combo;
	}

	private Flavor flavor() { return Flavor.values()[flavor.getSelectionIndex()]; }

	private void refresh() {
		try {
			configurations = GamaLaunches.configurations();
			configuration.removeAll();
			configuration.add("Generate from workspace product");
			ILaunchConfiguration mapped = GamaLaunches.mapped(flavor());
			int index = 0;
			for (int i = 0; i < configurations.size(); i++) {
				configuration.add(configurations.get(i).getName());
				if (configurations.get(i).equals(mapped)) { index = i + 1; }
			}
			configuration.select(index);
			jdk.setEnabled(index == 0);
			setErrorMessage(null);
		} catch (CoreException e) { error(e); }
	}

	private ILaunchConfiguration generate() throws CoreException {
		if (jdk.getSelectionIndex() < 0) {
			throw new CoreException(ResourceUtils.error("No JDK 25 is configured. Add one in Installed JREs first.", null));
		}
		return GamaLaunches.create(flavor(), jdks.get(jdk.getSelectionIndex()));
	}

	private ILaunchConfiguration choose() throws CoreException {
		int index = configuration.getSelectionIndex();
		if (index < 0) { return null; }
		ILaunchConfiguration config = index == 0 ? generate() : configurations.get(index - 1);
		GamaLaunches.map(flavor(), config);
		return config;
	}

	private void error(final CoreException e) {
		ResourceUtils.log(e);
		setErrorMessage(e.getStatus().getMessage());
	}

	@Override
	protected void okPressed() {
		try {
			result = choose();
			if (result != null) { super.okPressed(); }
		} catch (CoreException e) { error(e); }
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Use configuration", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
}
