/*******************************************************************************************************
 *
 * AutoStartup.java, in gama.ui.shared.modeling, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui;

import static org.eclipse.jface.preference.PreferenceConverter.setValue;
import static org.eclipse.jface.resource.JFaceResources.TEXT_FONT;

import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import gama.core.common.preferences.GamaPreferences;
import gama.core.util.GamaColor;
import gama.core.util.GamaFont;
import gama.dev.DEBUG;
import gama.ui.shared.access.HeapControl;
import gaml.compiler.ui.editor.GamlEditorBindings;
import gaml.compiler.ui.reference.OperatorsReferenceMenu;

/**
 * The Class AutoStartup.
 */
public class AutoStartup implements IStartup {

	static {
		DEBUG.OFF();
	}

	/**
	 * Gets the default background.
	 *
	 * @return the default background
	 */
	private static GamaColor getDefaultBackground() {
		EditorsPlugin.getDefault().getPreferenceStore()
				.setValue(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, false);
		final RGB rgb = PreferenceConverter.getColor(EditorsPlugin.getDefault().getPreferenceStore(),
				AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
		return GamaColor.get(rgb.red, rgb.green, rgb.blue);
	}

	/**
	 * Gets the default font data.
	 *
	 * @return the default font data
	 */
	public static GamaFont getDefaultFontData() {
		final FontData fd = PreferenceConverter.getFontData(EditorsPlugin.getDefault().getPreferenceStore(), TEXT_FONT);
		return new GamaFont(fd.getName(), fd.getStyle(), fd.getHeight());
	}

	@Override
	public void earlyStartup() {
		DEBUG.OUT("Startup of editor plugin begins");
		GamaPreferences.Modeling.EDITOR_BASE_FONT.init(AutoStartup::getDefaultFontData).onChange(font -> {
			try {
				final FontData newValue = new FontData(font.getName(), font.getSize(), font.getStyle());
				setValue(EditorsPlugin.getDefault().getPreferenceStore(), TEXT_FONT, newValue);
			} catch (final Exception e) {}
		});
		GamaPreferences.Modeling.EDITOR_BACKGROUND_COLOR.init(AutoStartup::getDefaultBackground).onChange(c -> {
			final RGB rgb = new RGB(c.getRed(), c.getGreen(), c.getBlue());
			PreferenceConverter.setValue(EditorsPlugin.getDefault().getPreferenceStore(),
					AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, rgb);
			GamaPreferences.Modeling.OPERATORS_MENU_SORT
					.onChange(newValue -> OperatorsReferenceMenu.byName = "Name".equals(newValue));
		});
		// GamlRuntimeModule.staticInitialize();
		GamlEditorBindings.install();
		HeapControl.install();
		DEBUG.OUT("Startup of editor plugin finished");
	}

}
