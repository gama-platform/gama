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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;

import gama.api.GAMA;
import gama.api.ui.IStatusMessage;
import gama.dev.DEBUG;
import gama.ui.shared.controls.StatusControlContribution;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 * The Class HeapControl.
 */
public class HeapControl {

    static {
	DEBUG.OFF();
    }

    /** Refresh period of the memory label, in ms. */
    private static final int REFRESH_DELAY = 2000;

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
	GamaToolbarSimple bar = new GamaToolbarSimple(composite, SWT.RIGHT);
	bar.space(16);
	bar.button("editor/command.find", null, "Search GAML reference", e -> {
	    final GamlAccessContents2 quickAccessDialog = new GamlAccessContents2();
	    quickAccessDialog.open();
	});
	item = bar.button("generic/garbage.collect", "", "", e -> {
	    System.gc();
	    updateLabel(bar);
	    Runtime runtime = Runtime.getRuntime();
	    long totalMem = convertToMeg(runtime.totalMemory());
	    GAMA.getGui().getStatus().informStatus(
		    "Memory after garbage collection: " + (totalMem - convertToMeg(runtime.freeMemory())) + "M used on " + totalMem + "M",
		    IStatusMessage.MEMORY_ICON);
	});
	GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).indent(16, 0).applyTo(bar);
	bar.addListener(SWT.MouseEnter, e -> updateToolTip());
	updateLabel(bar);
	final Display display = parent.getDisplay();
	display.timerExec(REFRESH_DELAY, new Runnable() {

	    @Override
	    public void run() {
		if (bar.isDisposed()) return;
		updateLabel(bar);
		display.timerExec(REFRESH_DELAY, this);
	    }
	});

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
	    public void controlMoved(final ControlEvent e) {
	    }
	});
	return composite;
    }

    /**
     * Update tool tip.
     */
    protected void updateToolTip() {
	Runtime runtime = Runtime.getRuntime();
	long maxMem = convertToMeg(runtime.maxMemory());
	long usedMem = convertToMeg(runtime.totalMemory() - runtime.freeMemory());
	item.setToolTipText("Memory used: " + usedMem + "M over " + maxMem + "M. Click to free unused memory.");
    }

    /**
     * Updates the "MEM xx%" label to show used heap as a percentage of maximum heap.
     */
    private void updateLabel(final GamaToolbarSimple bar) {
	if (item == null || item.isDisposed()) return;
	Runtime runtime = Runtime.getRuntime();
	int percent = (int) Math.round(100d * (runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory());
	String text = "MEM " + percent + "%";
	if (!text.equals(item.getText())) {
	    item.setText(text);
	    bar.requestLayout();
	    bar.getParent().requestLayout();
	}
	updateToolTip();
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
