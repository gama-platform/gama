# Using GAMA Server

GAMA's WebSocket server can control experiments in the **desktop GUI as well as headless mode**. Both modes accept the same `load` options. In GUI mode, the experiment opens normally with its displays and parameter view, but the client can choose its initial parameter values and receive simulation messages.

For the complete command protocol and headless startup instructions, see [Headless Server](https://gama-platform.org/wiki/next/HeadlessServer).

### Headless command-line help and server options

Run `gama-headless.bat -help` on Windows or `./gama-headless.sh -help` on Linux/macOS to print the local command-line options. Running either launcher with no arguments prints the same help. These are process-startup options, not WebSocket requests:

| Option | Purpose |
|---|---|
| `-m <memory>` | Set the maximum memory (for example, `2048m`). |
| `-ws <path>` | Use the specified workspace. |
| `-c`, `-v`, `-p` | Enable the XML parameter console, verbose mode, or external-framework pipeline. |
| `-hpc <cores>` | Set the number of cores available for experimentation. |
| `-steps <count>` | Set the number of steps to run. |
| `-socket <port>` | Start an unencrypted WebSocket server. |
| `-ssocket <port>` | Start a secure WebSocket server. |
| `-jks <path> -spwd <password> -kpwd <password>` | Configure the secure server's keystore and passwords. |
| `-ping_interval <ms>` | Set the server ping interval; `-1` disables pings. |
| `-no-delay` | Disable delayed WebSocket message sending. |
| `-server-log`, `-no-server-log` | Enable or disable exchange logging for this launch. Disable takes precedence. |
| `-server-log-directory <path>` | Select the exchange-log directory; this does not enable logging. |

There is no WebSocket `help` command: a connected client cannot query a server for its launch options. The client protocol operates after startup and is documented below, including the `load` options, experiment commands, status subscriptions and server-log control. Plug-ins may contribute additional commands, so the command set can vary by installation.

For legacy `-xml <experiment> <model.gaml> <parameters.xml>` generation and
`-gaml <experiment> <model.gaml> <output-directory>` execution, experiment names are case-sensitive.
An unknown name produces an error identifying the requested experiment and model, lists the available
non-batch experiments, and exits with a nonzero status. Batch experiments use `-batch` instead.

## Enable the desktop server

In GAMA's preferences, open **Network > Server mode**, enable **Enable GAMA server mode**, and check **Port to which GAMA server is listening** (default: `1000`). Connect to `ws://localhost:1000`, substituting your configured port.

The GUI server has one current experiment, shared with the desktop user; it does not create an independent simulation for every connection. Loading another experiment replaces the current one, subject to GAMA's normal close-confirmation dialog. Connecting another client does not transfer the loaded experiment's output to that client.

Use this server on a trusted network. Clients can load local models and execute GAML; the desktop WebSocket endpoint is not an authenticated public service.

## Platform preferences over WebSocket

GUI and headless servers accept `get_preferences` and `set_preference` **without loading an experiment**.
These commands operate on GAML-accessible preferences registered in the running installation, including plug-in
preferences. Internal preferences that are not exposed in GAML cannot be read or changed through these commands.

Read selected preferences, or omit `names` to discover all remotely exposed preferences:

```json
{"type":"get_preferences","names":["pref_experiment_ask_closing"]}
```

The `CommandExecutedSuccessfully` response's `content` is an object keyed by preference name. Each entry contains
`value`, `type`, `description`, `restart_required`, `min`, `max`, and `among`. Unspecified constraints are `null`.
An empty `names` array selects nothing; unknown or unexposed names cause the entire read to fail.

To suppress the GUI's experiment-replacement confirmation, send this command and **wait for its successful response
before sending `load`**:

```json
{"type":"set_preference","name":"pref_experiment_ask_closing","value":false}
```

Changes are runtime-only by default. Add `"persist":true` to save the value for subsequent launches. The response's
`content` contains the same metadata as a read, plus `name` and `persisted` (whether persistence was requested and
completed). `restart_required` means the new value is stored but may not take effect until GAMA restarts; the command
never restarts GAMA automatically. Setting the current value is harmless and can still explicitly persist it.

Values use the following JSON representations, both when reading and writing:

| Preference type | JSON value |
|---|---|
| `bool` | Boolean, not `"true"` / `"false"` strings. |
| `int` | Integral number in the signed 32-bit range. |
| `float` | Finite number. |
| `string` | String, including for enumerated string preferences. |
| `rgb` (color) | `[red, green, blue, alpha]`, integer components in `0..255`; writes also accept three components (alpha defaults to `255`). |
| `font` | `{"name":"Helvetica","style":0,"size":12}`; style `0..3` (plain, bold, italic, bold italic), positive integer size. |
| `point` | `[x,y,z]`; writes also accept `[x,y]` with `z=0`. |
| `date` | ISO date/time string. |
| `file` | Non-empty server-side path string. |

Missing fields, null values, incorrect types, unsupported write types, unknown keys, and values outside declared ranges
or allowed choices return `MalformedRequest`. Listener vetoes or update/persistence failures return
`UnableToExecuteRequest`. Persistence or listener failures can occur after an in-memory change; read the preference
again to determine its effective value. Changes use normal preference listeners, on the GUI thread when applicable.

**Preferences are global to the running GAMA instance**, not to an experiment or connection. They affect the desktop
user and other clients and are not restored on disconnect. Only trusted clients should access the server: GAML
visibility is not an authorization mechanism, and any connected client can modify exposed preferences.
There are no batch writes or preference-change subscriptions.

## Server exchange logs

**Exchange logging is off by default**, in both GUI and headless modes, including secure (`wss://`) headless servers. When enabled, each server run creates a separate UTF-8 JSON Lines file in **`~/.gama/server-logs/`**, outside the temporary headless workspace. The startup console prints its absolute path, for example:

```text
GAMA server exchange log: /home/alice/.gama/server-logs/server-1000-123456789.jsonl
```

The log records every received and sent WebSocket frame, including malformed JSON requests, command replies, asynchronous simulation output/errors, broadcasts (one record per recipient), binary data, fragmented messages, and ping/pong/close frames. Logging happens below command processing, so messages rejected or consumed by listeners are also captured. It does not enable simulation output that a client has chosen not to receive.

### Enable logging or change its directory

In desktop preferences, open **Network > Server mode**:

| Preference | Default |
|---|---|
| **Log all GAMA server exchanges** | Disabled |
| **Server exchange log directory (restart required)** | `~/.gama/server-logs` |

Changing the logging preference takes effect immediately on a running GUI server, without restarting GAMA or disconnecting clients, and is saved for future launches. If enabling fails, an error is shown and the preference change is rejected. The directory preference still requires restarting the server; toggling logging off and on keeps the same run's file.

In headless mode, use launch-only options instead:

```sh
./gama-headless.sh -socket 1000 -server-log
./gama-headless.sh -socket 1000 -server-log -server-log-directory "/path/to/server logs"
./gama-headless.sh -socket 1000 -no-server-log
```

On Windows, use the same options with `gama-headless.bat`. These options also work with `-ssocket` secure servers and do **not** modify saved preferences.

| Headless option | Effect for this launch |
|---|---|
| `-server-log` | Enable exchange logging. |
| `-no-server-log` | Disable exchange logging, even if `-server-log` is also present. |
| `-server-log-directory <path>` | Override the output directory; does not itself enable logging. Quote paths containing spaces. |

Launch options take precedence over JVM properties, which take precedence over saved preferences. With no launch overrides, the existing settings are retained. For either mode, the optional JVM properties remain available: `-Dgama.server.log=true`, `-Dgama.server.log=false`, and `-Dgama.server.log.directory=/absolute/path/to/logs`.

These precedence rules determine startup behavior. A subsequent GUI preference change or remote command overrides the running state immediately.

### Enable or disable logging over WebSocket

In GUI or headless mode, send a **`server_log`** command to toggle logging immediately, without restarting or loading an experiment:

```json
{"type":"server_log","enabled":true}
```

To disable it:

```json
{"type":"server_log","enabled":false}
```

`enabled` is required and must be a JSON boolean, not a string. A successful response has type `CommandExecutedSuccessfully` and `content` containing `enabled` and `log_file` (the absolute server-side path, or `null` if no log file has been created). Invalid requests return `MalformedRequest`; file creation/write failures return `UnableToExecuteRequest`.

This changes logging **for the whole running server, including all connected clients**, not only the requesting connection. Any connected client can toggle it; use this only with trusted clients. It overrides the current launch setting but does not change JVM properties or saved preferences. The next server launch uses its normal configuration again. The output directory remains the one configured locally; this command cannot select an arbitrary remote file path.

Enabling starts recording subsequent traffic, including the enable response; earlier exchanges cannot be recovered. Disabling records the request and a `logging_disabled` boundary, then closes the file before replying, so the disable response is not recorded. Re-enabling appends to the same run's file with a `logging_enabled` boundary and continuing sequence numbers. Repeating the current setting is harmless. A connection opened while logging was off has no earlier `connection_opened` record, but its subsequent frames still include its socket ID and remote address.

### Log format and troubleshooting

Each line is a JSON object with a UTC `timestamp`, an increasing per-run `sequence`, and an `event`. The `server_started` record includes the listening port and GAMA version; connection-open/close records identify the socket and remote address. Frame records also include:

| Field | Meaning |
|---|---|
| `socket_id`, `remote_address` | Connection identity, allowing concurrent clients to be distinguished. |
| `direction` | `received` (client to server) or `sent` (server to client). |
| `opcode`, `fin` | WebSocket frame type and whether it is the final fragment. |
| `encoding`, `payload` | Complete, untruncated payload: `utf-8` text for complete text frames, or lossless `base64` for binary, control, and fragmented frames. |

For example, a frame may be recorded as:

```json
{"timestamp":"2026-10-24T04:42:00Z","sequence":3,"event":"frame","socket_id":"12345","remote_address":"/127.0.0.1:54321","direction":"received","opcode":"TEXT","fin":true,"encoding":"utf-8","payload":"{\"type\":\"play\",\"exp_id\":\"exp\"}"}
```

Text is JSON-escaped, so even payloads containing newlines occupy one log line. For a fragmented message, concatenate the decoded payloads from its initial `TEXT`/`BINARY` frame and subsequent `CONTINUOUS` frames on the same socket until `fin` is `true`; control frames can occur between fragments. HTTP upgrade handshakes and TLS traffic are not logged. `sent` means submitted for WebSocket serialization, not proof that the client received it.

Records are serialized across threads and flushed after every write, allowing inspection while the server is running. This adds disk I/O to message handling. Shutdown while logging is enabled closes the file with a `server_stopped` record; that record is absent if logging was disabled or the process crashed. If the configuration is invalid, the directory cannot be created, or a write fails (for example, disk full), an explicit **`GAMA server exchange logging unavailable`** error is printed to stderr. The server continues handling clients, but recording stops. After fixing the cause, a `server_log` enable command can retry.

**Treat these files as sensitive:** they contain complete messages without redaction, including model paths, parameter values, executed expressions, outputs, and any credentials or personal data present in payloads. TLS does not protect the local log. Review/redact a copy before attaching it to a bug report. Files are not rotated or automatically deleted: monitor disk usage, archive/remove old runs yourself, or disable logging when it is not appropriate. When reporting a server problem, include the relevant run's log together with the model and reproduction steps.

## Calling agent actions with `ask`

The `agent` field is an **agent reference**, not a GAML expression. For example:

```json
{"type":"ask","exp_id":"exp","agent":"simulation[0].boids[12]","action":"say_hello","args":{"the_value":12}}
```

Use the reference returned when an agent is serialized by the server. References start at the simulation
and can include nested populations. `one_of(boids)` is not a reference; evaluate it with an `expression`
request first to obtain an agent reference, then use that reference in `ask`. Omit `agent` to call an
action on the experiment agent.

`action` must be a non-empty string. `args` accepts a JSON object or, for compatibility with existing
clients, a string containing a JSON object (for example, `"args":"{\"the_value\":12}"`).
Omitting `args` supplies no arguments. Malformed references or arguments return `MalformedRequest`;
missing/dead agents and unknown actions return `UnableToExecuteRequest`. Resolving an `ask` target
does not create missing agents.

## The `load` request

```json
{
  "type": "load",
  "model": "/absolute/path/to/server_load.gaml",
  "experiment": "exp",
  "parameters": { "var1": 42 },
  "until": "completed >= 10",
  "console": true,
  "status": false,
  "dialog": false,
  "runtime": true
}
```

| Field | Default | Meaning |
|---|---|---|
| `model` | Required | Path to a `.gaml` file on the machine running GAMA, not on the client. Absolute paths are recommended. |
| `experiment` | Required | Name of an experiment declared in the model. |
| `parameters` | No overrides | Either an object mapping parameter names to values (for example, `{ "var1": 42 }`) or the legacy array of `{ "name": "...", "value": ... }` objects. Names can be parameter titles (such as `"param"`) or variable names (such as `"var1"`). In the array form, `type` is optional; `"int"` and `"float"` also accept numeric strings. |
| `until` | No client override | Boolean GAML expression evaluated in the simulation scope. If omitted, the model's experiment stop condition is retained. |
| `console` | `true` | Forward console output (`SimulationOutput` and `SimulationDebug`). The global **Send console outputs to clients** preference must also be enabled. |
| `status` | `false` | Forward status-bar messages (`SimulationStatusInform`, `SimulationStatusError`). |
| `dialog` | `false` | Forward information/error dialog messages (`SimulationDialog`, `SimulationErrorDialog`). This does not remotely answer interactive questions. |
| `runtime` | `true` | Forward asynchronous runtime errors (`SimulationError`). Command failures are still returned when this is `false`. |

Parameter values are applied **before the simulation's `global init` executes**, so initialization and the parameter view use the supplied values immediately. Parameters omitted from the request keep their model defaults.

Model paths can contain spaces in both file and directory names, including on macOS. Send the plain filesystem path
(for example, `"/Users/alice/My models/Model with spaces.gaml"`), not a URL-encoded or shell-escaped path.
In the HTML connector's path field, omit the JSON quotes and click the checkmark to apply the model and experiment.

Forwarding settings are associated with the experiment and its requesting connection, including during initialization and reload. In GUI mode they only control the extra WebSocket messages: they do **not** hide the local console, status bar, dialogs or errors. Local modal dialogs may still require a desktop user's response.

The GUI also sends lifecycle notifications of type `SimulationStatus` (for example `RUNNING` or `PAUSED`). These are control notifications, independent of the `status` option.

### Runtime status subscription and status requests

The `status` load option sets the initial forwarding preference for status-bar messages. The owning client can change it while the experiment is loaded:

```json
{"type":"simulation_status_subscribe","exp_id":"exp"}
{"type":"simulation_status_unsubscribe","exp_id":"exp"}
```

These commands enable or disable forwarding of `SimulationStatusInform`, `SimulationStatusError` and `SimulationStatusNeutral` messages without reloading the experiment. While a simulation is running, commands are synchronized with execution and processed at an experiment step boundary. They do not affect local GUI status output or the GUI's lifecycle `SimulationStatus` notifications. A successful command returns `CommandExecutedSuccessfully` with `content` set to `{"status":true}` or `{"status":false}`. As with other experiment commands, headless clients must provide the `exp_id` returned by `load`; the GUI server has only one current experiment and can resolve it without an `exp_id`. Only the connection that loaded the experiment can change its status subscription or request its current state.

To retrieve the current lifecycle state on demand, send:

```json
{"type":"simulation_status_request","exp_id":"exp"}
```

The server replies with a `SimulationStatus` message, for example `{"type":"SimulationStatus","content":"PAUSED","exp_id":"exp"}`. This snapshot is independent of the status-bar subscription, so a client can unsubscribe from status-bar messages and still request the current state. A headless request needs `exp_id`; it is optional in GUI mode.

### Responses and completion

A successful GUI load returns `CommandExecutedSuccessfully` with the experiment name in `content`. Use that returned string as `exp_id` in subsequent requests. Headless mode returns its generated experiment ID instead. Missing required fields or malformed parameter objects return `MalformedRequest`; missing files, unknown experiments and invalid stop conditions return `UnableToExecuteRequest`.

Output from `global init` can arrive **before** the load response. Always install a message handler before sending the request, and distinguish command responses (which include `command`) from asynchronous simulation messages.

Send `{"type":"play","exp_id":"exp"}` to run the experiment. GUI experiments retain their usual autorun preference; declare `autorun: false` if the client should start execution explicitly.

The `stepBack` command (or its `back` alias) requires an experiment declared with `record: true`.
In both GUI and headless modes, non-record experiments return `UnableToExecuteRequest` with an
explanation, whether `sync` is omitted, `false`, or `true`; no backward step is submitted to the controller.

When the stop condition becomes true, the GUI experiment pauses and emits one `SimulationEnded` event with empty `content` and its `exp_id`. No further steps are executed until the experiment is reloaded. The displays remain open for inspection.

Send a `reload` request to initialize another run. Forwarding settings are retained. `reload` can supply new `parameters` in either supported format and `until`; parameters omitted from reload retain their current values. An omitted or empty reload `until` clears the previous stop condition, so resend it to retain a limit.

`stop` closes the experiment, unlike `pause` or reaching an `until` condition. Wait for its
`CommandExecutedSuccessfully` response before sending another lifecycle command, discard the old `exp_id`,
and use `load` (not `reload`) to open an experiment again. The GUI sends `SimulationStatus` with `content`
`NONE` when closing; a finishing cycle cannot change a disposing experiment back to `PAUSED` or `RUNNING`.
Lifecycle notifications and command replies are separate messages and can arrive in either order.
Successful `reload` replies have empty `content`: keep the existing `exp_id` rather than replacing it.

GUI mode remains interactive, single-experiment mode, not a replacement for headless multi-experiment execution. In particular, GUI `play` acknowledges starting execution; it does not implement headless `play`'s deferred `sync: true` response. Wait for `SimulationEnded` to detect completion.

### Compatibility notes: Stop lifecycle correction (#988)

This correction does not change GAML syntax, WebSocket command names, JSON fields, or response formats.
Existing models do not require changes, and headless command handling is unchanged. The following
observable behavior changes apply:

| Surface | Change |
|---|---|
| Experiment state queries | `GAMA.getExperimentState()` returns `NONE` for a disposing controller, even if its paused flag is still set. |
| GUI lifecycle notifications | A disposing experiment cannot emit a late `PAUSED`, `RUNNING`, or `NOTREADY` state after closure. Notifications are serialized to preserve delivery order. |
| GUI notification deduplication | A different experiment or destination connection can receive a notification even when the state name is unchanged. Clients must not assume a fixed notification count. |
| Bundled browser connector | Load, Reload and Stop requests wait for their replies before another Load, Reload or Stop can be sent, including with Headless mode enabled. This is a connector-side restriction, not a new server protocol requirement. |

Clients should process lifecycle notifications as state updates, not count them or depend on transient
shutdown states. Treat a successful Stop reply as confirmation that the experiment is closed; use Pause
to suspend an experiment that should later resume. Keep command-response handling separate from
asynchronous status handling. Clients following these rules should remain compatible; clients relying
on the previous shutdown race or notification suppression may need adjustments.

### Headless batch experiments

For a `type: batch` experiment, `play` runs the complete exploration, including all parameter sets and repetitions.
The experiment's `until` condition (or its override supplied by `load`) stops each individual simulation, not the
whole batch. The server emits one `SimulationEnded` event when the exploration finishes, even without a client-supplied
`until`. A batch `play` with `sync: true` defers its response until that event. Further `play` and `step` requests are
refused once the batch is complete; load a new experiment to run another batch.

## Complete example

Save this model as `server_load.gaml` on the GAMA machine:

```gaml
model server_load

global {
    int var1 <- -100;
    int completed <- 0;

    init {
        write "Initial value: " + var1;
    }

    reflex tick {
        completed <- completed + 1;
    }
}

experiment exp type: gui autorun: false {
    parameter "param" var: var1;
}
```

Save the following client as `client.mjs`. It uses the built-in WebSocket client in Node.js 22 or later, so no packages are needed:

```javascript
const model = process.argv[2];
if (!model) throw new Error("Usage: node client.mjs /absolute/path/to/server_load.gaml");

const socket = new WebSocket("ws://localhost:1000");
socket.addEventListener("error", event => {
  console.error("WebSocket error", event);
  process.exitCode = 1;
});
socket.addEventListener("message", event => {
  const message = JSON.parse(event.data);
  console.log(message);

  if (message.command?.type === "load") {
    if (message.type !== "CommandExecutedSuccessfully") {
      process.exitCode = 1;
      socket.close();
      return;
    }
    socket.send(JSON.stringify({ type: "play", exp_id: message.content }));
  } else if (message.type === "SimulationEnded") {
    socket.close();
  } else if (message.command && message.type !== "CommandExecutedSuccessfully") {
    process.exitCode = 1;
    socket.close();
  }
});
socket.addEventListener("open", () => {
  socket.send(JSON.stringify({
    type: "load",
    model,
    experiment: "exp",
    parameters: { var1: 42 },
    until: "completed >= 10",
    console: true,
    status: false,
    dialog: false,
    runtime: true
  }));
});
```

Run it while the desktop GAMA server is enabled:

```sh
node client.mjs /absolute/path/to/server_load.gaml
```

The local GAMA console and the client's `SimulationOutput` message should show `Initial value: 42`, not `-100`. The parameter view should show `42`. After ten executions of `tick`, the client receives `SimulationEnded` and the GUI remains paused with `completed = 10`.
