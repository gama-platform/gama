package gama.core.runtime.concurrent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

import gama.core.kernel.simulation.SimulationAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.statements.save.SaveOptions;

public class WriteController {
	
	public enum BufferingStrategies{
		NO_BUFFERING,
		PER_CYCLE_BUFFERING,
		PER_SIMULATION_BUFFERING
	}

	
	public class WriteTask {
		final public StringBuilder content;
		final public Charset encoding;
		protected boolean rewrite;
		
		public WriteTask(final CharSequence initialContent, final Charset encodingType, final boolean rewriteFile) {
			content = new StringBuilder(initialContent);
			encoding = encodingType;
			rewrite = rewriteFile;
		}
		
		public void setRewrite(boolean rewrite) {
			this.rewrite = rewrite;
		}
		public boolean isRewriting() {
			return rewrite;
		}
	}

	protected Map<String, Map<SimulationAgent, WriteTask>> fileWritingPerSimulationMap;
	protected Map<String, Map<SimulationAgent, WriteTask>> fileWritingPerCycleMap;
	
	public WriteController() {
		fileWritingPerSimulationMap = new HashMap<>();
		fileWritingPerCycleMap = new HashMap<>();
	}
	
	public boolean askWrite(String fileId, SimulationAgent owner, CharSequence content, final SaveOptions options) {
		switch (options.bufferingStrategy) {
			case PER_SIMULATION_BUFFERING:
				return appendWriteSimulation(fileId, owner, content, options);
			case PER_CYCLE_BUFFERING:
				return appendWriteCycle(fileId, owner, content, options);
			case NO_BUFFERING:
				return directWrite(fileId, content, options.getCharset(), !options.rewrite);
			default:
				throw GamaRuntimeException.create(new NotImplementedException("This buffering strategie has not been implemented yet: " + options.bufferingStrategy.toString()), owner.getScope());
		}
	}
	
	protected boolean appendWriteRequestToMap(String fileId, SimulationAgent owner, CharSequence content, Map<String, Map<SimulationAgent, WriteTask>> map, final SaveOptions options) {
		// If we don't have any map for this file yet we create one
		Map<SimulationAgent, WriteTask> fileSavingAsksMap = map.get(fileId);
		if (fileSavingAsksMap == null) {
			fileSavingAsksMap = new HashMap<>();
			map.put(fileId, fileSavingAsksMap);
		}
		
		// We look up for the previous write request of the owner simulation in the map
		// if there's already one we append our content or rewrite, depending on the append parameter
		// else we create one with the content as its initial value
		WriteTask askRequest = fileSavingAsksMap.get(owner);
		if (askRequest == null) {
			try {
				fileSavingAsksMap.put(owner, new WriteTask(content, options.getCharset(), options.rewrite));	
				return true;
			}
			catch(Exception ex) {
				GAMA.reportError(owner.getScope(), GamaRuntimeException.create(ex, owner.getScope()), false);
				return false;
			}
		}
		else {
			// If we are not in append mode, we empty the buffer
			if (options.rewrite) {
				askRequest.setRewrite(true);
				askRequest.content.setLength(0);
			}
			askRequest.content.append(content);
			return true;
		}
	}
	
	protected boolean appendWriteSimulation(final String fileId, final SimulationAgent owner, final CharSequence content, final SaveOptions options) {
		return appendWriteRequestToMap(fileId, owner, content, fileWritingPerSimulationMap, options);
	}

	protected boolean appendWriteCycle(final String fileId, final SimulationAgent owner, final CharSequence content, final SaveOptions options) {
		return appendWriteRequestToMap(fileId, owner, content, fileWritingPerCycleMap, options);
	}
	protected boolean directWrite(final String fileId, final CharSequence content, final Charset charset, final boolean append) {
		try (FileWriter fr = new FileWriter(new File(fileId), charset, append)){
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
	protected boolean flushMapOwner(SimulationAgent owner, Map<String, Map<SimulationAgent, WriteTask>> map) {
		boolean success = true;
		for(var entry : map.entrySet()) {
			var writeTask = entry.getValue().get(owner);
			if (writeTask != null) {
				var writeSuccess = directWrite(entry.getKey(), writeTask.content, writeTask.encoding, !writeTask.rewrite);
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
