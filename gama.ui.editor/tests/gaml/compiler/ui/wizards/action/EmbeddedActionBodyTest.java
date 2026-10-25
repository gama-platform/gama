package gaml.compiler.ui.wizards.action;

import java.util.List;

import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.ui.editor.XtextSourceViewer;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditor;
import org.eclipse.xtext.ui.editor.model.DocumentTokenSource;
import org.eclipse.xtext.ui.editor.model.XtextDocument;

import com.google.inject.Injector;

import gaml.compiler.GamlStandaloneSetup;

/** Real Xtext document, partial editor and SWT viewer; no workbench or automatic validation. */
public final class EmbeddedActionBodyTest {
	private static int checks;

	private static void check(final boolean condition, final String message) {
		checks++;
		if (!condition) throw new AssertionError(message);
	}

	private static void body(final Shell shell, final Injector injector, final String initial,
			final boolean reproduce) throws Exception {
		body(shell, injector, initial, reproduce, ActionBodyContext.standalone(ActionDraft.empty()),
				ActionDraft.empty().header());
	}

	private static void body(final Shell shell, final Injector injector, final String initial, final boolean reproduce,
			final ActionBodyContext context, final String header) throws Exception {
		var viewer = new XtextSourceViewer(shell, null, null, false, SWT.BORDER | SWT.MULTI);
		var tokens = new DocumentTokenSource();
		tokens.setLexer(injector.getProvider(org.eclipse.xtext.parser.antlr.Lexer.class));
		var document = new XtextDocument(tokens, null);
		viewer.setUndoManager(new TextViewerUndoManager(20));
		var resources = injector.getInstance(XtextResourceSet.class);
		var editor = new EmbeddedEditor(document, viewer, null, () -> {
			// Parsing is real; linking is excluded because the GAMA workspace requires a running workbench.
			var resource = new XtextResource(context.uri()) {
				@Override
				protected void doLinking() {}
			};
			injector.injectMembers(resource);
			resources.getResources().add(resource);
			return resource;
		}, () -> {});
		try {
			if (reproduce) {
				editor.createPartialEditor(context.prefix(), initial, context.suffix(), false);
				check(viewer.getTextWidget().getText().contains(ActionDraft.empty().header()),
						"Old initialization reproduces the leaked header for a new empty action");
				return;
			}
			var body = EmbeddedActionBody.create(editor, context, initial);
			check(viewer.getTextWidget().getText().equals(initial), "Widget must initially show only raw body: " + initial);
			check(body.text().equals(initial), "Editable part must preserve the exact initial body");
			check(viewer.getVisibleRegion().getLength() == initial.length(), "Visible region has body length only");
			check(viewer.getVisibleRegion().getOffset() > context.prefix().length(), "Hidden prefix ends on its own line");
			check(document.get().contains(header), "Signature remains in the hidden parsing context");
			check(document.get().endsWith(context.suffix()), "Closing braces remain in the hidden parsing context");
			check(document.get(viewer.getVisibleRegion().getOffset(), initial.length()).equals(initial),
					"Visible document region contains only body bytes");

			viewer.getTextWidget().append("\nwrite 'added';");
			check(body.text().equals(initial + "\nwrite 'added';"), "Typing changes only the editable body");
			check(viewer.getTextWidget().getText().equals(body.text()), "Displayed body matches returned body after typing");
			check(document.get().startsWith(context.prefix()) && document.get().endsWith(context.suffix()),
					"Typing does not change the hidden signature or braces");
			body.setEnabled(false);
			check(!viewer.isEditable() && body.text().equals(initial + "\nwrite 'added';"), "Disabling preserves body");
			body.setEnabled(true);
			check(viewer.isEditable() && viewer.getTextWidget().getText().equals(body.text()), "Enabling preserves body");
			body.setText("");
			check(body.text().isEmpty() && viewer.getTextWidget().getText().isEmpty(), "Selection opt-out clears only editable body");
			check(document.get().startsWith(context.prefix()) && document.get().endsWith(context.suffix()),
					"Replacing body keeps hidden model context");
			body.setText(initial);
			check(body.text().equals(initial) && viewer.getTextWidget().getText().equals(initial),
					"Selection opt-in restores exact selected body without hidden-header leakage");
		} finally {
			viewer.getControl().dispose();
			for (var resource : List.copyOf(resources.getResources())) resource.unload();
			resources.getResources().clear();
		}
	}

	public static void main(final String[] args) throws Exception {
		var injector = GamlStandaloneSetup.doSetup();
		Display display = new Display();
		Shell shell = new Shell(display);
		try {
			body(shell, injector, "", true);
			for (String initial : List.of("", "write 'hello';", "\n\twrite 'hello';\n", "\r\n\twrite 'hello';\r\n",
					"// action my_action() { must remain a comment }\nwrite 'braces { }';"))
				body(shell, injector, initial, false);
			// The behaviour wizard embeds the same editor with its own hidden header (reflex, state, plan...).
			for (var kind : List.of(gaml.compiler.ui.wizards.behavior.BehaviorKind.REFLEX,
					gaml.compiler.ui.wizards.behavior.BehaviorKind.STATE, gaml.compiler.ui.wizards.behavior.BehaviorKind.PLAN)) {
				var draft = gaml.compiler.ui.wizards.behavior.BehaviorDraft.empty(kind);
				for (String initial : List.of("", "\n\twrite 'hello';\n"))
					body(shell, injector, initial, false, gaml.compiler.ui.wizards.behavior.BehaviorWizard.standalone(draft),
							draft.header() + " {");
			}
			displayLayers(shell, injector);
			layerBodies(shell, injector);
			System.out.println("EmbeddedActionBodyTest: " + checks + " checks passed");
		} finally {
			shell.dispose();
			display.dispose();
		}
	}

	private static void layerBodies(final Shell shell, final Injector injector) {
		for (String source : List.of("graphics g { /*original*/ draw circle(1); }",
				"overlay 'HUD' { draw 'Text'; }", "species wolf {species child;}",
				"agents a value: wolf {draw square(1);}", "light 'custom' type: point {write 'test';}")) {
			var parser = injector.getInstance(org.eclipse.xtext.parser.IParser.class);
			var draft = gaml.compiler.ui.wizards.display.DisplaySource.parseLayer(source, "embedded", parser);
			var viewer = new XtextSourceViewer(shell, null, null, false, SWT.BORDER | SWT.MULTI);
			var tokens = new DocumentTokenSource();
			tokens.setLexer(injector.getProvider(org.eclipse.xtext.parser.antlr.Lexer.class));
			var document = new XtextDocument(tokens, null);
			viewer.setUndoManager(new TextViewerUndoManager(20));
			var resources = injector.getInstance(XtextResourceSet.class);
			var wizard = new gaml.compiler.ui.wizards.display.LayerWizard(shell, draft, d -> null, null, null,
					d -> new ActionBodyContext(org.eclipse.emf.common.util.URI.createURI("synthetic:/layer-body.gaml"),
							"model layer_wizard\nglobal { int n; }\nexperiment e type: gui { output { display d type: opengl {\n"
									+ source + "\n" + gaml.compiler.ui.wizards.display.DisplaySource.renderLayer(d, parser)
									+ "\n} } }\n", ""), null) {
				@Override
				protected EmbeddedActionBody createEmbeddedEditor(final org.eclipse.swt.widgets.Composite parent,
						final ActionBodyContext context, final String initial) {
					var editor = new EmbeddedEditor(document, viewer, null, () -> {
						var resource = new XtextResource(context.uri()) {
							@Override protected void doLinking() {}
						};
						injector.injectMembers(resource);
						resources.getResources().add(resource);
						return resource;
					}, () -> {});
					return EmbeddedActionBody.create(editor, context, initial);
				}
			};
			try {
				wizard.create();
				check(viewer.getTextWidget().getText().equals(draft.body()), "Layer editor exposes block contents only");
				check(wizard.readDraft() == draft, "Embedded initialization retains original layer");
				check(document.get().contains("display d type: opengl") && document.get().contains(draft.body()),
						"Embedded layer has hidden model/display context");
				check(document.get().contains("global { int n; }") && document.get().contains(source),
						"Host model and identical sibling layer remain in hidden context");
				viewer.getTextWidget().append("\n// edited");
				check(wizard.readDraft().body().equals(draft.body() + "\n// edited"), "Body listener returns editable bytes only");
				check(wizard.readDraft().source().equals(source), "Body typing retains source backing");
				check(document.get().contains(source) && !document.get().contains("layer_body_"),
						"Unique body location leaves sibling unchanged and never leaks markers");
			} finally {
				wizard.close();
				if (!viewer.getControl().isDisposed()) viewer.getControl().dispose();
				for (var resource : List.copyOf(resources.getResources())) resource.unload();
				resources.getResources().clear();
			}
		}
	}

	/** Exercises DisplayWizard's compatibility embedded-editor hook, not just an equivalent context string. */
	private static void displayLayers(final Shell shell, final Injector injector) {
		for (String initial : List.of("", "\n// chart\nchart c type: series { data n value: cycle; }\n",
				"\r\n/* kept */ graphics g { draw 'text'; }\r\n")) {
			var viewer = new XtextSourceViewer(shell, null, null, false, SWT.BORDER | SWT.MULTI);
			var tokens = new DocumentTokenSource();
			tokens.setLexer(injector.getProvider(org.eclipse.xtext.parser.antlr.Lexer.class));
			var document = new XtextDocument(tokens, null);
			viewer.setUndoManager(new TextViewerUndoManager(20));
			var resources = injector.getInstance(XtextResourceSet.class);
			String[] modified = { null };
			var wizard = new gaml.compiler.ui.wizards.display.DisplayWizard(shell,
					gaml.compiler.ui.wizards.display.DisplayDraft.empty(), d -> null, null, null, null) {
				@Override
				protected EmbeddedActionBody createEmbeddedEditor(final org.eclipse.swt.widgets.Composite parent,
						final ActionBodyContext context, final String code) {
					var editor = new EmbeddedEditor(document, viewer, null, () -> {
						var resource = new XtextResource(context.uri()) {
							@Override protected void doLinking() {}
						};
						injector.injectMembers(resource);
						resources.getResources().add(resource);
						return resource;
					}, () -> {});
					return EmbeddedActionBody.create(editor, context, code);
				}
				org.eclipse.swt.widgets.Control raw() { return createRawEditor(shell, initial, text -> modified[0] = text); }
			};
			try {
				wizard.raw();
				check(viewer.getTextWidget().getText().equals(initial), "Display raw editor hides the model/display header");
				check(document.get().contains("display d {") && document.get().contains(initial), "Hidden display parsing context");
				check(modified[0] == null, "Raw listener is not called by initialization");
				viewer.getTextWidget().append("\n// edited");
				check(modified[0].equals(initial + "\n// edited"), "Display listener returns only editable bytes");
				check(!modified[0].contains("model display_wizard"), "Display callback never leaks its hidden header");
			} finally {
				viewer.getControl().dispose();
				for (var resource : List.copyOf(resources.getResources())) resource.unload();
				resources.getResources().clear();
			}
		}
	}
}
