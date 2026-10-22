package gaml.compiler.ui.wizards.action;

import java.util.Objects;

import org.eclipse.emf.common.util.URI;

/**
 * Contextual source surrounding an action body: the validated text is {@code prefix + body + suffix}. The prefix
 * normally ends with the action's opening brace and the suffix starts with its closing brace. {@code uri} must be an
 * isolated URI (never a live file or editor resource); it is used to resolve relative imports.
 */
public record ActionBodyContext(URI uri, String prefix, String suffix) {

	public ActionBodyContext {
		Objects.requireNonNull(uri);
		Objects.requireNonNull(prefix);
		Objects.requireNonNull(suffix);
	}

	/** A minimal synthetic model placing the action in {@code global}; used when no adapter context is available. */
	public static ActionBodyContext standalone(final ActionDraft draft) {
		return new ActionBodyContext(URI.createURI("synthetic:/action-wizard/.action-wizard.gaml"),
				"model action_wizard\n\nglobal {\n\t" + draft.header() + " {", "}\n}\n");
	}

	public String text(final String body) {
		return prefix + body + suffix;
	}
}
