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
		check(windows.contains("Software\\Classes\\.gamx\\OpenWithProgids")
				&& windows.contains("#define PackageAssocKey \"Gama.SimulationPackage\""),
				"Windows installer must register a package-specific Open With entry");
		check(windows.contains("--launcher.openFile \"\"%1\"\"\"")
				&& windows.contains("ValueName: \".gamx\"; ValueData: \"\"; Flags: uninsdeletevalue"),
				"Quote package document paths and clean up registration on uninstall");
		check(windows.contains("ValueData: \"\"\"{app}\\StandaloneModel.ico\"\",0\""),
				"Windows playable models use their own document icon");
		check(!windows.contains("UserChoice"), "Do not overwrite the user's selected default application");
		final String desktop = Files.readString(installer.resolve("unix/gama-platform.desktop"));
		check(desktop.contains("--launcher.openFile %F")
				&& desktop.contains("MimeType=application/x-gama-simulation-package;text/x-gama-model;"),
				"Linux desktop launching must pass document paths to the native launcher");
		final var factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		final var xml = factory.newDocumentBuilder().parse(installer.resolve("unix/gama-simulation-package.xml").toFile());
		check((Boolean) XPathFactory.newInstance().newXPath().evaluate(
				"//*[local-name()='mime-type' and @type='application/x-gama-simulation-package']"
						+ "/*[local-name()='glob' and @pattern='*.gamx']", xml, XPathConstants.BOOLEAN),
				"Ship a shared MIME database definition for .gamx");
		check((Boolean) XPathFactory.newInstance().newXPath().evaluate(
				"//*[local-name()='icon' and @name='application-x-gama-simulation-package']",
				xml, XPathConstants.BOOLEAN), "Linux MIME icon name must match the installed hicolor asset");
		final String workflow = Files.readString(root.resolve(".github/workflows/travis-packaging-linux.yaml"));
		check(workflow.contains("/usr/share/mime/packages/gama-simulation-package.xml"),
				"Include the MIME definition in Debian release packaging");
		check(workflow.contains("/usr/share/icons/hicolor/512x512/mimetypes/application-x-gama-simulation-package.png"),
				"Install the document artwork in the system icon theme");
		check(Files.readString(installer.resolve("unix/DEBIAN/postinst")).contains("update-mime-database /usr/share/mime"),
				"Refresh the MIME cache during installation");
		final var plistFactory = DocumentBuilderFactory.newInstance();
		plistFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		plistFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		plistFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		final var plist = plistFactory.newDocumentBuilder().parse(root.resolve("gama.product/extraresources/Info.plist").toFile());
		check((Boolean) XPathFactory.newInstance().newXPath().evaluate(
				"/plist/dict/key[text()='CFBundleDocumentTypes']/following-sibling::*[1]/dict"
						+ "[array/string='gamx']/key[text()='CFBundleTypeIconFile']"
						+ "/following-sibling::*[1][text()='StandaloneModel.icns']", plist, XPathConstants.BOOLEAN),
				"Finder must associate playable models with the new document icon");
		final var product = factory.newDocumentBuilder().parse(root.resolve("gama.product/pom.xml").toFile());
		final String packaging = Files.readString(root.resolve("gama.product/pom.xml"));
		check(packaging.contains("extraresources/StandaloneModel.icns")
				&& packaging.contains("extraresources/StandaloneModel.ico")
				&& packaging.contains("extraresources/StandaloneModel.png"), "Ship icons in every product archive");
		check(product != null, "Product build XML remains valid");
		final Path resources = root.resolve("gama.product/extraresources");
		final byte[] ico = Files.readAllBytes(resources.resolve("StandaloneModel.ico"));
		final var header = java.nio.ByteBuffer.wrap(ico).order(java.nio.ByteOrder.LITTLE_ENDIAN);
		check(header.getShort() == 0 && header.getShort() == 1 && header.getShort() == 7,
				"Windows icon includes seven native resolutions");
		for (int i = 0; i < 7; i++) {
			header.position(6 + i * 16);
			final int widthByte = Byte.toUnsignedInt(header.get());
			final int width = widthByte == 0 ? 256 : widthByte;
			header.position(6 + i * 16 + 8);
			final int length = header.getInt();
			final int offset = header.getInt();
			final var image = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(ico, offset, length));
			check(image != null && image.getWidth() == width && image.getHeight() == width,
					"Each Windows icon entry must contain a valid PNG of the declared size");
		}
		check(java.util.Arrays.equals(Files.readAllBytes(resources.resolve("StandaloneModel.icns")),
				Files.readAllBytes(resources.resolve("Experiment.icns"))), "Reuse the existing green Play artwork exactly");
		check((Boolean) XPathFactory.newInstance().newXPath().evaluate(
				"/plist/dict/key[text()='CFBundleDocumentTypes']/following-sibling::*[1]/dict"
						+ "[array/string='experiment']/key[text()='CFBundleTypeIconFile']"
						+ "/following-sibling::*[1][text()='Model.icns']", plist, XPathConstants.BOOLEAN),
				"Experiment source files use the same blue-G artwork as model source files");
		check((Boolean) XPathFactory.newInstance().newXPath().evaluate(
				"/plist/dict/key[text()='UTExportedTypeDeclarations']/following-sibling::*[1]/dict"
						+ "/key[text()='UTTypeIdentifier']/following-sibling::string[1][text()='org.gama.simulation-package']",
				plist, XPathConstants.BOOLEAN), "Declare the package UTI at the top level, not inside another document type");
		System.out.println("macOS/Windows/Linux package association packaging checks passed");
	}
}
