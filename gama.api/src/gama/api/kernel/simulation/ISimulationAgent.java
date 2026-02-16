/*******************************************************************************************************
 *
 * ISimulationAgent.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.kernel.simulation;

import java.util.Map;

import gama.api.kernel.topology.IProjectionFactory;
import gama.api.runtime.scope.IScope;
import gama.api.types.date.IDate;
import gama.api.ui.IOutput;
import gama.api.ui.IOutputManager;
import gama.api.utils.SimulationLocal;
import gama.api.utils.random.IRandom;

/**
 *
 */
public interface ISimulationAgent extends ITopLevelAgent {

	/** The Constant DURATION. */
	String DURATION = "duration";

	/** The Constant TOTAL_DURATION. */
	String TOTAL_DURATION = "total_duration";

	/** The Constant AVERAGE_DURATION. */
	String AVERAGE_DURATION = "average_duration";

	/** The Constant TIME. */
	String TIME = "time";

	/** The Constant CURRENT_DATE. */
	String CURRENT_DATE = "current_date";

	/** The Constant STARTING_DATE. */
	String STARTING_DATE = "starting_date";

	/** The Constant PAUSED. */
	String PAUSED = "paused";

	/** The Constant USAGE. */
	String USAGE = "rng_usage";

	/**
	 * @return
	 */
	IDate getCurrentDate();

	/**
	 * @return
	 */
	IProjectionFactory getProjectionFactory();

	/**
	 * @param scope
	 * @return
	 */
	double getTimeStep(IScope scope);

	/**
	 * @return
	 */
	IDate getStartingDate();

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

	/**
	 * @param toBeScheduled
	 */
	void setScheduled(boolean toBeScheduled);

	/**
	 * @param originalSimulationOutputs
	 */
	void setOutputs(IOutputManager originalSimulationOutputs);

	/**
	 *
	 */
	void initOutputs();

	/**
	 * @param simulation
	 */
	void adoptTopologyOf(ISimulationAgent simulation);

	/**
	 * @param firstInitValues
	 */
	void setExternalInits(Map<String, Object> firstInitValues);

	/**
	 * @param randomUtils
	 */
	void setRandomGenerator(IRandom randomUtils);

	/**
	 * Generate random generator.
	 *
	 * @param seed
	 *            the seed
	 * @param rng
	 *            the rng
	 * @return the i random
	 */
	IRandom generateRandomGenerator(final Double seed, final String rng);

	/**
	 * @return
	 */
	Map<String, Object> getExternalInits();

	/**
	 * @param scope
	 * @return
	 */
	Integer getCycle(IScope scope);

	/**
	 * @return
	 */
	boolean getScheduled();

	/**
	 * @param scope
	 */
	Object _init_(IScope scope);

	/**
	 * @param double1
	 */
	void setSeed(Double double1);

	/**
	 * @param usageValue
	 */
	void setUsage(Integer usageValue);

	/**
	 * @return
	 */
	String getRng();

	/**
	 * @return
	 */
	Integer getUsage();

}
