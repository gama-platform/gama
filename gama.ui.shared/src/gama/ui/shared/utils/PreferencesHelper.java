/*******************************************************************************************************
 *
 * PreferencesHelper.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.utils;

import static gama.core.common.preferences.GamaPreferences.create;
import static gama.core.common.preferences.GamaPreferences.Interface.APPEARANCE;
import static gama.core.common.preferences.GamaPreferences.Interface.NAME;
import static gama.core.common.preferences.GamaPreferences.Interface.NAVIGATOR;

import org.eclipse.core.runtime.CoreException;

import gama.core.common.interfaces.IGui;
import gama.core.common.preferences.GamaPreferences;
import gama.core.common.preferences.Pref;
import gama.core.runtime.GAMA;
import gama.core.runtime.MemoryUtils;
import gama.core.util.GamaColor;
import gama.gaml.types.IType;
import gama.ui.shared.menus.GamaColorMenu;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.views.GamaPreferencesView;

/**
 * The Class PreferencesHelper.
 */
public class PreferencesHelper {

	/** The Constant CORE_EDITORS_HIGHLIGHT. */
	public static final Pref<Boolean> CORE_EDITORS_HIGHLIGHT =
			create("pref_editors_highlight", "Highlight in yellow the title of value editors when they change", true,
					IType.BOOL, false).in(NAME, APPEARANCE).hidden();

	/** The Constant SHAPEFILE_VIEWER_FILL. */
	public static final Pref<GamaColor> SHAPEFILE_VIEWER_FILL =
			create("pref_shapefile_background_color", "Shapefile viewer fill color", () -> GamaColor.get("lightgray"),
					IType.COLOR, false).in(NAME, APPEARANCE).hidden();

	/** The Constant SHAPEFILE_VIEWER_LINE_COLOR. */
	public static final Pref<GamaColor> SHAPEFILE_VIEWER_LINE_COLOR =
			create("pref_shapefile_line_color", "Shapefile viewer line color", () -> GamaColor.get("black"),
					IType.COLOR, false).in(NAME, APPEARANCE).hidden();

	/** The Constant ERROR_TEXT_COLOR. */
	public static final Pref<GamaColor> ERROR_TEXT_COLOR = create("pref_error_text_color", "Text color of errors",
			() -> GamaColors.toGamaColor(IGamaColors.ERROR.inactive()), IType.COLOR, false)
					.in(GamaPreferences.Runtime.NAME, GamaPreferences.Runtime.ERRORS).hidden();

	/** The Constant WARNING_TEXT_COLOR. */
	public static final Pref<GamaColor> WARNING_TEXT_COLOR = create("pref_warning_text_color", "Text color of warnings",
			() -> GamaColors.toGamaColor(IGamaColors.WARNING.inactive()), IType.COLOR, false)
					.in(GamaPreferences.Runtime.NAME, GamaPreferences.Runtime.ERRORS).hidden();

	/** The Constant IMAGE_VIEWER_BACKGROUND. */
	public static final Pref<GamaColor> IMAGE_VIEWER_BACKGROUND =
			create("pref_image_background_color", "Image viewer background color", () -> GamaColor.get("white"),
					IType.COLOR, false).in(NAME, APPEARANCE).hidden();

	// public static final Pref<GamaFont> BASE_BUTTON_FONT = create("pref_button_font", "Font of buttons and dialogs",
	// () -> new GamaFont(getBaseFont(), SWT.BOLD, baseSize), IType.FONT, false).in(NAME, APPEARANCE)
	// .onChange(GamaFonts::setLabelFont);

	/** The color menu sort. */
	public static final Pref<String> COLOR_MENU_SORT =
			create("pref_menu_colors_sort", "Sort colors menu by", "RGB value", IType.STRING, false)
					.among(GamaColorMenu.SORT_NAMES).activates("menu.colors.reverse", "menu.colors.group")
					.in(NAME, GamaPreferences.Interface.MENUS).onChange(pref -> {
						if (pref.equals(GamaColorMenu.SORT_NAMES[0])) {
							GamaColorMenu.colorComp = GamaColorMenu.byRGB;
						} else if (pref.equals(GamaColorMenu.SORT_NAMES[1])) {
							GamaColorMenu.colorComp = GamaColorMenu.byName;
						} else if (pref.equals(GamaColorMenu.SORT_NAMES[2])) {
							GamaColorMenu.colorComp = GamaColorMenu.byBrightness;
						} else {
							GamaColorMenu.colorComp = GamaColorMenu.byLuminescence;
						}
					});

	/** The color menu reverse. */
	public static final Pref<Boolean> COLOR_MENU_REVERSE =
			create("pref_menu_colors_reverse", "Reverse order", false, IType.BOOL, false)
					.in(NAME, GamaPreferences.Interface.MENUS).onChange(pref -> GamaColorMenu.setReverse(pref ? -1 : 1))
					.hidden();

	/** The color menu group. */
	public static final Pref<Boolean> COLOR_MENU_GROUP =
			create("pref_menu_colors_group", "Group colors", false, IType.BOOL, false)
					.in(NAME, GamaPreferences.Interface.MENUS).onChange(pref -> GamaColorMenu.breakdown = pref)
					.hidden();

	/** The Constant NAVIGATOR_METADATA. */
	public static final Pref<Boolean> NAVIGATOR_METADATA =
			create("pref_navigator_display_metadata", "Display metadata in navigator", true, IType.BOOL, false)
					.in(NAME, NAVIGATOR).onChange(newValue -> {
						final var mgr = WorkbenchHelper.getWorkbench().getDecoratorManager();
						try {
							mgr.setEnabled(IGui.NAVIGATOR_LIGHTWEIGHT_DECORATOR_ID, newValue);
						} catch (final CoreException e) {
							e.printStackTrace();
						}

					}).hidden();
	/** The keep navigator state. */
	public static final Pref<Boolean> KEEP_NAVIGATOR_STATE =
			create("pref_keep_navigator_state", "Maintain the state of the navigator across sessions", true, IType.BOOL,
					false).in(NAME, NAVIGATOR).hidden();
	/** The Constant NAVIGATOR_HIDDEN. */
	public static final Pref<Boolean> NAVIGATOR_HIDDEN =
			create("pref_navigator_display_hidden", "Display hidden files in navigator", false, IType.BOOL, false)
					.in(NAME, NAVIGATOR).onChange(newValue -> {
						GAMA.getGui().refreshNavigator();
					}).hidden();

	/** The Constant NAVIGATOR_OUTLINE. */
	public static final Pref<Boolean> NAVIGATOR_OUTLINE =
			create("pref_navigator_display_outline", "Display the outline of GAML files in navigator", false,
					IType.BOOL, false).in(NAME, NAVIGATOR).onChange(newValue -> {
						GAMA.getGui().refreshNavigator();
					}).hidden();

	/**
	 * Initialize.
	 */
	public static void initialize() {
		final var ini = MemoryUtils.findIniFile();
		Integer writtenMemory = ini == null ? null : MemoryUtils.readMaxMemoryInMegabytes(ini);
		final var text = ini == null || writtenMemory == null
				? "The max. memory allocated in Megabytes. It can be modified in Eclipse (developer version) or in Gama.ini file"
				: "Maximum memory allocated in Mb (requires to restart GAMA)";
		final var maxMemory = writtenMemory == null ? MemoryUtils.maxMemory() : writtenMemory;
		final var p = GamaPreferences.create("pref_memory_max", text, maxMemory, 1, false)
				.in(GamaPreferences.Runtime.NAME, GamaPreferences.Runtime.MEMORY);
		// Force the value to the one contained in the ini file or in the arguments.
		// Trick to force the pref to be read
		p.getValue();
		p.set(maxMemory);
		if (writtenMemory == null) { p.disabled(); }
		p.onChange(newValue -> {
			MemoryUtils.changeMaxMemory(ini, newValue);
			GamaPreferencesView.setRestartRequired();
		});

	}

}
