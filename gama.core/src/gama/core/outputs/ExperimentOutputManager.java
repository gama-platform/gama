/*******************************************************************************************************
 *
 * ExperimentOutputManager.java, in gama.core, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs;

import static gama.core.common.interfaces.IKeyword.LAYOUT;
import static gama.core.common.preferences.GamaPreferences.Displays.CORE_DISPLAY_LAYOUT;
import static gama.core.common.preferences.GamaPreferences.Displays.LAYOUTS;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.preferences.GamaPreferences;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.dev.DEBUG;
import gama.gaml.compilation.Symbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.factories.DescriptionFactory;

/**
 * The Class OutputManager.
 *
 * @author Alexis Drogoul modified by Romain Lavaud 05.07.2010
 */
@symbol (
		name = IKeyword.PERMANENT,
		kind = ISymbolKind.OUTPUT,
		with_sequence = true,
		unique_in_context = true,
		concept = { IConcept.BATCH, IConcept.DISPLAY })

@inside (
		kinds = { ISymbolKind.EXPERIMENT })
@doc (
		value = "Represents the outputs of the experiment itself. In a batch experiment, the permanent section allows to define an output block that will NOT be re-initialized at the beginning of each simulation but will be filled at the end of each simulation.",
		usages = { @usage (
				value = "For instance, this permanent section will allow to display for each simulation the end value of the food_gathered variable:",
				examples = { @example (
						value = "permanent {",
						isExecutable = false),
						@example (
								value = "	display Ants background: rgb('white') refresh_every: 1 {",
								isExecutable = false),
						@example (
								value = "		chart \"Food Gathered\" type: series {",
								isExecutable = false),
						@example (
								value = "			data \"Food\" value: food_gathered;",
								isExecutable = false),
						@example (
								value = "		}",
								isExecutable = false),
						@example (
								value = "	}",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }) })
public class ExperimentOutputManager extends AbstractOutputManager {

	static {
		DEBUG.OFF();
	}

	/**
	 * Creates the empty.
	 *
	 * @return the experiment output manager
	 */
	public static ExperimentOutputManager createEmpty() {
		return new ExperimentOutputManager(DescriptionFactory.create(IKeyword.PERMANENT, (String[]) null));
	}

	/**
	 * Instantiates a new experiment output manager.
	 *
	 * @param desc
	 *            the desc
	 */
	public ExperimentOutputManager(final IDescription desc) {
		super(desc);
	}

	@Override
	public boolean init(final IScope scope) {
		final Symbol layoutDefinition = layout == null ? this : layout;
		final String definitionFacet = layout == null ? LAYOUT : IKeyword.VALUE;
		final Object layoutObject =
				layoutDefinition.getFacetValue(scope, definitionFacet, LAYOUTS.indexOf(CORE_DISPLAY_LAYOUT.getValue()));
		super.init(scope);
		scope.getGui().applyLayout(scope, layoutObject);
		if (GamaPreferences.Runtime.CORE_MONITOR_PARAMETERS.getValue()) { GAMA.getGui().updateParameterView(scope); }
		return true;
	}

	@Override
	public boolean step(final IScope scope) {
		super.step(scope);
		if (GamaPreferences.Runtime.CORE_MONITOR_PARAMETERS.getValue()) { GAMA.getGui().updateParameterView(scope); }
		return true;
	}

	// We don't allow permanent outputs for batch experiments to do their first step (to fix Issue
	// #1273) -- Conflicts with Issue #2204
	@Override
	protected boolean initialStep(final IScope scope, final IOutput output) {
		if (scope.getExperiment().getSpecies().isBatch()) return true;
		return super.initialStep(scope, output);
	}

	@Override
	public void add(final IOutput output) {
		((AbstractOutput) output).setPermanent();
		super.add(output);
	}

	@Override
	public synchronized void dispose() {
		GAMA.getGui().cleanAfterExperiment();
		super.dispose();
	}

}
