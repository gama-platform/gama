/*******************************************************************************************************
 *
 * IKeywords.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.command;

import gama.annotations.constants.IKeyword;

/**
 * The Interface IKeywords.
 */
public interface IKeywords {

	/** The Constant RUNSIMULARTION. */
	String RUNSIMULARTION = "run";

	/** The Constant STARTSIMULATION. */
	String STARTSIMULATION = "start_simulation";

	/** The Constant LOADSUBMODEL. */
	String LOADSUBMODEL = "load_sub_model";

	/** The Constant STEPSUBMODEL. */
	String STEPSUBMODEL = "step_sub_model";

	/** The Constant EVALUATESUBMODEL. */
	String EVALUATESUBMODEL = "evaluate_sub_model";

	/** The Constant WITHPARAMS. */
	String WITHPARAMS = "with_param";

	/** The Constant WITHOUTPUTS. */
	String WITHOUTPUTS = "with_output";

	/** The Constant WITHSEED. */
	String WITHSEED = "seed";

	/** The Constant OUT. */
	String OUT = "out";

	/** The Constant CORE. */
	String CORE = "core";

	/** The Constant END. */
	String END = "end_cycle";

	/** The Constant EXPERIMENT. */
	String EXPERIMENT = IKeyword.NAME;

	/** The Constant MODEL. */
	String MODEL = "of";
}
