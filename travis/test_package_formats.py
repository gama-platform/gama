#!/usr/bin/env python3
"""End-to-end test of the headless .gamx / .gamp commands of an installed GAMA, on the samples it ships.

The samples of headless/samples are packaged in place, as they are installed (each one is a GAMA project):
roadTraffic as a .gamx and predatorPrey as a .gamp. For each one, the test:
  1. generates the archive with -package, embedding a test plugin given with -plugin,
  2. unzips it and checks its layout, its manifest, and that the packaged files are intact,
  3. runs -validate-package on it,
  4. runs an experiment with -run-package until cycle 2, and checks that it stopped there,
  5. checks that a broken copy of the archive is rejected by -validate-package.
It also checks that invalid command lines are rejected, and that no command modified the installed samples.

Usage: test_package_formats.py [--memory 4g] [--work DIR] -- <gama-headless command...>
  e.g. test_package_formats.py -- bash ./headless/gama-headless.sh
       test_package_formats.py -- gama-headless

Only the Python standard library is used, so that it runs as is on every CI runner.
"""

import argparse
import io
import os
import shutil
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ElementTree
import zipfile
from pathlib import Path, PurePosixPath

# The shipped samples: (project directory, archive format, experiment run)
SAMPLES = [("roadTraffic", ".gamx", "road_traffic"), ("predatorPrey", ".gamp", "prey_predator")]
UNTIL_CYCLE = 2

# A minimal OSGi bundle, to exercise plugin embedding (-plugin, -include-plugins) and installation for the run
PLUGIN_ID = "org.example.gama.packagetest"
PLUGIN_VERSION = "1.0.0"
PLUGIN_ARTIFACT = f"plugins/{PLUGIN_ID}_{PLUGIN_VERSION}.jar"

failures = []


def check(condition, message):
    """Records a failed check; the test goes on so that one run reports every problem."""
    if condition:
        print(f"  ok   {message}")
    else:
        print(f"  FAIL {message}")
        failures.append(message)
    return condition


class Headless:
    """Runs gama-headless commands, each in the work directory so that their temporary workspace lands there."""

    def __init__(self, command, memory, cwd):
        self.command = command
        self.memory = memory
        self.cwd = cwd

    def __call__(self, *args, expect=0, label=None):
        line = [*self.command, "-m", self.memory, *[str(a) for a in args]]
        print(f"\n$ {' '.join(line)}", flush=True)
        result = subprocess.run(line, cwd=self.cwd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                                text=True, encoding="utf-8", errors="replace", timeout=900)
        print(result.stdout, flush=True)
        name = label or " ".join(str(a) for a in args[:2])
        if expect == 0:
            check(result.returncode == 0, f"{name}: exit code {result.returncode} == 0")
        else:
            check(result.returncode != 0, f"{name}: rejected (exit code {result.returncode})")
        return result.stdout


def samples_directory(command):
    """The headless/samples directory next to the gama-headless script of the command (symlinks resolved)."""
    for word in command:
        found = shutil.which(word) if "/" not in word and os.sep not in word else word
        if found and Path(found).is_file() and Path(found).name.startswith("gama-headless"):
            return Path(os.path.realpath(found)).parent / "samples"
    return None


def snapshot(directory):
    """The files of a directory with their size and modification time, to detect any change."""
    return {p.relative_to(directory).as_posix(): (p.stat().st_size, p.stat().st_mtime_ns)
            for p in sorted(directory.rglob("*"))}


def make_plugin(path):
    """Writes a minimal OSGi bundle (a manifest and one resource)."""
    manifest = (f"Manifest-Version: 1.0\r\nBundle-ManifestVersion: 2\r\nBundle-SymbolicName: {PLUGIN_ID}\r\n"
                f"Bundle-Version: {PLUGIN_VERSION}\r\nBundle-Name: GAMA package test plugin\r\n\r\n")
    with zipfile.ZipFile(path, "w", zipfile.ZIP_DEFLATED) as jar:
        jar.writestr("META-INF/MANIFEST.MF", manifest)
        jar.writestr("readme.txt", "Test plugin of the GAMA CI\n")
    return path


def read_properties(data):
    """Reads a Java .properties file as written by java.util.Properties.store (ISO-8859-1, backslash escapes)."""
    logical, current = [], ""
    for line in data.decode("iso-8859-1").splitlines():
        stripped = line.lstrip()
        if not current and (not stripped or stripped[0] in "#!"):
            continue
        if (len(stripped) - len(stripped.rstrip("\\"))) % 2 == 1:  # an odd number of backslashes continues the line
            current += stripped[:-1]
            continue
        logical.append(current + stripped)
        current = ""
    result = {}
    for line in logical:
        key, i = _unescape(line, 0, "=: \t\f")
        while i < len(line) and line[i] in " \t\f":
            i += 1
        if i < len(line) and line[i] in "=:":
            i += 1
        while i < len(line) and line[i] in " \t\f":
            i += 1
        result[key] = _unescape(line, i, "")[0]
    return result


def _unescape(text, i, stops):
    out = []
    while i < len(text) and text[i] not in stops:
        c = text[i]
        if c == "\\" and i + 1 < len(text):
            n = text[i + 1]
            if n == "u":
                out.append(chr(int(text[i + 2:i + 6], 16)))
                i += 6
                continue
            out.append({"t": "\t", "n": "\n", "r": "\r", "f": "\f"}.get(n, n))
            i += 2
            continue
        out.append(c)
        i += 1
    return "".join(out), i


def description_summary(data):
    """What must survive packaging in a .project: the exporters rewrite the XML (name, links) but keep the rest."""
    root = ElementTree.fromstring(data)
    return (root.tag, root.findtext("name"), [n.text for n in root.iter("nature")],
            [b.findtext("name") for b in root.iter("buildCommand")])


def indexed(manifest, prefix, count, *fields):
    """The records {prefix}.{i}.{field} of a manifest, for i below the value of {count}."""
    return [tuple(manifest.get(f"{prefix}.{i}.{f}") for f in fields) for i in range(int(manifest.get(count, "0")))]


def check_archive(archive, project, name, extension, experiment, model, plugin):
    """Unzips the archive and checks its layout, manifest and payload."""
    label = archive.name
    check(zipfile.is_zipfile(archive), f"{label}: is a ZIP file")
    with zipfile.ZipFile(archive) as zf:
        check(zf.testzip() is None, f"{label}: every entry has a valid CRC")
        names = zf.namelist()
        check(len(names) == len(set(names)), f"{label}: no duplicate entry")
        files = [n for n in names if not n.endswith("/")]
        for entry in names:
            path = PurePosixPath(entry)
            if path.is_absolute() or ".." in path.parts or "\\" in entry or ".metadata" in path.parts:
                check(False, f"{label}: portable entry path {entry}")
        manifest_file = "manifest.properties" if extension == ".gamx" else "gama-project.properties"
        for entry in files:
            allowed = entry == manifest_file or entry.startswith((f"workspace/{name}/", "plugins/")) or (
                extension == ".gamx" and entry.startswith("configuration/"))
            check(allowed, f"{label}: supported entry {entry}")
        if not check(manifest_file in files, f"{label}: has {manifest_file}"):
            return
        manifest = read_properties(zf.read(manifest_file))
        check(manifest.get("format.version") == "1", f"{label}: manifest format.version is 1")
        if extension == ".gamx":
            check(manifest.get("package.id") == "org.gama.ci.sample" and manifest.get("package.version") == "1.2.3",
                  f"{label}: manifest keeps the package id and version")
            check(bool(manifest.get("runtime.version")), f"{label}: manifest declares the required GAMA version")
            experiments = indexed(manifest, "experiment", "experiments.count", "name", "model")
            check((experiment, model) in experiments, f"{label}: manifest lists {experiment}@{model}")
            plugins = dict(indexed(manifest, "bundle", "bundles.count", "id", "artifact"))
            if check("configuration/gama-launcher.properties" in files, f"{label}: has launcher settings"):
                launcher = read_properties(zf.read("configuration/gama-launcher.properties"))
                check(launcher.get("title") == "CI sample", f"{label}: launcher keeps the title")
                check(launcher.get("startupExperiment") == f"{experiment}@{model}",
                      f"{label}: launcher keeps the default experiment")
        else:
            check(manifest.get("archive.kind") == "project", f"{label}: manifest archive.kind is project")
            check(bool(manifest.get("gama.version")), f"{label}: manifest records the GAMA version")
            check(manifest.get("projects.count") == "1" and manifest.get("project.0.name") == name,
                  f"{label}: manifest lists the project {name}")
            check(manifest.get("links.resolved") in ("true", "false"), f"{label}: manifest records the link status")
            plugins = dict(indexed(manifest, "plugin", "plugins.count", "id", "artifact"))
        check(plugins.get(PLUGIN_ID) == PLUGIN_ARTIFACT, f"{label}: manifest declares the embedded test plugin")
        check({a for a in plugins.values() if a} == {f for f in files if f.startswith("plugins/")},
              f"{label}: every embedded plugin is declared and every declared plugin is embedded")
        if check(PLUGIN_ARTIFACT in files, f"{label}: embeds {PLUGIN_ARTIFACT}"):
            check(zf.read(PLUGIN_ARTIFACT) == plugin.read_bytes(), f"{label}: embedded plugin is unchanged")
            with zipfile.ZipFile(io.BytesIO(zf.read(PLUGIN_ARTIFACT))) as jar:
                check(f"Bundle-SymbolicName: {PLUGIN_ID}" in jar.read("META-INF/MANIFEST.MF").decode(),
                      f"{label}: embedded plugin manifest intact")
        # Every file of the sample project (models and data, e.g. shapefile companions) is packaged unchanged
        for path in sorted(p for p in project.rglob("*") if p.is_file() and p.name != ".project"):
            entry = f"workspace/{name}/{path.relative_to(project).as_posix()}"
            if check(entry in files, f"{label}: contains {entry}"):
                check(zf.read(entry) == path.read_bytes(), f"{label}: {entry} is unchanged")
        entry = f"workspace/{name}/.project"
        if check(entry in files, f"{label}: contains {entry}"):
            check(description_summary(zf.read(entry)) == description_summary((project / ".project").read_bytes()),
                  f"{label}: .project is a valid description, equivalent to the shipped one")


def broken_copy(archive, destination, drop):
    """A copy of the archive without the entry {drop}."""
    with zipfile.ZipFile(archive) as source, zipfile.ZipFile(destination, "w") as target:
        for item in source.infolist():
            if item.filename != drop:
                target.writestr(item, source.read(item.filename))
    return destination


def test_sample(headless, samples, name, extension, experiment, work, plugin):
    """Generates, unzips and checks, validates and runs an archive of a shipped sample."""
    print(f"\n== Sample {name} as {extension}")
    project = samples / name
    if not check((project / ".project").is_file(), f"{name}: the sample project is shipped in {samples}"):
        return None
    models = sorted(p.relative_to(project).as_posix() for p in project.rglob("*.gaml"))
    if not check(len(models) == 1, f"{name}: the sample has one model ({models})"):
        return None
    model = f"{name}/{models[0]}"
    archive = work / "out" / f"{name}{extension}"
    if extension == ".gamx":
        headless("-package", project, archive, "-plugin", plugin, "-title", "CI sample", "-default-experiment",
                 experiment, "-id", "org.gama.ci.sample", "-version", "1.2.3", label=f"generate {archive.name}")
    else:
        headless("-package", project, archive, "-plugin", plugin, "-include-plugins", label=f"generate {archive.name}")
    if not check(archive.is_file(), f"{archive.name}: the archive was written"):
        return None
    check_archive(archive, project, name, extension, experiment, model, plugin)

    text = headless("-validate-package", archive, label=f"validate {archive.name}")
    check("VALID:" in text, f"validate {archive.name}: reported valid")

    run = ["-run-package", archive, "-experiment", experiment, "-until-cycle", UNTIL_CYCLE]
    if extension == ".gamp":
        run += ["-model", model]
    text = headless(*run, label=f"run {archive.name}")
    check(f"stopped at cycle {UNTIL_CYCLE} after {UNTIL_CYCLE} step(s)" in text,
          f"run {archive.name}: the experiment stopped at cycle {UNTIL_CYCLE}")

    # A playable model without its embedded plugin, a project archive without its manifest
    drop = PLUGIN_ARTIFACT if extension == ".gamx" else "gama-project.properties"
    broken = broken_copy(archive, work / "out" / f"broken{extension}", drop)
    text = headless("-validate-package", broken, expect=1, label=f"validate {archive.name} without {drop}")
    check("INVALID" in text, f"validate {archive.name} without {drop}: reported invalid")
    return project, archive


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--memory", default="4g", help="memory given to gama-headless (-m)")
    parser.add_argument("--work", help="work directory (default: a new temporary directory, deleted on success)")
    parser.add_argument("command", nargs="+", help="the gama-headless command, after --")
    options = parser.parse_args()

    work = Path(options.work or tempfile.mkdtemp(prefix="gama-package-test-")).resolve()
    (work / "out").mkdir(parents=True, exist_ok=True)
    print(f"Work directory: {work}")
    headless = Headless(options.command, options.memory, work)
    plugin = make_plugin(work / "test-plugin.jar")

    samples = samples_directory(options.command)
    results = {}
    if not check(samples is not None and samples.is_dir(), f"the shipped samples are found ({samples})"):
        samples = None
    if samples:
        samples = samples.resolve()
        before = snapshot(samples)
        for name, extension, experiment in SAMPLES:
            results[extension] = test_sample(headless, samples, name, extension, experiment, work, plugin)

    # Invalid command lines
    if results.get(".gamx"):
        project, gamx = results[".gamx"]
        headless("-run-package", gamx, "-until-cycle", "-3", expect=1, label="negative -until-cycle")
        headless("-package", project, work / "out" / "wrong.zip", expect=1, label="unknown output format")
        headless("-package", project, work / "out" / "wrong.gamp", "-experiment", "road_traffic", expect=1,
                 label="gamx option for gamp")
    if samples:
        check(snapshot(samples) == before, "the commands did not modify the installed samples")

    print()
    if failures:
        print(f"{len(failures)} check(s) failed:")
        for failure in failures:
            print(f"  - {failure}")
        print(f"Work directory kept: {work}")
        return 1
    print("All .gamx/.gamp checks passed")
    if not options.work:
        shutil.rmtree(work, ignore_errors=True)
    return 0


if __name__ == "__main__":
    sys.exit(main())
