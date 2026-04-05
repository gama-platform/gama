/*******************************************************************************************************
 *
 * StatementSerializer.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.serialization;

import com.google.common.collect.Iterables;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.gaml.symbols.Arguments;
import gama.api.utils.StringUtils;

/**
 * Serializer for GAML statement descriptions with argument handling.
 * 
 * <p>
 * The {@code StatementSerializer} implements {@link ISymbolSerializer} to handle the serialization of GAML
 * statements that may have arguments, such as actions, do statements, and create statements. It extends the
 * standard facet serialization to include both formal parameters and passed arguments.
 * </p>
 * 
 * <h2>Purpose</h2>
 * 
 * <p>
 * This serializer enables:
 * </p>
 * <ul>
 *   <li><strong>Action Serialization:</strong> Convert action definitions with parameters to GAML code</li>
 *   <li><strong>Statement Calls:</strong> Serialize do/create statements with arguments</li>
 *   <li><strong>Parameter Handling:</strong> Properly format both formal and actual parameters</li>
 *   <li><strong>Named Arguments:</strong> Support both positional and named argument syntax</li>
 * </ul>
 * 
 * <h2>Argument Types</h2>
 * 
 * <h3>Formal Arguments (Parameters)</h3>
 * 
 * <p>
 * Used in action definitions to declare parameters:
 * </p>
 * <pre>{@code
 * action move(float speed, point target) { ... }
 * }</pre>
 * 
 * <h3>Passed Arguments (Actual Parameters)</h3>
 * 
 * <p>
 * Used in statement calls to provide values:
 * </p>
 * <pre>{@code
 * do move(5.0, {10, 20});           // Positional
 * do move(speed: 5.0, target: loc); // Named
 * }</pre>
 * 
 * <h2>Serialization Strategy</h2>
 * 
 * <p>
 * The serialization process:
 * </p>
 * <ol>
 *   <li><strong>Facets First:</strong> Serialize regular facets using parent implementation</li>
 *   <li><strong>Then Arguments:</strong> Add argument list in parentheses</li>
 *   <li><strong>Formal vs Passed:</strong> Use formal args if present, otherwise passed args</li>
 *   <li><strong>Formatting:</strong> Comma-separated, with named args using colon syntax</li>
 * </ol>
 * 
 * <h2>Argument Formatting</h2>
 * 
 * <h3>Positional Arguments</h3>
 * 
 * <p>
 * Arguments with numeric keys (0, 1, 2...) are serialized without names:
 * </p>
 * <pre>
 * (expr1, expr2, expr3)
 * </pre>
 * 
 * <h3>Named Arguments</h3>
 * 
 * <p>
 * Arguments with string keys are serialized with names:
 * </p>
 * <pre>
 * (param1: value1, param2: value2)
 * </pre>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * // Serialize an action definition
 * IStatementDescription action = ...; // action with formal args
 * StatementSerializer serializer = new StatementSerializer();
 * 
 * String gamlCode = serializer.serialize(action, false);
 * // Result: "action move(float speed, point target) { ... }"
 * 
 * // Serialize a do statement
 * IStatementDescription doStmt = ...; // do with passed args
 * String doCode = serializer.serialize(doStmt, false);
 * // Result: "do move(5.0, {10, 20})"
 * }</pre>
 * 
 * <h2>Extension Points</h2>
 * 
 * <p>
 * The {@link #serializeArg(IDescription, IDescription, StringBuilder, boolean)} method is designed to be
 * overridden by specialized serializers (e.g., for actions, do, create) to customize individual argument
 * serialization.
 * </p>
 * 
 * <h2>Empty Arguments</h2>
 * 
 * <p>
 * If neither formal arguments nor passed arguments are present, no parentheses are added to the output.
 * </p>
 * 
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 * 
 * @see ISymbolSerializer
 * @see IStatementDescription
 * @see Arguments
 */
public class StatementSerializer implements ISymbolSerializer {

	/**
	 * Serializes facets followed by arguments.
	 * 
	 * <p>
	 * This method extends the default facet serialization by appending the argument list after all facets have been
	 * processed. This ensures the proper GAML syntax where facets appear before the argument list.
	 * </p>
	 * 
	 * <p>
	 * Output format:
	 * </p>
	 * <pre>
	 * keyword facet1: value1 facet2: value2 (arg1, arg2)
	 * </pre>
	 * 
	 * @param s
	 *            the statement description to serialize
	 * @param sb
	 *            the string builder to append output to
	 * @param includingBuiltIn
	 *            whether to include built-in elements
	 */
	@Override
	public void serializeFacets(final IDescription s, final StringBuilder sb, final boolean includingBuiltIn) {
		ISymbolSerializer.super.serializeFacets(s, sb, includingBuiltIn);
		serializeArgs(s, sb, includingBuiltIn);

	}

	/**
	 * Serializes the argument list for a statement.
	 * 
	 * <p>
	 * This method handles both formal arguments (parameter declarations) and passed arguments (actual values). It
	 * prioritizes formal arguments when available, falling back to passed arguments otherwise.
	 * </p>
	 * 
	 * <p>
	 * Behavior:
	 * </p>
	 * <ul>
	 *   <li><strong>Formal Arguments Present:</strong> Serialize each parameter using {@link #serializeArg}</li>
	 *   <li><strong>Only Passed Arguments:</strong> Serialize values with positional or named syntax</li>
	 *   <li><strong>No Arguments:</strong> Do nothing (no parentheses added)</li>
	 * </ul>
	 * 
	 * <p>
	 * Formatting rules:
	 * </p>
	 * <ul>
	 *   <li>Arguments enclosed in parentheses: {@code (arg1, arg2)}</li>
	 *   <li>Positional args (numeric keys): {@code value}</li>
	 *   <li>Named args (string keys): {@code name: value}</li>
	 *   <li>Comma-separated with trailing comma removed</li>
	 * </ul>
	 * 
	 * <p>
	 * Example outputs:
	 * </p>
	 * <pre>
	 * (5.0, {10, 20})              // Positional
	 * (speed: 5.0, target: loc)    // Named
	 * (float speed, point target)  // Formal parameters
	 * </pre>
	 * 
	 * @param s
	 *            the statement description to process
	 * @param sb
	 *            the string builder to append output to
	 * @param includingBuiltIn
	 *            whether to include built-in elements
	 */
	protected void serializeArgs(final IDescription s, final StringBuilder sb, final boolean includingBuiltIn) {
		final IStatementDescription desc = (IStatementDescription) s;

		final Iterable<IDescription> formalArgs = desc.getFormalArgs();
		if (!Iterables.isEmpty(formalArgs)) {
			sb.append("(");
			for (final IDescription arg : formalArgs) {
				serializeArg(desc, arg, sb, includingBuiltIn);
				sb.append(", ");
			}
		} else {
			final Arguments passedArgs = desc.getPassedArgs();
			if (passedArgs.isEmpty()) return;
			sb.append("(");
			passedArgs.forEachFacet((name, value) -> {
				if (StringUtils.isGamaNumber(name)) {
					sb.append(value.serializeToGaml(includingBuiltIn));
				} else {
					sb.append(name).append(":").append(value.serializeToGaml(includingBuiltIn));
				}
				sb.append(", ");
				return true;
			});

		}
		sb.setLength(sb.length() - 2);
		sb.append(")");
	}

	/**
	 * Serializes a single formal argument (extension point for subclasses).
	 * 
	 * <p>
	 * This method is designed to be overridden by specialized statement serializers (such as those for actions,
	 * do statements, or create statements) to provide custom formatting for individual formal parameters.
	 * </p>
	 * 
	 * <p>
	 * The base implementation is intentionally empty since argument serialization is context-dependent. Subclasses
	 * typically append type and name information:
	 * </p>
	 * <pre>{@code
	 * // Example override in ActionSerializer:
	 * sb.append(arg.getGamlType().serialize()).append(" ").append(arg.getName());
	 * // Result: "float speed"
	 * }</pre>
	 * 
	 * @param desc
	 *            the statement description containing the argument
	 * @param arg
	 *            the formal argument description to serialize
	 * @param sb
	 *            the string builder to append output to
	 * @param includingBuiltIn
	 *            whether to include built-in elements
	 */
	protected void serializeArg(final IDescription desc, final IDescription arg, final StringBuilder sb,
			final boolean includingBuiltIn) {
		// normally never called as it is redefined for action, do and
		// create
	}

}