/*******************************************************************************************************
 *
 * TestProcessProtocol.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.tests;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.emf.common.util.URI;

/** Length-prefixed snapshots keep a killed worker's incomplete final write out of the report. */
public final class TestProcessProtocol {

	public static final int READY = 1, SUMMARY = 2, RUNNING = 3, TEST = 4, RESULT = 5, ERROR = 6, CONSOLE = 7;

	private TestProcessProtocol() {}

	@FunctionalInterface
	public interface Payload {
		void write(DataOutput out) throws IOException;
	}

	public static synchronized void send(final OutputStream stream, final int event, final Payload payload)
			throws IOException {
		final var bytes = new ByteArrayOutputStream();
		try (var out = new DataOutputStream(bytes)) {
			out.writeInt(event);
			payload.write(out);
		}
		final var framed = new DataOutputStream(stream);
		framed.writeInt(bytes.size());
		bytes.writeTo(framed);
		framed.flush();
	}

	public static void writeSummary(final DataOutput out, final AbstractSummary<?> summary) throws IOException {
		writeString(out, summary.getTitle());
		writeString(out, summary.getURI() == null ? "" : summary.getURI().toString());
		writeString(out, summary.getState().name());
		writeString(out, summary.getError() == null ? "" : summary.getError());
		writeString(out, summary.getErrorDetails() == null ? "" : summary.getErrorDetails());
		out.writeInt(summary.getSummaries().size());
		for (final var entry : summary.getSummaries().entrySet()) {
			writeString(out, entry.getKey());
			writeSummary(out, entry.getValue());
		}
	}

	public static TestExperimentSummary readExperiment(final DataInput in) throws IOException {
		final String title = readString(in), uri = readString(in);
		final var summary = new TestExperimentSummary(title, uri.isEmpty() ? null : URI.createURI(uri));
		final int count = readHeader(in, summary);
		for (int i = 0; i < count; i++) { summary.summaries.put(readString(in), readTest(in)); }
		return summary;
	}

	public static IndividualTestSummary readTest(final DataInput in) throws IOException {
		final String title = readString(in), uri = readString(in);
		final var summary = new IndividualTestSummary(title, uri.isEmpty() ? null : URI.createURI(uri));
		final int count = readHeader(in, summary);
		for (int i = 0; i < count; i++) {
			final String key = readString(in);
			final String assertionTitle = readString(in), assertionUri = readString(in);
			final var assertion = new AssertionSummary(assertionTitle,
					assertionUri.isEmpty() ? null : URI.createURI(assertionUri));
			if (readHeader(in, assertion) != 0) throw new IOException("Unexpected nested assertion snapshot");
			summary.summaries.put(key, assertion);
		}
		return summary;
	}

	private static int readHeader(final DataInput in, final AbstractSummary<?> summary) throws IOException {
		summary.setState(TestState.valueOf(readString(in)));
		final String error = readString(in);
		if (!error.isEmpty()) { summary.setError(error); }
		final String details = readString(in);
		if (!details.isEmpty()) { summary.setErrorDetails(details); }
		final int count = in.readInt();
		if (count < 0) throw new IOException("Negative test snapshot size");
		return count;
	}

	public static void writeString(final DataOutput out, final String value) throws IOException {
		final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		out.writeInt(bytes.length);
		out.write(bytes);
	}

	public static String readString(final DataInput in) throws IOException {
		final int length = in.readInt();
		if (length < 0 || length > 64 * 1024 * 1024) throw new IOException("Invalid test snapshot string size");
		final byte[] bytes = new byte[length];
		in.readFully(bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}
}
