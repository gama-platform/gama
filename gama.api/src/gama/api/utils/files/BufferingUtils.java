/*******************************************************************************************************
 *
 * BufferingUtils.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;

/**
 * The Class BufferingUtils.
 *
 * <h3>Thread-safety design</h3>
 * <p>
 * The four collections are {@link ConcurrentHashMap}s, so individual reads and single-key writes
 * are lock-free. The only operations that touch multiple entries atomically (the flush methods) are
 * protected by dedicated per-map {@link ReentrantLock}s — one for file-per-agent buffers, one for
 * file-per-cycle buffers, one for console-per-agent buffers, and one for console-per-cycle buffers.
 * This eliminates the single global lock that previously serialised every write and flush, allowing
 * concurrent writes on different files/agents to proceed in parallel.
 * </p>
 */
public class BufferingUtils {

	/** The Constant INSTANCE. */
	private static final BufferingUtils INSTANCE = new BufferingUtils();

	/**
	 * Gets the single instance of BufferingUtils.
	 *
	 * @return single instance of BufferingUtils
	 */
	public static BufferingUtils getInstance() { return INSTANCE; }

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
	 * @param scope
	 *            the current scope
	 * @param s
	 *            the strategy string
	 * @return the matching BufferingStrategies constant
	 */
	public static BufferingStrategies stringToBufferingStrategies(final IScope scope, final String s) {
		return switch (s) {
			case BufferingUtils.PER_CYCLE_BUFFERING -> BufferingStrategies.PER_CYCLE_BUFFERING;
			case BufferingUtils.PER_SIMULATION_BUFFERING -> BufferingStrategies.PER_SIMULATION_BUFFERING;
			case BufferingUtils.NO_BUFFERING -> BufferingStrategies.NO_BUFFERING;
			case BufferingUtils.PER_AGENT -> BufferingStrategies.PER_AGENT;
			default -> throw GamaRuntimeException.create(
					new UnsupportedOperationException("This buffering strategie has not been implemented yet: " + s),
					scope);
		};
	}

	/**
	 * Represents a buffer of text on a file or on the console. Buffers for console text have a color. Buffers for
	 * files have an encoding charset and also contain a variable rewrite that should be used to indicate if the file
	 * must be recreated or just appended (when rewrite is false).
	 */
	public static class TextBuffer {

		/** The content. */
		final public StringBuilder content;

		/** The encoding. */
		final public Charset encoding;

		/** The color. */
		final public IColor color;

		/** The rewrite. */
		protected boolean rewrite;

		/**
		 * Instantiates a new text buffer (file variant).
		 *
		 * @param initialContent
		 *            the initial content
		 * @param encodingType
		 *            the encoding type
		 * @param rewriteFile
		 *            whether to rewrite the file
		 */
		public TextBuffer(final CharSequence initialContent, final Charset encodingType, final boolean rewriteFile) {
			content = new StringBuilder(initialContent);
			encoding = encodingType;
			rewrite = rewriteFile;
			color = null;
		}

		/**
		 * Instantiates a new text buffer (console variant).
		 *
		 * @param initialContent
		 *            the initial content
		 * @param textColor
		 *            the text color
		 */
		public TextBuffer(final CharSequence initialContent, final IColor textColor) {
			content = new StringBuilder(initialContent);
			color = textColor;
			encoding = null;
			rewrite = false;
		}

		/**
		 * Sets the rewrite.
		 *
		 * @param rewrite
		 *            the new rewrite flag
		 */
		public void setRewrite(final boolean rewrite) { this.rewrite = rewrite; }

		/**
		 * Checks if is rewriting.
		 *
		 * @return true if this buffer will overwrite the target file
		 */
		public boolean isRewriting() { return rewrite; }
	}

	// -------------------------------------------------------------------------
	// Fields — ConcurrentHashMap replaces plain HashMap for lock-free reads/writes.
	// Per-map ReentrantLocks guard the compound flush operations.
	// -------------------------------------------------------------------------

	/**
	 * Maps an absolute file path to a per-agent map of pending write buffers, for
	 * PER_AGENT / PER_SIMULATION_BUFFERING strategies.
	 */
	protected final Map<String, Map<IAgent, TextBuffer>> fileBufferPerAgent = new ConcurrentHashMap<>();

	/** Lock protecting compound operations (iterate + remove) on {@link #fileBufferPerAgent}. */
	private final ReentrantLock filePerAgentLock = new ReentrantLock();

	/**
	 * Maps an absolute file path to a per-agent map of pending write buffers, for
	 * PER_CYCLE_BUFFERING strategy.
	 */
	protected final Map<String, Map<IAgent, TextBuffer>> fileBufferPerAgentForCycles = new ConcurrentHashMap<>();

	/** Lock protecting compound operations on {@link #fileBufferPerAgentForCycles}. */
	private final ReentrantLock fileCycleLock = new ReentrantLock();

	/**
	 * Maps each agent to its ordered list of pending console buffers, for
	 * PER_AGENT / PER_SIMULATION_BUFFERING strategies.
	 */
	protected final Map<IAgent, List<TextBuffer>> consoleBufferListPerAgent = new ConcurrentHashMap<>();

	/** Lock protecting compound operations on {@link #consoleBufferListPerAgent}. */
	private final ReentrantLock consolePerAgentLock = new ReentrantLock();

	/**
	 * Maps each agent to its ordered list of pending console buffers, for
	 * PER_CYCLE_BUFFERING strategy.
	 */
	protected final Map<IAgent, List<TextBuffer>> consoleBufferListPerAgentForCycles = new ConcurrentHashMap<>();

	/** Lock protecting compound operations on {@link #consoleBufferListPerAgentForCycles}. */
	private final ReentrantLock consoleCycleLock = new ReentrantLock();

	/**
	 * Instantiates a new buffering controller. All maps are ConcurrentHashMaps.
	 */
	public BufferingUtils() {}

	// -------------------------------------------------------------------------
	// Write requests
	// -------------------------------------------------------------------------

	/**
	 * Ask to write on a file following a given buffering strategy and some saving options. This method will take care
	 * of creating or not a new buffer if needed. If the strategy is different from no_buffering, the text will only be
	 * written when the corresponding flush operation is called on the file or on the agent. This is automatically
	 * managed inside the agents. If multiple calls of that method for the same file are made before flushing, the texts
	 * will be concatenated, resulting in performance gains. If the strategy is no_buffering, then the file is directly
	 * written.
	 *
	 * @param fileId
	 *            the id used to represent the file; should be its absolute path
	 * @param scope
	 *            the scope from which the save statement was called
	 * @param content
	 *            the text to write in the file
	 * @param options
	 *            the saving options (rewrite, buffering strategy, etc.)
	 * @return true if the operation was successful, false otherwise
	 */
	public boolean askWriteFile(final String fileId, final IScope scope, final CharSequence content,
			final SaveOptions options) {
		IAgent owner = scope.getSimulation();
		switch (options.bufferingStrategy()) {
			case PER_AGENT, PER_SIMULATION_BUFFERING:
				if (options.bufferingStrategy() == BufferingStrategies.PER_AGENT) { owner = scope.getAgent(); }
				return appendSaveFileRequestToMap(owner, getOrInitBufferingMap(fileId, fileBufferPerAgent), content,
						options);
			case PER_CYCLE_BUFFERING:
				return appendSaveFileRequestToMap(owner,
						getOrInitBufferingMap(fileId, fileBufferPerAgentForCycles), content, options);
			case NO_BUFFERING:
				return directWriteFile(fileId, content, options.writeCharset(), !options.rewrite());
			default:
				IScope ownerScope = owner.getScope();
				throw GamaRuntimeException.create(
						new UnsupportedOperationException("This buffering strategie has not been implemented yet: "
								+ options.bufferingStrategy().toString()),
						ownerScope);
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
	public boolean askWriteConsole(final IScope scope, final StringBuilder content, final IColor color,
			final BufferingStrategies bufferingStrategy) {
		IAgent owner = scope.getSimulation();
		switch (bufferingStrategy) {
			case PER_AGENT, PER_SIMULATION_BUFFERING:
				if (bufferingStrategy == BufferingStrategies.PER_AGENT) { owner = scope.getAgent(); }
				return appendWriteConsoleRequestToMap(owner, consoleBufferListPerAgent, content, color);
			case PER_CYCLE_BUFFERING:
				return appendWriteConsoleRequestToMap(owner, consoleBufferListPerAgentForCycles, content, color);
			case NO_BUFFERING:
				scope.getGui().getConsole().informConsole(content.toString(), scope.getRoot(), color);
				return true;
			default:
				IScope ownerScope = owner.getScope();
				throw GamaRuntimeException.create(new UnsupportedOperationException(
						"This buffering strategie has not been implemented yet: " + bufferingStrategy.toString()),
						ownerScope);
		}
	}

	// -------------------------------------------------------------------------
	// Internal helpers
	// -------------------------------------------------------------------------

	/**
	 * Tries to get the existing per-agent map for one given file. If it doesn't exist it will be created and
	 * added to the outer map. Uses {@link ConcurrentHashMap#computeIfAbsent} for an atomic, lock-free
	 * creation of the inner map.
	 *
	 * @param fileId
	 *            the id of the file (absolute path)
	 * @param map
	 *            the outer map in which to look
	 * @return the corresponding per-agent buffer map
	 */
	protected Map<IAgent, TextBuffer> getOrInitBufferingMap(final String fileId,
			final Map<String, Map<IAgent, TextBuffer>> map) {
		return map.computeIfAbsent(fileId, k -> new ConcurrentHashMap<>());
	}

	/**
	 * Takes care of properly adding the content of a file saving request into the map. It will create a buffer if none
	 * exists or append the content if one is already present. It will also take care of resetting the content in case
	 * the new request has the rewrite option set to true.
	 *
	 * @param owner
	 *            the agent responsible for the request
	 * @param bufferingMap
	 *            the map containing already present buffers
	 * @param content
	 *            the text to write
	 * @param options
	 *            the saving options
	 * @return true if the operation was successful, false otherwise
	 */
	protected static boolean appendSaveFileRequestToMap(final IAgent owner, final Map<IAgent, TextBuffer> bufferingMap,
			final CharSequence content, final SaveOptions options) {
		TextBuffer request = bufferingMap.get(owner);
		if (request == null) {
			try {
				bufferingMap.put(owner, new TextBuffer(content, options.writeCharset(), options.rewrite()));
				return true;
			} catch (Exception ex) {
				GAMA.reportError(owner.getScope(), GamaRuntimeException.create(ex, owner.getScope()), false);
				return false;
			}
		}
		if (options.rewrite()) {
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
	 *            the agent responsible for the request
	 * @param bufferingMap
	 *            the map containing the list of already created buffers
	 * @param content
	 *            the text to write
	 * @param color
	 *            the color in which the text should be printed
	 * @return true if the operation was successful, false otherwise
	 */
	protected static boolean appendWriteConsoleRequestToMap(final IAgent owner,
			final Map<IAgent, List<TextBuffer>> bufferingMap, final StringBuilder content, final IColor color) {
		List<TextBuffer> requests = bufferingMap.get(owner);
		if (requests == null) {
			requests = new ArrayList<>();
			bufferingMap.put(owner, requests);
		}
		if (requests.size() != 0 && (requests.get(requests.size() - 1).color == null && color == null
				|| requests.get(requests.size() - 1).color != null
						&& requests.get(requests.size() - 1).color.equals(color))) {
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
	 *            if true the content will be appended, else it will replace the current file content (if any)
	 * @return true if no exception, false otherwise
	 */
	protected static boolean directWriteFile(final String fileId, final CharSequence content, final Charset charset,
			final boolean append) {
		try {
			if (append) {
				Files.write(Paths.get(fileId), content.toString().getBytes(charset), StandardOpenOption.CREATE,
						StandardOpenOption.APPEND);
			} else {
				Files.write(Paths.get(fileId), content.toString().getBytes(charset), StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	// -------------------------------------------------------------------------
	// Flush helpers — protected by per-map ReentrantLocks
	// -------------------------------------------------------------------------

	/**
	 * Flushes all the save requests linked to an agent in the given map.
	 * The per-map lock prevents a concurrent flush of the same map from removing entries
	 * that are still being iterated.
	 *
	 * @param owner
	 *            the agent whose entries should be flushed
	 * @param map
	 *            the outer map to flush from
	 * @param lock
	 *            the per-map lock to acquire
	 * @return true if everything went well, false in case of error
	 */
	private static boolean flushAllFilesOfAgent(final IAgent owner,
			final Map<String, Map<IAgent, TextBuffer>> map, final ReentrantLock lock) {
		lock.lock();
		try {
			boolean success = true;
			for (var entry : map.entrySet()) {
				var writeTask = entry.getValue().get(owner);
				if (writeTask != null) {
					var writeSuccess =
							directWriteFile(entry.getKey(), writeTask.content, writeTask.encoding, !writeTask.rewrite);
					success &= writeSuccess;
					if (writeSuccess) { entry.getValue().remove(owner); }
				}
			}
			return success;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Flushes all the console write requests linked to an agent in the given map.
	 *
	 * @param owner
	 *            the agent whose entries should be flushed
	 * @param map
	 *            the map to flush from
	 * @param lock
	 *            the per-map lock to acquire
	 */
	private static void flushAllWriteOfAgent(final IAgent owner,
			final Map<IAgent, List<TextBuffer>> map, final ReentrantLock lock) {
		lock.lock();
		try {
			var tasks = map.get(owner);
			if (tasks != null) {
				var scope = owner.getScope();
				for (var task : tasks) {
					scope.getGui().getConsole().informConsole(task.content.toString(), scope.getRoot(), task.color);
				}
				tasks.clear();
			}
		} finally {
			lock.unlock();
		}
	}

	// -------------------------------------------------------------------------
	// Public flush API
	// -------------------------------------------------------------------------

	/**
	 * Flushes all the save requests made by an agent with the 'per_simulation' or 'per_agent' strategy.
	 *
	 * @param owner
	 *            the simulation or agent whose save statements should be flushed
	 * @return true if everything went well, false in case of error
	 */
	public boolean flushSaveFilesOfAgent(final IAgent owner) {
		return flushAllFilesOfAgent(owner, fileBufferPerAgent, filePerAgentLock);
	}

	/**
	 * Flushes all the save requests made in a simulation with the 'per_cycle' strategy.
	 *
	 * @param owner
	 *            the simulation whose save statements should be flushed
	 * @return true if everything went well, false in case of error
	 */
	public boolean flushSaveFilesInCycle(final IAgent owner) {
		return flushAllFilesOfAgent(owner, fileBufferPerAgentForCycles, fileCycleLock);
	}

	/**
	 * Flushes all the write statement requests made in a simulation with the 'per_cycle' strategy.
	 *
	 * @param owner
	 *            the simulation whose write statements should be flushed
	 */
	public void flushWriteInCycle(final IAgent owner) {
		flushAllWriteOfAgent(owner, consoleBufferListPerAgentForCycles, consoleCycleLock);
	}

	/**
	 * Flushes all the write requests made by an agent with the 'per_simulation' or 'per_agent' strategy.
	 *
	 * @param owner
	 *            the agent or simulation whose write statements should be flushed
	 */
	public void flushWriteOfAgent(final IAgent owner) {
		flushAllWriteOfAgent(owner, consoleBufferListPerAgent, consolePerAgentLock);
	}

	/**
	 * Flushes all buffers that are waiting — write and save buffers, whether registered per cycle or per agent.
	 */
	public void flushAllBuffers() {
		for (var agent : consoleBufferListPerAgentForCycles.keySet()) { flushWriteInCycle(agent); }
		for (var agent : consoleBufferListPerAgent.keySet()) { flushWriteOfAgent(agent); }
		var agents = fileBufferPerAgentForCycles.entrySet().stream().map(s -> s.getValue().keySet())
				.flatMap(Collection::stream).toArray(length -> new IAgent[length]);
		for (IAgent agent : agents) { flushSaveFilesInCycle(agent); }
		agents = fileBufferPerAgent.entrySet().stream().map(s -> s.getValue().keySet()).flatMap(Collection::stream)
				.toArray(length -> new IAgent[length]);
		for (IAgent agent : agents) { flushSaveFilesOfAgent(agent); }
	}

	/**
	 * Check if a file is currently buffered, which means it may not exist yet on the disk, but will be at some point.
	 *
	 * @param f
	 *            the file to test
	 * @return true if there are pending writing operations for this file, false otherwise
	 */
	public boolean isFileWaitingToBeWritten(final File f) {
		return fileBufferPerAgent.containsKey(f.getAbsolutePath())
				|| fileBufferPerAgentForCycles.containsKey(f.getAbsolutePath());
	}
}
