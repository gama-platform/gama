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

import static gama.ui.devtools.preferences.DevToolsPreferences.*;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page of the GAMA developer tools (Window > Preferences > GAMA Developer Tools).
 */
public class GamaDevToolsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/** Constructor. */
	public GamaDevToolsPreferencePage() {
		super(GRID);
		setDescription("Settings of the GAMA developer tools (wizards and 'Clean and Rebuild').");
	}

	@Override
	public void init(final IWorkbench workbench) {
		setPreferenceStore(DevToolsPreferences.store());
	}

	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor(DEFAULT_VENDOR, "Default vendor of new plugins:", getFieldEditorParent()));
		addField(new StringFieldEditor(DEFAULT_PREFIX, "Default prefix of new plugin ids:", getFieldEditorParent()));
	}

}
