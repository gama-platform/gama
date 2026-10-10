# Batch regression models

Not automated: open `Batch Regression.gaml`, run each experiment and read the
console. Experiments with checks print `PASS`/`FAIL` lines.

| Experiment | Verifies | Expected |
|---|---|---|
| `until_sequential`, `until_parallel` | each simulation stops on its **own** `until` (#198, #358, #418) | only `PASS` |
| `many_not_kept` | 100 short simulations, `parallel: 4`, `keep_simulations: false` finish; the end-of-run reflex fires (#198, #282) | batch ends by itself; status bar shows "Batch over" |
| `explicit_with` | `method exploration with:` plans (string keys) really apply the parameters | only `PASS` |
| `permanent_reload` | reload closes the old `permanent` display and opens the new one (#199) | one chart only, after several reloads |

Manual checks not expressible in GAML:

- **Threads (#282)**: during/after `many_not_kept`, the number of `Thread of Simulation …` threads (jstack, or the Eclipse
  debug view) must stay about `parallel` and drop to 0 when the batch ends; the JVM/GAMA must not hang when closing.
- **Status bar (#325)**: shows `N simulations finished | R running | W waiting (using T threads)`, updating as it runs.
- **Pause / resume / stop** a running batch: no hung threads.
- **Failing model**: add `reflex boom when: cycle = 3 { int i <- 1 / 0; }` to `global`; the error must appear in the GAMA
  error view and the failing simulation must stop (no endless "running" at the same cycle).
- Compare `exhaustive_exploration` and `explicit_exploration` in `models/Batch Simulation/Exploration.gaml`.
