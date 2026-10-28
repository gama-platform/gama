/*******************************************************************************************************
 *
 * GamlKeywordDialog.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.keywords;

import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

import gama.ui.devtools.GamaDevToolsActivator;
import gama.ui.devtools.keywords.GamlKeywordIndex.Entry;
import gama.ui.devtools.keywords.GamlKeywordIndex.Kind;

/**
 * An "Open Type"-like dialog listing the GAML keywords (operators, statements, skills, attributes, facets...) declared
 * in the Java sources of the workspace, with the Java elements that implement them.
 *
 * <p>
 * The pattern is matched against the beginning of the GAML names (case-insensitive); {@code *} and {@code ?} can be
 * used as wildcards when the pattern also contains a letter or a digit (otherwise, they designate the operators
 * {@code *}...). The pattern can be prefixed by a kind followed by a colon to restrict the search, e.g.
 * {@code facet:speed}, {@code op:+} or {@code action:go}.
 * </p>
 */
public class GamlKeywordDialog extends FilteredItemsSelectionDialog {

	private static final String SETTINGS = GamlKeywordDialog.class.getName();

	/** The raw text of the pattern, as typed by the user (used to sort the exact matches first). */
	private String currentName = "";

	/**
	 * Creates the dialog.
	 */
	public GamlKeywordDialog(final Shell shell) {
		super(shell, true);
		setTitle("Find GAML Keyword Implementation");
		setMessage("Enter a GAML keyword (prefix with 'kind:' to restrict, e.g. 'facet:speed', 'op:+'; * and ? are "
				+ "wildcards):");
		KeywordLabelProvider labels = new KeywordLabelProvider();
		setListLabelProvider(labels);
		setDetailsLabelProvider(new LabelProvider() {
			@Override
			public String getText(final Object element) {
				return element instanceof Entry e ? e.path() + ":" + e.line() + " (" + e.plugin() + ")" : null;
			}

			@Override
			public Image getImage(final Object element) {
				return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CUNIT);
			}
		});
		setSelectionHistory(new EntryHistory());
	}

	@Override
	protected Control createExtendedContentArea(final Composite parent) {
		return null;
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		IDialogSettings root = GamaDevToolsActivator.getInstance().getDialogSettings();
		IDialogSettings section = root.getSection(SETTINGS);
		return section == null ? root.addNewSection(SETTINGS) : section;
	}

	@Override
	protected IStatus validateItem(final Object item) {
		return Status.OK_STATUS;
	}

	@Override
	protected ItemsFilter createFilter() {
		String raw = getPatternControl() instanceof Text t ? t.getText() : "";
		KeywordFilter filter = new KeywordFilter(raw);
		currentName = filter.name;
		return filter;
	}

	@Override
	protected Comparator<Object> getItemsComparator() {
		return (a, b) -> {
			Entry e1 = (Entry) a;
			Entry e2 = (Entry) b;
			boolean x1 = e1.name().equalsIgnoreCase(currentName);
			boolean x2 = e2.name().equalsIgnoreCase(currentName);
			if (x1 != x2) { return x1 ? -1 : 1; }
			int c = e1.name().compareToIgnoreCase(e2.name());
			if (c != 0) { return c; }
			c = e1.kind().compareTo(e2.kind());
			if (c != 0) { return c; }
			c = String.valueOf(e1.owner()).compareTo(String.valueOf(e2.owner()));
			return c != 0 ? c : e1.java().compareTo(e2.java());
		};
	}

	@Override
	protected void fillContentProvider(final AbstractContentProvider provider, final ItemsFilter filter,
			final IProgressMonitor monitor) throws CoreException {
		for (Entry e : GamlKeywordSearch.refresh(monitor)) {
			if (monitor.isCanceled()) { return; }
			provider.add(e, filter);
		}
		monitor.done();
	}

	@Override
	public String getElementName(final Object item) {
		return item instanceof Entry e ? e.name() + " " + e.description() + " " + e.java() + " " + e.path() + ":"
				+ e.offset() : String.valueOf(item);
	}

	/** Matches the entries against the pattern typed by the user. */
	private class KeywordFilter extends ItemsFilter {

		final String raw;
		final Kind kind;
		final String name;
		final Pattern wildcard;

		KeywordFilter(final String text) {
			raw = text.strip();
			String n = raw;
			Kind k = null;
			int colon = raw.indexOf(':');
			// A colon at the start or the end is not a kind prefix ("::" is not a GAML operator, but ':' could be)
			if (colon > 0) {
				k = Kind.fromPrefix(raw.substring(0, colon));
				if (k != null) { n = raw.substring(colon + 1).strip(); }
			}
			kind = k;
			name = n;
			boolean hasWord = n.chars().anyMatch(Character::isLetterOrDigit);
			if (hasWord && (n.indexOf('*') >= 0 || n.indexOf('?') >= 0)) {
				StringBuilder sb = new StringBuilder();
				for (char c : n.toCharArray()) {
					sb.append(switch (c) {
						case '*' -> ".*";
						case '?' -> ".";
						default -> Pattern.quote(String.valueOf(c));
					});
				}
				wildcard = Pattern.compile(sb.append(".*").toString(), Pattern.CASE_INSENSITIVE);
			} else {
				wildcard = null;
			}
		}

		@Override
		public boolean matchItem(final Object item) {
			if (!(item instanceof Entry e) || kind != null && e.kind() != kind) { return false; }
			if (name.isEmpty()) { return true; }
			if (wildcard != null) { return wildcard.matcher(e.name()).matches(); }
			return e.name().toLowerCase(Locale.ROOT).startsWith(name.toLowerCase(Locale.ROOT));
		}

		@Override
		public boolean isConsistentItem(final Object item) {
			return true;
		}

		@Override
		public boolean isSubFilter(final ItemsFilter filter) {
			return false;
		}

		@Override
		public boolean equalsFilter(final ItemsFilter filter) {
			return filter instanceof KeywordFilter k && k.raw.equals(raw);
		}

		@Override
		public String getPattern() {
			return raw;
		}
	}

	/** Labels and icons of the entries: "name - kind of owner - Java element". */
	private static class KeywordLabelProvider extends LabelProvider implements IStyledLabelProvider {

		@Override
		public String getText(final Object element) {
			return element instanceof Entry e ? e.name() + " - " + e.description() + " - " + e.java() : null;
		}

		@Override
		public StyledString getStyledText(final Object element) {
			if (!(element instanceof Entry e)) { return new StyledString(); }
			StyledString s = new StyledString(e.name());
			s.append(" - " + e.description(), StyledString.DECORATIONS_STYLER);
			s.append(" - " + e.java() + " (" + e.plugin() + ")", StyledString.QUALIFIER_STYLER);
			return s;
		}

		@Override
		public Image getImage(final Object element) {
			if (!(element instanceof Entry e)) { return null; }
			String key = switch (e.kind()) {
				case OPERATOR, ACTION, GETTER, SETTER -> ISharedImages.IMG_OBJS_PUBLIC;
				case ATTRIBUTE, CONSTANT, FACET, ARGUMENT -> ISharedImages.IMG_FIELD_PUBLIC;
				case STATEMENT, SKILL, SPECIES, TYPE, FILE, EXPERIMENT, DISPLAY, CLASS -> ISharedImages.IMG_OBJS_CLASS;
			};
			return JavaUI.getSharedImages().getImage(key);
		}
	}

	/** Stores the entries recently opened. */
	private static class EntryHistory extends SelectionHistory {

		@Override
		protected Object restoreItemFromMemento(final IMemento m) {
			try {
				Kind kind = Kind.valueOf(m.getString("kind"));
				return new Entry(m.getString("name"), kind, m.getString("owner"), m.getString("java"),
						m.getString("plugin"), m.getString("path"), m.getInteger("offset"), m.getInteger("length"),
						m.getInteger("line"));
			} catch (RuntimeException e) {
				return null;
			}
		}

		@Override
		protected void storeItemToMemento(final Object item, final IMemento m) {
			if (!(item instanceof Entry e)) { return; }
			m.putString("name", e.name());
			m.putString("kind", e.kind().name());
			if (e.owner() != null) { m.putString("owner", e.owner()); }
			m.putString("java", e.java());
			m.putString("plugin", e.plugin());
			m.putString("path", e.path());
			m.putInteger("offset", e.offset());
			m.putInteger("length", e.length());
			m.putInteger("line", e.line());
		}
	}

}
