/*******************************************************************************************************
 *
 * BatchValidator.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.validation;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.constants.IGamlIssue;
import gama.api.constants.IKeyword;
import gama.api.gaml.expressions.IExpression;
import gama.api.kernel.simulation.IExploration;

/**
 * Validator for batch experiment descriptions and their exploration methods.
 *
 * <p>
 * This validator ensures that batch experiments are properly configured with valid exploration methods and appropriate
 * parameters. It validates experiment types, exploration methods, sampling configurations, and parameter requirements
 * for different exploration strategies.
 * </p>
 *
 * <h2>Validation Scope</h2>
 *
 * <p>
 * The validator performs comprehensive checks on:
 * </p>
 * <ul>
 * <li><strong>Experiment Type:</strong> Verifies the experiment type is recognized (batch, gui, etc.)</li>
 * <li><strong>Batch-Specific Features:</strong> Ensures batch-only features aren't used in other experiments</li>
 * <li><strong>Exploration Methods:</strong> Validates method-specific parameter requirements</li>
 * <li><strong>Sampling Configuration:</strong> Checks sample sizes, levels, and iterations</li>
 * <li><strong>Output Configuration:</strong> Validates batch output settings</li>
 * </ul>
 *
 * <h2>Supported Exploration Methods</h2>
 *
 * <h3>Morris Sampling:</h3>
 * <ul>
 * <li><strong>Required:</strong> Even sample size (default: 132)</li>
 * <li><strong>Optional:</strong> {@code nb_levels} (default: 4, must be positive)</li>
 * <li><strong>Use Case:</strong> Sensitivity analysis with elementary effects</li>
 * </ul>
 *
 * <h3>Saltelli Sampling:</h3>
 * <ul>
 * <li><strong>Required:</strong> Sample size (default: 132)</li>
 * <li><strong>Ignored:</strong> {@code nb_levels} (not needed for Saltelli)</li>
 * <li><strong>Use Case:</strong> Sobol sensitivity analysis</li>
 * </ul>
 *
 * <h3>LHS (Latin Hypercube Sampling):</h3>
 * <ul>
 * <li><strong>Required:</strong> Sample size (default: 132)</li>
 * <li><strong>Use Case:</strong> Efficient parameter space exploration</li>
 * </ul>
 *
 * <h3>Orthogonal Sampling:</h3>
 * <ul>
 * <li><strong>Required:</strong> Sample size (default: 132) and iterations (default: 5)</li>
 * <li><strong>Use Case:</strong> Orthogonal array-based exploration</li>
 * </ul>
 *
 * <h3>Uniform Sampling:</h3>
 * <ul>
 * <li><strong>Required:</strong> Sample size (default: 132)</li>
 * <li><strong>Use Case:</strong> Uniform random sampling</li>
 * </ul>
 *
 * <h3>Factorial Design:</h3>
 * <ul>
 * <li><strong>Required:</strong> Sample size (default: 132)</li>
 * <li><strong>Optional:</strong> {@code sample_factorial} for explicit design specification</li>
 * <li><strong>Use Case:</strong> Full or fractional factorial experiments</li>
 * </ul>
 *
 * <h2>Error and Warning Conditions</h2>
 *
 * <h3>Errors ({@link IGamlIssue}):</h3>
 * <ul>
 * <li>Invalid experiment type</li>
 * <li>Non-batch experiment with exploration method</li>
 * <li>Invalid sample size (&lt; 1)</li>
 * <li>Invalid levels for Morris (&lt;= 0)</li>
 * <li>Odd sample size for Morris (must be even)</li>
 * <li>Unknown exploration method</li>
 * <li>{@code batch_var_outputs} is not a list</li>
 * </ul>
 *
 * <h3>Warnings ({@link IGamlIssue}):</h3>
 * <ul>
 * <li>{@link IGamlIssue#CONFLICTING_FACETS} - Batch experiments with 'record' facet (ignored)</li>
 * <li>{@link IGamlIssue#MISSING_FACET} - Missing recommended parameters (sample size, levels, etc.)</li>
 * <li>Missing {@code batch_output} file specification</li>
 * <li>Missing {@code until} stopping condition (may cause endless runs)</li>
 * <li>Sobol method suggestion (when "sobol" is used instead of "saltelli")</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Valid batch experiment
 * experiment my_batch type: batch {
 *     method morris sample_size: 100 nb_levels: 4;
 *     parameter "param1" var: p1 min: 0 max: 10;
 *     until: cycle > 1000;
 * }
 *
 * // Invalid - will trigger error
 * experiment bad_gui type: gui {
 *     method morris;  // Error: gui experiments cannot define exploration methods
 * }
 * }</pre>
 *
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 *
 * @see IDescriptionValidator
 * @see IExperimentDescription
 * @see IExploration
 */
public class BatchValidator implements IDescriptionValidator {

	/**
	 * Validates a batch experiment description and its exploration configuration.
	 *
	 * <p>
	 * This method performs multi-level validation:
	 * </p>
	 * <ol>
	 * <li>Validates the experiment type is recognized</li>
	 * <li>Checks for conflicting features (non-batch with methods, batch with record)</li>
	 * <li>Validates exploration method configuration if present</li>
	 * <li>Ensures batch experiments have stopping conditions</li>
	 * </ol>
	 *
	 * <p>
	 * The validation process adapts based on the experiment type and configured features, providing specific guidance
	 * for each exploration method's requirements.
	 * </p>
	 *
	 * @param desc
	 *            the experiment description to validate (must not be null)
	 *
	 * @see IExperimentDescription#getExperimentTypes
	 * @see IExploration
	 */
	@Override
	public void validate(final IDescription desc) {
		final String type = desc.getLitteral(TYPE);

		if (!IExperimentDescription.getExperimentTypes().contains(type)) {
			desc.error("The type of the experiment must belong to " + IExperimentDescription.getExperimentTypes());
			return;
		}

		if (!BATCH.equals(type) && desc.getChildWithKeyword(METHOD) != null) {
			desc.error(type + " experiments cannot define exploration methods", IGamlIssue.CONFLICTING_FACETS, METHOD);
		}

		if (BATCH.equals(type) && desc.hasFacet(RECORD)) {
			desc.warning("Batch experiments cannot be recorded and played backwards. 'record' will be ignored",
					IGamlIssue.CONFLICTING_FACETS, "ignored" + RECORD);
			desc.setFacetExprDescription("ignored" + RECORD, desc.getFacet(RECORD));
			desc.removeFacets(RECORD);
		}

		if (desc.getChildWithKeyword(EXPLORATION) != null) {

			IDescription tmpDesc = desc.getChildWithKeyword(EXPLORATION);
			if (tmpDesc.hasFacet(IKeyword.BATCH_VAR_OUTPUTS)) {
				IExpression xp = tmpDesc.getFacet(IKeyword.BATCH_VAR_OUTPUTS).getExpression();
				if (!(xp instanceof IExpression.List list)) {
					desc.error(IKeyword.BATCH_VAR_OUTPUTS + " expects a list of variables");
				}
				if (!tmpDesc.hasFacet(IKeyword.BATCH_OUTPUT)) {
					desc.warning(
							"Facet " + IKeyword.BATCH_OUTPUT
									+ " is undefined. Output will be saved in a default file beside this .gaml file",
							"");
				}
			}

			if (tmpDesc.hasFacet(IExploration.SAMPLE_SIZE)) {
				int samples = Integer.parseInt(tmpDesc.getLitteral(IExploration.SAMPLE_SIZE));
				if (samples < 1) { desc.error("Sampling must be a positive integer !"); }
			}

			if (tmpDesc.hasFacet(IExploration.SAMPLING)) {

				switch (tmpDesc.getLitteral(IExploration.SAMPLING)) {

					case IKeyword.MORRIS:
						if (!tmpDesc.hasFacet(IExploration.NB_LEVELS)) {
							tmpDesc.warning("Levels are not defined for Morris sampling, will be 4 by default",
									IGamlIssue.MISSING_FACET);
						} else {
							int levels = Integer.parseInt(tmpDesc.getLitteral(IExploration.NB_LEVELS));
							if (levels <= 0) { tmpDesc.error("Levels should be positive"); }
						}
						if (!tmpDesc.hasFacet(IExploration.SAMPLE_SIZE)) {
							tmpDesc.warning("Sample size not defined, will be 132 by default",
									IGamlIssue.MISSING_FACET);
						} else {
							int sample = Integer.parseInt(tmpDesc.getLitteral(IExploration.SAMPLE_SIZE));
							if (sample % 2 != 0) { tmpDesc.error("The sample size should be even"); }

						}
						break;
					case IKeyword.SALTELLI:
						if (!tmpDesc.hasFacet(IExploration.SAMPLE_SIZE)) {
							tmpDesc.warning("Sample size not defined, will be 132 by default",
									IGamlIssue.MISSING_FACET);
						} else {
							// int sample=
							// Integer.valueOf(tmpDesc.getLitteral(Exploration.SAMPLE_SIZE));
							/*
							 * if(!((sample & (sample-1)) ==0)) { tmpDesc.
							 * error("The sample size should be a power of 2"); }
							 */

						}
						if (tmpDesc.hasFacet(IExploration.NB_LEVELS)) {
							tmpDesc.warning("Saltelli sampling doesn't need the levels facet",
									IGamlIssue.MISSING_FACET);
						}
						break;

					case IKeyword.LHS:
						if (!tmpDesc.hasFacet(IExploration.SAMPLE_SIZE)) {
							tmpDesc.warning("Sample size not defined, will be 132 by default",
									IGamlIssue.MISSING_FACET);
						}
						break;

					case IKeyword.ORTHOGONAL:
						if (!tmpDesc.hasFacet(IExploration.SAMPLE_SIZE)) {
							tmpDesc.warning("Sample size not defined, will be 132 by default",
									IGamlIssue.MISSING_FACET);
						}
						if (!tmpDesc.hasFacet(IExploration.ITERATIONS)) {
							tmpDesc.warning("Number of Iterations not defined, will be 5 by default",
									IGamlIssue.MISSING_FACET);
						}
						break;

					case IKeyword.SOBOL:
						tmpDesc.warning(
								"The sampling " + tmpDesc.getLitteral(IExploration.SAMPLING)
										+ " doesn't exist yet, do you perhaps mean 'saltelli' ?",
								IGamlIssue.MISSING_FACET);
						break;
					case IKeyword.UNIFORM:
						if (!tmpDesc.hasFacet(IExploration.SAMPLE_SIZE)) {
							tmpDesc.warning("Sample size not defined, will be 132 by default",
									IGamlIssue.MISSING_FACET);
						}
						break;
					case IKeyword.FACTORIAL:
						if (!tmpDesc.hasFacet(IExploration.SAMPLE_SIZE)) {
							tmpDesc.warning("Sample size not defined, will be 132 by default",
									IGamlIssue.MISSING_FACET);
						}
						if (!tmpDesc.hasFacet(IExploration.SAMPLE_FACTORIAL)) {
							tmpDesc.warning("If no factorial design is defined, it will be "
									+ "approximated according to sample size and equi-distribution of values per parameters",
									IGamlIssue.MISSING_FACET);
						}
						break;
					default:
						tmpDesc.error("The sampling " + tmpDesc.getLitteral(IExploration.SAMPLING) + " doesn't exist",
								IGamlIssue.MISSING_FACET);
				}
			}

		}
		if (BATCH.equals(type) && !desc.hasFacet(UNTIL)) {
			desc.warning(
					"No stopping condition have been defined (facet 'until:'). This may result in an endless run of the "
							+ type + " experiment",
					IGamlIssue.MISSING_FACET, desc.getUnderlyingElement(), UNTIL, "true");
		}
	}
}