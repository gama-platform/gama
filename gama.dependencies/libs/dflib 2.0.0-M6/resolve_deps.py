#!/usr/bin/env python3
"""
resolve_deps.py — Resolves the full transitive JAR dependency tree for
dflib-excel and dflib-parquet (DFLib 2.0.0-M6) from Maven Central.

Usage:
    python resolve_deps.py            # print the dependency list
    python resolve_deps.py --download # also download all missing JARs

Output directory: the folder that contains this script.
"""

import sys
import os
import urllib.request
import xml.etree.ElementTree as ET
from collections import deque

# ─── Configuration ─────────────────────────────────────────────────────────────

MAVEN_CENTRAL = "https://repo1.maven.org/maven2"
OUT_DIR = os.path.dirname(os.path.abspath(__file__))

# Direct compile-scope dependencies of dflib-excel and dflib-parquet
# (test/provided/optional excluded up-front)
ROOTS = [
    # dflib-excel 2.0.0-M6 compile deps
    ("org.apache.poi",           "poi",              "5.5.1"),
    ("org.apache.poi",           "poi-ooxml",        "5.5.1"),
    ("org.apache.commons",       "commons-compress", "1.27.1"),
    ("org.apache.logging.log4j", "log4j-to-slf4j",   "2.24.3"),
    # POI-OOXML XML parsing (Woodstox / StAX2)
    ("com.fasterxml.woodstox",   "woodstox-core",    "7.1.0"),
    ("org.codehaus.woodstox",    "stax2-api",        "4.2.2"),
    # Hadoop shaded Guava (org.apache.hadoop.thirdparty.com.google.common.*)
    ("org.apache.hadoop.thirdparty", "hadoop-shaded-guava",    "1.3.0"),
    ("org.apache.hadoop.thirdparty", "hadoop-shaded-protobuf", "1.3.0"),
    # dflib-parquet 2.0.0-M6 compile deps
    ("org.slf4j",           "slf4j-api",            "2.0.16"),
    ("org.apache.parquet",  "parquet-common",       "1.17.0"),
    ("org.apache.parquet",  "parquet-column",       "1.17.0"),
    ("org.apache.parquet",  "parquet-hadoop",       "1.17.0"),
    ("org.apache.hadoop",   "hadoop-common",        "3.5.0"),
    ("org.apache.hadoop",   "hadoop-client",        "3.5.0"),
    ("org.apache.hadoop",   "hadoop-mapreduce-client-core", "3.5.0"),
]

# Artifacts known to be already present in the bundle (skip download, still resolve deps)
ALREADY_PRESENT = {
    "org.apache.poi:poi",
    "org.apache.parquet:parquet-common",
    "org.apache.parquet:parquet-column",
    "org.apache.parquet:parquet-hadoop",
    "org.apache.hadoop:hadoop-common",
    "org.apache.hadoop:hadoop-mapreduce-client-core",
    "org.apache.commons:commons-codec",
    "org.apache.commons:commons-csv",
}

# Scopes that should NOT be followed transitively
SKIP_SCOPES = {"test", "provided", "system", "import"}

# Artifacts to exclude entirely (heavyweight Hadoop runtime components
# not needed for in-process Parquet I/O)
EXCLUDE_PREFIXES = [
    "com.sun.jersey",
    "org.mortbay.jetty",
    "tomcat",
    "javax.servlet",
    "org.eclipse.jetty",
    "com.google.inject",
    "io.netty:netty-all",
    "org.apache.hadoop:hadoop-yarn",
    "org.apache.hadoop:hadoop-hdfs",
    "org.apache.hadoop:hadoop-auth",   # keep only what parquet actually loads
]

NS = "http://maven.apache.org/POM/4.0.0"  # default POM namespace

# ─── Maven helpers ─────────────────────────────────────────────────────────────

def pom_url(g, a, v):
    return f"{MAVEN_CENTRAL}/{g.replace('.','/')}/{a}/{v}/{a}-{v}.pom"

def jar_url(g, a, v):
    return f"{MAVEN_CENTRAL}/{g.replace('.','/')}/{a}/{v}/{a}-{v}.jar"

def tag(name):
    """Return both namespaced and plain tag for robust parsing."""
    return [f"{{{NS}}}{name}", name]

def find(elem, *path_parts):
    """Walk a tag path, trying both namespaced and plain forms."""
    cur = elem
    for part in path_parts:
        found = None
        for t in tag(part):
            found = cur.find(t)
            if found is not None:
                break
        if found is None:
            return None
        cur = found
    return cur

def findall(elem, *path_parts):
    """Like find but returns a list for the last segment."""
    parent = find(elem, *path_parts[:-1]) if len(path_parts) > 1 else elem
    if parent is None:
        return []
    results = []
    for t in tag(path_parts[-1]):
        results.extend(parent.findall(t))
    return results

def text(elem, *path_parts, default=""):
    node = find(elem, *path_parts)
    return (node.text or "").strip() if node is not None else default

# ─── POM cache and resolution ──────────────────────────────────────────────────

_pom_cache = {}

def fetch_pom(g, a, v):
    key = f"{g}:{a}:{v}"
    if key in _pom_cache:
        return _pom_cache[key]
    url = pom_url(g, a, v)
    try:
        with urllib.request.urlopen(url, timeout=15) as r:
            content = r.read()
        root = ET.fromstring(content)
        _pom_cache[key] = root
        return root
    except Exception as e:
        print(f"  [WARN] Cannot fetch POM {key}: {e}", file=sys.stderr)
        _pom_cache[key] = None
        return None

def resolve_version(version_str, properties, dep_mgmt):
    """Resolve a version string: substitute ${props} and look up depMgmt."""
    if not version_str:
        return None
    v = version_str.strip()
    # Substitute property placeholders
    if v.startswith("${") and v.endswith("}"):
        prop_name = v[2:-1]
        v = properties.get(prop_name, v)
    return v if not v.startswith("${") else None

def collect_properties(pom):
    """Collect <properties> entries, recursing into parent POMs."""
    props = {}
    # Parent
    parent_g = text(pom, "parent", "groupId")
    parent_a = text(pom, "parent", "artifactId")
    parent_v = text(pom, "parent", "version")
    if parent_g and parent_a and parent_v:
        parent_pom = fetch_pom(parent_g, parent_a, parent_v)
        if parent_pom is not None:
            props.update(collect_properties(parent_pom))
    # Own <properties>
    for prop in findall(pom, "properties"):
        for child in prop:
            local = child.tag.split("}")[-1] if "}" in child.tag else child.tag
            props[local] = (child.text or "").strip()
    # version from <groupId> / <version>
    g = text(pom, "groupId") or text(pom, "parent", "groupId")
    v = text(pom, "version") or text(pom, "parent", "version")
    if g:
        props["project.groupId"] = g
    if v:
        props["project.version"] = v
    return props

def collect_dep_mgmt(pom, properties):
    """Collect <dependencyManagement> version map."""
    mgmt = {}
    for dep in findall(pom, "dependencyManagement", "dependencies", "dependency"):
        g = text(dep, "groupId")
        a = text(dep, "artifactId")
        v = resolve_version(text(dep, "version"), properties, {})
        if g and a and v:
            mgmt[f"{g}:{a}"] = v
    return mgmt

def get_compile_deps(g, a, v):
    """Return list of (groupId, artifactId, version) compile-scope deps for a POM."""
    pom = fetch_pom(g, a, v)
    if pom is None:
        return []

    properties = collect_properties(pom)
    dep_mgmt   = collect_dep_mgmt(pom, properties)

    deps = []
    for dep in findall(pom, "dependencies", "dependency"):
        scope    = text(dep, "scope") or "compile"
        optional = text(dep, "optional").lower() == "true"
        if scope in SKIP_SCOPES or optional:
            continue

        dg = text(dep, "groupId").strip()
        da = text(dep, "artifactId").strip()
        dv = text(dep, "version").strip()

        # Resolve version via properties then depMgmt
        dv = resolve_version(dv, properties, dep_mgmt)
        if not dv:
            dv = dep_mgmt.get(f"{dg}:{da}")
        if not dv:
            continue  # cannot resolve — skip

        deps.append((dg, da, dv))

    return deps

# ─── Main resolution logic ─────────────────────────────────────────────────────

def should_exclude(g, a):
    coord = f"{g}:{a}"
    return any(coord.startswith(p) or g.startswith(p) for p in EXCLUDE_PREFIXES)

def resolve_all(roots):
    """BFS transitive dependency resolution. Returns ordered list of (g,a,v)."""
    visited = {}   # coord -> version
    order   = []
    queue   = deque(roots)

    while queue:
        g, a, v = queue.popleft()
        coord = f"{g}:{a}"

        if coord in visited:
            continue
        if should_exclude(g, a):
            print(f"  [SKIP] {coord}:{v}")
            visited[coord] = v
            continue

        visited[coord] = v
        order.append((g, a, v))

        for dg, da, dv in get_compile_deps(g, a, v):
            dc = f"{dg}:{da}"
            if dc not in visited:
                queue.append((dg, da, dv))

    return order

# ─── Download helper ───────────────────────────────────────────────────────────

def jar_filename(a, v):
    return f"{a}-{v}.jar"

def download_jar(g, a, v):
    fname  = jar_filename(a, v)
    dest   = os.path.join(OUT_DIR, fname)
    if os.path.exists(dest):
        print(f"  [OK]   {fname} (already present)")
        return True
    url = jar_url(g, a, v)
    print(f"  [DL]   {fname}  <-  {url}")
    try:
        urllib.request.urlretrieve(url, dest)
        return True
    except Exception as e:
        print(f"  [ERR]  {fname}: {e}", file=sys.stderr)
        return False

# ─── Entry point ──────────────────────────────────────────────────────────────

def main():
    do_download = "--download" in sys.argv

    print("Resolving transitive dependencies for dflib-excel + dflib-parquet …\n")
    deps = resolve_all(ROOTS)

    print(f"\n{'='*70}")
    print(f"{'ARTIFACT':<55} {'VERSION':<15} {'STATUS'}")
    print(f"{'='*70}")

    missing = []
    for g, a, v in deps:
        fname  = jar_filename(a, v)
        coord  = f"{g}:{a}"
        exists = os.path.exists(os.path.join(OUT_DIR, fname))
        preset = coord in ALREADY_PRESENT
        status = "present" if (exists or preset) else "MISSING"
        if not (exists or preset):
            missing.append((g, a, v))
        print(f"  {g+':'+a:<53} {v:<15} {status}")

    print(f"\nTotal: {len(deps)} JARs  |  Missing: {len(missing)}")

    if missing:
        print("\nMissing JARs:")
        for g, a, v in missing:
            print(f"  {jar_url(g, a, v)}")

    if do_download and missing:
        print("\nDownloading missing JARs …")
        for g, a, v in missing:
            download_jar(g, a, v)
        print("Done.")
    elif missing and not do_download:
        print("\nRe-run with --download to fetch missing JARs automatically.")

if __name__ == "__main__":
    main()
