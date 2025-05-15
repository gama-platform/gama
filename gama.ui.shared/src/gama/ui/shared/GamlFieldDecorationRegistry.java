/*******************************************************************************************************
 *
 * GamlFieldDecorationRegistry.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared;

import static org.eclipse.jface.resource.JFaceResources.getImageRegistry;

import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.JFaceResources;

import gama.dev.DEBUG;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;

/**
 * The class GamlFieldDecorationRegistry.
 *
 * @author drogoul
 * @since 19 avr. 2025
 *
 */
public class GamlFieldDecorationRegistry extends FieldDecorationRegistry {

	static {
		DEBUG.OFF();
	}

	/** The error fd. */
	static FieldDecoration ERROR_FD = new FieldDecoration(GamaIcon.named(IGamaIcons.MARKER_ERROR).image(), "Errors");

	/** The warning fd. */
	static FieldDecoration WARNING_FD =
			new FieldDecoration(GamaIcon.named(IGamaIcons.MARKER_WARNING).image(), "Warnings");

	/** The info fd. */
	static FieldDecoration INFO_FD = new FieldDecoration(GamaIcon.named(IGamaIcons.MARKER_INFO).image(), "Info");

	/**
	 * Instantiates a new gaml field decoration registry.
	 */
	public GamlFieldDecorationRegistry() {

		registerFieldDecoration(DEC_CONTENT_PROPOSAL,
				JFaceResources.getString("FieldDecorationRegistry.contentAssistMessage"), //$NON-NLS-1$
				"org.eclipse.jface.fieldassist.IMG_DEC_FIELD_CONTENT_PROPOSAL", getImageRegistry());
		registerFieldDecoration(DEC_REQUIRED, JFaceResources.getString("FieldDecorationRegistry.requiredFieldMessage"), //$NON-NLS-1$
				"org.eclipse.jface.fieldassist.IMG_DEC_FIELD_REQUIRED", getImageRegistry());
		registerFieldDecoration(DEC_ERROR, JFaceResources.getString("FieldDecorationRegistry.errorMessage"),
				GamaIcon.named(IGamaIcons.MARKER_ERROR).image());
		registerFieldDecoration(DEC_ERROR_QUICKFIX, JFaceResources.getString("FieldDecorationRegistry.errorMessage"),
				GamaIcon.named(IGamaIcons.MARKER_ERROR).image());
		registerFieldDecoration(DEC_WARNING, null, GamaIcon.named(IGamaIcons.MARKER_WARNING).image());
		registerFieldDecoration(DEC_INFORMATION, null, GamaIcon.named(IGamaIcons.MARKER_INFO).image());
	}

	@Override
	public FieldDecoration getFieldDecoration(final String id) {
		DEBUG.OUT("FD : " + id);
		return switch (id) {
			case DEC_ERROR -> ERROR_FD;
			case DEC_WARNING -> WARNING_FD;
			case DEC_INFORMATION -> INFO_FD;
			default -> super.getFieldDecoration(id);
		};
	}

}
