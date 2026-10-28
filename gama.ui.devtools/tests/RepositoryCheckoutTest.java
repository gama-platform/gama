package gama.ui.devtools.install;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jgit.api.Git;

import gama.ui.devtools.install.InstallModel.Repository;

/** Runs only against disposable local repositories, never the developer's checkout. */
public class RepositoryCheckoutTest {

	public static void main(final String[] args) throws Exception {
		Path root = Files.createTempDirectory("gama-install-checkout-");
		try {
			Path origin = root.resolve("origin"), checkout = root.resolve("checkout");
			try (Git upstream = Git.init().setDirectory(origin.toFile()).setInitialBranch("main").call()) {
				commit(upstream, "initial", "one");
				Repository source = new Repository("test", origin.toUri().toString(), "main");
				var monitor = new NullProgressMonitor();
				RepositoryCheckout.prepare(source, checkout, monitor);
				try (Git local = Git.open(checkout.toFile())) {
					String initial = local.getRepository().resolve("HEAD").name();
					commit(upstream, "upstream", "two");
					RepositoryCheckout.prepare(source, checkout, monitor);
					check(!initial.equals(local.getRepository().resolve("HEAD").name()), "Fast-forward applied");
					Files.writeString(checkout.resolve("file.txt"), "unsaved work");
					expectFailure(() -> RepositoryCheckout.prepare(source, checkout, monitor), "Dirty checkout refused");
					check(Files.readString(checkout.resolve("file.txt")).equals("unsaved work"), "Local edits preserved");
					local.add().addFilepattern(".").call();
					local.commit().setMessage("local commit").setAuthor("Test", "test@example.invalid").call();
					String localCommit = local.getRepository().resolve("HEAD").name();
					expectFailure(() -> RepositoryCheckout.prepare(source, checkout, monitor), "Local commits refused");
					check(localCommit.equals(local.getRepository().resolve("HEAD").name()), "Local commit preserved");
					upstream.branchCreate().setName("GAMA_2026-06").call();
					Repository branch = new Repository("test", source.url(), "GAMA_2026-06");
					RepositoryCheckout.prepare(branch, checkout, monitor);
					check(local.getRepository().getBranch().equals("GAMA_2026-06"), "Requested branch checked out");
					check(localCommit.equals(local.getRepository().resolve("refs/heads/main").name()), "Other branch retained");
					expectFailure(() -> RepositoryCheckout.prepare(
							new Repository("test", root.resolve("other").toUri().toString(), branch.branch()), checkout, monitor),
							"Mismatched origin refused");
					expectFailure(() -> RepositoryCheckout.prepare(
							new Repository("test", source.url(), "missing"), checkout, monitor), "Missing branch refused");
					check(local.getRepository().getBranch().equals("GAMA_2026-06"), "Branch unchanged after missing branch");
					monitor.setCanceled(true);
					try {
						RepositoryCheckout.prepare(branch, checkout, monitor);
						throw new AssertionError("Cancellation ignored");
					} catch (OperationCanceledException expected) {}
				}
				Path occupied = root.resolve("occupied");
				Files.createDirectories(occupied);
				Files.writeString(occupied.resolve("keep.txt"), "keep");
				expectFailure(() -> RepositoryCheckout.prepare(source, occupied, new NullProgressMonitor()),
						"Non-repository directory refused");
				check(Files.readString(occupied.resolve("keep.txt")).equals("keep"), "Existing files retained");
				check(RepositoryCheckout.sameRemote("git@github.com:gama-platform/gama.git", InstallModel.Kind.CORE.url),
						"SSH origin matches HTTPS source");
			}
		} finally {
			try (var paths = Files.walk(root)) {
				for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
			}
		}
		System.out.println("Repository checkout safety checks passed");
	}

	private static void commit(final Git git, final String message, final String text) throws Exception {
		Files.writeString(git.getRepository().getWorkTree().toPath().resolve("file.txt"), text);
		git.add().addFilepattern(".").call();
		git.commit().setMessage(message).setAuthor("Test", "test@example.invalid").call();
	}

	@FunctionalInterface
	private interface Checked { void run() throws Exception; }

	private static void expectFailure(final Checked operation, final String message) throws Exception {
		try {
			operation.run();
			throw new AssertionError(message);
		} catch (IOException | org.eclipse.jgit.api.errors.GitAPIException expected) {}
	}

	private static void check(final boolean condition, final String message) {
		if (!condition) throw new AssertionError(message);
	}
}
