/**
 *
 */
package gaml.compiler.gaml.serializer;

import static gama.api.utils.StringUtils.LN;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import gama.api.constants.IKeyword;
import gama.api.utils.StringUtils;
import gaml.compiler.gaml.Access;
import gaml.compiler.gaml.ActionDefinition;
import gaml.compiler.gaml.ActionFakeDefinition;
import gaml.compiler.gaml.ActionRef;
import gaml.compiler.gaml.ArgumentDefinition;
import gaml.compiler.gaml.Array;
import gaml.compiler.gaml.BinaryOperator;
import gaml.compiler.gaml.Block;
import gaml.compiler.gaml.BooleanLiteral;
import gaml.compiler.gaml.DoubleLiteral;
import gaml.compiler.gaml.EGaml;
import gaml.compiler.gaml.EquationDefinition;
import gaml.compiler.gaml.EquationFakeDefinition;
import gaml.compiler.gaml.EquationRef;
import gaml.compiler.gaml.Expression;
import gaml.compiler.gaml.ExpressionList;
import gaml.compiler.gaml.Facet;
import gaml.compiler.gaml.Function;
import gaml.compiler.gaml.GamlDefinition;
import gaml.compiler.gaml.If;
import gaml.compiler.gaml.Import;
import gaml.compiler.gaml.IntLiteral;
import gaml.compiler.gaml.Model;
import gaml.compiler.gaml.Parameter;
import gaml.compiler.gaml.Point;
import gaml.compiler.gaml.Pragma;
import gaml.compiler.gaml.ReservedLiteral;
import gaml.compiler.gaml.S_Action;
import gaml.compiler.gaml.S_Assignment;
import gaml.compiler.gaml.S_Definition;
import gaml.compiler.gaml.S_Display;
import gaml.compiler.gaml.S_Do;
import gaml.compiler.gaml.S_Equations;
import gaml.compiler.gaml.S_Experiment;
import gaml.compiler.gaml.S_Global;
import gaml.compiler.gaml.S_If;
import gaml.compiler.gaml.S_Loop;
import gaml.compiler.gaml.S_Reflex;
import gaml.compiler.gaml.S_Return;
import gaml.compiler.gaml.S_Solve;
import gaml.compiler.gaml.S_Species;
import gaml.compiler.gaml.S_Switch;
import gaml.compiler.gaml.S_Try;
import gaml.compiler.gaml.SkillFakeDefinition;
import gaml.compiler.gaml.SkillRef;
import gaml.compiler.gaml.StandaloneBlock;
import gaml.compiler.gaml.StandaloneExperiment;
import gaml.compiler.gaml.StandaloneExpression;
import gaml.compiler.gaml.Statement;
import gaml.compiler.gaml.StringLiteral;
import gaml.compiler.gaml.TerminalExpression;
import gaml.compiler.gaml.TypeDefinition;
import gaml.compiler.gaml.TypeFakeDefinition;
import gaml.compiler.gaml.TypeInfo;
import gaml.compiler.gaml.TypeRef;
import gaml.compiler.gaml.Unary;
import gaml.compiler.gaml.Unit;
import gaml.compiler.gaml.UnitFakeDefinition;
import gaml.compiler.gaml.UnitName;
import gaml.compiler.gaml.VarDefinition;
import gaml.compiler.gaml.VarFakeDefinition;
import gaml.compiler.gaml.VariableRef;
import gaml.compiler.gaml.util.GamlSwitch;

/**
 *
 */
public class GamlSerializerToString extends GamlSwitch<String> {
	/** The Constant COLON. */
	private static final String COLON = ":";

	/** The Constant SEMI. */
	private static final String SEMI = ";";

	/** The Constant LESS_THAN. */
	private static final String LESS_THAN = "<";

	/** The Constant GREATER_THAN. */
	private static final String GREATER_THAN = ">";

	/** The Constant ARROW. */
	private static final String ARROW = "<-";

	/** The Constant LEFT_PAREN. */
	private static final String LEFT_PAREN = "(";

	/** The Constant RIGHT_PAREN. */
	private static final String RIGHT_PAREN = ")";

	/** The Constant LEFT_BRACE. */
	private static final String LEFT_BRACE = "{";

	/** The Constant RIGHT_BRACE. */
	private static final String RIGHT_BRACE = "}";

	/** The Constant LEFT_BRACKET. */
	private static final String LEFT_BRACKET = "[";

	/** The Constant RIGHT_BRACKET. */
	private static final String RIGHT_BRACKET = "]";

	/** The Constant COMMA. */
	private static final String COMMA = ",";

	/** The Constant DOT. */
	private static final String DOT = ".";

	/** The Constant QUESTION_MARK. */
	private static final String QUESTION_MARK = "?";

	/** The Constant TAB. */
	private static final String TAB = "\t";

	/** The Constant DASH. */
	private static final String DASH = "#";

	/** Current indentation level. Incremented on block open, decremented on block close. */
	private int indent = 0;

	/**
	 * Shared {@link StringBuilder} reused across all serialization calls to avoid repeated allocations. Each
	 * {@code case*} method records its start offset via {@link #mark()}, appends its content, then returns the slice
	 * via {@link #since(int)}, so recursive inner calls do not interfere with the outer frame.
	 */
	private final StringBuilder result = new StringBuilder();

	/**
	 * Returns a newline followed by {@link #indent} tab characters, to be used wherever a line break is needed so that
	 * the next line is correctly indented.
	 *
	 * @return the indented newline string
	 */
	private String nl() {
		return LN + " ".repeat(indent * 4);
	}

	/**
	 * Increments the indentation level by one.
	 */
	private void indentIn() {
		indent++;
	}

	/**
	 * Decrements the indentation level by one, down to a minimum of zero.
	 */
	private void indentOut() {
		if (indent > 0) { indent--; }
	}

	/**
	 * @param gamlEObjectImpl
	 * @return
	 */
	public static String asString(final EObject object) {
		GamlSerializerToString s = new GamlSerializerToString();
		return s.doSwitch(object);
	}

	@Override
	public String caseStandaloneBlock(final StandaloneBlock object) {
		return caseBlock(object.getBlock());
	}

	@Override
	public String caseStandaloneExpression(final StandaloneExpression object) {
		return caseExpression(object.getExpr());
	}

	@Override
	public String caseStandaloneExperiment(final StandaloneExperiment object) {
		int m = mark();
		sb().append(object.getKey()).append(" ");
		sb().append(object.getName()).append(" ");
		sb().append("model: ").append(object.getImportURI()).append(" ");
		sb().append(facets(object.getFacets()));
		appendBlock(object.getBlock(), true);
		return since(m);
	}

	@Override
	public String caseModel(final Model object) {
		int m = mark();
		object.getPragmas().forEach(p -> sb().append(serialize(p)).append(nl()));
		object.getImports().forEach(i -> sb().append(serialize(i)).append(nl()));
		sb().append("model").append(" ").append(object.getName()).append(nl());
		appendBlock(object.getBlock(), false);
		return since(m);
	}

	@Override
	public String casePragma(final Pragma object) {
		return "@" + object.getName();
	}

	@Override
	public String caseImport(final Import object) {
		return "import " + object.getImportURI() + (object.getName() != null ? " as " + object.getName() : "") + SEMI;
	}

	@Override
	public String caseStatement(final Statement object) {
		int m = mark();
		sb().append(object.getKey()).append(" ").append(serialize(object.getExpr())).append(" ");
		sb().append(facets(object.getFacets()));
		appendBlock(object.getBlock(), true);
		return since(m);
	}

	@Override
	public String caseS_Global(final S_Global object) {
		int m = mark();
		sb().append(object.getKey()).append(" ");
		sb().append(facets(object.getFacets()));
		appendBlock(object.getBlock(), true);
		return since(m);
	}

	@Override
	public String caseS_Species(final S_Species object) {
		int m = mark();
		sb().append(object.getKey()).append(" ");
		sb().append(object.getName()).append(" ");
		sb().append(facets(object.getFacets()));
		appendBlock(object.getBlock(), true);
		return since(m);
	}

	@Override
	public String caseS_Experiment(final S_Experiment object) {
		int m = mark();
		sb().append(object.getKey()).append(" ");
		sb().append(object.getName()).append(" ");
		sb().append(facets(object.getFacets()));
		appendBlock(object.getBlock(), true);
		return since(m);
	}

	@Override
	public String caseS_Do(final S_Do object) {
		int m = mark();
		sb().append(object.getKey()).append(" ").append(serialize(object.getExpr())).append(SEMI);
		return since(m);
	}

	@Override
	public String caseS_Loop(final S_Loop object) {
		int m = mark();
		sb().append(object.getKey()).append(" ");
		if (object.getName() != null) { sb().append(object.getName()).append(" "); }
		sb().append(facets(object.getFacets()));
		appendBlock(object.getBlock(), true);
		return since(m);
	}

	@Override
	public String caseS_If(final S_If object) {
		int m = mark();
		sb().append(object.getKey()).append(" ");
		if (object.getExpr() != null) { sb().append(parenthesize(object.getExpr())).append(" "); }
		appendBlock(object.getBlock(), true);
		if (object.getElse() != null) {
			sb().append(" else ").append(LEFT_BRACE);
			indentIn();
			sb().append(nl()).append(serialize(object.getElse()));
			indentOut();
			sb().append(nl()).append(RIGHT_BRACE);
		}
		return since(m);
	}

	@Override
	public String caseS_Try(final S_Try object) {
		int m = mark();
		sb().append(object.getKey()).append(" ");
		appendBlock(object.getBlock(), true);
		if (object.getCatch() != null) {
			sb().append(" catch ");
			appendBlock(object.getCatch(), true);
		}
		return since(m);
	}

	@Override
	public String caseS_Switch(final S_Switch object) {
		int m = mark();
		sb().append(object.getKey()).append(parenthesize(object.getExpr()));
		return since(m);
	}

	@Override
	public String caseS_Return(final S_Return object) {
		return object.getKey() + (object.getExpr() != null ? serialize(object.getExpr()) : "") + SEMI;
	}

	@Override
	public String caseS_Reflex(final S_Reflex object) {
		int m = mark();
		sb().append(object.getKey()).append(' ');
		if (object.getName() != null) { sb().append(object.getName()).append(" "); }
		sb().append(facets(object.getFacets()));
		appendBlock(object.getBlock(), true);
		return since(m);
	}

	@Override
	public String caseS_Definition(final S_Definition object) {
		int m = mark();
		EObject tkey = object.getTkey();
		String type = tkey == null ? object.getKey() : tkey.toString();
		sb().append(type).append(' ').append(object.getName()).append(" ");
		if (object.getArgs() != null && !object.getArgs().isEmpty()) {
			sb().append(parenthesize(join(object.getArgs(), ", "))).append(" ");
		}
		sb().append(facets(object.getFacets()));
		appendBlock(object.getBlock(), false);
		return since(m);
	}

	@Override
	public String caseS_Assignment(final S_Assignment object) {
		int m = mark();
		sb().append(serialize(object.getExpr())).append(" ").append(object.getKey()).append(" ")
				.append(serialize(object.getValue())).append(" ");
		sb().append(facets(object.getFacets()));
		appendBlock(object.getBlock(), false);
		return since(m);
	}

	@Override
	public String caseS_Equations(final S_Equations object) {
		int m = mark();
		sb().append(object.getKey()).append(" ").append(object.getName()).append(facets(object.getFacets()))
				.append(LEFT_BRACE);
		indentIn();
		sb().append(nl()).append(join(object.getEquations(), SEMI + nl()));
		indentOut();
		sb().append(nl()).append(RIGHT_BRACE);
		return since(m);
	}

	@Override
	public String caseS_Solve(final S_Solve object) {
		int m = mark();
		sb().append(object.getKey()).append(" ").append(serialize(object.getExpr())).append(facets(object.getFacets()))
				.append(SEMI);
		return since(m);
	}

	@Override
	public String caseS_Display(final S_Display object) {
		int m = mark();
		sb().append(object.getKey()).append(" ").append(object.getName()).append(facets(object.getFacets()))
				.append(" ");
		appendBlock(object.getBlock(), false);
		return since(m);
	}

	@Override
	public String caseArgumentDefinition(final ArgumentDefinition object) {
		int m = mark();
		sb().append(object.getType()).append(" ").append(object.getName());
		if (object.getDefault() != null) {
			sb().append(" ").append(ARROW).append(" ").append(serialize(object.getDefault()));
		}
		return since(m);
	}

	@Override
	public String caseFacet(final Facet object) {
		int m = mark();
		String key = object.getKey();
		sb().append(key);
		if (!key.endsWith(COLON) && !ARROW.equals(key)) { sb().append(COLON).append(" "); }
		Block block = object.getBlock();
		if (block != null) {
			sb().append(LEFT_BRACE).append(serialize(block)).append(RIGHT_BRACE);
		} else {
			sb().append(serialize(object.getExpr()));
		}
		return since(m);
	}

	@Override
	public String caseBlock(final Block object) {
		int m = mark();
		sb().append(join(object.getStatements(), nl()));
		return since(m);
	}

	@Override
	public String caseExpression(final Expression object) {
		return "";
	}

	@Override
	public String caseExpressionList(final ExpressionList object) {
		return join(object.getExprs(), ", ");
	}

	@Override
	public String caseVariableRef(final VariableRef object) {
		return object.getRef().getName();
	}

	@Override
	public String caseTypeInfo(final TypeInfo object) {
		String first = object.getFirst() == null ? null : serialize(object.getFirst());
		String second = object.getSecond() == null ? null : serialize(object.getSecond());
		if (second == null) return first;
		return first + COMMA + second;
	}

	@Override
	public String caseGamlDefinition(final GamlDefinition object) {
		return object.getName();
	}

	@Override
	public String caseEquationDefinition(final EquationDefinition object) {
		return object.getName();
	}

	@Override
	public String caseTypeDefinition(final TypeDefinition object) {
		return object.getName();
	}

	@Override
	public String caseActionDefinition(final ActionDefinition object) {
		return object.getName();
	}

	@Override
	public String caseVarDefinition(final VarDefinition object) {
		return object.getName();
	}

	@Override
	public String caseUnitFakeDefinition(final UnitFakeDefinition object) {
		return object.getName();
	}

	@Override
	public String caseTypeFakeDefinition(final TypeFakeDefinition object) {
		return object.getName();
	}

	@Override
	public String caseActionFakeDefinition(final ActionFakeDefinition object) {
		return object.getName();
	}

	@Override
	public String caseSkillFakeDefinition(final SkillFakeDefinition object) {
		return object.getName();
	}

	@Override
	public String caseVarFakeDefinition(final VarFakeDefinition object) {
		return object.getName();
	}

	@Override
	public String caseEquationFakeDefinition(final EquationFakeDefinition object) {
		return super.caseEquationFakeDefinition(object);
	}

	@Override
	public String caseTerminalExpression(final TerminalExpression object) {
		return object.getOp();
	}

	@Override
	public String caseStringLiteral(final StringLiteral object) {
		return StringUtils.toGamlString(object.getOp());
	}

	@Override
	public String caseS_Action(final S_Action object) {
		int m = mark();
		EObject tkey = object.getTkey();
		String type = tkey == null ? IKeyword.ACTION : tkey.toString();
		sb().append(type).append(' ').append(object.getName()).append(parenthesize(join(object.getArgs(), ", ")))
				.append(" ").append(facets(object.getFacets()));
		appendBlock(object.getBlock(), true);
		return since(m);
	}

	@Override
	public String caseBinaryOperator(final BinaryOperator p) {
		int m = mark();
		sb().append(parenthesize(p.getLeft())).append(p.getOp()).append(parenthesize(p.getRight()));
		return since(m);
	}

	@Override
	public String caseIf(final If i) {
		int m = mark();
		sb().append(parenthesize(i.getLeft())).append(i.getOp()).append(parenthesize(i.getRight())).append(COLON)
				.append(serialize(i.getIfFalse()));
		return since(m);
	}

	@Override
	public String caseUnit(final Unit u) {
		int m = mark();
		sb().append(serialize(u.getLeft())).append(u.getOp()).append(serialize(u.getRight()));
		return since(m);
	}

	@Override
	public String caseUnary(final Unary u) {
		int m = mark();
		String s = u.getOp();
		sb().append(s);
		if (DASH.equals(s)) {
			sb().append(serialize(u.getRight()));
		} else {
			sb().append(parenthesize(u.getRight()));
		}
		return since(m);
	}

	@Override
	public String caseAccess(final Access object) {
		int m = mark();
		if (LEFT_BRACKET.equals(object.getOp())) {
			sb().append(serialize(object.getLeft())).append(bracketize(object.getRight()));
		} else {
			sb().append(serialize(object.getLeft())).append(DOT).append(serialize(object.getRight()));
		}
		return since(m);
	}

	@Override
	public String caseArray(final Array object) {
		return bracketize(object.getExprs());
	}

	@Override
	public String casePoint(final Point object) {
		int m = mark();
		sb().append(LEFT_BRACE).append(parenthesize(object.getLeft())).append(',')
				.append(parenthesize(object.getRight()));
		if (object.getZ() != null) { sb().append(',').append(parenthesize(object.getZ())); }
		sb().append(RIGHT_BRACE);
		return since(m);
	}

	@Override
	public String caseFunction(final Function object) {
		int m = mark();
		sb().append(serialize(object.getLeft()));
		if (object.getType() != null) {
			sb().append(LESS_THAN).append(serialize(object.getType())).append(GREATER_THAN);
		}
		sb().append(parenthesize(object.getRight()));
		return since(m);
	}

	@Override
	public String caseParameter(final Parameter object) {
		String left = object.getBuiltInFacetKey();
		if (left == null) { left = serialize(object.getLeft()); }
		return left + ": " + serialize(object.getRight());
	}

	@Override
	public String caseUnitName(final UnitName object) {
		return EGaml.getInstance().getNameOfRef(object);
	}

	@Override
	public String caseSkillRef(final SkillRef object) {
		return EGaml.getInstance().getNameOfRef(object);
	}

	@Override
	public String caseActionRef(final ActionRef object) {
		return EGaml.getInstance().getNameOfRef(object);
	}

	@Override
	public String caseEquationRef(final EquationRef object) {
		return EGaml.getInstance().getNameOfRef(object);
	}

	@Override
	public String caseTypeRef(final TypeRef object) {
		int m = mark();
		sb().append(EGaml.getInstance().getNameOfRef(object));
		if (object.getParameter() != null) {
			sb().append(LESS_THAN).append(serialize(object.getParameter())).append(GREATER_THAN);
		}
		return since(m);
	}

	@Override
	public String caseIntLiteral(final IntLiteral object) {
		return object.getOp();
	}

	@Override
	public String caseDoubleLiteral(final DoubleLiteral object) {
		return object.getOp();
	}

	@Override
	public String caseBooleanLiteral(final BooleanLiteral object) {
		return object.getOp();
	}

	@Override
	public String caseReservedLiteral(final ReservedLiteral object) {
		return object.getOp();
	}

	@Override
	public String defaultCase(final EObject object) {
		if (object instanceof GamlEObjectImpl geo) return geo.superToString();
		return "";
	}

	/**
	 * Serialize.
	 *
	 * @param serializer
	 *            the serializer
	 * @param object
	 *            the object
	 */
	private String serialize(final EObject object) {
		if (object == null) return "";
		String s = doSwitch(object);
		return s != null ? s : object.toString();
	}

	/**
	 * Serialize.
	 *
	 * @param object
	 *            the object
	 * @return the string builder
	 */
	private String serialize(final String object) {
		return object == null ? "" : object;
	}

	/**
	 * Parenthesize.
	 *
	 * @param object
	 *            the object
	 * @return the string builder
	 */
	private String parenthesize(final EObject object) {
		return parenthesize(serialize(object));
	}

	/**
	 * @param joining
	 */
	private String parenthesize(final String object) {
		return LEFT_PAREN + serialize(object) + RIGHT_PAREN;

	}

	/**
	 * Bracketize.
	 *
	 * @param object
	 *            the object
	 * @return the string builder
	 */
	private String bracketize(final EObject object) {
		return LEFT_BRACKET + serialize(object) + RIGHT_BRACKET;
	}

	/**
	 * Records the current length of the shared {@link #result} builder as a frame start offset. Used in conjunction
	 * with {@link #since(int)} to extract the string contributed by the current {@code case*} method without
	 * interference from recursive inner calls.
	 *
	 * @return the current length of {@link #result}, to be passed later to {@link #since(int)}
	 */
	private int mark() {
		return result.length();
	}

	/**
	 * Returns the slice of {@link #result} appended since the given {@code offset}, then truncates the builder back to
	 * that offset so the shared buffer does not grow unboundedly.
	 *
	 * @param offset
	 *            the start offset previously returned by {@link #mark()}
	 * @return the string appended to {@link #result} since {@code offset}
	 */
	private String since(final int offset) {
		String s = result.substring(offset);
		result.setLength(offset);
		return s;
	}

	/**
	 * Sb.
	 *
	 * @return the shared {@link #result} builder, positioned at the current end, ready for appending
	 */
	private StringBuilder sb() {
		return result;
	}

	/**
	 * Appends the serialized form of a block to the shared {@link #result} builder. When {@code block} is not null, its
	 * content is wrapped in braces and preceded by an indented newline. When {@code block} is null, the behaviour
	 * depends on {@code emptyBraces}: if {@code true}, empty braces {@code {}} are appended; if {@code false}, a
	 * semicolon {@code ;} is appended instead.
	 *
	 * @param block
	 *            the {@link Block} to serialize, may be {@code null}
	 * @param emptyBraces
	 *            if {@code true}, output {@code {}} when the block is {@code null}; if {@code false}, output {@code ;}
	 */
	private void appendBlock(final Block block, final boolean emptyBraces) {
		if (block != null) {
			sb().append(LEFT_BRACE);
			indentIn();
			sb().append(nl()).append(serialize(block));
			indentOut();
			sb().append(nl()).append(RIGHT_BRACE);
		} else if (emptyBraces) {
			sb().append(LEFT_BRACE).append(RIGHT_BRACE);
		} else {
			sb().append(SEMI);
		}
	}

	/**
	 * Serializes a list of {@link Facet} objects separated by spaces. Convenience wrapper around
	 * {@link #join(EList, String)} using a single space as delimiter.
	 *
	 * @param list
	 *            the list of facets to serialize, may be {@code null} or empty
	 * @return the space-separated serialized facets, or an empty string if the list is {@code null} or empty
	 */
	private String facets(final EList<? extends EObject> list) {
		return join(list, " ");
	}

	/**
	 * Join.
	 *
	 * @param list
	 *            the list
	 * @param delimiter
	 *            the delimiter
	 * @return the string
	 */
	private String join(final EList<? extends EObject> list, final String delimiter) {
		if (list == null || list.isEmpty()) return "";
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) { result.append(delimiter); }
			result.append(serialize(list.get(i)));
		}
		return result.toString();
	}
}