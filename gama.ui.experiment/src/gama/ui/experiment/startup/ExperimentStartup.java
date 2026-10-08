/*******************************************************************************************************
 *
 * ExperimentStartup.java, in gama.ui.experiment, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.experiment.startup;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.commands.ICommandService;

import gama.dev.DEBUG;
import gama.ui.application.workbench.PerspectiveHelper;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 * Preloads expensive first-use resources shortly after workbench startup, so that the very first launch of a simulation
 * experiment is as fast as subsequent ones.
 *
 * <h2>What is pre-warmed and why</h2>
 * <ul>
 * <li><b>{@link WorkbenchHelper#SERVICES} cache</b> — {@link EModelService}, {@link EPartService}, {@link MApplication}
 * and {@link ICommandService} are looked up on every call to {@link gama.ui.experiment.commands.ArrangeDisplayViews}.
 * The Guava {@code LoadingCache} calls {@code IWorkbench.getService} on first access; pre-warming avoids that overhead
 * on the critical display-layout path.</li>
 * <li><b>{@link PerspectiveHelper#prewarmPerspective()}</b> — constructs a throw-away
 * {@link gama.ui.application.workbench.SimulationPerspectiveDescriptor} so that the reflection-based write into
 * {@code PerspectiveRegistry} happens at startup, not when the user clicks the experiment button.</li>
 * <li><b>{@link GamaIcon#preloadAllIcons()}</b> — walks the SVG icon tree and populates the icon cache. Without
 * pre-loading, every icon lookup during view creation causes a file-system walk and SVG rasterization on the UI
 * thread.</li>
 * <li><b>{@link IGamaColors} class loading</b> — the interface's static fields allocate SWT
 * {@link org.eclipse.swt.graphics.Color} objects. Loading the class early ensures those allocations happen before the
 * first display view is painted.</li>
 * <li><b>{@link ThemeHelper#isDark()}</b> — triggers the CSS-engine theme detection, which can be slow on first
 * call.</li>
 * </ul>
 *
 * <p>
 * All work runs on a low-priority daemon thread, started with a short delay so the workbench finishes its own startup
 * first. SWT-resource creation (colors, images) is marshalled onto the UI thread via {@link WorkbenchHelper#runInUI}.
 * </p>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 */
public class ExperimentStartup implements IStartup {

	static {
		DEBUG.OFF();
	}

	/**
	 * Delay in milliseconds before the pre-warm thread starts. Gives the workbench time to finish its own startup
	 * before we compete for class-loading and I/O resources.
	 */
	private static final int PREWARM_DELAY_MS = 3000;

	@Override
	public void earlyStartup() {
		Thread.ofPlatform().name("GAMA-experiment-prewarmer").daemon(true).start(() -> {
			try {
				Thread.sleep(PREWARM_DELAY_MS);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			prewarm();
		});
	}

	/**
	 * Runs all pre-warm tasks from the background thread.
	 */
	private static void prewarm() {
		DEBUG.OUT("ExperimentStartup: pre-warm starting");

		// 1. Warm the WorkbenchHelper service cache — pure Java, safe from any thread.
		prewarmServices();

		// 2. Warm the SimulationPerspectiveDescriptor (reflection) — must be on the UI thread.
		WorkbenchHelper.runInUI("Pre-warming simulation perspective", 0, m -> PerspectiveHelper.prewarmPerspective());

		// 4. Force IGamaColors class loading — its static fields allocate SWT Color objects,
		// which need the Display. Referencing any field triggers the class initializer.
		WorkbenchHelper.runInUI("Pre-warming GAMA colors", 0, m -> {
			try {
				// Touching one field is enough to trigger full interface static-field initialization.
				@SuppressWarnings ("unused") final var unused = IGamaColors.BLUE;
				DEBUG.OUT("ExperimentStartup: IGamaColors pre-warmed");
			} catch (final Exception e) {
				DEBUG.ERR("ExperimentStartup: color pre-warm failed — " + e.getMessage());
			}
		});

		DEBUG.OUT("ExperimentStartup: pre-warm tasks submitted");
	}

	/**
	 * Pre-warms the {@link WorkbenchHelper#SERVICES} Guava cache for the E4 services used in every
	 * {@code ArrangeDisplayViews} and {@code PerspectiveHelper} call.
	 */
	private static void prewarmServices() {
		try {
			WorkbenchHelper.getService(EModelService.class);
			WorkbenchHelper.getService(EPartService.class);
			WorkbenchHelper.getService(MApplication.class);
			WorkbenchHelper.getService(ICommandService.class);
			DEBUG.OUT("ExperimentStartup: E4 services pre-warmed");
		} catch (final Exception e) {
			DEBUG.ERR("ExperimentStartup: service pre-warm failed — " + e.getMessage());
		}
	}

}