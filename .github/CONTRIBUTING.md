## How to contribute to GAMA

#### **Did you write a patch that fixes a bug or adds a new feature?**

* Open a new GitHub pull request with the patch if you think your addition is worth being included in the release.

* Ensure the PR description clearly describes the problem and solution or the new addition. Include the relevant issue number if applicable.

#### **Do you intend to add a new feature or change an existing one?**

* Suggest your change in the [gama-dev mailing list](https://groups.google.com/forum/#!forum/gama-dev) and start writing code.

* Do not open an issue on GitHub until you have collected positive feedback about the change. GitHub issues are primarily intended for bug reports and fixes.

### Runtime errors and warnings

Creating an exception, aborting an operation, and reporting a problem to the user are separate actions.
`GamaRuntimeException.error(...)`, `.warning(...)`, and `.create(...)` produce or wrap exceptions; they do not themselves
deliver them to the runtime error handler. Wrapping a Java exception may also print diagnostic stack traces.

Choose the handling strategy according to whether the current operation can safely continue:

| Situation | Handling |
| --- | --- |
| The operation cannot produce a valid result or safely continue | Throw a `GamaRuntimeException` and let the enclosing execution boundary report it. |
| A recoverable condition has a defined fallback or safe continuation | Call `GAMA.reportAndThrowIfNeeded(...)`, then explicitly implement that fallback or continuation if the call returns. |
| An execution boundary catches an exception | Add relevant context and use `GAMA.reportAndThrowIfNeeded(...)` to apply reporting and execution policy. |
| A caller only needs reporting and manages control flow itself | Use `GAMA.reportError(...)`; it returns a continuation decision but does not itself throw or pause the experiment. |

For example, an invalid operand should not be used after reporting:

```java
if (matrix == null) {
    throw GamaRuntimeException.error("Matrix cannot be nil", scope);
}
```

A recoverable warning can instead have an explicit fallback:

```java
if (index < 0 || index >= values.size()) {
    GAMA.reportAndThrowIfNeeded(scope,
            GamaRuntimeException.warning("Index outside the bounds of the list", scope), false);
    return defaultValue;
}
```

**A direct throw does not necessarily bypass reporting.** In normal model execution,
[`AbstractStatement.executeOn`](../gama.api/src/gama/api/gaml/statements/AbstractStatement.java) and
[`ExecutionScope`](../gama.api/src/gama/api/runtime/scope/ExecutionScope.java) catch exceptions and route them through the
reporting machinery. However, a throw has already abandoned the current operation before reaching that boundary.
Depending on the boundary, execution can return `null` or a failed execution result even when the simulation is allowed
to continue. An intervening catch can consume an exception; code running outside these boundaries must arrange its own
handling. Source-site counts of direct throws therefore do not measure the proportion of unreported errors.

#### Reporting and execution policy

In the normal live-experiment reporting path,
[`GAMA.reportAndThrowIfNeeded`](../gama.api/src/gama/api/GAMA.java) adds agent context, records the current error, and
delegates to `reportError`. The configured GUI, headless, or server handler receives the report.

* For errors, throwing/pausing requires both `shouldStopSimulation` and **Stop simulation at first error**.
* For warnings, throwing/pausing requires both **Treat warnings as errors** and **Stop simulation at first error**.
  The `shouldStopSimulation` argument does not control warning escalation; passing `false` does not guarantee that a
  warning will never throw.
* In GAML `try` mode, the helper throws to the model's `catch` before delivering a report, so handled conditions need
  not appear as runtime errors.
* Delivery is conditional: display/output reporting can be disabled, null-scope and some non-reporting-scope paths only
  print a stack trace, and experiment-lifecycle checks can prevent delivery. File exceptions receive special handling
  when scope reporting is disabled.
* An exception already marked as reported is not delivered again, but can still be rethrown according to policy.

**Do not mechanically replace direct throws with reporting calls.** The helper can return normally, including when
stopping at errors is disabled. Continuing after a failed precondition can cause secondary exceptions, invalid results,
or partial updates. In particular, review directly thrown warnings to decide whether they should abort the operation or
continue with a documented fallback; warning severity alone does not make continuation safe.

Keep the exception factories available for constructing and propagating failures. Report recoverable conditions locally,
throw to abort invalid operations, and report propagated exceptions at execution boundaries. See
[#398](https://github.com/gama-platform/gama/issues/398) for the discussion motivating this guidance.

<br/>


Thanks!

GAMA Team
