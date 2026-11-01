package gama.ui.export.launcher;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.splash.AbstractSplashHandler;

import gama.export.StartupSplash;

/** Reuses the startup renderer installed before GAMA's core bundles are activated. */
public final class PlayerSplashHandler extends AbstractSplashHandler {

	@Override
	public void init(final Shell splash) {
		super.init(splash);
		StartupSplash.install(splash);
	}
}
