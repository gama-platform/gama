/*******************************************************************************************************
 *
 * SeedReloadTest.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.server;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.Map;

import gama.api.gaml.types.Types;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.types.map.GamaMapFactory;
import gama.api.utils.server.DefaultServerCommands;
import gama.api.utils.server.IGamaServer;
import gama.api.utils.server.MessageType;

/**
 * Regression checks for #733. Invoke {@link #run()} after initializing the headless GAMA runtime.
 */
public final class SeedReloadTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static double randomValue(final IExperimentSpecies plan) {
		final var simulation = plan.getCurrentSimulation();
		return ((Number) simulation.getDirectVarValue(simulation.getScope(), "r")).doubleValue();
	}

	private static IList<?> parameters(final String name, final double seed) {
		final var result = GamaListFactory.create(Types.MAP);
		result.add(GamaMapFactory.wrap(Map.of("name", name, "type", "float", "value", seed)));
		return result;
	}

	private static void reload(final IGamaServer server, final IList<?> parameters) {
		final Map<String, Object> command = parameters == null ? Map.of("type", "reload")
				: Map.of("type", "reload", "parameters", parameters);
		final var response = DefaultServerCommands.RELOAD(server, null, command);
		check(response.type == MessageType.CommandExecutedSuccessfully, "Reload failed: " + response.content);
	}

	public static void run() throws Exception {
		final var file = Files.createTempFile("seed-reload-", ".gaml");
		GamaServerExperimentJob job = null;
		try {
			Files.writeString(file, """
					model seedtest
					global {
						float r;
						init { r <- rnd(10.0); }
					}
					experiment main type: gui {}
					""");
			job = new GamaServerExperimentJob(file.toString(), "main", null, null, "",
					false, false, false, false);
			check(job.controller.processOpen(true), "Loading the seed model failed");
			final var plan = job.controller.getExperiment();
			final var server = (IGamaServer) Proxy.newProxyInstance(IGamaServer.class.getClassLoader(),
					new Class<?>[] { IGamaServer.class }, (proxy, method, args) -> {
						if ("retrieveExperimentPlan".equals(method.getName())) return plan;
						throw new UnsupportedOperationException(method.getName());
					});
			final double originalSeed = plan.getAgent().getSeed();
			final double originalValue = randomValue(plan);
			for (int i = 0; i < 3; i++) {
				reload(server, null);
				check(plan.getAgent().getSeed() == originalSeed, "Reload must retain the experiment seed");
				check(plan.getCurrentSimulation().getSeed() == originalSeed, "Reload must retain the simulation seed");
				check(randomValue(plan) == originalValue, "Reload must reproduce the initialization-time draw");
			}

			reload(server, parameters("seed", 42d));
			check(plan.getAgent().getSeed() == 42d, "The experiment must use the requested seed");
			check(plan.getCurrentSimulation().getSeed() == 42d, "The simulation must use the requested seed");
			final double seededValue = randomValue(plan);
			check(seededValue != originalValue, "A different seed must change initialization-time randomness");
			reload(server, parameters("seed", 42d));
			check(randomValue(plan) == seededValue, "The same requested seed must reproduce the draw");
			reload(server, null);
			check(randomValue(plan) == seededValue, "Omitting parameters must retain the last requested seed");

			reload(server, parameters("Default random seed", 123d));
			check(plan.getAgent().getSeed() == 123d, "Seed parameters must also be accepted by title");
			check(plan.getCurrentSimulation().getSeed() == 123d, "A seed supplied by title must reach the simulation");
			check(randomValue(plan) != seededValue, "Changing the seed must affect the simulation's init");

			double previousValue = randomValue(plan);
			for (int i = 0; i < 3; i++) {
				reload(server, parameters("seed", 0d));
				check(randomValue(plan) != previousValue, "A zero seed must choose new randomness on every reload");
				previousValue = randomValue(plan);
			}
		} finally {
			try {
				if (job != null && job.controller.getExperiment() != null) { job.controller.close(); }
			} finally {
				Files.delete(file);
			}
		}
	}
}
