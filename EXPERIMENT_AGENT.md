# Experiment agent: execution and maintenance

These controls and pause-state migrations are part of the **unreleased 2026
development version**, not GAMA 2025-06. This guide is also published on the
[GAMA wiki](https://github.com/gama-platform/gama/wiki/ExperimentAgent).

`experiment` is the agent managing the current experiment and its simulations.
Execution control belongs here, not to the model species (`global`). From a
simulation or one of its agents:

```gaml
ask experiment { do pause; }
write experiment.status;
```

Inside a `user_command` declared in an experiment, use `of: experiment` to
call `do pause;` directly. A command without `of:` runs in the current
simulation instead. These actions
target the receiving experiment's controller, not whichever experiment happens
to be frontmost. They control all simulations scheduled by that experiment;
they do not independently pause a single simulation. A simulation's `paused`
attribute is deprecated and combines the experiment controller's pause state
with the simulation's user-interaction hold. Use `experiment.is_paused` and
`simulation.is_on_user_hold` to distinguish the two.

## Execution actions

All new controls take no arguments and return the receiving experiment agent.
That return value is **not** a completion acknowledgement. `play` is queued;
`step_once`, `step_back` and `reload` are submitted at the end of the current
experiment cycle when called during execution, and then processed asynchronously
by the controller. When no experiment step is in progress, submission is
immediate. Do not assume that the next GAML statement sees their completed effect.

`pause` normally changes the scheduler state immediately, but is queued when
called from a display thread. A cycle already in progress can still finish:
`PAUSED` does not mean that every executing agent has stopped mid-action.
Display synchronization changes immediately and does not change execution status.

| Action | Effect | UI/server counterpart and constraints |
|---|---|---|
| `play` | Requests continuous execution. Does not toggle; an already running experiment stays running. | UI Play / server `play`. An ended server execution can refuse further play; reload or a new load may be necessary according to its lifecycle. |
| `pause` | Requests a scheduler pause. Already paused stays paused. | UI Pause / server `pause`. Does not abort the current cycle. |
| `step_once` | Requests one experiment step and leaves the controller paused. | UI Step / server `step` with `nb_steps: 1`. A running cycle finishes first; the requested step is additional. In batch mode an experiment step can run an entire exploration, not one model cycle. |
| `step_back` | Requests restoration of the preceding recorded states; leaves the controller paused. | UI Back / server `stepBack` with `nb_steps: 1`. Requires recording and an available recorder. Restores simulations having history; without earlier history there is nothing to restore. |
| `reload` | Disposes the old experiment agent and simulations, creates fresh ones and runs initialization again. | UI Reload / server `reload`. Uses the current experiment definition and parameter values; does not recompile edited source or accept the server command's optional parameter payload. |
| `synchronize` | Makes execution wait for display rendering. | UI Synchronize enabled. **Platform-wide**, shared by experiments. |
| `desynchronize` | Removes the rendering wait; execution and rendering may advance at different rates. | UI Synchronize disabled. **Platform-wide**; some rendered frames can be skipped. |

Execution commands require an active interactive controller: the regular GUI
controller or a server controller. The non-interactive command-line/headless
controller runs to completion and does not implement these controls; requests
raise a runtime error instead of pretending to succeed. "Headless server" and
"non-interactive headless" are different execution modes.

Missing/disposed controllers, unsupported controllers and failed queue
submissions raise runtime errors. A deferred submission can report its error
at the experiment-cycle boundary rather than at the original call site.
Acceptance into the queue does not guarantee successful later execution: normal
controller runtime-error handling still applies. No argument enables blocking
completion, because waiting from a simulation thread could deadlock the
experiment that must finish that simulation's current step.

### Recording and stepping backward

Declare recording on the experiment:

```gaml
experiment Interactive type: gui record: true {
    user_command "One step" of: experiment {
        do step_once;
    }
    user_command "Previous state" of: experiment {
        do step_back;
    }
}
```

Without a recorder, `step_back` raises a runtime error. Recording consumes
memory. Rewinding restores simulation state, not the experiment clock or
external side effects such as written files, sent network messages or database
operations. Random-seed behavior follows the experiment's `keep_seed` setting;
rewinding is not necessarily a guarantee of identical subsequent random draws.

### Reload and lifetime

`reload` replaces the receiving agent. Its return value refers to the **old**
agent. Reacquire `experiment` and `simulation` after reload instead of retaining
them in another experiment or external client.

Reload follows the controller's existing lifecycle. In the GUI it normally
resumes an experiment that was running and honors `autorun`; it normally leaves
a manually paused experiment paused. Server reload behavior follows its own
controller and load configuration. It is not an undo operation for external
effects.

Do not unconditionally call `reload` from `init`: initialization will request
another reload. Likewise, calling `step_once` every cycle deliberately keeps
requesting more steps rather than providing a stable pause.

## Read-only status

| Attribute | Type | Meaning |
|---|---|---|
| `status` | `string` | `"RUNNING"`, `"PAUSED"` or `"NONE"` from the same query used by server `simulation_status_request`. |
| `is_paused` | `bool` | Experiment scheduler pause state, equivalent to `status = "PAUSED"`. Does not include simulation user holds; `false` when status is `"NONE"`. |
| `is_synchronized` | `bool` | The current platform-wide rendering synchronization flag. |

`NONE` means that no controller exists or that it is disposing. `NOTREADY` is
a transient lifecycle notification delivered to state listeners, not a value
returned by this query. `RUNNING`/`PAUSED` describe controller state; they do not
encode initialization errors, server stop-condition completion, wall-clock
progress or a particular simulation's local pause flag. Server completion is
reported separately through its existing lifecycle messages.

These attributes cannot be assigned. Change execution through actions and
rendering synchronization through `synchronize`/`desynchronize`.

On simulations, read-only `is_on_user_hold` reports the existing user-interaction
hold flag, for example while a user panel waits for input. It is not an
independent scheduler-pause control: user-interaction controls manage it.
`simulation.paused` remains a deprecated read-only compatibility query combining
the controller pause and simulation hold. Its ineffective setter has been
removed: assignments now fail validation instead of silently doing nothing.

### Scheduler pause versus user hold

The two flags answer different questions. For an active experiment controller:

| `experiment.is_paused` | `simulation.is_on_user_hold` | Deprecated `simulation.paused` | Meaning |
|---|---|---|---|
| `false` | `false` | `false` | Neither a scheduler pause nor a user hold. |
| `true` | `false` | `true` | Experiment scheduler paused; no user hold on this simulation. |
| `false` | `true` | `true` | This simulation is waiting for user interaction; the controller can still report `"RUNNING"`. |
| `true` | `true` | `true` | Both scheduler pause and user-interaction hold apply. |

`is_on_user_hold` initially defaults to `false`. It reports the receiving
simulation's flag, not the experiment agent's flag or another simulation's
flag. User controls set the flag while waiting and clear it when released.
A user panel can wait **inside** a cycle: this does not change the experiment's
controller status to `"PAUSED"`. `play` does not dismiss such a panel, and
`pause` does not clear the hold. Release the user control through its normal
interaction, then use `play` if the experiment scheduler is also paused.

Neither `is_paused` nor `is_on_user_hold` is writable. In particular, do not
replace the old ineffective `paused <- true` assignment with
`is_on_user_hold <- true`: use `ask experiment { do pause; }` if the intention
is to pause execution. There is no new attribute-based independent pause
mechanism for an individual simulation.

```gaml
write experiment.is_paused;
write simulation.is_on_user_hold;
```

When `experiment.status = "NONE"`, `experiment.is_paused` is `false`; this is
**not** evidence of a running experiment. Check `status` when controller
availability matters. A retained disposed simulation reference is not a
supported way to inspect the newly reloaded simulation.

```gaml
ask experiment {
    if is_paused {
        do play;
    } else if status = "RUNNING" {
        do pause;
    }
}
```

Use this toggle from a user command or display event. A paused experiment no
longer schedules model reflexes, so a reflex cannot later resume itself.
See the library recipe **User Pause and Resume** for a clickable example.

Synchronization is about **rendering**, not synchronization between agents,
simulations, wall-clock time or remote clients. Without displays there is no
rendering to wait for. Output configuration, synchronized-display preferences
and initialization can also set this shared flag.

## Existing maintenance and lifecycle actions

| Action | Arguments | Effect and return value |
|---|---|---|
| `update_outputs` | Required `recompute: bool` | `false` refreshes the presentation of active experiment/simulation outputs without recomputing their values. `true` runs an output-manager step: refreshable outputs recompute and refresh according to their refresh conditions, and autosave is evaluated. Does not advance simulation agents or clocks. Returns the experiment. |
| `update_parameters` | None | Refreshes the GUI Parameters view from current values. Does not change values, reload or step simulations. This view is a frontmost-experiment GUI facility, not a separate view for background/server experiments. Without the view there is no visible effect. Returns the experiment. |
| `compact_memory` | None | Requests JVM garbage collection **across GAMA**. The JVM may defer or ignore it. Does not delete reachable agents, outputs or recorded history; can cause a platform-wide pause. Returns the experiment. |
| `die` | None | Closes this experiment, disposing its simulations, outputs and controller. Returns `nil`. Does not quit GAMA. No redundant `exit` action is provided. |

```gaml
ask experiment {
    do update_outputs recompute: false;
    do update_parameters;
}
```

Use `recompute: true` only when evaluation is intended: output expressions can
have side effects and output-manager stepping can trigger autosave. Calling
`compact_memory` every cycle is not a substitute for managing retained data.

```gaml
ask experiment { do die; } // Close only this experiment.
```

The platform shutdown action is different: `ask gama { do die; }` quits GAMA.

Experiments also inherit ordinary agent actions: `debug(message, separator,
end)` writes diagnostics; `tell(msg, add_name)` requests an information dialog;
`error(message)` requests an error dialog. Optional `debug` separators/endings
default to newlines; `tell` includes the agent name unless `add_name: false`.
These use the configured GUI/server handlers and are not execution controls.
Their return values are their message strings. The inherited `_init_` and
`_step_` are lifecycle primitives: calling `_step_` manually is **not**
equivalent to a controller-managed `step_once`. Overriding them replaces the
normal initialization/stepping behavior; see the library recipe **Experiment
and simulation**.

## Existing experiment attributes

Execution control complements, rather than replaces, these existing attributes:

| Attribute | Purpose |
|---|---|
| `simulations`, `simulation` | All current simulations and the most recently created/current simulation; the latter is `nil` before one exists. |
| `parameters` | Current experiment parameter values. Updating parameter values and refreshing their GUI presentation are separate operations. |
| `cycle` | The experiment's own cycle counter. Do not confuse it with `simulation.cycle`, especially with multiple simulations or batch runs. |
| `seed`, `rng`, `usage` | Experiment random-generator state, separate from each simulation's generator. Setting `seed` reinitializes the generator; zero requests a random seed. |
| `minimum_cycle_duration` | Minimum wall-clock duration of a cycle in **seconds**, default `0`. Can slow execution but does not alter simulated time. |
| `maximum_cycle_duration` | Upper range of the GUI speed control in **seconds**, default `1`; not a timeout that aborts long-running cycles. |
| `is_gui` | Whether the experiment is GUI-based; not a controller-capability or running-status query. |
| `model_path`, `project_path` | Read-only absolute containing-folder paths, ending in a path separator. |

## Experiment and simulation contexts

These ownership changes are in the **unreleased 2026 development version**.
The experiment is a controller agent; each simulation is a separate agent with
its own model globals, clock, random generator and populations. An experiment
can manage several simulations. `experiment.simulation` is its currently selected
simulation, initially the most recently created one; it can change and can be
`nil` when no simulation exists. It is not a synonym for every simulation.

### Creating and accessing agents from an experiment

Ordinary model species belong to a simulation, not to the experiment. Code
executing directly on the experiment must explicitly select a simulation before
creating agents or looking up its populations:

```gaml
model Slicing

species dummy;

experiment slicing_tests type: test {
    matrix<dummy> md;
    list<dummy> ld;

    init {
        ask simulation {
            create dummy number: 9;
            // dummy(i) is resolved in this simulation, not in the experiment.
            experiment.md <- matrix(range(0, 2) collect
                (c: range(0, 2) collect (r: dummy(c + r * 3))));
            experiment.ld <- as_list(experiment.md);
        }
    }
}
```

In the `ask` block, creation and population lookup use the selected simulation;
`experiment.md` and `experiment.ld` still address the experiment's attributes.
Keep the explicit receiver when assigning experiment state from a simulation.
If the intended simulation is a particular member of `experiment.simulations`,
ask that agent instead of assuming that `simulation` identifies it. Ensure that
the target exists before asking it: some experiment types, notably batch
experiments, do not automatically create an initial simulation.

Direct `create dummy` in experiment code no longer implicitly creates agents in
the current simulation. The compiler also rejects direct population operations
on a known model species in experiment context, such as `length(dummy)`,
`empty(dummy)`, `dummy.population`, `dummy collect each.energy`, `dummy[0]`, `dummy(0)`,
`list(dummy)`, `loop a over: dummy` and `ask dummy`. Move the operation inside
an explicit `ask simulation` (or ask a particular simulation). A species name
remains usable as a type or species value without accessing its population.
Access through a dynamically determined species can instead fail at runtime
rather than silently selecting a simulation or returning an empty population.
The simulation population itself remains
accessible to the experiment. Creating an experiment from a directly imported
comodel remains a separate supported operation; it does not make ordinary
model-species populations experiment-owned.

### Parameters and user commands

For `parameter` and `user_command` **directly declared in an experiment**,
omitting `of:` is equivalent to `of: simulation`. `of: experiment` selects the
experiment. Names and actions are resolved against that owner, including when
both owners have an attribute or action with the same name; there is no
implicit fallback to the other owner.

```gaml
model owners

global {
    int amount <- 1;
    action increase() { amount <- amount + 1; }
}

experiment interactive type: gui {
    int amount <- 100;
    action increase() { amount <- amount + 10; }

    parameter "Simulation amount" var: amount;
    parameter "Experiment amount" of: experiment var: amount;

    user_command "Increase simulation" {
        do increase;
    }
    user_command "Increase experiment" of: experiment {
        do increase;
    }
    user_command "Pause experiment" of: experiment {
        do pause;
    }
}
```

The first parameter and command address the simulation's `amount` and
`increase`; the second pair address the experiment's. In simulation-owned
command code, use `experiment.amount` or `ask experiment { ... }` for an
explicit cross-owner access. Conversely, an experiment-owned command can use
`ask simulation { ... }` to act on the currently selected simulation.
Parameter configuration before a simulation exists has its own documented
initial-value behavior: simulation parameters use configured parameter values
and other globals' initial values, not a running simulation; `self` and
`world` are unavailable in that configuration context. It does not create a
simulation.

For a command, `of:` selects the context for its body, `action:`, `with:`
arguments and dynamic `when:`, `color:` and `continue:` facets. A command
entered through the experiment's parameters panel selects the current simulation
when clicked; when already invoked in a simulation scope it keeps that
simulation. It does **not** execute on every simulation. If the required
simulation is unavailable or disposed, execution reports an error rather than
running on the experiment; the command's enablement check returns false.
Experiment-owned commands do not require a simulation.

For the legacy `action:` form, both the action and each `with:` value use the
selected owner. For example, given an `adjust(int delta)` action and a
`change_amount` attribute on both owners:

```gaml
user_command "Adjust simulation" action: adjust with: [delta::change_amount];
user_command "Adjust experiment" of: experiment action: adjust with: [delta::change_amount];
```

The first resolves both names on the simulation and the second on the
experiment. Use `when:` similarly for an owner-specific condition. Inline
command blocks are the preferred alternative to the older `action:` form.

`of:` on `user_command` is only accepted for commands directly inside an
experiment. Commands in `global`, a species or a `user_panel` retain their
existing declaring-agent context. This change does not add `of:` to `monitor`.

**Migration:** add `of: experiment` to existing experiment commands intended to
use experiment actions or attributes. Previously, a command's `action:` could
fall back to an action in `global` and execute it on **all** simulations; it
now targets only the selected simulation by default. If all simulations should
be modified, explicitly iterate over the intended simulations. Qualify
cross-owner references rather than relying on implicit name lookup.

### Batch methods and experiment facets

Facet expressions are not all evaluated on the agent that structurally
contains their declaration:

| Declaration | Compilation and runtime meaning |
|---|---|
| Experiment `until:` in a batch | Resolves model globals and is evaluated for the simulation being stopped. |
| Experiment `record:` | Resolves model globals and is evaluated for each simulation when recording is considered. Batch recording is ignored. |
| Batch method `minimize:` / `maximize:` | Resolves model globals and actions; evaluates the fitness in **each completed simulation** as its result is collected. |
| Exploration method `outputs:` | The list entries identify simulation attribute **names**, read from each completed simulation. Bare names and string names are supported; arbitrary expressions are not evaluated as output values. |
| Other batch method configuration facets | Resolve in the experiment context; they configure the exploration or optimization, not an individual simulation's fitness. |

The fitness rule applies to `genetic`, `annealing`, `hill_climbing`, `tabu`,
`reactive_tabu` and `pso`. The output-name rule applies to `exploration`,
`betad`, `morris`, `sobol` and `stochanalyse`, where the method exposes
`outputs:`.

```gaml
model batch_context
global {
    float x <- 0.5;
    float loss <- 0.0;
}

experiment optimize type: batch until: cycle >= 100 {
    int iterations <- 20;
    parameter "x" var: x min: 0.0 max: 1.0;
    method hill_climbing iter_max: iterations minimize: loss;
}
```

Here `iterations` is experiment-owned, whereas `cycle` and `loss` refer to
the simulation being evaluated. A same-named experiment attribute does not
replace a model global in a model-context facet. An experiment-only attribute
must be accessed explicitly, for example `minimize: loss + experiment.offset`.
In particular, `experiment.simulation` is not necessarily the completed
simulation whose fitness or output is being collected, especially with grouped
or parallel runs. `outputs: ["loss"]` names an attribute; use a model attribute
to store a computed quantity if that quantity must be tracked. An unavailable
attribute name produces a `nil` result rather than evaluating an expression.

For example, a GUI experiment can use `record: cycle mod 10 = 0` to decide
separately for each simulation whether its current state should be recorded.
`record:` remains unavailable for batch recording.

## Migration from simulation pause/resume and paused

The model-species actions `pause` and `resume` remain available for compatibility
but are deprecated in GAML documentation/validation and Java. They delegate to
the owning experiment's new controls, preserving their legacy `nil` return.
They never offered independent control of one simulation.

| Deprecated | Replacement |
|---|---|
| `do pause;` in `global`, or `ask simulation { do pause; }` | `ask experiment { do pause; }` |
| `do resume;` in `global`, or `ask simulation { do resume; }` | `ask experiment { do play; }` |
| Reading `paused` for scheduler state | `experiment.is_paused` |
| Reading `paused` for a user-interaction hold | `is_on_user_hold` on the simulation |
| Assigning `paused <- true` (formerly ineffective) | `ask experiment { do pause; }` to pause the experiment |
| Assigning `paused <- false` (formerly ineffective) | `ask experiment { do play; }` to resume the experiment |

To preserve the combined meaning of the old query, use
`experiment.is_paused or simulation.is_on_user_hold` while the experiment has
an active controller. Neither replacement attribute is assignable.

Existing model-species `sleep(duration)` is **not** a scheduler pause:
it blocks the calling execution thread for a machine-time duration (seconds by
default; explicit units such as `10#ms` are accepted). It does not replace these
experiment controls.
