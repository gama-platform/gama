# GAMA display web streaming — demo client

A self-contained web page that subscribes to a GAMA display and renders the live frame
stream pushed by the `gama.extension.streaming` plugin over the gama-server WebSocket.

## Run

1. Start a gama-server (default port `6868`):
   - **Headless**: launch GAMA headless in server mode.
   - **GUI**: launch the GAMA desktop with server mode enabled
     (`Preferences ▸ Runtime ▸ gama-server`) and open an experiment.
2. Open `index.html` in a browser (just double-click it — no build step).
3. Click **Connect**, fill in the absolute **Model path** and **Experiment name**, click **load**.
4. Set the **Display name** (the name of a `display` in your model), then **stream on**.
5. Click **play**. Frames appear on the canvas as the simulation advances.

## Message sequence

```jsonc
// client → server
{ "type": "load", "model": "/abs/model.gaml", "experiment": "my_experiment" }
// server → client
{ "type": "CommandExecutedSuccessfully", "content": "0", "command": { "type": "load", ... } }  // content = exp_id

{ "type": "stream", "exp_id": "0", "display": "my_display",
  "width": 500, "height": 500, "frame_rate": 1, "enabled": true }
{ "type": "play", "exp_id": "0" }

// server → client, one per streamed cycle
{ "type": "SimulationImage", "exp_id": "0",
  "content": { "display": "my_display", "cycle": 12, "width": 500, "height": 500,
               "mime": "image/png", "data": "<base64 PNG>" } }

// stop streaming a display
{ "type": "stream", "display": "my_display", "enabled": false }
```

`frame_rate` is a **decimation in simulation cycles**: `1` streams every cycle, `5` one cycle
out of five. Resolution is set per subscription via `width`/`height`.

## Notes

- The frame pump reads the live display off a background thread; for very high-rate streaming or
  3D/OpenGL displays, expect occasional tearing — displays are discrete sim-step frames, not video.
- PNG keeps frames pixel-exact; switch the server encoding to JPEG if bandwidth matters more than fidelity.
