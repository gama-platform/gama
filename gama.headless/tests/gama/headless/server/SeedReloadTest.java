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

import gama.api.kernel.species.IExperimentSpecies;
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

	private static void reload(final IGamaServer server, final Double seed) {
		final Map<String, Object> command =
				seed == null ? Map.of("type", "reload") : Map.of("type", "reload", "seed", seed);
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
					false, false, false, false, 12d);
			check(job.controller.processOpen(true, 321d), "Loading the seed model failed");
			final var plan = job.controller.getExperiment();
			check(plan.getCurrentSimulation().getSeed() == 321d, "An explicit seed must override the job seed");
			final var server = (IGamaServer) Proxy.newProxyInstance(IGamaServer.class.getClassLoader(),
					new Class<?>[] { IGamaServer.class }, (proxy, method, args) -> {
						if ("retrieveExperimentPlan".equals(method.getName())) return plan;
						throw new UnsupportedOperationException(method.getName());
					});
			final double originalValue = randomValue(plan);

			reload(server, 42d);
			check(plan.getAgent().getSeed() == 42d, "The experiment must use the requested seed");
			check(plan.getCurrentSimulation().getSeed() == 42d, "The simulation must use the requested seed");
			final double seededValue = randomValue(plan);
			check(seededValue != originalValue, "A different seed must change initialization-time randomness");
			reload(server, 42d);
			check(randomValue(plan) == seededValue, "The same requested seed must reproduce the draw");

			reload(server, null);
			final double randomSeed = plan.getCurrentSimulation().getSeed();
			check(randomSeed != 42d, "Omitting seed must draw a new seed");
			reload(server, null);
			final double nextSeed = plan.getCurrentSimulation().getSeed();
			check(nextSeed != randomSeed, "Reload must renew a previously random seed");
			reload(server, 0d);
			check(plan.getCurrentSimulation().getSeed() != nextSeed, "Zero must draw a new seed");
		} finally {
			try {
				if (job != null && job.controller.getExperiment() != null) { job.controller.close(); }
			} finally {
				Files.delete(file);
			}
		}
	}
}
