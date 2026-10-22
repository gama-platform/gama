package gaml.compiler.ui.wizards.species;

import java.util.Objects;

/** Workspace container path and file name of a model; plain strings keep the form workspace-independent. */
public record ModelLocation(String container, String fileName) {

	public ModelLocation {
		Objects.requireNonNull(container);
		Objects.requireNonNull(fileName);
	}
}
