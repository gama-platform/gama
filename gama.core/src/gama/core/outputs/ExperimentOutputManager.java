/*******************************************************************************************************
 *
 * ExperimentOutputManager.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs;

import static gama.annotations.constants.IKeyword.LAYOUT;
import static gama.api.utils.prefs.GamaPreferences.Displays.CORE_DISPLAY_LAYOUT;
import static gama.api.utils.prefs.GamaPreferences.Displays.LAYOUTS;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.annotations.inside;
import gama.annotations.symbol;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.GAMA;
import gama.api.compilation.descriptions.IDescription;
import gama.api.gaml.GAML;
import gama.api.gaml.symbols.Symbol;
import gama.api.gaml.types.IType;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IOutput;
import gama.api.utils.prefs.GamaPreferences;
import gama.dev.DEBUG;

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
@facets ({ @facet (
		name = "synchronized",
		type = IType.BOOL,
		optional = true,
		doc = @doc (
				value = "Indicates whether the displays that compose this output should be synchronized with the simulation cycles")),
		@facet (
				name = IKeyword.AUTOSAVE,
				type = { IType.BOOL, IType.STRING },
				optional = true,
				doc = @doc ("Allows to save the whole screen on disk. A value of true/false will save it with the resolution of the physical screen. Passing it a string allows to define the filename "
						+ "Note that setting autosave to true (or to any other value than false) will synchronize all the displays defined in the experiment")) })
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
		return new ExperimentOutputManager(GAML.getDescriptionFactory().create(IKeyword.PERMANENT, (String[]) null));
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
		// Delegate to openAndApplyLayout so the GUI can freeze the shell, open all display views
		// and run the layout algorithm in one atomic UI call — eliminating intermediate repaints.
		scope.getGui().openAndApplyLayout(scope, () -> super.init(scope), layoutObject);
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
	public void dispose() {
		GAMA.getGui().cleanAfterExperiment();
		super.dispose();
	}

}
