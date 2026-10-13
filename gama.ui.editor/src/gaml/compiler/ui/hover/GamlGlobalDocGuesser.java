/*******************************************************************************************************
 *
 * GamlGlobalDocGuesser.java, in gama.ui.editor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.ui.hover;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import gama.annotations.constants.IKeyword;
import gama.api.additions.registries.ArtefactRegistry;
import gama.api.additions.registries.GamaSkillRegistry;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.GamlIdiomsProvider;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Signature;
import gama.api.gaml.types.Types;
import gama.api.kernel.GamaMetaModel;
import gaml.compiler.EGaml;
import gaml.compiler.gaml.BooleanLiteral;
import gaml.compiler.gaml.DoubleLiteral;
import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.IntLiteral;
import gaml.compiler.gaml.Statement;
import gaml.compiler.gaml.StringLiteral;
import gaml.compiler.gaml.UnitName;
import gaml.compiler.ui.hover.GamlHoverDocumentationProvider.Result;

/**
 * The Class GamlGlobalDocGuesser.
 *
 * <p>
 * When a GAML model contains syntax or semantic errors and cannot compile, the compiler cannot produce contextualized
 * documentation for hovered elements, typically resulting in unhelpful "Unknown object of type xxx" messages.
 * This class applies syntactic and textual heuristics to deduce which global GAML reference documentation to show.
 * </p>
 *
 * <h2>Heuristics applied:</h2>
 * <ol>
 * <li><b>Literals:</b> integer, float, boolean, string, nil, units and constants (e.g. #m, #sec, #red).</li>
 * <li><b>Facets:</b> tokens ending with ':' or followed by ':', matching against the enclosing statement or globally.</li>
 * <li><b>Parentheses:</b> inside argument lists, identifying action parameters or operators.</li>
 * <li><b>Sentence starters:</b> keywords at the beginning of a line or statement are identified as statements.</li>
 * <li><b>Types:</b> built-in and declared primitive/complex types (int, float, string, point, list, etc.).</li>
 * <li><b>Operators:</b> arithmetic, geometric, spatial, and collection operators.</li>
 * <li><b>Built-in concepts:</b> built-in species (agent, world), skills, and their attributes/actions.</li>
 * <li><b>Global GAML idioms:</b> broad search across all indexed language constructs.</li>
 * </ol>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since 2025-03
 */
public class GamlGlobalDocGuesser {

	/**
	 * Wrapper for decontextualized documentation, appending a banner indicating that this is global reference doc.
	 */
	public record DecontextualizedDoc(IGamlDescription delegate, String hint) implements IGamlDescription {

		@Override
		public String getTitle() {
			return delegate == null ? hint : delegate.getTitle();
		}

		@Override
		public IGamlDocumentation getDocumentation() {
			if (delegate == null) return new GamlConstantDocumentation("");
			IGamlDocumentation orig = delegate.getDocumentation();
			String docStr = orig != null ? orig.toString() : "";
			String banner = "<div style=\"background-color: #f0f4f8; color: #496989; padding: 4px 8px; border-left: 3px solid #3498db; margin-bottom: 8px; font-size: 0.9em;\">"
					+ "<i>GAML Reference (uncompiled model) &mdash; " + hint + "</i></div>";
			return new GamlConstantDocumentation(banner + docStr);
		}

		@Override
		public String getName() {
			return delegate == null ? "" : delegate.getName();
		}
	}

	/**
	 * Textual and syntactic context gathered around the hovered position.
	 */
	public static class TextualContext {

		/** The extracted word. */
		public final String word;

		/** Whether the token has a trailing colon or is immediately followed by ':'. */
		public final boolean hasTrailingColon;

		/** Whether the token begins a sentence / statement (start of line or after ';', '{', '}'). */
		public final boolean beginsSentence;

		/** Whether the token is inside parentheses. */
		public final boolean isInsideParentheses;

		/** The keyword of the enclosing statement on this line, if any. */
		public final String enclosingKeyword;

		/** The function/action name preceding the opening parenthesis, if inside parentheses. */
		public final String enclosingFunction;

		/** Text before the word in the statement. */
		public final String textBeforeInStmt;

		/**
		 * Instantiates a new textual context.
		 */
		public TextualContext(final String word, final boolean hasTrailingColon, final boolean beginsSentence,
				final boolean isInsideParentheses, final String enclosingKeyword, final String enclosingFunction,
				final String textBeforeInStmt) {
			this.word = word;
			this.hasTrailingColon = hasTrailingColon;
			this.beginsSentence = beginsSentence;
			this.isInsideParentheses = isInsideParentheses;
			this.enclosingKeyword = enclosingKeyword;
			this.enclosingFunction = enclosingFunction;
			this.textBeforeInStmt = textBeforeInStmt;
		}
	}

	/**
	 * Guesses the documentation for an EObject and its textual context when contextual compilation documentation
	 * is unavailable.
	 *
	 * @param o
	 *            the AST or grammar EObject at the hover location (may be null or uncompiled)
	 * @param region
	 *            the hover region
	 * @param document
	 *            the editor document
	 * @return the guessed IGamlDescription, or null if no plausible documentation could be found
	 */
	public IGamlDescription guessDocumentation(final EObject o, final IRegion region, final IDocument document) {
		TextualContext ctx = analyzeContext(o, region, document);
		if (ctx == null || ctx.word == null || ctx.word.isEmpty()) return null;

		IGamlDescription doc;

		// 1. Literal heuristics
		doc = guessFromLiterals(o, ctx);
		if (doc != null) return doc;

		// 2. Facet heuristic (ends with ':' or is a Facet)
		if (ctx.hasTrailingColon || o instanceof Facet) {
			doc = guessFromFacet(o, ctx);
			if (doc != null) return doc;
		}

		// 3. Parentheses context (function/action arguments or calls)
		if (ctx.isInsideParentheses) {
			doc = guessFromParentheses(o, ctx);
			if (doc != null) return doc;
		}

		// 4. Statement heuristic: if the word begins a sentence / statement
		if (ctx.beginsSentence || o instanceof Statement) {
			doc = guessFromStatement(o, ctx);
			if (doc != null) return doc;
		}

		// 5. Types
		doc = guessFromType(ctx);
		if (doc != null) return doc;

		// 6. Statement heuristic (even if not strictly at beginning of line)
		doc = guessFromStatement(o, ctx);
		if (doc != null) return doc;

		// 7. Operators
		doc = guessFromOperator(ctx);
		if (doc != null) return doc;

		// 8. Built-in species, skills, attributes, actions
		doc = guessFromBuiltInConcepts(ctx);
		if (doc != null) return doc;

		// 9. Variables and declarations (search document for type/declaration)
		doc = guessFromVariableOrDeclaration(o, ctx, document);
		if (doc != null) return doc;

		// 10. Incomplete keyword or statement prefix
		doc = guessFromPrefix(ctx);
		if (doc != null) return doc;

		// 11. Global reference search via idioms providers
		doc = guessFromIdiomsProviders(ctx);
		if (doc != null) return doc;

		return null;
	}

	/**
	 * Analyzes the textual context around the hovered region.
	 */
	public TextualContext analyzeContext(final EObject o, final IRegion region, final IDocument document) {
		String word = null;
		int offset = region != null ? region.getOffset() : -1;

		if (document != null && offset >= 0) {
			word = extractWordAt(document, offset);
		}

		if (word == null || word.isEmpty()) {
			if (o instanceof Keyword kw) {
				word = kw.getValue();
			} else if (o != null) {
				INode node = NodeModelUtils.findActualNodeFor(o);
				if (node != null) {
					word = node.getText();
				} else {
					word = EGaml.getInstance().getKeyOf(o);
					if (word == null) { word = EGaml.getInstance().getNameOf(o); }
				}
			}
		}

		if (word != null) { word = word.trim(); }
		if (word == null || word.isEmpty()) return null;

		boolean hasColon = word.endsWith(":");
		String cleanWord = hasColon ? word.substring(0, word.length() - 1) : word;
		boolean begins = false;
		boolean insideParens = false;
		String encKw = null;
		String encFunc = null;
		String textBeforeInStmt = null;

		if (document != null && offset >= 0) {
			try {
				int lineNum = document.getLineOfOffset(offset);
				IRegion lineInfo = document.getLineInformation(lineNum);
				String line = document.get(lineInfo.getOffset(), lineInfo.getLength());
				int col = Math.max(0, Math.min(offset - lineInfo.getOffset(), line.length()));

				// Check if followed by colon
				if (!hasColon) {
					int afterWord = col;
					while (afterWord < line.length() && isWordChar(line.charAt(afterWord))) afterWord++;
					while (afterWord < line.length() && Character.isWhitespace(line.charAt(afterWord))) afterWord++;
					if (afterWord < line.length() && line.charAt(afterWord) == ':') {
						if (afterWord + 1 >= line.length() || line.charAt(afterWord + 1) != ':') {
							hasColon = true;
						}
					}
				}

				// Find start of statement on this line (after last ';', '{', or '}')
				int stmtStart = 0;
				for (int i = 0; i < col; i++) {
					char ch = line.charAt(i);
					if (ch == ';' || ch == '{' || ch == '}') { stmtStart = i + 1; }
				}
				textBeforeInStmt = line.substring(stmtStart, col).trim();

				// Begins statement if nothing preceding except optional comments
				begins = textBeforeInStmt.isEmpty() || textBeforeInStmt.startsWith("//");

				// Check if inside parentheses: count unclosed '('
				int openParens = 0;
				int lastOpenParenIdx = -1;
				for (int i = stmtStart; i < col; i++) {
					char ch = line.charAt(i);
					if (ch == '(') {
						openParens++;
						lastOpenParenIdx = i;
					} else if (ch == ')') {
						openParens = Math.max(0, openParens - 1);
					}
				}
				insideParens = openParens > 0;

				// If inside parens, what was the function name before '('?
				if (insideParens && lastOpenParenIdx > 0) {
					int k = lastOpenParenIdx - 1;
					while (k >= 0 && Character.isWhitespace(line.charAt(k))) k--;
					int funcEnd = k + 1;
					while (k >= 0 && isWordChar(line.charAt(k))) k--;
					int funcStart = k + 1;
					if (funcEnd > funcStart) { encFunc = line.substring(funcStart, funcEnd); }
				}

				// Enclosing statement keyword (first word in statement on this line)
				if (!textBeforeInStmt.isEmpty()) {
					int k = 0;
					while (k < textBeforeInStmt.length() && isWordChar(textBeforeInStmt.charAt(k))) k++;
					if (k > 0) { encKw = textBeforeInStmt.substring(0, k); }
				}

				// If enclosing keyword not found on current line, scan previous lines for multi-line statements
				if (encKw == null && lineNum > 0) {
					for (int pLine = lineNum - 1; pLine >= Math.max(0, lineNum - 20); pLine--) {
						IRegion prevLineInfo = document.getLineInformation(pLine);
						String prevLine = document.get(prevLineInfo.getOffset(), prevLineInfo.getLength()).trim();
						if (prevLine.isEmpty() || prevLine.startsWith("//")) continue;
						int k = 0;
						while (k < prevLine.length() && isWordChar(prevLine.charAt(k))) k++;
						if (k > 0) {
							String candKw = prevLine.substring(0, k);
							if (ArtefactRegistry.isStatementArtefact(candKw) || GAML.isAStatement(candKw)) {
								encKw = candKw;
								break;
							}
						}
						if (prevLine.endsWith(";") || prevLine.endsWith("{") || prevLine.endsWith("}")) break;
					}
				}
			} catch (BadLocationException e) {
				// ignore
			}
		}

		if (encKw == null && o != null) {
			EObject cont = o instanceof Statement ? o : EGaml.getInstance().getSurroundingStatement(o);
			if (cont != null) { encKw = EGaml.getInstance().getKeyOf(cont); }
		}

		return new TextualContext(cleanWord, hasColon, begins, insideParens, encKw, encFunc, textBeforeInStmt);
	}

	/**
	 * Guess documentation for literal values and constants.
	 */
	protected IGamlDescription guessFromLiterals(final EObject o, final TextualContext ctx) {
		String w = ctx.word;
		if (o instanceof IntLiteral || w.matches("-?\\d+")) {
			IType<?> t = Types.get(IKeyword.INT);
			return new DecontextualizedDoc(t != null ? t : new Result("Integer literal", "Whole number value of type int."), "integer literal");
		}
		if (o instanceof DoubleLiteral || w.matches("-?\\d+\\.\\d+([eE][+-]?\\d+)?")) {
			IType<?> t = Types.get(IKeyword.FLOAT);
			return new DecontextualizedDoc(t != null ? t : new Result("Float literal", "Floating-point numeric value of type float."), "float literal");
		}
		if (o instanceof BooleanLiteral || IKeyword.TRUE.equals(w) || IKeyword.FALSE.equals(w)) {
			IType<?> t = Types.get(IKeyword.BOOL);
			return new DecontextualizedDoc(t != null ? t : new Result("Boolean literal", "Boolean literal (true or false)."), "boolean literal");
		}
		if (o instanceof StringLiteral || (w.startsWith("\"") && w.endsWith("\"")) || (w.startsWith("'") && w.endsWith("'"))) {
			IType<?> t = Types.get(IKeyword.STRING);
			return new DecontextualizedDoc(t != null ? t : new Result("String literal", "Text string enclosed in quotes."), "string literal");
		}
		if ("nil".equals(w)) {
			return new DecontextualizedDoc(new Result("Literal nil", "The constant <b>nil</b> represents an undefined value or null reference in GAML."), "literal");
		}
		if (o instanceof UnitName || w.startsWith("#") || GAML.getUnits().containsKey(w.startsWith("#") ? w.substring(1) : w)) {
			String unitKey = w.startsWith("#") ? w.substring(1) : w;
			IGamlDescription unit = GAML.getUnit(unitKey);
			if (unit != null) return new DecontextualizedDoc(unit, "unit / constant");
		}
		return null;
	}

	/**
	 * Guess documentation for facets.
	 */
	protected IGamlDescription guessFromFacet(final EObject o, final TextualContext ctx) {
		String facetName = ctx.word.endsWith(":") ? ctx.word.substring(0, ctx.word.length() - 1) : ctx.word;

		// Try enclosing statement first
		String stmtKey = ctx.enclosingKeyword;
		if (stmtKey == null && o != null) {
			EObject cont = o instanceof Facet ? o.eContainer() : EGaml.getInstance().getSurroundingStatement(o);
			if (cont != null) { stmtKey = EGaml.getInstance().getKeyOf(cont); }
		}

		if (stmtKey != null) {
			IArtefact.Symbol stmtProto = ArtefactRegistry.getArtefact(stmtKey, null);
			if (stmtProto != null && stmtProto.getPossibleFacets().containsKey(facetName)) {
				return new DecontextualizedDoc(stmtProto.getPossibleFacets().get(facetName), "facet of statement " + stmtKey);
			}
		}

		// Search across all statement facets
		for (IArtefact.Facet f : ArtefactRegistry.getFacetsArtefacts()) {
			if (f.getName().equals(facetName)) {
				return new DecontextualizedDoc(f, "facet " + f.getName());
			}
		}

		return null;
	}

	/**
	 * Guess documentation when inside parentheses (arguments, action parameters, operators).
	 */
	protected IGamlDescription guessFromParentheses(final EObject o, final TextualContext ctx) {
		String name = ctx.word.endsWith(":") ? ctx.word.substring(0, ctx.word.length() - 1) : ctx.word;

		if (ctx.hasTrailingColon) {
			// Argument to function / action
			if (ctx.enclosingFunction != null) {
				for (IActionDescription ad : GamaSkillRegistry.INSTANCE.getRegisteredSkillsActions()) {
					if (ad.getName().equals(ctx.enclosingFunction) && ad.containsArg(name)) {
						if (ad.getFormalArgs() != null) {
							for (IDescription arg : ad.getFormalArgs()) {
								if (name.equals(arg.getName())) {
									return new DecontextualizedDoc(arg, "argument " + name + ": of action " + ctx.enclosingFunction);
								}
							}
						}
						return new DecontextualizedDoc(ad, "argument " + name + ": of action " + ctx.enclosingFunction);
					}
				}
			}
			// Fallback to facet lookup
			return guessFromFacet(o, ctx);
		}

		// Inside parentheses but not a named facet: could be operator or type
		if (GAML.getOperatorsNames().contains(name)) {
			Map<Signature, IArtefact.Operator> ops = GAML.getOperatorsNamed(name);
			if (ops != null && !ops.isEmpty()) {
				return new DecontextualizedDoc(ops.values().iterator().next(), "operator");
			}
		}
		if (Types.hasType(name)) {
			return new DecontextualizedDoc(Types.get(name), "type");
		}

		return null;
	}

	/**
	 * Guess documentation for statements.
	 */
	protected IGamlDescription guessFromStatement(final EObject o, final TextualContext ctx) {
		String w = ctx.word.endsWith(":") ? ctx.word.substring(0, ctx.word.length() - 1) : ctx.word;

		if (ArtefactRegistry.isStatementArtefact(w)) {
			IArtefact.Symbol stmt = ArtefactRegistry.getStatementArtefact(w);
			if (stmt != null) return new DecontextualizedDoc(stmt, "statement");
		}
		if (GAML.isAStatement(w)) {
			IArtefact.Symbol stmt = ArtefactRegistry.getStatementArtefact(w);
			if (stmt != null) return new DecontextualizedDoc(stmt, "statement");
		}
		if (o instanceof Statement stmt) {
			String key = EGaml.getInstance().getKeyOf(stmt);
			if (key != null && ArtefactRegistry.isStatementArtefact(key)) {
				IArtefact.Symbol proto = ArtefactRegistry.getStatementArtefact(key);
				if (proto != null) return new DecontextualizedDoc(proto, "statement");
			}
		}
		return null;
	}

	/**
	 * Guess documentation for types.
	 */
	protected IGamlDescription guessFromType(final TextualContext ctx) {
		String w = ctx.word.endsWith(":") ? ctx.word.substring(0, ctx.word.length() - 1) : ctx.word;
		if (Types.hasType(w)) {
			IType<?> t = Types.get(w);
			if (t != null) return new DecontextualizedDoc(t, "type");
		}
		return null;
	}

	/**
	 * Guess documentation for operators.
	 */
	protected IGamlDescription guessFromOperator(final TextualContext ctx) {
		String w = ctx.word;
		if (GAML.getOperatorsNames().contains(w)) {
			Map<Signature, IArtefact.Operator> ops = GAML.getOperatorsNamed(w);
			if (ops != null && !ops.isEmpty()) {
				return new DecontextualizedDoc(ops.values().iterator().next(), "operator");
			}
		}
		// Common operators that might not be in getOperatorsNames directly
		if ("<-".equals(w)) {
			return new DecontextualizedDoc(new Result("Assignment operator &lt;-",
					"Assigns the value of the right-hand expression to the variable or attribute on the left."), "operator");
		}
		if ("::".equals(w)) {
			return new DecontextualizedDoc(new Result("Pair operator ::",
					"Creates a pair (key::value) used for map construction, action arguments, or facet pairs."), "operator");
		}
		return null;
	}

	/**
	 * Guess documentation for built-in species, skills, attributes, and actions.
	 */
	protected IGamlDescription guessFromBuiltInConcepts(final TextualContext ctx) {
		String w = ctx.word.endsWith(":") ? ctx.word.substring(0, ctx.word.length() - 1) : ctx.word;

		// Built-in species
		ISpeciesDescription spec = GamaMetaModel.getSpeciesDescription(w);
		if (spec != null) return new DecontextualizedDoc(spec, "built-in species");

		// Skills
		ISkillDescription skill = GamaSkillRegistry.INSTANCE.get(w);
		if (skill != null) return new DecontextualizedDoc(skill, "skill");

		// Skill attributes
		for (IVariableDescription vd : GamaSkillRegistry.INSTANCE.getRegisteredSkillsAttributes()) {
			if (vd.getName().equals(w)) return new DecontextualizedDoc(vd, "skill attribute");
		}

		// Species attributes
		for (ISpeciesDescription sd : GamaMetaModel.getAllSpeciesDescriptions()) {
			IVariableDescription vd = sd.getOwnAttributes().get(w);
			if (vd != null) return new DecontextualizedDoc(vd, "built-in species attribute");
		}

		// Skill actions
		for (IActionDescription ad : GamaSkillRegistry.INSTANCE.getRegisteredSkillsActions()) {
			if (ad.getName().equals(w)) return new DecontextualizedDoc(ad, "skill action");
		}

		// Species actions
		for (ISpeciesDescription sd : GamaMetaModel.getAllSpeciesDescriptions()) {
			IActionDescription ad = sd.getOwnActions().get(w);
			if (ad != null) return new DecontextualizedDoc(ad, "built-in species action");
		}

		return null;
	}

	/**
	 * Guess documentation for variables and declarations by searching the document text.
	 */
	protected IGamlDescription guessFromVariableOrDeclaration(final EObject o, final TextualContext ctx,
			final IDocument document) {
		String w = ctx.word.endsWith(":") ? ctx.word.substring(0, ctx.word.length() - 1) : ctx.word;
		if (w.isEmpty() || !isWordChar(w.charAt(0)) || Character.isDigit(w.charAt(0))) return null;

		boolean isParamVar = ctx.textBeforeInStmt != null
				&& (ctx.textBeforeInStmt.matches(".*\\bvar:\\s*$") || ctx.textBeforeInStmt.matches(".*\\bvar\\s*:\\s*$"));

		if (document != null) {
			String text = document.get();
			// Look for variable declaration: e.g. "float infection_distance" or "int infection_distance <- 0"
			Pattern varDecl = Pattern.compile("(?m)^[ \\t]*([a-zA-Z_][a-zA-Z0-9_]*)[ \\t]+" + Pattern.quote(w)
					+ "\\b([ \\t]*(<-|=)[ \\t]*[^;\\n]+)?");
			Matcher m = varDecl.matcher(text);
			if (m.find()) {
				String typeName = m.group(1);
				if (!"parameter".equals(typeName) && !"facet".equals(typeName) && !"action".equals(typeName)
						&& !"species".equals(typeName) && !"experiment".equals(typeName)) {
					try {
						int lineNum = document.getLineOfOffset(m.start()) + 1;
						String lineText = m.group(0).trim();
						String title = "Variable " + w + " (type " + typeName + ")";
						String doc = "Declared at line " + lineNum + ": <code>" + lineText + "</code>"
								+ (isParamVar ? "<br/>Target variable bound to parameter <b>" + ctx.enclosingKeyword + "</b>."
										: "");
						return new DecontextualizedDoc(new Result(title, doc), "variable declaration");
					} catch (BadLocationException e) {
						// ignore
					}
				}
			}

			// Look for action declaration: "action my_action"
			Pattern actDecl = Pattern.compile("(?m)^[ \\t]*action[ \\t]+" + Pattern.quote(w) + "\\b");
			Matcher mAct = actDecl.matcher(text);
			if (mAct.find()) {
				try {
					int lineNum = document.getLineOfOffset(mAct.start()) + 1;
					return new DecontextualizedDoc(new Result("Action " + w, "Declared at line " + lineNum), "action declaration");
				} catch (BadLocationException e) {
					// ignore
				}
			}

			// Look for species declaration: "species my_species"
			Pattern spDecl = Pattern.compile("(?m)^[ \\t]*species[ \\t]+" + Pattern.quote(w) + "\\b");
			Matcher mSp = spDecl.matcher(text);
			if (mSp.find()) {
				try {
					int lineNum = document.getLineOfOffset(mSp.start()) + 1;
					return new DecontextualizedDoc(new Result("Species " + w, "Declared at line " + lineNum), "species declaration");
				} catch (BadLocationException e) {
					// ignore
				}
			}
		}

		if (isParamVar) {
			return new DecontextualizedDoc(new Result("Parameter variable: " + w,
					"Target variable bound to parameter. Ensure <b>" + w + "</b> is declared in the <code>global</code> block."),
					"parameter variable");
		}

		return null;
	}

	/**
	 * Suggest documentation or possible completions when an incomplete keyword begins a statement.
	 */
	protected IGamlDescription guessFromPrefix(final TextualContext ctx) {
		String w = ctx.word;
		if (!ctx.beginsSentence || w.length() > 4 || w.isEmpty()) return null;

		List<String> matchingStmts = new ArrayList<>();
		for (IArtefact.Symbol s : ArtefactRegistry.getStatementArtefacts()) {
			if (s.getName().startsWith(w) && !matchingStmts.contains(s.getName())) {
				matchingStmts.add(s.getName());
			}
		}
		List<String> matchingTypes = new ArrayList<>();
		for (String type : Types.getTypeNames()) {
			if (type.startsWith(w) && !matchingTypes.contains(type)) {
				matchingTypes.add(type);
			}
		}

		IExpression.Unit unit = GAML.getUnit(w);

		if (!matchingStmts.isEmpty() || !matchingTypes.isEmpty() || unit != null) {
			StringBuilder doc = new StringBuilder();
			if (!matchingStmts.isEmpty()) {
				doc.append("Possible statements starting with <b>").append(w).append("</b>: ")
						.append(String.join(", ", matchingStmts)).append("<br/>");
			}
			if (!matchingTypes.isEmpty()) {
				doc.append("Possible types starting with <b>").append(w).append("</b>: ")
						.append(String.join(", ", matchingTypes)).append("<br/>");
			}
			if (unit != null) {
				doc.append("Unit: <b>#").append(w).append("</b>");
				if (unit.getDocumentation() != null) {
					doc.append(" (").append(unit.getDocumentation().toString()).append(")");
				}
			}
			return new DecontextualizedDoc(new Result("Incomplete keyword or token '" + w + "'", doc.toString()),
					"syntax hint");
		}
		return null;
	}

	/**
	 * Fallback search across all GamlIdiomsProviders.
	 */
	protected IGamlDescription guessFromIdiomsProviders(final TextualContext ctx) {
		String w = ctx.word.endsWith(":") ? ctx.word.substring(0, ctx.word.length() - 1) : ctx.word;
		for (GamlIdiomsProvider<?> p : GamlIdiomsProvider.getProviders()) {
			Collection<?> matches = p.get(w);
			if (matches != null && !matches.isEmpty()) {
				Object first = matches.iterator().next();
				if (first instanceof IGamlDescription gd) {
					return new DecontextualizedDoc(gd, p.name);
				}
			}
		}
		return null;
	}

	/**
	 * Extracts the word surrounding the specified document offset.
	 */
	public static String extractWordAt(final IDocument document, final int offset) {
		if (document == null || offset < 0 || offset >= document.getLength()) return null;
		try {
			int len = document.getLength();
			char cur = document.getChar(offset);

			// 1. Check if inside quotes (string literal)
			int lineNum = document.getLineOfOffset(offset);
			IRegion lineInfo = document.getLineInformation(lineNum);
			int lineStart = lineInfo.getOffset();
			String line = document.get(lineStart, lineInfo.getLength());
			int col = offset - lineStart;

			int inQuote = -1;
			char quoteChar = 0;
			for (int i = 0; i < line.length(); i++) {
				char c = line.charAt(i);
				if (c == '"' || c == '\'') {
					if (inQuote == -1) {
						inQuote = i;
						quoteChar = c;
					} else if (c == quoteChar && (i == 0 || line.charAt(i - 1) != '\\')) {
						if (col >= inQuote && col <= i) {
							return line.substring(inQuote, i + 1);
						}
						inQuote = -1;
					}
				}
			}
			if (inQuote != -1 && col >= inQuote) {
				return line.substring(inQuote);
			}

			// 2. Colon check: if cursor is on ':' and preceded by word char (e.g. 'var:')
			if (cur == ':' && offset > 0 && isWordChar(document.getChar(offset - 1))) {
				int start = offset - 1;
				while (start > 0 && isWordChar(document.getChar(start - 1))) {
					start--;
				}
				return document.get(start, offset - start + 1);
			}

			// 3. Scan backwards for word boundary
			int start = offset;
			while (start > 0 && isWordChar(document.getChar(start - 1))) {
				start--;
			}
			// Scan forwards for word boundary
			int end = offset;
			while (end < len && isWordChar(document.getChar(end))) {
				end++;
			}

			if (end > start) {
				// Check if followed by ':' (facet key, but not '::')
				if (end < len && document.getChar(end) == ':') {
					if (end + 1 >= len || document.getChar(end + 1) != ':') {
						return document.get(start, end - start + 1);
					}
				}
				// Check if float literal like 0.0 or 1.5
				if (end < len && document.getChar(end) == '.' && end + 1 < len
						&& Character.isDigit(document.getChar(end + 1))) {
					int floatEnd = end + 1;
					while (floatEnd < len && Character.isDigit(document.getChar(floatEnd))) floatEnd++;
					return document.get(start, floatEnd - start);
				}
				if (start > 0 && document.getChar(start - 1) == '.' && start > 1
						&& Character.isDigit(document.getChar(start - 2))) {
					int floatStart = start - 2;
					while (floatStart > 0 && Character.isDigit(document.getChar(floatStart - 1))) floatStart--;
					return document.get(floatStart, end - floatStart);
				}
				return document.get(start, end - start).trim();
			}

			// 4. Multi-character operators
			if (offset + 1 < len) {
				String twoChar = document.get(offset, 2);
				if ("<-".equals(twoChar) || "::".equals(twoChar) || "!=".equals(twoChar) || "<=".equals(twoChar)
						|| ">=".equals(twoChar) || "->".equals(twoChar)) {
					return twoChar;
				}
			}
			if (offset > 0) {
				String twoChar = document.get(offset - 1, 2);
				if ("<-".equals(twoChar) || "::".equals(twoChar) || "!=".equals(twoChar) || "<=".equals(twoChar)
						|| ">=".equals(twoChar) || "->".equals(twoChar)) {
					return twoChar;
				}
			}

			// 5. Single operator char
			if (isOperatorChar(cur)) return String.valueOf(cur);
		} catch (BadLocationException e) {
			// ignore
		}
		return null;
	}

	/**
	 * Checks if a character is part of a GAML word or identifier.
	 */
	public static boolean isWordChar(final char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == '#';
	}

	/**
	 * Checks if a character is a standalone operator symbol.
	 */
	public static boolean isOperatorChar(final char c) {
		return c == '+' || c == '-' || c == '*' || c == '/' || c == '<' || c == '>' || c == '?' || c == '!' || c == '^';
	}
}
