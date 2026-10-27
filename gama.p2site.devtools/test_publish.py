import tempfile
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET
from zipfile import ZipFile

from publish import assemble


def repository(path, version):
    path.mkdir(parents=True)
    for kind in ("content", "artifacts"):
        with ZipFile(path / f"{kind}.jar", "w") as archive:
            archive.writestr(f"{kind}.xml", "<repository/>")
    for kind in ("plugins", "features"):
        (path / kind).mkdir()
        (path / kind / f"gama_{version}.jar").write_bytes(version.encode())
    (path / "p2.index").write_text("version=1\n")


class PublishTest(unittest.TestCase):
    def test_retention_and_immutable_releases(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            one, two, site = root / "one", root / "two", root / "site"
            repository(one, "1")
            repository(two, "2")
            assemble(one, site, "build-1")
            saved = {p.relative_to(site): p.read_bytes()
                     for p in (site / "releases/build-1").rglob("*") if p.is_file()}
            assemble(two, site, "build-2")
            for path, data in saved.items():
                self.assertEqual((site / path).read_bytes(), data)
            for name, kind in (("compositeContent.xml", "metadata"),
                               ("compositeArtifacts.xml", "artifact")):
                tree = ET.parse(site / name).getroot()
                self.assertIn(f".p2.{kind}.repository.", tree.get("type"))
                self.assertEqual([e.get("location") for e in tree.findall("children/child")],
                                 ["releases/build-1", "releases/build-2"])
            assemble(one, site, "build-1")
            with self.assertRaises(ValueError):
                assemble(two, site, "build-1")
            with self.assertRaises(ValueError):
                assemble(two, site, "../unsafe")

    def test_migration_preserves_cached_artifact_paths(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            incoming, site = root / "incoming", root / "site"
            repository(site, "old")
            repository(incoming, "new")
            (site / "content.xml.xz").write_bytes(b"old compressed metadata")
            assemble(incoming, site, "new")
            self.assertEqual((site / "plugins/gama_old.jar").read_bytes(), b"old")
            self.assertEqual((site / "features/gama_old.jar").read_bytes(), b"old")
            self.assertTrue((site / "releases/legacy/content.jar").exists())
            self.assertFalse((site / "content.jar").exists())
            self.assertFalse((site / "content.xml.xz").exists())
            self.assertIn("compositeContent.xml", (site / "p2.index").read_text())
            children = ET.parse(site / "compositeContent.xml").findall("children/child")
            self.assertEqual(len(children), 2)
            assemble(incoming, site, "new")

    def test_invalid_input_does_not_modify_site(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "incoming").mkdir()
            with self.assertRaises(FileNotFoundError):
                assemble(root / "incoming", root / "site", "new")
            self.assertFalse((root / "site").exists())


if __name__ == "__main__":
    unittest.main()
