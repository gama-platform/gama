/*******************************************************************************************************
 *
 * SimulationOutputManager.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs;

import java.util.HashMap;
import java.util.Map;

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
import gama.api.annotations.validator;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.constants.IGamlIssue;
import gama.api.gaml.GAML;
import gama.api.gaml.types.IType;
import gama.api.kernel.simulation.ISimulationAgent;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IOutput;
import gama.api.ui.IStatusMessage;
import gama.core.outputs.SimulationOutputManager.SimulationOutputValidator;

/**
 * The Class OutputManager.
 *
 * @author Alexis Drogoul modified by Romain Lavaud 05.07.2010
 */

/**
 * The Class SimulationOutputManager.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 16 sept. 2023
 */
@symbol (
		name = IKeyword.OUTPUT,
		kind = ISymbolKind.OUTPUT,
		with_sequence = true,
		concept = { IConcept.DISPLAY, IConcept.OUTPUT })
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

@inside (
		kinds = { ISymbolKind.MODEL, ISymbolKind.EXPERIMENT })
@doc (
		value = "`output` blocks define how to visualize a simulation (with one or more display blocks that define separate windows). It will include a set of displays, monitors and files statements. It will be taken into account only if the experiment type is `gui`.",
		usages = { @usage (
				value = "Its basic syntax is: ",
				examples = { @example (
						value = "experiment exp_name type: gui {",
						isExecutable = false),
						@example (
								value = "   // [inputs]",
								isExecutable = false),
						@example (
								value = "   output {",
								isExecutable = false),
						@example (
								value = "      // [display, file, inspect, layout or monitor statements]",
								isExecutable = false),
						@example (
								value = "   }",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }) },
		see = { IKeyword.DISPLAY, IKeyword.MONITOR, IKeyword.INSPECT, IKeyword.LAYOUT })
@validator (SimulationOutputValidator.class)
public class SimulationOutputManager extends AbstractOutputManager {

	/**
	 * The Class SimulationOutputValidator.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 16 sept. 2023
	 */
	public static class SimulationOutputValidator implements IDescriptionValidator<IDescription> {

		@Override
		public void validate(final IDescription description) {
			Iterable<IDescription> displays = description.getChildrenWithKeyword(DISPLAY);
			Map<String, IDescription> map = new HashMap<>();
			for (IDescription display : displays) {
				String s = display.getName();
				if (s == null) { continue; }
				IDescription existing = map.get(s);
				if (existing == null) {
					map.put(s, display);
					continue;
				}

				display.info("'" + s
						+ "' is defined twice in this experiment. Only this definition will be taken into account.",
						IGamlIssue.DUPLICATE_DEFINITION);
				existing.info("'" + s
						+ "' is defined twice in this experiment. This definition will not be taken into account.",
						IGamlIssue.DUPLICATE_DEFINITION);
			}

		}

	}

	/**
	 * Creates the empty.
	 *
	 * @return the simulation output manager
	 */
	public static SimulationOutputManager createEmpty() {
		return new SimulationOutputManager(GAML.getDescriptionFactory().create(IKeyword.OUTPUT, (String[]) null));
	}

	/**
	 * Instantiates a new simulation output manager.
	 *
	 * @param desc
	 *            the desc
	 */
	public SimulationOutputManager(final IDescription desc) {
		super(desc);

	}

	@Override
	public boolean init(final IScope scope) {
		boolean[] result = { true };
		scope.getGui().getStatus().waitStatus(" Building outputs ", IStatusMessage.VIEW_ICON, () -> {
			result[0] = super.init(scope);
			updateDisplayOutputsName(scope.getSimulation());
		});
		return result[0];
	}

	/**
	 * Update display outputs name.
	 *
	 * @param agent
	 *            the agent
	 */
	public void updateDisplayOutputsName(final ISimulationAgent agent) {
		for (final IOutput out : this) { GAMA.getGui().updateViewTitle(out, agent); }
	}

}
