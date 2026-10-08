/*******************************************************************************************************
 *
 * EditorSearchControls.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.editor.toolbar;

import static gama.ui.application.workbench.ThemeHelper.isDark;
import static gama.ui.shared.resources.IGamaColors.VERY_DARK_GRAY;
import static gama.ui.shared.resources.IGamaColors.VERY_LIGHT_GRAY;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.swt.IFocusService;

import gama.api.runtime.SystemInfo;
import gama.ui.shared.bindings.GamaKeyBindings;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;
import gaml.compiler.ui.editor.GamlEditor;

/**
 * The class EditToolbarFindControls.
 *
 * <p>
 * Provides an incremental search bar embedded in the GAML editor toolbar. Characters typed into the bar trigger a
 * debounced forward search (100 ms idle delay) so that the document read lock is not contended on every keystroke,
 * which was the primary cause of editor freezes on Windows.
 * </p>
 *
 * @author drogoul
 * @since 5 déc. 2014
 */
public class EditorSearchControls {

	/**
	 * Placeholder message shown inside the empty find control. Includes the keyboard shortcut so that users can
	 * discover the feature without documentation.
	 */
	static final String EMPTY = "Find... (" + GamaKeyBindings.format(SWT.MOD1, 'G') + ")"; //$NON-NLS-1$

	/** The SWT {@link Text} widget that receives the search string. */
	Text find;

	/**
	 * Tracks the caret offset at which an incremental search session started. Reset to {@code -1} when a new search
	 * session begins (focus gained) or when the field is cleared.
	 */
	int incrementalOffset = -1;

	/** The host editor that owns this search control. */
	final GamlEditor editor;

	/**
	 * Tracks the last text seen by {@link #modifyListener} so that the wrap direction can be determined correctly
	 * (wrapping is only needed when the new text is not a prefix of the previous text, i.e., the user is typing forward
	 * rather than deleting).
	 */
	private String lastModifyText = EMPTY;

	/**
	 * Holds the next debounced find {@link Runnable} that has been posted via {@link Display#timerExec}. Kept so that
	 * it can be cancelled when a new character arrives before the delay expires, preventing stale searches and reducing
	 * contention on the {@link org.eclipse.xtext.ui.editor.model.XtextDocument} read lock.
	 */
	private Runnable pendingFindRunnable = null;

	/**
	 * Instantiates a new EditorSearchControls and binds it to the given editor.
	 *
	 * @param editor
	 *            the GAML editor that owns this search control; must not be {@code null}
	 */
	public EditorSearchControls(final GamlEditor editor) {
		this.editor = editor;
	}

	/**
	 * Creates and wires all SWT widgets that make up the search bar inside the given toolbar. On Windows an extra
	 * {@link Composite} wrapper is added to work around a rendering quirk with the native {@link Text} widget inside a
	 * {@link org.eclipse.swt.custom.CoolBar}. The method also registers the {@link #modifyListener} and the key
	 * listener that handles ESC (return focus to editor) and ENTER (find next).
	 *
	 * @param toolbar
	 *            the toolbar into which the search control is inserted; must not be {@code null} and must not be
	 *            disposed
	 * @return {@code this}, for fluent chaining
	 */
	public EditorSearchControls fill(final GamaToolbarSimple toolbar) {
		Composite parent = toolbar;
		Color c = parent.getBackground();
		if (SystemInfo.isWindows()) {
			parent = new Composite(toolbar, SWT.NONE);
			final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			data.heightHint = 24;
			data.widthHint = 100;
			parent.setLayoutData(data);
			final GridLayout layout = new GridLayout();
			parent.setLayout(layout);
			GamaColors.setBackground(c, parent);
		}
		find = new Text(parent, SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		final IFocusService focusService = editor.getSite().getService(IFocusService.class);
		focusService.addFocusTracker(find, "search");

		final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 24;
		data.widthHint = 100;
		find.setLayoutData(data);
		// find.setBackground(IGamaColors.WHITE.color());
		// find.setForeground(IGamaColors.BLACK.color());
		find.setMessage(EMPTY);
		find.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(final FocusEvent e) {
				find.setText("");
			}

			@Override
			public void focusGained(final FocusEvent e) {
				adjustEnablement(false, null);
				incrementalOffset = -1;
			}
		});
		GamaColors.setBackAndForeground(c, isDark() ? VERY_LIGHT_GRAY.color() : VERY_DARK_GRAY.color(), find);
		toolbar.control(parent == toolbar ? find : parent, 100);
		find.addModifyListener(modifyListener);
		find.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(final KeyEvent e) {}

			@Override
			public void keyPressed(final KeyEvent e) {
				// Use DEBUG level: INFO logging calls perform synchronous I/O on the UI thread
				// on Windows (Logback file/console appenders), causing measurable freezes.
				// logger.debug("Key pressed in find control: char=" + e.character + " code=" + e.keyCode);
				if (e.character == SWT.ESC) { editor.setFocus(); }
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) { findNext(); }
			}
		});
		this.adjustEnablement(false, null);
		return this;

	}

	/**
	 * Returns the underlying SWT {@link Text} widget used as the search input field.
	 *
	 * @return the find text widget; {@code null} if {@link #fill} has not been called yet
	 */
	public Text getFindControl() { return find; }

	/**
	 * Debounced {@link ModifyListener} attached to the find {@link Text} widget.
	 *
	 * <p>
	 * Instead of calling {@link #find(boolean, boolean, boolean)} synchronously on every character, the listener posts
	 * the search via {@link org.eclipse.swt.widgets.Display#timerExec(int, Runnable)} with a 100 ms delay. If another
	 * character arrives before the timer fires, the pending runnable is cancelled and a new one is posted. This
	 * coalesces rapid keystrokes into a single search call, drastically reducing contention on the
	 * {@link org.eclipse.xtext.ui.editor.model.XtextDocument} read lock and preventing the UI freeze that Windows users
	 * experienced when typing in the search field.
	 * </p>
	 */
	private final ModifyListener modifyListener = new ModifyListener() {
		@Override
		public void modifyText(final ModifyEvent e) {
			final String text = find.getText();
			// If the new text is NOT a prefix of the previous text (i.e., the user typed a new
			// character rather than deleting one), start from the current position rather than
			// wrapping around to the beginning of the document.
			final boolean wrap = !lastModifyText.startsWith(text);
			lastModifyText = text;

			// Cancel any search that was previously scheduled but has not fired yet.
			if (pendingFindRunnable != null && !find.isDisposed()) {
				find.getDisplay().timerExec(-1, pendingFindRunnable);
			}
			pendingFindRunnable = null;

			if (EMPTY.equals(text) || text.isEmpty()) {
				adjustEnablement(false, null);
				final ISelectionProvider selectionProvider = editor.getSelectionProvider();
				if (selectionProvider != null) {
					final ISelection selection = selectionProvider.getSelection();
					if (selection instanceof ITextSelection textSelection) {
						selectionProvider.setSelection(new TextSelection(textSelection.getOffset(), 0));
					}
				}
			} else {
				final boolean doWrap = wrap;
				pendingFindRunnable = () -> {
					pendingFindRunnable = null;
					if (!find.isDisposed() && !find.getText().isEmpty()) { find(true, true, doWrap); }
				};
				if (!find.isDisposed()) { find.getDisplay().timerExec(100, pendingFindRunnable); }
			}
		}
	};

	/**
	 * Updates the foreground colour of the search field to give visual feedback about the last search result.
	 *
	 * @param found
	 *            {@code true} if the last search located at least one match (unused; retained for API symmetry)
	 * @param color
	 *            the colour to apply, or {@code null} to restore the default widget foreground
	 */
	void adjustEnablement(final boolean found, final Color color) {
		if (color == null) {
			find.setForeground(IGamaColors.WIDGET_FOREGROUND.color());
		} else {
			find.setForeground(color);
		}
	}

	/**
	 * Triggers a backwards (upward) search for the current find-bar text, starting immediately before the current
	 * selection. Wraps around to the end of the document when no match is found above.
	 */
	public void findPrevious() {
		find(false);
	}

	/**
	 * Triggers a forward (downward) search for the current find-bar text, starting immediately after the current
	 * selection. Wraps around to the beginning of the document when no match is found below.
	 */
	public void findNext() {
		find(true);
	}

	/**
	 * Initiates a non-incremental search in the given direction with wrap-around enabled.
	 *
	 * @param forward
	 *            {@code true} to search forward (downward); {@code false} to search backward (upward)
	 */
	private void find(final boolean forward) {
		find(forward, false);
	}

	/**
	 * Initiates a search in the given direction. When {@code incremental} is {@code true} the search starts from the
	 * offset at which the current incremental session began rather than from the current caret position, allowing the
	 * match to grow as more characters are typed. Wrap-around is always enabled.
	 *
	 * @param forward
	 *            {@code true} to search forward; {@code false} to search backward
	 * @param incremental
	 *            {@code true} for an incremental (type-ahead) search; {@code false} for a manual next/previous step
	 */
	private void find(final boolean forward, final boolean incremental) {
		find(forward, incremental, true, false);
	}

	/**
	 * Initiates a search in the given direction.
	 *
	 * @param forward
	 *            {@code true} to search forward; {@code false} to search backward
	 * @param incremental
	 *            {@code true} for an incremental (type-ahead) search
	 * @param wrap
	 *            {@code true} to wrap around to the other end of the document when no match is found
	 */
	void find(final boolean forward, final boolean incremental, final boolean wrap) {
		find(forward, incremental, wrap, false);
	}

	/**
	 * Core search implementation. Resolves the starting offset, delegates to the editor's {@link IFindReplaceTarget}
	 * adapter and updates the UI colour to indicate success or failure. When {@code wrap} is {@code true} and no match
	 * is found, the method calls itself recursively once with {@code wrapping=true} to restart from the opposite end of
	 * the document.
	 *
	 * <p>
	 * The {@link IFindReplaceTargetExtension} session is opened before the search and closed in a {@code finally} block
	 * to ensure the underlying viewer always exits its find session, even if an exception is thrown.
	 * </p>
	 *
	 * @param forward
	 *            {@code true} to search forward; {@code false} to search backward
	 * @param incremental
	 *            {@code true} for incremental (type-ahead) mode
	 * @param wrap
	 *            {@code true} to allow wrap-around after the first pass
	 * @param wrapping
	 *            {@code true} when this call is itself the wrap-around retry (prevents infinite recursion)
	 */
	private void find(final boolean forward, final boolean incremental, final boolean wrap, final boolean wrapping) {

		final IFindReplaceTarget findReplaceTarget = editor.getAdapter(IFindReplaceTarget.class);
		if (findReplaceTarget != null) {
			try {
				final String findText = find.getText();
				if (findReplaceTarget instanceof IFindReplaceTargetExtension findReplaceTargetExtension) {
					findReplaceTargetExtension.beginSession();
				}
				final ISourceViewer sourceViewer = getSourceViewer();
				final StyledText textWidget = sourceViewer.getTextWidget();
				int offset = textWidget.getCaretOffset();
				Point selection = textWidget.getSelection();
				if (wrapping) {
					if (forward) {
						offset = 0;
					} else {
						offset = sourceViewer.getDocument().getLength() - 1;
					}
				} else if (forward) {
					if (incremental) {
						if (incrementalOffset == -1) {
							incrementalOffset = offset;
						} else {
							offset = incrementalOffset;
						}
					} else {
						incrementalOffset = selection.x;
					}
				} else {
					incrementalOffset = selection.x;
					if (selection.x != offset) { offset = selection.x; }
				}
				int newOffset = -1;
				if (findReplaceTarget instanceof IFindReplaceTargetExtension3) {
					newOffset = ((IFindReplaceTargetExtension3) findReplaceTarget).findAndSelect(offset, findText,
							forward, false, false, false);

				} else {
					newOffset = findReplaceTarget.findAndSelect(offset, findText, forward, false, false);
				}

				if (newOffset != -1) {
					adjustEnablement(true, IGamaColors.OK.inactive());
					selection = textWidget.getSelection();
					if (!forward) { incrementalOffset = selection.x; }
				} else {
					if (wrap && !wrapping) {
						find(forward, incremental, wrap, true);
						return;
					}
					if (!EMPTY.equals(findText) && !"".equals(findText)) {
						adjustEnablement(false, IGamaColors.ERROR.inactive());
					}
				}
			} finally {

				if (findReplaceTarget instanceof IFindReplaceTargetExtension findReplaceTargetExtension) {
					findReplaceTargetExtension.endSession();
				}
			}
		}
	}

	/**
	 * Returns the {@link ISourceViewer} of the host editor by adapting it as an {@link ITextOperationTarget}.
	 *
	 * @return the source viewer; may be {@code null} if the editor has not yet been fully initialised
	 */
	private ISourceViewer getSourceViewer() { return (ISourceViewer) editor.getAdapter(ITextOperationTarget.class); }
}
