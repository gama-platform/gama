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
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import gama.api.GAMA;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.agent.AgentReference;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.IColor;

/**
 * The Class BufferingUtils.
 *
 * Buffered data is keyed by agent references rather than agent instances, so this singleton does not
 * keep simulations alive. Per-file and per-agent {@link ConcurrentHashMap#compute(Object,
 * java.util.function.BiFunction)} operations serialize mutation with flushes for the same key while
 * allowing unrelated files and agents to proceed independently.
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

		/** A weak reference used only to flush pending console output globally. */
		private final WeakReference<IAgent> owner;

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
			owner = null;
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
			this(initialContent, textColor, null);
		}

		private TextBuffer(final CharSequence initialContent, final IColor textColor, final IAgent owner) {
			content = new StringBuilder(initialContent);
			color = textColor;
			encoding = null;
			rewrite = false;
			this.owner = owner == null ? null : new WeakReference<>(owner);
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
	// Agent references, not agents, are used as keys so buffered output cannot retain simulations.
	// -------------------------------------------------------------------------

	/**
	 * Maps an absolute file path to a per-agent map of pending write buffers, for
	 * PER_AGENT / PER_SIMULATION_BUFFERING strategies.
	 */
	protected final Map<String, Map<String, TextBuffer>> fileBufferPerAgent = new ConcurrentHashMap<>();

	/**
	 * Maps an absolute file path to a per-agent map of pending write buffers, for
	 * PER_CYCLE_BUFFERING strategy.
	 */
	protected final Map<String, Map<String, TextBuffer>> fileBufferPerAgentForCycles = new ConcurrentHashMap<>();

	/**
	 * Maps each agent to its ordered list of pending console buffers, for
	 * PER_AGENT / PER_SIMULATION_BUFFERING strategies.
	 */
	protected final Map<String, List<TextBuffer>> consoleBufferListPerAgent = new ConcurrentHashMap<>();

	/**
	 * Maps each agent to its ordered list of pending console buffers, for
	 * PER_CYCLE_BUFFERING strategy.
	 */
	protected final Map<String, List<TextBuffer>> consoleBufferListPerAgentForCycles = new ConcurrentHashMap<>();

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
				return appendSaveFileRequestToMap(owner, fileId, fileBufferPerAgent, content, options);
			case PER_CYCLE_BUFFERING:
				return appendSaveFileRequestToMap(owner, fileId, fileBufferPerAgentForCycles, content, options);
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
	protected static boolean appendSaveFileRequestToMap(final IAgent owner, final String fileId,
			final Map<String, Map<String, TextBuffer>> map, final CharSequence content, final SaveOptions options) {
		boolean[] success = { true };
		final String ownerReference = AgentReference.of(owner).toString();
		map.compute(fileId, (id, bufferingMap) -> {
			if (bufferingMap == null) { bufferingMap = new ConcurrentHashMap<>(); }
			Map<String, TextBuffer> buffers = bufferingMap;
			buffers.compute(ownerReference, (key, request) -> {
				try {
					if (request == null) return new TextBuffer(content, options.writeCharset(), options.rewrite());
					if (options.rewrite()) {
						request.setRewrite(true);
						request.content.setLength(0);
					}
					request.content.append(content);
					return request;
				} catch (Exception ex) {
					GAMA.reportError(owner.getScope(), GamaRuntimeException.create(ex, owner.getScope()), false);
					success[0] = false;
					return request;
				}
			});
			return bufferingMap;
		});
		return success[0];
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
			final Map<String, List<TextBuffer>> bufferingMap, final StringBuilder content, final IColor color) {
		boolean[] success = { true };
		final String ownerReference = AgentReference.of(owner).toString();
		bufferingMap.compute(ownerReference, (key, requests) -> {
			if (requests == null) { requests = new ArrayList<>(); }
			if (!requests.isEmpty()) {
				TextBuffer last = requests.get(requests.size() - 1);
				if (last.color == null && color == null || last.color != null && last.color.equals(color)) {
					last.content.append(content);
					return requests;
				}
			}
			try {
				requests.add(new TextBuffer(content, color, owner));
			} catch (Exception ex) {
				GAMA.reportError(owner.getScope(), GamaRuntimeException.create(ex, owner.getScope()), false);
				success[0] = false;
			}
			return requests;
		});
		return success[0];
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
	// Flush helpers — compute operations serialize writes and flushes for the same key.
	// -------------------------------------------------------------------------

	/**
	 * Flushes all the save requests linked to an agent in the given map.
	 *
	 * @param owner
	 *            the agent whose entries should be flushed
	 * @param map
	 *            the outer map to flush from
	 * @return true if everything went well, false in case of error
	 */
	private static boolean flushAllFilesOfAgent(final IAgent owner,
			final Map<String, Map<String, TextBuffer>> map) {
		boolean[] success = { true };
		final String ownerReference = AgentReference.of(owner).toString();
		for (String fileId : map.keySet()) {
			map.computeIfPresent(fileId, (id, buffers) -> {
				buffers.computeIfPresent(ownerReference, (key, writeTask) -> {
					boolean writeSuccess =
							directWriteFile(id, writeTask.content, writeTask.encoding, !writeTask.rewrite);
					success[0] &= writeSuccess;
					return writeSuccess ? null : writeTask;
				});
				return buffers.isEmpty() ? null : buffers;
			});
		}
		return success[0];
	}

	/**
	 * Flushes all the console write requests linked to an agent in the given map.
	 *
	 * @param owner
	 *            the agent whose entries should be flushed
	 * @param map
	 *            the map to flush from
	 */
	private static void flushAllWriteOfAgent(final IAgent owner, final Map<String, List<TextBuffer>> map) {
		flushConsoleBuffers(AgentReference.of(owner).toString(), owner, map);
	}

	private static void flushConsoleBuffers(final String ownerReference, final IAgent owner,
			final Map<String, List<TextBuffer>> map) {
		map.computeIfPresent(ownerReference, (key, tasks) -> {
			IAgent currentOwner = owner;
			if (currentOwner == null && !tasks.isEmpty() && tasks.get(0).owner != null) {
				currentOwner = tasks.get(0).owner.get();
			}
			if (currentOwner == null) return null;
			var scope = currentOwner.getScope();
			for (var task : tasks) {
				scope.getGui().getConsole().informConsole(task.content.toString(), scope.getRoot(), task.color);
			}
			return null;
		});
	}

	private static void flushAllFiles(final Map<String, Map<String, TextBuffer>> map) {
		for (String fileId : map.keySet()) {
			map.computeIfPresent(fileId, (id, buffers) -> {
				for (String ownerReference : buffers.keySet()) {
					buffers.computeIfPresent(ownerReference, (key, writeTask) -> {
						boolean success =
								directWriteFile(id, writeTask.content, writeTask.encoding, !writeTask.rewrite);
						return success ? null : writeTask;
					});
				}
				return buffers.isEmpty() ? null : buffers;
			});
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
	public boolean flushSaveFilesOfAgent(final IAgent owner) { return flushAllFilesOfAgent(owner, fileBufferPerAgent); }

	/**
	 * Flushes all the save requests made in a simulation with the 'per_cycle' strategy.
	 *
	 * @param owner
	 *            the simulation whose save statements should be flushed
	 * @return true if everything went well, false in case of error
	 */
	public boolean flushSaveFilesInCycle(final IAgent owner) {
		return flushAllFilesOfAgent(owner, fileBufferPerAgentForCycles);
	}

	/**
	 * Flushes all the write statement requests made in a simulation with the 'per_cycle' strategy.
	 *
	 * @param owner
	 *            the simulation whose write statements should be flushed
	 */
	public void flushWriteInCycle(final IAgent owner) { flushAllWriteOfAgent(owner, consoleBufferListPerAgentForCycles); }

	/**
	 * Flushes all the write requests made by an agent with the 'per_simulation' or 'per_agent' strategy.
	 *
	 * @param owner
	 *            the agent or simulation whose write statements should be flushed
	 */
	public void flushWriteOfAgent(final IAgent owner) { flushAllWriteOfAgent(owner, consoleBufferListPerAgent); }

	/**
	 * Flushes all buffers that are waiting — write and save buffers, whether registered per cycle or per agent.
	 */
	public void flushAllBuffers() {
		for (String ownerReference : new ArrayList<>(consoleBufferListPerAgentForCycles.keySet())) {
			flushConsoleBuffers(ownerReference, null, consoleBufferListPerAgentForCycles);
		}
		for (String ownerReference : new ArrayList<>(consoleBufferListPerAgent.keySet())) {
			flushConsoleBuffers(ownerReference, null, consoleBufferListPerAgent);
		}
		flushAllFiles(fileBufferPerAgentForCycles);
		flushAllFiles(fileBufferPerAgent);
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
