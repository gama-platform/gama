package gama.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.eclipse.core.resources.IFile;

import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.utils.files.AbstractFileMetaData;
import gama.dev.DEBUG;

public final class ProjectArchiveInfo extends AbstractFileMetaData {

	private final String description;
	private final String suffix;

	public ProjectArchiveInfo(final IFile file) {
		super(file);
		String details;
		String summary;
		try {
			if (file.getLocation() == null) throw new IOException("Project archive is not local");
			final var info = ProjectArchive.inspect(file.getLocation().toFile().toPath());
			details = info.description();
			summary = String.join(", ", info.projects()) + SUFFIX_DEL + info.files() + " files"
					+ SUFFIX_DEL + info.plugins().stream().filter(p -> !p.artifact().isEmpty()).count() + " plugins";
		} catch (final IOException e) {
			details = "Invalid project archive: " + e.getMessage();
			summary = details;
			DEBUG.ERR(details, e);
		}
		description = details;
		suffix = summary;
	}

	public ProjectArchiveInfo(final String properties) {
		super(properties);
		final var parts = split(properties);
		suffix = decode(parts[1]);
		description = decode(parts[2]);
	}

	public String getDescription() { return description; }

	@Override public void appendSuffix(final StringBuilder sb) { sb.append(suffix); }

	@Override public IGamlDocumentation getDocumentation() {
		return new GamlConstantDocumentation(description.replace("&", "&amp;").replace("<", "&lt;")
				.replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;").replace("\n", "<br/>"));
	}

	@Override public String toPropertyString() {
		return super.toPropertyString() + DELIMITER + encode(suffix) + DELIMITER + encode(description);
	}

	private static String encode(final String text) {
		return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
	}

	private static String decode(final String text) {
		return new String(Base64.getDecoder().decode(text), StandardCharsets.UTF_8);
	}
}
