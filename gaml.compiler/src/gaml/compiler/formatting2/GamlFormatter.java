/*******************************************************************************************************
 *
 * GamlFormatter.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.formatting2;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.formatting2.AbstractJavaFormatter;
import org.eclipse.xtext.formatting2.IFormattableDocument;
import org.eclipse.xtext.formatting2.regionaccess.ISemanticRegion;

import com.google.inject.Inject;

import gaml.compiler.gaml.Access;
import gaml.compiler.gaml.ArgumentDefinition;
import gaml.compiler.gaml.Array;
import gaml.compiler.gaml.BinaryOperator;
import gaml.compiler.gaml.Block;
import gaml.compiler.gaml.ExpressionList;
import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.Function;
import gaml.compiler.gaml.GamlPackage;
import gaml.compiler.gaml.If;
import gaml.compiler.gaml.Import;
import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.Parameter;
import gaml.compiler.gaml.Point;
import gaml.compiler.gaml.Pragma;
import gaml.compiler.gaml.S_Assignment;
import gaml.compiler.gaml.S_Definition;
import gaml.compiler.gaml.S_Display;
import gaml.compiler.gaml.S_Do;
import gaml.compiler.gaml.S_Equations;
import gaml.compiler.gaml.S_Experiment;
import gaml.compiler.gaml.S_If;
import gaml.compiler.gaml.S_Loop;
import gaml.compiler.gaml.S_Method;
import gaml.compiler.gaml.S_Reflex;
import gaml.compiler.gaml.S_Return;
import gaml.compiler.gaml.S_Solve;
import gaml.compiler.gaml.S_Species;
import gaml.compiler.gaml.S_Switch;
import gaml.compiler.gaml.S_Try;
import gaml.compiler.gaml.Statement;
import gaml.compiler.gaml.StandaloneBlock;
import gaml.compiler.gaml.StandaloneExpression;
import gaml.compiler.gaml.TypeInfo;
import gaml.compiler.gaml.TypeRef;
import gaml.compiler.gaml.Unary;
import gaml.compiler.gaml.Unit;
import gaml.compiler.services.GamlGrammarAccess;

/**
 * Formats GAML source code using the Xtext 2 formatter API ({@link AbstractJavaFormatter}).
 *
 * <p>
 * This formatter is a translation of the older {@code AbstractDeclarativeFormatter}-based formatter into the
 * {@code IFormattableDocument} idiom introduced by the new Xtext formatting API. It handles:
 * </p>
 * <ul>
 * <li>Operator spacing (binary operators, unary minus, type parameters, etc.)</li>
 * <li>Block indentation and line-wrapping for {@code {…}} constructs</li>
 * <li>Line-wrap rules around major declarations (species, experiment, global, reflex, action, equation)</li>
 * <li>Comment formatting (single-line and multi-line)</li>
 * <li>Punctuation spacing (commas, colons, dots, parentheses, brackets)</li>
 * </ul>
 *
 * <p>
 * Each {@code format()} overload receives a typed AST node and an {@link IFormattableDocument}, through which
 * spacing and indentation rules are applied at specific grammar regions. The class relies on
 * {@link GamlGrammarAccess} to resolve the concrete grammar keywords and assignments needed to target specific
 * token positions in the document.
 * </p>
 *
 * <h2>API mapping from old to new formatter</h2>
 * <table border="1">
 * <tr><th>Old API</th><th>New API</th></tr>
 * <tr><td>{@code c.setIndentationIncrement().after(k)}</td><td>{@code doc.append(region, it -> it.indent())}</td></tr>
 * <tr><td>{@code c.setIndentationDecrement().before(k)}</td><td>{@code doc.prepend(region, it -> it.unindent())}</td></tr>
 * <tr><td>{@code c.setNoLinewrap().before(k)}</td><td>{@code doc.prepend(region, it -> it.setNewLines(0))}</td></tr>
 * <tr><td>{@code c.setLinewrap(n).after(k)}</td><td>{@code doc.append(region, it -> it.setNewLines(n))}</td></tr>
 * <tr><td>{@code c.setSpace(" ").around(k)}</td><td>{@code doc.surround(region, it -> it.oneSpace())}</td></tr>
 * <tr><td>{@code c.setNoSpace().before(k)}</td><td>{@code doc.prepend(region, it -> it.noSpace())}</td></tr>
 * </table>
 */
public class GamlFormatter extends AbstractJavaFormatter {

	// ────────────────────────────────────────────────────────────────────────────────────────
	// Injected grammar access – needed to resolve grammar elements (keywords, rules, etc.)
	// ────────────────────────────────────────────────────────────────────────────────────────

	/**
	 * The grammar access singleton injected by the Guice container. Used to obtain references to specific
	 * grammar elements (keywords, assignments, etc.) needed to define region-based spacing rules.
	 */
	@Inject
	private GamlGrammarAccess ga;

	// ════════════════════════════════════════════════════════════════════════════════════════
	// ENTRY POINTS – standalone wrappers
	// ════════════════════════════════════════════════════════════════════════════════════════

	/**
	 * Formats a {@link StandaloneBlock} — a raw {@code { … }} block used outside a full model (e.g. in the
	 * UI console). Delegates all formatting to the inner {@link Block}.
	 *
	 * @param node
	 *            the standalone block AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final StandaloneBlock node, final IFormattableDocument doc) {
		doc.format(node.getBlock());
	}

	/**
	 * Formats a {@link StandaloneExpression} — a single expression used outside a full model (e.g. in the
	 * UI console). Delegates all formatting to the inner expression.
	 *
	 * @param node
	 *            the standalone expression AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final StandaloneExpression node, final IFormattableDocument doc) {
		doc.format(node.getExpr());
	}

	// ════════════════════════════════════════════════════════════════════════════════════════
	// MODEL & TOP-LEVEL
	// ════════════════════════════════════════════════════════════════════════════════════════

	/**
	 * Formats the top-level {@link Model} node.
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>Two blank lines after the {@code model} keyword / model name.</li>
	 * <li>One blank line after each {@code import} directive.</li>
	 * <li>Comments are formatted according to single- and multi-line comment rules.</li>
	 * <li>Recursively formats every pragma, every import and the model block.</li>
	 * </ul>
	 *
	 * @param model
	 *            the model AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final Model model, final IFormattableDocument doc) {
		// Two blank lines after the model name assignment
		final ISemanticRegion nameRegion =
				regionFor(model).assignment(ga.getModelAccess().getNameAssignment_2());
		if (nameRegion != null) { doc.append(nameRegion, it -> it.setNewLines(2)); }
		// Format imports: one blank line after each
		for (final Import imp : model.getImports()) {
			doc.format(imp);
			final ISemanticRegion impRegion =
					regionFor(imp).assignment(ga.getImportAccess().getImportURIAssignment_1());
			if (impRegion != null) { doc.append(impRegion, it -> it.setNewLines(1)); }
		}
		// Recursively format pragmas
		for (final Pragma p : model.getPragmas()) {
			doc.format(p);
		}
		// Recursively format the model block
		doc.format(model.getBlock());
		// Comment formatting – applied globally
		formatComments(model, doc);
	}

	/**
	 * Formats a {@link Pragma} annotation node (e.g. {@code @doc[…]}).
	 * The inner {@link ExpressionList} (plugin list) is formatted recursively if present.
	 *
	 * @param node
	 *            the pragma AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final Pragma node, final IFormattableDocument doc) {
		// No space between '@' and name — default Xtext behaviour covers this
		if (node.getPlugins() != null) { doc.format(node.getPlugins()); }
	}

	/**
	 * Formats an {@link Import} statement (e.g. {@code import "other.gaml" as alias}).
	 * No special layout beyond what is handled at the {@link Model} level.
	 *
	 * @param node
	 *            the import AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final Import node, final IFormattableDocument doc) {
		// Line-wrap after each import is handled at the Model level
	}

	// ════════════════════════════════════════════════════════════════════════════════════════
	// SPECIES / EXPERIMENT / GLOBAL – top-level sections
	// ════════════════════════════════════════════════════════════════════════════════════════

	/**
	 * Formats a {@link S_Species} declaration ({@code species}, {@code grid}, {@code class}, {@code skill}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>Two blank lines before the {@code species} keyword.</li>
	 * <li>Recursively formats each facet and the inner block.</li>
	 * </ul>
	 *
	 * @param node
	 *            the species statement AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final S_Species node, final IFormattableDocument doc) {
		doc.prepend(regionFor(node).assignment(ga.getS_SpeciesAccess().getKeyAssignment_0()),
				it -> it.setNewLines(2));
		for (final Facet f : node.getFacets()) {
			doc.format(f);
		}
		if (node.getBlock() != null) { formatBlock(node.getBlock(), doc); }
	}

	/**
	 * Formats an {@link S_Experiment} declaration (e.g. {@code experiment my_exp type: gui { … }}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>Two blank lines before the {@code experiment} keyword.</li>
	 * <li>Recursively formats each facet and the inner block.</li>
	 * </ul>
	 *
	 * @param node
	 *            the experiment statement AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final S_Experiment node, final IFormattableDocument doc) {
		doc.prepend(regionFor(node).assignment(ga.getS_ExperimentAccess().getKeyAssignment_0()),
				it -> it.setNewLines(2));
		for (final Facet f : node.getFacets()) {
			doc.format(f);
		}
		if (node.getBlock() != null) { formatBlock(node.getBlock(), doc); }
	}

	// ════════════════════════════════════════════════════════════════════════════════════════
	// REGULAR STATEMENTS
	// ════════════════════════════════════════════════════════════════════════════════════════

	/**
	 * Formats a generic {@link Statement} (fallback for any statement not otherwise specialised).
	 * Formats each facet and the inner block when present.
	 *
	 * @param node
	 *            the statement AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final Statement node, final IFormattableDocument doc) {
		for (final Facet f : node.getFacets()) {
			doc.format(f);
		}
		if (node.getBlock() != null) { formatBlock(node.getBlock(), doc); }
	}

	/**
	 * Formats an {@link S_Do} statement ({@code do} or {@code invoke}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>No space before the terminating semicolon.</li>
	 * <li>Recursively formats each facet.</li>
	 * </ul>
	 *
	 * @param node
	 *            the do statement AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final S_Do node, final IFormattableDocument doc) {
		doc.prepend(regionFor(node).keyword(ga.getS_DoAccess().getSemicolonKeyword_3()), it -> it.noSpace());
		for (final Facet f : node.getFacets()) {
			doc.format(f);
		}
	}

	/**
	 * Formats an {@link S_Loop} statement (e.g. {@code loop i from: 1 to: 5 { … }}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>Block is formatted with indentation and line-wrapping.</li>
	 * <li>Each facet is formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the loop statement AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final S_Loop node, final IFormattableDocument doc) {
		for (final Facet f : node.getFacets()) {
			doc.format(f);
		}
		if (node.getBlock() != null) { formatBlock(node.getBlock(), doc); }
	}

	/**
	 * Formats an {@link S_If} statement ({@code if (…) { … } else { … }}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>No line-wrap before the {@code else} keyword — it stays on the same line as the closing brace.</li>
	 * <li>The condition expression is formatted recursively.</li>
	 * <li>Both the if-block and the else clause (block or nested {@link S_If}) are formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the if statement AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final S_If node, final IFormattableDocument doc) {
		doc.format(node.getExpr());
		if (node.getBlock() != null) { formatBlock(node.getBlock(), doc); }
		if (node.getElse() != null) {
			// No line-wrap before 'else' — keep it on the same line as the closing brace
			doc.prepend(regionFor(node).keyword(ga.getS_IfAccess().getElseKeyword_3_0()),
					it -> it.setNewLines(0));
			doc.format(node.getElse());
		}
	}

	/**
	 * Formats an {@link S_Try} statement ({@code try { … } catch { … }}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>No line-wrap before {@code catch}.</li>
	 * <li>Both the try-block and the catch-block are formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the try statement AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final S_Try node, final IFormattableDocument doc) {
		if (node.getBlock() != null) { formatBlock(node.getBlock(), doc); }
		if (node.getCatch() != null) {
			doc.prepend(regionFor(node).keyword(ga.getS_TryAccess().getCatchKeyword_2_0()),
					it -> it.setNewLines(0));
			formatBlock(node.getCatch(), doc);
		}
	}

	/**
	 * Formats an {@link S_Switch} statement ({@code switch expr { match … }}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>The switch expression is formatted recursively.</li>
	 * <li>The match block is formatted as a regular block.</li>
	 * </ul>
	 *
	 * @param node
	 *            the switch statement AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final S_Switch node, final IFormattableDocument doc) {
		doc.format(node.getExpr());
		if (node.getBlock() != null) { formatBlock(node.getBlock(), doc); }
	}

	/**
	 * Formats an {@link S_Return} statement ({@code return expr;}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>No space before the semicolon.</li>
	 * <li>The return expression is formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the return statement AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final S_Return node, final IFormattableDocument doc) {
		doc.prepend(regionFor(node).keyword(ga.getS_ReturnAccess().getSemicolonKeyword_2()), it -> it.noSpace());
		if (node.getExpr() != null) { doc.format(node.getExpr()); }
	}

	/**
	 * Formats an {@link S_Definition} variable declaration (e.g. {@code int my_var <- 5;}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>No space before the terminating semicolon.</li>
	 * <li>The type reference and all facets are formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the variable definition statement AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final S_Definition node, final IFormattableDocument doc) {
		doc.prepend(regionFor(node).keyword(ga.getS_DefinitionAccess().getSemicolonKeyword_3()),
				it -> it.noSpace());
		if (node.getTkey() != null) { doc.format(node.getTkey()); }
		for (final Facet f : node.getFacets()) {
			doc.format(f);
		}
	}

	/**
	 * Formats an {@link S_Method} typed action definition (e.g. {@code int toto(int a) { … }}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>Two blank lines before the method declaration.</li>
	 * <li>No space before the opening parenthesis {@code (}.</li>
	 * <li>No space before the closing parenthesis {@code )}.</li>
	 * <li>Each argument definition and each facet are formatted recursively.</li>
	 * <li>The inner block is formatted with indentation.</li>
	 * </ul>
	 *
	 * @param node
	 *            the method definition AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final S_Method node, final IFormattableDocument doc) {
		doc.prepend(regionFor(node).assignment(ga.getS_MethodAccess().getTkeyAssignment_1()),
				it -> it.setNewLines(2));
		doc.prepend(regionFor(node).keyword(ga.getS_MethodAccess().getLeftParenthesisKeyword_3()),
				it -> it.noSpace());
		doc.prepend(regionFor(node).keyword(ga.getS_MethodAccess().getRightParenthesisKeyword_5()),
				it -> it.noSpace());
		if (node.getTkey() != null) { doc.format(node.getTkey()); }
		for (final ArgumentDefinition arg : node.getArgs()) {
			doc.format(arg);
		}
		for (final Facet f : node.getFacets()) {
			doc.format(f);
		}
		if (node.getBlock() != null) { formatBlock(node.getBlock(), doc); }
	}

	/**
	 * Formats an {@link S_Assignment} statement (e.g. {@code my_var <- 5;}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>No space before the terminating semicolon.</li>
	 * <li>The left-hand side expression, the right-hand side value and all facets are formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the assignment statement AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final S_Assignment node, final IFormattableDocument doc) {
		// No space before the trailing ';'
		for (final ISemanticRegion sc : allRegionsFor(node).keywords(";")) {
			doc.prepend(sc, it -> it.noSpace());
		}
		doc.format(node.getExpr());
		doc.format(node.getValue());
		for (final Facet f : node.getFacets()) {
			doc.format(f);
		}
	}

	/**
	 * Formats an {@link S_Equations} block (e.g. {@code equation eq1 { x = y + 5; }}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>Two blank lines before the {@code equation} keyword.</li>
	 * <li>The inner {@code {…}} braces are indented with the same rules as a regular block.</li>
	 * <li>Each facet is formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the equation block AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final S_Equations node, final IFormattableDocument doc) {
		doc.prepend(regionFor(node).keyword(ga.getS_EquationsAccess().getKeyEquationKeyword_0_0()),
				it -> it.setNewLines(2));
		for (final Facet f : node.getFacets()) {
			doc.format(f);
		}
		// Opening brace
		final ISemanticRegion open =
				regionFor(node).keyword(ga.getS_EquationsAccess().getLeftCurlyBracketKeyword_3_0_0());
		// Closing brace
		final ISemanticRegion close =
				regionFor(node).keyword(ga.getS_EquationsAccess().getRightCurlyBracketKeyword_3_0_2());
		if (open != null && close != null) {
			doc.append(open, it -> it.newLine());
			doc.interior(open, close, it -> it.indent());
			doc.prepend(close, it -> it.setNewLines(0, 0, 1));
			doc.append(close, it -> it.setNewLines(2));
		}
	}

	/**
	 * Formats an {@link S_Solve} statement (e.g. {@code solve eq1 method: "rk4";}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>Each facet is formatted recursively.</li>
	 * <li>The inner block (if present) is formatted with indentation.</li>
	 * </ul>
	 *
	 * @param node
	 *            the solve statement AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final S_Solve node, final IFormattableDocument doc) {
		for (final Facet f : node.getFacets()) {
			doc.format(f);
		}
		if (node.getBlock() != null) { formatBlock(node.getBlock(), doc); }
	}

	/**
	 * Formats an {@link S_Display} statement (e.g. {@code display my_disp type: java2D { … }}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>Each facet is formatted recursively.</li>
	 * <li>The display block is formatted with indentation and line-wrapping.</li>
	 * </ul>
	 *
	 * @param node
	 *            the display statement AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final S_Display node, final IFormattableDocument doc) {
		for (final Facet f : node.getFacets()) {
			doc.format(f);
		}
		if (node.getBlock() != null) { formatBlock(node.getBlock(), doc); }
	}

	/**
	 * Formats an {@link S_Reflex} declaration (e.g. {@code reflex move when: time > 10 { … }}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>Two blank lines before the reflex keyword.</li>
	 * <li>Each facet is formatted recursively.</li>
	 * <li>The inner block is formatted with indentation.</li>
	 * </ul>
	 *
	 * @param node
	 *            the reflex statement AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final S_Reflex node, final IFormattableDocument doc) {
		doc.prepend(regionFor(node).assignment(ga.getS_ReflexAccess().getKeyAssignment_0()),
				it -> it.setNewLines(2));
		for (final Facet f : node.getFacets()) {
			doc.format(f);
		}
		if (node.getBlock() != null) { formatBlock(node.getBlock(), doc); }
	}

	// ════════════════════════════════════════════════════════════════════════════════════════
	// BLOCKS
	// ════════════════════════════════════════════════════════════════════════════════════════

	/**
	 * Formats a {@link Block} node ({@code { statement* }}).
	 * Delegates to the shared {@link #formatBlock(Block, IFormattableDocument)} helper.
	 *
	 * @param block
	 *            the block AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final Block block, final IFormattableDocument doc) {
		formatBlock(block, doc);
	}

	/**
	 * Shared helper that applies block-level spacing rules to any {@link Block} node.
	 *
	 * <p>
	 * Specifically:
	 * </p>
	 * <ul>
	 * <li>A newline after the opening {@code {}.</li>
	 * <li>Indentation increased after the opening {@code {}.</li>
	 * <li>Zero-to-one newlines before the closing {@code }}.</li>
	 * <li>Indentation decreased before the closing {@code }}.</li>
	 * <li>Two newlines after the closing {@code }}.</li>
	 * <li>Each statement inside the block is recursively formatted.</li>
	 * </ul>
	 *
	 * @param block
	 *            the block to format; must not be {@code null}
	 * @param doc
	 *            the formattable document
	 */
	private void formatBlock(final Block block, final IFormattableDocument doc) {
		// Opening brace: newline after
		final ISemanticRegion openBrace =
				regionFor(block).keyword(ga.getBlockAccess().getLeftCurlyBracketKeyword_1());
		// Closing brace
		final ISemanticRegion closeBrace =
				regionFor(block).keyword(ga.getBlockAccess().getRightCurlyBracketKeyword_2_1());
		if (openBrace != null && closeBrace != null) {
			doc.append(openBrace, it -> it.newLine());
			// Indent the interior between braces
			doc.interior(openBrace, closeBrace, it -> it.indent());
			doc.prepend(closeBrace, it -> it.setNewLines(0, 0, 1));
			doc.append(closeBrace, it -> it.setNewLines(2));
		}
		// Recursively format all statements
		for (final Statement s : block.getStatements()) {
			doc.format(s);
		}
	}

	// ════════════════════════════════════════════════════════════════════════════════════════
	// FACETS AND ARGUMENTS
	// ════════════════════════════════════════════════════════════════════════════════════════

	/**
	 * Formats a {@link Facet} (e.g. {@code color: #red} or {@code <- 5}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>The facet expression is formatted recursively.</li>
	 * <li>An inner block (e.g. action facet) is formatted with indentation.</li>
	 * </ul>
	 *
	 * @param facet
	 *            the facet AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final Facet facet, final IFormattableDocument doc) {
		if (facet.getExpr() != null) { doc.format(facet.getExpr()); }
		if (facet.getBlock() != null) { formatBlock(facet.getBlock(), doc); }
	}

	/**
	 * Formats an {@link ArgumentDefinition} (e.g. {@code int my_arg <- 5}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>One space around the {@code <-} default-value operator.</li>
	 * <li>The type reference and the default expression are formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the argument definition AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final ArgumentDefinition node, final IFormattableDocument doc) {
		if (node.getType() != null) { doc.format(node.getType()); }
		if (node.getDefault() != null) {
			// Space around '<-' default operator
			final ISemanticRegion arrow = regionFor(node)
					.keyword(ga.getArgumentDefinitionAccess().getLessThanSignHyphenMinusKeyword_2_0());
			if (arrow != null) { doc.surround(arrow, it -> it.oneSpace()); }
			doc.format(node.getDefault());
		}
	}

	// ════════════════════════════════════════════════════════════════════════════════════════
	// EXPRESSIONS
	// ════════════════════════════════════════════════════════════════════════════════════════

	/**
	 * Formats a {@link BinaryOperator} expression (e.g. {@code a + b}, {@code a or b}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>One space before and after every operator token found in the node's text region.</li>
	 * <li>The left and right sub-expressions are formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the binary operator expression AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final BinaryOperator node, final IFormattableDocument doc) {
		final ISemanticRegion opRegion =
				regionFor(node).feature(GamlPackage.Literals.BINARY_OPERATOR__OP);
		if (opRegion != null) { doc.surround(opRegion, it -> it.oneSpace()); }
		doc.format(node.getLeft());
		doc.format(node.getRight());
	}

	/**
	 * Formats an {@link If} ternary expression ({@code cond ? then : else}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>One space before and after the {@code ?} operator.</li>
	 * <li>One space before the {@code :} separator.</li>
	 * <li>Left, right and ifFalse sub-expressions are formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the ternary-if expression AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final If node, final IFormattableDocument doc) {
		final ISemanticRegion qm =
				regionFor(node).feature(GamlPackage.Literals.IF__OP);
		if (qm != null) { doc.surround(qm, it -> it.oneSpace()); }
		final ISemanticRegion colon =
				regionFor(node).keyword(ga.getIfAccess().getColonKeyword_1_3_0());
		if (colon != null) { doc.prepend(colon, it -> it.oneSpace()); }
		doc.format(node.getLeft());
		doc.format(node.getRight());
		if (node.getIfFalse() != null) { doc.format(node.getIfFalse()); }
	}

	/**
	 * Formats a {@link Unit} expression ({@code value #unit}, e.g. {@code 5 #m}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>No space before the {@code #} operator.</li>
	 * <li>The left sub-expression is formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the unit expression AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final Unit node, final IFormattableDocument doc) {
		final ISemanticRegion hash =
				regionFor(node).feature(GamlPackage.Literals.UNIT__OP);
		if (hash != null) { doc.prepend(hash, it -> it.noSpace()); }
		doc.format(node.getLeft());
	}

	/**
	 * Formats a {@link Unary} expression ({@code -expr}, {@code !expr}, {@code not expr}, {@code #unit}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>No space after the symbolic unary operators {@code -}, {@code !} and {@code #}.</li>
	 * <li>One space after the word operator {@code not} (e.g. {@code not condition}).</li>
	 * <li>The inner (right) expression is formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the unary expression AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final Unary node, final IFormattableDocument doc) {
		// '-' and '!' : no space after
		final ISemanticRegion minus =
				regionFor(node).keyword(ga.getUnaryAccess().getOpHyphenMinusKeyword_1_1_1_0_0_0());
		if (minus != null) { doc.append(minus, it -> it.noSpace()); }
		final ISemanticRegion excl =
				regionFor(node).keyword(ga.getUnaryAccess().getOpExclamationMarkKeyword_1_1_1_0_0_1());
		if (excl != null) { doc.append(excl, it -> it.noSpace()); }
		// '#' (unit reference) : no space after
		final ISemanticRegion hash =
				regionFor(node).keyword(ga.getUnaryAccess().getOpNumberSignKeyword_1_1_0_0_0());
		if (hash != null) { doc.append(hash, it -> it.noSpace()); }
		// 'not' : one space after (word operator)
		final ISemanticRegion not =
				regionFor(node).keyword(ga.getUnaryAccess().getOpNotKeyword_1_1_1_0_0_2());
		if (not != null) { doc.append(not, it -> it.oneSpace()); }
		if (node.getRight() != null) { doc.format(node.getRight()); }
	}

	/**
	 * Formats an {@link Access} expression ({@code expr[index]} or {@code expr.field}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>No space before the {@code [} or {@code .} operator.</li>
	 * <li>No space before the closing {@code ]}.</li>
	 * <li>The left and right sub-expressions are formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the access expression AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final Access node, final IFormattableDocument doc) {
		// op is '[' or '.'
		final ISemanticRegion op =
				regionFor(node).feature(GamlPackage.Literals.ACCESS__OP);
		if (op != null) { doc.prepend(op, it -> it.noSpace()); }
		// closing ']'
		final ISemanticRegion closeBracket =
				regionFor(node).keyword(ga.getAccessAccess().getRightSquareBracketKeyword_1_1_0_2());
		if (closeBracket != null) { doc.prepend(closeBracket, it -> it.noSpace()); }
		doc.format(node.getLeft());
		if (node.getRight() != null) { doc.format(node.getRight()); }
	}

	/**
	 * Formats an {@link Array} literal ({@code [expr, expr, …]}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>No space after {@code [}.</li>
	 * <li>No space before {@code ]}.</li>
	 * <li>The expression list is formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the array literal AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final Array node, final IFormattableDocument doc) {
		final ISemanticRegion open =
				regionFor(node).keyword(ga.getPrimaryAccess().getLeftSquareBracketKeyword_3_0());
		if (open != null) { doc.append(open, it -> it.noSpace()); }
		final ISemanticRegion close =
				regionFor(node).keyword(ga.getPrimaryAccess().getRightSquareBracketKeyword_3_3());
		if (close != null) { doc.prepend(close, it -> it.noSpace()); }
		if (node.getExprs() != null) { doc.format(node.getExprs()); }
	}

	/**
	 * Formats a {@link Point} literal ({@code {x, y}} or {@code {x, y, z}}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>No space after the opening {@code {}.</li>
	 * <li>No space before the closing {@code }}.</li>
	 * <li>The coordinate expressions are formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the point literal AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final Point node, final IFormattableDocument doc) {
		final ISemanticRegion open =
				regionFor(node).keyword(ga.getPrimaryAccess().getLeftCurlyBracketKeyword_4_0());
		if (open != null) { doc.append(open, it -> it.noSpace()); }
		final ISemanticRegion close =
				regionFor(node).keyword(ga.getPrimaryAccess().getRightCurlyBracketKeyword_4_6());
		if (close != null) { doc.prepend(close, it -> it.noSpace()); }
		doc.format(node.getLeft());
		doc.format(node.getRight());
		if (node.getZ() != null) { doc.format(node.getZ()); }
	}

	/**
	 * Formats a {@link Function} call expression (e.g. {@code my_action(arg1, arg2)}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>No space before the opening {@code (}.</li>
	 * <li>No space before the closing {@code )}.</li>
	 * <li>The argument list (right) and the optional type info are formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the function call expression AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final Function node, final IFormattableDocument doc) {
		final ISemanticRegion lp =
				regionFor(node).keyword(ga.getFunctionAccess().getLeftParenthesisKeyword_3());
		if (lp != null) { doc.prepend(lp, it -> it.noSpace()); }
		final ISemanticRegion rp =
				regionFor(node).keyword(ga.getFunctionAccess().getRightParenthesisKeyword_5());
		if (rp != null) { doc.prepend(rp, it -> it.noSpace()); }
		if (node.getRight() != null) { doc.format(node.getRight()); }
		if (node.getType() != null) { doc.format(node.getType()); }
	}

	/**
	 * Formats an {@link ExpressionList} (comma-separated list of expressions or parameters).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>No space before each {@code ,}.</li>
	 * <li>One space after each {@code ,}.</li>
	 * <li>Each contained expression / parameter is formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the expression list AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final ExpressionList node, final IFormattableDocument doc) {
		for (final ISemanticRegion comma : allRegionsFor(node).keywords(",")) {
			doc.prepend(comma, it -> it.noSpace());
			doc.append(comma, it -> it.oneSpace());
		}
		for (final var expr : node.getExprs()) {
			doc.format(expr);
		}
	}

	/**
	 * Formats a named {@link Parameter} (e.g. {@code name: "test"} inside an action call).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>No space before the {@code :} separator.</li>
	 * <li>One space after the {@code :} separator.</li>
	 * <li>The right-hand expression is formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the parameter AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final Parameter node, final IFormattableDocument doc) {
		final ISemanticRegion colon =
				regionFor(node).keyword(ga.getParameterAccess().getColonKeyword_1_1_1());
		if (colon != null) {
			doc.prepend(colon, it -> it.noSpace());
			doc.append(colon, it -> it.oneSpace());
		}
		if (node.getRight() != null) { doc.format(node.getRight()); }
	}

	/**
	 * Formats a {@link TypeRef} (e.g. {@code int}, {@code list<string>}).
	 * If a {@link TypeInfo} type-parameter clause is present, it is formatted recursively.
	 *
	 * @param node
	 *            the type reference AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final TypeRef node, final IFormattableDocument doc) {
		if (node.getParameter() != null) { doc.format(node.getParameter()); }
	}

	/**
	 * Formats a {@link TypeInfo} type-parameter clause ({@code <int>} or {@code <int, string>}).
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>No space before the opening {@code <}.</li>
	 * <li>No space after the opening {@code <}.</li>
	 * <li>No space before the closing {@code >}.</li>
	 * <li>The contained type references are formatted recursively.</li>
	 * </ul>
	 *
	 * @param node
	 *            the type information AST node
	 * @param doc
	 *            the formattable document
	 */
	protected void format(final TypeInfo node, final IFormattableDocument doc) {
		final ISemanticRegion lt =
				regionFor(node).keyword(ga.getTypeInfoAccess().getLessThanSignKeyword_0());
		if (lt != null) {
			doc.prepend(lt, it -> it.noSpace());
			doc.append(lt, it -> it.noSpace());
		}
		final ISemanticRegion gt =
				regionFor(node).keyword(ga.getTypeInfoAccess().getGreaterThanSignKeyword_3());
		if (gt != null) { doc.prepend(gt, it -> it.noSpace()); }
		if (node.getFirst() != null) { doc.format(node.getFirst()); }
		if (node.getSecond() != null) { doc.format(node.getSecond()); }
	}

	// ════════════════════════════════════════════════════════════════════════════════════════
	// COMMENT HANDLING (applied globally from the model root)
	// ════════════════════════════════════════════════════════════════════════════════════════

	/**
	 * Applies comment-formatting rules to the whole document starting from the given root.
	 *
	 * <p>
	 * Rules applied:
	 * </p>
	 * <ul>
	 * <li>0–1–2 newlines before single-line ({@code //}) comments.</li>
	 * <li>0–1–2 newlines before multi-line ({@code /* … *\/}) comments.</li>
	 * <li>0–1 newlines after multi-line comments.</li>
	 * </ul>
	 *
	 * @param root
	 *            the root AST node from which hidden-region iteration starts
	 * @param doc
	 *            the formattable document on which comment rules are set
	 */
	private void formatComments(final EObject root, final IFormattableDocument doc) {
		for (final ISemanticRegion region : allRegionsFor(root).keywords("//")) {
			doc.prepend(region, it -> it.setNewLines(0, 1, 2));
		}
		for (final ISemanticRegion region : allRegionsFor(root).keywords("/*")) {
			doc.prepend(region, it -> it.setNewLines(0, 1, 2));
			doc.append(region, it -> it.setNewLines(0, 1, 1));
		}
	}
}
