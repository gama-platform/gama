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
 * The Class BatchValidator.
 */
public class BatchValidator implements IDescriptionValidator {

	/**
	 * Method validate()
	 *
	 * @see gama.api.compilation.descriptions.IDescriptionValidator#validate(gama.api.compilation.descriptions.IDescription)
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

			if (tmpDesc.hasFacet(IExploration.METHODS)) {

				switch (tmpDesc.getLitteral(IExploration.METHODS)) {

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
								"The sampling " + tmpDesc.getLitteral(IExploration.METHODS)
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
						tmpDesc.error("The sampling " + tmpDesc.getLitteral(IExploration.METHODS) + " doesn't exist",
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