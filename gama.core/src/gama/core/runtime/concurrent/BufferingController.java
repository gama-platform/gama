package gama.core.runtime.concurrent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

import gama.core.kernel.simulation.SimulationAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.gaml.statements.save.SaveOptions;

public class BufferingController {
	
	public enum BufferingStrategies{
		NO_BUFFERING,
		PER_CYCLE_BUFFERING,
		PER_SIMULATION_BUFFERING
	}


		
	public static class BufferingTask {	

		final public StringBuilder content;
		final public Charset encoding;
		final GamaColor color;
		protected boolean rewrite;
		
		public BufferingTask(final CharSequence initialContent, final Charset encodingType, final boolean rewriteFile) {
			content = new StringBuilder(initialContent);
			encoding = encodingType;
			rewrite = rewriteFile;
			color = null;
		}
		
		public BufferingTask(final CharSequence initialContent, final GamaColor textColor) {
			content = new StringBuilder(initialContent);
			color = textColor;
			encoding = null;
			rewrite = false;
		}
		
		public void setRewrite(boolean rewrite) {
			this.rewrite = rewrite;
		}
		public boolean isRewriting() {
			return rewrite;
		}
	}

	protected Map<String, Map<SimulationAgent, BufferingTask>> fileWritingPerSimulationMap;
	protected Map<String, Map<SimulationAgent, BufferingTask>> fileWritingPerCycleMap;
	protected Map<SimulationAgent, List<BufferingTask>> consoleWritingPerSimulationMap;
	protected Map<SimulationAgent, List<BufferingTask>> consoleWritingPerCycleMap;
	
	public BufferingController() {
		fileWritingPerSimulationMap 	= new HashMap<>();
		fileWritingPerCycleMap 			= new HashMap<>();
		consoleWritingPerSimulationMap 	= new HashMap<>();
		consoleWritingPerCycleMap 		= new HashMap<>();
	}
	
	public boolean askWriteFile(final String fileId, final SimulationAgent owner, final CharSequence content, final SaveOptions options) {
		switch (options.bufferingStrategy) {
			case PER_SIMULATION_BUFFERING:
				return appendSaveFileRequestToMap(owner, getOrInitBufferingMap(fileId, fileWritingPerSimulationMap), content, options);
			case PER_CYCLE_BUFFERING:
				return appendSaveFileRequestToMap(owner, getOrInitBufferingMap(fileId, fileWritingPerCycleMap), content, options);
			case NO_BUFFERING:
				return directWriteFile(fileId, content, options.getCharset(), !options.rewrite);
			default:
				throw GamaRuntimeException.create(new NotImplementedException("This buffering strategie has not been implemented yet: " + options.bufferingStrategy.toString()), owner.getScope());
		}
	}
	

	public boolean askWriteConsole(final SimulationAgent owner, final CharSequence content, final GamaColor color, final BufferingStrategies bufferingStrategy) {
		switch (bufferingStrategy) {
			case PER_SIMULATION_BUFFERING:
				return appendWriteConsoleRequestToMap(owner, consoleWritingPerSimulationMap, content, color);
			case PER_CYCLE_BUFFERING:
				return appendWriteConsoleRequestToMap(owner, consoleWritingPerCycleMap, content, color);
			case NO_BUFFERING:
				directWriteConsole(owner.getScope(), content.toString(), color);
				return true;
			default:
				throw GamaRuntimeException.create(new NotImplementedException("This buffering strategie has not been implemented yet: " + bufferingStrategy.toString()), owner.getScope());
		}
	}
	
	
	protected Map<SimulationAgent, BufferingTask> getOrInitBufferingMap(final String fileId, final  Map<String, Map<SimulationAgent, BufferingTask>> map){
		// If we don't have any map for this file yet we create one
		Map<SimulationAgent, BufferingTask> bufferingMap = map.get(fileId);
		if (bufferingMap == null) {
			bufferingMap = new HashMap<>();
			map.put(fileId, bufferingMap);
		}
		return bufferingMap;
	}
	
	protected boolean appendSaveFileRequestToMap(final SimulationAgent owner, final Map<SimulationAgent, BufferingTask> bufferingMap, final CharSequence content, final SaveOptions options) {
		
		// We look up for the previous request of the owner simulation in the map
		// if there's already one we append our content or rewrite, depending on the append parameter
		// else we create one with the content as its initial value
		BufferingTask request = bufferingMap.get(owner);
		if (request == null) {
			try {
				bufferingMap.put(owner, new BufferingTask(content, options.getCharset(), options.rewrite));
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
				request.setRewrite(true);
				request.content.setLength(0);
			}
			request.content.append(content);
			return true;
		}
	}
	
	protected boolean appendWriteConsoleRequestToMap(final SimulationAgent owner, final Map<SimulationAgent, List<BufferingTask>> bufferingMap, final CharSequence content, final GamaColor color) {
		
		// We look up for the previous request of the owner simulation in the map
		List<BufferingTask> requests = bufferingMap.get(owner);
		
		// If there's no list yet we create one and add it to the map
		if (requests == null) {
			requests = new ArrayList<BufferingTask>();
			bufferingMap.put(owner, requests);	
		}
		
		// If the last element of the list is not of the same color as the currently requested color we append a new task with the new color
		if (requests.size() == 0 || (requests.get(requests.size()-1).color != null && !requests.get(requests.size()-1).color.equals(color))) {
			try {
				requests.add(new BufferingTask(content, color));
				return true;
			}
			catch(Exception ex) {
				GAMA.reportError(owner.getScope(), GamaRuntimeException.create(ex, owner.getScope()), false);
				return false;
			}
		}
		else {
			requests.get(requests.size()-1).content.append(content);
			return true;
		}
	}
	
	
	protected boolean directWriteFile(final String fileId, final CharSequence content, final Charset charset, final boolean append) {
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
	

	protected void directWriteConsole(final IScope scope, final String message, final GamaColor color) {
		scope.getGui().getConsole().informConsole(message, scope.getRoot(), color);
	}
	
	
	/**
	 * Flushes all the save requests made by a simulation saved in a map
	 * @param owner: the simulation in which the save statements have been executed
	 * @return true if everything went well, false in case of error
	 */
	protected boolean flushFilesInMapPerOwner(SimulationAgent owner, Map<String, Map<SimulationAgent, BufferingTask>> map) {
		boolean success = true;
		for(var entry : map.entrySet()) {
			var writeTask = entry.getValue().get(owner);
			if (writeTask != null) {
				var writeSuccess = directWriteFile(entry.getKey(), writeTask.content, writeTask.encoding, !writeTask.rewrite);
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
	 * Flushes all the write requests made by a simulation saved in a map
	 * @param owner: the simulation in which the write statements have been executed
	 */
	protected void flushWriteInMapPerOwner(final SimulationAgent owner, final Map<SimulationAgent, List<BufferingTask>> map) {
		var tasks = map.get(owner);
		if (tasks != null) {
			for (var task : tasks) {
				directWriteConsole(owner.getScope(), task.content.toString(), task.color);
			}
			tasks.clear();
		}
	}
	
	
	
	/**
	 * Flushes all the save requests made by a simulation with the 'per_simulation_buffering' strategy
	 * @param owner: the simulation in which the save statements have been executed
	 * @return true if everything went well, false in case of error
	 */
	public boolean flushSaveFilesSimulationPerOwner(SimulationAgent owner) {
		return flushFilesInMapPerOwner(owner, fileWritingPerSimulationMap);		
	}
	
	/**
	 * Flushes all the save requests made by a simulation with the 'per_cycle_buffering' strategy
	 * @param owner: the simulation in which the save statements have been executed
	 * @return true if everything went well, false in case of error
	 */
	public boolean flushSaveFilesCycleOwner(SimulationAgent owner) {
		return flushFilesInMapPerOwner(owner, fileWritingPerCycleMap);
	}
	
	/**
	 * Flushes all the write statement requests made by a simulation with the 'per_cycle_buffering' strategy
	 * @param owner: the simulation in which the write statements have been executed
	 */
	public void flushwriteCycleOwner(SimulationAgent owner) {
		flushWriteInMapPerOwner(owner, consoleWritingPerCycleMap);
	}

	/**
	 * Flushes all the save requests made by a simulation with the 'per_simulation_buffering' strategy
	 * @param owner: the simulation in which the save statements have been executed
	 */
	public void flushWriteSimulationPerOwner(SimulationAgent owner) {
		flushWriteInMapPerOwner(owner, consoleWritingPerSimulationMap);		
	}
	
}
