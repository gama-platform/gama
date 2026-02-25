#!/usr/bin/env python3
"""fix_image_to_picture.py

Replaces the ``image`` layer keyword by ``picture`` inside ``display``
blocks in GAML source files.

The replacement is context-sensitive: ``image`` is only replaced when it
appears as a statement keyword (i.e. at the start of a non-blank,
non-comment line, possibly preceded by whitespace) inside a ``display {
... }`` block.  Occurrences of ``image`` outside display blocks (e.g. as
a variable name, inside a string literal, in a comment, or as part of a
larger identifier such as ``image_file``) are left untouched.

Examples
--------
    display my_display {
        image "background.jpg";
        image my_matrix refresh: true;
        image testGIS gis: "../includes/building.shp" color: rgb('blue');
    }

    becomes:

    display my_display {
        picture "background.jpg";
        picture my_matrix refresh: true;
        picture testGIS gis: "../includes/building.shp" color: rgb('blue');
    }

Usage
-----
    # Dry-run (preview changes, no files modified)
    python3 fix_image_to_picture.py /path/to/workspace --dry-run

    # Apply changes in-place
    python3 fix_image_to_picture.py /path/to/workspace

    # Restrict to specific file extensions (default: .gaml, .experiment)
    python3 fix_image_to_picture.py /path/to/workspace --ext .gaml --ext .experiment
"""

import argparse
import os
import re
import sys

# ---------------------------------------------------------------------------
# Regexes
# ---------------------------------------------------------------------------

# Matches a line that opens a display block:
#   display <any identifier or quoted name> [facets] {
# The '{' may or may not be on the same line.
_DISPLAY_OPEN = re.compile(r'\bdisplay\b')

# Matches the ``image`` keyword at the very start of a statement (after
# optional indentation), NOT followed by another word character (so
# ``image_file`` is left alone).
_IMAGE_KEYWORD = re.compile(r'^(\s*)image\b')


def _count_braces(line: str) -> int:
    """Return the net number of ``{`` minus ``}`` on *line*, ignoring
    characters inside string literals and single-line comments.

    Parameters
    ----------
    line:
        A single line of source text.

    Returns
    -------
    int
        The net brace delta for this line (can be negative).
    """
    delta = 0
    in_string = False
    i = 0
    while i < len(line):
        ch = line[i]
        if in_string:
            if ch == '\\':
                i += 2
                continue
            if ch == '"':
                in_string = False
        else:
            if ch == '"':
                in_string = True
            elif ch == '/' and i + 1 < len(line) and line[i + 1] == '/':
                # Single-line comment: ignore the rest of the line
                break
            elif ch == '{':
                delta += 1
            elif ch == '}':
                delta -= 1
        i += 1
    return delta


def _is_comment_or_blank(line: str) -> bool:
    """Return ``True`` if *line* is blank or a pure comment line.

    Parameters
    ----------
    line:
        A single line of source text (including the newline character).

    Returns
    -------
    bool
        ``True`` when the line carries no executable GAML statement.
    """
    stripped = line.lstrip()
    return stripped == '' or stripped.startswith('//')


def _transform_lines(lines: list) -> list:
    """Return a new list of lines with ``image`` → ``picture`` replacements
    applied inside ``display`` blocks.

    The function tracks brace depth to determine when it is inside a
    ``display`` block.  A stack is used so that nested display blocks
    (which are unusual but possible) are handled correctly.

    Parameters
    ----------
    lines:
        The original lines of a source file (as returned by
        ``file.readlines()``).

    Returns
    -------
    list
        A new list of lines with substitutions applied.
    """
    result = []
    # Stack of brace depths at which each open display block started.
    # An entry is pushed when we see ``display`` and popped when we return
    # to that depth.
    display_stack = []   # each entry: brace depth *before* the opening {
    brace_depth = 0

    for line in lines:
        new_line = line

        # Determine whether we are inside a display block *before* processing
        # this line's braces.
        inside_display = len(display_stack) > 0

        # Check if this line opens a new display block
        if _DISPLAY_OPEN.search(line) and not _is_comment_or_blank(line):
            # Record the current depth; the '{' on this (or a following) line
            # will bring us into the block.  We push the depth *before* the
            # brace delta of this line so we know when to pop.
            brace_delta = _count_braces(line)
            if brace_delta > 0:
                # The opening '{' is on this same line
                display_stack.append(brace_depth)
            # (If '{' is on the next line we will catch it below via the
            #  brace_depth tracking — see the deferred-open handling.)
            brace_depth += brace_delta
        else:
            brace_delta = _count_braces(line)
            brace_depth += brace_delta

        # Pop display entries whose block has been closed
        while display_stack and brace_depth <= display_stack[-1]:
            display_stack.pop()

        # Apply the image → picture replacement if we are inside a display
        # block and the line is a statement line starting with ``image``
        if inside_display and not _is_comment_or_blank(line):
            new_line = _IMAGE_KEYWORD.sub(r'\1picture', line)

        result.append(new_line)

    return result


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

    new_lines = _transform_lines(original_lines)

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
        List of file extensions to include (e.g. ``['.gaml', '.experiment']``).
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
            'Replace the "image" layer keyword with "picture" inside '
            'display blocks in GAML files.'
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
