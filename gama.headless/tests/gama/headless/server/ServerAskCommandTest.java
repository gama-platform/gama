package gama.headless.server;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gama.api.GAMA;
import gama.api.compilation.factories.IExpressionDescriptionFactory;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.statements.IStatement;
import gama.api.gaml.symbols.Arguments;
import gama.api.gaml.types.GamaMapType;
import gama.api.gaml.types.Types;
import gama.api.kernel.agent.AgentReference;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.agent.IPopulation;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.ISpecies;
import gama.api.runtime.scope.IExecutionResult;
import gama.api.runtime.scope.IScope;
import gama.api.utils.server.DefaultServerCommands;
import gama.api.utils.server.GamaServerMessage;
import gama.api.utils.server.IGamaServer;
import gama.api.utils.server.MessageType;
import gama.core.util.json.Json;

/** Standalone regression checks for #990; uses real JSON and fails if lookup creates agents. */
public final class ServerAskCommandTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static <T> T proxy(final Class<T> type, final InvocationHandler handler) {
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler));
	}

	private static final class Fixture {
		boolean dead;
		int executions;
		int cleared;
		IAgent target;
		Arguments arguments;

		final IStatement.WithArgs action = proxy(IStatement.WithArgs.class, (p, method, args) -> {
			throw new AssertionError("The scope must execute the action: " + method);
		});
		final ISpecies species = proxy(ISpecies.class, (p, method, args) -> {
			if ("getAction".equals(method.getName())) return "say_hello".equals(args[0]) ? action : null;
			throw new AssertionError("Unexpected species call: " + method);
		});
		final IScope actionScope = proxy(IScope.class, (p, method, args) -> switch (method.getName()) {
			case "execute" -> {
				check(args[0] == action, "Wrong action");
				target = (IAgent) args[1];
				arguments = (Arguments) args[2];
				executions++;
				yield IExecutionResult.PASSED;
			}
			case "clear" -> { cleared++; yield null; }
			default -> throw new AssertionError("Unexpected action scope call: " + method);
		});
		final IScope scope = proxy(IScope.class, (p, method, args) -> switch (method.getName()) {
			case "getExperiment" -> experimentAgent();
			case "copy" -> actionScope;
			default -> throw new AssertionError("Unexpected scope call: " + method);
		});
		final IAgent boid = proxy(IAgent.class, (p, method, args) -> switch (method.getName()) {
			case "dead" -> dead;
			case "getScope" -> scope;
			case "getSpecies" -> species;
			case "getPopulationFor" -> "nested".equals(args[0]) ? population() : null;
			default -> throw new AssertionError("Unexpected boid call: " + method);
		});
		final IPopulation<?> boids = proxy(IPopulation.class, (p, method, args) -> {
			if ("getAgent".equals(method.getName())) return Integer.valueOf(12).equals(args[0]) ? boid : null;
			throw new AssertionError("Lookup must not create agents: " + method);
		});
		final ISimulationAgent simulation = proxy(ISimulationAgent.class, (p, method, args) ->
				switch (method.getName()) {
					case "dead" -> false;
					case "getPopulationFor" -> "boids".equals(args[0]) ? boids : null;
					default -> throw new AssertionError("Unexpected simulation call: " + method);
				});
		final IPopulation.Simulation simulations = proxy(IPopulation.Simulation.class, (p, method, args) -> {
			if ("getAgent".equals(method.getName())) return Integer.valueOf(0).equals(args[0]) ? simulation : null;
			throw new AssertionError("Lookup must not create simulations: " + method);
		});
		final IExperimentAgent experiment = proxy(IExperimentAgent.class, (p, method, args) ->
				switch (method.getName()) {
					case "getSimulationPopulation" -> simulations;
					case "getScope" -> scope;
					case "getSpecies" -> species;
					case "dead" -> false;
					default -> throw new AssertionError("Unexpected experiment call: " + method);
				});
		final IExperimentSpecies plan = proxy(IExperimentSpecies.class, (p, method, args) -> {
			if ("getAgent".equals(method.getName())) return experiment;
			throw new AssertionError("Unexpected plan call: " + method);
		});
		final IGamaServer server = proxy(IGamaServer.class, (p, method, args) -> {
			if ("retrieveExperimentPlan".equals(method.getName())) return plan;
			throw new AssertionError("Unexpected server call: " + method);
		});

		private IExperimentAgent experimentAgent() { return experiment; }

		private IPopulation<?> population() { return boids; }

		GamaServerMessage ask(final Map<String, Object> request, final MessageType expected) {
			final int before = executions;
			final var response = DefaultServerCommands.ASK(server, null, request);
			check(response.type == expected, "Expected " + expected + ", got " + response.type + ": " + response.content);
			if (expected == MessageType.CommandExecutedSuccessfully)
				response.serializeToJson(GAMA.getJsonEncoder());
			if (expected != MessageType.CommandExecutedSuccessfully)
				check(executions == before, "Invalid requests must not execute an action");
			return response;
		}
	}

	private static Map<String, Object> request() {
		return new HashMap<>(Map.of("type", "ask", "agent", "simulation[0].boids[12]",
				"action", "say_hello", "args", "{\"the_value\":12}\n"));
	}

	public static void main(final String[] args) {
		final var previousJson = GAMA.getJsonEncoder();
		final var previousFactory = GAML.getExpressionDescriptionFactory();
		final var previousMapType = Types.MAP;
		if (Types.MAP == null) { Types.MAP = new GamaMapType(Types.getBuiltInTypeManager()); }
		GAMA.setJsonEncoder(Json.getNew());
		GAML.registerExpressionDescriptionFactory(proxy(IExpressionDescriptionFactory.class, (p, method, values) -> {
			if (!"createConstant".equals(method.getName()))
				throw new AssertionError("Unexpected expression factory call: " + method);
			final Object value = values[0];
			final var expression = proxy(IExpression.class, (e, m, a) -> {
				if ("value".equals(m.getName())) return value;
				throw new AssertionError("Unexpected expression call: " + m);
			});
			return proxy(IExpressionDescription.class, (d, m, a) -> {
				if ("getExpression".equals(m.getName())) return expression;
				throw new AssertionError("Unexpected expression description call: " + m);
			});
		}));
		try {
			final var fixture = new Fixture();
			for (final Object invalid : List.of("one_of(boids)", "", "simulation", "simulation[0",
					"simulation[0].", "simulation[0]..boids[12]", "simulation[-1]", "simulation[2147483648]",
					"simulation[0].boids[x]", "simulation[0]trailing", "simulation[0].boids[12]extra[3]",
					12, Map.of("reference", "simulation[0]"))) {
				final var request = request();
				request.put("agent", invalid);
				fixture.ask(request, MessageType.MalformedRequest);
			}
			check(AgentReference.NULL.getReferencedAgent(fixture.scope) == null, "Null reference must resolve safely");
			check(AgentReference.of("simulation[0].boids[12]").getExistingAgent(fixture.scope) == fixture.boid,
					"Existing agent lookup failed");
			check(AgentReference.of("simulation[0].boids[12].nested[12]").getExistingAgent(fixture.scope) == fixture.boid,
					"Nested references must resolve");
			check(AgentReference.of("simulation[00].boids[012]").index()[1] == 12, "Indices must be decimal");
			for (final String missing : List.of("simulation[99]", "simulation[0].boids[99]",
					"simulation[0].missing[12]", "simulation[0].boids[99].nested[0]")) {
				final var request = request();
				request.put("agent", missing);
				fixture.ask(request, MessageType.UnableToExecuteRequest);
			}
			fixture.dead = true;
			fixture.ask(request(), MessageType.UnableToExecuteRequest);
			fixture.dead = false;
			for (final Object invalid : List.of("", "{broken", "[]", "null", "12", 12, List.of(12), Map.of(12, 12))) {
				final var request = request();
				request.put("args", invalid);
				fixture.ask(request, MessageType.MalformedRequest);
			}
			for (final Object invalid : List.of("", " ", 12)) {
				final var request = request();
				request.put("action", invalid);
				fixture.ask(request, MessageType.MalformedRequest);
			}
			final var missingAction = request();
			missingAction.remove("action");
			fixture.ask(missingAction, MessageType.MalformedRequest);
			missingAction.put("action", "missing");
			fixture.ask(missingAction, MessageType.UnableToExecuteRequest);

			fixture.ask(request(), MessageType.CommandExecutedSuccessfully);
			check(fixture.target == fixture.boid, "The action must run on the referenced agent");
			check(fixture.arguments.getExpr("the_value").value(fixture.scope).equals(12), "Lost JSON argument");
			final var request = request();
			request.put("args", Map.of("the_value", 42));
			request.put("escaped", true);
			fixture.ask(request, MessageType.CommandExecutedSuccessfully);
			check(fixture.arguments.getExpr("the_value").value(fixture.scope).equals(42), "Lost object argument");
			request.put("agent", " simulation[0].boids[12] ");
			request.put("action", " say_hello ");
			request.put("args", "{}");
			fixture.ask(request, MessageType.CommandExecutedSuccessfully);
			check(fixture.target == fixture.boid && fixture.arguments.isEmpty(),
					"Whitespace and empty arguments must be supported");
			request.remove("agent");
			request.remove("args");
			fixture.ask(request, MessageType.CommandExecutedSuccessfully);
			check(fixture.target == fixture.experiment, "Omitting agent must target the experiment");
			check(fixture.arguments.isEmpty(), "Omitting args must supply no arguments");
			check(fixture.cleared == fixture.executions, "Every action scope must be released");
			System.out.println("Server ask regression checks passed");
		} finally {
			GAMA.setJsonEncoder(previousJson);
			GAML.registerExpressionDescriptionFactory(previousFactory);
			Types.MAP = previousMapType;
		}
	}
}
