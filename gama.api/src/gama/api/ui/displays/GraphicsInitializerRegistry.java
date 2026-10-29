package gama.api.ui.displays;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.FutureTask;

/** Registration must remain UI-independent: display bundles activate before the workbench exists. */
public final class GraphicsInitializerRegistry {

	private static final CopyOnWriteArrayList<FutureTask<Void>> INITIALIZERS = new CopyOnWriteArrayList<>();

	private GraphicsInitializerRegistry() {}

	public static void register(final Runnable initializer) {
		INITIALIZERS.add(new FutureTask<>(initializer, null));
	}

	public static List<FutureTask<Void>> initializers() {
		return List.copyOf(INITIALIZERS);
	}
}
