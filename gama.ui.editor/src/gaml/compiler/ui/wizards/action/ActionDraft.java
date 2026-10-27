package gaml.compiler.ui.wizards.action;

import java.util.HashSet;
import java.util.List;

import gaml.compiler.ui.wizards.species.SpeciesDraft;

/**
 * Detached action settings. {@code body} is the raw text strictly between the action braces; it is never trimmed or
 * normalized. A virtual draft keeps its body so that toggling Virtual never loses text; adapters decide how to emit it.
 */
public record ActionDraft(String name, String returnType, boolean virtual, List<Argument> arguments, String body) {

	/** Return type meaning "no returned value" (keyword {@code action}). */
	public static final String NO_RETURN = "action";

	/** An argument; an empty default value means no default. */
	public record Argument(String name, String type, String defaultValue) {
		public Argument {
			name = name == null ? "" : name;
			type = type == null ? "" : type;
			defaultValue = defaultValue == null ? "" : defaultValue;
		}

		/** Source form, e.g. {@code int n <- 1}. */
		public String declaration() {
			return type.trim() + " " + name + (defaultValue.isBlank() ? "" : " <- " + defaultValue.trim());
		}
	}

	public ActionDraft {
		name = name == null ? "" : name;
		returnType = returnType == null || returnType.isBlank() ? NO_RETURN : returnType;
		arguments = arguments == null ? List.of() : List.copyOf(arguments);
		body = body == null ? "" : body;
	}

	public static ActionDraft empty() {
		return new ActionDraft("my_action", NO_RETURN, false, List.of(), "");
	}

	public boolean returnsValue() {
		return !NO_RETURN.equals(returnType.trim());
	}

	/** Returns an actionable error, or null. Compilation belongs to the dialog/adapter. */
	public String validationError() {
		if (!SpeciesDraft.isIdentifier(name)) return "Action name must be a GAML identifier.";
		if (!isTypeExpression(returnType.trim())) return "Invalid return type: " + returnType;
		var names = new HashSet<String>();
		for (Argument argument : arguments) {
			if (!SpeciesDraft.isIdentifier(argument.name()))
				return "Argument name must be a GAML identifier: '" + argument.name() + "'.";
			if (!names.add(argument.name())) return "Duplicate argument: " + argument.name();
			if (argument.type().isBlank()) return "Enter a type for argument " + argument.name() + ".";
			if (!isTypeExpression(argument.type().trim()))
				return "Invalid type for argument " + argument.name() + ": " + argument.type();
			if (argument.defaultValue().contains(";"))
				return "The default value of " + argument.name() + " must be a single expression.";
		}
		return null;
	}

	/** Declaration header, e.g. {@code float grow(int n <- 1, point p)} or {@code action grow()}. */
	public String header() {
		return returnType.trim() + " " + name + "(" + argumentsText() + ")";
	}

	/** Comma-separated argument declarations, without parentheses. */
	public String argumentsText() {
		return String.join(", ", arguments.stream().map(Argument::declaration).toList());
	}

	/** Type references such as {@code int}, {@code list<point>}, {@code map<string, list<int>>} or {@code my_species}. */
	static boolean isTypeExpression(final String type) {
		if (!type.matches("[A-Za-z_][A-Za-z_0-9]*(\\s*<[A-Za-z_0-9<>,\\s]*>)?")) return false;
		int depth = 0;
		for (char c : type.toCharArray()) {
			if (c == '<') depth++;
			else if (c == '>' && --depth < 0) return false;
		}
		return depth == 0;
	}
}
