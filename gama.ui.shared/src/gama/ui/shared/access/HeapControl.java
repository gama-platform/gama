package gama.ui.shared.access;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolItem;

import gama.ui.shared.views.toolbar.GamaToolbarSimple;

public class HeapControl {

	ToolItem item;

	void displayOn(final Composite parent) {
		GamaToolbarSimple bar = new GamaToolbarSimple(parent, SWT.NONE);
		item = bar.button("generic/garbage.collect", "", "", e -> { System.gc(); });
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).indent(30, 0).applyTo(bar);
		bar.addListener(SWT.MouseEnter, e -> updateToolTip());
	}

	protected void updateToolTip() {
		Runtime runtime = Runtime.getRuntime();
		long totalMem = convertToMeg(runtime.totalMemory());
		item.setToolTipText(
				"Memory used: " + (totalMem - convertToMeg(runtime.freeMemory())) + "M on " + totalMem + "M");
	}

	private long convertToMeg(final long numBytes) {
		return (numBytes + 512 * 1024) / (1024 * 1024);
	}

}
