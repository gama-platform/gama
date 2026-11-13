#!/usr/bin/env python3
"""Discover GUI experiments and their declared displays in a GAML tree."""

import argparse
import json
import math
from pathlib import Path
import re
import sys


TOKEN = re.compile(
    r'(?P<comment>//[^\r\n]*|/\*[\s\S]*?\*/)|'
    r'''(?P<name>"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|[A-Za-z_]\w*)|'''
    r'(?P<punct>[^\s])'
)
BLOCKS = {"{": "}", "(": ")", "[": "]"}


def block_ends(tokens):
    stack = []
    ends = {}
    for index, token in enumerate(tokens):
        if token in BLOCKS:
            stack.append((index, BLOCKS[token]))
        elif token in BLOCKS.values():
            if not stack or stack[-1][1] != token:
                raise ValueError(f"unmatched {token}")
            start, _ = stack.pop()
            ends[start] = index
    if stack:
        raise ValueError(f"unclosed {tokens[stack[-1][0]]}")
    return ends


def level(tokens, ends, start, stop):
    """Yield tokens at this level and skip the contents of nested blocks."""
    while start < stop:
        yield start
        start = ends.get(start, start) + 1


def label(tokens, index):
    if tokens[index:index + 2] == ["name", ":"]:
        index += 2
    if index == len(tokens):
        return None
    token = tokens[index]
    if token.startswith(("'", '"')):
        return re.sub(r"""\\([\\'"])""", r"\1", token[1:-1])
    return token if re.fullmatch(r"[A-Za-z_]\w*", token) else None


def is_virtual_display(tokens, ends, start, stop):
    # Only inspect display facets, before its body or terminating semicolon.
    for index in level(tokens, ends, start, stop):
        if tokens[index] in {"{", ";"}:
            break
        if tokens[index:index + 3] == ["virtual", ":", "true"]:
            return True
    return False


def discover(source):
    tokens = [match.group() for match in TOKEN.finditer(source) if match.lastgroup != "comment"]
    ends = block_ends(tokens)
    jobs = []
    for index in level(tokens, ends, 0, len(tokens)):
        if tokens[index] != "experiment":
            continue
        name = label(tokens, index + 1)
        if not name:
            continue
        kind = "gui"
        for body in level(tokens, ends, index + 2, len(tokens)):
            token = tokens[body]
            if tokens[body:body + 2] == ["type", ":"]:
                kind = tokens[body + 2:body + 3]
                kind = kind[0].strip("\"'") if kind else ""
            if token in {"{", ";", "experiment"}:
                break
        else:
            continue
        if kind != "gui" or tokens[body] != "{":
            continue
        displays = []
        for section in level(tokens, ends, body + 1, ends[body]):
            if tokens[section] not in {"output", "permanent"} or tokens[section + 1] != "{":
                continue
            start = section + 1
            for item in level(tokens, ends, start + 1, ends[start]):
                if (tokens[item] == "display" and (display := label(tokens, item + 1))
                        and not is_virtual_display(tokens, ends, item + 2, ends[start])):
                    displays.append(display)
        if displays:
            jobs.append({"experiment": name, "displays": list(dict.fromkeys(displays))})
    return jobs


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("models", type=Path, help="root directory containing the models")
    parser.add_argument("output", nargs="?", type=Path, default=Path("capture.generated.json"))
    parser.add_argument("--seed", type=float, default=42, help="default seed in the generated config (default: 42)")
    parser.add_argument("--force", action="store_true", help="replace an existing JSON config")
    args = parser.parse_args()
    if not math.isfinite(args.seed) or args.seed == 0:
        parser.error("--seed must be finite and nonzero")
    root = args.models.resolve()
    if not root.is_dir():
        parser.error(f"not a directory: {root}")
    if args.output.suffix.lower() != ".json":
        parser.error("the output must have a .json extension")
    if args.output.resolve().suffix.lower() != ".json":
        parser.error("the output must resolve to a .json file")

    models = {}
    try:
        for path in sorted(root.rglob("*")):
            if path.suffix.lower() != ".gaml" or not path.is_file():
                continue
            relative = path.relative_to(root).as_posix()
            try:
                jobs = discover(path.read_text(encoding="utf-8-sig"))
            except (OSError, ValueError) as exc:
                raise ValueError(f"{relative}: {exc}") from exc
            if jobs:
                models[relative] = jobs
        if not models:
            raise ValueError("no GUI experiments with displays found")
        document = json.dumps({"seed": args.seed, "models": models}, indent=2, ensure_ascii=False) + "\n"
        with args.output.open("w" if args.force else "x", encoding="utf-8") as stream:
            stream.write(document)
    except (OSError, ValueError) as exc:
        print(f"Error: {exc}", file=sys.stderr)
        return 2
    print(f"{len(models)} model(s), {sum(map(len, models.values()))} experiment(s): {args.output}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
