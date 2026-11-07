#!/usr/bin/env python3
"""Capture GAMA displays and compare PNG trees."""

from __future__ import annotations

import argparse
import asyncio
from collections.abc import Awaitable, Callable, Iterable, Iterator
from concurrent.futures import ThreadPoolExecutor
from hashlib import sha256
from html import escape
from io import BytesIO
import json
import math
import os
from pathlib import Path
import re
import shutil
import struct
import sys
from typing import Any, Literal, NotRequired, TypedDict, cast
from urllib.parse import quote

from PIL import Image, ImageChops, ImageOps


ExperimentKey = tuple[str, str]
ExperimentSettings = tuple[list[str], float | None]
CaptureJob = tuple[str, str, list[str], float | None]
ImageSize = tuple[int, int]
ServerMessage = dict[str, Any]


class CaptureEntry(TypedDict):
    model: str
    experiment: str
    displays: list[str]
    seed: float | None
    status: Literal["pending", "ok", "error"]
    error: NotRequired[str]
    error_kind: NotRequired[Literal["seed_mismatch"]]
    close_error: NotRequired[str]


def new_output(path: Path) -> Path:
    path.mkdir(parents=True, exist_ok=True)
    if any(path.iterdir()):
        raise ValueError(f"Output directory must be empty: {path}")
    return path


def write_json(path: Path, value: object) -> None:
    # Replace the manifest only after the complete JSON has been written.
    temporary = path.with_suffix(".json.tmp")
    temporary.write_text(json.dumps(value, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    temporary.replace(path)


def png_tree(root: Path) -> dict[str, Path]:
    if not root.is_dir():
        raise ValueError(f"Missing image directory: {root}")
    return {
        path.relative_to(root).as_posix(): path
        for path in root.rglob("*")
        if path.is_file() and path.suffix.lower() == ".png"
    }


def rgba(path: Path) -> Image.Image:
    with Image.open(path, formats=["PNG"]) as image:
        return image.convert("RGBA")


def model_jobs(args: argparse.Namespace, root: Path) -> tuple[list[CaptureJob], list[str]]:
    config = json.loads(args.config.read_text(encoding="utf-8"))
    if not isinstance(config, dict):
        raise ValueError("Capture config must be a JSON object")
    default = config.get("default")
    default_seed = parse_seed(config.get("seed"))
    overrides = config.get("models", {})
    if not isinstance(overrides, dict):
        raise ValueError("'models' must be a JSON object")
    models = {path.relative_to(root).as_posix() for path in root.rglob("*")
              if path.suffix.lower() == ".gaml" and path.is_file()}
    if unknown := overrides.keys() - models:
        raise ValueError(f"Configured GAML files not found: {', '.join(sorted(unknown))}")
    jobs = []
    skipped = []
    for model in sorted(models):
        settings = overrides.get(model, default)
        if settings is None:
            skipped.append(model)
            continue
        entries = settings if isinstance(settings, list) else [settings]
        seen = set()
        if not entries:
            raise ValueError(f"Expected at least one experiment for {model}, or null to skip it")
        for entry in entries:
            if not isinstance(entry, dict):
                raise ValueError(f"Invalid capture settings for {model}")
            experiment, displays = entry.get("experiment"), entry.get("displays")
            if not isinstance(experiment, str) or not experiment.strip():
                raise ValueError(f"Expected a nonempty GAML experiment name for {model}")
            folder = experiment_folder(experiment).casefold()
            if folder in seen:
                raise ValueError(f"Duplicate or conflicting experiment name {experiment!r} for {model}")
            seen.add(folder)
            if (not isinstance(displays, list) or not displays
                    or any(not isinstance(name, str) or not name.strip() for name in displays)
                    or len(set(displays)) != len(displays)):
                raise ValueError(f"Expected unique nonempty display names for {model} [{experiment}]")
            seed = parse_seed(entry.get("seed", default_seed))
            override = getattr(args, "seed", None)
            jobs.append((model, experiment, displays, seed if override is None else override))
    if not jobs:
        raise ValueError("No GAML model selected for capture")
    return jobs, skipped


def experiment_folder(name: str) -> str:
    if (re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*", name)
            and not re.fullmatch(r"CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]", name, re.IGNORECASE)):
        return name
    return "experiment-" + sha256(name.encode("utf-8")).hexdigest()[:16]


def display_folder(name: str) -> str:
    slug = re.sub(r"[^A-Za-z0-9_-]+", "-", name).strip("-")[:40]
    digest = sha256(name.encode("utf-8")).hexdigest()[:16]
    return f"display-{slug}-{digest}"


def checkpoints(args: argparse.Namespace) -> Iterator[int]:
    yield from range(args.every, args.steps, args.every)
    yield args.steps


class Server:
    """A WebSocket connection dedicated to one capture job."""

    def __init__(self, args: argparse.Namespace) -> None:
        from gama_client.async_client import GamaAsyncClient

        self.client = GamaAsyncClient(args.host, args.port, self.receive)
        self.timeout = args.timeout
        self.errors: list[object] = []
        self.request_id = 0
        self.reply: asyncio.Future[ServerMessage] | None = None

    async def receive(self, message: ServerMessage) -> None:
        command = message.get("command")
        if isinstance(command, dict):
            if (command.get("visual_id") == str(self.request_id)
                    and self.reply is not None and not self.reply.done()):
                self.reply.set_result(message)
            return
        kind = message.get("type")
        # GAMA broadcasts stderr to every socket as GamaServerError without an experiment ID.
        if kind == "GamaServerError" and not message.get("exp_id"):
            return
        if kind in {"SimulationError", "SimulationErrorDialog", "RuntimeError", "GamaServerError"}:
            self.errors.append(message.get("content"))

    async def result(
        self,
        send: Callable[..., Awaitable[None]],
        *args: Any,
        additional_data: dict[str, Any] | None = None,
        **kwargs: Any,
    ) -> Any:
        # Each capture sends commands sequentially. The SDK delivers replies to receive().
        self.request_id += 1
        name = send.__name__.removesuffix("_async")
        reply = self.reply = asyncio.get_running_loop().create_future()
        try:
            async with asyncio.timeout(self.timeout):
                fields = {**(additional_data or {}), "visual_id": str(self.request_id)}
                await send(*args, additional_data=fields, **kwargs)
                closed = asyncio.create_task(self.client.socket.wait_closed())
                try:
                    await asyncio.wait((reply, closed), return_when=asyncio.FIRST_COMPLETED)
                    if not reply.done():
                        raise RuntimeError(f"GAMA WebSocket closed while waiting for {name}")
                    response = reply.result()
                finally:
                    closed.cancel()
        except TimeoutError as exc:
            raise RuntimeError(f"GAMA did not respond to {name} within {self.timeout:g} s") from exc
        finally:
            reply.cancel()
            self.reply = None
        if response.get("type") != "CommandExecutedSuccessfully":
            raise server_error(f"{name}: {response.get('type')}", response.get("content"))
        return response.get("content")


def server_error(context: str, content: object) -> RuntimeError:
    if isinstance(content, dict) and isinstance(content.get("message"), str):
        message = content["message"]
        if content.get("source") and content.get("line"):
            message = f"{content['source']}:{content['line']}: {message}"
        error = RuntimeError(f"{context}: {message}")
        error.add_note(str(content))
        return error
    return RuntimeError(f"{context}: {content}")


def inline_png(value: object, size: ImageSize) -> bytes:
    if isinstance(value, str):
        value = json.loads(value)
    if not isinstance(value, dict) or (value.get("width"), value.get("height")) != size:
        raise ValueError("GAMA returned no snapshot or an unexpected image size")
    pixels = value.get("pixels")
    if not isinstance(pixels, dict) or (pixels.get("cols"), pixels.get("rows")) != size:
        raise ValueError("GAMA returned an invalid pixel matrix")
    contents = pixels.get("contents")
    if not isinstance(contents, list) or len(contents) != size[0] * size[1]:
        raise ValueError("GAMA returned an unexpected pixel count")
    # Java BufferedImage stores these pixels as packed RGB or ARGB integers.
    kind = value.get("type")
    if kind == 1:
        mode, layout = "RGB", "XRGB"
    elif kind == 2:
        mode, layout = "RGBA", "ARGB"
    else:
        raise ValueError(f"Unsupported GAMA image type: {kind!r}")
    raw = struct.pack(f">{len(contents)}I", *(pixel & 0xFFFFFFFF for pixel in contents))
    with Image.frombytes(mode, size, raw, "raw", layout) as image, BytesIO() as stream:
        image.save(stream, format="PNG")
        return stream.getvalue()


def save_snapshot(value: object, size: ImageSize, path: Path) -> None:
    payload = inline_png(value, size)
    with Image.open(BytesIO(payload), formats=["PNG"]) as image:
        image.load()
        if image.size != size:
            raise ValueError(f"Unexpected PNG size: {image.size}")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(payload)


class SeedMismatchError(RuntimeError):
    pass


async def capture_model(
    args: argparse.Namespace,
    server: Server,
    source: str,
    experiment: str,
    displays: list[str],
    destination: Path,
    seed: float | None = None,
) -> None:
    exp_id = None
    client = server.client
    try:
        exp_id = await server.result(
            client.load_async, source, experiment, runtime=True,
            additional_data={"seed": seed} if seed is not None else None,
        )
        if not isinstance(exp_id, str) or not exp_id:
            exp_id = None
            raise RuntimeError("GAMA load did not return an experiment ID")

        def check_runtime() -> None:
            if server.errors:
                raise server_error("GAMA simulation error", server.errors[0])

        async def expression(code: str) -> Any:
            return await server.result(client.expression_async, exp_id, code)

        if seed is not None:
            actual_seed = await expression("simulation.seed")
            if float(actual_seed) != seed:
                raise SeedMismatchError(
                    f"Requested seed {seed:g}, but GAMA initialized the simulation with {actual_seed!r}. "
                    "Check that this GAMA build applies the load command's seed before initialization "
                    "and that the model does not override it."
                )
        initial = int(await expression("simulation.cycle"))
        previous = 0
        for step in checkpoints(args):
            await server.result(client.step_async, exp_id, nb_step=step - previous, sync=True)
            actual = int(await expression("simulation.cycle"))
            check_runtime()
            if actual != initial + step:
                raise RuntimeError(f"After step {step}, expected cycle {initial + step}, got {actual}")
            for display in displays:
                label = json.dumps(display, ensure_ascii=False)
                snapshot = f"snapshot(simulation, {label}, {{{args.width}, {args.height}}})"
                folder = destination / display_folder(display)
                try:
                    value = await expression(snapshot)
                    check_runtime()
                    # Keep the WebSocket event loop available for pings while processing snapshots.
                    await asyncio.to_thread(save_snapshot, value, (args.width, args.height),
                                            folder / f"step-{step:09d}.png")
                except (RuntimeError, ValueError, TypeError, struct.error) as exc:
                    raise RuntimeError(
                        f"Cannot capture display {display!r} at step {step}: {exc}. "
                        "Check the exact display name and update 'displays' if it was renamed."
                    ) from exc
            previous = step
    except BaseException as exc:
        if exp_id is not None:
            try:
                await server.result(client.stop_async, exp_id)
            except Exception as cleanup:
                exc.add_note(f"GAMA stop also failed: {cleanup}")
        raise
    else:
        if exp_id is not None:
            await server.result(client.stop_async, exp_id)


def resume_entries(
    path: Path, settings: dict[str, object], plan: dict[ExperimentKey, ExperimentSettings],
    skipped: set[str],
) -> dict[ExperimentKey, CaptureEntry]:
    previous = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(previous, dict) or not isinstance(previous.get("models"), list):
        raise ValueError(f"Invalid capture manifest: {path}")
    for key, value in settings.items():
        # Older manifests did not record the source or server.
        if (key in {"steps", "every", "size"} or key in previous) and previous.get(key) != value:
            raise ValueError(f"Cannot resume with different capture settings ({key})")
    entries: dict[ExperimentKey, CaptureEntry] = {}
    for entry in previous["models"]:
        if not isinstance(entry, dict):
            raise ValueError(f"Invalid experiment entry in {path}")
        key = entry.get("model"), entry.get("experiment")
        if not all(isinstance(part, str) for part in key) or key in entries:
            raise ValueError(f"Invalid or duplicate experiment entry in {path}")
        if key[0] in skipped:
            continue
        previous_displays = entry.get("displays")
        if (key not in plan or entry.get("seed") != plan[key][1]
                or not isinstance(previous_displays, list)
                or not all(display in previous_displays for display in plan[key][0])):
            raise ValueError(f"Cannot resume with different experiment settings for {key[0]} [{key[1]}]")
        entries[key] = cast(CaptureEntry, entry)
    return entries


def print_capture_summary(entries: list[CaptureEntry], tested: Iterable[CaptureEntry]) -> None:
    errors = [entry for entry in entries if entry["status"] == "error"]
    seed_errors = [entry for entry in errors if entry.get("error_kind") == "seed_mismatch"]
    other_errors = [entry for entry in errors if entry.get("error_kind") != "seed_mismatch"]

    for title, failures in (("Capture errors", other_errors), ("Seeds that could not be set", seed_errors)):
        print(f"\n{title}")
        if not failures:
            print("  None")
        for entry in failures:
            detail = entry.get("error") or entry.get("close_error") or ""
            message = detail.partition("\n")[0]
            print(f"  - {entry['model']} [{entry['experiment']}]" + (f" — {message}" if message else ""))

    tested_models = {entry["model"] for entry in tested}
    validated_models = {entry["model"] for entry in entries} - tested_models
    print("\nCapture summary")
    print("| Result                         | Models |")
    print("|--------------------------------|-------:|")
    for label, total in (
        ("Tested in this run", len(tested_models)),
        ("Already validated", len(validated_models)),
        ("With capture errors", len({entry["model"] for entry in other_errors})),
        ("With seed mismatches", len({entry["model"] for entry in seed_errors})),
    ):
        print(f"| {label:<30} | {total:>6} |")


async def capture(args: argparse.Namespace) -> int:
    root, output = args.models.resolve(), args.output.resolve()
    if not root.is_dir():
        raise ValueError(f"Missing model directory: {root}")
    if output.is_relative_to(root) or root.is_relative_to(output):
        raise ValueError("The capture directory must be separate from the model directory")
    jobs, skipped = model_jobs(args, root)
    settings: dict[str, object] = {
        "steps": args.steps, "every": args.every, "size": [args.width, args.height],
        "source": str(root), "server_root": args.server_root, "host": args.host, "port": args.port,
    }
    path = output / "capture.json"
    plan = {(model, experiment): (displays, seed) for model, experiment, displays, seed in jobs}
    if args.resume and output.exists() and any(output.iterdir()):
        if not path.is_file():
            raise ValueError(f"Cannot resume without a capture manifest: {path}")
        previous = resume_entries(path, settings, plan, set(skipped))
    else:
        new_output(output)
        previous = {}
    entries: list[CaptureEntry] = []
    manifest = {"complete": False, **settings, "skipped": skipped, "models": entries}
    pending: list[tuple[CaptureEntry, Path]] = []
    for (model, experiment), (displays, seed) in plan.items():
        destination = output / "images" / model / experiment_folder(experiment)
        old = previous.get((model, experiment))
        entry: CaptureEntry = {
            "model": model, "experiment": experiment, "displays": displays, "seed": seed, "status": "pending",
        }
        # A successful manifest entry is reusable only if every expected image still exists.
        if old is not None and old.get("status") == "ok" and all(
                (destination / display_folder(display) / f"step-{step:09d}.png").is_file()
                for display in displays for step in checkpoints(args)):
            entry["status"] = "ok"
            print(f"{model} [{experiment}]: already captured", flush=True)
        else:
            pending.append((entry, destination))
        entries.append(entry)
    write_json(path, manifest)

    async def run_job(entry: CaptureEntry, destination: Path) -> None:
        model, experiment = entry["model"], entry["experiment"]
        server = Server(args)
        try:
            try:
                await asyncio.wait_for(server.client.connect_async(max_size=32 * 1024 * 1024), args.timeout)
            except TimeoutError as exc:
                raise RuntimeError(f"No GAMA handshake from {args.host}:{args.port}") from exc
            if server.client.socket_id == -1:
                raise RuntimeError("Server did not send a valid GAMA handshake")
            source = (args.server_root.rstrip("/\\") + "/" + model
                      if args.server_root else (root / model).as_posix())
            await capture_model(args, server, source, experiment, entry["displays"], destination, entry["seed"])
        except Exception as exc:
            details = "\n".join([str(exc), *getattr(exc, "__notes__", [])])
            entry.update(status="error", error=f"{type(exc).__name__}: {details}")
            if isinstance(exc, SeedMismatchError):
                entry["error_kind"] = "seed_mismatch"
        finally:
            if getattr(server.client, "socket", None) is not None:
                try:
                    await asyncio.wait_for(server.client.close_connection_async(), args.timeout)
                except Exception as exc:
                    entry["status"] = "error"
                    entry["close_error"] = str(exc)
        if entry["status"] == "pending":
            entry["status"] = "ok"
        # No await between updating the entry and writing the shared manifest.
        write_json(path, manifest)
        print(f"{model} [{experiment}]: {entry['status']}", flush=True)
        if entry["status"] == "error":
            print(entry.get("error") or entry.get("close_error"), file=sys.stderr)

    # Workers share an iterator. Each takes its next job before yielding to the event loop.
    remaining = iter(pending)

    async def worker() -> None:
        for entry, destination in remaining:
            await run_job(entry, destination)

    workers = [asyncio.create_task(worker()) for _ in range(min(args.threads, len(pending)))]
    try:
        await asyncio.gather(*workers)
    finally:
        for task in workers:
            task.cancel()
        await asyncio.gather(*workers, return_exceptions=True)

    complete = all(entry["status"] == "ok" for entry in entries)
    manifest["complete"] = complete
    write_json(path, manifest)
    print_capture_summary(entries, (entry for entry, _ in pending))
    return 0 if complete else 2


def pixel_delta(left: Image.Image, right: Image.Image, tolerance: int) -> Image.Image:
    bands = ImageChops.difference(left, right).split()
    delta = bands[0]
    for band in bands[1:]:
        delta = ImageChops.lighter(delta, band)
    return delta.point([0] * (tolerance + 1) + list(range(tolerance + 1, 256)))


DIFF_LOW_COLOR = "#ffa4dc"
DIFF_HIGH_COLOR = "#6500b8"
DIFF_BASE_ONLY = "#ff9800"
DIFF_CANDIDATE_ONLY = "#00bcd4"
DIFF_LEGEND = ("White and transparent pixels are treated as background. Pink to violet shows the magnitude "
               "of changes where both captures contain foreground or both show background.")


def foreground_mask(image: Image.Image, tolerance: int) -> Image.Image:
    white = Image.new("RGBA", image.size, "white")
    on_white = Image.alpha_composite(white, image)
    return pixel_delta(on_white, white, tolerance).point([0] + [255] * 255)


def save_diff(
    path: Path, left: Image.Image, right: Image.Image, delta: Image.Image | None, tolerance: int
) -> None:
    coverage_a = coverage_b = None
    if delta is None:
        # Align different sizes at the top left and mark pixels present on only one side.
        size = (max(left.width, right.width), max(left.height, right.height))
        panel = Image.new("RGBA", size)
        panel.paste(left, (0, 0))
        candidate = Image.new("RGBA", size)
        candidate.paste(right, (0, 0))
        coverage_a = Image.new("L", size)
        coverage_b = Image.new("L", size)
        coverage_a.paste(255, (0, 0, *left.size))
        coverage_b.paste(255, (0, 0, *right.size))
        delta = ImageChops.lighter(pixel_delta(panel, candidate, tolerance),
                                  ImageChops.difference(coverage_a, coverage_b))
    else:
        panel, candidate = left, right

    foreground_a = foreground_mask(panel, tolerance)
    foreground_b = foreground_mask(candidate, tolerance)
    only_a = ImageChops.subtract(foreground_a, foreground_b)
    only_b = ImageChops.subtract(foreground_b, foreground_a)
    if coverage_a is not None and coverage_b is not None:
        only_a = ImageChops.lighter(only_a, ImageChops.subtract(coverage_a, coverage_b))
        only_b = ImageChops.lighter(only_b, ImageChops.subtract(coverage_b, coverage_a))

    background = panel.convert("L").convert("RGBA")
    background.putalpha(panel.getchannel("A"))
    changed = delta.point([0] + [255] * 255)
    colors = ImageOps.colorize(delta, DIFF_LOW_COLOR, DIFF_HIGH_COLOR)
    background.paste(colors, mask=changed)
    bounds = (0, 0, *background.size)
    background.paste(DIFF_BASE_ONLY, bounds, ImageChops.darker(only_a, changed))
    background.paste(DIFF_CANDIDATE_ONLY, bounds, ImageChops.darker(only_b, changed))
    path.parent.mkdir(parents=True, exist_ok=True)
    background.save(path)


def capture_image_location(key: str) -> tuple[str, int] | None:
    # Captures use model.gaml/experiment/display/step-N.png under images/.
    parts = key.split("/")
    model_parts = parts[:-3]
    step = re.fullmatch(r"step-(\d+)\.png", parts[-1])
    if not model_parts or not model_parts[-1].lower().endswith(".gaml") or step is None:
        return None
    if model_parts[0] == "images" and len(model_parts) > 1:
        model_parts = model_parts[1:]
    return "/".join(model_parts), int(step[1])


def summarize_missing(
    paths: Iterable[str], locations: dict[str, tuple[str, int] | None], other_models: set[str]
) -> tuple[list[dict[str, Any]], set[str], set[str]]:
    missing_by_model: dict[str, list[str]] = {}
    ungrouped = set()
    for path in sorted(paths):
        location = locations[path]
        if location is None:
            ungrouped.add(path)
        else:
            model, _ = location
            missing_by_model.setdefault(model, []).append(path)

    entries = []
    for model, files in sorted(missing_by_model.items()):
        entry: dict[str, Any] = {"model": model}
        if model in other_models:
            entry["files"] = files
        entries.append(entry)
    entries.extend({"file": path} for path in sorted(ungrouped))
    return entries, set(missing_by_model), ungrouped


def write_html_preview(
    path: Path, overview: str, previews: Iterable[tuple[str, int | str, list[str]]],
    capture_names: tuple[str, str],
) -> None:
    baseline_name, candidate_name = map(escape, capture_names)
    titles = (baseline_name, candidate_name, "Diff")
    by_model: dict[str, list[tuple[int | str, list[str]]]] = {}
    for model, step, urls in previews:
        by_model.setdefault(model, []).append((step, urls))

    with path.open("w", encoding="utf-8") as stream:
        def write_table(images: Iterable[tuple[int | str, list[str]]]) -> None:
            columns = "".join(f"<th>{title}</th>" for title in titles)
            stream.write(f"<table><thead><tr><th>Step</th>{columns}</tr></thead><tbody>\n")
            for step, urls in images:
                stream.write(f"<tr><td>{escape(str(step))}</td>")
                for title, url in zip(titles, urls):
                    url = escape(url, quote=True)
                    stream.write(f'<td><a href="{url}"><img src="{url}" alt="{title}" '
                                 'loading="lazy" decoding="async"></a></td>')
                stream.write("</tr>\n")
            stream.write("</tbody></table>\n")

        stream.write("""<!doctype html>
<html lang="en">
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>GAMA visual comparison</title>
<style>
body { font: 1rem system-ui, sans-serif; margin: 1rem; }
section { margin: 1.5rem 0; }
h2 { margin-bottom: .25rem; overflow-wrap: anywhere; }
.model-path { margin: 0 0 .5rem; overflow-wrap: anywhere; }
details { margin-top: .5rem; }
summary { cursor: pointer; }
table { width: 100%; border-collapse: collapse; table-layout: fixed; }
th, td { border: 1px solid #bbb; padding: .35rem; overflow-wrap: anywhere; }
th { background: #eee; color: #111; }
th:first-child { width: 4rem; }
img { display: block; width: 100%; aspect-ratio: 4 / 3; object-fit: contain; }
</style>
<h1>GAMA visual comparison</h1>
""")
        stream.write(f"<p>{escape(overview)} · <a href=\"report.json\">JSON report</a></p>\n")
        stream.write(f'<p>Grayscale background: {baseline_name}. '
                     f'<span style="color: {DIFF_BASE_ONLY}" aria-hidden="true">■</span> '
                     f'Orange: only in {baseline_name}. '
                     f'<span style="color: {DIFF_CANDIDATE_ONLY}" aria-hidden="true">■</span> '
                     f'Cyan: only in {candidate_name}.</p>\n')
        stream.write(f'<p><span aria-hidden="true" style="display: inline-block; width: 3rem; height: .75rem; '
                     f'background: linear-gradient(90deg, {DIFF_LOW_COLOR}, {DIFF_HIGH_COLOR})"></span> '
                     f'{DIFF_LEGEND}</p>\n')
        if not by_model:
            stream.write("<p>No visual differences to preview.</p>\n")
        for model, images in sorted(by_model.items()):
            images.sort(key=lambda image: image[0] if isinstance(image[0], int) else -1)
            name = Path(model).name
            stream.write(f"<section><h2>{escape(name)}</h2>\n")
            if name != model:
                stream.write(f'<p class="model-path">{escape(model)}</p>\n')
            write_table(images[:1])
            if len(images) > 1:
                stream.write(f"<details><summary>Show {len(images) - 1} more comparisons</summary>\n")
                write_table(images[1:])
                stream.write("</details>\n")
            stream.write("</section>\n")
        stream.write("</html>\n")


def compare(args: argparse.Namespace) -> int:
    base, candidate, output = (path.resolve() for path in (args.baseline, args.candidate, args.output))
    capture_names = (base.name, candidate.name) if base.name != candidate.name else (str(base), str(candidate))
    for root in (base, candidate):
        if output.is_relative_to(root) or root.is_relative_to(output):
            raise ValueError("The report directory must be separate from both inputs")
    before, after = png_tree(base), png_tree(candidate)
    new_output(output)
    errors = []
    if not before and not after:
        errors.append("Neither input contains PNGs")
    for label, root in (("baseline", base), ("candidate", candidate)):
        manifest = root / "capture.json"
        if manifest.is_file():
            try:
                if json.loads(manifest.read_text(encoding="utf-8"))["complete"] is not True:
                    errors.append(f"Incomplete {label} capture (see capture.json)")
            except (OSError, ValueError, KeyError, TypeError) as exc:
                errors.append(f"Invalid {label} capture manifest: {exc}")
    def compare_image(key: str) -> dict[str, Any]:
        row: dict[str, Any] = {"image": key}
        try:
            left = rgba(before[key]) if key in before else None
            right = rgba(after[key]) if key in after else None
            if left is None:
                row["status"] = "added"
            elif right is None:
                row["status"] = "removed"
            elif left.size != right.size:
                row["status"] = "size_changed"
                delta = None
            else:
                delta = pixel_delta(left, right, args.channel_tolerance)
                changed = left.width * left.height - delta.histogram()[0]
                changed_fraction = changed / (left.width * left.height)
                row["status"] = "changed" if changed_fraction > args.max_changed_fraction else "equal"
            if row["status"] in {"changed", "size_changed"}:
                diff = Path("diff") / key
                save_diff(output / diff, left, right, delta, args.channel_tolerance)
                row["diff"] = diff.as_posix()
        except (OSError, ValueError, Image.DecompressionBombError) as exc:
            row.update(status="error", error=str(exc))
        if row["status"] != "equal":
            for label, index in (("baseline", before), ("candidate", after)):
                if key in index:
                    relative = Path(label) / key
                    destination = output / relative
                    destination.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copyfile(index[key], destination)
                    row[label] = relative.as_posix()
        return row

    keys = sorted(before.keys() | after.keys())
    if args.threads == 1:
        rows = [compare_image(key) for key in keys]
    else:
        # Each worker writes its own image paths. map keeps the report in sorted order.
        with ThreadPoolExecutor(max_workers=args.threads) as executor:
            rows = list(executor.map(compare_image, keys))

    locations = {key: capture_image_location(key) for key in keys}
    models_a = {locations[key][0] for key in before if locations[key] is not None}
    models_b = {locations[key][0] for key in after if locations[key] is not None}
    only_in_a, missing_models_b, missing_files_b = summarize_missing(
        before.keys() - after.keys(), locations, models_b,
    )
    only_in_b, missing_models_a, missing_files_a = summarize_missing(
        after.keys() - before.keys(), locations, models_a,
    )
    first_divergence: dict[str, int] = {}
    other_differences = []
    for row in rows:
        if row["status"] == "error":
            errors.append(f"{row['image']}: {row['error']}")
        if row["status"] not in {"changed", "size_changed"}:
            continue
        location = locations[row["image"]]
        if location is None:
            other_differences.append(row["image"])
            continue
        model, number = location
        first_divergence[model] = min(first_divergence.get(model, number), number)

    different_models = len(first_divergence.keys() | missing_models_a | missing_models_b)
    different_files = len(missing_files_a | missing_files_b | set(other_differences))
    exit_code = 2 if errors else int(bool(different_models or different_files))
    report = {
        "captures": {"a": capture_names[0], "b": capture_names[1]},
        "compared_files": len(before.keys() & after.keys()),
        "different_models": different_models,
        "only_in_a": only_in_a,
        "only_in_b": only_in_b,
        "divergent_models": [{"model": model, "first_step": step}
                             for model, step in sorted(first_divergence.items())],
    }
    if other_differences:
        report["other_differences"] = other_differences
    if different_files:
        report["different_ungrouped_files"] = different_files
    if errors:
        report["errors"] = errors
    write_json(output / "report.json", report)
    totals = f"{different_models} model(s) with differences"
    if different_files:
        totals += f", {different_files} differing PNG(s) outside the capture layout"
    overview = f"{len(rows)} PNG(s), {totals}, {len(errors)} error(s)."
    lines = ["## GAMA visual comparison", "", overview, ""]
    # GitHub's job summary stays short. The artifact carries every path and all images.
    lines.extend(f"- {error}" for error in errors)
    github_summary = "\n".join(lines + ["", "See the `gama-visual-report` artifact for the full report.", ""])
    baseline_name, candidate_name = (escape(name).replace("|", "&#124;").replace("[", "&#91;")
                                     .replace("]", "&#93;") for name in capture_names)
    lines.extend(["", f"Grayscale background: {baseline_name}. Orange: only in {baseline_name}. "
                  f"Cyan: only in {candidate_name}. {DIFF_LEGEND}", "",
                  f"| Model | Step | {baseline_name} | {candidate_name} | Diff |",
                  "| --- | --- | --- | --- | --- |"])
    previews = []
    for row in rows:
        if row["status"] not in {"changed", "size_changed"}:
            continue
        location = locations[row["image"]]
        model, step = location if location is not None else (row["image"], "")
        name = Path(model).name
        urls = [quote(row[label]) for label in ("baseline", "candidate", "diff")]
        previews.append((model, step, urls))
        markdown_name = name.replace("|", "&#124;").replace("<", "&lt;").replace("\n", " ")
        links = [f"![{title}]({url})" for title, url in zip((baseline_name, candidate_name, "Diff"), urls)]
        lines.append(f"| {markdown_name} | {step} | " + " | ".join(links) + " |")
    (output / "summary.md").write_text("\n".join(lines) + "\n", encoding="utf-8")
    write_html_preview(output / "summary.html", overview, previews, capture_names)
    if summary := os.environ.get("GITHUB_STEP_SUMMARY"):
        with open(summary, "a", encoding="utf-8") as stream:
            stream.write(github_summary)
    print(f"{len(rows)} PNG(s), {totals}. Report: {output / 'summary.html'}")
    return exit_code


def positive_int(value: str) -> int:
    number = int(value)
    if number < 1:
        raise argparse.ArgumentTypeError("must be positive")
    return number


def parse_seed(value: object) -> float | None:
    if value is None:
        return None
    if isinstance(value, bool) or not isinstance(value, (int, float)):
        raise ValueError("Seed must be a finite nonzero number or null")
    try:
        number = float(value)
    except OverflowError as exc:
        raise ValueError("Seed must be a finite nonzero number") from exc
    if not math.isfinite(number) or number == 0:
        raise ValueError("Seed must be a finite nonzero number")
    return number


def seed_argument(value: str) -> float | None:
    try:
        return parse_seed(float(value))
    except ValueError as exc:
        raise argparse.ArgumentTypeError(str(exc)) from exc


def positive_float(value: str) -> float:
    number = float(value)
    if not math.isfinite(number) or number <= 0:
        raise argparse.ArgumentTypeError("must be finite and positive")
    return number


def port_number(value: str) -> int:
    port = int(value)
    if not 1 <= port <= 65535:
        raise argparse.ArgumentTypeError("must be between 1 and 65535")
    return port


def fraction(value: str) -> float:
    number = float(value)
    if not 0 <= number <= 1:
        raise argparse.ArgumentTypeError("must be between 0 and 1")
    return number


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    record = commands.add_parser("capture")
    record.add_argument("models", type=Path)
    record.add_argument("output", type=Path)
    record.add_argument("--config", type=Path, required=True)
    record.add_argument("--resume", action="store_true", help="resume a previous capture in the output directory")
    record.add_argument("--threads", type=positive_int, default=1,
                        help="maximum number of experiments captured concurrently (default: 1)")
    record.add_argument("--seed", type=seed_argument, help="override all configured seeds")
    record.add_argument("--steps", type=positive_int, required=True)
    record.add_argument("--every", type=positive_int, default=1)
    record.add_argument("--width", type=positive_int, default=640)
    record.add_argument("--height", type=positive_int, default=480)
    record.add_argument("--host", default="localhost")
    record.add_argument("--port", type=port_number, default=6868)
    record.add_argument("--timeout", type=positive_float, default=60)
    record.add_argument("--server-root", help="absolute path to the models as seen by GAMA")
    diff = commands.add_parser("compare")
    diff.add_argument("baseline", type=Path)
    diff.add_argument("candidate", type=Path)
    diff.add_argument("output", type=Path)
    diff.add_argument("--threads", type=positive_int, default=1,
                      help="maximum number of images compared concurrently (default: 1)")
    diff.add_argument("--channel-tolerance", type=int, choices=range(256), metavar="0..255", default=0)
    diff.add_argument("--max-changed-fraction", type=fraction, default=0)
    args = parser.parse_args()
    try:
        if args.command == "capture":
            return asyncio.run(capture(args))
        return compare(args)
    except (OSError, ValueError, RuntimeError, ImportError) as exc:
        print(f"Error: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())
