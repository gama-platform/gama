/*******************************************************************************************************
 *
 * AutoStartup.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
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

import gama.api.types.color.IColor;
import gama.api.types.font.GamaFontFactory;
import gama.api.types.font.IFont;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;
import gama.ui.application.workbench.ThemeHelper;
import gama.ui.shared.access.HeapControl;
import gama.ui.shared.utils.SyntaxHighlightingPreferences;
import gama.ui.shared.utils.WorkbenchHelper;
import gaml.compiler.ui.highlight.DelegateHighlightingConfiguration;

/**
 * The Class AutoStartup.
 */
public class AutoStartup implements IStartup {

	static {
		DEBUG.OFF();
	}

	/** Applies the active palette's background to open editors and their rulers. */
	private static void applyEditorBackground() {
		final var store = EditorsPlugin.getDefault().getPreferenceStore();
		store.setValue(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, false);
		final IColor color = SyntaxHighlightingPreferences.current().backgrounds()
				.get(ThemeHelper.isDark() ? "dark" : "light");
		PreferenceConverter.setValue(store, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND,
				new RGB(color.red(), color.green(), color.blue()));
	}

	/**
	 * Gets the default font data.
	 *
	 * @return the default font data
	 */
	public static IFont getDefaultFontData() {
		final FontData fd = PreferenceConverter.getFontData(EditorsPlugin.getDefault().getPreferenceStore(), TEXT_FONT);
		return GamaFontFactory.createFont(fd.getName(), fd.getStyle(), fd.getHeight());
	}

	@Override
	public void earlyStartup() {
		DEBUG.OUT("Startup of editor plugin begins");
		DelegateHighlightingConfiguration.addListener(
				() -> WorkbenchHelper.asyncRun(AutoStartup::applyEditorBackground));
		ThemeHelper.addListener(light -> WorkbenchHelper.asyncRun(AutoStartup::applyEditorBackground));
		WorkbenchHelper.asyncRun(AutoStartup::applyEditorBackground);

		GamaPreferences.Modeling.EDITOR_BASE_FONT.init(AutoStartup::getDefaultFontData).onChange(font -> {
			try {
				final FontData newValue = new FontData(font.getName(), font.getSize(), font.getStyle());
				setValue(EditorsPlugin.getDefault().getPreferenceStore(), TEXT_FONT, newValue);
			} catch (final Exception e) {}
		});
		// GamlRuntimeModule.staticInitialize();
		// GamlEditorBindings.install();
		HeapControl.install();
		DEBUG.OUT("Startup of editor plugin finished");
	}

}
