package gaml.compiler.ui.wizards.experiment;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParser;

import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.GamlPackage;
import gaml.compiler.gaml.S_Definition;
import gaml.compiler.gaml.S_Experiment;
import gaml.compiler.gaml.S_Reflex;
import gaml.compiler.gaml.Statement;
import gaml.compiler.gaml.StringLiteral;
import gaml.compiler.gaml.TypeRef;
import gaml.compiler.gaml.VariableRef;
import gaml.compiler.ui.wizards.action.ActionBodyContext;
import gaml.compiler.ui.wizards.species.SpeciesDraft;

/**
 * Lossless GAML source adapter for one experiment. Edits replace only the tokens that changed (name, type, header facet
 * values, attribute/parameter/monitor fields, the text between the init braces), remove deleted facets and entries
 * and add new ones after the existing ones of the same kind. Everything the draft does not represent (displays,
 * methods, tests, reflexes, actions, permanent outputs, comments, unknown facets of attributes, parameters and
 * monitors) is kept as written. Rendering returns a complete snapshot, never an edit on live offsets.
 */
public final class ExperimentSource {

	private record Patch(int offset, int length, String text) {}

	private static final String MARKER = "/*__experiment_wizard_init__*/";
	private static final List<String> ATTRIBUTE_FACETS = List.of("init", "update", "function", "min", "max");

	private final String source;
	private final List<String> otherNames = new ArrayList<>();
	private final java.util.Map<String, String> experimentParents = new java.util.LinkedHashMap<>();
	private final List<String> globalNames = new ArrayList<>();
	private final S_Experiment experiment;
	private final List<S_Definition> attributeNodes = new ArrayList<>();
	private final List<Statement> parameterNodes = new ArrayList<>();
	private final List<Statement> monitorNodes = new ArrayList<>();
	private final List<Statement> outputs = new ArrayList<>();
	private S_Reflex initNode;
	private final Set<Statement> removed = new java.util.HashSet<>();
	private final ExperimentDraft original;

	/**
	 * @param create
	 *            true to add an experiment at the end of the model; false to edit the experiment enclosing
	 *            {@code offset}
	 * @param type
	 *            the type of a created experiment
	 */
	public ExperimentSource(final String source, final int offset, final IParser parser, final boolean create,
			final String type) {
		this.source = source;
		var parsed = parser.parse(new StringReader(source));
		if (parsed.hasSyntaxErrors()) throw new IllegalArgumentException("Fix the document's syntax errors first.");
		EObject root = parsed.getRootASTElement();
		S_Experiment selected = null;
		if (!create) {
			int length = Integer.MAX_VALUE;
			for (var it = root.eAllContents(); it.hasNext();) if (it.next() instanceof S_Experiment candidate) {
				INode node = NodeModelUtils.findActualNodeFor(candidate);
				if (offset >= node.getOffset() && offset < node.getEndOffset() && node.getLength() < length) {
					selected = candidate;
					length = node.getLength();
				}
			}
			if (selected == null) throw new IllegalArgumentException("Place the cursor in an experiment declaration.");
		}
		experiment = selected;
		for (var it = root.eAllContents(); it.hasNext();)
			if (it.next() instanceof S_Experiment other && other.getName() != null) {
				if (other != experiment) otherNames.add(other.getName());
				Facet parent = facet(other, "parent");
				experimentParents.put(other.getName(), parent == null ? "" : text(parent.getExpr()).trim());
			}
		for (var it = root.eAllContents(); it.hasNext();) if (it.next() instanceof gaml.compiler.gaml.S_Global global
				&& global.getBlock() != null)
			for (Statement statement : global.getBlock().getStatements())
				if (isDeclaration(statement) && !isStatementKeyword(keyword(statement)))
					globalNames.add(((S_Definition) statement).getName());
		original = create ? ExperimentDraft.empty(type) : read();
	}

	/** Attributes declared in the model's global section (candidate parameter variables). */
	public List<String> globalNames() { return List.copyOf(globalNames); }

	/** Existing local experiments excluding this declaration and descendants that would introduce a parent cycle. */
	public List<String> parentNames() {
		return otherNames.stream().filter(candidate -> {
			Set<String> seen = new java.util.HashSet<>();
			String ancestor = candidate;
			while (ancestor != null && seen.add(ancestor)) {
				if (!isCreation() && ancestor.equals(original.name())) return false;
				ancestor = experimentParents.get(ancestor);
			}
			return true;
		}).distinct().sorted().toList();
	}

	public ExperimentDraft draft() { return original; }

	public boolean isCreation() { return experiment == null; }

	/** True when the selection covers the experiment header (or the whole declaration). */
	public boolean isExperimentSelection(final int offset, final int length) {
		if (experiment == null || offset < 0 || length < 0) return false;
		INode node = NodeModelUtils.findActualNodeFor(experiment);
		int headerEnd = experiment.getBlock() == null ? node.getEndOffset()
				: NodeModelUtils.findActualNodeFor(experiment.getBlock()).getOffset();
		long end = (long) offset + length;
		return offset >= node.getOffset() && end <= headerEnd
				|| offset <= node.getOffset() && end >= node.getEndOffset();
	}

	/** Names of the attributes declared in the experiment (offered to parameters with the adapter's variables). */
	public List<String> attributeNames() {
		return original.variables().stream().map(SpeciesDraft.Variable::name).toList();
	}

	// ----------------------------------------------------------------------------------------------- reading

	private ExperimentDraft read() {
		if (experiment.getBlock() == null) throw new IllegalArgumentException("This experiment has no block.");
		String type = ExperimentDraft.GUI;
		List<SpeciesDraft.Facet> facets = new ArrayList<>();
		for (Facet facet : experiment.getFacets()) {
			String key = key(facet);
			if (!SpeciesDraft.isIdentifier(key) || facet.getBlock() != null || facet.getExpr() == null || "name".equals(key))
				throw new IllegalArgumentException("Edit this experiment's " + key + " facet directly in the source.");
			String value = text(facet.getExpr()).trim();
			if ("type".equals(key)) {
				type = unquote(value);
				if (!ExperimentDraft.TYPES.contains(type))
					throw new IllegalArgumentException("Edit this experiment's type directly in the source.");
			} else facets.add(new SpeciesDraft.Facet(key, value));
		}
		List<SpeciesDraft.Variable> variables = new ArrayList<>();
		List<ExperimentDraft.Parameter> parameters = new ArrayList<>();
		List<ExperimentDraft.Monitor> monitors = new ArrayList<>();
		String init = "";
		for (Statement statement : experiment.getBlock().getStatements()) {
			String keyword = keyword(statement);
			if ("parameter".equals(keyword)) {
				parameterNodes.add(statement);
				parameters.add(readParameter(statement));
			} else if (isDeclaration(statement) && !isStatementKeyword(keyword)) {
				attributeNodes.add((S_Definition) statement);
				variables.add(readAttribute((S_Definition) statement));
			} else if (statement instanceof S_Reflex reflex && "init".equals(reflex.getKey()) && initNode == null) {
				initNode = reflex;
				init = body(reflex);
			} else if ("output".equals(keyword) && statement.getBlock() != null) {
				outputs.add(statement);
				for (Statement child : statement.getBlock().getStatements()) if ("monitor".equals(keyword(child))) {
					monitorNodes.add(child);
					monitors.add(readMonitor(child));
				}
			}
		}
		return new ExperimentDraft(experiment.getName(), type, facets, variables, init, parameters, monitors);
	}

	private static boolean isDeclaration(final Statement statement) {
		return statement instanceof S_Definition && !(statement instanceof gaml.compiler.gaml.S_Callable);
	}

	/** Keywords whose {@code keyword name ...;} form parses as a typed declaration but is a statement. */
	private static boolean isStatementKeyword(final String keyword) {
		return List.of("parameter", "monitor", "method", "test", "state", "task", "plan", "perceive", "rule")
				.contains(keyword);
	}

	private static String keyword(final Statement statement) {
		if (statement instanceof S_Definition definition)
			return definition.getTkey() instanceof TypeRef type ? text(type).trim() : null;
		return statement.getKey();
	}

	private static SpeciesDraft.Variable readAttribute(final S_Definition definition) {
		String[] values = { "", "", "", "", "" };
		for (Facet facet : definition.getFacets()) {
			int index = ATTRIBUTE_FACETS.indexOf(attributeKey(facet));
			if (index >= 0 && facet.getExpr() != null) values[index] = text(facet.getExpr()).trim();
		}
		return new SpeciesDraft.Variable(definition.getName(), text(definition.getTkey()).trim(), values[0], values[1],
				values[2], values[3], values[4]);
	}

	private static String attributeKey(final Facet facet) {
		String key = key(facet);
		return switch (key) {
			case "<-" -> "init";
			case "->" -> "function";
			default -> key;
		};
	}

	/** The label of a parameter or monitor: its string literal or identifier name. */
	private static String title(final Statement statement, final String kind) {
		if (statement instanceof S_Definition definition) return definition.getName();
		if (statement.getExpr() instanceof StringLiteral literal) return literal.getOp();
		if (statement.getExpr() instanceof VariableRef) return text(statement.getExpr()).trim();
		throw new IllegalArgumentException("Edit the title of the " + kind + " '" + text(statement).trim()
				+ "' directly in the source (it is not a plain label).");
	}

	private static ExperimentDraft.Parameter readParameter(final Statement statement) {
		String variable = "";
		List<SpeciesDraft.Facet> facets = new ArrayList<>();
		for (Facet facet : statement.getFacets()) {
			String key = key(facet);
			if ("var".equals(key)) variable = value(facet);
			else if (!"name".equals(key)) facets.add(new SpeciesDraft.Facet(key, value(facet)));
		}
		return new ExperimentDraft.Parameter(title(statement, "parameter"), variable, facets);
	}

	private static ExperimentDraft.Monitor readMonitor(final Statement statement) {
		String monitored = "";
		List<SpeciesDraft.Facet> facets = new ArrayList<>();
		for (Facet facet : statement.getFacets()) {
			String key = key(facet);
			if ("value".equals(key)) monitored = value(facet);
			else if (!"name".equals(key)) facets.add(new SpeciesDraft.Facet(key, value(facet)));
		}
		return new ExperimentDraft.Monitor(title(statement, "monitor"), monitored, facets);
	}

	private static String value(final Facet facet) {
		if (facet.getExpr() != null) return text(facet.getExpr()).trim();
		return facet.getBlock() == null ? "" : text(facet.getBlock()).trim();
	}

	private static String key(final Facet facet) {
		String key = facet.getKey() == null ? "" : facet.getKey().trim();
		return key.endsWith(":") ? key.substring(0, key.length() - 1).trim() : key;
	}

	private static String unquote(final String value) {
		return value.length() >= 2 && (value.startsWith("\"") && value.endsWith("\"")
				|| value.startsWith("'") && value.endsWith("'")) ? value.substring(1, value.length() - 1) : value;
	}

	private static String body(final Statement statement) {
		String text = NodeModelUtils.findActualNodeFor(statement.getBlock()).getText();
		return text.substring(text.indexOf('{') + 1, text.lastIndexOf('}'));
	}

	private static Facet facet(final Statement target, final String key) {
		for (Facet facet : target.getFacets()) if (key(facet).equals(key)) return facet;
		return null;
	}

	private static Facet attributeFacet(final S_Definition target, final String key) {
		for (Facet facet : target.getFacets()) if (attributeKey(facet).equals(key)) return facet;
		return null;
	}

	private static String text(final EObject object) {
		INode node = NodeModelUtils.findActualNodeFor(object);
		StringBuilder value = new StringBuilder();
		for (var leaf : node.getLeafNodes())
			value.append(!leaf.isHidden() || leaf.getText().isBlank() ? leaf.getText() : " ");
		return value.toString();
	}

	// ----------------------------------------------------------------------------------------------- validation

	/** Structural checks against the document (compilation is separate). */
	public String validationError(final ExperimentDraft draft) {
		String error = draft.validationError();
		if (error != null) return error;
		if (otherNames.contains(draft.name())) return "An experiment named '" + draft.name() + "' already exists.";
		return null;
	}

	/** Names of the other experiments of the model. */
	public Set<String> otherExperimentNames() { return new TreeSet<>(otherNames); }

	/** The rendered document split around the init code (an init block is added for the context when absent). */
	public ActionBodyContext bodyContext(final ExperimentDraft draft, final URI uri) {
		String rendered = render(new ExperimentDraft(draft.name(), draft.type(), draft.facets(), draft.variables(),
				MARKER, draft.parameters(), draft.monitors()));
		int index = rendered.indexOf(MARKER);
		if (index < 0 || rendered.indexOf(MARKER, index + 1) >= 0)
			throw new IllegalStateException("The document already contains the experiment wizard marker.");
		return new ActionBodyContext(uri, rendered.substring(0, index), rendered.substring(index + MARKER.length()));
	}

	// ----------------------------------------------------------------------------------------------- rendering

	/** The complete document with the draft applied; the source itself when nothing changed. */
	public String render(final ExperimentDraft draft) {
		if (experiment == null) return create(draft);
		if (draft.equals(original)) return source;
		removed.clear();
		List<Patch> patches = new ArrayList<>();
		patchName(draft, patches);
		patchHeader(draft, patches);
		patchAttributes(draft, patches);
		patchParameters(draft, patches);
		patchInit(draft, patches);
		patchMonitors(draft, patches);
		if (patches.isEmpty()) return source;
		// Insertions at the same offset keep their order of creation.
		List<Patch> ordered = new ArrayList<>(patches);
		ordered.sort(Comparator.comparingInt(Patch::offset));
		StringBuilder result = new StringBuilder(source);
		for (int i = ordered.size() - 1; i >= 0; i--) {
			Patch patch = ordered.get(i);
			result.replace(patch.offset(), patch.offset() + patch.length(), patch.text());
		}
		return result.toString();
	}

	private void patchName(final ExperimentDraft draft, final List<Patch> patches) {
		if (draft.name().equals(original.name())) return;
		INode node = NodeModelUtils.findNodesForFeature(experiment, GamlPackage.Literals.GAML_DEFINITION__NAME).getFirst();
		boolean quoted = node.getText().trim().startsWith("\"") || node.getText().trim().startsWith("'");
		replaceTokens(patches, node, quoted ? ExperimentDraft.quote(draft.name()) : ExperimentDraft.quoteName(draft.name()));
	}

	private void patchHeader(final ExperimentDraft draft, final List<Patch> patches) {
		StringBuilder added = new StringBuilder();
		if (!draft.type().equals(original.type())) {
			Facet type = facet(experiment, "type");
			if (type != null) replaceTokens(patches, NodeModelUtils.findActualNodeFor(type.getExpr()), draft.type());
			else added.append(" type: ").append(draft.type());
		}
		facets(experiment, original.facets(), draft.facets(), patches, added);
		if (!added.isEmpty()) patches.add(new Patch(headerEnd(experiment), 0, added.toString()));
	}

	/** Replaces changed values, removes deleted facets and appends new ones to {@code added}. */
	private void facets(final Statement target, final List<SpeciesDraft.Facet> before,
			final List<SpeciesDraft.Facet> after, final List<Patch> patches, final StringBuilder added) {
		for (var facet : after) {
			String value = find(before, facet.name());
			if (value == null) added.append(' ').append(facet.name()).append(": ").append(facet.value().trim());
			else if (!value.equals(facet.value().trim())) {
				Facet node = facet(target, facet.name());
				replaceTokens(patches, NodeModelUtils.findActualNodeFor(
						node.getExpr() != null ? node.getExpr() : node.getBlock()), facet.value().trim());
			}
		}
		for (var facet : before) if (find(after, facet.name()) == null) removeNode(patches, facet(target, facet.name()));
	}

	private static String find(final List<SpeciesDraft.Facet> facets, final String name) {
		for (var facet : facets) if (facet.name().equals(name)) return facet.value().trim();
		return null;
	}

	private void patchAttributes(final ExperimentDraft draft, final List<Patch> patches) {
		int[] match = align(original.variables(), draft.variables());
		boolean[] kept = new boolean[attributeNodes.size()];
		StringBuilder added = new StringBuilder();
		String indent = memberIndent();
		for (int i = 0; i < match.length; i++) {
			var after = draft.variables().get(i);
			if (match[i] < 0) {
				added.append(newline()).append(indent).append(attribute(after));
				continue;
			}
			kept[match[i]] = true;
			var before = original.variables().get(match[i]);
			if (before.equals(after)) continue;
			S_Definition node = attributeNodes.get(match[i]);
			if (!before.type().equals(after.type()))
				replaceTokens(patches, NodeModelUtils.findActualNodeFor(node.getTkey()), after.type());
			if (!before.name().equals(after.name())) replaceTokens(patches,
					NodeModelUtils.findNodesForFeature(node, GamlPackage.Literals.GAML_DEFINITION__NAME).getFirst(),
					after.name());
			StringBuilder facets = new StringBuilder();
			String[] was = values(before);
			String[] now = values(after);
			for (int f = 0; f < ATTRIBUTE_FACETS.size(); f++) {
				if (was[f].equals(now[f].trim())) continue;
				Facet facet = attributeFacet(node, ATTRIBUTE_FACETS.get(f));
				if (now[f].isBlank()) {
					if (facet != null) removeNode(patches, facet);
				} else if (facet != null && facet.getExpr() != null)
					replaceTokens(patches, NodeModelUtils.findActualNodeFor(facet.getExpr()), now[f].trim());
				else facets.append(attributeFacet(ATTRIBUTE_FACETS.get(f), now[f]));
			}
			if (!facets.isEmpty()) patches.add(new Patch(headerEnd(node), 0, facets.toString()));
		}
		for (int i = 0; i < kept.length; i++) if (!kept[i]) removeStatement(patches, attributeNodes.get(i));
		// Members are added in this order (attributes, parameters, init), so later anchors see earlier removals.
		if (!added.isEmpty()) patches.add(new Patch(anchor(attributeNodes), 0, added.toString()));
	}

	private static String[] values(final SpeciesDraft.Variable variable) {
		return new String[] { variable.init().trim(), variable.update().trim(), variable.function().trim(),
				variable.min().trim(), variable.max().trim() };
	}

	private static String attributeFacet(final String name, final String value) {
		return switch (name) {
			case "init" -> " <- " + value.trim();
			case "function" -> " -> " + value.trim();
			default -> " " + name + ": " + value.trim();
		};
	}

	private static String attribute(final SpeciesDraft.Variable variable) {
		StringBuilder text = new StringBuilder(variable.type()).append(' ').append(variable.name());
		String[] values = values(variable);
		for (int f = 0; f < values.length; f++)
			if (!values[f].isEmpty()) text.append(attributeFacet(ATTRIBUTE_FACETS.get(f), values[f]));
		return text.append(';').toString();
	}

	private void patchParameters(final ExperimentDraft draft, final List<Patch> patches) {
		int[] match = align(original.parameters(), draft.parameters());
		boolean[] kept = new boolean[parameterNodes.size()];
		StringBuilder added = new StringBuilder();
		String indent = memberIndent();
		for (int i = 0; i < match.length; i++) {
			var after = draft.parameters().get(i);
			if (match[i] < 0) {
				added.append(newline()).append(indent).append(ExperimentWizard.parameter(after));
				continue;
			}
			kept[match[i]] = true;
			var before = original.parameters().get(match[i]);
			if (before.equals(after)) continue;
			Statement node = parameterNodes.get(match[i]);
			if (!before.title().equals(after.title())) patchTitle(node, after.title(), patches);
			StringBuilder facets = new StringBuilder();
			patchRequired(node, "var", before.variable(), after.variable(), patches, facets);
			facets(node, before.facets(), after.facets(), patches, facets);
			if (!facets.isEmpty()) patches.add(new Patch(headerEnd(node), 0, facets.toString()));
		}
		for (int i = 0; i < kept.length; i++) if (!kept[i]) removeStatement(patches, parameterNodes.get(i));
		if (!added.isEmpty()) {
			int offset = anchor(parameterNodes, attributeNodes);
			patches.add(new Patch(offset, 0, added.toString()));
		}
	}

	private void patchMonitors(final ExperimentDraft draft, final List<Patch> patches) {
		int[] match = align(original.monitors(), draft.monitors());
		boolean[] kept = new boolean[monitorNodes.size()];
		List<ExperimentDraft.Monitor> added = new ArrayList<>();
		for (int i = 0; i < match.length; i++) {
			var after = draft.monitors().get(i);
			if (match[i] < 0) {
				added.add(after);
				continue;
			}
			kept[match[i]] = true;
			var before = original.monitors().get(match[i]);
			if (before.equals(after)) continue;
			Statement node = monitorNodes.get(match[i]);
			if (!before.title().equals(after.title())) patchTitle(node, after.title(), patches);
			StringBuilder facets = new StringBuilder();
			patchRequired(node, "value", before.value(), after.value(), patches, facets);
			facets(node, before.facets(), after.facets(), patches, facets);
			if (!facets.isEmpty()) patches.add(new Patch(headerEnd(node), 0, facets.toString()));
		}
		for (int i = 0; i < kept.length; i++) if (!kept[i]) removeStatement(patches, monitorNodes.get(i));
		if (added.isEmpty()) return;
		String nl = newline();
		StringBuilder text = new StringBuilder();
		List<Statement> keptMonitors = monitorNodes.stream().filter(m -> !removed.contains(m)).toList();
		if (!monitorNodes.isEmpty() || !outputs.isEmpty()) {
			Statement output = keptMonitors.isEmpty()
					? monitorNodes.isEmpty() ? outputs.getFirst() : (Statement) monitorNodes.getFirst().eContainer().eContainer()
					: null;
			String indent = output == null ? lineIndent(NodeModelUtils.findActualNodeFor(keptMonitors.getLast()).getOffset())
					: lineIndent(NodeModelUtils.findActualNodeFor(output).getOffset()) + "\t";
			for (var monitor : added) text.append(nl).append(indent).append(ExperimentWizard.monitor(monitor));
			int offset = output == null ? NodeModelUtils.findActualNodeFor(keptMonitors.getLast()).getEndOffset()
					: afterBrace(output);
			patches.add(new Patch(offset, 0, text.toString()));
			return;
		}
		String indent = memberIndent();
		text.append(nl).append(indent).append("output {");
		for (var monitor : added) text.append(nl).append(indent).append('\t').append(ExperimentWizard.monitor(monitor));
		text.append(nl).append(indent).append('}');
		patches.add(new Patch(beforeClosing(), 0, text.toString()));
	}

	/** Patches a required facet ({@code var:}, {@code value:}): replaced, or added when it was missing. */
	private static void patchRequired(final Statement node, final String name, final String before, final String after,
			final List<Patch> patches, final StringBuilder added) {
		if (before.equals(after.trim())) return;
		Facet facet = facet(node, name);
		if (facet != null && facet.getExpr() != null)
			replaceTokens(patches, NodeModelUtils.findActualNodeFor(facet.getExpr()), after.trim());
		else if (facet == null) added.append(' ').append(name).append(": ").append(after.trim());
		else throw new IllegalArgumentException("Edit the " + name + " facet directly in the source.");
	}

	private static void patchTitle(final Statement node, final String title, final List<Patch> patches) {
		INode target = node instanceof S_Definition
				? NodeModelUtils.findNodesForFeature(node, GamlPackage.Literals.GAML_DEFINITION__NAME).getFirst()
				: NodeModelUtils.findActualNodeFor(node.getExpr());
		replaceTokens(patches, target, ExperimentDraft.quote(title));
	}

	private void patchInit(final ExperimentDraft draft, final List<Patch> patches) {
		if (draft.init().equals(original.init())) return;
		if (initNode != null) {
			INode block = NodeModelUtils.findActualNodeFor(initNode.getBlock());
			int start = block.getTotalOffset() + block.getText().indexOf('{') + 1;
			int end = block.getTotalOffset() + block.getText().lastIndexOf('}');
			patches.add(new Patch(start, end - start, draft.init()));
			return;
		}
		if (draft.init().isBlank()) return;
		String indent = memberIndent();
		int offset = anchor(parameterNodes, attributeNodes);
		patches.add(new Patch(offset, 0, newline() + indent + "init " + block(draft.init(), indent)));
	}

	private String create(final ExperimentDraft draft) {
		String nl = newline();
		String text = ExperimentWizard.render(draft, null).replace("\n", nl);
		String trimmed = source.stripTrailing();
		return trimmed + nl + nl + text;
	}

	/** {@code { body }}: the body is kept verbatim; line breaks are only added around it when absent. */
	private String block(final String body, final String indent) {
		String nl = newline();
		int lastLine = body.lastIndexOf('\n');
		boolean closed = lastLine >= 0 && body.substring(lastLine + 1).isBlank();
		return "{" + (body.startsWith("\n") || body.startsWith("\r") ? "" : nl) + body + (closed ? "" : nl + indent)
				+ "}";
	}

	// ----------------------------------------------------------------------------------------------- positions

	/** Pairs each new entry with an original one: equal entries in order, then the remaining ones by position. */
	static <T> int[] align(final List<T> before, final List<T> after) {
		int n = before.size();
		int m = after.size();
		int[][] lcs = new int[n + 1][m + 1];
		for (int i = n - 1; i >= 0; i--) for (int j = m - 1; j >= 0; j--)
			lcs[i][j] = before.get(i).equals(after.get(j)) ? lcs[i + 1][j + 1] + 1 : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
		int[] match = new int[m];
		java.util.Arrays.fill(match, -1);
		int i = 0;
		int j = 0;
		List<int[]> anchors = new ArrayList<>();
		while (i < n && j < m) {
			if (before.get(i).equals(after.get(j))) {
				anchors.add(new int[] { i, j });
				i++;
				j++;
			} else if (lcs[i + 1][j] >= lcs[i][j + 1]) i++;
			else j++;
		}
		anchors.add(new int[] { n, m });
		int pi = 0;
		int pj = 0;
		// Unmatched entries between two equal ones are modifications of each other, in order.
		for (int[] anchor : anchors) {
			for (int bi = pi, aj = pj; bi < anchor[0] && aj < anchor[1]; bi++, aj++) match[aj] = bi;
			if (anchor[0] < n) match[anchor[1]] = anchor[0];
			pi = anchor[0] + 1;
			pj = anchor[1] + 1;
		}
		return match;
	}

	/**
	 * End of the last kept statement of the first non-empty list (or just after the experiment's opening brace);
	 * removed statements are never anchors since their text disappears.
	 */
	@SafeVarargs
	private int anchor(final List<? extends Statement>... lists) {
		for (var list : lists) for (int i = list.size() - 1; i >= 0; i--)
			if (!removed.contains(list.get(i))) return NodeModelUtils.findActualNodeFor(list.get(i)).getEndOffset();
		return afterBrace(experiment);
	}

	private static int afterBrace(final Statement statement) {
		INode block = NodeModelUtils.findActualNodeFor(statement.getBlock());
		return block.getTotalOffset() + block.getText().indexOf('{') + 1;
	}

	/** Just before the end of the line preceding the experiment's closing brace. */
	private int beforeClosing() {
		INode block = NodeModelUtils.findActualNodeFor(experiment.getBlock());
		int close = block.getTotalOffset() + block.getText().lastIndexOf('}');
		int lineStart = source.lastIndexOf('\n', close - 1) + 1;
		if (!source.substring(lineStart, close).isBlank()) return close;
		int end = lineStart - 1;
		if (end > 0 && source.charAt(end - 1) == '\r') end--;
		return Math.max(end, afterBrace(experiment));
	}

	/** The indentation of the experiment's members. */
	private String memberIndent() {
		for (Statement statement : experiment.getBlock().getStatements()) {
			int offset = NodeModelUtils.findActualNodeFor(statement).getOffset();
			int lineStart = source.lastIndexOf('\n', offset - 1) + 1;
			if (source.substring(lineStart, offset).isBlank()) return source.substring(lineStart, offset);
		}
		return lineIndent(NodeModelUtils.findActualNodeFor(experiment).getOffset()) + "\t";
	}

	/** End offset of the last header token (before the block or the ';'). */
	private static int headerEnd(final Statement statement) {
		INode node = NodeModelUtils.findActualNodeFor(statement);
		int limit = statement.getBlock() == null ? node.getEndOffset()
				: NodeModelUtils.findActualNodeFor(statement.getBlock()).getOffset();
		ILeafNode last = null;
		for (ILeafNode leaf : node.getLeafNodes()) {
			if (leaf.getOffset() >= limit) break;
			if (!leaf.isHidden()
					&& !(statement.getBlock() == null && ";".equals(leaf.getText()) && leaf.getEndOffset() == limit))
				last = leaf;
		}
		return last.getEndOffset();
	}

	private String lineIndent(final int offset) {
		int start = source.lastIndexOf('\n', offset - 1) + 1;
		int end = start;
		while (end < source.length() && (source.charAt(end) == ' ' || source.charAt(end) == '\t')) end++;
		return source.substring(start, end);
	}

	private String newline() { return source.contains("\r\n") ? "\r\n" : "\n"; }

	/** Removes a statement; its whole lines when nothing else is on them. */
	private void removeStatement(final List<Patch> patches, final Statement statement) {
		removed.add(statement);
		INode node = NodeModelUtils.findActualNodeFor(statement);
		int start = node.getOffset();
		int end = node.getEndOffset();
		int lineStart = source.lastIndexOf('\n', start - 1) + 1;
		int lineEnd = source.indexOf('\n', end);
		if (lineEnd < 0) lineEnd = source.length();
		String after = source.substring(end, lineEnd).replace("\r", "");
		if (source.substring(lineStart, start).isBlank() && after.isBlank()) {
			start = lineStart;
			end = Math.min(source.length(), lineEnd + 1);
		}
		patches.add(new Patch(start, end - start, ""));
	}

	/** Removes a facet's tokens and the whitespace just before it; comments are kept. */
	private void removeNode(final List<Patch> patches, final Facet facet) {
		INode node = NodeModelUtils.findActualNodeFor(facet);
		int previous = node.getOffset();
		while (previous > 0 && (source.charAt(previous - 1) == ' ' || source.charAt(previous - 1) == '\t')) previous--;
		boolean comments = false;
		for (ILeafNode leaf : node.getLeafNodes()) comments |= leaf.isHidden() && !leaf.getText().isBlank();
		if (!comments && previous < node.getOffset()) patches.add(new Patch(previous, node.getOffset() - previous, ""));
		for (ILeafNode leaf : node.getLeafNodes())
			if (leaf.getOffset() >= node.getOffset() && (!leaf.isHidden() || leaf.getText().isBlank()))
				patches.add(new Patch(leaf.getOffset(), leaf.getLength(), ""));
	}

	/** Hidden whitespace/comments are retained even inside a replaced expression. */
	private static void replaceTokens(final List<Patch> patches, final INode node, final String replacement) {
		ILeafNode start = null;
		ILeafNode end = null;
		boolean comments = false;
		for (var leaf : node.getLeafNodes()) {
			if (leaf.isHidden()) {
				if (start != null && !leaf.getText().isBlank()) comments = true;
				continue;
			}
			if (start == null) start = leaf;
			end = leaf;
		}
		if (start == null) return;
		if (!comments || start == end) {
			patches.add(new Patch(start.getOffset(), end.getEndOffset() - start.getOffset(), replacement));
			return;
		}
		boolean first = true;
		for (var leaf : node.getLeafNodes()) {
			if (leaf.isHidden()) continue;
			patches.add(new Patch(leaf.getOffset(), leaf.getLength(), first ? replacement : ""));
			first = false;
		}
	}
}
