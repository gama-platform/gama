#!/usr/bin/env python3
"""fix_arrow_braces.py

Replaces expressions of the form ``-> { ... }`` by ``-> ...`` in GAML 
source files, correctly handling nested braces, strings, and comments.

Examples
--------
    action my_action -> { do something; };    
        →  action my_action -> do something;
        
    -> { if (true) { do A; } }; 
        →  -> if (true) { do A; };

Usage
-----
    # Dry-run (preview changes, no files modified)
    python3 fix_arrow_braces.py /path/to/workspace --dry-run

    # Apply changes in-place
    python3 fix_arrow_braces.py /path/to/workspace

    # Restrict to a specific file extension (default: .gaml, .experiment)
    python3 fix_arrow_braces.py /path/to/workspace --ext .gaml
"""

import argparse
import difflib
import os
import sys

# ---------------------------------------------------------------------------
# Parser
# ---------------------------------------------------------------------------

def _remove_arrow_braces(text: str) -> str:
    """Parse the text and remove outermost curly braces after an arrow (->).

    Tracks string literals and comments to avoid counting false braces, and 
    handles nested curly braces robustly.

    Parameters
    ----------
    text:
        The full source code of the file.

    Returns
    -------
    str
        The transformed source text.
    """
    result = []
    i = 0
    n = len(text)
    
    while i < n:
        # 1. Skip string literals (single and double quotes)
        if text[i] in '"\'':
            quote = text[i]
            start = i
            i += 1
            while i < n and text[i] != quote:
                if text[i] == '\\':
                    i += 2  # Skip escaped character
                else:
                    i += 1
            if i < n:
                i += 1
            result.append(text[start:i])
            continue
        
        # 2. Skip single-line comments
        if text[i:i+2] == '//':
            start = i
            while i < n and text[i] != '\n':
                i += 1
            result.append(text[start:i])
            continue
        
        # 3. Skip multi-line comments
        if text[i:i+2] == '/*':
            start = i
            i += 2
            while i < n - 1 and text[i:i+2] != '*/':
                i += 1
            if i < n - 1:
                i += 2
            else:
                i = n
            result.append(text[start:i])
            continue

        # 4. Look for the arrow operator "->"
        if text[i:i+2] == '->':
            result.append('->')
            i += 2
            
            # Consume any whitespace after the arrow
            ws = []
            while i < n and text[i].isspace():
                ws.append(text[i])
                i += 1
            
            # If the next character is an opening brace, we found a target
            if i < n and text[i] == '{':
                result.append(' ') # Standardize to a single space
                
                brace_depth = 1
                j = i + 1
                content_start = j
                
                # Scan to find the matching closing brace
                while j < n and brace_depth > 0:
                    # Must also skip strings and comments while inside the block
                    if text[j] in '"\'':
                        quote = text[j]
                        j += 1
                        while j < n and text[j] != quote:
                            if text[j] == '\\': j += 2
                            else: j += 1
                        if j < n: j += 1
                        continue
                    elif text[j:j+2] == '//':
                        while j < n and text[j] != '\n':
                            j += 1
                        continue
                    elif text[j:j+2] == '/*':
                        j += 2
                        while j < n - 1 and text[j:j+2] != '*/':
                            j += 1
                        if j < n - 1: j += 2
                        else: j = n
                        continue
                    
                    if text[j] == '{':
                        brace_depth += 1
                    elif text[j] == '}':
                        brace_depth -= 1
                        if brace_depth == 0:
                            break
                    j += 1
                
                if brace_depth == 0:
                    inner_content = text[content_start:j]
                    clean_content = inner_content.strip(' \t\n\r')
                    
                    # Prevent double semicolons: -> { do A; }; becomes -> do A;
                    if clean_content.endswith(';'):
                        clean_content = clean_content[:-1].rstrip(' \t\n\r')
                        
                    # Recursively process the inner content in case of nested arrows
                    result.append(_remove_arrow_braces(clean_content))
                    i = j + 1
                else:
                    # Unmatched brace (syntax error in original), revert safely
                    result.append("".join(ws))
                    result.append('{')
                    i += 1
            else:
                # Arrow without a brace following it
                result.append("".join(ws))
        else:
            result.append(text[i])
            i += 1
            
    return "".join(result)

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
            original_text = fh.read()
    except OSError as exc:
        print(f'  [ERROR] Cannot read {path}: {exc}', file=sys.stderr)
        return False

    new_text = _remove_arrow_braces(original_text)

    if new_text == original_text:
        return False

    original_lines = original_text.splitlines(keepends=True)
    new_lines = new_text.splitlines(keepends=True)

    print(f'  {"[dry-run] " if dry_run else ""}Updating: {path}')
    
    # Generate a unified diff to clearly show changes across lines
    diff = list(difflib.unified_diff(
        original_lines, new_lines,
        fromfile='original', tofile='new', n=0
    ))
    
    # Print the diff, skipping the standard file headers
    for line in diff[2:]:
        if line.startswith('@@'):
            print(f'    {line.strip()}')
        else:
            print(f'      {line.rstrip()}')

    if not dry_run:
        try:
            with open(path, 'w', encoding='utf-8') as fh:
                fh.write(new_text)
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
        description='Replace "-> { ... }" with "-> ..." in GAML files.')
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