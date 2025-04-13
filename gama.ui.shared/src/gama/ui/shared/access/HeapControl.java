/*******************************************************************************************************
 *
 * HeapControl.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.access;

import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;

import gama.core.common.IStatusMessage;
import gama.core.runtime.GAMA;
import gama.dev.DEBUG;
import gama.ui.shared.controls.StatusControlContribution;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 * The Class HeapControl.
 */
public class HeapControl {

	static {
		DEBUG.ON();
	}

	/** The item. */
	ToolItem item;

	/**
	 * Display on.
	 *
	 * @param parent
	 *            the parent
	 * @return the control
	 */
	Control displayOn(final Composite parent) {
		// TrimBarLayout layout = (TrimBarLayout) parent.getLayout();
		// // layout.marginTop = 10;
		// // layout.marginBottom = 10;
		// layout.marginLeft = 10;
		// layout.marginRight = 10;

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0).extendedMargins(0, 5, 5, 5).numColumns(2)
				.equalWidth(false).applyTo(composite);
		GamaToolbarSimple bar = new GamaToolbarSimple(composite, SWT.NONE);
		bar.space(20);
		bar.button("editor/command.find", null, "Search GAML reference", e -> {
			final GamlAccessContents2 quickAccessDialog = new GamlAccessContents2();
			quickAccessDialog.open();
		});
		item = bar.button("generic/garbage.collect", "", "", e -> {
			Runtime runtime = Runtime.getRuntime();
			long totalMem = convertToMeg(runtime.totalMemory());
			System.gc();
			totalMem = convertToMeg(runtime.totalMemory());
			GAMA.getGui().getStatus().informStatus(
					"Compact memory (" + (totalMem - convertToMeg(runtime.freeMemory())) + "M on " + totalMem + "M)",
					IStatusMessage.MEMORY_ICON);
		});
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).indent(30, 0).applyTo(bar);
		bar.addListener(SWT.MouseEnter, e -> updateToolTip());

		new StatusControlContribution().fill(bar, 0);
		parent.requestLayout();
		parent.addControlListener(new ControlListener() {

			@Override
			public void controlResized(final ControlEvent e) {
				DEBUG.OUT("Size of parent : " + parent.getSize());
				DEBUG.OUT("Size of composite : " + composite.getSize());
				DEBUG.OUT("Size of toolbar : " + bar.getSize());
				parent.requestLayout();
			}

			@Override
			public void controlMoved(final ControlEvent e) {}
		});
		return composite;
	}

	/**
	 * Update tool tip.
	 */
	protected void updateToolTip() {
		Runtime runtime = Runtime.getRuntime();
		long totalMem = convertToMeg(runtime.totalMemory());
		item.setToolTipText(
				"Memory used: " + (totalMem - convertToMeg(runtime.freeMemory())) + "M on " + totalMem + "M");
	}

	/**
	 * Convert to meg.
	 *
	 * @param numBytes
	 *            the num bytes
	 * @return the long
	 */
	private long convertToMeg(final long numBytes) {
		return (numBytes + 512 * 1024) / (1024 * 1024);
	}

	/**
	 * Install.
	 */
	public static void install() {
		WorkbenchHelper.runInUI("Install GAMA Status and Heap Controls", 0, m -> {
			final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window instanceof WorkbenchWindow ww) {
				final MTrimBar topTrim = ww.getTopTrim();
				for (final MTrimElement element : topTrim.getChildren()) {
					if ("SearchField".equals(element.getElementId())) {
						final Composite parent = ((Control) element.getWidget()).getParent();
						final Control old = (Control) element.getWidget();
						WorkbenchHelper.asyncRun(() -> old.dispose(), 500, () -> true);
						element.setWidget(new HeapControl().displayOn(parent));
						parent.requestLayout();
						break;
					}
				}
			}
		});
	}

}
