/*******************************************************************************************************
 *
 * ISimulationAgent.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.kernel.simulation;

import java.util.Map;

import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.metamodel.topology.projection.ProjectionFactory;
import gama.core.outputs.IOutput;
import gama.core.runtime.IScope;
import gama.core.runtime.concurrent.SimulationLocal;
import gama.core.util.GamaDate;

/**
 *
 */
public interface ISimulationAgent extends ITopLevelAgent {

	/**
	 * @return
	 */
	GamaDate getCurrentDate();

	/**
	 * @return
	 */
	ProjectionFactory getProjectionFactory();

	/**
	 * @param scope
	 * @return
	 */
	double getTimeStep(IScope scope);

	/**
	 * @return
	 */
	GamaDate getStartingDate();

	/**
	 * @return
	 */
	boolean isMicroSimulation();

	/**
	 * @return
	 */
	Double getSeed();

	/**
	 * @param monitorOutput
	 */
	void addOutput(IOutput monitorOutput);

	/**
	 * @return
	 */
	<T> Map<SimulationLocal<T>, T> getSimulationLocalMap();

	/**
	 * @param map
	 */
	<T> void setSimulationLocalMap(Map<SimulationLocal<T>, T> map);

}
