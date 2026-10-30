package gama.export;

import java.util.List;

/** Document classification must not load SWT, GAMA or a workspace service. */
public final class PackageDocumentsTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	public static void main(final String[] args) {
		check(PackageDocuments.fromArguments(new String[] { "--launcher.defaultAction", "openFile", "/a model.GAMX" })
				.equals(List.of("/a model.GAMX")), "Do not interpret native launcher options as documents");
		check(PackageDocuments.fromArguments(new String[] { "-data", "workspace.gamx", "-nosplash", "/model.gamx",
				"-vmargs", "-Dexample=other.gamx" }).equals(List.of("/model.gamx")), "Ignore option values and VM arguments");
		check(PackageDocuments.fromArguments(new String[] { "-openFile", "one.gamx", "two.gamx" })
				.equals(List.of("one.gamx", "two.gamx")), "Support multiple native document arguments");
		check(PackageDocuments.fromArguments(new String[] { "--launcher.openFile", "/a model.gamx" })
				.equals(List.of("/a model.gamx")), "Use the same native open-file switch as desktop associations");
		check(PackageDocuments.fromArguments(new String[] { "--", "-model.gamx" }).equals(List.of("-model.gamx")),
				"Allow explicit documents beginning with a hyphen");
		check(!PackageDocuments.packagesOnly(List.of()) && !PackageDocuments.packagesOnly(List.of("model.gaml"))
				&& !PackageDocuments.packagesOnly(List.of("one.gamx", "model.gaml")),
				"Preserve normal modeling and mixed-document invocations");
		check(PackageDocuments.packagesOnly(List.of("/one.gamx", "/other.GAMX")), "Package-only launches use the cold router");
		check(!PackageDocuments.isPackage("/project.gamp") && !PackageDocuments.isPackage("/project.GAMP"),
				"Reserve project archives for a separate workflow");
		System.out.println("Cold-start document classification checks passed");
	}
}
