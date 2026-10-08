/*******************************************************************************************************
 *
 * JsonEditorUtil.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import gama.ui.viewers.json.document.JSONFormatSupport;
import gama.ui.viewers.json.eclipse.preferences.JsonEditorPreferences;
import gama.ui.viewers.json.script.JsonError;

/**
 * The Class JsonEditorUtil.
 */
public class JsonEditorUtil {

	/**
	 * Gets the preferences.
	 *
	 * @return the preferences
	 */
	public static JsonEditorPreferences getPreferences() { return JsonEditorPreferences.getInstance(); }

	/** The error marker helper. */
	private static UnpersistedMarkerHelper errorMarkerHelper = new UnpersistedMarkerHelper("json.error.marker");

	/** The info marker helper. */
	private static UnpersistedMarkerHelper infoMarkerHelper = new UnpersistedMarkerHelper("json.info.marker");

	/**
	 * Log info.
	 *
	 * @param info
	 *            the info
	 */
	public static void logInfo(final String info) {
		getLog().log(new Status(IStatus.INFO, JsonEditorActivator.PLUGIN_ID, info));
	}

	/**
	 * Log warning.
	 *
	 * @param warning
	 *            the warning
	 */
	public static void logWarning(final String warning) {
		getLog().log(new Status(IStatus.WARNING, JsonEditorActivator.PLUGIN_ID, warning));
	}

	/**
	 * Log error.
	 *
	 * @param error
	 *            the error
	 * @param t
	 *            the t
	 */
	public static void logError(final String error, final Throwable t) {
		getLog().log(new Status(IStatus.ERROR, JsonEditorActivator.PLUGIN_ID, error, t));
	}

	/**
	 * Removes the script errors.
	 *
	 * @param editor
	 *            the editor
	 */
	public static void removeScriptErrors(final IEditorPart editor) {
		if (editor == null) return;
		IEditorInput input = editor.getEditorInput();
		if (input == null) return;
		IResource editorResource = input.getAdapter(IResource.class);
		errorMarkerHelper.removeMarkers(editorResource);
		infoMarkerHelper.removeMarkers(editorResource);

	}

	/**
	 * Adds the error marker.
	 *
	 * @param editor
	 *            the editor
	 * @param line
	 *            the line
	 * @param error
	 *            the error
	 */
	public static void addErrorMarker(final IEditorPart editor, final int line, final JsonError error) {
		if (editor == null || error == null) return;

		IEditorInput input = editor.getEditorInput();
		if (input == null) return;
		IResource editorResource = input.getAdapter(IResource.class);
		addErrorMarker(line, error, editorResource);

	}

	/**
	 * Adds the error marker.
	 *
	 * @param line
	 *            the line
	 * @param error
	 *            the error
	 * @param editorResource
	 *            the editor resource
	 */
	public static void addErrorMarker(final int line, final JsonError error, final IResource editorResource) {
		if (editorResource == null || error == null) return;
		try {
			errorMarkerHelper.createErrorMarker(editorResource, error.message, line);
		} catch (CoreException e) {
			logError("Was not able to add error markers", e);
		}
	}

	/**
	 * Adds the info marker.
	 *
	 * @param editor
	 *            the editor
	 * @param line
	 *            the line
	 * @param message
	 *            the message
	 */
	public static void addInfoMarker(final IEditorPart editor, final int line, final String message) {
		if (editor == null || message == null) return;

		IEditorInput input = editor.getEditorInput();
		if (input == null) return;
		IResource editorResource = input.getAdapter(IResource.class);
		addInfoMarker(line, message, editorResource);

	}

	/**
	 * Adds the info marker.
	 *
	 * @param line
	 *            the line
	 * @param message
	 *            the message
	 * @param editorResource
	 *            the editor resource
	 */
	public static void addInfoMarker(final int line, final String message, final IResource editorResource) {
		if (editorResource == null || message == null) return;
		try {
			infoMarkerHelper.createMarker(editorResource, message, line, IMarker.SEVERITY_INFO, -1, -1);
		} catch (CoreException e) {
			logError("Was not able to add error markers", e);
		}
	}

	/**
	 * Adds the error marker.
	 *
	 * @param line
	 *            the line
	 * @param message
	 *            the message
	 * @param input
	 *            the input
	 * @param start
	 *            the start
	 * @param end
	 *            the end
	 */
	public static void addErrorMarker(final int line, final String message, final IEditorInput input, final int start,
			final int end) {
		if (input == null) return;
		IResource editorResource = input.getAdapter(IResource.class);
		if (editorResource == null || message == null) return;
		try {
			errorMarkerHelper.createMarker(editorResource, message, line, IMarker.SEVERITY_ERROR, start, end);
		} catch (CoreException e) {
			logError("Was not able to add error markers", e);
		}
	}

	/**
	 * Gets the log.
	 *
	 * @return the log
	 */
	private static ILog getLog() {
		ILog log = JsonEditorActivator.getDefault().getLog();
		return log;
	}

	/**
	 * Refresh parser settings.
	 */
	public static void refreshParserSettings() {
		JSONFormatSupport.DEFAULT.setAllowComents(JsonEditorPreferences.getInstance().isAllowingComments());
		JSONFormatSupport.DEFAULT
				.setAllowUnquotedControlChars(JsonEditorPreferences.getInstance().isAllowingUnquotedControlChars());
	}

}
