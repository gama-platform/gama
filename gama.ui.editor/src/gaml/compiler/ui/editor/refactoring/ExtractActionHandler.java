package gaml.compiler.ui.editor.refactoring;

import gaml.compiler.ui.editor.GamlEditor;

/** Compatibility entry point; creation and extraction now use the same transactional action wizard. */
public final class ExtractActionHandler {

	private ExtractActionHandler() {}

	public static void extractAction(final GamlEditor editor) {
		editor.openActionWizard(true);
	}
}
