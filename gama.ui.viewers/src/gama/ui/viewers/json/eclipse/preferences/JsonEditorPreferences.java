/*******************************************************************************************************
 *
 * JsonEditorPreferences.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
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

import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_CREATE_GROUPED_ARRAYS_TRESHOLD;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_CREATE_OUTLINE_FOR_NEW_EDITOR;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_ALLOW_COMMENTS_ENABLED;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_EDITOR_ALLOW_UNQUOTED_CONTROL_CHARS;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_LINK_EDITOR_WITH_OUTLINE;
import static gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferenceConstants.P_VALIDATE_ON_SAVE;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.viewers.json.eclipse.ColorUtil;
import gama.ui.viewers.json.eclipse.JsonEditor;
import gama.ui.viewers.json.eclipse.JsonEditorActivator;

/**
 * The Class JsonEditorPreferences.
 */
public class JsonEditorPreferences {

	/** The instance. */
	private static JsonEditorPreferences INSTANCE = new JsonEditorPreferences();

	/** The store. */
	private IPreferenceStore store;

	/**
	 * Instantiates a new highspeed JSON editor preferences.
	 */
	private JsonEditorPreferences() {
		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, JsonEditorActivator.PLUGIN_ID);
		store.addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				if (event == null) return;
				String property = event.getProperty();
				if (property == null) return;
				ChangeContext context = new ChangeContext();
				for (JsonEditorSyntaxColorPreferenceConstants c : JsonEditorSyntaxColorPreferenceConstants.values()) {
					if (property.equals(c.getId())) {
						context.colorChanged = true;
						break;
					}
				}
				updateColorsInYamlEditors(context);

			}

			private void updateColorsInYamlEditors(final ChangeContext context) {
				if (!context.hasChanges()) return;
				/* inform all Yaml editors about color changes */
				IWorkbenchPage activePage = WorkbenchHelper.getPage();
				if (activePage == null) return;
				IEditorReference[] references = activePage.getEditorReferences();
				for (IEditorReference ref : references) {
					IEditorPart editor = ref.getEditor(false);
					if (editor == null || !(editor instanceof JsonEditor geditor)) { continue; }
					if (context.colorChanged) { geditor.handleColorSettingsChanged(); }
				}
			}
		});
	}

	/**
	 * The Class ChangeContext.
	 */
	private static class ChangeContext {

		/** The color changed. */
		private boolean colorChanged = false;

		/** The validation changed. */
		private final boolean validationChanged = false;

		/**
		 * Checks for changes.
		 *
		 * @return true, if successful
		 */
		private boolean hasChanges() {
			boolean changedAtAll = colorChanged;
			changedAtAll = changedAtAll || validationChanged;
			return changedAtAll;
		}
	}

	/**
	 * Gets the string preference.
	 *
	 * @param id
	 *            the id
	 * @return the string preference
	 */
	public String getStringPreference(final JsonEditorPreferenceConstants id) {
		String data = getPreferenceStore().getString(id.getId());
		if (data == null) { data = ""; }
		return data;
	}

	/**
	 * Gets the boolean preference.
	 *
	 * @param id
	 *            the id
	 * @return the boolean preference
	 */
	public boolean getBooleanPreference(final JsonEditorPreferenceConstants id) {
		boolean data = getPreferenceStore().getBoolean(id.getId());
		return data;
	}

	/**
	 * Sets the boolean preference.
	 *
	 * @param id
	 *            the id
	 * @param value
	 *            the value
	 */
	public void setBooleanPreference(final JsonEditorPreferenceConstants id, final boolean value) {
		getPreferenceStore().setValue(id.getId(), value);
	}

	/**
	 * Checks if is link outline with editor enabled.
	 *
	 * @return true, if is link outline with editor enabled
	 */
	public boolean isLinkOutlineWithEditorEnabled() { return getBooleanPreference(P_LINK_EDITOR_WITH_OUTLINE); }

	/**
	 * Checks if is outline build enabled.
	 *
	 * @return true, if is outline build enabled
	 */
	public boolean isOutlineBuildEnabled() { return getBooleanPreference(P_CREATE_OUTLINE_FOR_NEW_EDITOR); }

	/**
	 * Checks if is validate on save enabled.
	 *
	 * @return true, if is validate on save enabled
	 */
	public boolean isValidateOnSaveEnabled() { return getBooleanPreference(P_VALIDATE_ON_SAVE); }

	/**
	 * Checks if is allowing comments.
	 *
	 * @return true, if is allowing comments
	 */
	public boolean isAllowingComments() { return getBooleanPreference(P_EDITOR_ALLOW_COMMENTS_ENABLED); }

	/**
	 * Checks if is allowing unquoted control chars.
	 *
	 * @return true, if is allowing unquoted control chars
	 */
	public boolean isAllowingUnquotedControlChars() {
		return getBooleanPreference(P_EDITOR_ALLOW_UNQUOTED_CONTROL_CHARS);
	}

	/**
	 * Gets the preference store.
	 *
	 * @return the preference store
	 */
	public IPreferenceStore getPreferenceStore() { return store; }

	/**
	 * Gets the default boolean preference.
	 *
	 * @param id
	 *            the id
	 * @return the default boolean preference
	 */
	public boolean getDefaultBooleanPreference(final JsonEditorPreferenceConstants id) {
		boolean data = getPreferenceStore().getDefaultBoolean(id.getId());
		return data;
	}

	/**
	 * Gets the color.
	 *
	 * @param identifiable
	 *            the identifiable
	 * @return the color
	 */
	public RGB getColor(final PreferenceIdentifiable identifiable) {
		RGB color = PreferenceConverter.getColor(getPreferenceStore(), identifiable.getId());
		return color;
	}

	/**
	 * Returns color as a web color in format "#RRGGBB"
	 *
	 * @param identifiable
	 * @return web color string
	 */
	public String getWebColor(final PreferenceIdentifiable identifiable) {
		RGB color = getColor(identifiable);
		if (color == null) return null;
		String webColor = ColorUtil.convertToHexColor(color);
		return webColor;
	}

	/**
	 * Sets the default color.
	 *
	 * @param identifiable
	 *            the identifiable
	 * @param color
	 *            the color
	 */
	public void setDefaultColor(final PreferenceIdentifiable identifiable, final RGB color) {
		PreferenceConverter.setDefault(getPreferenceStore(), identifiable.getId(), color);
	}

	/**
	 * Gets the single instance of JsonEditorPreferences.
	 *
	 * @return single instance of JsonEditorPreferences
	 */
	public static JsonEditorPreferences getInstance() { return INSTANCE; }

	/**
	 * Gets the groupd arrays treshold.
	 *
	 * @return the groupd arrays treshold
	 */
	public int getGroupdArraysTreshold() {
		return getPreferenceStore().getInt(P_CREATE_GROUPED_ARRAYS_TRESHOLD.getId());
	}

}
