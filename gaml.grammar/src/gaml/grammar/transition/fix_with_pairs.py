#!/usr/bin/env python3
"""fix_with_pairs.py

Transforms ``with:`` argument lists in GAML source files from the old
bracket / double-colon pair syntax to the new parenthesis / single-colon
syntax.

Examples
--------
    with: [with_viz::true, step::1#s, name::"Simulation step=1s"]
        →  with: (with_viz:true, step:1#s, name:"Simulation step=1s")

    with: [agents::ag, values::[1,2,3]]
        →  with: (agents:ag, values:[1,2,3])

    # Multi-line form is also handled:
    create iris from: csv_file("f.csv", true) with:
        [sepal_length::float(get("sl")),
         sepal_width::float(get("sw"))
        ];
        →
    create iris from: csv_file("f.csv", true) with:
        (sepal_length:float(get("sl")),
         sepal_width:float(get("sw"))
        );

Rules applied
-------------
1. The opening ``[`` immediately following ``with:`` (with optional
   whitespace, including newlines) is replaced by ``(``.
2. The matching closing ``]`` (respecting nested brackets and quoted
   strings, across multiple lines) is replaced by ``)``.
3. Every ``::`` that sits directly inside the outermost list (i.e. not
   inside a nested ``[...]`` or a quoted string) is replaced by ``:``.

Nested ``[...]`` expressions and their contents are left completely
untouched.

Usage
-----
    # Dry-run (preview changes, no files modified)
    python3 fix_with_pairs.py /path/to/workspace --dry-run

    # Apply changes in-place
    python3 fix_with_pairs.py /path/to/workspace

    # Restrict to a specific file extension (default: .gaml)
    python3 fix_with_pairs.py /path/to/workspace --ext .gaml
"""

import argparse
import os
import re
import sys

# ---------------------------------------------------------------------------
# Regex — only used to locate the start of a  with: [  construct.
# \s already matches newlines, so multi-line "with:\n   [" is handled.
# ---------------------------------------------------------------------------

# Finds "with:" followed by optional whitespace (including newlines) and "[".
_WITH_PREFIX = re.compile(r'with:\s*(?=\[)')


def _find_matching_bracket(text: str, start: int) -> int:
    """Return the index of the ``]`` that closes the ``[`` at *start*.

    The search respects:
    - Nested ``[...]`` brackets (depth tracking).
    - Double-quoted strings (characters inside quotes are ignored).
    - Newlines (the search continues across line boundaries).

    Parameters
    ----------
    text:
        The full source text (may span multiple lines).
    start:
        Index of the opening ``[`` character.

    Returns
    -------
    int
        Index of the matching ``]``, or ``-1`` if not found.
    """
    depth = 0
    in_string = False
    i = start
    while i < len(text):
        ch = text[i]
        if in_string:
            if ch == '\\':
                i += 2          # skip escaped character
                continue
            if ch == '"':
                in_string = False
        else:
            if ch == '"':
                in_string = True
            elif ch == '[':
                depth += 1
            elif ch == ']':
                depth -= 1
                if depth == 0:
                    return i
        i += 1
    return -1


def _transform_content(content: str) -> str:
    """Replace ``::`` that sit at the top level of *content* with ``:``.

    Characters inside nested ``[...]`` brackets or double-quoted strings
    are left completely untouched.

    Parameters
    ----------
    content:
        The raw text between the outermost ``[`` and ``]`` delimiters
        (i.e. not including those delimiters themselves).

    Returns
    -------
    str
        The transformed content.
    """
    result = []
    depth = 0
    in_string = False
    i = 0
    while i < len(content):
        ch = content[i]
        if in_string:
            if ch == '\\':
                result.append(ch)
                result.append(content[i + 1])
                i += 2
                continue
            if ch == '"':
                in_string = False
            result.append(ch)
        else:
            if ch == '"':
                in_string = True
                result.append(ch)
            elif ch == '[':
                depth += 1
                result.append(ch)
            elif ch == ']':
                depth -= 1
                result.append(ch)
            elif ch == ':' and depth == 0 and i + 1 < len(content) and content[i + 1] == ':':
                # Top-level "::" → replace with single ":"
                result.append(':')
                i += 2
                continue
            else:
                result.append(ch)
        i += 1
    return ''.join(result)


def _transform_text(text: str) -> str:
    """Return *text* with all ``with: [...]`` patterns replaced.

    Uses a bracket-aware parser so that nested ``[...]`` inside the list
    are preserved intact, and multi-line ``with: [...]`` blocks are handled
    correctly.

    Parameters
    ----------
    text:
        The full source text of a file (may span multiple lines).

    Returns
    -------
    str
        The transformed text (unchanged if no pattern matched).
    """
    result = []
    pos = 0
    for m in _WITH_PREFIX.finditer(text):
        # Skip any match that falls inside an already-consumed region
        # (e.g. a "with:" that appears as a value inside a previous [...]).
        if m.start() < pos:
            continue
        bracket_start = m.end()          # index of the '['
        bracket_end = _find_matching_bracket(text, bracket_start)
        if bracket_end == -1:
            # No matching ']' found — leave untouched
            continue
        # Append everything up to and including "with: "
        result.append(text[pos:bracket_start])
        # Opening '(' instead of '['
        result.append('(')
        # Transformed content (nested brackets preserved, top-level :: → :)
        content = text[bracket_start + 1:bracket_end]
        result.append(_transform_content(content))
        # Closing ')' instead of ']'
        result.append(')')
        pos = bracket_end + 1

    result.append(text[pos:])
    return ''.join(result)


def _process_file(path: str, dry_run: bool) -> bool:
    """Process a single file.

    Reads the entire file as one string so that multi-line ``with: [...]``
    constructs are handled correctly.

    Parameters
    ----------
    path:
        Absolute path to the file to process.
    dry_run:
        When ``True`` the file is not written; changes are only printed.

    Returns
    -------
    bool
        ``True`` if at least one substitution was made (or would be made).
    """
    try:
        with open(path, 'r', encoding='utf-8', errors='replace') as fh:
            original = fh.read()
    except OSError as exc:
        print(f'  [ERROR] Cannot read {path}: {exc}', file=sys.stderr)
        return False

    updated = _transform_text(original)

    if updated == original:
        return False

    print(f'  {"[dry-run] " if dry_run else ""}Updating: {path}')
    # Show a compact diff: print changed lines with their line numbers
    orig_lines = original.splitlines(keepends=True)
    new_lines = updated.splitlines(keepends=True)
    for i, (old, new) in enumerate(zip(orig_lines, new_lines), start=1):
        if old != new:
            print(f'    line {i}:')
            print(f'      - {old.rstrip()}')
            print(f'      + {new.rstrip()}')
    # Handle files where the number of lines changed (multiline replacements)
    if len(new_lines) != len(orig_lines):
        print(f'    (line count changed: {len(orig_lines)} → {len(new_lines)})')

    if not dry_run:
        try:
            with open(path, 'w', encoding='utf-8') as fh:
                fh.write(updated)
        except OSError as exc:
            print(f'  [ERROR] Cannot write {path}: {exc}', file=sys.stderr)
            return False

    return True


def _iter_files(root: str, extensions: list):
    """Yield all files under *root* whose suffix is in *extensions*.

    Hidden directories (names starting with ``.``) are skipped.

    Parameters
    ----------
    root:
        The directory to walk recursively.
    extensions:
        List of file extensions to include (e.g. ``['.gaml']``).
    """
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [d for d in dirnames if not d.startswith('.')]
        for fname in filenames:
            if any(fname.endswith(ext) for ext in extensions):
                yield os.path.join(dirpath, fname)


def main(argv=None):
    """Entry point for the script.

    Parameters
    ----------
    argv:
        Argument list (defaults to ``sys.argv[1:]``).

    Returns
    -------
    int
        Exit code (0 = success).
    """
    parser = argparse.ArgumentParser(
        description=(
            'Replace "with: [key::val, ...]" with "with: (key:val, ...)" '
            'in GAML files, leaving nested [...] untouched.'
        )
    )
    parser.add_argument('root', help='Root directory to search recursively.')
    parser.add_argument('--dry-run', action='store_true',
                        help='Preview changes without modifying any file.')
    parser.add_argument('--ext', action='append', dest='extensions',
                        default=['.gaml', '.experiment'],
                        metavar='EXT',
                        help='File extension to process (default: .gaml, .experiment). '
                             'Repeat for multiple extensions.')
    args = parser.parse_args(argv)

    changed = 0
    total = 0
    for filepath in _iter_files(args.root, args.extensions):
        total += 1
        if _process_file(filepath, args.dry_run):
            changed += 1

    action = 'Would update' if args.dry_run else 'Updated'
    print(f'\n{action} {changed} / {total} file(s).')
    return 0


if __name__ == '__main__':
    sys.exit(main())
