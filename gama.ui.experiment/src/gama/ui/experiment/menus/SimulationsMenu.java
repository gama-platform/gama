/*******************************************************************************************************
 *
 * SimulationsMenu.java, in gama.ui.experiment, is part of the source code of the
 * GAMA modeling and simulation platform (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.ui.experiment.menus;

import java.io.IOException;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import gama.core.common.interfaces.ISerialisationConstants;
import gama.core.common.preferences.GamaPreferences;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.kernel.experiment.ParametersSet;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.runtime.GAMA;
import gama.extension.serialize.binary.BinarySerialisation;
import gama.gaml.constants.GamlCoreConstants;
import gama.ui.experiment.commands.ArrangeDisplayViews;
import gama.ui.shared.menus.GamaMenu;
import gama.ui.shared.utils.WorkbenchHelper;
import gama.ui.shared.views.toolbar.GamaCommand;

/**
 * The Class SimulationsMenu.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 18 août 2023
 */
public class SimulationsMenu extends ContributionItem {

	/** The create new simulation. */
	public static final GamaCommand addNewSimulation = new GamaCommand("experiment/experiment.simulations.add",
			"Add Simulation", "Add a new simulation (with the current parameters) to this experiment", e -> {
				final SimulationAgent sim = GAMA.getExperiment().getAgent().createSimulation(new ParametersSet(), true);
				if (sim == null) return;
				WorkbenchHelper.runInUI("", 0, m -> {
					if ("None".equals(GamaPreferences.Displays.CORE_DISPLAY_LAYOUT.getValue())) {
						ArrangeDisplayViews.execute(GamlCoreConstants.split);
					} else {
						ArrangeDisplayViews.execute(GamaPreferences.Displays.LAYOUTS
								.indexOf(GamaPreferences.Displays.CORE_DISPLAY_LAYOUT.getValue()));
					}
				});
			});

	/** The duplicate current simulation. */
	public static final GamaCommand duplicateCurrentSimulation =
			new GamaCommand("experiment/experiment.simulations.duplicate", "Duplicate Simulation",
					"Duplicate the current simulation and add it to the experiment", e -> {
						byte[] bytes =
								BinarySerialisation.saveToBytes(GAMA.getRuntimeScope(), GAMA.getSimulation(), true);
						final SimulationAgent sim =
								GAMA.getExperiment().getAgent().createSimulation(new ParametersSet(), true);
						GAMA.runAndUpdateAll(() -> {
							try {
								BinarySerialisation.restoreFromBytes(sim, bytes);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						});
					});

	/** The kill current simulation. */
	public static final GamaCommand killCurrentSimulation = new GamaCommand("experiment/experiment.simulations.kill",
			"Kill Simulation", "Kill and remove the current simulation from the experiment", e -> {
				SimulationAgent sim = GAMA.getSimulation();
				if (sim == null) return;
				sim.primDie(sim.getScope());
			});

	/** The save current simulation. */
	public static final GamaCommand saveCurrentSimulation = new GamaCommand("experiment/experiment.simulations.save",
			"Save Simulation...", "Save the current simulation to disk", e -> {
				SimulationAgent sim = GAMA.getSimulation();
				if (sim == null) return;
				FileDialog fileSave = new FileDialog(e.display.getActiveShell(), SWT.SAVE);
				fileSave.setFilterNames(new String[] {
						"Simulation files"/*
											 * SerialisationConstants.BINARY_FORMAT ,
											 * SerialisationConstants.JSON_FORMAT, SerialisationConstants.XML_FORMAT
											 */ });
				fileSave.setFilterExtensions(
						new String[] { "*.simulation"/* , "*.simulation; *.json", "*.simulation; *.xml" */ });
				fileSave.setFileName(
						sim.getModel().getName() + "_cycle" + sim.getCycle(sim.getScope()) + ".simulation");
				String open = fileSave.open();
				// String format = switch (fileSave.getFilterIndex()) {
				// case 1 -> SerialisationConstants.JSON_FORMAT;
				// case 2 -> SerialisationConstants.XML_FORMAT;
				// default -> SerialisationConstants.BINARY_FORMAT;
				// };

				BinarySerialisation.saveToFile(sim.getScope(), sim, open, ISerialisationConstants.BINARY_FORMAT, true,
						false);
			});

	/** The save current simulation and history. */
	public static final GamaCommand saveCurrentSimulationAndHistory =
			new GamaCommand("experiment/experiment.simulations.save.history", "Save Simulation with its History...",
					"Save the current simulation and its history to disk", e -> {
						SimulationAgent sim = GAMA.getSimulation();
						if (sim == null) return;
						FileDialog fileSave = new FileDialog(e.display.getActiveShell(), SWT.SAVE);
						// Only binary allowed when saving history
						fileSave.setFilterNames(new String[] { ISerialisationConstants.BINARY_FORMAT });
						fileSave.setFilterExtensions(new String[] { "*.simulation" });
						fileSave.setFileName(sim.getModel().getName() + "_" + sim.getCycle(sim.getScope()) + "_cycles"
								+ ".simulation");
						String open = fileSave.open();
						BinarySerialisation.saveToFile(sim.getScope(), sim, open, ISerialisationConstants.BINARY_FORMAT,
								true, true);
					});

	/** The replace current simulation. */
	public static final GamaCommand replaceCurrentSimulation =
			new GamaCommand("experiment/experiment.simulations.load.replace", "Load and Replace Simulation...",
					"Load a previously saved simulation and replace the current one", e -> {

						SimulationAgent sim = GAMA.getSimulation();
						if (sim == null) return;
						FileDialog fileOpen = new FileDialog(e.display.getActiveShell(), SWT.OPEN);
						fileOpen.setFilterExtensions(new String[] { "*.simulation" });
						fileOpen.setFilterNames(new String[] { "Simulation files" });
						String open = fileOpen.open();
						if (open != null) {
							GAMA.runAndUpdateAll(() -> { BinarySerialisation.restoreFromFile(sim, open); });
							GAMA.changeCurrentTopLevelAgent(sim, true);
						}
					});

	/** The load new simulation. */
	public static final GamaCommand loadNewSimulation = new GamaCommand("experiment/experiment.simulations.load.new",
			"Load New Simulation...", "Load a previously saved simulation and add it to the experiment", e -> {
				FileDialog fileOpen = new FileDialog(e.display.getActiveShell(), SWT.OPEN);
				fileOpen.setFilterExtensions(new String[] { "*.simulation" });
				fileOpen.setFilterNames(new String[] { "Simulation files" });
				String open = fileOpen.open();
				if (open != null) {
					final SimulationAgent sim =
							GAMA.getExperiment().getAgent().createSimulation(new ParametersSet(), true);
					BinarySerialisation.restoreFromFile(sim, open);
				}

			});

	/**
	 * Instantiates a new simulations menu.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @date 18 août 2023
	 */
	public SimulationsMenu() {
		this("gama.ui.application.simulations.menu");
	}

	/**
	 * Instantiates a new simulations menu.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param id
	 *            the id
	 * @date 18 août 2023
	 */
	public SimulationsMenu(final String id) {
		super(id);
	}

	@Override
	public void fill(final Menu menu, final int index) {

		MenuItem add = SimulationsMenu.addNewSimulation.toItem(menu);
		MenuItem kill = SimulationsMenu.killCurrentSimulation.toItem(menu);
		MenuItem duplicate = SimulationsMenu.duplicateCurrentSimulation.toItem(menu);
		GamaMenu.separate(menu);
		MenuItem save = SimulationsMenu.saveCurrentSimulation.toItem(menu);
		MenuItem saveHistory = SimulationsMenu.saveCurrentSimulationAndHistory.toItem(menu);
		MenuItem loadAndReplace = SimulationsMenu.replaceCurrentSimulation.toItem(menu);
		MenuItem loadNew = SimulationsMenu.loadNewSimulation.toItem(menu);
		boolean isExperiment = GAMA.getCurrentTopLevelAgent() instanceof IExperimentAgent;
		boolean isSimulation = GAMA.getCurrentTopLevelAgent() instanceof SimulationAgent;
		boolean isBackward = isSimulation && GAMA.getExperiment() != null && GAMA.getExperiment().isMemorize();
		add.setEnabled(isExperiment || isSimulation);
		kill.setEnabled(isSimulation);
		duplicate.setEnabled(isSimulation);
		save.setEnabled(isSimulation);
		saveHistory.setEnabled(isBackward);
		loadAndReplace.setEnabled(isSimulation);
		loadNew.setEnabled(isExperiment || isSimulation);
	}

	@Override
	public boolean isDynamic() { return true; }

}
