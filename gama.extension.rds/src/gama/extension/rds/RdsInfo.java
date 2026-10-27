/*******************************************************************************************************
 *
 * RdsInfo.java, in gama.extension.rds, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.rds;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.utils.files.AbstractFileMetaData;
import gama.dev.DEBUG;

/** Cached stream-header metadata, without deserializing the R object. */
public final class RdsInfo extends AbstractFileMetaData {

	private final Map<String, String> properties = new LinkedHashMap<>();
	private String summary = "RDS";

	public RdsInfo(final IFile file) {
		super(file);
		try {
			final var header = RdsIO.readHeader(file.getContents());
			properties.put("Serialization", "version " + header.version());
			properties.put("Compression", header.gzip() ? "gzip" : "none");
			properties.put("Encoding", header.encoding());
			properties.put("Written by R", RdsIO.Header.rVersion(header.writerVersion()));
			properties.put("Minimum R reader", RdsIO.Header.rVersion(header.minimumReaderVersion()));
			summary = "RDS";
			if (header.version() != 2) {
				summary = "RDS v" + header.version() + " (unsupported)";
				properties.put("Status", "Unsupported serialization version. Re-export with saveRDS(object, path, version = 2).");
			}
		} catch (final IOException | CoreException | IllegalArgumentException e) {
			summary = "RDS (unreadable header)";
			properties.put("Status", "Cannot read RDS header: " + e.getMessage());
			DEBUG.ERR("Cannot inspect RDS file " + file.getName() + ": " + e.getMessage());
		}
	}

	public RdsInfo(final String propertyString) {
		super(propertyString);
		final String[] parts = split(propertyString);
		if (parts.length != 3 || !"rds-header1".equals(parts[1]))
			throw new IllegalArgumentException("Invalid RDS header metadata");
		final Properties stored = new Properties();
		try {
			stored.load(new StringReader(new String(Base64.getDecoder().decode(parts[2]), StandardCharsets.UTF_8)));
			summary = stored.getProperty("summary");
			final int count = Integer.parseInt(stored.getProperty("count"));
			for (int i = 0; i < count; i++)
				properties.put(stored.getProperty("key" + i), stored.getProperty("value" + i));
		} catch (final IOException e) {
			throw new IllegalArgumentException("Invalid RDS metadata", e);
		}
	}

	@Override
	public void appendSuffix(final StringBuilder sb) {
		sb.append(summary);
		if (properties.containsKey("Serialization")) sb.append(SUFFIX_DEL).append(properties.get("Serialization"));
		if (properties.containsKey("Compression")) sb.append(SUFFIX_DEL).append(properties.get("Compression"));
	}

	@Override
	public IGamlDocumentation getDocumentation() {
		final var doc = new GamlRegularDocumentation();
		doc.append("R serialized data").append("<br/>");
		properties.forEach((key, value) -> doc.append(escape(key)).append(": ").append(escape(value)).append("<br/>"));
		return doc;
	}

	private static String escape(final String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	@Override
	public String toPropertyString() {
		final Properties stored = new Properties();
		stored.setProperty("summary", summary);
		stored.setProperty("count", String.valueOf(properties.size()));
		int i = 0;
		for (final var entry : properties.entrySet()) {
			stored.setProperty("key" + i, entry.getKey());
			stored.setProperty("value" + i++, entry.getValue());
		}
		final StringWriter output = new StringWriter();
		try {
			stored.store(output, null);
		} catch (final IOException e) {
			throw new IllegalStateException("Cannot encode RDS metadata", e);
		}
		return super.toPropertyString() + DELIMITER + "rds-header1" + DELIMITER
				+ Base64.getEncoder().encodeToString(output.toString().getBytes(StandardCharsets.UTF_8));
	}
}
