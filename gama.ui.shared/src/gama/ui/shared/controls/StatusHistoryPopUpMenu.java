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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;

import com.google.common.base.Strings;
import com.google.common.collect.ForwardingList;

import gama.core.common.IStatusMessage;
import gama.core.common.StatusMessage;
import gama.core.runtime.GAMA;
import gama.dev.DEBUG;
import gama.gaml.compilation.GamlIdiomsProvider;
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
		super(WorkbenchHelper.getShell(), PopupDialog.HOVER_SHELLSTYLE, false, false, false, false, false, null, null);
		this.status = status;
		final Shell shell = status.getControllingShell();
		shell.addListener(SWT.Move, hide);
		shell.addListener(SWT.Resize, hide);
		shell.addListener(SWT.Close, hide);
		shell.addListener(SWT.Deactivate, hide);
		shell.addListener(SWT.Hide, hide);
		shell.addListener(SWT.Dispose, event -> close());
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
				label.setText(GamlIdiomsProvider.toText(command.message()));
				label.setToolTipText(command.message());
				time.pack(true);
				image.pack(true);
				labelComposite.requestLayout();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (size < labels.size()) {
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
					StatusMessage message = (StatusMessage) labelText.getParent().getData();
					if (message.isError()) {
						GAMA.getGui().editModel(message.exception().getEditorContext());
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
		return status.getLocation();
	}

	@Override
	protected Point getDefaultSize() {
		int width = status.getWidth();
		if (width <= 0) { width = SWT.DEFAULT; }
		return getShell().computeSize(width, SWT.DEFAULT, true);
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
			shell.setLocation(getDefaultLocation(null));
			shell.setSize(getDefaultSize());
			shell.setVisible(true);
		} else {
			open();
		}
	}

	/**
	 * Hide.
	 */
	public void hide() {
		final Shell shell = getShell();
		if (shell != null && !shell.isDisposed()) { shell.setVisible(false); }
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

}
