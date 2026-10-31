/*******************************************************************************************************
 *
 * TextSearchItem.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views.toolbar;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.IFindReplaceTargetExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.findandreplace.overlay.FindReplaceOverlay;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;
import org.eclipse.ui.texteditor.StatusTextEditor;

import gama.api.types.color.IColor;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;
import gama.ui.shared.bindings.GamaKeyBindings;
import gama.ui.shared.resources.GamaColors;

/** Shared Find launcher and read-only native Find overlay integration for text views. */
public final class TextSearchItem {

	private TextSearchItem() {}

	public static void installFindOverlayAlignment(final Display display) {
		final String key = "gama.ui.shared.findOverlayAlignment";
		if (display.getData(key) != null) return;
		final Listener listener = event -> {
			if (!(event.widget instanceof Composite container)
					|| container.getClass().getEnclosingClass() != FindReplaceOverlay.class
					|| container.getParent().getClass().getEnclosingClass() == FindReplaceOverlay.class) return;
			// Widget IDs are assigned after the native overlay's Show event.
			display.asyncExec(() -> {
				if (!container.isDisposed() && container.isVisible() && !centerFindActions(container)) {
					DEBUG.ERR("Unable to locate the native Find action row");
				}
			});
		};
		display.addFilter(SWT.Show, listener);
		display.setData(key, listener);
	}

	private static boolean centerFindActions(final Composite container) {
		for (final Control child : container.getChildren()) {
			if ("searchInput".equals(child.getData(FindReplaceOverlay.ID_DATA_KEY))) {
				final Composite row = child.getParent().getParent();
				for (final Control action : row.getChildren()) {
					if (action.getLayoutData() instanceof GridData data
							&& data.verticalAlignment == GridData.END) {
						data.verticalAlignment = GridData.CENTER;
					}
				}
				row.layout(true, true);
				return true;
			}
			if (child instanceof Composite composite && centerFindActions(composite)) return true;
		}
		return false;
	}

	public static ToolItem install(final GamaToolbarSimple toolbar, final Runnable openFind) {
		return toolbar.button("editor/search", null,
				"Find (" + GamaKeyBindings.format(SWT.MOD1, 'F') + ")", e -> openFind.run());
	}

	public static void open(final IViewPart part) {
		final IAction action = part.getViewSite().getActionBars().getGlobalActionHandler(ActionFactory.FIND.getId());
		if (action == null) throw new IllegalStateException("Find is not installed in " + part.getTitle());
		action.run();
	}

	private static void setFindBackground(final Control control, final Color background) {
		GamaColors.setBackground(background, control);
		if (control instanceof Composite composite) {
			for (final Control child : composite.getChildren()) {
				setFindBackground(child, background);
			}
		}
	}

	public static void bind(final IViewPart part, final TextViewer viewer) {
		final SearchAction action = new SearchAction(part, viewer);
		final IActionBars bars = part.getViewSite().getActionBars();
		bars.setGlobalActionHandler(ActionFactory.FIND.getId(), action);
		bars.updateActionBars();
		final IContextService contexts = part.getSite().getService(IContextService.class);
		final IContextActivation context = contexts.activateContext("org.eclipse.ui.textEditorScope");
		final IPartListener2 listener = new IPartListener2() {
			@Override
			public void partActivated(final IWorkbenchPartReference reference) {
				final IWorkbenchPart active = reference.getPart(false);
				if (active != null && active != part) { action.close(active); }
			}

			@Override
			public void partHidden(final IWorkbenchPartReference reference) {
				if (reference.getPart(false) == part) { action.close(part.getSite().getPage().getActivePart()); }
			}
		};
		part.getSite().getPage().addPartListener(listener);
		viewer.prependVerifyKeyListener(event -> {
			if ((event.stateMask & SWT.MODIFIER_MASK) == SWT.MOD1
					&& (event.keyCode == 'f' || event.keyCode == 'F')) {
				event.doit = false;
				action.run();
			}
		});
		final Shell shell = part.getSite().getShell();
		final Listener reposition = event -> action.positionPopup();
		viewer.getControl().addListener(SWT.Move, reposition);
		viewer.getControl().addListener(SWT.Resize, reposition);
		shell.addListener(SWT.Move, reposition);
		shell.addListener(SWT.Resize, reposition);
		// Release the search target before TextViewer's own text-widget disposal listener runs.
		viewer.getControl().getParent().addDisposeListener(event -> {
			action.close(null);
			if (!shell.isDisposed()) {
				shell.removeListener(SWT.Move, reposition);
				shell.removeListener(SWT.Resize, reposition);
			}
			part.getSite().getPage().removePartListener(listener);
			contexts.deactivateContext(context);
			if (bars.getGlobalActionHandler(ActionFactory.FIND.getId()) == action) {
				bars.setGlobalActionHandler(ActionFactory.FIND.getId(), null);
			}
		});
	}

	private static final class SearchAction extends Action {

		private final IViewPart part;
		private final TextViewer viewer;
		private final ReadOnlyTarget target;
		private FindReplaceOverlay overlay;
		private Shell popup;
		private RGB popupBackground;
		private boolean closing;
		private boolean positioning;

		SearchAction(final IViewPart part, final TextViewer viewer) {
			super("Find");
			this.part = part;
			this.viewer = viewer;
			target = ReadOnlyTarget.forViewer(viewer);
			setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_REPLACE);
		}

		@Override
		public void run() {
			if (viewer.getTextWidget().isDisposed()) return;
			part.getSite().getPage().activate(part);
			part.setFocus();
			final Color background = editorBackground();
			if (overlay != null && !overlay.getContainerControl().isDisposed()
					&& !background.getRGB().equals(popupBackground)) {
				close(null);
			}
			if (overlay == null || overlay.getContainerControl().isDisposed()) {
				// A view is not an editor target: isolate the native overlay from the workbench shell's layout.
				popup = new Shell(part.getSite().getShell(), SWT.TOOL | SWT.NO_TRIM);
				// The native view fallback probes a temporary search field and freezes its colors.
				GamaColors.setBackground(background, popup);
				popup.setBackgroundMode(SWT.INHERIT_FORCE);
				overlay = new FindReplaceOverlay(popup, null, target);
				popupBackground = background.getRGB();
				final Composite control = overlay.getContainerControl();
				setFindBackground(control, background);
				popup.setLayout(new FillLayout());
				final Point preferred = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				final Rectangle monitor = viewer.getTextWidget().getMonitor().getClientArea();
				popup.setSize(Math.min(monitor.width, Math.max(600, preferred.x)), preferred.y);
				control.addListener(SWT.Move, event -> positionPopup());
				control.addListener(SWT.Resize, event -> positionPopup());
				control.addListener(SWT.Hide, event -> {
					target.endSession();
					popup.setVisible(false);
					if (!closing && !viewer.getControl().isDisposed()
							&& part.getSite().getPage().getActivePart() == part) {
						part.setFocus();
					}
				});
				popup.addListener(SWT.Close, event -> {
					event.doit = false;
					close(part);
				});
			}
			target.beginSession();
			popup.setVisible(true);
			overlay.open();
			if (!centerFindActions(overlay.getContainerControl())) {
				DEBUG.ERR("Unable to locate the console Find action row");
			}
			positionPopup();
		}

		private Color editorBackground() {
			final IEditorPart editor = part.getSite().getPage().getActiveEditor();
			if (editor instanceof StatusTextEditor textEditor) {
				final ITextViewer editorViewer = textEditor.getAdapter(ITextViewer.class);
				if (editorViewer != null) {
					final StyledText text = editorViewer.getTextWidget();
					if (text != null && !text.isDisposed()) return text.getBackground();
				}
			}
			final IColor configured = GamaPreferences.Modeling.EDITOR_BACKGROUND_COLOR.getValue();
			return configured == null ? viewer.getTextWidget().getBackground() : GamaColors.get(configured).color();
		}

		private void positionPopup() {
			if (closing || positioning || popup == null || popup.isDisposed() || viewer.getControl().isDisposed()) return;
			positioning = true;
			try {
				// Fill the popup instead of masking the native overlay's editor-relative margins.
				popup.layout(true, true);
				final Rectangle bounds = popup.getClientArea();
				final Rectangle monitor = viewer.getTextWidget().getMonitor().getClientArea();
				final Point anchor = viewer.getTextWidget().toDisplay(viewer.getTextWidget().getSize().x, 0);
				final int x = Math.max(monitor.x, Math.min(anchor.x - bounds.width,
						monitor.x + monitor.width - bounds.width));
				final int y = Math.max(monitor.y, Math.min(anchor.y, monitor.y + monitor.height - bounds.height));
				popup.setLocation(x, y);
			} finally {
				positioning = false;
			}
		}

		void close(final IWorkbenchPart restoreFocus) {
			if (closing || overlay == null) return;
			closing = true;
			try {
				if (!overlay.getContainerControl().isDisposed()) {
					overlay.close();
				}
				target.endSession();
				if (popup != null && !popup.isDisposed()) { popup.dispose(); }
				popup = null;
				popupBackground = null;
				overlay = null;
				if (restoreFocus != null && restoreFocus != part) {
					part.getSite().getPage().activate(restoreFocus);
					restoreFocus.setFocus();
				}
			} finally {
				closing = false;
			}
		}
	}

	private static final class ReadOnlyTarget
			implements IFindReplaceTarget, IFindReplaceTargetExtension, IFindReplaceTargetExtension3,
			IFindReplaceTargetExtension4 {

		private final IFindReplaceTarget target;
		private final IFindReplaceTargetExtension session;
		private final IFindReplaceTargetExtension3 regex;
		private final IFindReplaceTargetExtension4 multi;
		private boolean sessionActive;

		private ReadOnlyTarget(final IFindReplaceTarget target, final IFindReplaceTargetExtension session,
				final IFindReplaceTargetExtension3 regex, final IFindReplaceTargetExtension4 multi) {
			this.target = target;
			this.session = session;
			this.regex = regex;
			this.multi = multi;
		}

		static ReadOnlyTarget forViewer(final TextViewer viewer) {
			final IFindReplaceTarget target = viewer.getFindReplaceTarget();
			if (!(target instanceof IFindReplaceTargetExtension session)
					|| !(target instanceof IFindReplaceTargetExtension3 regex)
					|| !(target instanceof IFindReplaceTargetExtension4 multi)) {
				throw new IllegalArgumentException("The text viewer does not support the native Find overlay");
			}
			return new ReadOnlyTarget(target, session, regex, multi);
		}

		@Override
		public boolean canPerformFind() { return target.canPerformFind(); }

		@Override
		public int findAndSelect(final int offset, final String text, final boolean forward, final boolean caseSensitive,
				final boolean wholeWord) {
			return target.findAndSelect(offset, text, forward, caseSensitive, wholeWord);
		}

		@Override
		public int findAndSelect(final int offset, final String text, final boolean forward, final boolean caseSensitive,
				final boolean wholeWord, final boolean regEx) {
			return regex.findAndSelect(offset, text, forward, caseSensitive, wholeWord, regEx);
		}

		@Override
		public Point getSelection() { return target.getSelection(); }

		@Override
		public String getSelectionText() { return target.getSelectionText(); }

		@Override
		public boolean isEditable() { return false; }

		@Override
		public void replaceSelection(final String text) {
			throw new UnsupportedOperationException("Console search is read-only");
		}

		@Override
		public void replaceSelection(final String text, final boolean regEx) {
			throw new UnsupportedOperationException("Console search is read-only");
		}

		@Override
		public void beginSession() {
			if (!sessionActive) {
				session.beginSession();
				sessionActive = true;
			}
		}

		@Override
		public void endSession() {
			if (sessionActive) {
				session.endSession();
				sessionActive = false;
			}
		}

		@Override
		public IRegion getScope() { return session.getScope(); }

		@Override
		public void setScope(final IRegion scope) { session.setScope(scope); }

		@Override
		public Point getLineSelection() { return session.getLineSelection(); }

		@Override
		public void setSelection(final int offset, final int length) { session.setSelection(offset, length); }

		@Override
		public void setSelection(final IRegion[] regions) { multi.setSelection(regions); }

		@Override
		public void setScopeHighlightColor(final Color color) { session.setScopeHighlightColor(color); }

		@Override
		public void setReplaceAllMode(final boolean replacing) {
			if (replacing) throw new UnsupportedOperationException("Console search is read-only");
			session.setReplaceAllMode(false);
		}
	}
}
