/*******************************************************************************************************
 *
 * ISymbolKind.java, in gama.annotations, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.annotations.support;

import java.util.Map;
import java.util.Set;

/**
 * Written by drogoul Modified on 1 ao�t 2010
 *
 * @todo Description
 *
 */
public enum ISymbolKind {

	/** The number. */
	NUMBER(101),

	/** The container. */
	CONTAINER(102),

	/** The regular. */
	REGULAR(104),

	/** The species. */
	SPECIES(0),

	/** The model. */
	MODEL(1),

	/** The single statement. */
	SINGLE_STATEMENT(2),

	/** The behavior. */
	BEHAVIOR(3),

	/** The parameter. */
	PARAMETER(4),

	/** The output. */
	OUTPUT(5),

	/** The layer. */
	LAYER(6),

	/** The skill. */
	SKILL(7),

	/** The batch section. */
	BATCH_SECTION(8),

	/** The batch method. */
	BATCH_METHOD(9),

	/** The environment. */
	ENVIRONMENT(10),

	/** The sequence statement. */
	SEQUENCE_STATEMENT(11),

	/** The action. */
	// Equal to SEQUENCE_STATEMENT
	ACTION(11),
	/** The experiment. */
	EXPERIMENT(13),

	/** The abstract section. */
	ABSTRACT_SECTION(14),

	/** The operator. */
	OPERATOR(15),

	/** The platform. */
	PLATFORM(16),

	/** The facet. Doesnt count */
	FACET(9999);

	/** The number. */
	// Update this variable when adding a kind of symbol
	final static int __NUMBER_OF_DECLARED_SYMBOL_KINDS__ = 17;

	/** The code. */
	final int code;

	/**
	 * Instantiates a new i symbol kind 2.
	 *
	 * @param code
	 *            the code
	 */
	ISymbolKind(final int code) {
		this.code = code;
	}

	/**
	 * Code.
	 *
	 * @return the int
	 */
	public int code() {
		return code;
	}

	/**
	 * Gets the.
	 *
	 * @param code
	 *            the code
	 */
	public static ISymbolKind get(final int code) {
		for (ISymbolKind kind : ISymbolKind.values()) { if (kind.code() == code) return kind; }
		return null;
	}

	/** The template menu. */
	public static String[] TEMPLATE_MENU = { "Species", "Model", "Statement", "Behavior", "Parameter", "Output",
			"Layer", "Skill", "Batch", "Batch", "", "Statement", "Statement", "Experiment", "", "Operator", "" };

	/** The Constant STATEMENTS_WITH_ATTRIBUTES. */
	public static final Set<ISymbolKind> STATEMENTS_CONTAINING_ATTRIBUTES = Set.of(SPECIES, EXPERIMENT, OUTPUT, MODEL);

	/** The kinds. */
	public static Set<ISymbolKind> VARIABLES = Set.of(NUMBER, CONTAINER, REGULAR);

	/** The kinds as string. */
	public static Map<ISymbolKind, String> KINDS_AS_STRING =
			Map.of(NUMBER, "Number variable", CONTAINER, "Container variable", REGULAR, "Variable");

	/**
	 * Defines attributes.
	 *
	 * @param kind
	 *            the kind
	 * @return true, if successful
	 */
	public static boolean definesAttributes(final ISymbolKind kind) {
		return STATEMENTS_CONTAINING_ATTRIBUTES.contains(kind);
	}

}
