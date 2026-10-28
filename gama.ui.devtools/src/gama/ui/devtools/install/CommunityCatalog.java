package gama.ui.devtools.install;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import gama.ui.devtools.install.InstallModel.RemoteProject;
import gama.ui.devtools.install.InstallModel.Repository;

/** Discovers projects without cloning every repository in the community organization. */
public final class CommunityCatalog {

	public record Catalog(List<RemoteProject> projects, List<String> unavailable) {}

	private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();

	public Catalog load(final String branch, final IProgressMonitor monitor) throws Exception {
		List<RemoteProject> projects = new ArrayList<>();
		List<String> unavailable = new ArrayList<>();
		for (int page = 1; ; page++) {
			var repositories = request("/orgs/gama-platform-plugins/repos?per_page=100&page=" + page, monitor)
					.getAsJsonArray();
			for (JsonElement element : repositories) {
				var repo = element.getAsJsonObject();
				String name = repo.get("name").getAsString();
				monitor.subTask("Listing " + name + " (" + branch + ")");
				Repository repository = new Repository(name, repo.get("clone_url").getAsString(), branch);
				JsonElement tree = request("/repos/gama-platform-plugins/" + name + "/git/trees/"
						+ URLEncoder.encode(branch, StandardCharsets.UTF_8) + "?recursive=1", monitor);
				if (tree == null) {
					unavailable.add(name + " (no branch " + branch + ")");
					continue;
				}
				if (tree.getAsJsonObject().get("truncated").getAsBoolean()) {
					throw new IOException("GitHub truncated the project list for " + name + ". Try that repository separately.");
				}
				int before = projects.size();
				for (JsonElement entry : tree.getAsJsonObject().getAsJsonArray("tree")) {
					String path = entry.getAsJsonObject().get("path").getAsString();
					if (path.equals(".project") || path.endsWith("/.project")) {
						projects.add(new RemoteProject(repository,
								path.equals(".project") ? "" : path.substring(0, path.length() - 9)));
					}
				}
				if (before == projects.size()) unavailable.add(name + " (no Eclipse projects)");
			}
			if (repositories.size() < 100) break;
		}
		projects.sort(Comparator.comparing(RemoteProject::toString));
		return new Catalog(List.copyOf(projects), List.copyOf(unavailable));
	}

	private JsonElement request(final String path, final IProgressMonitor monitor) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.github.com" + path))
				.timeout(Duration.ofSeconds(60)).header("Accept", "application/vnd.github+json")
				.header("User-Agent", "GAMA-Developer-Tools").GET().build();
		var response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
		try {
			while (true) {
				if (monitor.isCanceled()) throw new OperationCanceledException();
				try {
					var result = response.get(200, TimeUnit.MILLISECONDS);
					if (result.statusCode() == 404 && path.contains("/git/trees/")) return null;
					if (result.statusCode() != 200) {
						throw new IOException("GitHub returned HTTP " + result.statusCode() + " for " + path
								+ ". Check connectivity and the public API rate limit. " + result.body());
					}
					return JsonParser.parseString(result.body());
				} catch (TimeoutException e) {
					// Poll only to allow cancellation of the HTTP request.
				} catch (ExecutionException e) {
					throw new IOException("Cannot contact GitHub: " + e.getCause().getMessage(), e.getCause());
				}
			}
		} finally {
			if (!response.isDone()) response.cancel(true);
		}
	}
}
