/*******************************************************************************************************
 *
 * StatusHistoryPopUpMenu.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.controls;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;

import com.google.common.base.Strings;
import com.google.common.collect.ForwardingList;

import gama.api.GAMA;
import gama.api.compilation.documentation.GamlIdiomsProvider;
import gama.api.ui.IStatusMessage;
import gama.dev.DEBUG;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * The Class CustomMenu. An alternative to Popup & Regular menus
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 22 août 2023
 */
public class StatusHistoryPopUpMenu extends PopupDialog {

	/** The sdf. */
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	/** The limit. */
	int limit = 30;

	static {
		DEBUG.OFF();
	}

	/**
	 * The Class BoundedList.
	 *
	 * @param <T>
	 *            the generic type
	 */
	public class BoundedList<T> extends ForwardingList<T> {

		/** The delegate. */
		LinkedList<T> delegate = new LinkedList<>();

		@Override
		protected List<T> delegate() {
			return delegate;
		}

		@Override
		public void add(final int index, final T element) {
			if (delegate.size() > limit) { delegate.removeLast(); }
			super.add(index, element);
		}

		@Override
		public boolean addAll(final int index, final Collection<? extends T> elements) {
			for (T element : elements) { add(index, element); }
			return true;
		}

		@Override
		public boolean add(final T element) {
			if (delegate.size() > limit) { delegate.removeLast(); }
			delegate.addFirst(element);
			return true;
		}

		@Override
		public boolean addAll(final Collection<? extends T> elements) {
			for (T element : elements) { add(element); }
			return true;
		}

	}

	/** The contents. */
	Composite parent, contents;

	/** The labels. */
	List<Composite> labels = new CopyOnWriteArrayList<>();

	/** The labels. */
	List<IStatusMessage> events = new BoundedList<>();

	/** The hide. */
	final Listener hide = event -> hide();

	/** Hides the popup when a click occurs outside it. */
	private final Listener dismissOnOutsideClick;

	/** The popup shell currently observed for deactivation. */
	private Shell observedPopupShell;

	/** Delay (in ms) during which a dismissed popup cannot be reopened by the same click. */
	private static final long REOPEN_DELAY = 300;

	/** The last time the popup was hidden. */
	private volatile long lastHideTime;

	/** Whether rows were added or removed, or a text no longer fits, since the last sizing. */
	private boolean needsResize;

	/**
	 * Sets the text of a label and records whether the popup needs to be enlarged to show it.
	 */
	private void setText(final Label label, final String text) {
		if (text.equals(label.getText())) return;
		label.setText(text);
		if (!needsResize && label.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x > label.getSize().x) {
			needsResize = true;
		}
	}

	/** The provider. */
	private final StatusControlContribution status;

	/**
	 * Instantiates a new popup 2.
	 *
	 * @param status
	 *            the provider
	 * @param controls
	 *            the controls
	 */
	/*
	 *
	 */
	public StatusHistoryPopUpMenu(final StatusControlContribution status) {
		super(WorkbenchHelper.getShell(), SWT.NO_TRIM | SWT.NO_FOCUS, false, false, false, false, false, null, null);
		this.status = status;
		dismissOnOutsideClick = event -> {
			if (!(event.widget instanceof Control control)) return;
			final Shell popupShell = getShell();
			if (popupShell != null && !popupShell.isDisposed() && control.getShell() != popupShell
					&& !this.status.isStatusControl(control)) { hide(); }
		};
		final Shell shell = status.getControllingShell();
		shell.addListener(SWT.Move, hide);
		shell.addListener(SWT.Resize, hide);
		shell.addListener(SWT.Close, hide);
		shell.addListener(SWT.Hide, hide);
		final Display display = shell.getDisplay();
		display.addFilter(SWT.Deactivate, hide);
		display.addFilter(SWT.MouseDown, dismissOnOutsideClick);
		shell.addListener(SWT.Dispose, event -> {
			display.removeFilter(SWT.Deactivate, hide);
			display.removeFilter(SWT.MouseDown, dismissOnOutsideClick);
			close();
		});
	}

	@Override
	protected Control createContents(final Composite p) {
		if (parent != p || parent.isDisposed()) {
			this.parent = p;
			GridLayoutFactory.swtDefaults().numColumns(1).margins(0, 0).spacing(0, 0).applyTo(parent);
		}
		createToolbar();
		if (contents == null || contents.isDisposed()) {
			this.contents = (Composite) super.createDialogArea(parent);
			GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.CENTER).applyTo(contents);
			GridLayoutFactory.swtDefaults().numColumns(1).margins(5, 5).spacing(0, 5).applyTo(contents);
		}
		fillLabels();
		contents.requestLayout();
		return contents;
	}

	/**
	 * Fill labels.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param events
	 *            the events
	 * @date 26 août 2023
	 */
	private void fillLabels() {
		int size = events.size();
		for (int i = 0; i < size; i++) {
			try {
				IStatusMessage command = events.get(i);
				Composite labelComposite = getOrCreateLabel(i);
				final Label image = (Label) labelComposite.getChildren()[1];
				final Label time = (Label) labelComposite.getChildren()[0];
				final Label label = (Label) labelComposite.getChildren()[2];
				time.setText(sdf.format(new Date(command.timeStamp())));
				image.setImage(GamaIcon.named(command.icon()).image());
				labelComposite.setData(command);
				setText(label, GamlIdiomsProvider.toText(command.message()));
				label.setForeground(command.isError() ? GamaColors.get(command.color()).color()
						: contents.getForeground());
				label.setToolTipText(command.exception() == null ? command.message() : command.exception().getAllText());
				time.pack(true);
				image.pack(true);
				labelComposite.requestLayout();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (size < labels.size()) {
			needsResize = true;
			for (int i = size; i < labels.size(); i++) {
				labels.get(i).dispose();
				labels.remove(i);
			}
		}

	}

	/**
	 * Gets the or create label.
	 *
	 * @param i
	 *            the i
	 * @return the or create label
	 */
	Composite getOrCreateLabel(final int i) {
		if (i > labels.size() - 1) { labels.add(createLabel()); }
		Composite c = labels.get(i);
		if (c == null || c.isDisposed()) {
			labels.remove(i);
			c = createLabel();
			labels.add(i, c);
		}
		return c;
	}

	/**
	 * Creates the label.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param labels
	 *            the labels
	 * @date 26 août 2023
	 */
	private Composite createLabel() {
		try {
			final Composite labelComposite = new Composite(contents, SWT.NONE);
			GridLayoutFactory.swtDefaults().numColumns(3).margins(0, 0).spacing(5, 0).applyTo(labelComposite);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(labelComposite);
			final Label labelButton = new Label(labelComposite, SWT.NONE);
			final Label timeText = new Label(labelComposite, SWT.NONE);
			final Label labelText = new Label(labelComposite, SWT.NONE);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).applyTo(labelButton);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).applyTo(timeText);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(labelText);

			labelText.addMouseTrackListener(new MouseTrackListener() {

				Color background, foreground;

				@Override
				public void mouseHover(final MouseEvent e) {}

				@Override
				public void mouseEnter(final MouseEvent e) {
					if (background == null && foreground == null) {
						background = labelText.getBackground();
						foreground = labelText.getForeground();
					}
					// We invert the colors
					GamaColors.setBackAndForeground(foreground, background, labelText);
				}

				@Override
				public void mouseExit(final MouseEvent e) {
					GamaColors.setBackAndForeground(background, foreground, labelText);
				}
			});
			labelText.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseDown(final MouseEvent e) {
					hide();
					IStatusMessage message = (IStatusMessage) labelText.getParent().getData();
					if (message.isError()) {
						if (message.exception().getEditorContext() == null) {
							status.showErrorDetails(message.exception());
						} else {
							GAMA.getGui().getModelsManager().editModel(message.exception().getEditorContext());
						}
					} else {
						GAMA.getGui().showView(null, "org.eclipse.ui.views.ProgressView", null,
								IWorkbenchPage.VIEW_ACTIVATE);
					}

				}

			});
			return labelComposite;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates the toolbar.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 25 août 2023
	 */
	@SuppressWarnings ("unused")
	private void createToolbar() {
		// if (toolbarComposite == null || toolbarComposite.isDisposed()) {
		// try {
		// toolbarComposite = new Composite(parent, SWT.BORDER);
		// RowLayoutFactory.swtDefaults().center(true).fill(true).applyTo(toolbarComposite);
		// GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.CENTER).applyTo(toolbarComposite);
		// toolbar = new ToolBar(toolbarComposite, SWT.FLAT | SWT.HORIZONTAL);
		// RowDataFactory.swtDefaults().exclude(false).applyTo(toolbar);
		// GamaCommand
		// .build(StatusMessage.SYSTEM_ICON, "", "Show system events",
		// e -> status.showSystemEvents(!status.showSystemEvents()))
		// .toCheckItem(toolbar).setSelection(status.showSystemEvents());
		// GamaCommand
		// .build(StatusMessage.VIEW_ICON, "", "Show view events",
		// e -> status.showViewEvents(!status.showViewEvents()))
		// .toCheckItem(toolbar).setSelection(status.showViewEvents());
		// } catch (Exception e) {
		// toolbarComposite = null;
		// toolbar = null;
		// return; // will be initialised next time
		// }
		// }
	}

	@Override
	protected boolean hasTitleArea() {
		return false;
	}

	@Override
	protected boolean hasInfoArea() {
		return false;
	}

	@Override
	protected void showDialogMenu() {}

	@Override
	protected void setInfoText(final String text) {}

	@Override
	protected void setTitleText(final String text) {}

	@Override
	protected void saveDialogBounds(final Shell shell) {}

	/**
	 * Gets the default location.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the default location
	 * @date 26 août 2023
	 */
	@Override
	protected Point getDefaultLocation(final Point initialSize) {
		final Point location = status.getLocation();
		final Shell shell = getShell();
		final Point size = initialSize == null && shell != null && !shell.isDisposed() ? shell.getSize() : initialSize;
		final Rectangle area = status.getControllingShell().getMonitor().getClientArea();
		if (size != null) {
			location.x = Math.max(area.x, Math.min(location.x, area.x + area.width - size.x));
			location.y = Math.max(area.y, Math.min(location.y, area.y + area.height - size.y));
		}
		return location;
	}

	@Override
	protected Point getDefaultSize() {
		final Point size = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		final Rectangle area = status.getControllingShell().getMonitor().getClientArea();
		size.x = Math.min(Math.max(size.x, status.getWidth()), area.width);
		return size;
	}

	/**
	 * Checks if is visible.
	 *
	 * @return true, if is visible
	 */
	public boolean isVisible() { return getShell() != null && getShell().isVisible(); }

	/**
	 * Display.
	 */
	public void display() {
		final Shell shell = getShell();
		if (shell != null && !shell.isDisposed()) {
			createContents(parent);
			observePopupShell(shell);
			needsResize = false;
			final Point size = getDefaultSize();
			shell.setSize(size);
			shell.setLocation(getDefaultLocation(size));
			shell.setVisible(true);
		} else {
			open();
			observePopupShell(getShell());
		}
	}

	private void observePopupShell(final Shell shell) {
		if (shell == null || shell.isDisposed() || shell == observedPopupShell) return;
		observedPopupShell = shell;
		shell.addListener(SWT.Deactivate, hide);
		shell.addListener(SWT.Dispose, event -> observedPopupShell = null);
	}

	/**
	 * Updates the contents without changing the popup's visibility.
	 */
	public void refresh() {
		final Shell shell = getShell();
		if (shell == null || shell.isDisposed() || !shell.isVisible()) return;
		createContents(parent);
		if (!needsResize) return;
		needsResize = false;
		final Point size = getDefaultSize();
		// Grows only: shorter texts do not shrink the popup while it is visible
		size.x = Math.max(size.x, shell.getSize().x);
		if (!size.equals(shell.getSize())) {
			shell.setSize(size);
		} else {
			shell.layout(true, true);
		}
		final Point location = getDefaultLocation(size);
		if (!location.equals(shell.getLocation())) { shell.setLocation(location); }
	}

	/**
	 * Hide.
	 */
	public void hide() {
		final Shell shell = getShell();
		if (shell != null && !shell.isDisposed() && shell.isVisible()) {
			shell.setVisible(false);
			lastHideTime = System.currentTimeMillis();
		}
	}

	/**
	 * Whether the popup has been hidden within the last few milliseconds. Used to prevent a click on the status control
	 * from reopening the popup it has just dismissed (through a deactivation event delivered before the click).
	 */
	public boolean wasJustHidden() {
		return System.currentTimeMillis() - lastHideTime < REOPEN_DELAY;
	}

	/**
	 * Wipe.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 31 août 2023
	 */
	public void wipe() {
		for (Control c : labels) { if (!c.isDisposed()) { c.setData(null); } }
	}

	/**
	 * @param gamaCommand
	 */
	public void addStatus(final IStatusMessage gc) {
		String msg = gc.message();
		if (Strings.isNullOrEmpty(msg)) return;
		Iterator<IStatusMessage> iterator = events.iterator();
		while (iterator.hasNext()) {
			IStatusMessage event = iterator.next();
			if (msg.equals(event.message())) {
				iterator.remove();
				break;
			}
		}
		events.add(gc);
	}

	public void addFinishedStatus(final String taskName, final IStatusMessage finishedStatus) {
		events.removeIf(event -> taskName.equals(event.message()));
		addStatus(finishedStatus);
	}

}
