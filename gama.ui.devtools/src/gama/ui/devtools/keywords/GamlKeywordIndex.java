/*******************************************************************************************************
 *
 * GamlKeywordIndex.java, in gama.ui.devtools, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.devtools.keywords;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TextBlock;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * An index of the GAML keywords declared in Java sources through the GAMA annotations ({@code @operator},
 * {@code @symbol}, {@code @skill}, {@code @species}, {@code @type}, {@code @file}, {@code @constant}, {@code @action},
 * {@code @variable}, {@code @facet}, {@code @arg}, {@code @getter}, {@code @setter}...), with the location of their
 * Java implementation.
 * <p>
 * The sources are parsed with the JDT DOM, without bindings, so that the index can be built quickly and independently
 * of the state of the build. Names given as constants (e.g. {@code IKeyword.SPEED}, or a constant of the class itself
 * or of one of its super types) are resolved by looking up the declarations of these constants in the indexed sources.
 * The index is incremental: only the files whose stamp has changed are parsed again.
 * </p>
 * This class does not depend on the Eclipse workspace (see {@link GamlKeywordSearch} for that).
 */
public final class GamlKeywordIndex {

	/** The kinds of GAML keywords. */
	public enum Kind {
		/** {@code @operator} */
		OPERATOR("operator"),
		/** {@code @symbol} */
		STATEMENT("statement"),
		/** {@code @skill} */
		SKILL("skill"),
		/** {@code @species} */
		SPECIES("species"),
		/** {@code @type} */
		TYPE("type"),
		/** {@code @file} */
		FILE("file type"),
		/** {@code @experiment} */
		EXPERIMENT("experiment type"),
		/** {@code @display} */
		DISPLAY("display type"),
		/** {@code @gaml_class} */
		CLASS("class"),
		/** {@code @constant} */
		CONSTANT("constant"),
		/** {@code @action} */
		ACTION("action"),
		/** {@code @variable} */
		ATTRIBUTE("attribute"),
		/** {@code @getter} */
		GETTER("attribute getter"),
		/** {@code @setter} */
		SETTER("attribute setter"),
		/** {@code @facet} */
		FACET("facet"),
		/** {@code @arg} */
		ARGUMENT("argument");

		/** The label of the kind. */
		public final String label;

		Kind(final String label) {
			this.label = label;
		}

		/** The kind whose label (or name) starts with the given prefix, or null. */
		public static Kind fromPrefix(final String prefix) {
			String p = prefix.strip().toLowerCase();
			if (p.isEmpty()) { return null; }
			for (Kind k : values()) { if (k.label.startsWith(p) || k.name().toLowerCase().startsWith(p)) { return k; } }
			return null;
		}
	}

	/**
	 * A GAML keyword and the location of its Java implementation.
	 *
	 * @param name
	 *            the GAML name of the keyword
	 * @param kind
	 *            its kind
	 * @param owner
	 *            what declares it (e.g. "skill moving" for an attribute, "action goto" for an argument), or null
	 * @param java
	 *            the Java element that implements it (e.g. {@code Spatial.Punctal.closest_points_with(IShape, IShape)})
	 * @param plugin
	 *            the name of the plugin (project)
	 * @param path
	 *            the path of the Java file
	 * @param offset
	 *            the offset of the name in the Java file
	 * @param length
	 *            the length of the name in the Java file
	 * @param line
	 *            the line of the name in the Java file
	 */
	public record Entry(String name, Kind kind, String owner, String java, String plugin, String path, int offset,
			int length, int line) {

		/** A description of the keyword, e.g. "facet 'speed' of statement draw". */
		public String description() {
			return kind.label + (owner == null ? "" : " of " + owner);
		}
	}

	/**
	 * A Java source file to index.
	 *
	 * @param path
	 *            a unique path
	 * @param plugin
	 *            the name of the plugin (project) that contains it
	 * @param stamp
	 *            a modification stamp: the file is parsed again only when it changes
	 * @param content
	 *            the content of the file (only read when needed)
	 */
	public record Source(String path, String plugin, long stamp, Supplier<String> content) {

		/** The simple name of the main type declared in the file. */
		String typeName() {
			int slash = path.lastIndexOf('/');
			String n = path.substring(slash + 1);
			return n.endsWith(".java") ? n.substring(0, n.length() - 5) : n;
		}
	}

	// ------------------------------------------------------------------------------------------------------------
	// Values (names given as literals, constants or concatenations)
	// ------------------------------------------------------------------------------------------------------------

	/** A string value, as written in the sources. */
	private sealed interface Value permits Literal, Reference, Concat, Unknown {}

	private record Literal(String value) implements Value {}

	private record Reference(String name) implements Value {}

	private record Concat(List<Value> parts) implements Value {}

	private record Unknown(String text) implements Value {}

	private static Value valueOf(final Expression e) {
		return switch (e) {
			case StringLiteral s -> new Literal(s.getLiteralValue());
			case TextBlock t -> new Literal(t.getLiteralValue());
			case CharacterLiteral c -> new Literal(String.valueOf(c.charValue()));
			case NumberLiteral n -> new Literal(n.getToken());
			case Name n -> new Reference(n.getFullyQualifiedName());
			case ParenthesizedExpression p -> valueOf(p.getExpression());
			case InfixExpression i when i.getOperator() == InfixExpression.Operator.PLUS -> {
				List<Value> parts = new ArrayList<>();
				parts.add(valueOf(i.getLeftOperand()));
				parts.add(valueOf(i.getRightOperand()));
				for (Object o : i.extendedOperands()) { parts.add(valueOf((Expression) o)); }
				yield new Concat(parts);
			}
			default -> new Unknown(e.toString());
		};
	}

	/** The text of a value as written in the sources (used when it cannot be resolved). */
	private static String text(final Value v) {
		return switch (v) {
			case Literal l -> l.value();
			case Reference r -> r.name();
			case Concat c -> String.join(" + ", c.parts().stream().map(GamlKeywordIndex::text).toList());
			case Unknown u -> u.text();
		};
	}

	// ------------------------------------------------------------------------------------------------------------
	// Parsed files
	// ------------------------------------------------------------------------------------------------------------

	/** A type declared in a file: its super types and String constants, used to resolve the names. */
	private static final class TypeInfo {
		final String name;
		final TypeInfo enclosing;
		final FileInfo file;
		final List<String> supers = new ArrayList<>();
		final Map<String, Value> constants = new HashMap<>();

		TypeInfo(final String name, final TypeInfo enclosing, final FileInfo file) {
			this.name = name;
			this.enclosing = enclosing;
			this.file = file;
		}
	}

	/** A keyword whose name (and owner) remain to be resolved. */
	private record RawEntry(Value name, Kind kind, String ownerKind, Value owner, String ownerFallback, String java,
			TypeInfo context, int offset, int length, int line) {}

	/** The result of the parsing of a file. */
	private static final class FileInfo {
		final Source source;
		final List<TypeInfo> types = new ArrayList<>();
		final List<RawEntry> entries = new ArrayList<>();
		/** Static imports: member name -> type simple name. */
		final Map<String, String> staticImports = new HashMap<>();
		/** Static on-demand imports: type simple names. */
		final List<String> staticWildcards = new ArrayList<>();

		FileInfo(final Source source) {
			this.source = source;
		}
	}

	/** The marker that signals a file using the GAMA annotations. */
	private static final String MARKER = "gama.annotations";

	/** All the known sources (path -> source). */
	private Map<String, Source> sources = new HashMap<>();

	/** The sources by the simple name of their main type. */
	private final Map<String, List<Source>> sourcesByType = new HashMap<>();

	/** The files parsed (path -> info): the ones that use annotations, and those loaded to resolve constants. */
	private final Map<String, FileInfo> parsed = new HashMap<>();

	/** The stamps of the files when they were parsed (or found not to use annotations). */
	private final Map<String, Long> stamps = new HashMap<>();

	/** The types parsed, by simple name. */
	private final Map<String, List<TypeInfo>> types = new HashMap<>();

	/** The resolved entries. */
	private List<Entry> entries = List.of();

	/**
	 * Updates the index with the current set of sources: the new and modified files are parsed (if they use the GAMA
	 * annotations), the removed ones are forgotten, and all the names are resolved again.
	 *
	 * @return the number of files parsed
	 */
	public synchronized int update(final Collection<Source> current) {
		Map<String, Source> map = new LinkedHashMap<>();
		for (Source s : current) { map.put(s.path(), s); }
		sources = map;
		sourcesByType.clear();
		for (Source s : map.values()) { sourcesByType.computeIfAbsent(s.typeName(), k -> new ArrayList<>()).add(s); }
		int count = 0;
		parsed.keySet().removeIf(p -> !map.containsKey(p) || map.get(p).stamp() != stamps.get(p));
		stamps.keySet().removeIf(p -> !map.containsKey(p) || map.get(p).stamp() != stamps.get(p));
		for (Source s : map.values()) {
			if (stamps.containsKey(s.path())) { continue; }
			String content = s.content().get();
			stamps.put(s.path(), s.stamp());
			if (content != null && content.contains(MARKER)) {
				parsed.put(s.path(), parse(s, content));
				count++;
			}
		}
		rebuildTypes();
		entries = resolveAll();
		return count;
	}

	/** The resolved entries. */
	public synchronized List<Entry> entries() {
		return entries;
	}

	private void rebuildTypes() {
		types.clear();
		for (FileInfo f : parsed.values()) {
			for (TypeInfo t : f.types) { types.computeIfAbsent(t.name, k -> new ArrayList<>()).add(t); }
		}
	}

	/** The types with this simple name, parsing (once) the files that may declare it. */
	private List<TypeInfo> typesNamed(final String name) {
		List<TypeInfo> result = types.get(name);
		if (result != null) { return result; }
		for (Source s : sourcesByType.getOrDefault(name, List.of())) {
			if (parsed.containsKey(s.path())) { continue; }
			String content = s.content().get();
			if (content == null) { continue; }
			FileInfo f = parse(s, content);
			parsed.put(s.path(), f);
			stamps.put(s.path(), s.stamp());
			for (TypeInfo t : f.types) { types.computeIfAbsent(t.name, k -> new ArrayList<>()).add(t); }
		}
		return types.computeIfAbsent(name, k -> new ArrayList<>());
	}

	private List<Entry> resolveAll() {
		List<Entry> result = new ArrayList<>();
		for (FileInfo f : new ArrayList<>(parsed.values())) {
			for (RawEntry r : f.entries) {
				String name = resolveOrText(r.name(), r.context());
				String owner = r.ownerKind() == null ? null
						: r.ownerKind() + " " + (r.owner() == null ? r.ownerFallback()
								: resolveOrText(r.owner(), r.context()));
				result.add(new Entry(name, r.kind(), owner, r.java(), f.source.plugin(), f.source.path(), r.offset(),
						r.length(), r.line()));
			}
		}
		return List.copyOf(result);
	}

	private String resolveOrText(final Value v, final TypeInfo context) {
		String s = resolve(v, context, 0);
		return s == null ? text(v) : s;
	}

	// ------------------------------------------------------------------------------------------------------------
	// Resolution of the constants
	// ------------------------------------------------------------------------------------------------------------

	private String resolve(final Value v, final TypeInfo context, final int depth) {
		if (depth > 16) { return null; }
		return switch (v) {
			case Literal l -> l.value();
			case Unknown u -> null;
			case Concat c -> {
				StringBuilder sb = new StringBuilder();
				for (Value p : c.parts()) {
					String s = resolve(p, context, depth + 1);
					if (s == null) { yield null; }
					sb.append(s);
				}
				yield sb.toString();
			}
			case Reference r -> resolveReference(r.name(), context, depth);
		};
	}

	private String resolveReference(final String name, final TypeInfo context, final int depth) {
		int dot = name.lastIndexOf('.');
		if (dot > 0) {
			// Type.FIELD, Outer.Inner.FIELD or package.Type.FIELD
			String field = name.substring(dot + 1);
			String qualifier = name.substring(0, dot);
			String typeName = qualifier.substring(qualifier.lastIndexOf('.') + 1);
			String value = constantIn(typeName, field, depth);
			if (value != null) { return value; }
			// The type may be nested in a file named after one of the segments
			for (String segment : qualifier.split("\\.")) { typesNamed(segment); }
			return constantIn(typeName, field, depth);
		}
		// Own constants, those of the enclosing types and of the super types
		Deque<TypeInfo> queue = new ArrayDeque<>();
		Set<TypeInfo> visited = new HashSet<>();
		for (TypeInfo t = context; t != null; t = t.enclosing) { queue.add(t); }
		while (!queue.isEmpty()) {
			TypeInfo t = queue.poll();
			if (!visited.add(t) || visited.size() > 64) { continue; }
			Value v = t.constants.get(name);
			if (v != null) { return resolve(v, t, depth + 1); }
			for (String s : t.supers) {
				// A nested super type (e.g. ITopLevelAgent.Platform) lives in the file of its outermost type
				String[] segments = s.split("\\.");
				for (String segment : segments) { typesNamed(segment); }
				queue.addAll(typesNamed(segments[segments.length - 1]));
			}
		}
		if (context == null) { return null; }
		// Static imports
		String imported = context.file.staticImports.get(name);
		if (imported != null) {
			String value = constantIn(imported, name, depth);
			if (value != null) { return value; }
		}
		for (String type : context.file.staticWildcards) {
			String value = constantIn(type, name, depth);
			if (value != null) { return value; }
		}
		return null;
	}

	private String constantIn(final String typeName, final String field, final int depth) {
		for (TypeInfo t : typesNamed(typeName)) {
			Value v = t.constants.get(field);
			if (v != null) { return resolve(v, t, depth + 1); }
			// The constant may be inherited (e.g. from IKeyword)
			String inherited = resolveReference(field, t, depth + 1);
			if (inherited != null) { return inherited; }
		}
		return null;
	}

	// ------------------------------------------------------------------------------------------------------------
	// Parsing
	// ------------------------------------------------------------------------------------------------------------

	private static FileInfo parse(final Source source, final String content) {
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		Map<String, String> options = new HashMap<>();
		JavaCore.setComplianceOptions(JavaCore.latestSupportedJavaVersion(), options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(content.toCharArray());
		parser.setResolveBindings(false);
		parser.setIgnoreMethodBodies(true);
		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		FileInfo file = new FileInfo(source);
		for (Object o : unit.imports()) {
			ImportDeclaration i = (ImportDeclaration) o;
			if (!i.isStatic()) { continue; }
			String n = i.getName().getFullyQualifiedName();
			if (i.isOnDemand()) {
				file.staticWildcards.add(n.substring(n.lastIndexOf('.') + 1));
			} else {
				String type = n.substring(0, n.lastIndexOf('.'));
				file.staticImports.put(n.substring(n.lastIndexOf('.') + 1), type.substring(type.lastIndexOf('.') + 1));
			}
		}
		for (Object o : unit.types()) { visitType((AbstractTypeDeclaration) o, null, file, unit); }
		return file;
	}

	private static void visitType(final AbstractTypeDeclaration type, final TypeInfo enclosing, final FileInfo file,
			final CompilationUnit unit) {
		TypeInfo info = new TypeInfo(type.getName().getIdentifier(), enclosing, file);
		file.types.add(info);
		boolean isInterface = type instanceof TypeDeclaration td && td.isInterface();
		if (type instanceof TypeDeclaration td) {
			if (td.getSuperclassType() != null) { info.supers.add(qualifiedName(td.getSuperclassType())); }
			for (Object o : td.superInterfaceTypes()) { info.supers.add(qualifiedName((Type) o)); }
		}
		String javaType = enclosing == null ? info.name : javaName(enclosing) + "." + info.name;
		// The GAML name of the type (skill, species...), used as the owner of its attributes, facets and actions
		String ownerKind = null;
		Value owner = null;
		for (Object o : type.modifiers()) {
			if (!(o instanceof Annotation a)) { continue; }
			Kind k = switch (simpleName(a)) {
				case "skill" -> Kind.SKILL;
				case "species" -> Kind.SPECIES;
				case "symbol" -> Kind.STATEMENT;
				case "type" -> Kind.TYPE;
				case "gaml_class" -> Kind.CLASS;
				case "experiment" -> Kind.EXPERIMENT;
				case "display" -> Kind.DISPLAY;
				case "file" -> Kind.FILE;
				default -> null;
			};
			if (k == null) { continue; }
			List<Expression> names = values(a, k == Kind.EXPERIMENT || k == Kind.DISPLAY ? "value" : "name");
			if (!names.isEmpty() && owner == null) {
				ownerKind = k.label;
				owner = valueOf(names.get(0));
			}
		}
		for (Object o : type.modifiers()) {
			if (!(o instanceof Annotation a)) { continue; }
			switch (simpleName(a)) {
				case "skill" -> add(file, unit, info, Kind.SKILL, values(a, "name"), null, null, javaType);
				case "species" -> add(file, unit, info, Kind.SPECIES, values(a, "name"), null, null, javaType);
				case "symbol" -> add(file, unit, info, Kind.STATEMENT, values(a, "name"), null, null, javaType);
				case "type" -> add(file, unit, info, Kind.TYPE, values(a, "name"), null, null, javaType);
				case "gaml_class" -> add(file, unit, info, Kind.CLASS, values(a, "name"), null, null, javaType);
				case "experiment" -> add(file, unit, info, Kind.EXPERIMENT, values(a, "value"), null, null, javaType);
				case "display" -> add(file, unit, info, Kind.DISPLAY, values(a, "value"), null, null, javaType);
				case "file" -> add(file, unit, info, Kind.FILE, values(a, "name"), null, null, javaType);
				case "operator" -> add(file, unit, info, Kind.OPERATOR, values(a, "value"), null, null, javaType);
				case "vars" -> nested(file, unit, info, Kind.ATTRIBUTE, values(a, "value"), "variable",
						ownerKind == null ? "class" : ownerKind, owner, info.name, javaType);
				case "facets" -> nested(file, unit, info, Kind.FACET, values(a, "value"), "facet",
						ownerKind == null ? "class" : ownerKind, owner, info.name, javaType);
				default -> {}
			}
		}
		String membersOwnerKind = ownerKind == null ? "class" : ownerKind;
		for (Object o : type.bodyDeclarations()) {
			BodyDeclaration d = (BodyDeclaration) o;
			switch (d) {
				case AbstractTypeDeclaration nested -> visitType(nested, info, file, unit);
				case FieldDeclaration f -> visitField(f, info, isInterface, file, unit, javaType);
				case MethodDeclaration m -> visitMethod(m, info, file, unit, javaType, membersOwnerKind, owner);
				default -> {}
			}
		}
	}

	private static void visitField(final FieldDeclaration f, final TypeInfo info, final boolean isInterface,
			final FileInfo file, final CompilationUnit unit, final String javaType) {
		boolean isString = "String".equals(simpleName(f.getType()));
		for (Object o : f.fragments()) {
			VariableDeclarationFragment v = (VariableDeclarationFragment) o;
			if (isString && v.getInitializer() != null) {
				info.constants.put(v.getName().getIdentifier(), valueOf(v.getInitializer()));
			}
		}
		for (Object o : f.modifiers()) {
			if (o instanceof Annotation a && "constant".equals(simpleName(a))) {
				String java = javaType + "." + ((VariableDeclarationFragment) f.fragments().get(0)).getName();
				List<Expression> names = new ArrayList<>(values(a, "value"));
				names.addAll(values(a, "altNames"));
				add(file, unit, info, Kind.CONSTANT, names, null, null, java);
			}
		}
	}

	private static void visitMethod(final MethodDeclaration m, final TypeInfo info, final FileInfo file,
			final CompilationUnit unit, final String javaType, final String ownerKind, final Value owner) {
		String java = javaType + "." + m.getName().getIdentifier() + "(" + parameters(m) + ")";
		for (Object o : m.modifiers()) {
			if (!(o instanceof Annotation a)) { continue; }
			switch (simpleName(a)) {
				case "operator" -> add(file, unit, info, Kind.OPERATOR, values(a, "value"), null, null, java);
				case "getter" -> add(file, unit, info, Kind.GETTER, values(a, "value"), ownerKind, owner, java);
				case "setter" -> add(file, unit, info, Kind.SETTER, values(a, "value"), ownerKind, owner, java);
				case "action" -> {
					List<Expression> names = values(a, "name");
					add(file, unit, info, Kind.ACTION, names, ownerKind, owner, java);
					nested(file, unit, info, Kind.ARGUMENT, values(a, "args"), "arg", "action",
							names.isEmpty() ? null : valueOf(names.get(0)), m.getName().getIdentifier(), java);
				}
				default -> {}
			}
		}
	}

	/** Adds the entries corresponding to the names given in a nested annotation (variable, facet, arg). */
	private static void nested(final FileInfo file, final CompilationUnit unit, final TypeInfo info, final Kind kind,
			final List<Expression> annotations, final String annotation, final String ownerKind, final Value owner,
			final String ownerFallback, final String java) {
		for (Expression e : annotations) {
			if (e instanceof Annotation a && annotation.equals(simpleName(a))) {
				for (Expression n : values(a, "name")) {
					file.entries.add(new RawEntry(valueOf(n), kind, ownerKind, owner, ownerFallback, java, info,
							n.getStartPosition(), n.getLength(), unit.getLineNumber(n.getStartPosition())));
				}
			}
		}
	}

	private static void add(final FileInfo file, final CompilationUnit unit, final TypeInfo info, final Kind kind,
			final List<Expression> names, final String ownerKind, final Value owner, final String java) {
		for (Expression n : names) {
			file.entries.add(new RawEntry(valueOf(n), kind, ownerKind, owner, info.name, java, info,
					n.getStartPosition(), n.getLength(), unit.getLineNumber(n.getStartPosition())));
		}
	}

	/** The values of a member of an annotation (the elements of an array, or a single value). */
	private static List<Expression> values(final Annotation a, final String member) {
		Expression value = null;
		if (a instanceof SingleMemberAnnotation s && "value".equals(member)) {
			value = s.getValue();
		} else if (a instanceof NormalAnnotation n) {
			for (Object o : n.values()) {
				MemberValuePair p = (MemberValuePair) o;
				if (member.equals(p.getName().getIdentifier())) { value = p.getValue(); }
			}
		}
		if (value == null) { return List.of(); }
		if (value instanceof ArrayInitializer array) {
			List<Expression> result = new ArrayList<>();
			for (Object o : array.expressions()) { result.add((Expression) o); }
			return result;
		}
		return List.of(value);
	}

	private static String parameters(final MethodDeclaration m) {
		List<String> params = new ArrayList<>();
		for (Object o : m.parameters()) {
			SingleVariableDeclaration p = (SingleVariableDeclaration) o;
			String type = p.getType().toString() + (p.isVarargs() ? "..." : "");
			if (!"IScope".equals(simpleName(p.getType()))) { params.add(type); }
		}
		return String.join(", ", params);
	}

	private static String javaName(final TypeInfo t) {
		return t.enclosing == null ? t.name : javaName(t.enclosing) + "." + t.name;
	}

	private static String simpleName(final Annotation a) {
		return simpleName(a.getTypeName());
	}

	private static String simpleName(final Name n) {
		return n instanceof QualifiedName q ? q.getName().getIdentifier() : ((SimpleName) n).getIdentifier();
	}

	/** The (possibly qualified) name of a type, without its type arguments. */
	private static String qualifiedName(final Type t) {
		String s = t.toString();
		int lt = s.indexOf('<');
		return (lt >= 0 ? s.substring(0, lt) : s).strip();
	}

	/** The simple name of a type, without its type arguments (e.g. {@code List} for {@code java.util.List<String>}). */
	private static String simpleName(final Type t) {
		String s = t.toString();
		int lt = s.indexOf('<');
		if (lt >= 0) { s = s.substring(0, lt); }
		return s.substring(s.lastIndexOf('.') + 1).strip();
	}

}
