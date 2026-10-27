/*******************************************************************************************************
 *
 * DelegateHighlightingConfiguration.java, in gama.ui.editor, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.highlight;

import static gama.api.utils.prefs.GamaPreferences.create;
import static gama.ui.shared.resources.GamaColors.toGamaColor;
import static java.util.Map.entry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.xtext.ide.editor.syntaxcoloring.HighlightingStyles;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor;
import org.eclipse.xtext.ui.editor.utils.TextStyle;

import gama.api.gaml.types.IType;
import gama.api.types.color.IColor;
import gama.api.types.font.IFont;
import gama.api.utils.prefs.GamaPreferences.Theme;
import gama.api.utils.prefs.Pref;

/**
 * The class IGamlHighlightingConfiguration.
 *
 * @author drogoul
 * @since 1 déc. 2020
 *
 */
@SuppressWarnings ("unchecked")
public abstract class DelegateHighlightingConfiguration implements IHighlightingConfiguration {

	/** The Constant OPERATOR_ID. */
	public static final String OPERATOR_ID = "binary";

	/** The Constant RESERVED_ID. */
	public static final String RESERVED_ID = "reserved";

	/** The Constant FACET_ID. */
	public static final String FACET_ID = "facet";

	/** The Constant FIELD_ID. */
	public static final String FIELD_ID = "field";

	/** The Constant GLOBAL_ID. */
	public static final String GLOBAL_ID = "global";

	/** The Constant VARIABLE_ID. */
	public static final String VARIABLE_ID = "variable";

	/** The Constant VARDEF_ID. */
	public static final String VARDEF_ID = "varDef";

	/** The Constant TYPE_ID. */
	public static final String TYPE_ID = "typeDef";

	/** The Constant ASSIGN_ID. */
	public static final String ASSIGN_ID = "assignment";

	/** The Constant UNIT_ID. */
	public static final String UNIT_ID = "unit";

	/** The Constant PRAGMA_ID. */
	public static final String PRAGMA_ID = "pragma";

	/** The Constant KEYWORD_ID. */
	public static final String KEYWORD_ID = HighlightingStyles.KEYWORD_ID;

	/** The Constant PUNCTUATION_ID. */
	public static final String PUNCTUATION_ID = HighlightingStyles.PUNCTUATION_ID;

	/** The Constant COMMENT_ID. */
	public static final String COMMENT_ID = HighlightingStyles.COMMENT_ID;

	/** The Constant STRING_ID. */
	public static final String STRING_ID = HighlightingStyles.STRING_ID;

	/** The Constant NUMBER_ID. */
	public static final String NUMBER_ID = HighlightingStyles.NUMBER_ID;

	/** The Constant DEFAULT_ID. */
	public static final String DEFAULT_ID = HighlightingStyles.DEFAULT_ID;

	/** The Constant INVALID_TOKEN_ID. */
	public static final String INVALID_TOKEN_ID = HighlightingStyles.INVALID_TOKEN_ID;

	/** The Constant TASK_ID. */
	public static final String TASK_ID = HighlightingStyles.TASK_ID;

	/** The theme. */
	String theme;

	/** The listeners. */
	private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();

	/** The default colors. */
	final Map<String, RGB> defaultColors = new HashMap<>();

	/** The color preferences. */
	final Map<String, Pref<IColor>> colorPreferences = new HashMap<>();

	/** The preference controlling whether to use default colors. */
	Pref<Boolean> useDefaultPref;

	/**
	 * Adds a listener to be notified when syntax coloring changes.
	 *
	 * @param r
	 *            the listener
	 */
	public static void addListener(final Runnable r) {
		LISTENERS.add(r);
	}

	/**
	 * Removes a listener.
	 *
	 * @param r
	 *            the listener
	 */
	public static void removeListener(final Runnable r) {
		LISTENERS.remove(r);
	}

	/**
	 * Notifies listeners that syntax coloring has changed.
	 */
	public static void notifyListeners() {
		LISTENERS.forEach(Runnable::run);
	}

	/** The Constant TITLES. */
	// final Map<String, Pref<?>> preferences = new HashMap<>();
	final static Map<String, String> TITLES = Map.ofEntries(entry(KEYWORD_ID, "Statement keyword"),
			entry(PUNCTUATION_ID, "Punctuation character"), entry(OPERATOR_ID, "Operator & action call"),
			entry(RESERVED_ID, "Reserved symbol"), entry(COMMENT_ID, "Comment"), entry(STRING_ID, "String"),
			entry(NUMBER_ID, "Literal constant"), entry(DEFAULT_ID, "Default"), entry(FACET_ID, "Facet key"),
			entry(VARIABLE_ID, "Variable"), entry(VARDEF_ID, "Variable definition"), entry(TYPE_ID, "Type"),
			entry(ASSIGN_ID, "Assignment sign"), entry(UNIT_ID, "Unit & constant"), entry(TASK_ID, "Task"),
			entry(PRAGMA_ID, "Pragma"), entry(FIELD_ID, "Field"));

	/** The styles. */
	final Map<String, TextStyle> styles = Map.ofEntries(entry(KEYWORD_ID, keywordTextStyle()),
			entry(PUNCTUATION_ID, punctuationTextStyle()), entry(OPERATOR_ID, operatorTextStyle()),
			entry(RESERVED_ID, reservedTextStyle()), entry(COMMENT_ID, commentTextStyle()),
			entry(STRING_ID, stringTextStyle()), entry(NUMBER_ID, numberTextStyle()),
			entry(DEFAULT_ID, defaultTextStyle()), entry(FACET_ID, facetTextStyle()),
			entry(VARIABLE_ID, variableTextStyle()), entry(VARDEF_ID, varDefTextStyle()),
			entry(TYPE_ID, typeTextStyle()), entry(ASSIGN_ID, assignTextStyle()), entry(UNIT_ID, unitTextStyle()),
			entry(TASK_ID, taskTextStyle()), entry(PRAGMA_ID, pragmaTextStyle()), entry(FIELD_ID, fieldTextStyle()));

	/**
	 * New style.
	 *
	 * @param style
	 *            the style
	 * @param red
	 *            the red
	 * @param green
	 *            the green
	 * @param blue
	 *            the blue
	 * @return the text style
	 */
	TextStyle newStyle(final int style, final int red, final int green, final int blue) {
		final var textStyle = newStyle(style);
		textStyle.setColor(new RGB(red, green, blue));
		return textStyle;
	}

	/**
	 * New style.
	 *
	 * @param style
	 *            the style
	 * @return the text style
	 */
	TextStyle newStyle(final int style) {
		final var textStyle = newStyle();
		textStyle.setStyle(style);
		return textStyle;
	}

	/**
	 * New style.
	 *
	 * @return the text style
	 */
	TextStyle newStyle() {
		return defaultTextStyle().copy();
	}

	/**
	 * Instantiates a new delegate highlighting configuration.
	 *
	 * @param themeName
	 *            the theme name
	 */
	public DelegateHighlightingConfiguration(final String themeName) {
		theme = themeName;
		configurePreferences();
	}

	/**
	 * Configure.
	 *
	 * @param acceptor
	 *            the acceptor
	 * @param key
	 *            the key
	 */
	private void configure(final IHighlightingConfigurationAcceptor acceptor, final String key) {
		acceptor.acceptDefaultHighlighting(key, TITLES.get(key), styles.get(key));
	}

	@Override
	public void configure(final IHighlightingConfigurationAcceptor acceptor) {
		TITLES.keySet().forEach(each -> configure(acceptor, each));
	}

	/**
	 * Configure preferences.
	 */
	public void configurePreferences() {
		// Capture default compiled-in colors
		styles.forEach((id, style) -> {
			final RGB rgb = style.getColor();
			defaultColors.put(id, new RGB(rgb.red, rgb.green, rgb.blue));
		});

		final String[] colorKeys = TITLES.keySet().stream()
				.map(id -> "pref_" + id + "_color_" + theme)
				.toArray(String[]::new);

		final var useDefaultKey = "pref_syntax_use_default_colors_" + theme;
		useDefaultPref = create(useDefaultKey, "Use default syntax colors", true, IType.BOOL, false)
				.in(Theme.NAME, "Syntax coloring (" + theme + " theme)")
				.deactivates(colorKeys)
				.onChange(this::applyAllColors);

		// we create and/or read the preferences
		configure((id, name, style) -> {
			final var key = "pref_" + id + "_color_" + theme;
			final var pref2 = create(key, theme + " theme " + name + " color",
					() -> toGamaColor(defaultColors.get(id)), IType.COLOR, false)
					.in(Theme.NAME, "Syntax coloring (" + theme + " theme)")
					.onChange(color -> {
						if (!Boolean.TRUE.equals(useDefaultPref.getValue())) {
							applyColor(id, name, style, color);
							notifyListeners();
						}
					});
			colorPreferences.put(id, pref2);
		});

		applyAllColors(useDefaultPref.getValue());
	}

	/**
	 * Apply all colors.
	 *
	 * @param useDefaults
	 *            whether to use default colors or user-chosen colors
	 */
	public void applyAllColors(final Boolean useDefaults) {
		final boolean defaults = Boolean.TRUE.equals(useDefaults);
		for (final String id : TITLES.keySet()) {
			final TextStyle style = styles.get(id);
			if (style == null) continue;
			if (defaults) {
				final RGB rgb = defaultColors.get(id);
				if (rgb != null) { style.setColor(new RGB(rgb.red, rgb.green, rgb.blue)); }
			} else {
				final Pref<IColor> pref = colorPreferences.get(id);
				if (pref != null) {
					final IColor color = pref.getValue();
					if (color != null) {
						style.setColor(new RGB(color.red(), color.green(), color.blue()));
					}
				}
			}
		}
		notifyListeners();
	}

	/**
	 * Apply font.
	 *
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @param style
	 *            the style
	 * @param font
	 *            the font
	 */
	private void applyFont(final String id, final String name, final TextStyle style, final IFont font) {
		if (font != null) { style.setFontData(new FontData(font.getFontName(), font.getSize(), font.getStyle())); }
		// acceptDefaultHighlighting(id, name, style);
	}

	/**
	 * Apply color.
	 *
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @param style
	 *            the style
	 * @param color
	 *            the color
	 */
	private void applyColor(final String id, final String name, final TextStyle style, final IColor color) {
		if (color != null) {
			style.setColor(new RGB(color.red(), color.green(), color.blue()));
		}
		// acceptDefaultHighlighting(id, name, style);
	}

	/**
	 * Gets the use default pref.
	 *
	 * @return the use default pref
	 */
	public Pref<Boolean> getUseDefaultPref() {
		return useDefaultPref;
	}

	/**
	 * Facet text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle facetTextStyle();

	/**
	 * Pragma text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle pragmaTextStyle();

	/**
	 * Type text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle typeTextStyle();

	/**
	 * Assign text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle assignTextStyle();

	/**
	 * Variable text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle variableTextStyle();

	/**
	 * Field text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle fieldTextStyle();

	/**
	 * Operator text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle operatorTextStyle();

	/**
	 * Reserved text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle reservedTextStyle();

	/**
	 * Error text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle errorTextStyle();

	/**
	 * Unit text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle unitTextStyle();

	/**
	 * Number text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle numberTextStyle();

	/**
	 * Keyword text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle keywordTextStyle();

	/**
	 * Punctuation text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle punctuationTextStyle();

	/**
	 * String text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle stringTextStyle();

	/**
	 * Var def text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle varDefTextStyle();

	/**
	 * Task text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle taskTextStyle();

	/**
	 * Comment text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle commentTextStyle();

	/**
	 * Default text style.
	 *
	 * @return the text style
	 */
	abstract TextStyle defaultTextStyle();

}
