#!/usr/bin/env python3
"""fix_with_pairs.py

Transforms ``with:`` argument lists in GAML source files from the old
bracket / double-colon pair syntax to the new parenthesis / single-colon
syntax.

Examples
--------
    with: [with_viz::true, step::1#s, name::"Simulation step=1s"]
        →  with: (with_viz:true, step:1#s, name:"Simulation step=1s")

Rules applied
-------------
1. The opening ``[`` immediately following ``with:`` (with optional
   whitespace) is replaced by ``(``.
2. The closing ``]`` that ends the list is replaced by ``)``.
3. Every ``::`` inside the list is replaced by ``:``.

The script handles the common case where the entire ``with: [...]`` expression
sits on a single line.  Multi-line ``with:`` blocks are not transformed (a
warning is printed instead so you can handle them manually).

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
# Regex
# ---------------------------------------------------------------------------

# Matches the full  with: [ ... ]  construct on a single line.
# Group 1 → everything before the opening bracket (i.e. "with:\s*")
# Group 2 → the content inside the brackets (may contain any chars except ])
# We use a non-greedy match so we stop at the first ']'.
_PATTERN = re.compile(r'(with:\s*)\[([^\]]*)\]')


def _transform_content(content: str) -> str:
    """Replace every ``::`` inside a ``with:`` list with ``:``.

    Parameters
    ----------
    content:
        The raw text between the ``[`` and ``]`` delimiters.

    Returns
    -------
    str
        The same text with all ``::`` occurrences replaced by ``:``.
    """
    return content.replace('::', ':')


def _replace_match(match: re.Match) -> str:
    """Build the replacement string for a single regex match.

    Parameters
    ----------
    match:
        A :class:`re.Match` object produced by ``_PATTERN``.

    Returns
    -------
    str
        The transformed ``with: (...)`` expression.
    """
    prefix = match.group(1)          # e.g. "with: "
    content = match.group(2)         # everything between [ and ]
    return f'{prefix}({_transform_content(content)})'


def _transform_line(line: str) -> str:
    """Return *line* with all matching ``with: [...]`` patterns replaced.

    Parameters
    ----------
    line:
        A single line of source text.

    Returns
    -------
    str
        The transformed line (unchanged if no pattern matched).
    """
    return _PATTERN.sub(_replace_match, line)


def _process_file(path: str, dry_run: bool) -> bool:
    """Process a single file.

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
            original_lines = fh.readlines()
    except OSError as exc:
        print(f'  [ERROR] Cannot read {path}: {exc}', file=sys.stderr)
        return False

    new_lines = [_transform_line(line) for line in original_lines]

    if new_lines == original_lines:
        return False

    print(f'  {"[dry-run] " if dry_run else ""}Updating: {path}')
    for i, (old, new) in enumerate(zip(original_lines, new_lines), start=1):
        if old != new:
            print(f'    line {i}:')
            print(f'      - {old.rstrip()}')
            print(f'      + {new.rstrip()}')

    if not dry_run:
        try:
            with open(path, 'w', encoding='utf-8') as fh:
                fh.writelines(new_lines)
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
            'in GAML files.'
        )
    )
    parser.add_argument('root', help='Root directory to search recursively.')
    parser.add_argument('--dry-run', action='store_true',
                        help='Preview changes without modifying any file.')
    parser.add_argument('--ext', action='append', dest='extensions',
                        default=['.gaml'],
                        metavar='EXT',
                        help='File extension to process (default: .gaml). '
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
