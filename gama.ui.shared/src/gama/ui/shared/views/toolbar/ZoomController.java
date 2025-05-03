/*******************************************************************************************************
 *
 * ZoomController.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.views.toolbar;

import static gama.ui.shared.resources.IGamaIcons.DISPLAY_TOOLBAR_CAMERA;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.GestureListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolItem;

import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.resources.GamaIcon;
import gama.ui.shared.resources.IGamaIcons;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * Class ZoomController.
 *
 * @author drogoul
 * @since 9 févr. 2015
 *
 */
public class ZoomController {

	/** The including scrolling. */
	// Fix for Issue #1291
	final boolean includingScrolling;

	/** The view. */
	final IToolbarDecoratedView.Zoomable view;

	/** The camera locked. */
	ToolItem cameraLocked;

	/**
	 * @param view
	 */
	public ZoomController(final IToolbarDecoratedView.Zoomable view) {
		this.view = view;
		this.includingScrolling = view.zoomWhenScrolling();
	}

	/**
	 * @param tb
	 */
	public void install(final GamaToolbar2 tb) {
		final GestureListener gl = ge -> {
			if (ge.detail == SWT.GESTURE_MAGNIFY) {
				if (ge.magnification > 1.0) {
					view.zoomIn();
				} else if (ge.magnification < 1.0) { view.zoomOut(); }
			}

		};

		final MouseListener ml = new MouseAdapter() {

			@Override
			public void mouseDoubleClick(final MouseEvent e) {
				if (e.button == 1) { view.zoomFit(); }
			}
		};

		final MouseWheelListener mw = e -> {
			if (e.count < 0) {
				view.zoomOut();
			} else {
				view.zoomIn();
			}
		};

		tb.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(final ControlEvent e) {
				final Control[] controls = view.getZoomableControls();
				for (final Control c : controls) {
					if (c != null) {
						c.addGestureListener(gl);
						c.addMouseListener(ml);
						if (includingScrolling) { c.addMouseWheelListener(mw); }
						// once installed the listener removes itself from the
						// toolbar
					}
				}
				if (cameraLocked != null) {
					boolean locked = view.isCameraLocked();
					boolean dynamic = view.isCameraDynamic();
					tb.setSelection(cameraLocked, locked || dynamic);
					// If the camera is dynamic, we disable the control (see #350)
					cameraLocked.setEnabled(!dynamic);
				}
				tb.removeControlListener(this);
			}

		});
		tb.button(view.largeZoomIcons() ? IGamaIcons.DISPLAY_TOOLBAR_ZOOMIN : "toolbar/font.increase", "Zoom in",
				"Zoom in", e -> view.zoomIn(), SWT.RIGHT);
		tb.button(view.largeZoomIcons() ? IGamaIcons.DISPLAY_TOOLBAR_ZOOMFIT : "toolbar/font.reset", "Zoom fit",
				"Zoom to fit view", e -> view.zoomFit(), SWT.RIGHT);
		tb.button(view.largeZoomIcons() ? IGamaIcons.DISPLAY_TOOLBAR_ZOOMOUT : "toolbar/font.decrease", "Zoom out",
				"Zoom out", e -> view.zoomOut(), SWT.RIGHT);
		tb.sep(SWT.RIGHT);
		if (view.hasCameras()) {
			tb.menu(DISPLAY_TOOLBAR_CAMERA, "", "Choose a camera...", trigger -> {
				final GamaMenu menu = new GamaMenu() {

					@Override
					protected void fillMenu() {
						final Collection<String> cameras = view.getCameraHelper().getCameraNames();

						for (final String p : cameras) {
							action(p, new SelectionAdapter() {

								@Override
								public void widgetSelected(final SelectionEvent e) {
									view.getCameraHelper().setCameraName(p);
									cameraLocked.setSelection(view.isCameraLocked());
								}

							}, p.equals(view.getCameraHelper().getCameraName())
									? GamaIcon.named(DISPLAY_TOOLBAR_CAMERA).image()
									: GamaIcon.named(IGamaIcons.CAMERA_EMPTY).image());
						}
						sep();
						action("Copy current camera", new SelectionAdapter() {

							@Override
							public void widgetSelected(final SelectionEvent e) {
								final String text = view.getCameraHelper().getCameraDefinition();
								WorkbenchHelper.copy(text);
							}

						}, GamaIcon.named(IGamaIcons.PASTE).image());
					}

				};
				menu.open(tb.getToolbar(SWT.RIGHT), trigger, tb.getToolbar(SWT.RIGHT).getSize().y, 96);
			}, SWT.RIGHT);
			cameraLocked = tb.check(IGamaIcons.CAMERA_LOCK, "Lock/unlock", "Lock/unlock view",
					e -> { view.toggleLock(); }, SWT.RIGHT);
		}

	}

}
