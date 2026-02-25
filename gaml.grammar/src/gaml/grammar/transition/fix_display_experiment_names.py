#!/usr/bin/env python3
"""fix_display_experiment_names.py

Transforms GAML `display` and `experiment` statements that use a plain
double-quoted string as their name into the new syntax that separates the
identifier from the human-readable title.

Examples
--------
    display "3 Simulations"
        → display _3_Simulations title: "3 Simulations"
        
    display "My Disp" title: "Custom Title"
        → display My_Disp title: "Custom Title"

    experiment "Hello World!"
        → experiment Hello_World_ title: "Hello World!"

Rules applied to build the identifier from the quoted string
------------------------------------------------------------
1. Every run of non-alphanumeric characters is replaced by a single ``_``.
2. Leading and trailing underscores are stripped.
3. If the resulting identifier starts with a digit, an ``_`` is prepended.

Usage
-----
    # Dry-run (preview changes, no files modified)
    python3 fix_display_experiment_names.py /path/to/workspace --dry-run

    # Apply changes in-place
    python3 fix_display_experiment_names.py /path/to/workspace

    # Restrict to a specific file extension (default: .gaml)
    python3 fix_display_experiment_names.py /path/to/workspace --ext .gaml --ext .experiment
"""

import argparse
import os
import re
import sys


# ---------------------------------------------------------------------------
# Regex
# ---------------------------------------------------------------------------

# Matches:  display "some string"  or  experiment "some string"
# Group 1 → keyword (display | experiment)
# Group 2 → the raw string inside the quotes (may contain any chars except ")
_PATTERN = re.compile(r'\b(display|experiment)\s+"([^"]+)"')


def _make_identifier(raw: str) -> str:
    """Build a valid GAML identifier from *raw* (the unquoted display/experiment name).

    Parameters
    ----------
    raw:
        The original string as it appears inside the double quotes.

    Returns
    -------
    str
        A string suitable for use as an identifier: non-alphanumeric runs
        replaced by ``_``, no leading/trailing underscores, and a leading
        ``_`` prepended when the first character is a digit.
    """
    # Replace every run of non-alphanumeric characters with a single underscore
    ident = re.sub(r'[^A-Za-z0-9]+', '_', raw)
    # Strip leading/trailing underscores
    ident = ident.strip('_')
    # Prepend underscore if identifier starts with a digit
    if ident and ident[0].isdigit():
        ident = '_' + ident
    return ident


def _transform_line(line: str) -> str:
    """Return *line* with all matching patterns replaced.

    Parameters
    ----------
    line:
        A single line of source text.

    Returns
    -------
    str
        The transformed line (unchanged if no pattern matched).
    """
    # Check if the line already contains a title facet
    has_title = re.search(r'\btitle\s*:', line) is not None

    def _replace(match: re.Match) -> str:
        keyword = match.group(1)
        raw = match.group(2)
        ident = _make_identifier(raw)
        
        if has_title:
            return f'{keyword} {ident}'
        else:
            return f'{keyword} {ident} title: "{raw}"'

    return _PATTERN.sub(_replace, line)


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


def _iter_files(root: str, extensions: list[str]):
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
        # Skip hidden directories in-place so os.walk does not descend into them
        dirnames[:] = [d for d in dirnames if not d.startswith('.')]
        for fname in filenames:
            if any(fname.endswith(ext) for ext in extensions):
                yield os.path.join(dirpath, fname)


def main(argv: list[str] | None = None) -> int:
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
        description='Rewrite display/experiment quoted names to identifier + title: syntax.')
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