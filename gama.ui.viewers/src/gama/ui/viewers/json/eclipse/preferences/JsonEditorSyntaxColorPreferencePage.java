/*******************************************************************************************************
 *
 * JsonEditorSyntaxColorPreferencePage.java, in gama.ui.viewers, is part of the source code of the GAMA
 * modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse.preferences;
/*
 * Copyright 2020 Albert Tregnaghi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_BOOLEAN;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_COMMENT;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_KEY;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_NORMAL_TEXT;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_NULL;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_STRING;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import gama.ui.viewers.json.eclipse.JsonEditorColorConstants;
import gama.ui.viewers.json.eclipse.JsonEditorUtil;

/**
 * The Class JsonEditorSyntaxColorPreferencePage.
 */
public class JsonEditorSyntaxColorPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	/**
	 * Instantiates a new highspeed JSON editor syntax color preference page.
	 */
	public JsonEditorSyntaxColorPreferencePage() {
		setPreferenceStore(JsonEditorUtil.getPreferences().getPreferenceStore());
	}

	@Override
	public void init(final IWorkbench workbench) {

	}

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		Map<JsonEditorSyntaxColorPreferenceConstants, ColorFieldEditor> editorMap =
				new HashMap<>();
		for (JsonEditorSyntaxColorPreferenceConstants colorIdentifier : JsonEditorSyntaxColorPreferenceConstants
				.values()) {
			ColorFieldEditor editor =
					new ColorFieldEditor(colorIdentifier.getId(), colorIdentifier.getLabelText(), parent);
			editorMap.put(colorIdentifier, editor);
			addField(editor);
		}
		Button restoreDarkThemeColorsButton = new Button(parent, SWT.PUSH);
		restoreDarkThemeColorsButton.setText("Restore Defaults for Dark Theme");
		restoreDarkThemeColorsButton.setToolTipText(
				"Same as 'Restore Defaults' but for dark themes.\n Editor makes just a suggestion, you still have to apply or cancel the settings.");
		restoreDarkThemeColorsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {

				/* editor colors */
				changeColor(editorMap, COLOR_NORMAL_TEXT, JsonEditorColorConstants.GRAY_JAVA);

				changeColor(editorMap, COLOR_STRING, JsonEditorColorConstants.MIDDLE_ORANGE);
				changeColor(editorMap, COLOR_COMMENT, JsonEditorColorConstants.GREEN_JAVA);
				changeColor(editorMap, COLOR_NULL, JsonEditorColorConstants.BRIGHT_CYAN);

				changeColor(editorMap, COLOR_KEY, JsonEditorColorConstants.DARK_THEME_LIGHT_BLUE);
				changeColor(editorMap, COLOR_BOOLEAN, JsonEditorColorConstants.DARK_THEME_LIGHT_ORANGE);

			}

			private void changeColor(final Map<JsonEditorSyntaxColorPreferenceConstants, ColorFieldEditor> editorMap,
					final JsonEditorSyntaxColorPreferenceConstants colorId, final RGB rgb) {
				editorMap.get(colorId).getColorSelector().setColorValue(rgb);
			}

		});

	}

}