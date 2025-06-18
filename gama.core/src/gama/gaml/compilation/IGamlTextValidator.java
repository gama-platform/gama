/*******************************************************************************************************
 *
 * IGamlTextValidator.java, in gama.lang.gaml, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.compilation;

import java.util.List;

/**
 * The Interface IGamlTextValidator.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 11 janv. 2024
 */
public interface IGamlTextValidator {

	/**
	 * Semantic validation of expression.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param expr
	 *            the expr
	 * @param syntaxOnly TODO
	 * @return the list
	 * @date 11 janv. 2024
	 */
	default void validateExpression(final String expr, final List<GamlCompilationError> errors, boolean syntaxOnly) {
		String fixed_exp = expr;
		if (! expr.endsWith("}") && !expr.endsWith(";")) {
			fixed_exp += ";";
		}
		if (!expr.contains("<-")) {
			fixed_exp = "unknow result <- " + fixed_exp;
		}
		validateStatements(fixed_exp, errors, syntaxOnly);
	}

	/**
	 * Semantic validation of block.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param expr
	 *            the expr
	 * @param syntaxOnly TODO
	 * @return the list
	 * @date 11 janv. 2024
	 */
	default void validateStatements(final String expr, final List<GamlCompilationError> errors, boolean syntaxOnly) {
		validateSpecies("species synthetic_species { init {" + expr + " }}", errors, syntaxOnly);
	}

	/**
	 * Semantic validation of species.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param expr
	 *            the expr
	 * @param errors
	 *            the errors
	 * @param syntaxOnly TODO
	 * @date 11 janv. 2024
	 */
	default void validateSpecies(final String expr, final List<GamlCompilationError> errors, boolean syntaxOnly) {
		validateModel("model synthetic \n" + expr, errors, syntaxOnly);
	}

	/**
	 * Semantic validation of model.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param expr
	 *            the expr
	 * @param errors
	 *            the errors
	 * @param syntaxOnly TODO
	 * @date 11 janv. 2024
	 */
	void validateModel(final String expr, final List<GamlCompilationError> errors, boolean syntaxOnly);

}