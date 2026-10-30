import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/** Source-level packaging assertions; installed OS association checks are performed separately. */
public final class PackageAssociationTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	public static void main(final String[] args) throws Exception {
		final Path root = Path.of(args.length == 0 ? "." : args[0]);
		final Path installer = root.resolve("gama.product/extraresources/installer");
		final String windows = Files.readString(installer.resolve("windows/windows_installer_script.iss"));
		check(windows.contains("Software\\Classes\\.gamp\\OpenWithProgids")
				&& windows.contains("#define PackageAssocKey \"Gama.SimulationPackage\""),
				"Windows installer must register a package-specific Open With entry");
		check(windows.contains("--launcher.openFile \"\"%1\"\"\"")
				&& windows.contains("ValueName: \".gamp\"; ValueData: \"\"; Flags: uninsdeletevalue"),
				"Quote package document paths and clean up registration on uninstall");
		check(!windows.contains("UserChoice"), "Do not overwrite the user's selected default application");
		final String desktop = Files.readString(installer.resolve("unix/gama-platform.desktop"));
		check(desktop.contains("--launcher.openFile %F")
				&& desktop.contains("MimeType=application/x-gama-simulation-package;"),
				"Linux desktop launching must pass document paths to the native launcher");
		final var factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		final var xml = factory.newDocumentBuilder().parse(installer.resolve("unix/gama-simulation-package.xml").toFile());
		check((Boolean) XPathFactory.newInstance().newXPath().evaluate(
				"//*[local-name()='mime-type' and @type='application/x-gama-simulation-package']"
						+ "/*[local-name()='glob' and @pattern='*.gamp']", xml, XPathConstants.BOOLEAN),
				"Ship a shared MIME database definition for .gamp");
		final String workflow = Files.readString(root.resolve(".github/workflows/travis-packaging-linux.yaml"));
		check(workflow.contains("/usr/share/mime/packages/gama-simulation-package.xml"),
				"Include the MIME definition in Debian release packaging");
		check(Files.readString(installer.resolve("unix/DEBIAN/postinst")).contains("update-mime-database /usr/share/mime"),
				"Refresh the MIME cache during installation");
		final var plistFactory = DocumentBuilderFactory.newInstance();
		plistFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		plistFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		plistFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		final var plist = plistFactory.newDocumentBuilder().parse(root.resolve("gama.product/extraresources/Info.plist").toFile());
		check((Boolean) XPathFactory.newInstance().newXPath().evaluate(
				"/plist/dict/key[text()='UTExportedTypeDeclarations']/following-sibling::*[1]/dict"
						+ "/key[text()='UTTypeIdentifier']/following-sibling::string[1][text()='org.gama.simulation-package']",
				plist, XPathConstants.BOOLEAN), "Declare the package UTI at the top level, not inside another document type");
		System.out.println("macOS/Windows/Linux package association packaging checks passed");
	}
}
