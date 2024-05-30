package gama.core.runtime.concurrent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

import gama.core.kernel.simulation.SimulationAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.exceptions.GamaRuntimeException;

public class WriteController {
	
	public enum BufferingStrategies{
		NO_BUFFERING,
		PER_CYCLE_BUFFERING,
		PER_SIMULATION_BUFFERING
	}


	protected Map<String, Map<SimulationAgent, StringBuilder>> fileWritingPerSimulationMap;
	protected Map<String, Map<SimulationAgent, StringBuilder>> fileWritingPerCycleMap;
	
	public WriteController() {
		fileWritingPerSimulationMap = new HashMap<>();
		fileWritingPerCycleMap = new HashMap<>();
	}
	
	public boolean askWrite(String fileId, SimulationAgent owner, CharSequence content, BufferingStrategies bs, boolean append) {
		switch (bs) {
			case PER_SIMULATION_BUFFERING:
				return appendWriteSimulation(fileId, owner, content, append);
			case PER_CYCLE_BUFFERING:
				return appendWriteCycle(fileId, owner, content, append);
			case NO_BUFFERING:
				return directWrite(fileId, content, append);
			default:
				throw GamaRuntimeException.create(new NotImplementedException("This buffering strategie has not been implemented yet: " + bs.toString()), owner.getScope());
		}
	}
	
	protected boolean appendWriteRequestToMap(String fileId, SimulationAgent owner, CharSequence content, Map<String, Map<SimulationAgent, StringBuilder>> map, boolean append) {
		// If we don't have any map for this file yet we create one
		Map<SimulationAgent, StringBuilder> fileSavingAsksMap = map.get(fileId);
		if (fileSavingAsksMap == null) {
			fileSavingAsksMap = new HashMap<>();
			map.put(fileId, fileSavingAsksMap);
		}
		
		// We look up for the previous write request of the owner simulation in the map
		// if there's already one we append our content or rewrite, depending on the append parameter
		// else we create one with the content as its initial value
		StringBuilder askRequest = fileSavingAsksMap.get(owner);
		if (askRequest == null) {
			try {
				fileSavingAsksMap.put(owner, new StringBuilder(content));	
				return true;
			}
			catch(Exception ex) {
				GAMA.reportError(owner.getScope(), GamaRuntimeException.create(ex, owner.getScope()), false);
				return false;
			}
		}
		else {
			// If we are not in append mode, we empty the buffer
			if (!append) {
				askRequest.setLength(0);
			}
			askRequest.append(content);
			return true;
		}
	}
	
	protected boolean appendWriteSimulation(String fileId, SimulationAgent owner, CharSequence content, boolean append) {
		return appendWriteRequestToMap(fileId, owner, content, fileWritingPerSimulationMap, append);
	}

	protected boolean appendWriteCycle(String fileId, SimulationAgent owner, CharSequence content, boolean append) {
		return appendWriteRequestToMap(fileId, owner, content, fileWritingPerCycleMap, append);
	}
	protected boolean directWrite(String fileId, CharSequence content, boolean append ) {
		// TODO: should we always use utf8 ?
		try (FileWriter fr = new FileWriter(new File(fileId), StandardCharsets.UTF_8,append )){
			fr.append(content);
			fr.flush();
			fr.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Flushes all the save requests made by a simulation saved in a map
	 * @param owner: the simulation in which the save statements have been executed
	 * @return true if everything went well, false in case of error
	 */
	protected boolean flushMapOwner(SimulationAgent owner, Map<String, Map<SimulationAgent, StringBuilder>> map) {
		boolean success = true;
		for(var entry : map.entrySet()) {
			var cumulatedContent = entry.getValue().get(owner);
			if (cumulatedContent != null) {
				var writeSuccess = directWrite(entry.getKey(), cumulatedContent, true);
				// we don't return false directly because we try to flush as much files as possible
				success &= writeSuccess;
				// if the write was successful we remove the operation from the map 
				if (writeSuccess) {
					entry.getValue().remove(owner);
				}
			}
		}
		return success;
	}
	/**
	 * Flushes all the save requests made by a simulation with the 'per_simulation_buffering' strategy
	 * @param owner: the simulation in which the save statements have been executed
	 * @return true if everything went well, false in case of error
	 */
	public boolean flushSimulationOwner(SimulationAgent owner) {
		return flushMapOwner(owner, fileWritingPerSimulationMap);		
	}
	
	/**
	 * Flushes all the save requests made by a simulation with the 'per_cycle_buffering' strategy
	 * @param owner: the simulation in which the save statements have been executed
	 * @return true if everything went well, false in case of error
	 */
	public boolean flushCycleOwner(SimulationAgent owner) {
		return flushMapOwner(owner, fileWritingPerCycleMap);
	}
	
}
