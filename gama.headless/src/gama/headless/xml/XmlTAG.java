/*******************************************************************************************************
 *
 * XmlTAG.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.xml;

import gama.core.common.interfaces.IKeyword;

/**
 * The Interface XmlTAG.
 */
public interface XmlTAG {

	/** The Constant SIMULATION_TAG. */
	String SIMULATION_TAG = "Simulation";

	/** The Constant PARAMETERS_TAG. */
	String PARAMETERS_TAG = "Parameters";

	/** The Constant OUTPUTS_TAG. */
	String OUTPUTS_TAG = "Outputs";

	/** The Constant EXPERIMENT_ID_TAG. */
	String EXPERIMENT_ID_TAG = "id";

	/** The Constant SOURCE_PATH_TAG. */
	String SOURCE_PATH_TAG = "sourcePath";

	/** The Constant FINAL_STEP_TAG. */
	String FINAL_STEP_TAG = "finalStep";

	/** The Constant EXPERIMENT_NAME_TAG. */
	String EXPERIMENT_NAME_TAG = "experiment";

	/** The Constant SEED_TAG. */
	String SEED_TAG = "seed";

	/** The Constant OUTPUT_TAG. */
	String OUTPUT_TAG = "Output";

	/** The Constant PARAMETER_TAG. */
	String PARAMETER_TAG = "Parameter";

	/** The Constant ID_TAG. */
	String ID_TAG = "id";

	/** The Constant NAME_TAG. */
	String NAME_TAG = IKeyword.NAME;

	/** The Constant VAR_TAG. */
	String VAR_TAG = "var";

	/** The Constant VALUE_TAG. */
	String VALUE_TAG = IKeyword.VALUE;

	/** The Constant TYPE_TAG. */
	String TYPE_TAG = IKeyword.TYPE;

	/** The Constant WIDTH_TAG. */
	String WIDTH_TAG = "width";

	/** The Constant HEIGHT_TAG. */
	String HEIGHT_TAG = "height";

	/** The Constant FRAMERATE_TAG. */
	String FRAMERATE_TAG = "framerate";

	/** The Constant EXPERIMENT_PLAN_TAG. */
	String EXPERIMENT_PLAN_TAG = "Experiment_plan";

	/** The Constant OUTPUT_PATH. */
	String OUTPUT_PATH = "output_path";

	/** The Constant UNTIL_TAG. */
	String UNTIL_TAG = "until";

}
