/*******************************************************************************************************
 *
 * DelegateHighlightingConfiguration.java, in gama.ui.editor, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.highlight;

import static java.util.Map.entry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.xtext.ide.editor.syntaxcoloring.HighlightingStyles;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor;
import org.eclipse.xtext.ui.editor.utils.TextStyle;

import gama.ui.shared.utils.SyntaxHighlightingPreferences;

/** Adapts the shared, file-backed syntax preferences to Xtext styles. */
public abstract class DelegateHighlightingConfiguration implements IHighlightingConfiguration {

	public static final String OPERATOR_ID = "binary";
	public static final String RESERVED_ID = "reserved";
	public static final String FACET_ID = "facet";
	public static final String FIELD_ID = "field";
	public static final String GLOBAL_ID = "global";
	public static final String VARIABLE_ID = "variable";
	public static final String VARDEF_ID = "varDef";
	public static final String TYPE_ID = "typeDef";
	public static final String ASSIGN_ID = "assignment";
	public static final String UNIT_ID = "unit";
	public static final String PRAGMA_ID = "pragma";
	public static final String KEYWORD_ID = HighlightingStyles.KEYWORD_ID;
	public static final String PUNCTUATION_ID = HighlightingStyles.PUNCTUATION_ID;
	public static final String COMMENT_ID = HighlightingStyles.COMMENT_ID;
	public static final String STRING_ID = HighlightingStyles.STRING_ID;
	public static final String NUMBER_ID = HighlightingStyles.NUMBER_ID;
	public static final String DEFAULT_ID = HighlightingStyles.DEFAULT_ID;
	public static final String INVALID_TOKEN_ID = HighlightingStyles.INVALID_TOKEN_ID;
	public static final String TASK_ID = HighlightingStyles.TASK_ID;

	private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();

	static final Map<String, String> TITLES = new LinkedHashMap<>();

	static {
		for (final var entry : List.of(entry(DEFAULT_ID, "Default"), entry(KEYWORD_ID, "Statement keyword"),
				entry(PUNCTUATION_ID, "Punctuation character"), entry(OPERATOR_ID, "Operator & action call"),
				entry(RESERVED_ID, "Reserved symbol"), entry(COMMENT_ID, "Comment"), entry(STRING_ID, "String"),
				entry(NUMBER_ID, "Literal constant"), entry(FACET_ID, "Facet key"), entry(VARIABLE_ID, "Variable"),
				entry(VARDEF_ID, "Variable definition"), entry(TYPE_ID, "Type"), entry(ASSIGN_ID, "Assignment sign"),
				entry(UNIT_ID, "Unit & constant"), entry(TASK_ID, "Task"), entry(PRAGMA_ID, "Pragma"),
				entry(FIELD_ID, "Field"))) {
			TITLES.put(entry.getKey(), entry.getValue());
		}
		SyntaxHighlightingPreferences.initialize(TITLES);
		SyntaxHighlightingPreferences.onChange(DelegateHighlightingConfiguration::notifyListeners);
	}

	private final String theme;

	protected DelegateHighlightingConfiguration(final String theme) {
		this.theme = theme;
	}

	public static void addListener(final Runnable listener) {
		LISTENERS.add(listener);
	}

	public static void removeListener(final Runnable listener) {
		LISTENERS.remove(listener);
	}

	public static void notifyListeners() {
		LISTENERS.forEach(Runnable::run);
	}

	@Override
	public void configure(final IHighlightingConfigurationAcceptor acceptor) {
		final var configuration = SyntaxHighlightingPreferences.current().themes().get(theme);
		TITLES.forEach((id, title) -> {
			final var setting = configuration.get(id);
			final var style = new TextStyle();
			final var color = setting.color();
			style.setColor(new RGB(color.red(), color.green(), color.blue()));
			style.setStyle((setting.bold() ? SWT.BOLD : SWT.NORMAL) | (setting.italic() ? SWT.ITALIC : SWT.NORMAL));
			acceptor.acceptDefaultHighlighting(id, title, style);
		});
	}
}
