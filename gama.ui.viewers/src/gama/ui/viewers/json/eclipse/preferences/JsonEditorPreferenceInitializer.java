/*******************************************************************************************************
 *
 * JsonEditorPreferenceInitializer.java, in gama.ui.viewers, is part of the source code of the GAMA modeling
 * and simulation platform (v.2025-03).
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

import static gama.ui.viewers.json.eclipse.JsonEditorColorConstants.BLACK;
import static gama.ui.viewers.json.eclipse.JsonEditorColorConstants.DARK_BLUE;
import static gama.ui.viewers.json.eclipse.JsonEditorColorConstants.DARK_GRAY;
import static gama.ui.viewers.json.eclipse.JsonEditorColorConstants.DARK_GREEN;
import static gama.ui.viewers.json.eclipse.JsonEditorColorConstants.GRAY_JAVA;
import static gama.ui.viewers.json.eclipse.JsonEditorColorConstants.GREEN_JAVA;
import static gama.ui.viewers.json.eclipse.JsonEditorUtil.getPreferences;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_CODE_ASSIST_ADD_KEYWORDS;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_CODE_ASSIST_ADD_SIMPLEWORDS;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_CREATE_GROUPED_ARRAYS_TRESHOLD;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_CREATE_OUTLINE_FOR_NEW_EDITOR;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_ALLOW_COMMENTS_ENABLED;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_ALLOW_UNQUOTED_CONTROL_CHARS;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_AUTO_CREATE_END_BRACKETSY;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_ENCLOSING_BRACKETS;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_HIGHLIGHT_BRACKET_AT_CARET_LOCATION;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_MATCHING_BRACKETS_COLOR;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_MATCHING_BRACKETS_ENABLED;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_LINK_EDITOR_WITH_OUTLINE;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_VALIDATE_ON_SAVE;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_BOOLEAN;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_COMMENT;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_KEY;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_NORMAL_TEXT;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_NULL;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorSyntaxColorPreferenceConstants.COLOR_STRING;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import gama.ui.viewers.json.eclipse.JsonEditorColorConstants;

/**
 * Class used to initialize default preference values.
 */
public class JsonEditorPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		JsonEditorPreferences preferences = getPreferences();
		IPreferenceStore store = preferences.getPreferenceStore();

		/* Outline */
		store.setDefault(P_LINK_EDITOR_WITH_OUTLINE.getId(), true);
		store.setDefault(P_CREATE_OUTLINE_FOR_NEW_EDITOR.getId(), true);
		store.setDefault(P_CREATE_GROUPED_ARRAYS_TRESHOLD.getId(), 100);

		/* PARSING */
		store.setDefault(P_EDITOR_ALLOW_COMMENTS_ENABLED.getId(), true);
		store.setDefault(P_EDITOR_ALLOW_UNQUOTED_CONTROL_CHARS.getId(), false);
		/* VALIDATION */
		store.setDefault(P_VALIDATE_ON_SAVE.getId(), true);

		/* ++++++++++++ */
		/* + Brackets + */
		/* ++++++++++++ */
		/* bracket rendering configuration */
		store.setDefault(P_EDITOR_MATCHING_BRACKETS_ENABLED.getId(), true); // per default matching is enabled, but
																			// without the two other special parts
		store.setDefault(P_EDITOR_HIGHLIGHT_BRACKET_AT_CARET_LOCATION.getId(), false);
		store.setDefault(P_EDITOR_ENCLOSING_BRACKETS.getId(), false);
		store.setDefault(P_EDITOR_AUTO_CREATE_END_BRACKETSY.getId(), true);

		/* +++++++++++++++++ */
		/* + Editor ColorOperators + */
		/* +++++++++++++++++ */
		preferences.setDefaultColor(COLOR_NORMAL_TEXT, BLACK);
		preferences.setDefaultColor(COLOR_COMMENT, GREEN_JAVA);
		preferences.setDefaultColor(COLOR_STRING, DARK_BLUE);
		preferences.setDefaultColor(COLOR_NULL, DARK_GRAY);

		preferences.setDefaultColor(COLOR_KEY, JsonEditorColorConstants.KEYWORD_DEFAULT_PURPLE);
		preferences.setDefaultColor(COLOR_BOOLEAN, DARK_GREEN);

		/* bracket color */
		preferences.setDefaultColor(P_EDITOR_MATCHING_BRACKETS_COLOR, GRAY_JAVA);

		/* +++++++++++++++++++ */
		/* + Code Assistence + */
		/* +++++++++++++++++++ */
		store.setDefault(P_CODE_ASSIST_ADD_KEYWORDS.getId(), true);
		store.setDefault(P_CODE_ASSIST_ADD_SIMPLEWORDS.getId(), true);

	}

}
