# Loading experiments through GAMA Server

GAMA's WebSocket server can control experiments in the **desktop GUI as well as headless mode**. Both modes accept the same `load` options. In GUI mode, the experiment opens normally with its displays and parameter view, but the client can choose its initial parameter values and receive simulation messages.

For the complete command protocol and headless startup instructions, see [Headless Server](https://gama-platform.org/wiki/next/HeadlessServer).

## Enable the desktop server

In GAMA's preferences, open **Network > Server mode**, enable **Enable GAMA server mode**, and check **Port to which GAMA server is listening** (default: `1000`). Connect to `ws://localhost:1000`, substituting your configured port.

The GUI server has one current experiment, shared with the desktop user; it does not create an independent simulation for every connection. Loading another experiment replaces the current one, subject to GAMA's normal close-confirmation dialog. Connecting another client does not transfer the loaded experiment's output to that client.

Use this server on a trusted network. Clients can load local models and execute GAML; the desktop WebSocket endpoint is not an authenticated public service.

## The `load` request

```json
{
  "type": "load",
  "model": "/absolute/path/to/server_load.gaml",
  "experiment": "exp",
  "parameters": [
    { "name": "var1", "value": 42, "type": "int" }
  ],
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
| `parameters` | No overrides | Array of objects containing `name` and `value`; `type` is optional. Names can be parameter titles (such as `"param"`) or variable names (such as `"var1"`). `"int"` and `"float"` also accept numeric strings. |
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

Send a `reload` request to initialize another run. Forwarding settings are retained. `reload` can supply new `parameters` and `until`; parameters omitted from reload retain their current values. An omitted or empty reload `until` clears the previous stop condition, so resend it to retain a limit.

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
    parameters: [{ name: "var1", value: 42, type: "int" }],
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
