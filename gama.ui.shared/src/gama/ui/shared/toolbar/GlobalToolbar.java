/*******************************************************************************************************
 *
 * GlobalToolbar.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.toolbar;

import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimBarLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;

import gama.dev.DEBUG;
import gama.ui.shared.access.GamlAccessContents2;
import gama.ui.shared.controls.StatusControlContribution;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 * The Class GlobalToolbar.
 */
public class GlobalToolbar {

	static {
		DEBUG.OFF();
	}

	/** The item. */
	ToolItem memoryItem;

	/**
	 * Display on.
	 *
	 * @param parent
	 *            the parent
	 * @return the control
	 */
	Control displayOn(final Composite parent) {
		TrimBarLayout tbl = (TrimBarLayout) parent.getLayout();
		tbl.marginTop = 10;
		tbl.marginBottom = 10;
		tbl.marginLeft = 10;
		tbl.marginRight = 10;
		GamaToolbarSimple bar = new GamaToolbarSimple(parent, SWT.NONE);
		bar.space(20);
		bar.button("editor/command.find", null, "Search GAML reference", e -> {
			final GamlAccessContents2 quickAccessDialog = new GamlAccessContents2();
			quickAccessDialog.open();
		});
		memoryItem = new MemoryControl().installOn(bar);

		new StatusControlContribution().fill(bar, 0);
		parent.requestLayout();
		parent.addControlListener(new ControlListener() {

			@Override
			public void controlResized(final ControlEvent e) {
				DEBUG.OUT("Size of parent : " + parent.getSize());
				// DEBUG.OUT("Size of composite : " + composite.getSize());
				DEBUG.OUT("Size of toolbar : " + bar.getSize());
				parent.requestLayout();
			}

			@Override
			public void controlMoved(final ControlEvent e) {}
		});
		return bar;
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
						element.setWidget(new GlobalToolbar().displayOn(parent));
						parent.requestLayout();
						break;
					}
				}
			}
		});
	}

}
