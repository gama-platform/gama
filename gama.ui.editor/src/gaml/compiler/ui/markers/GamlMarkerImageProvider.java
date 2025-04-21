/*******************************************************************************************************
 *
 * GamlMarkerImageProvider.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.internal.ide.IMarkerImageProvider;

import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;

/**
 * The Class GamlMarkerImageProvider.
 */
public class GamlMarkerImageProvider implements IMarkerImageProvider {

	/**
	 * Returns the relative path for the image to be used for displaying an marker in the workbench. This path is
	 * relative to the plugin location
	 *
	 * Returns <code>null</code> if there is no appropriate image.
	 *
	 * @param marker
	 *            The marker to get an image path for.
	 *
	 */
	@Override
	public String getImagePath(final IMarker marker) {
		GamaIcon icon = getImage(marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING));
		if (icon == null) return null;
		return "/icons/" + icon.getCode() + ".png";
	}

	/**
	 * Gets the image.
	 *
	 * @param description
	 *            the description
	 * @return the image
	 */
	public static GamaIcon getImage(final String description) {
		if (description.contains("Errors")) return getImage(IMarker.SEVERITY_ERROR);
		if (description.contains("Warnings")) return getImage(IMarker.SEVERITY_WARNING);
		if (description.contains("Info")) return getImage(IMarker.SEVERITY_INFO);
		if (description.contains("Task")) return getImage(-1);
		return null;
	}

	/**
	 * Gets the image.
	 *
	 * @param severity
	 *            the severity
	 * @return the image
	 */
	public static GamaIcon getImage(final int severity) {
		return switch (severity) {
			case IMarker.SEVERITY_ERROR -> GamaIcon.named(IGamaIcons.MARKER_ERROR);
			case IMarker.SEVERITY_WARNING -> GamaIcon.named(IGamaIcons.MARKER_WARNING);
			case IMarker.SEVERITY_INFO -> GamaIcon.named(IGamaIcons.MARKER_INFO);
			case -1 -> GamaIcon.named(IGamaIcons.MARKER_TASK);
			default -> null;
		};

	}

}
