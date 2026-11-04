PERFORMANCE AND STRESS BENCHMARKS - PROTOCOL 2
============================================

These are long scaling experiments, not short demonstrations. Five workloads
cover population execution, spatial queries, containers/retained heap, dense
matrices and synchronized displays/charts. Use identical sources on both GAMA
versions. There are no external datasets or required optional extensions.

WARNING: full sweeps can take hours. Stress combines growing memory demand and
growing computational work, and may exhaust the Java heap or make GAMA
unresponsive. The highest level is deliberately not assumed to be safe.
Start at low levels; do not run several experiments concurrently.

Long, progressively heavier runs
--------------------------------
Every plateau has 300 warm-up cycles and 5000 measured cycles by default,
with a sample every 250 measured cycles. Cycle counts are fixed, not adapted
to elapsed time: two versions perform the same work at each level. Slower
versions and higher levels therefore run longer. This is not a minimum
wall-clock-duration guarantee. Increase measured_cycles for longer runs.

The four compute models expose native batch experiments:

  Replications: SIZE sweep, levels [0,1,2,3,4,5], three fresh simulations/level.
  Operations:   OPERATION sweep, the same levels and three simulations/level.
  Stress:       BOTH axes together, the same levels, one simulation/level.

The multiplier is 2^level: 1, 2, 4, 8, 16, 32. Each batch has only one explored
parameter, load_level; there is no Cartesian product of sizes and operation
counts. Runs are sequential (parallel: false) and stop with until: finished.
The batch controller advances without a simulation pause action.
18 simulations in Replications/Operations means 95,418 scheduled cycles,
including each simulation's closing boundary. Stress has six simulations.

Benchmark is a single unprofiled GUI run at a selected load_level/load_axis.
Profile inherits it and uses GAMA's native benchmark: true facet. Closing
Profile exports statement/operator counts and durations to models/benchmarks/.
Profiling includes initialization and warm-up and adds overhead: compare
Profile with Profile, not with unprofiled results.

The visual Benchmark/Profile experiments ramp inside ONE simulation, from
first_level=0 to last_level=5. Each plateau has its own warm-up and measurement.
Particles and chart history persist as load grows; new agents are allocated
between measurements. Set first_level=last_level for an isolated plateau.

Workload definitions and default size sweep
-------------------------------------------
Agent Execution:
  base_population=5000, base_operations=10 action calls/agent/cycle.
  Size: populations 5000, 10000, 20000, 40000, 80000, 160000.
  Operations: 10, 20, 40, 80, 160, 320 calls at a fixed 5000 agents.
  Sequential reflex/action dispatch, attribute access and arithmetic.
  Stress level 5: 160000 agents x 320 calls = 51.2 million calls/cycle.

Spatial Queries:
  base_population=2000, base_queries=100, query_radius=20, world side=1000.
  Size: populations 2000, 4000, 8000, 16000, 32000, 64000.
  Operations: 100, 200, 400, 800, 1600, 3200 queries/cycle.
  All sites move every cycle. Radius/nearest queries, geometry construction,
  intersection and area are executed for each target.
  Fixed world area/radius makes size levels increase neighborhood density,
  not just index size. This intentionally stresses spatial result allocation.
  Stress level 5: 64000 moving sites and 3200 queries/cycle.

Containers and Allocation:
  base_size=4000, base_operations=20, base_survivors=64.
  Size: source lists 4000 .. 128000 and survivor slots 64 .. 2048.
  Operations: 20 .. 640 iterations at fixed source size/retention.
  Copy, append/remove, filter, collect, sort, map creation and numeric casts.
  Source size AND retained-list count increase on the size axis: retained
  element capacity grows approximately as multiplier squared. The ring is
  bounded per level, but its highest levels intentionally stress the heap.
  This is allocation/retention throughput, NOT a direct GC timing benchmark.
  JVM GC logs are needed to distinguish GC pauses from other latency spikes.

Matrix Computation:
  base_side=256, base_operations=10.
  Size: sides 256, 362, 512, 724, 1024, 1448. Area approximately doubles;
  integer truncation of base_side * sqrt(2^level) is intentional.
  Operations: 10 .. 320 iterations at fixed side 256.
  Dense element-wise arithmetic, transposition, reductions, indexed updates.
  Two persistent float matrices and multiple dense temporaries pressure the
  heap. These are not sparse matrices or matrix multiplication/FLOP timings.

Displays and Charts:
  base_population=2000, base_series=16, base_draw_copies=1.
  Size: 2000 .. 64000 particles, 16 .. 512 time-series charts' series.
  Operations: 1 .. 32 geometry draws/particle/view, fixed population/series.
  Both a 2D view and an OpenGL extruded view draw every particle; a third
  view plots series. Positions/heights change every cycle.
  Chart history is not discarded between plateaus: long histories and
  increasing series count deliberately add sustained memory/rendering load.
  Stress combines more particles/series with more draws, in the same run.

Finding a limit
---------------
1. Run Replications to locate nonlinear growth with problem size, then
   Operations to locate throughput saturation at fixed size.
2. Look at window averages and maximum cycle latency, not only the final
   mean. Check whether the heap sampled at each window keeps growing.
3. Run Stress only after the separate sweeps. To go beyond level 5, extend
   load_level's batch among list (for example through 6,7,8), or choose a
   higher GUI level. GUI bounds are 0..10; they are not memory safety limits.
   Increase base sizes or cycle counts when a machine still handles these
   levels. Keep identical level lists and bases for version comparisons.
4. A START record is persisted BEFORE allocating the requested workload.
   Every WINDOW is also flushed immediately. If a run fails or is interrupted,
   its START/WINDOW rows remain; do not call an unfinished run a speedup.
   Report the last completed level AND the failed/interrupted level, heap
   limit, elapsed observation time and failure/timeout reason.
5. There is no forced GC, silent downscaling, early success or automatic
   timeout. Interrupt manually if necessary. Out-of-memory or very long
   cycles cannot be safely recovered from by a GAML reflex.

Timing and result files
------------------------
The global measurement reflex observes boundaries BEFORE population stepping.
The default interval covers complete cycles 300 through 5299; cycle 5300
records the last duration and final result without doing more workload.
All compute workloads stop at this boundary. For visual ramps the same
boundary begins the next plateau's warm-up instead.

Timing uses gama.machine_time in milliseconds. Each interval includes
simulation/population execution and synchronized output since the previous
boundary. Measurement bookkeeping and console/CSV reporting are excluded by
starting the next interval after reporting. Initialization/plateau growth
is excluded; its memory demands are still part of the stress experiment.
Sub-millisecond cycles can quantize to zero: use window aggregates and
longer/larger workloads, not the maximum alone.

Results are appended to results/{agents,spatial,containers,matrices,displays}.csv
(ignored by Git). Each run/plateau emits START, WINDOW and RESULT to console
and CSV. No header is written; the CSV column schema is:

  phase, run_id, GAMA version, seed, axis, level, [workload columns],
  warmup_cycles, requested_measured_cycles, completed_measured_cycles,
  elapsed_ms, mean_ms_per_cycle, window_mean_ms_per_cycle, max_cycle_ms,
  work_units_per_second, sampled_used_heap_MB, sampled_peak_heap_MB, checksum

Workload columns:
  agents:     population, calls_per_agent
  spatial:    population, queries_per_cycle, radius
  containers: source_size, operations_per_cycle, survivor_slots
  matrices:   matrix_side, operations_per_cycle
  displays:   population, series_count, draw_copies_per_particle_per_view

run_id combines simulation name and startup machine time; levels identify
plateaus within a visual run. WINDOW reports its window mean and cumulative
mean/max. RESULT carries the final checksum; its window mean is zero (the
last window was already emitted). START has zero measured work. A zero
throughput with zero elapsed time is an unmeasurable interval, not a success
claim. At partial windows, division uses their actual cycle count.

Work units are calls, queries, source elements processed, matrix cells
processed, or geometry draws respectively. They are normalized throughput
indicators, not instruction counts, FPS or FLOPs. Spatial throughput also
includes population movement overhead; visual throughput includes charts.
Heap is JVM-wide used Java heap (max_memory - free_memory), sampled at reports;
its sampled peak is NOT a precise allocation counter, process RSS or GPU memory.
CSV I/O errors are surfaced by GAMA; output is never silently discarded.

Reproducibility and portability
--------------------------------
Reload for each independent comparison; use the same bases, axis, level,
warm-up, measured/sample counts, execution mode and source protocol.
Compute models reset Mersenne Twister to seed 42 before initialization AND at
cycle zero, because a batch can replace the seed after init. Thus setup and
stepping have separate repeatable streams in batch and GUI. Checksums include
warm-up state. Compare checksums only for identical workloads; floating-point
results need tolerance. Protocol 2's spatial stream differs from protocol 1.

Keep Java version, JVM heap/GC flags, GAMA revision, CPU/power mode, OS and
background load fixed or record their differences. Compare medians and spread
of completed replications at EACH level, plotting ms/cycle and throughput
against actual size/operations. Do not merge different levels into one median.
These fixed warm-ups are starting points, not proof of JIT steady state.

For visual comparisons, use the desktop GUI and keep all three views visible.
Use identical pixel sizes, scaling, camera, anti-aliasing, VSync, GPU/driver
and renderer/backend. Do not resize or interact during a run. Compare the
same starting/ending levels: retained chart history makes a standalone level
different from the same level reached through a ramp.
Visual checking remains manual; headless results are not rendering evidence.

Headless compute:
  ./gama-headless.sh -batch Replications "/path/to/Agent Execution.gaml"
Substitute Operations or Stress for the other sweeps.
For one GUI-style compute experiment:
  ./gama-headless.sh -xml Benchmark "/path/to/Agent Execution.gaml" run.xml
Set finalStep="5301" (warmup + measured + 1), then:
  ./gama-headless.sh run.xml /path/to/headless-output
The model CSV path is independent of the runner's output directory.

The five entry models and their folder can be copied into a GAMA 2025
workspace. They use established core syntax, native batch experiments and
benchmark: true, and type: opengl instead of backend-specific newer syntax.
Execution on GAMA 2025 remains to be verified; preserve workload/measurement
semantics when porting. No newer benchmark returns:/console: syntax is needed.

These workloads do not isolate serialization, server latency, optimizers or
filesystem throughput. Batch is the execution driver here, not an independent
benchmark of exploration algorithms.
