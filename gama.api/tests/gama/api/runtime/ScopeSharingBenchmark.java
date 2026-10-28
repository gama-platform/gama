package gama.api.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Spliterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.types.IType;
import gama.api.kernel.agent.IAgent;
import gama.api.runtime.scope.ExecutionContext;
import gama.api.runtime.scope.ExecutionScope;
import gama.api.runtime.scope.FlowStatus;
import gama.api.runtime.scope.IScope;

/**
 * Standalone scope-sharing regression checks and a warmed execution-path microbenchmark.
 * Run with the API bundle's runtime classpath; this does not compile or execute a GAML model.
 */
public final class ScopeSharingBenchmark {

	private static final Field SHARED;

	static {
		try {
			SHARED = ExecutionContext.class.getDeclaredField("shared");
			SHARED.setAccessible(true);
		} catch (final ReflectiveOperationException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private static volatile long sink;

	private static boolean shared(final IScope scope) throws IllegalAccessException {
		return SHARED.getBoolean(scope.getExecutionContext());
	}

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	private static IAgent agent(final int index) {
		return (IAgent) Proxy.newProxyInstance(ScopeSharingBenchmark.class.getClassLoader(),
				new Class<?>[] { IAgent.class }, (proxy, method, args) -> switch (method.getName()) {
					case "dead" -> false;
					case "getIndex", "hashCode" -> index;
					case "equals" -> proxy == args[0];
					case "toString" -> "benchmark_agent_" + index;
					default -> {
						if (method.getReturnType().isPrimitive())
							throw new UnsupportedOperationException(method.getName());
						yield null;
					}
				});
	}

	private static IExpression parallelism(final int threshold) {
		final IType<?> type = (IType<?>) Proxy.newProxyInstance(ScopeSharingBenchmark.class.getClassLoader(),
				new Class<?>[] { IType.class }, (proxy, method, args) -> {
					if ("id".equals(method.getName())) return IType.INT;
					throw new UnsupportedOperationException(method.getName());
				});
		return (IExpression) Proxy.newProxyInstance(ScopeSharingBenchmark.class.getClassLoader(),
				new Class<?>[] { IExpression.class }, (proxy, method, args) -> switch (method.getName()) {
					case "isConst" -> false;
					case "getGamlType" -> type;
					case "value" -> threshold;
					default -> throw new UnsupportedOperationException(method.getName());
				});
	}

	private static void checkSharing() throws Exception {
		try (IScope scope = new ExecutionScope(null)) {
			scope.addVarWithValue("value", 0L);
			final IScope first = scope.fork("first");
			final IScope second = scope.fork("second");
			check(shared(scope), "Forks must enable synchronization");
			first.clear();
			first.clear();
			check(shared(scope), "Clearing one fork must not unlock another");
			try (IScope nested = second.fork("nested")) {
				nested.setVarValue("value", 7L);
				check(scope.getVarValue("value").equals(7L), "Nested writes must reach the enclosing frame");
			}
			check(shared(scope), "Clearing a nested fork must retain its parent");
			second.clear();
			check(!shared(scope), "The last fork must restore the unsynchronized fast path");
			try (IScope copy = scope.copy("long-lived"); IScope fork = scope.fork("alongside copy")) {
				fork.clear();
				check(shared(scope), "A sharing copy must keep enclosing frames synchronized");
			}
		}
	}

	private static void checkScopeFixes() throws Exception {
		try (IScope scope = new ExecutionScope(null)) {
			// Copies release the frames they retain when cleared
			try (IScope copy = scope.copy("short-lived")) {
				check(shared(scope), "A sharing copy must synchronize the frames");
			}
			check(!shared(scope), "Clearing the last copy must restore the unsynchronized fast path");
			// Retained frames survive their pop
			scope.addVarWithValue("outer", 1);
			scope.push((ISymbol) null);
			scope.addVarWithValue("inner", 2);
			try (IScope copy = scope.copy("outliving")) {
				scope.pop((ISymbol) null);
				check(Integer.valueOf(2).equals(copy.getVarValue("inner")), "A retained frame must stay readable");
				check(Integer.valueOf(1).equals(copy.getVarValue("outer")), "Enclosing frames must stay readable");
			}
			check(!shared(scope), "Popped retained frames must be released");
			// Clearing a status preserves a different one set meanwhile
			scope.setFlowStatus(FlowStatus.DIE);
			check(scope.getAndClearFlowStatus(FlowStatus.BREAK) == FlowStatus.DIE, "Status must be returned");
			check(scope.getAndClearFlowStatus(FlowStatus.DIE) == FlowStatus.DIE, "Status must be unchanged");
			check(scope.getAndClearFlowStatus(FlowStatus.DIE) == FlowStatus.NORMAL, "Status must be cleared");
			// Data is copied on write
			scope.setData("key", 1);
			try (IScope copy = scope.copy("data")) {
				copy.setData("key", 2);
				copy.setData("other", 3);
				check(Integer.valueOf(1).equals(scope.getData("key")), "Copies must not write in the source data");
				check(scope.getData("other") == null, "Copies must not add to the source data");
				scope.setData("key", null);
				check(Integer.valueOf(2).equals(copy.getData("key")), "Sources must not write in the copy data");
				check(scope.getData("key") == null, "Null must remove the key");
			}
			// The agents stack is de-duplicated and ordered from the top
			final IAgent a = agent(0), b = agent(1);
			scope.push(a);
			scope.push(b);
			scope.push(a);
			check(Arrays.equals(scope.getAgentsStack(), new IAgent[] { a, b }), "Stack must be [a, b]");
			// Detached copies never retain the frames of their source
			try (IScope detached = scope.detachedCopy("detached")) {
				check(!shared(scope), "A detached copy must not synchronize the frames");
				check(detached.getVarValue("outer") == null, "A detached copy must not see the locals");
			}
			// Scopes without a root use a single random generator
			check(scope.getRandom() == scope.getRandom(), "The fallback random generator must be cached");
		}
	}

	private static void checkThreadBinding() throws Exception {
		final IScope scope = new ExecutionScope(null);
		scope.addVarWithValue("root", 1);
		final Thread other = new Thread(() -> {
			// Not the owner yet: the copy is isolated
			try (IScope copy = scope.copy("before binding")) {
				check(copy.getVarValue("root") == null, "Copies from a foreign thread must be isolated");
			}
			scope.bindToCurrentThread();
			try (IScope copy = scope.copy("after binding")) {
				check(Integer.valueOf(1).equals(copy.getVarValue("root")), "Copies from the bound thread must share");
			}
		});
		final Throwable[] failure = new Throwable[1];
		other.setUncaughtExceptionHandler((t, e) -> failure[0] = e);
		other.start();
		other.join();
		if (failure[0] != null) throw new AssertionError(failure[0]);
		try (IScope copy = scope.copy("previous owner")) {
			check(copy.getVarValue("root") == null, "The previous owner must no longer share");
		}
		check(!shared(scope), "All copies must have released the root frame");
		scope.clear();
	}

	/** Forces the right branch to fail while its left sibling is still using the shared frame. */
	private static final class FailingRunner extends ParallelAgentRunner<Boolean> {
		private final boolean fail;
		private final CountDownLatch entered, finished;

		FailingRunner(final IScope scope, final Spliterator<IAgent> agents, final boolean fail,
				final CountDownLatch entered, final CountDownLatch finished) {
			super(scope, agents);
			this.fail = fail;
			this.entered = entered;
			this.finished = finished;
		}

		@Override
		ParallelAgentRunner<Boolean> subTask(final Spliterator<IAgent> sub) {
			return new FailingRunner(originalScope, sub, false, entered, finished);
		}

		@Override
		public Boolean executeOn(final IScope scope) {
			try {
				if (fail) {
					check(entered.await(10, TimeUnit.SECONDS), "Sibling did not start");
					throw new IllegalStateException("expected failure");
				}
				entered.countDown();
				Thread.sleep(50);
				scope.setVarValue("value", 42L);
				finished.countDown();
				return true;
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new AssertionError(e);
			}
		}
	}

	private static void checkFailureJoining(final IAgent[] agents) throws Exception {
		try (IScope scope = new ExecutionScope(null)) {
			scope.addVarWithValue("value", 0L);
			final CountDownLatch entered = new CountDownLatch(1), finished = new CountDownLatch(1);
			final FailingRunner runner = new FailingRunner(scope,
					AgentSpliterator.of(Arrays.copyOf(agents, 2), 1), true, entered, finished);
			try {
				ParallelAgentRunner.execute(runner);
				throw new AssertionError("Failure was swallowed");
			} catch (final IllegalStateException expected) {
				check(finished.getCount() == 0, "Failure returned before its sibling finished");
				check(scope.getVarValue("value").equals(42L), "Sibling write was lost");
				check(!shared(scope), "Failed run retained its shared frames");
			}
		}
	}

	private static void checkPerAgentTasks(final IAgent[] agents) throws Exception {
		final IAgent[] small = Arrays.copyOf(agents, 8);
		final IExpression parallel = parallelism(1);
		try (IScope scope = new ExecutionScope(null)) {
			final AtomicInteger completed = new AtomicInteger();
			GamaExecutorService.execute(scope, outer -> {
				GamaExecutorService.execute(outer, inner -> {
					completed.incrementAndGet();
					return null;
				}, small, parallel);
				return null;
			}, small, parallel);
			check(completed.get() == 64, "Nested per-agent tasks were not all joined");
			check(!shared(scope), "Nested per-agent tasks retained shared frames");

			completed.set(0);
			try {
				GamaExecutorService.execute(scope, s -> {
					try {
						if (s.getAgent().getIndex() == 0) throw new AssertionError("expected task failure");
						Thread.sleep(20);
						return null;
					} catch (final InterruptedException e) {
						Thread.currentThread().interrupt();
						throw new AssertionError(e);
					} finally {
						completed.incrementAndGet();
					}
				}, small, parallel);
				throw new IllegalStateException("Task failure was swallowed");
			} catch (final AssertionError expected) {
				check(completed.get() == small.length, "Failure skipped unfinished per-agent tasks");
				check(!shared(scope), "Failed per-agent tasks retained shared frames");
			}
		}
	}

	private static long median(final Runnable run, final int repetitions) {
		for (int i = 0; i < 5; i++) run.run();
		final long[] times = new long[9];
		for (int i = 0; i < times.length; i++) {
			final long start = System.nanoTime();
			for (int n = 0; n < repetitions; n++) run.run();
			times[i] = (System.nanoTime() - start) / repetitions;
		}
		Arrays.sort(times);
		return times[times.length / 2];
	}

	private static void benchmarkLookups() {
		try (IScope clean = new ExecutionScope(null); IScope locked = new ExecutionScope(null)) {
			clean.addVarWithValue("value", 1L);
			locked.addVarWithValue("value", 1L);
			try (IScope copy = locked.copy("permanently shared")) {
				try (IScope fork = clean.fork("completed run")) {
					fork.setVarValue("value", 1L);
				}
				for (final IScope scope : new IScope[] { locked, clean }) {
					final long nanos = median(() -> {
						long sum = 0;
						for (int i = 0; i < 1_000_000; i++) sum += (Long) scope.getVarValue("value");
						sink = sum;
					}, 3);
					System.out.printf("Enclosing lookups, %s: %.2f ns/read%n",
							scope == locked ? "retained lock" : "released lock", nanos / 1_000_000.0);
				}
			}
		}
	}

	private static void benchmarkExecution(final IAgent[] agents, final int work) throws Exception {
		try (IScope scope = new ExecutionScope(null)) {
			scope.addVarWithValue("seed", 17L);
			final long[] results = new long[agents.length];
			final IExecutable action = s -> {
				long value = (Long) s.getVarValue("seed");
				final int index = s.getAgent().getIndex();
				for (int n = 0; n < work; n++) value = Long.rotateLeft(value * 6364136223846793005L + n, 13);
				results[index] = value;
				return null;
			};
			final Runnable sequential = () -> {
				for (final IAgent agent : agents) scope.execute(action, agent, null);
				sink = results[results.length - 1];
			};
			final Runnable parallel = () -> {
				ParallelAgentRunner.execute(scope, action, agents, 64);
				sink = results[results.length - 1];
			};
			final long serial = median(sequential, 5);
			final long[] expected = results.clone();
			final long concurrent = median(parallel, 5);
			check(Arrays.equals(expected, results), "Sequential and parallel results differ");
			check(!shared(scope), "Parallel execution left synchronization enabled");
			System.out.printf("%d agents, %d iterations/action: sequential %.3f ms, parallel %.3f ms (%.2fx)%n",
					agents.length, work, serial / 1e6, concurrent / 1e6, (double) serial / concurrent);
		}
	}

	public static void main(final String[] args) throws Exception {
		GamaExecutorService.setConcurrencyLevel(4);
		try {
			final IAgent[] agents = new IAgent[2048];
			for (int i = 0; i < agents.length; i++) agents[i] = agent(i);
			checkSharing();
			checkScopeFixes();
			checkThreadBinding();
			checkFailureJoining(agents);
			checkPerAgentTasks(agents);
			System.out.println("Scope-sharing regression checks passed");
			System.out.printf("Java %s; %d processors; pool parallelism %d%n", System.getProperty("java.version"),
					Runtime.getRuntime().availableProcessors(),
					GamaExecutorService.AGENT_PARALLEL_EXECUTOR.getParallelism());
			benchmarkLookups();
			benchmarkExecution(agents, 64);
			benchmarkExecution(agents, 4096);
		} finally {
			GamaExecutorService.AGENT_PARALLEL_EXECUTOR.shutdown();
		}
	}
}
