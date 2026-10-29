package gama.ui.export.launcher;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.splash.EclipseSplashHandler;
import org.eclipse.ui.splash.AbstractSplashHandler;

/** Leaves player artwork untouched while preserving the normal product's splash behavior. */
public final class PlayerSplashHandler extends AbstractSplashHandler {

	private EclipseSplashHandler standard;

	@Override
	public void init(final Shell splash) {
		super.init(splash);
		if (!Boolean.getBoolean("exported_runner")) {
			standard = new EclipseSplashHandler();
			standard.init(splash);
		}
	}

	@Override
	public IProgressMonitor getBundleProgressMonitor() {
		return standard == null ? null : standard.getBundleProgressMonitor();
	}

	@Override
	public void dispose() {
		if (standard != null) standard.dispose();
		else super.dispose();
	}
}
