/*******************************************************************************************************
 *
 * BufferingController.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.runtime.concurrent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.NotImplementedException;

import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.gaml.statements.save.SaveOptions;

/**
 * The Class BufferingController.
 */
public class BufferingController {

	/** The Constant PER_CYCLE_BUFFERING. */
	public static final String PER_CYCLE_BUFFERING = "per_cycle";

	/** The Constant PER_SIMULATION_BUFFERING. */
	public static final String PER_SIMULATION_BUFFERING = "per_simulation";

	/** The Constant PER_AGENT. */
	public static final String PER_AGENT = "per_agent";

	/** The Constant NO_BUFFERING. */
	public static final String NO_BUFFERING = "no_buffering";

	/** The Constant BUFFERING_STRATEGIES. */
	public static final Set<String> BUFFERING_STRATEGIES = Collections.unmodifiableSet(
			new HashSet<>(Arrays.asList(PER_CYCLE_BUFFERING, PER_SIMULATION_BUFFERING, NO_BUFFERING, PER_AGENT)));

	/**
	 * The Enum BufferingStrategies.
	 */
	public enum BufferingStrategies {

		/** The no buffering. */
		NO_BUFFERING,

		/** The per cycle buffering. */
		PER_CYCLE_BUFFERING,

		/** The per simulation buffering. */
		PER_SIMULATION_BUFFERING,

		/** The per agent. */
		PER_AGENT,
	}

	/**
	 * Converts a string into a BufferingStrategies if it matches the corresponding static variables. If not it returns
	 * NO_BUFFERING
	 *
	 * @param s
	 * @return
	 */
	public static BufferingStrategies stringToBufferingStrategies(final IScope scope, final String s) {
		return switch (s) {
			case BufferingController.PER_CYCLE_BUFFERING -> BufferingStrategies.PER_CYCLE_BUFFERING;
			case BufferingController.PER_SIMULATION_BUFFERING -> BufferingStrategies.PER_SIMULATION_BUFFERING;
			case BufferingController.NO_BUFFERING -> BufferingStrategies.NO_BUFFERING;
			case BufferingController.PER_AGENT -> BufferingStrategies.PER_AGENT;
			default -> throw GamaRuntimeException.create(
					new NotImplementedException("This buffering strategie has not been implemented yet: " + s), scope);
		};
	}

	/**
	 * Represents a buffer of text on a file or on the console. Buffers for console text have a color. Buffers for files
	 * have an encoding charset and also contain a variable rewrite that should be used to indicate if the file must be
	 * recreated or just appended (when rewrite is false).
	 */
	public static class TextBuffer {

		/** The content. */
		final public StringBuilder content;

		/** The encoding. */
		final public Charset encoding;

		/** The color. */
		final public GamaColor color;

		/** The rewrite. */
		protected boolean rewrite;

		/**
		 * Instantiates a new text buffer.
		 *
		 * @param initialContent
		 *            the initial content
		 * @param encodingType
		 *            the encoding type
		 * @param rewriteFile
		 *            the rewrite file
		 */
		// Constructor for a text buffer on a file
		public TextBuffer(final CharSequence initialContent, final Charset encodingType, final boolean rewriteFile) {
			content = new StringBuilder(initialContent);
			encoding = encodingType;
			rewrite = rewriteFile;
			color = null;
		}

		/**
		 * Instantiates a new text buffer.
		 *
		 * @param initialContent
		 *            the initial content
		 * @param textColor
		 *            the text color
		 */
		// Constructor for a text buffer on the console
		public TextBuffer(final CharSequence initialContent, final GamaColor textColor) {
			content = new StringBuilder(initialContent);
			color = textColor;
			encoding = null;
			rewrite = false;
		}

		/**
		 * Sets the rewrite.
		 *
		 * @param rewrite
		 *            the new rewrite
		 */
		public void setRewrite(final boolean rewrite) { this.rewrite = rewrite; }

		/**
		 * Checks if is rewriting.
		 *
		 * @return true, if is rewriting
		 */
		public boolean isRewriting() { return rewrite; }
	}

	// those are the maps that are mapping a file to one or multiple agents each responsible for a buffer of text.
	/** The file buffer per agent. */
	// the files in those maps MUST be absolute paths for it to work
	protected Map<String, Map<IAgent, TextBuffer>> fileBufferPerAgent;

	/** The file buffer per agent for cycles. */
	protected Map<String, Map<IAgent, TextBuffer>> fileBufferPerAgentForCycles;

	// the maps that manage console writing, per agent and per agent for cycle buffering.
	/** The console buffer list per agent. */
	// each agent is responsible for a list of buffers (one each color change)
	protected Map<IAgent, List<TextBuffer>> consoleBufferListPerAgent;

	/** The console buffer list per agent for cycles. */
	protected Map<IAgent, List<TextBuffer>> consoleBufferListPerAgentForCycles;

	/**
	 * Instantiates a new buffering controller.
	 */
	public BufferingController() {
		fileBufferPerAgent = new HashMap<>();
		fileBufferPerAgentForCycles = new HashMap<>();
		consoleBufferListPerAgent = new HashMap<>();
		consoleBufferListPerAgentForCycles = new HashMap<>();
	}

	/**
	 * Ask to write on a file following a given buffering strategy and some saving options. This method will take care
	 * of creating or not a new buffer if needed. If the strategy is different from no_buffering, the text will only be
	 * written when the corresponding flush operation is called on the file or on the agent. This is automatically
	 * managed inside the agents. If multiple calls of that method for the same file are made before flushing, the texts
	 * will be concatenated, resulting in performance gains. If the strategy is no_buffering, then the file is directly
	 * written.
	 *
	 * @param fileId
	 *            The id used to represent the file, it should be its absolute path
	 * @param scope
	 *            the scope from which the save statement was called, used to identify the "owner" of the request
	 * @param content
	 *            the text to write in the file
	 * @param options
	 *            the saving options (rewrite, buffering strategy etc. )
	 * @return true if the operation was successful, false otherwise
	 */
	public synchronized boolean askWriteFile(final String fileId, final IScope scope, final CharSequence content,
			final SaveOptions options) {
		IAgent owner = scope.getSimulation();
		switch (options.bufferingStrategy) {
			case PER_AGENT, PER_SIMULATION_BUFFERING:
				// in case it's per agent we just switch the owner to the calling agent
				// instead of the whole simulation
				if (options.bufferingStrategy == BufferingStrategies.PER_AGENT) { owner = scope.getAgent(); }
				return appendSaveFileRequestToMap(owner, getOrInitBufferingMap(fileId, fileBufferPerAgent), content,
						options);
			case PER_CYCLE_BUFFERING:
				return appendSaveFileRequestToMap(owner, getOrInitBufferingMap(fileId, fileBufferPerAgentForCycles),
						content, options);
			case NO_BUFFERING:
				return directWriteFile(fileId, content, options.getCharset(), !options.rewrite);
			default:
				IScope ownerScope = owner.getScope();
				throw GamaRuntimeException
						.create(new NotImplementedException("This buffering strategie has not been implemented yet: "
								+ options.bufferingStrategy.toString()), ownerScope);
		}
	}

	/**
	 * Ask to write some text in the console following a given buffering strategy and with a given color. This method
	 * will take care of creating or not a new buffer if needed. If the strategy is different from no_buffering, the
	 * text will only be written when the corresponding flush operation is called. This is automatically managed inside
	 * the agents. If multiple calls of that method are made before flushing, the texts will be concatenated, resulting
	 * in performance gains. If the strategy is no_buffering, then the content is directly written in the console.
	 *
	 * @param scope
	 *            the scope from which the write statement was called
	 * @param content
	 *            the text to print
	 * @param color
	 *            the color of the text
	 * @param bufferingStrategy
	 *            the buffering strategy to apply
	 * @return true if the operation is successful, false otherwise
	 */
	public synchronized boolean askWriteConsole(final IScope scope, final StringBuilder content, final GamaColor color,
			final BufferingStrategies bufferingStrategy) {
		IAgent owner = scope.getSimulation();
		switch (bufferingStrategy) {
			case PER_AGENT, PER_SIMULATION_BUFFERING:
				// in case it's per agent we just switch the owner to the calling agent
				// instead of the whole simulation
				if (bufferingStrategy == BufferingStrategies.PER_AGENT) { owner = scope.getAgent(); }
				return appendWriteConsoleRequestToMap(owner, consoleBufferListPerAgent, content, color);
			case PER_CYCLE_BUFFERING:
				return appendWriteConsoleRequestToMap(owner, consoleBufferListPerAgentForCycles, content, color);
			case NO_BUFFERING:
				scope.getGui().getConsole().informConsole(content.toString(), scope.getRoot(), color);
				return true;
			default:
				IScope ownerScope = owner.getScope();
				throw GamaRuntimeException.create(new NotImplementedException(
						"This buffering strategie has not been implemented yet: " + bufferingStrategy.toString()),
						ownerScope);
		}
	}

	/**
	 * Tries to get the existing map of agent/buffer for one given file. If it doesn't exist it will be created and
	 * added to the map in which it was looking in.
	 *
	 * @param fileId
	 *            the id of the file, it should be its absolute path
	 * @param map
	 *            the map in which to look in
	 * @return the corresponding map of agent/buffer
	 */
	protected synchronized Map<IAgent, TextBuffer> getOrInitBufferingMap(final String fileId,
			final Map<String, Map<IAgent, TextBuffer>> map) {
		// If we don't have any map for this file yet we create one
		Map<IAgent, TextBuffer> bufferingMap = map.get(fileId);
		if (bufferingMap == null) {
			bufferingMap = new HashMap<>();
			map.put(fileId, bufferingMap);
		}
		return bufferingMap;
	}

	/**
	 * Takes care of properly adding the content of a file saving request into the map. It will create a buffer if none
	 * exists or append the content if one is already present. It will also take care of resetting the content in case
	 * the new request has the rewrite option set to true
	 *
	 * @param owner
	 *            the agent that is responsible for asking to write, will be used later to flush
	 * @param bufferingMap
	 *            the map containing already present buffers
	 * @param content
	 *            the text to write
	 * @param options
	 *            the saving options (used to get the rewrite option)
	 * @return true if the operation was successful, false otherwise
	 */
	protected static boolean appendSaveFileRequestToMap(final IAgent owner, final Map<IAgent, TextBuffer> bufferingMap,
			final CharSequence content, final SaveOptions options) {

		// We look up for the previous request of the owner simulation in the map
		// if there's already one we append our content or rewrite, depending on the append parameter
		// else we create one with the content as its initial value
		TextBuffer request = bufferingMap.get(owner);
		if (request == null) {
			try {
				bufferingMap.put(owner, new TextBuffer(content, options.getCharset(), options.rewrite));
				return true;
			} catch (Exception ex) {
				GAMA.reportError(owner.getScope(), GamaRuntimeException.create(ex, owner.getScope()), false);
				return false;
			}
		}
		// If we are not in append mode, we empty the buffer
		if (options.rewrite) {
			request.setRewrite(true);
			request.content.setLength(0);
		}
		request.content.append(content);
		return true;
	}

	/**
	 * Takes care of properly adding the content of a console write request into the map. It will create a buffer if
	 * none exists or append the content into the last one if it is compatible (same color).
	 *
	 * @param owner
	 *            the agent that is responsible for asking to write, will be used later to flush
	 * @param bufferingMap
	 *            the map containing the list of already created buffers
	 * @param content
	 *            the text to write
	 * @param color
	 *            the color in which the text should be printed
	 * @return true if the operation was successful, false otherwise
	 */
	protected static boolean appendWriteConsoleRequestToMap(final IAgent owner,
			final Map<IAgent, List<TextBuffer>> bufferingMap, final StringBuilder content, final GamaColor color) {

		// We look up for the previous request of the owner simulation in the map
		List<TextBuffer> requests = bufferingMap.get(owner);

		// If there's no list yet we create one and add it to the map
		if (requests == null) {
			requests = new ArrayList<>();
			bufferingMap.put(owner, requests);
		}

		// If the last element of the list is not of the same color as the currently requested color we append a new
		// task with the new color
		if (requests.size() != 0 && (requests.get(requests.size() - 1).color == null
				|| requests.get(requests.size() - 1).color.equals(color))) {
			requests.get(requests.size() - 1).content.append(content);
			return true;
		}
		try {
			requests.add(new TextBuffer(content, color));
			return true;
		} catch (Exception ex) {
			GAMA.reportError(owner.getScope(), GamaRuntimeException.create(ex, owner.getScope()), false);
			return false;
		}
	}

	/**
	 * Creates a file object and directly writes the content into it. No buffering.
	 *
	 * @param fileId
	 *            the path of the file
	 * @param content
	 *            the text to print
	 * @param charset
	 *            the charset used to write
	 * @param append
	 *            if true the content will be appened, else it will replace the current file content (if any)
	 * @return true if no exception, false otherwise
	 */
	protected static boolean directWriteFile(final String fileId, final CharSequence content, final Charset charset,
			final boolean append) {
		try {
			FileUtils.write(new File(fileId), content, charset, append);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Flushes all the save requests linked to an agent (per_agent or per_simulation buffering)
	 *
	 * @param owner
	 *            the agent in which the save statements have been executed
	 * @param map
	 *            the map in which to look up
	 * @return true if everything went well, false in case of error
	 */
	protected static boolean flushAllFilesOfAgent(final IAgent owner, final Map<String, Map<IAgent, TextBuffer>> map) {
		boolean success = true;
		for (var entry : map.entrySet()) {
			var writeTask = entry.getValue().get(owner);
			if (writeTask != null) {
				var writeSuccess =
						directWriteFile(entry.getKey(), writeTask.content, writeTask.encoding, !writeTask.rewrite);
				// we don't return false directly because we try to flush as much files as possible
				success &= writeSuccess;
				// if the write was successful we remove the operation from the map
				if (writeSuccess) { entry.getValue().remove(owner); }
			}
		}
		return success;
	}

	/**
	 * Flushes all the write requests linked to an agent (per_agent or per_simulation buffering)
	 *
	 * @param owner
	 *            the agent in which the write statements have been executed
	 * @param map
	 *            the map in which to look up
	 */
	protected static void flushAllWriteOfAgent(final IAgent owner, final Map<IAgent, List<TextBuffer>> map) {
		var tasks = map.get(owner);
		if (tasks != null) {
			var scope = owner.getScope();
			for (var task : tasks) {
				scope.getGui().getConsole().informConsole(task.content.toString(), scope.getRoot(), task.color);
			}
			tasks.clear();
		}
	}

	/**
	 * Flushes all the save requests made by an agent with the 'per_simulation' or 'per_agent' strategy
	 *
	 * @param owner
	 *            the simulation or agent in which the save statements have been executed
	 * @return true if everything went well, false in case of error
	 */
	public synchronized boolean flushSaveFilesOfAgent(final IAgent owner) {
		return flushAllFilesOfAgent(owner, fileBufferPerAgent);
	}

	/**
	 * Flushes all the save requests made in a simulation with the 'per_cycle' strategy
	 *
	 * @param owner
	 *            the simulation in which the save statements have been executed
	 * @return true if everything went well, false in case of error
	 */
	public synchronized boolean flushSaveFilesInCycle(final IAgent owner) {
		return flushAllFilesOfAgent(owner, fileBufferPerAgentForCycles);
	}

	/**
	 * Flushes all the write statement requests made in a simulation with the 'per_cycle' strategy
	 *
	 * @param owner
	 *            the simulation in which the write statements have been executed
	 */
	public synchronized void flushWriteInCycle(final IAgent owner) {
		flushAllWriteOfAgent(owner, consoleBufferListPerAgentForCycles);
	}

	/**
	 * Flushes all the write requests made by an agent with the 'per_simulation' or 'per_agent' strategy
	 *
	 * @param owner:
	 *            the agent or simulation in which the write statements have been executed
	 */
	public synchronized void flushWriteOfAgent(final IAgent owner) {
		flushAllWriteOfAgent(owner, consoleBufferListPerAgent);
	}

	/**
	 * Flushes all buffers that are waiting. Flushes write and save buffers, whether registered per cycle or per agent
	 */
	public synchronized void flushAllBuffers() {
		// flushes the console per cycle first
		for (var agent : consoleBufferListPerAgentForCycles.keySet()) { flushWriteInCycle(agent); }
		// flushes the others for the console
		for (var agent : consoleBufferListPerAgent.keySet()) { flushWriteOfAgent(agent); }
		// flushes the files registered for the cycle
		var agents = fileBufferPerAgentForCycles.entrySet().stream().map(s -> s.getValue().keySet())
				.flatMap(Collection::stream).toArray(length -> new IAgent[length]);
		for (IAgent agent : agents) { flushSaveFilesInCycle(agent); }
		// finally flushes the files registered per agent
		agents = fileBufferPerAgent.entrySet().stream().map(s -> s.getValue().keySet()).flatMap(Collection::stream)
				.toArray(length -> new IAgent[length]);
		for (IAgent agent : agents) { flushSaveFilesOfAgent(agent); }
	}

	/**
	 * Check if a file is currently buffered, which means it may not exist yet on the disk, but will be at some point
	 *
	 * @param f
	 *            the file to test
	 * @return true if there are pending writing operations for this file, false otherwise
	 */
	public synchronized boolean isFileWaitingToBeWritten(final File f) {
		// visits all files that are registered by agents
		// visits all files that are registered by cycle
		if (fileBufferPerAgent.keySet().parallelStream()
				.anyMatch(registeredFile -> registeredFile.equals(f.getAbsolutePath()))
				|| fileBufferPerAgentForCycles.keySet().parallelStream()
						.anyMatch(registeredFile -> registeredFile.equals(f.getAbsolutePath())))
			return true;
		return false;
	}
}
