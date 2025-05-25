/*******************************************************************************************************
 *
 * MemoryControl.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.toolbar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolItem;

import gama.core.common.IStatusMessage;
import gama.core.runtime.GAMA;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 *
 */
public class MemoryControl {

	/**
	 * Install on.
	 *
	 * @param toolbar
	 *            the toolbar
	 * @return the tool item
	 */
	public ToolItem installOn(final GamaToolbarSimple toolbar) {
		ToolItem item = toolbar.button("generic/garbage.collect", "", "", e -> {
			Runtime runtime = Runtime.getRuntime();
			long totalMem = convertToMeg(runtime.totalMemory());
			System.gc();
			totalMem = convertToMeg(runtime.totalMemory());
			GAMA.getGui().getStatus().informStatus(
					"Compact memory (" + (totalMem - convertToMeg(runtime.freeMemory())) + "M on " + totalMem + "M)",
					IStatusMessage.MEMORY_ICON);
		});

		toolbar.addListener(SWT.MouseEnter, e -> {
			Runtime runtime = Runtime.getRuntime();
			long totalMem = convertToMeg(runtime.totalMemory());
			item.setToolTipText(
					"Memory used: " + (totalMem - convertToMeg(runtime.freeMemory())) + "M on " + totalMem + "M");
		});
		return item;
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

}
