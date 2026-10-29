package gama.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;

import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.utils.files.AbstractFileMetaData;
import gama.dev.DEBUG;

/** Reads archive metadata only; never extracts content or loads embedded plugins. */
public final class ExportedModelInfo extends AbstractFileMetaData {

	private final String suffix;
	private final String documentation;

	public ExportedModelInfo(final IFile file) {
		super(file);
		String summary;
		String details;
		try {
			if (file.getLocation() == null) throw new IOException("The exported model is not a local file");
			final var manifest = SimulationPackage.inspect(file.getLocation().toFile().toPath());
			final var models = manifest.experiments().stream().map(SimulationPackageManifest.Experiment::model)
					.distinct().toList();
			summary = String.join(", ", models) + SUFFIX_DEL + manifest.experiments().size() + " experiment"
					+ (manifest.experiments().size() == 1 ? "" : "s");
			details = "Standalone model\n\nModels: " + String.join(", ", models)
					+ "\nExperiments: " + manifest.experiments().stream()
							.map(e -> e.name() + " (" + e.model() + ")").collect(Collectors.joining(", "))
					+ "\nRequires: GAMA " + manifest.runtimeVersion() + " or newer";
			final String plugins = manifest.bundles().stream().filter(b -> !b.artifact().isEmpty())
					.map(b -> b.symbolicName() + " " + b.version()).collect(Collectors.joining(", "));
			if (!plugins.isEmpty()) details += "\nIncluded plugins: " + plugins;
		} catch (final IOException e) {
			summary = "Invalid exported model: " + e.getMessage();
			details = summary;
			DEBUG.ERR("Unable to read exported model " + file.getName() + ": " + e.getMessage());
		}
		suffix = summary;
		documentation = details;
	}

	public ExportedModelInfo(final String properties) {
		super(properties);
		final String[] parts = split(properties);
		suffix = decode(parts[1]);
		documentation = decode(parts[2]);
	}

	@Override
	public void appendSuffix(final StringBuilder sb) { sb.append(suffix); }

	@Override
	public IGamlDocumentation getDocumentation() {
		return new GamlConstantDocumentation(html(documentation).replace("\n", "<br/>"));
	}

	public String getDescription() { return documentation; }

	@Override
	public String toPropertyString() {
		return super.toPropertyString() + DELIMITER + encode(suffix) + DELIMITER + encode(documentation);
	}

	private static String encode(final String text) {
		return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
	}

	private static String decode(final String text) {
		return new String(Base64.getDecoder().decode(text), StandardCharsets.UTF_8);
	}

	private static String html(final String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&#39;");
	}
}
