/*******************************************************************************************************
 *
 * DisplayLayoutFactory.java, in gama.ui.shared.experiment, is part of the source code of the GAMA modeling and
 * simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.factories;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import gama.ui.experiment.commands.ArrangeDisplayViews;
import gama.ui.shared.interfaces.IDisplayLayoutManager;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * A factory for creating DisplayLayout objects.
 */
public class DisplayLayoutFactory extends AbstractServiceFactory implements IDisplayLayoutManager {

	@SuppressWarnings("rawtypes")
	@Override
	public Object create(final Class serviceInterface, final IServiceLocator parentLocator,
			final IServiceLocator locator) {
		return this;
	}

	/**
	 * Applies the given layout by scheduling it as a {@link org.eclipse.core.runtime.jobs.Job UIJob} rather than a raw
	 * SWT {@code asyncExec}.
	 *
	 * <p>
	 * The individual display-view openers in {@link gama.core.outputs.AbstractOutput#open()} are also submitted as
	 * {@code UIJob}s (via {@link WorkbenchHelper#runInUI}). The Eclipse job scheduler processes UIJobs in
	 * first-in-first-out order for the same priority, so submitting the layout as a UIJob <em>after</em> the N opener
	 * UIJobs guarantees that all views exist before {@link ArrangeDisplayViews#execute} runs.
	 * </p>
	 * <p>
	 * The previous {@code asyncRun} (SWT {@code asyncExec}) did not share a queue with UIJobs and could execute before
	 * the opener UIJobs completed, producing the "split-view flash" on first launch.
	 * </p>
	 *
	 * @param layout
	 *            the layout descriptor (Integer index, {@link gama.api.utils.collections.GamaTree} or
	 *            {@link gama.api.utils.collections.GamaNode})
	 */
	@Override
	public void applyLayout(final Object layout) {
		WorkbenchHelper.runInUI("Applying display layout", 0, m -> ArrangeDisplayViews.execute(layout));
	}

	/**
	 * Applies the layout synchronously on the current (UI) thread. Called from
	 * {@link gama.ui.shared.utils.SwtGui#openAndApplyLayout} which is already running inside a
	 * {@code syncExec} under {@code shell.setRedraw(false)}, so no further scheduling is needed.
	 *
	 * @param layout
	 *            the layout descriptor
	 */
	@Override
	public void applyLayoutNow(final Object layout) {
		ArrangeDisplayViews.execute(layout);
	}

}
