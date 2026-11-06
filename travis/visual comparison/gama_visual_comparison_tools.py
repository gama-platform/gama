#!/usr/bin/env python3
"""Capture GAMA displays and compare PNG trees."""

from __future__ import annotations

import argparse
import asyncio
from collections import Counter
from collections.abc import Awaitable, Callable, Iterable, Iterator
from hashlib import sha256
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
        error = RuntimeError(f"{context}: {content['message']}")
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


def save_diff(path: Path, left: Image.Image, right: Image.Image, delta: Image.Image | None) -> None:
    # Different dimensions are shown side by side at native size.
    if delta is None:
        panel = Image.new("RGBA", (left.width + right.width, max(left.height, right.height)), "#808080")
        panel.paste(left, (0, 0))
        panel.paste(right, (left.width, 0))
    else:
        panel = ImageOps.grayscale(left).convert("RGB")
        red = ImageOps.colorize(delta, "#800000", "#ff0000")
        panel.paste(red, mask=delta.point([0] + [255] * 255))
    path.parent.mkdir(parents=True, exist_ok=True)
    panel.save(path)


def compare(args: argparse.Namespace) -> int:
    base, candidate, output = (path.resolve() for path in (args.baseline, args.candidate, args.output))
    for root in (base, candidate):
        if output.is_relative_to(root) or root.is_relative_to(output):
            raise ValueError("The report directory must be separate from both inputs")
    before, after = png_tree(base), png_tree(candidate)
    new_output(output)
    rows = []
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
    for key in sorted(before.keys() | after.keys()):
        row = {"image": key}
        rows.append(row)
        try:
            left = rgba(before[key]) if key in before else None
            right = rgba(after[key]) if key in after else None
            if left is None:
                row["status"] = "added"
            elif right is None:
                row["status"] = "removed"
            elif left.size != right.size:
                row.update(status="size_changed", baseline_size=left.size, candidate_size=right.size)
                delta = None
            else:
                delta = pixel_delta(left, right, args.channel_tolerance)
                changed = left.width * left.height - delta.histogram()[0]
                changed_fraction = changed / (left.width * left.height)
                row.update(status="changed" if changed_fraction > args.max_changed_fraction else "equal",
                           changed_pixels=changed, changed_fraction=changed_fraction)
            if row["status"] in {"changed", "size_changed"}:
                diff = Path("diff") / key
                save_diff(output / diff, left, right, delta)
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

    counts = Counter(row["status"] for row in rows)
    differences = sum(counts[k] for k in ("added", "removed", "changed", "size_changed"))
    exit_code = 2 if errors or counts["error"] else int(bool(differences))
    write_json(output / "report.json", {
        "counts": dict(counts), "errors": errors, "differences": differences,
        "channel_tolerance": args.channel_tolerance, "max_changed_fraction": args.max_changed_fraction,
        "exit_code": exit_code, "images": rows,
    })
    lines = ["## GAMA visual comparison", "",
             f"{len(rows)} PNG(s), {differences} difference(s), {len(errors) + counts['error']} error(s).", ""]
    # GitHub's job summary stays short. The artifact carries every path and all images.
    lines.extend(f"- {error}" for error in errors)
    github_summary = "\n".join(lines + ["", "See the `gama-visual-report` artifact for the full report.", ""])
    lines.extend(["", "| Image | Status | Baseline | Candidate | Diff |",
                  "| --- | --- | --- | --- | --- |"])
    for row in rows:
        if row["status"] == "equal":
            continue
        image = row["image"].replace("|", "&#124;").replace("<", "&lt;").replace("\n", " ")
        links = [f"[PNG]({quote(row[label])})" if label in row else ""
                 for label in ("baseline", "candidate", "diff")]
        lines.append(f"| {image} | {row['status']} | " + " | ".join(links) + " |")
    (output / "summary.md").write_text("\n".join(lines) + "\n", encoding="utf-8")
    if summary := os.environ.get("GITHUB_STEP_SUMMARY"):
        with open(summary, "a", encoding="utf-8") as stream:
            stream.write(github_summary)
    print(f"{len(rows)} PNG(s), {differences} difference(s). Report: {output / 'report.json'}")
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
