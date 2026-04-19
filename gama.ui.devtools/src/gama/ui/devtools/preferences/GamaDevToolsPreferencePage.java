/*******************************************************************************************************
 *
 * GamaDevToolsPreferencePage.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import gama.ui.devtools.GamaDevToolsActivator;

/**
 * Preference page for the GAMA Developer Tools plugin. Provides user-configurable settings that control the
 * behaviour of code-generation wizards and the Really Refresh action.
 *
 * <p>
 * Settings managed by this page include:
 * </p>
 * <ul>
 * <li>{@link #PREF_DEFAULT_VENDOR} — the default vendor name inserted into generated {@code MANIFEST.MF} files</li>
 * <li>{@link #PREF_DEFAULT_PACKAGE} — the default base Java package for generated code</li>
 * <li>{@link #PREF_PROCESSOR_JAR_PATH} — the path to {@code gama.processor.jar} used by the
 * annotation-processor wiring wizard</li>
 * </ul>
 *
 * @author GAMA Development Team
 * @since 2026
 */
public class GamaDevToolsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Preference key for the default vendor name inserted in generated {@code MANIFEST.MF} files.
	 * Default value: {@code "UMMISCO"}.
	 */
	public static final String PREF_DEFAULT_VENDOR = "gama.devtools.defaultVendor";

	/**
	 * Preference key for the default base Java package used when generating new source files.
	 * Default value: {@code "gama"}.
	 */
	public static final String PREF_DEFAULT_PACKAGE = "gama.devtools.defaultPackage";

	/**
	 * Preference key for the workspace-relative or absolute path to {@code gama.processor.jar}.
	 * Used by the New GAMA Plugin wizard to configure the {@code .factorypath} file.
	 * Default value: {@code "../gama.processor/gama.processor.jar"}.
	 */
	public static final String PREF_PROCESSOR_JAR_PATH = "gama.devtools.processorJarPath";

	/**
	 * Constructs the preference page using the {@link FieldEditorPreferencePage#GRID} layout.
	 */
	public GamaDevToolsPreferencePage() {
		super(GRID);
		setDescription("Settings for the GAMA Developer Tools plugin (code-generation wizards and Really Refresh).");
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Binds this page to the {@link GamaDevToolsActivator} preference store so that all field editors read from and
	 * write to the correct store.
	 * </p>
	 *
	 * @param workbench
	 *            the workbench instance — not used directly
	 */
	@Override
	public void init(final IWorkbench workbench) {
		setPreferenceStore(GamaDevToolsActivator.getInstance().getPreferenceStore());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Declares the three field editors that manage the preferences exposed by this page:
	 * {@link #PREF_DEFAULT_VENDOR}, {@link #PREF_DEFAULT_PACKAGE}, and {@link #PREF_PROCESSOR_JAR_PATH}.
	 * </p>
	 */
	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor(PREF_DEFAULT_VENDOR, "Default vendor name:", getFieldEditorParent()));
		addField(new StringFieldEditor(PREF_DEFAULT_PACKAGE, "Default base package:", getFieldEditorParent()));
		addField(new StringFieldEditor(PREF_PROCESSOR_JAR_PATH, "Path to gama.processor.jar:",
				getFieldEditorParent()));
	}

}
