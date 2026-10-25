/*******************************************************************************************************
 *
 * JavaSourceFixer.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.quickfix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.TextEdit;

import gama.ui.devtools.util.ResourceUtils;

/**
 * Modifies the Java sources of GAMA plugins (using the JDT DOM) to fix the warnings emitted by the GAML annotation
 * processor: adds documentation skeletons, test skeletons or {@code @no_test} annotations.
 */
public final class JavaSourceFixer {

	/** The available fixes. */
	public enum Fix {

		/** Adds (or completes) a {@code @doc} annotation. */
		ADD_DOC("Add a @doc skeleton",
				"Adds a @doc annotation (or fills the existing one) with a TODO text to complete",
				ProcessorWarning.Kind.UNDOCUMENTED),

		/** Adds a {@code @test} annotation. */
		ADD_TEST("Add a @test skeleton (to complete)",
				"Adds a failing @test annotation to the operator, that must be replaced by a real GAML assertion",
				ProcessorWarning.Kind.UNTESTED),

		/** Adds {@code @no_test(Reason.ALREADY_TESTED)}. */
		NO_TEST_ALREADY_TESTED("Mark as @no_test(Reason.ALREADY_TESTED)",
				"Declares that the operator is tested elsewhere (e.g. in a hand-written GAML test model)",
				ProcessorWarning.Kind.UNTESTED),

		/** Adds {@code @no_test(Reason.IMPOSSIBLE_TO_TEST)}. */
		NO_TEST_IMPOSSIBLE("Mark as @no_test(Reason.IMPOSSIBLE_TO_TEST)",
				"Declares that the operator cannot be tested automatically", ProcessorWarning.Kind.UNTESTED);

		/** The label displayed in the quick fix dialog. */
		public final String label;

		/** The description displayed in the quick fix dialog. */
		public final String description;

		/** The kind of warnings this fix applies to. */
		public final ProcessorWarning.Kind target;

		Fix(final String label, final String description, final ProcessorWarning.Kind target) {
			this.label = label;
			this.description = description;
			this.target = target;
		}
	}

	private static final String ANNOTATIONS = "gama.annotations.";

	private JavaSourceFixer() {}

	/**
	 * Applies the fix to the elements designated by the markers (grouped by file, each file being modified once).
	 *
	 * @return the number of elements modified
	 */
	public static int apply(final Fix fix, final List<IMarker> markers, final IProgressMonitor monitor)
			throws CoreException {
		Map<IFile, List<IMarker>> byFile = new LinkedHashMap<>();
		for (IMarker m : markers) {
			ProcessorWarning w = ProcessorWarning.of(m);
			if (w != null && w.kind() == fix.target) {
				byFile.computeIfAbsent((IFile) m.getResource(), k -> new ArrayList<>()).add(m);
			}
		}
		SubMonitor sub = SubMonitor.convert(monitor, "Fixing GAML processor warnings", byFile.size());
		int count = 0;
		for (Map.Entry<IFile, List<IMarker>> e : byFile.entrySet()) {
			count += apply(fix, e.getKey(), e.getValue(), sub.split(1));
		}
		return count;
	}

	private static int apply(final Fix fix, final IFile file, final List<IMarker> markers,
			final IProgressMonitor monitor) throws CoreException {
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
		if (cu == null) { return 0; }
		boolean shared = cu.isWorkingCopy();
		if (!shared) { cu.becomeWorkingCopy(monitor); }
		try {
			List<Target> targets = new ArrayList<>();
			for (IMarker m : markers) {
				targets.add(new Target(m.getAttribute(IMarker.CHAR_START, -1), m.getAttribute(IMarker.LINE_NUMBER, -1),
						ProcessorWarning.of(m)));
			}
			int[] count = new int[1];
			String result = fix(fix, cu.getSource(), targets, cu.getJavaProject().getOptions(true), count);
			if (result == null) { return 0; }
			cu.getBuffer().setContents(result);
			// If the file is open in an editor (shared working copy), let the user save it
			if (!shared) { cu.commitWorkingCopy(false, monitor); }
			return count[0];
		} catch (BadLocationException e) {
			throw new CoreException(ResourceUtils.error("Cannot modify " + file.getName(), e));
		} finally {
			if (!shared) { cu.discardWorkingCopy(); }
		}
	}

	/**
	 * The position of a warning in a source file.
	 *
	 * @param start
	 *            the offset of the name of the element (or -1)
	 * @param line
	 *            the line of the element (or -1)
	 * @param warning
	 *            the warning
	 */
	public record Target(int start, int line, ProcessorWarning warning) {}

	/**
	 * Applies a fix to a Java source.
	 *
	 * @param options
	 *            the JDT options (compliance, formatter) to use
	 * @param count
	 *            receives, in its first cell, the number of elements modified
	 * @return the modified source, or null if nothing has been modified
	 */
	public static String fix(final Fix fix, final String source, final List<Target> targets,
			final Map<String, String> options, final int[] count) throws BadLocationException {
		CompilationUnit root = parse(source, options);
		ASTRewrite rewrite = ASTRewrite.create(root.getAST());
		Set<String> imports = new LinkedHashSet<>();
		Set<String> done = new HashSet<>();
		int n = 0;
		for (Target t : targets) {
			ProcessorWarning w = t.warning();
			if (w == null || w.kind() != fix.target) { continue; }
			BodyDeclaration decl = locate(root, t.start(), t.line());
			if (decl == null || !done.add(decl.getStartPosition() + "/" + w.displayedName())) { continue; }
			boolean changed = switch (fix) {
				case ADD_DOC -> addDoc(rewrite, imports, decl, w);
				case ADD_TEST -> addTest(rewrite, imports, decl, w);
				case NO_TEST_ALREADY_TESTED -> addNoTest(rewrite, imports, decl, "ALREADY_TESTED");
				case NO_TEST_IMPOSSIBLE -> addNoTest(rewrite, imports, decl, "IMPOSSIBLE_TO_TEST");
			};
			if (changed) { n++; }
		}
		count[0] = n;
		if (n == 0) { return null; }
		IDocument document = new Document(source);
		TextEdit edit = rewrite.rewriteAST(document, options);
		// The imports are located before the modified declarations: inserting them last keeps the offsets valid
		TextEdit importEdit = importEdit(root, imports);
		edit.apply(document);
		if (importEdit != null) { importEdit.apply(document); }
		return document.get();
	}

	/** Parses a source without resolving bindings. */
	static CompilationUnit parse(final String source, final Map<String, String> options) {
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setCompilerOptions(options);
		parser.setSource(source.toCharArray());
		parser.setResolveBindings(false);
		return (CompilationUnit) parser.createAST(null);
	}

	/** Parses a compilation unit without resolving bindings. */
	static CompilationUnit parse(final ICompilationUnit cu, final IProgressMonitor monitor) {
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(cu);
		parser.setResolveBindings(false);
		return (CompilationUnit) parser.createAST(monitor);
	}

	/** Builds the edit that adds the missing imports (after the last import, or after the package declaration). */
	private static TextEdit importEdit(final CompilationUnit root, final Set<String> required) {
		List<?> existing = root.imports();
		StringBuilder sb = new StringBuilder();
		for (String qn : required) {
			String pkg = qn.substring(0, qn.lastIndexOf('.'));
			boolean present = false;
			for (Object o : existing) {
				ImportDeclaration i = (ImportDeclaration) o;
				String name = i.getName().getFullyQualifiedName();
				if (!i.isStatic() && (i.isOnDemand() ? name.equals(pkg) : name.equals(qn))) { present = true; }
			}
			if (!present) { sb.append("\nimport ").append(qn).append(';'); }
		}
		if (sb.isEmpty()) { return null; }
		if (!existing.isEmpty()) {
			ASTNode last = (ASTNode) existing.get(existing.size() - 1);
			return new InsertEdit(last.getStartPosition() + last.getLength(), sb.toString());
		}
		if (root.getPackage() != null) {
			ASTNode p = root.getPackage();
			return new InsertEdit(p.getStartPosition() + p.getLength(), "\n" + sb);
		}
		return new InsertEdit(0, sb.substring(1) + "\n\n");
	}

	/**
	 * Finds the declaration (type, method, field) a processor marker is attached to. The processor reports the warnings
	 * on the name of the element, so the declaration whose name starts at the marker position (or, by default, at its
	 * line) is chosen.
	 */
	static BodyDeclaration locate(final CompilationUnit root, final IMarker marker) {
		return locate(root, marker.getAttribute(IMarker.CHAR_START, -1), marker.getAttribute(IMarker.LINE_NUMBER, -1));
	}

	/** Finds the declaration whose name starts at the given offset (or, by default, is on the given line). */
	static BodyDeclaration locate(final CompilationUnit root, final int start, final int line) {
		BodyDeclaration[] found = new BodyDeclaration[2];
		root.accept(new ASTVisitor() {
			@Override
			public void preVisit(final ASTNode node) {
				if (!(node instanceof BodyDeclaration decl)) { return; }
				for (SimpleName name : namesOf(decl)) {
					int pos = name.getStartPosition();
					if (pos == start) { found[0] = decl; }
					if (line > 0 && root.getLineNumber(pos) == line) { found[1] = decl; }
				}
			}
		});
		if (found[0] != null) { return found[0]; }
		if (found[1] != null) { return found[1]; }
		if (start < 0) { return null; }
		ASTNode n = NodeFinder.perform(root, start, 0);
		while (n != null && !(n instanceof BodyDeclaration)) { n = n.getParent(); }
		return (BodyDeclaration) n;
	}

	private static List<SimpleName> namesOf(final BodyDeclaration decl) {
		return switch (decl) {
			case AbstractTypeDeclaration t -> List.of(t.getName());
			case MethodDeclaration m -> List.of(m.getName());
			case EnumConstantDeclaration c -> List.of(c.getName());
			case FieldDeclaration f -> {
				List<SimpleName> names = new ArrayList<>();
				for (Object o : f.fragments()) { names.add(((VariableDeclarationFragment) o).getName()); }
				yield names;
			}
			default -> List.of();
		};
	}

	// ------------------------------------------------------------------------------------------------------------
	// Documentation
	// ------------------------------------------------------------------------------------------------------------

	private static boolean addDoc(final ASTRewrite rewrite, final Set<String> imports, final BodyDeclaration decl,
			final ProcessorWarning w) {
		String text = "TODO: document the " + w.displayedName();
		if (w.nested() != null) {
			NormalAnnotation holder = findNested(decl, w.nested(), w.name());
			if (holder == null) { return false; }
			MemberValuePair doc = member(holder, "doc");
			if (doc != null) {
				fillDoc(rewrite, imports, doc.getValue(), text);
			} else {
				rewrite.getListRewrite(holder, NormalAnnotation.VALUES_PROPERTY)
						.insertLast(pair(holder.getAST(), "doc", newDoc(holder.getAST(), imports, text)), null);
			}
			return true;
		}
		Annotation direct = annotation(decl, "doc");
		if (direct != null) {
			fillDoc(rewrite, imports, direct, text);
			return true;
		}
		for (Object o : decl.modifiers()) {
			if (o instanceof NormalAnnotation a && member(a, "doc") != null) {
				fillDoc(rewrite, imports, member(a, "doc").getValue(), text);
				return true;
			}
		}
		insertAnnotation(rewrite, decl, newDoc(decl.getAST(), imports, text));
		return true;
	}

	/** Fills an existing (empty) documentation value: {@code @doc}, {@code @doc(...)}, {@code {}} or {@code {@doc}}. */
	private static void fillDoc(final ASTRewrite rewrite, final Set<String> imports, final Expression value,
			final String text) {
		AST ast = value.getAST();
		switch (value) {
			case ArrayInitializer array when !array.expressions().isEmpty() -> fillDoc(rewrite, imports,
					(Expression) array.expressions().get(0), text);
			case SingleMemberAnnotation a when isNamed(a, "doc") -> rewrite.set(a,
					SingleMemberAnnotation.VALUE_PROPERTY, string(ast, text), null);
			case NormalAnnotation a when isNamed(a, "doc") -> {
				MemberValuePair v = member(a, "value");
				if (v != null) {
					rewrite.set(v, MemberValuePair.VALUE_PROPERTY, string(ast, text), null);
				} else {
					rewrite.getListRewrite(a, NormalAnnotation.VALUES_PROPERTY).insertFirst(pair(ast, "value",
							string(ast, text)), null);
				}
			}
			default -> rewrite.replace(value, newDoc(ast, imports, text), null);
		}
	}

	/**
	 * Finds, in the annotations of the declaration, the {@code @variable}, {@code @arg} or {@code @facet} annotation
	 * whose name is the given one (either a literal or a constant like {@code IKeyword.SPEED}).
	 */
	private static NormalAnnotation findNested(final BodyDeclaration decl, final String annotation,
			final String name) {
		NormalAnnotation[] found = new NormalAnnotation[2];
		ASTVisitor visitor = new ASTVisitor() {
			@Override
			public boolean visit(final NormalAnnotation node) {
				if (isNamed(node, annotation)) {
					MemberValuePair n = member(node, "name");
					if (n != null) {
						if (n.getValue() instanceof StringLiteral s && name.equals(s.getLiteralValue())) {
							found[0] = node;
						} else if (n.getValue() instanceof Name constant
								&& name.equalsIgnoreCase(simpleName(constant))) {
							found[1] = node;
						}
					}
				}
				return true;
			}
		};
		for (Object o : decl.modifiers()) { ((ASTNode) o).accept(visitor); }
		return found[0] != null ? found[0] : found[1];
	}

	// ------------------------------------------------------------------------------------------------------------
	// Tests
	// ------------------------------------------------------------------------------------------------------------

	private static boolean addTest(final ASTRewrite rewrite, final Set<String> imports, final BodyDeclaration decl,
			final ProcessorWarning w) {
		AST ast = decl.getAST();
		SingleMemberAnnotation test = ast.newSingleMemberAnnotation();
		test.setTypeName(ast.newName(use(imports, "test")));
		test.setValue(string(ast, "false /* TODO: replace by a real assertion on '" + w.name() + "', e.g. "
				+ w.name() + "(...) = ... */"));
		insertAnnotation(rewrite, decl, test);
		return true;
	}

	private static boolean addNoTest(final ASTRewrite rewrite, final Set<String> imports,
			final BodyDeclaration decl, final String reason) {
		if (annotation(decl, "no_test") != null) { return false; }
		AST ast = decl.getAST();
		SingleMemberAnnotation noTest = ast.newSingleMemberAnnotation();
		noTest.setTypeName(ast.newName(use(imports, "no_test")));
		noTest.setValue(ast.newName(use(imports, "support.Reason") + "." + reason));
		insertAnnotation(rewrite, decl, noTest);
		return true;
	}

	// ------------------------------------------------------------------------------------------------------------
	// Utilities
	// ------------------------------------------------------------------------------------------------------------

	/** Inserts an annotation after the existing annotations and before the keyword modifiers of the declaration. */
	private static void insertAnnotation(final ASTRewrite rewrite, final BodyDeclaration decl,
			final Annotation annotation) {
		List<?> modifiers = decl.modifiers();
		int index = 0;
		while (index < modifiers.size() && ((IExtendedModifier) modifiers.get(index)).isAnnotation()) { index++; }
		ListRewrite list = rewrite.getListRewrite(decl, decl.getModifiersProperty());
		list.insertAt(annotation, index, null);
	}

	private static Annotation newDoc(final AST ast, final Set<String> imports, final String text) {
		SingleMemberAnnotation doc = ast.newSingleMemberAnnotation();
		doc.setTypeName(ast.newName(use(imports, "doc")));
		doc.setValue(string(ast, text));
		return doc;
	}

	/** Records the import of a GAMA annotation and returns its simple name. */
	private static String use(final Set<String> imports, final String annotation) {
		imports.add(ANNOTATIONS + annotation);
		return annotation.substring(annotation.lastIndexOf('.') + 1);
	}

	private static StringLiteral string(final AST ast, final String text) {
		StringLiteral s = ast.newStringLiteral();
		s.setLiteralValue(text);
		return s;
	}

	private static MemberValuePair pair(final AST ast, final String name, final Expression value) {
		MemberValuePair p = ast.newMemberValuePair();
		p.setName(ast.newSimpleName(name));
		p.setValue(value);
		return p;
	}

	static Annotation annotation(final BodyDeclaration decl, final String simpleName) {
		for (Object o : decl.modifiers()) { if (o instanceof Annotation a && isNamed(a, simpleName)) { return a; } }
		return null;
	}

	static MemberValuePair member(final NormalAnnotation a, final String name) {
		for (Object o : a.values()) {
			if (o instanceof MemberValuePair p && name.equals(p.getName().getIdentifier())) { return p; }
		}
		return null;
	}

	static boolean isNamed(final Annotation a, final String simpleName) {
		return simpleName.equals(simpleName(a.getTypeName()));
	}

	private static String simpleName(final Name name) {
		return name instanceof QualifiedName q ? q.getName().getIdentifier() : ((SimpleName) name).getIdentifier();
	}

}
