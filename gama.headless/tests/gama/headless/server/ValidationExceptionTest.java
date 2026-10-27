/*******************************************************************************************************
 *
 * ValidationExceptionTest.java, in gama.headless, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.server;

import java.util.Map;

import gama.api.GAMA;
import gama.api.utils.server.DefaultServerCommands;
import gama.api.utils.server.GamaServerMessage;
import gama.api.utils.server.MessageType;

/** Regression checks for #1084. Invoke {@link #run()} after initializing the headless GAMA runtime. */
public final class ValidationExceptionTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static GamaServerMessage validate(final String source, final boolean syntaxOnly) {
		return DefaultServerCommands.VALIDATE(null, null,
				Map.<String, Object> of("type", "validate", "expr", source, "syntax", syntaxOnly));
	}

	private static void expectError(final String source, final boolean syntaxOnly, final String token) {
		final var response = validate(source, syntaxOnly);
		check(response.type == MessageType.UnableToExecuteRequest, "Expected compilation errors for " + source + " (syntax=" + syntaxOnly + "): " + response.type + " " + response.content);
		final var error = response.serializeToJson(GAMA.getJsonEncoder()).asObject().get("content").asObject()
				.get("errors").asArray().get(0).asObject();
		check(!error.get("message").asString().contains("internal error"),
				"Errors must be reported as GAML diagnostics: " + error);
		check(error.get("offset").asInt() == source.indexOf(token), "Incorrect offset: " + error);
	}

	public static void run() throws Exception {
		expectError("model test\nglobal { \n  init {\n    if false {\n      int a <- 2;\n    }\n    write a;\n  }\n}",
				false, "a;");
		expectError("model test\nglobal { \n  init {write i;} \n}", false, "i;");
		expectError("model test\nglobal { \n  if false {int a <- 2;} \n}", false, "if");
		final String misplacedWrite = "model test\nglobal { \n  if false {int a <- 2;} \n  write i; \n}";
		expectError(misplacedWrite, false, "if");
		expectError(misplacedWrite, true, "if");
		expectError("model test\nglobal {\n  write 1;\n}", true, "write");
		final String valid = "model test\nglobal {\n  int a <- 1;\n  init { if a > 0 { write a; } }\n}\n"
				+ "species s { reflex r { write a; } }\nexperiment e type: gui { output { display d {} } }\n";
		final var response = validate(valid, true);
		check(response.type == MessageType.CommandExecutedSuccessfully,
				"Well-placed statements must pass syntax-only validation: " + response.content);
		check(validate(valid, false).type == MessageType.CommandExecutedSuccessfully,
				"Valid models must pass full validation");
	}
}
