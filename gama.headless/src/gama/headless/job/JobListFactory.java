/*******************************************************************************************************
 *
 * JobListFactory.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.job;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gama.api.compilation.descriptions.IExperimentDescription;
import gama.api.constants.IKeyword;
import gama.api.exceptions.GamaCompilationFailedException;
import gama.api.gaml.GAML;
import gama.api.gaml.types.Types;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.IModelSpecies;
import gama.headless.core.GamaHeadlessException;
import gaml.compiler.gaml.validation.GamlModelBuilder;

/**
 * The Class JobPlan.
 */
public class JobListFactory {

	/**
	 * The JobPlanExperimentID.
	 */
	public record JobPlanExperimentID(String modelName, String experimentName) {}

	/**
	 * Construct all jobs.
	 *
	 * @param seeds
	 *            the seeds
	 * @param finalStep
	 *            the final step
	 * @return the list
	 * @throws GamaHeadlessException
	 * @throws IOException
	 */
	public static List<IExperimentJob> constructAllJobs(final String modelPath, final long[] seeds,
			final long finalStep, final Integer numberOfCores) throws IOException, GamaCompilationFailedException {
		IModelSpecies model = GamlModelBuilder.getInstance().compile(new File(modelPath), null, null);
		Map<JobPlanExperimentID, IExperimentJob> originalJobs = new LinkedHashMap<>();
		if (numberOfCores != null && numberOfCores > 0) {
			for (IExperimentSpecies exp : model.getExperiments()) {
				exp.setConcurrency(GAML.getExpressionFactory().createConst(numberOfCores, Types.INT));
			}
		}
		for (final IExperimentDescription expD : model.getDescription().getExperiments()) {
			if (!IKeyword.BATCH.equals(expD.getLitteral(IKeyword.TYPE))) {
				final IExperimentJob tj = ExperimentJob.loadAndBuildJob(expD, model.getFilePath(), model);
				// TODO AD Why 12 ??
				tj.setSeed(12);
				originalJobs.put(new JobPlanExperimentID(tj.getModelName(), tj.getExperimentName()), tj);
			}
		}
		final List<IExperimentJob> jobs = new ArrayList<>();
		for (final IExperimentJob locJob : originalJobs.values()) {
			jobs.addAll(constructJobWithName(locJob, seeds, finalStep, null, null));
		}
		return jobs;
	}

	/**
	 * Construct job with name.
	 *
	 * @param originalExperiment
	 *            the original experiment
	 * @param seeds
	 *            the seeds
	 * @param finalStep
	 *            the final step
	 * @param in
	 *            the in
	 * @param out
	 *            the out
	 * @return the list
	 */
	private static List<IExperimentJob> constructJobWithName(final IExperimentJob originalExperiment,
			final long[] seeds, final long finalStep, final List<Parameter> in, final List<Output> out) {
		final List<IExperimentJob> res = new ArrayList<>();
		for (final long sd : seeds) {
			final IExperimentJob job = new ExperimentJob((ExperimentJob) originalExperiment);
			job.setSeed(sd);
			job.setFinalStep(finalStep);
			if (in != null) { for (final Parameter p : in) { job.setParameterValueOf(p.getName(), p.getValue()); } }
			if (out != null) {
				final List<String> availableOutputs = job.getOutputNames();
				for (final Output o : out) {
					job.setOutputFrameRate(o.getName(), o.getFrameRate());
					availableOutputs.remove(o.getName());
				}
				for (final String s : availableOutputs) { job.removeOutputWithName(s); }
			}

			res.add(job);
		}
		return res;
	}

}
