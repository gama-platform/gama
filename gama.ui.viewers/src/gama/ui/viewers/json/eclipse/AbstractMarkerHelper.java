/*******************************************************************************************************
 *
 * AbstractMarkerHelper.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.MarkerUtilities;

import gama.ui.viewers.json.SimpleStringUtils;

/**
 * The Class AbstractMarkerHelper.
 */
abstract class AbstractMarkerHelper {

	/** The marker type. */
	protected String markerType;

	/**
	 * Find marker.
	 *
	 * @param resource
	 *            the resource
	 * @param message
	 *            the message
	 * @param lineNumber
	 *            the line number
	 * @param type
	 *            the type
	 * @return the i marker
	 * @throws CoreException
	 *             the core exception
	 */
	private IMarker findMarker(final IResource resource, final String message, final int lineNumber, final String type)
			throws CoreException {
		IMarker[] marker = resource.findMarkers(type, true, IResource.DEPTH_ZERO);
		for (IMarker currentMarker : marker) {
			if (currentMarker == null) { continue; }
			Object lineNrAttribute = currentMarker.getAttribute(IMarker.LINE_NUMBER);
			String markerLineNumber = null;
			if (lineNrAttribute != null) { markerLineNumber = lineNrAttribute.toString(); }
			Object messageAttribute = currentMarker.getAttribute(IMarker.MESSAGE);
			String markerMessage = null;
			if (messageAttribute != null) { markerMessage = messageAttribute.toString(); }
			boolean sameMessageAndLineNr = SimpleStringUtils.equals(markerLineNumber, String.valueOf(lineNumber))
					&& SimpleStringUtils.equals(markerMessage, message);
			if (sameMessageAndLineNr) return currentMarker;
		}
		return null;
	}

	/**
	 * Creates the error marker.
	 *
	 * @param resource
	 *            the resource
	 * @param message
	 *            the message
	 * @param lineNumber
	 *            the line number
	 * @throws CoreException
	 *             the core exception
	 */
	public void createErrorMarker(final IResource resource, final String message, final int lineNumber)
			throws CoreException {
		createErrorMarker(resource, message, lineNumber, -1, -1);
	}

	/**
	 * Creates the error marker.
	 *
	 * @param resource
	 *            the resource
	 * @param message
	 *            the message
	 * @param lineNumber
	 *            the line number
	 * @param charStart
	 *            the char start
	 * @param charEnd
	 *            the char end
	 * @throws CoreException
	 *             the core exception
	 */
	public void createErrorMarker(final IResource resource, final String message, final int lineNumber,
			final int charStart, final int charEnd) throws CoreException {
		createMarker(resource, message, lineNumber, markerType, IMarker.SEVERITY_ERROR, charStart, charEnd);
	}

	/**
	 * Creates the marker.
	 *
	 * @param resource
	 *            the resource
	 * @param message
	 *            the message
	 * @param lineNumber
	 *            the line number
	 * @param severity
	 *            the severity
	 * @param charStart
	 *            the char start
	 * @param charEnd
	 *            the char end
	 * @throws CoreException
	 *             the core exception
	 */
	public void createMarker(final IResource resource, final String message, final int lineNumber, final int severity,
			final int charStart, final int charEnd) throws CoreException {
		this.createMarker(resource, message, lineNumber, markerType, severity, charStart, charEnd);
	}

	/**
	 * Creates the marker.
	 *
	 * @param resource
	 *            the resource
	 * @param message
	 *            the message
	 * @param lineNumber
	 *            the line number
	 * @param markerType
	 *            the marker type
	 * @param severity
	 *            the severity
	 * @param charStart
	 *            the char start
	 * @param charEnd
	 *            the char end
	 * @throws CoreException
	 *             the core exception
	 */
	private void createMarker(final IResource resource, final String message, int lineNumber, final String markerType,
			final int severity, final int charStart, final int charEnd) throws CoreException {
		if (lineNumber <= 0) { lineNumber = 1; }
		IMarker marker = findMarker(resource, message, lineNumber, markerType);
		if (marker == null) {
			HashMap<String, Object> map = new HashMap<>();
			map.put(IMarker.SEVERITY, Integer.valueOf(severity));
			map.put(IMarker.LOCATION, resource.getFullPath().toOSString());
			map.put(IMarker.MESSAGE, message);
			MarkerUtilities.setLineNumber(map, lineNumber);
			MarkerUtilities.setMessage(map, message);
			if (charStart != -1) {
				MarkerUtilities.setCharStart(map, charStart);
				MarkerUtilities.setCharEnd(map, charEnd);
			}
			internalCreateMarker(resource, map, markerType);
		}
	}

	/**
	 * Creates a marker on the given resource with the given type and attributes.
	 * <p>
	 * This method modifies the workspace (progress is not reported to the user).
	 * </p>
	 *
	 * @param resource
	 *            the resource
	 * @param attributes
	 *            the attribute map
	 * @param markerType
	 *            the type of marker
	 * @throws CoreException
	 *             if this method fails
	 * @see IResource#createMarker(java.lang.String)
	 */
	private void internalCreateMarker(final IResource resource, final Map<String, Object> attributes,
			final String markerType) throws CoreException {

		IWorkspaceRunnable r = monitor -> {
			IMarker marker = resource.createMarker(markerType);
			marker.setAttributes(attributes);
			handleMarkerAdded(marker);

		};

		resource.getWorkspace().run(r, null, IWorkspace.AVOID_UPDATE, null);
	}

	/**
	 * Handle marker added.
	 *
	 * @param marker
	 *            the marker
	 */
	protected void handleMarkerAdded(final IMarker marker) {
		/* do nothing per default */
	}

	/**
	 * Removes all markers from this file having defined marker type
	 *
	 * @param resource
	 */
	public void removeMarkers(final IResource resource) {
		if (resource == null) return;
		removeMarkers(resource, markerType);

	}

	/**
	 * Removes the markers.
	 *
	 * @param resource
	 *            the resource
	 * @param markerType
	 *            the marker type
	 * @return the i marker[]
	 */
	private IMarker[] removeMarkers(final IResource resource, final String markerType) {
		if (resource == null) /* maybe sync problem - guard close */
			return new IMarker[] {};
		IMarker[] tasks = null;

		try {
			tasks = resource.findMarkers(markerType, true, IResource.DEPTH_ZERO);
			for (IMarker task : tasks) { task.delete(); }

		} catch (CoreException e) {
			EclipseUtil.logError("Was not able to delete markers", e);
		}
		if (tasks == null) { tasks = new IMarker[] {}; }
		return tasks;
	}

}