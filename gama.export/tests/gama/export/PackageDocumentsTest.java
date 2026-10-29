package gama.export;

import java.util.List;

/** Document classification must not load SWT, GAMA or a workspace service. */
public final class PackageDocumentsTest {

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}

	public static void main(final String[] args) {
		check(PackageDocuments.fromArguments(new String[] { "--launcher.defaultAction", "openFile", "/a model.GAMP" })
				.equals(List.of("/a model.GAMP")), "Do not interpret native launcher options as documents");
		check(PackageDocuments.fromArguments(new String[] { "-data", "workspace.gamp", "-nosplash", "/model.gamp",
				"-vmargs", "-Dexample=other.gamp" }).equals(List.of("/model.gamp")), "Ignore option values and VM arguments");
		check(PackageDocuments.fromArguments(new String[] { "-openFile", "one.gamp", "two.gamp" })
				.equals(List.of("one.gamp", "two.gamp")), "Support multiple native document arguments");
		check(PackageDocuments.fromArguments(new String[] { "--launcher.openFile", "/a model.gamp" })
				.equals(List.of("/a model.gamp")), "Use the same native open-file switch as desktop associations");
		check(PackageDocuments.fromArguments(new String[] { "--", "-model.gamp" }).equals(List.of("-model.gamp")),
				"Allow explicit documents beginning with a hyphen");
		check(!PackageDocuments.packagesOnly(List.of()) && !PackageDocuments.packagesOnly(List.of("model.gaml"))
				&& !PackageDocuments.packagesOnly(List.of("one.gamp", "model.gaml")),
				"Preserve normal modeling and mixed-document invocations");
		check(PackageDocuments.packagesOnly(List.of("/one.gamp", "/other.GAMP")), "Package-only launches use the cold router");
		System.out.println("Cold-start document classification checks passed");
	}
}
