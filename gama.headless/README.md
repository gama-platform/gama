# Using GAMA Server

GAMA's WebSocket server can control experiments in the **desktop GUI as well as headless mode**. Both modes accept the same `load` options. In GUI mode, the experiment opens normally with its displays and parameter view, but the client can choose its initial parameter values and receive simulation messages.

For the complete command protocol and headless startup instructions, see [Headless Server](https://gama-platform.org/wiki/next/HeadlessServer).

## Enable the desktop server

In GAMA's preferences, open **Network > Server mode**, enable **Enable GAMA server mode**, and check **Port to which GAMA server is listening** (default: `1000`). Connect to `ws://localhost:1000`, substituting your configured port.

The GUI server has one current experiment, shared with the desktop user; it does not create an independent simulation for every connection. Loading another experiment replaces the current one, subject to GAMA's normal close-confirmation dialog. Connecting another client does not transfer the loaded experiment's output to that client.

Use this server on a trusted network. Clients can load local models and execute GAML; the desktop WebSocket endpoint is not an authenticated public service.

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

Forwarding settings are associated with the experiment and its requesting connection, including during initialization and reload. In GUI mode they only control the extra WebSocket messages: they do **not** hide the local console, status bar, dialogs or errors. Local modal dialogs may still require a desktop user's response.

The GUI also sends lifecycle notifications of type `SimulationStatus` (for example `RUNNING` or `PAUSED`). These are control notifications, independent of the `status` option.

### Responses and completion

A successful GUI load returns `CommandExecutedSuccessfully` with the experiment name in `content`. Use that returned string as `exp_id` in subsequent requests. Headless mode returns its generated experiment ID instead. Missing required fields or malformed parameter objects return `MalformedRequest`; missing files, unknown experiments and invalid stop conditions return `UnableToExecuteRequest`.

Output from `global init` can arrive **before** the load response. Always install a message handler before sending the request, and distinguish command responses (which include `command`) from asynchronous simulation messages.

Send `{"type":"play","exp_id":"exp"}` to run the experiment. GUI experiments retain their usual autorun preference; declare `autorun: false` if the client should start execution explicitly.

When the stop condition becomes true, the GUI experiment pauses and emits one `SimulationEnded` event with empty `content` and its `exp_id`. No further steps are executed until the experiment is reloaded. The displays remain open for inspection.

Send a `reload` request to initialize another run. Forwarding settings are retained. `reload` can supply new `parameters` in either supported format and `until`; parameters omitted from reload retain their current values. An omitted or empty reload `until` clears the previous stop condition, so resend it to retain a limit.

GUI mode remains interactive, single-experiment mode, not a replacement for headless multi-experiment execution. In particular, GUI `play` acknowledges starting execution; it does not implement headless `play`'s deferred `sync: true` response. Wait for `SimulationEnded` to detect completion.

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
