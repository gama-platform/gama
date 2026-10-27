package gama.core.util;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import gama.api.kernel.agent.IAgent;

public final class CommandProcessManager {

	private static final Map<IAgent, Set<Process>> PROCESSES = new IdentityHashMap<>();

	private CommandProcessManager() {}

	public static void register(final IAgent agent, final Process process) {
		if (agent == null) return;
		final boolean disposed;
		synchronized (PROCESSES) {
			disposed = agent.dead();
			if (!disposed) { PROCESSES.computeIfAbsent(agent, ignored -> new HashSet<>()).add(process); }
		}
		if (disposed) {
			terminate(process);
			return;
		}
		process.onExit().thenRun(() -> unregister(agent, process));
	}

	public static void dispose(final IAgent agent) {
		final Set<Process> processes;
		synchronized (PROCESSES) {
			processes = PROCESSES.remove(agent);
		}
		if (processes != null) { processes.forEach(CommandProcessManager::terminate); }
	}

	private static void unregister(final IAgent agent, final Process process) {
		synchronized (PROCESSES) {
			final Set<Process> processes = PROCESSES.get(agent);
			if (processes != null && processes.remove(process) && processes.isEmpty()) { PROCESSES.remove(agent); }
		}
	}

	private static void terminate(final Process process) {
		final ProcessHandle handle = process.toHandle();
		handle.descendants().forEach(ProcessHandle::destroyForcibly);
		handle.destroyForcibly();
	}
}
