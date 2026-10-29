package gama.export;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Startup document classification without activating the modeling application. */
public final class PackageDocuments {

	public static final String PENDING_DOCUMENTS = "gama.pendingStartupDocuments";
	public static final String ARGUMENTS_CONSUMED = "gama.startupArgumentsConsumed";
	private static final Set<String> VALUE_OPTIONS = Set.of("-data", "-configuration", "-install", "-application",
			"-product", "-vm", "-name", "--launcher.defaultAction", "--launcher.library", "--launcher.ini");

	private PackageDocuments() {}

	public static boolean isPackage(final String path) {
		return path != null && path.toLowerCase(Locale.ROOT).endsWith(SimulationPackage.EXTENSION);
	}

	public static List<String> fromArguments(final String[] arguments) {
		final var result = new ArrayList<String>();
		boolean documentsOnly = false;
		for (int i = 0; i < arguments.length; i++) {
			final String argument = arguments[i];
			if (argument.equals("-vmargs")) break;
			if (argument.equals("--") || argument.equals("-openFile") || argument.equals("--launcher.openFile")) {
				documentsOnly = true;
			} else if (!documentsOnly && VALUE_OPTIONS.contains(argument)) {
				if (i + 1 < arguments.length) i++;
			} else if (documentsOnly || !argument.startsWith("-")) {
				result.add(argument);
			}
		}
		return List.copyOf(result);
	}

	public static boolean packagesOnly(final List<String> documents) {
		return !documents.isEmpty() && documents.stream().allMatch(PackageDocuments::isPackage);
	}
}
