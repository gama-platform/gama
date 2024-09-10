/*******************************************************************************************************
 *
 * GamlSearchField.java, in gama.ui.shared.shared, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.access;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.swt.IFocusService;

import gama.dev.DEBUG;
import gama.gaml.interfaces.IGamlDescription;
import gama.ui.shared.bindings.GamaKeyBindings;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.utils.WebHelper;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class GamlSearchField.
 */
public class GamlSearchField {

	static {
		DEBUG.ON();
	}

	/** The shell. */
	Shell shell;

	/** The text. */
	protected Text text;

	/** The instance. */
	public static GamlSearchField INSTANCE;

	/** The quick access contents. */
	GamlAccessContents quickAccessContents;

	/** The dialog height. */
	int dialogHeight = -1;

	/** The dialog width. */
	int dialogWidth = -1;

	/** The previous focus control. */
	Control previousFocusControl;

	/** The composite. */
	// private GamaToolbarSimple toolbar;
	private Composite composite;

	/** The table. */
	Table table;

	/** The selected string. */
	String selectedString = ""; //$NON-NLS-1$

	/** The accessible listener. */
	private AccessibleAdapter accessibleListener;

	/** The commands installed. */
	private boolean commandsInstalled;

	/**
	 * Instantiates a new gaml search field.
	 */
	private GamlSearchField() {}

	/**
	 * Gets the text.
	 *
	 * @return the text
	 */
	public Text getText() { return text; }

	/**
	 * Hook up commands.
	 */
	void hookUpCommands() {
		if (commandsInstalled) return;
		commandsInstalled = true;
		final IFocusService focus = WorkbenchHelper.getService(IFocusService.class);
		focus.addFocusTracker(text, GamlSearchField.class.getName());

		final org.eclipse.core.expressions.Expression focusExpr = new org.eclipse.core.expressions.Expression() {
			@Override
			public void collectExpressionInfo(final ExpressionInfo info) {
				info.addVariableNameAccess(ISources.ACTIVE_FOCUS_CONTROL_ID_NAME);
			}

			@Override
			public EvaluationResult evaluate(final IEvaluationContext context) {
				return EvaluationResult.valueOf(GamlSearchField.class.getName()
						.equals(context.getVariable(ISources.ACTIVE_FOCUS_CONTROL_ID_NAME)));
			}
		};

		final IHandlerService whService = WorkbenchHelper.getService(IHandlerService.class);
		whService.activateHandler(IWorkbenchCommandConstants.EDIT_SELECT_ALL, new AbstractHandler() {
			@Override
			public Object execute(final ExecutionEvent event) {
				text.selectAll();
				return null;
			}
		}, focusExpr);
		whService.activateHandler(IWorkbenchCommandConstants.EDIT_CUT, new AbstractHandler() {
			@Override
			public Object execute(final ExecutionEvent event) {
				text.cut();
				return null;
			}
		}, focusExpr);
		whService.activateHandler(IWorkbenchCommandConstants.EDIT_COPY, new AbstractHandler() {
			@Override
			public Object execute(final ExecutionEvent event) {
				text.copy();
				return null;
			}
		}, focusExpr);
		whService.activateHandler(IWorkbenchCommandConstants.EDIT_PASTE, new AbstractHandler() {
			@Override
			public Object execute(final ExecutionEvent event) {
				text.paste();
				return null;
			}
		}, focusExpr);
	}

	/**
	 * Creates the widget.
	 *
	 * @param parent
	 *            the parent
	 * @return the control
	 */
	public Control createWidget(final Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0).extendedMargins(0, 5, 5, 5).numColumns(3)
				.equalWidth(false).applyTo(composite);
		text = createText(composite);
		final int height = 24;// PlatformHelper.isWindows() ? 16 : ;
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).hint(250, SWT.DEFAULT)
				.applyTo(text);

		new HeapControl().displayOn(composite);

		// GamaToolbarSimple bar = new GamaToolbarSimple(composite, SWT.NONE);
		// bar.button("generic/garbage.collect", "", "60M on 4096M", e -> { System.gc(); });
		// GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).indent(30, 0).applyTo(bar);
		parent.getShell().addControlListener(new ControlListener() {
			@Override
			public void controlResized(final ControlEvent e) {
				closeDropDown();
			}

			@Override
			public void controlMoved(final ControlEvent e) {
				closeDropDown();
			}

			private void closeDropDown() {
				if (shell == null || shell.isDisposed() || text.isDisposed() || !shell.isVisible()) return;
				quickAccessContents.doClose();
			}
		});

		// restoreDialog();

		quickAccessContents = new GamlAccessContents() {

			@Override
			protected void doClose() {
				text.setText(""); //$NON-NLS-1$
				dialogHeight = shell.getSize().y;
				dialogWidth = shell.getSize().x;
				shell.setVisible(false);
				removeAccessibleListener();
			}

			@Override
			protected void handleElementSelected(final String text, final GamlAccessEntry entry) {
				if (entry == null) return;
				final IGamlDescription element = entry.element;
				final String search = "https://gama-platform.org/search?q=" + element.getName();
				// DEBUG.OUT("Search phrase: " + search);
				WebHelper.openPage(search);

			}

			@Override
			public PopupText getPopupText() {
				final TableItem[] selection = table.getSelection();
				if (selection != null && selection.length > 0) {
					final GamlAccessEntry entry = (GamlAccessEntry) selection[0].getData();
					if (entry != null) {
						final IGamlDescription element = entry.element;
						return PopupText.with(IGamaColors.BLUE, entry.provider.document(element));
					}
				}

				return null;
			}

			@Override
			public Shell getControllingShell() { return shell; }

			@Override
			public Point getAbsoluteOrigin() { return shell.toDisplay(0, shell.getSize().y); }

			@Override
			public int getPopupWidth() { return table.getSize().x; }

		};
		quickAccessContents.hookFilterText(text);
		shell = new Shell(parent.getShell(), SWT.RESIZE | SWT.ON_TOP | SWT.BORDER);
		shell.setBackground(IGamaColors.VERY_LIGHT_GRAY.color());
		shell.setText(""); // just for debugging, not shown anywhere

		GridLayoutFactory.fillDefaults().applyTo(shell);
		table = quickAccessContents.createTable(shell, Window.getDefaultOrientation());
		text.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(final FocusEvent e) {
				// Once the focus event is complete, check if we should close the shell
				table.getDisplay().asyncExec(() -> checkFocusLost(table, text));
			}

			@Override
			public void focusGained(final FocusEvent e) {
				hookUpCommands();
			}

		});
		table.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent e) {
				// Once the focus event is complete, check if we should close
				// the shell
				table.getDisplay().asyncExec(() -> checkFocusLost(table, text));
			}
		});
		text.addModifyListener(e -> {
			final boolean wasVisible = shell.getVisible();
			final boolean nowVisible = text.getText().length() > 0;
			if (!wasVisible && nowVisible) {
				layoutShell();
				addAccessibleListener();
			}
			if (wasVisible && !nowVisible) { removeAccessibleListener(); }
			if (nowVisible) { notifyAccessibleTextChanged(); }
			shell.setVisible(nowVisible);
		});
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				switch (e.keyCode) {
					case SWT.ESC:
						text.setText(""); //$NON-NLS-1$
						if (previousFocusControl != null && !previousFocusControl.isDisposed()) {
							previousFocusControl.setFocus();
						}
						break;
					case SWT.ARROW_UP:
						// Windows moves caret left/right when pressing up/down,
						// avoid this as the table selection changes for up/down
						e.doit = false;
						break;
					case SWT.ARROW_DOWN:
						e.doit = false;
						break;
					default:
						break;
				}
				if (!e.doit) {
					// arrow key pressed
					notifyAccessibleTextChanged();
				}
			}
		});
		// hookToWorkbench();
		return composite;
	}

	/**
	 * Creates the text.
	 *
	 * @param parent
	 *            the parent
	 * @return the text
	 */
	private Text createText(final Composite parent) {
		final Text text = new Text(parent, SWT.SEARCH | SWT.ICON_SEARCH);
		final String message = "GAML reference (" + GamaKeyBindings.SEARCH_STRING + ")";
		text.setMessage(message);
		return text;
	}

	/**
	 * This method was copy/pasted from JFace.
	 */
	private static Monitor getClosestMonitor(final Display toSearch, final Point toFind) {
		int closest = Integer.MAX_VALUE;

		final Monitor[] monitors = toSearch.getMonitors();
		Monitor result = monitors[0];

		for (final Monitor current : monitors) {
			final Rectangle clientArea = current.getClientArea();

			if (clientArea.contains(toFind)) return current;

			final int distance = Geometry.distanceSquared(Geometry.centerPoint(clientArea), toFind);
			if (distance < closest) {
				closest = distance;
				result = current;
			}
		}

		return result;
	}

	/**
	 * This method was copy/pasted from JFace.
	 */
	private Rectangle getConstrainedShellBounds(final Display display, final Rectangle preferredSize) {
		final Rectangle result =
				new Rectangle(preferredSize.x, preferredSize.y, preferredSize.width, preferredSize.height);

		final Point topLeft = new Point(preferredSize.x, preferredSize.y);
		final Monitor mon = getClosestMonitor(display, topLeft);
		final Rectangle bounds = mon.getClientArea();

		if (result.height > bounds.height) { result.height = bounds.height; }

		if (result.width > bounds.width) { result.width = bounds.width; }

		result.x = Math.max(bounds.x, Math.min(result.x, bounds.x + bounds.width - result.width));
		result.y = Math.max(bounds.y, Math.min(result.y, bounds.y + bounds.height - result.height));

		return result;
	}

	/**
	 * Layout shell.
	 */
	void layoutShell() {
		final Display display = text.getDisplay();
		final Rectangle tempBounds = text.getBounds();
		final Rectangle compBounds = display.map(text, null, tempBounds);
		final int w = quickAccessContents.maxDefinitionWidth + quickAccessContents.maxProviderWidth;
		final int preferredWidth = dialogWidth == -1 ? w : dialogWidth;
		final int width = Math.max(preferredWidth, compBounds.width);
		final int height = dialogHeight == -1 ? 400 : dialogHeight;

		// If size would extend past the right edge of the shell, try to move it
		// to the left of the text
		final Rectangle shellBounds = text.getShell().getBounds();
		if (compBounds.x + width > shellBounds.x + shellBounds.width) {
			compBounds.x = Math.max(shellBounds.x, compBounds.x + compBounds.width - width);
		}

		shell.setBounds(getConstrainedShellBounds(display,
				new Rectangle(compBounds.x, compBounds.y + compBounds.height, width, height)));
		shell.layout();
	}

	/**
	 * Activate.
	 *
	 * @param previousFocusControl
	 *            the previous focus control
	 */
	public void activate(final Control previousFocusControl) {
		this.previousFocusControl = previousFocusControl;
		if (!shell.isVisible()) {
			layoutShell();
			shell.setVisible(true);
			addAccessibleListener();
			quickAccessContents.refresh(text.getText().toLowerCase());
		}
	}

	/**
	 * Checks if the text or shell has focus. If not, closes the shell.
	 *
	 * @param table
	 *            the shell's table
	 * @param text
	 *            the search text field
	 */
	protected void checkFocusLost(final Table table, final Text text) {
		if (!shell.isDisposed() && !table.isDisposed() && !text.isDisposed()) {
			if (table.getDisplay().getActiveShell() == table.getShell()) {
				// If the user selects the trim shell, leave focus on the text
				// so shell stays open
				text.setFocus();
				return;
			}
			if (!shell.isFocusControl() && !table.isFocusControl() && !text.isFocusControl()) {
				quickAccessContents.doClose();
			}
		}
	}

	/**
	 * Adds a listener to the <code>org.eclipse.swt.accessibility.Accessible</code> object assigned to the Quick Access
	 * search box. The listener sets a name of a selected element in the search result list as a text to read for a
	 * screen reader.
	 */
	private void addAccessibleListener() {
		if (accessibleListener == null) {
			accessibleListener = new AccessibleAdapter() {
				@Override
				public void getName(final AccessibleEvent e) {
					e.result = selectedString;
				}
			};
			text.getAccessible().addAccessibleListener(accessibleListener);
		}
	}

	/**
	 * Removes a listener from the <code>org.eclipse.swt.accessibility.Accessible</code> object assigned to the Quick
	 * Access search box.
	 */
	void removeAccessibleListener() {
		if (accessibleListener != null) {
			text.getAccessible().removeAccessibleListener(accessibleListener);
			accessibleListener = null;
		}
		selectedString = ""; //$NON-NLS-1$
	}

	/**
	 * Notifies <code>org.eclipse.swt.accessibility.Accessible<code> object that selected item has been changed.
	 */
	void notifyAccessibleTextChanged() {
		if (table.getSelection().length == 0) return;
		final TableItem item = table.getSelection()[0];
		selectedString = NLS.bind("{0}: {1}", item.getText(0), item.getText(1));
		text.getAccessible().sendEvent(ACC.EVENT_NAME_CHANGED, null);
	}

	/**
	 * Search.
	 */
	public void search() {
		final IWorkbenchPart part = WorkbenchHelper.getActivePart();
		if (part instanceof IEditorPart editor) {
			final IWorkbenchPartSite site = editor.getSite();
			if (site != null) {
				final ISelectionProvider provider = site.getSelectionProvider();
				if (provider != null) {
					final ISelection viewSiteSelection = provider.getSelection();
					if (viewSiteSelection instanceof TextSelection textSelection) {
						text.setText(textSelection.getText());
					}
				}
			}

		}
		activate(null);
		text.setFocus();

	}

	/**
	 * Install on.
	 *
	 * @param parent
	 *            the parent
	 * @return the control
	 */
	public static Control installOn(final Composite parent) {
		INSTANCE = new GamlSearchField();
		return INSTANCE.createWidget(parent);
	}

}
