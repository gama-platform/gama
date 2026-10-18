package gaml.compiler.ui.wizards.action;

import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditor;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorFactory;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorModelAccess;
import org.eclipse.xtext.validation.IResourceValidator;

import gama.ui.editor.internal.EditorActivator;

/**
 * Embedded GAML editor showing only the action code. The surrounding synthetic model (no imports) gives correct
 * parsing and highlighting; automatic validation is disabled so the compiler never runs on keystrokes.
 */
final class EmbeddedActionBody implements ActionWizard.BodyEditor {

	private static final IResourceValidator NO_VALIDATION = (resource, mode, indicator) -> List.of();

	private final EmbeddedEditor editor;
	private final EmbeddedEditorModelAccess access;

	private EmbeddedActionBody(final EmbeddedEditor editor, final EmbeddedEditorModelAccess access) {
		this.editor = editor;
		this.access = access;
	}

	static EmbeddedActionBody create(final Composite parent, final ActionBodyContext context, final String initial) {
		EditorActivator activator = EditorActivator.getInstance();
		if (activator == null) return null;
		var injector = activator.getInjector(EditorActivator.GAML_COMPILER_GAML);
		if (injector == null) return null;
		URI uri = context.uri();
		EmbeddedEditor editor = injector.getInstance(EmbeddedEditorFactory.class).newEditor(() -> {
			XtextResourceSet set = new XtextResourceSet();
			Resource resource = set.createResource(uri);
			if (!(resource instanceof XtextResource result)) throw new IllegalStateException("GAML is not available.");
			return result;
		}).withResourceValidator(NO_VALIDATION).withParent(parent);
		return create(editor, context, initial);
	}

	static EmbeddedActionBody create(final EmbeddedEditor editor, final ActionBodyContext context, final String initial) {
		// Xtext's visible region is line-based: keep both hidden boundaries on separate lines, even for an empty body.
		var access = editor.createPartialEditor(context.prefix(), initial, context.suffix(), true);
		return new EmbeddedActionBody(editor, access);
	}

	@Override
	public Control control() { return editor.getViewer().getControl(); }

	@Override
	public String text() { return access.getEditablePart(); }

	@Override
	public void setEnabled(final boolean enabled) {
		editor.getViewer().setEditable(enabled);
		StyledText text = editor.getViewer().getTextWidget();
		text.setEnabled(enabled);
	}
}
