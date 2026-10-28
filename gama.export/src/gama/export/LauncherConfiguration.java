package gama.export;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Presentation and startup options embedded in an exported launcher. */
public record LauncherConfiguration(String title, String description, String startupExperiment,
		String image) {

	public static final String FILE = "configuration/gama-launcher.properties";
	public static final String IMAGE = "configuration/gama-launcher-image";

	public LauncherConfiguration {
		title = title == null ? "" : title;
		description = description == null ? "" : description;
		startupExperiment = startupExperiment == null ? "" : startupExperiment;
		image = image == null ? "" : image;
	}

	public static LauncherConfiguration defaults() {
		return new LauncherConfiguration("", "", "", "");
	}

	/** Without a default experiment, startup stops at Home for the user to choose. */
	public boolean showHome() { return startupExperiment.isBlank(); }

	public String serialize() throws IOException {
		final Properties properties = new Properties();
		properties.setProperty("title", title);
		properties.setProperty("description", description);
		properties.setProperty("startupExperiment", startupExperiment);
		properties.setProperty("image", image.isBlank() ? "" : IMAGE);
		final StringWriter writer = new StringWriter();
		properties.store(writer, "GAMA simulation launcher");
		return writer.toString();
	}

	public static LauncherConfiguration load(final Path installation) throws IOException {
		final Path file = installation.resolve(FILE);
		if (!Files.exists(file)) return defaults();
		final Properties properties = new Properties();
		try (var input = Files.newBufferedReader(file)) { properties.load(input); }
		return new LauncherConfiguration(properties.getProperty("title", ""), properties.getProperty("description", ""),
				properties.getProperty("startupExperiment", ""), properties.getProperty("image", ""));
	}
}
