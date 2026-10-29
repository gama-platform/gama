package gama.workspace.manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import gama.dev.DEBUG;
import gama.api.GAMA;

/** Result of reconciling the workspace with the running installation's library. */
public final class LibraryUpdateReport {

	private static final String PLUGIN = "gama.workspace";
	private final List<String> changes = new ArrayList<>();
	private final MultiStatus status = new MultiStatus(PLUGIN, 0, "Library reconciliation needs attention", null);
	private int available;
	private Path savedReport;

	void available() { available++; }

	void changed(final String message) { changes.add(message); }

	void warning(final String message) {
		status.add(new Status(IStatus.WARNING, PLUGIN, message));
		DEBUG.LOG(message);
	}

	public void error(final String message, final Throwable cause) {
		status.add(new Status(IStatus.ERROR, PLUGIN, message, cause));
		if (cause == null) {
			DEBUG.ERR(message);
		} else {
			DEBUG.ERR(message, cause);
		}
	}

	public boolean needsAttention() { return !status.isOK(); }

	public IStatus getStatus() { return status.isOK() ? Status.OK_STATUS : status; }

	/** Keep the full diagnostics in workspace metadata; dialogs show a bounded preview. */
	public void save() {
		final Path destination = Path.of(GAMA.getWorkspaceManager().getWorkspaceLocation(),
				".metadata", "gama-library-repair.log");
		try {
			Files.writeString(destination, Instant.now() + "\n" + summary(false));
			savedReport = destination;
		} catch (final IOException e) {
			error("Could not save the library repair report to " + destination, e);
		}
	}

	public String getSummary() { return summary(true); }

	private String summary(final boolean preview) {
		final StringBuilder result = new StringBuilder("Workspace library: ").append(available)
				.append(" projects available from this installation.\n");
		if (preview) { result.append(changes.size()).append(" changes made.\n"); }
		final int limit = preview ? 5 : Integer.MAX_VALUE;
		final IStatus[] issues = status.getChildren();
		for (int i = 0; i < Math.min(issues.length, limit); i++) {
			result.append("\n").append(issues[i].getMessage()).append("\n");
		}
		for (int i = 0; i < Math.min(changes.size(), limit); i++) { result.append("\n").append(changes.get(i)); }
		if (preview && (changes.size() > limit || issues.length > limit)) {
			result.append("\n\nAdditional details are in the full report.");
		}
		if (preview && savedReport != null) { result.append("\n\nFull report: ").append(savedReport); }
		return result.toString();
	}
}
