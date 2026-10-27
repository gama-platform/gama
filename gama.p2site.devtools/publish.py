"""Assemble a persistent composite p2 site. Uses only the Python standard library."""

import argparse
from pathlib import Path
import re
import shutil
import time
import xml.etree.ElementTree as ET
from zipfile import ZipFile


METADATA = ("content.jar", "content.xml", "content.xml.xz",
            "artifacts.jar", "artifacts.xml", "artifacts.xml.xz", "p2.index")


def same_tree(left, right):
    def files(root):
        return {p.relative_to(root) for p in root.rglob("*") if p.is_file()}
    names = files(left)
    return names == files(right) and all(
        (left / p).read_bytes() == (right / p).read_bytes() for p in names)


def assemble(incoming, site, release):
    if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9._-]*", release):
        raise ValueError("Invalid release directory name")
    for name in ("content.jar", "artifacts.jar"):
        with ZipFile(incoming / name) as archive:
            ET.fromstring(archive.read(name.replace(".jar", ".xml")))
    if not (incoming / "plugins").is_dir() or not (incoming / "features").is_dir():
        raise ValueError("The incoming p2 repository has no plugins or features")
    site.mkdir(parents=True, exist_ok=True)
    releases = site / "releases"
    releases.mkdir(exist_ok=True)

    # Keep the old root JAR paths available for clients with cached simple-repository metadata.
    if any((site / name).exists() for name in ("content.jar", "content.xml", "content.xml.xz")):
        legacy = releases / "legacy"
        if legacy.exists():
            raise ValueError("Cannot overwrite an existing legacy repository")
        legacy.mkdir()
        for name in (*METADATA, "plugins", "features"):
            source = site / name
            if source.is_dir():
                shutil.copytree(source, legacy / name)
            elif source.is_file():
                shutil.copy2(source, legacy / name)

    destination = releases / release
    if destination.exists():
        if not same_tree(incoming, destination):
            raise ValueError(f"Refusing to replace immutable release {release}")
    else:
        shutil.copytree(incoming, destination)

    for name in METADATA:
        (site / name).unlink(missing_ok=True)
    children = [p for p in sorted(releases.iterdir())
                if p.is_dir() and any((p / n).is_file()
                                     for n in ("content.jar", "content.xml", "content.xml.xz"))]
    for kind in ("Metadata", "Artifact"):
        root = ET.Element("repository", {
            "name": "GAMA Developer Tools",
            "type": f"org.eclipse.equinox.internal.p2.{kind.lower()}.repository.Composite{kind}Repository",
            "version": "1.0.0",
        })
        properties = ET.SubElement(root, "properties", size="1")
        ET.SubElement(properties, "property", name="p2.timestamp", value=str(int(time.time() * 1000)))
        entries = ET.SubElement(root, "children", size=str(len(children)))
        for child in children:
            ET.SubElement(entries, "child", location=f"releases/{child.name}")
        ET.indent(root)
        filename = "compositeContent.xml" if kind == "Metadata" else "compositeArtifacts.xml"
        instruction = "compositeMetadataRepository" if kind == "Metadata" else "compositeArtifactRepository"
        (site / filename).write_bytes(
            f'<?xml version="1.0" encoding="UTF-8"?>\n<?{instruction} version="1.0.0"?>\n'.encode()
            + ET.tostring(root, encoding="utf-8") + b"\n")
    (site / "p2.index").write_text(
        "version=1\nmetadata.repository.factory.order=compositeContent.xml,!\n"
        "artifact.repository.factory.order=compositeArtifacts.xml,!\n", encoding="utf-8")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("incoming", type=Path)
    parser.add_argument("site", type=Path)
    parser.add_argument("release")
    args = parser.parse_args()
    assemble(args.incoming, args.site, args.release)
