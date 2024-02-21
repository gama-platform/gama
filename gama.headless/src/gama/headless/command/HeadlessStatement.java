/*******************************************************************************************************
 *
 * HeadlessStatement.java, in gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.headless.command;

import java.io.File;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.descriptions.IDescription;
import gama.gaml.operators.Cast;
import gama.gaml.statements.AbstractStatement;
import gama.gaml.types.IType;
import gama.headless.job.ExperimentJob;
import gama.headless.runtime.SimulationRuntime;

/**
 * The Class HeadlessStatement.
 */
@symbol (
		name = IKeywords.RUNSIMULARTION,
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		doc = @doc ("Allows to run an experiment on a different model"),
		concept = { IConcept.HEADLESS })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SINGLE_STATEMENT, ISymbolKind.SPECIES, ISymbolKind.MODEL })
@facets (
		value = { @facet (
				name = IKeywords.MODEL,
				type = IType.STRING,
				optional = false,
				doc = @doc ("Indicates the model containing the experiment to run")),
				@facet (
						name = IKeywords.EXPERIMENT,
						type = IType.STRING,
						optional = false,
						doc = @doc ("Indicates the name of the experiment to run")),
				@facet (
						name = IKeywords.END,
						type = IType.INT,
						optional = true,
						doc = @doc ("Indicates the cycle at which the experiment should stop")),
				@facet (
						name = IKeywords.CORE,
						type = IType.INT,
						optional = true,
						doc = @doc ("Indicates the number of cores to use to run the experiments")),
				@facet (
						name = IKeywords.WITHSEED,
						type = IType.INT,
						optional = true,
						doc = @doc ("Provides a predetermined seed instead of letting GAMA choose one")),
				@facet (
						name = IKeywords.WITHOUTPUTS,
						type = IType.MAP,
						doc = @doc ("<i>This needs to be docummented</i>"),
						optional = true),
				@facet (
						name = IKeywords.WITHPARAMS,
						type = IType.MAP,
						doc = @doc ("The parameters to pass to the new experiment"),
						optional = true) },
		omissible = IKeywords.EXPERIMENT)
public class HeadlessStatement extends AbstractStatement {

	/** The processor queue. */
	private final SimulationRuntime processorQueue;

	/** The max simulation ID. */
	private int maxSimulationID = 0;

	/**
	 * Gets the simulation id.
	 *
	 * @return the simulation id
	 */
	public String getSimulationId() { return String.valueOf(maxSimulationID++); }

	/**
	 * Instantiates a new headless statement.
	 *
	 * @param desc
	 *            the desc
	 */
	public HeadlessStatement(final IDescription desc) {
		super(desc);
		processorQueue = new SimulationRuntime();
	}

	/**
	 * Retrieve model file absolute path.
	 *
	 * @param scope
	 *            the scope
	 * @param filename
	 *            the filename
	 * @return the string
	 */
	private String retrieveModelFileAbsolutePath(final IScope scope, final String filename) {
		if (filename.charAt(0) == '/') return filename;
		return new File(scope.getModel().getFilePath()).getParentFile().getAbsolutePath() + "/" + filename;
	}

	@Override
	protected Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {

		int seed = 0;
		final String expName = Cast.asString(scope, getFacetValue(scope, IKeywords.EXPERIMENT));
		String modelPath = Cast.asString(scope, getFacetValue(scope, IKeywords.MODEL));
		if (modelPath != null && !modelPath.isEmpty()) {
			modelPath = retrieveModelFileAbsolutePath(scope, modelPath);
		} else {
			// no model specified, this caller model path is used.
			modelPath = scope.getModel().getFilePath();
		}

		// final GamaMap<String, ?> outputs = Cast.asMap(scope, getFacetValue(scope, IKeywords.WITHOUTPUTS), false);

		if (this.hasFacet(IKeywords.WITHSEED)) { seed = Cast.asInt(scope, getFacetValue(scope, IKeywords.WITHSEED)); }

		final long lseed = seed;

		// DEBUG.OUT("chemin du fichier" + new File(scope.getModel().getFilePath()).getParentFile().getAbsolutePath());

		final ExperimentJob sim = new ExperimentJob(this.getSimulationId(), modelPath, expName, 1000, "", lseed);

		this.processorQueue.execute(sim);

		return null;
	}

}
