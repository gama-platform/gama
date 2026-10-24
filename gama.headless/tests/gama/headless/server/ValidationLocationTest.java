/*******************************************************************************************************
 *
 * ValidationLocationTest.java, in gama.headless, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.server;

import java.nio.file.Files;
import java.util.Map;

import gama.api.GAMA;
import gama.api.compilation.GamlCompilationError;
import gama.api.exceptions.GamaCompilationFailedException;
import gama.api.utils.json.IJsonObject;
import gama.api.utils.server.DefaultServerCommands;
import gama.api.utils.server.GamaServerMessage;
import gama.api.utils.server.MessageType;
import gama.api.utils.server.ReceivedMessage;
import gama.headless.listener.HeadlessLoadCommand;

/** Regression checks for #484. Invoke {@link #run()} after initializing the headless GAMA runtime. */
public final class ValidationLocationTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static IJsonObject firstError(final GamaServerMessage response) {
		check(response.type == MessageType.UnableToExecuteRequest, "Expected compilation errors: " + response.content);
		final var json = response.serializeToJson(GAMA.getJsonEncoder()).asObject();
		return json.get("content").asObject().get("errors").asArray().get(0).asObject();
	}

	private static void location(final IJsonObject error, final String source, final String token) {
		final int offset = source.indexOf(token);
		final int line = 1 + (int) source.substring(0, offset).chars().filter(c -> c == '\n').count();
		final int column = offset - source.lastIndexOf('\n', offset);
		check(error.get("starting_at_line").asInt() == line, "Incorrect line: " + error);
		check(error.get("column").asInt() == column, "Incorrect column: " + error);
		check(error.get("offset").asInt() == offset, "Incorrect offset: " + error);
	}

	private static void validate(final String source, final boolean syntaxOnly, final String token) {
		final var command = Map.<String, Object>of("type", "validate", "expr", source, "syntax", syntaxOnly);
		final var response = DefaultServerCommands.VALIDATE(null, null, command);
		location(firstError(response), source, token);
		check(response.serializeToJson(GAMA.getJsonEncoder()).asObject().get("command").asObject()
				.get("expr").asString().equals(source), "The response must reference the unmodified command");
	}

	public static void run() throws Exception {
		final String semantic = "model locations\nglobal {\n    int x <- missing;\n}\nexperiment main type: gui {}\n";
		final String syntax = "model locations\nglobal {\n    int x <- ;\n}\nexperiment main type: gui {}\n";
		validate(semantic, false, "missing");
		validate(syntax, true, "<-");
		validate("\n  " + semantic, false, "missing");
		validate("let x <- missing", false, "missing");
		validate("let l <- llama", false, "llama");
		validate("missing", false, "missing");
		validate("let x <- 1;\nlet y <- missing;", false, "missing");
		validate("let x <- 1;\r\nlet y <- missing;", false, "missing");
		validate("species person {\n    int x <- missing;\n}", false, "missing");
		validate(syntax.replace("\n", "\r\n"), true, "<-");
		validate("model locations\nexperiment main type: gui {\n    output display d {}\n}", true, "display");
		validate("model locations\nglobal {\n    string s <- \"caf\u00e9\";\n    int x <- missing;\n}", false, "missing");

		final var valid = DefaultServerCommands.VALIDATE(null, null,
				Map.of("expr", "let l <- llama", "syntax", true));
		check(valid.type == MessageType.CommandExecutedSuccessfully, "Syntax-only validation must skip semantic errors");
		final var diagnostic = GamlCompilationError.create("A \"quoted\" message\nwith a newline", "test",
				(org.eclipse.emf.common.util.URI) null, GamlCompilationError.Type.Error, "a\\b", "\"c\"");
		final var encoded = GAMA.getJsonEncoder().parse(new GamaCompilationFailedException(java.util.List.of(diagnostic))
				.toJsonString()).asObject().get("errors").asArray().get(0).asObject();
		check(encoded.get("message").asString().equals(diagnostic.message()), "Error messages must be JSON escaped");
		check(encoded.get("data").asArray().get(0).asString().equals("a\\b")
				&& encoded.get("data").asArray().get(1).asString().equals("\"c\""),
				"Diagnostic data must be JSON escaped");
		check(encoded.get("starting_at_line").asInt() == 0 && encoded.get("column").asInt() == 0,
				"Source-less errors must have an explicitly unknown position");
		final String multiple = "model locations\nglobal {\n int x <- missing_one;\n int y <- missing_two;\n}";
		final var response = DefaultServerCommands.VALIDATE(null, null, Map.of("expr", multiple, "syntax", false));
		final var errors = response.serializeToJson(GAMA.getJsonEncoder()).asObject().get("content").asObject()
				.get("errors").asArray();
		check(errors.size() >= 2, "Validation must retain all semantic diagnostics");
		location(errors.get(0).asObject(), multiple, "missing_one");
		location(errors.get(1).asObject(), multiple, "missing_two");

		final var file = Files.createTempFile("validation-locations-", ".gaml");
		try {
			for (final String source : new String[] { syntax, semantic }) {
				Files.writeString(file, source);
				final var command = Map.<String, Object>of("model", file.toString(), "experiment", "main");
				final String token = source == syntax ? "<-" : "missing";
				location(firstError(DefaultServerCommands.LOAD(null, null, command)), source, token);
				location(firstError(DefaultServerCommands.DESCRIBE(null, null, command)), source, token);
				final var headlessCommand = new ReceivedMessage("", command);
				headlessCommand.put("socket_id", "validation-location-test");
				location(firstError(new HeadlessLoadCommand().execute(null, null, headlessCommand)), source, token);
			}
		} finally {
			Files.delete(file);
		}
	}
}
