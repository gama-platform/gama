package gama.ui.application;

/** Uses the real modeler workbench but bypasses its interactive workspace picker in smoke checks. */
public final class NativeModelerProbe extends Application {

	@Override
	protected Object checkWorkspace() { return null; }
}
