package gama.export;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Presentation and startup options embedded in an exported launcher. */
public record LauncherConfiguration(String title, String description, boolean showHome, String startupExperiment,
		String image) {

	public static final String FILE = "configuration/gama-launcher.properties";
	public static final String IMAGE = "configuration/gama-launcher-image";

	public LauncherConfiguration {
		title = title == null ? "" : title;
		description = description == null ? "" : description;
		startupExperiment = startupExperiment == null ? "" : startupExperiment;
		showHome = showHome || startupExperiment.isBlank();
		image = image == null ? "" : image;
	}

	public static LauncherConfiguration defaults() {
		return new LauncherConfiguration("", "", true, "", "");
	}

	public String serialize() throws IOException {
		final Properties properties = new Properties();
		properties.setProperty("title", title);
		properties.setProperty("description", description);
		properties.setProperty("showHome", Boolean.toString(showHome));
		properties.setProperty("startupExperiment", startupExperiment);
		properties.setProperty("image", image.isBlank() ? "" : IMAGE);
		final StringWriter writer = new StringWriter();
		properties.store(writer, "GAMA playable model");
		return writer.toString();
	}

	public static LauncherConfiguration load(final Path installation) throws IOException {
		final Path file = installation.resolve(FILE);
		if (!Files.exists(file)) return defaults();
		final Properties properties = new Properties();
		try (var input = Files.newBufferedReader(file)) { properties.load(input); }
		return new LauncherConfiguration(properties.getProperty("title", ""), properties.getProperty("description", ""),
				Boolean.parseBoolean(properties.getProperty("showHome", "false")),
				properties.getProperty("startupExperiment", ""), properties.getProperty("image", ""));
	}
}
