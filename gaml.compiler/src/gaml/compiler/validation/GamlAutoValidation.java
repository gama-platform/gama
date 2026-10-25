/*******************************************************************************************************
 *
 * GamlAutoValidation.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform.
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.validation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.emf.common.util.URI;

/**
 * Workspace-local, per-file automatic validation policy. Explicit compilation does not consult this policy.
 * Requests carry a revision so work started before a toggle cannot publish results afterwards.
 */
public final class GamlAutoValidation {

	private static final QualifiedName DISABLED = new QualifiedName("gaml.compiler", "automaticValidationDisabled");
	private static final ConcurrentHashMap<URI, AtomicLong> revisions = new ConcurrentHashMap<>();
	private static final ThreadLocal<Request> current = new ThreadLocal<>();

	private GamlAutoValidation() {}

	private static IFile getFile(final URI uri) {
		if (uri == null || ResourcesPlugin.getPlugin() == null) return null;
		var root = ResourcesPlugin.getWorkspace().getRoot();
		if (uri.isPlatformResource()) return root.getFile(new Path(uri.toPlatformString(true)));
		if (uri.isFile()) {
			IFile[] files = root.findFilesForLocationURI(java.net.URI.create(uri.toString()));
			if (files.length > 0) return files[0];
		}
		return null;
	}

	public static boolean isEnabled(final URI uri) {
		return isEnabled(getFile(uri));
	}

	public static boolean isEnabled(final IFile file) {
		if (file == null || !file.exists()) return true;
		try {
			return !Boolean.parseBoolean(file.getPersistentProperty(DISABLED));
		} catch (CoreException e) {
			throw new IllegalStateException("Cannot read automatic validation setting for " + file.getFullPath(), e);
		}
	}

	public static void setEnabled(final IFile file, final boolean enabled) throws CoreException {
		file.setPersistentProperty(DISABLED, enabled ? null : Boolean.TRUE.toString());
		revision(URI.createPlatformResourceURI(file.getFullPath().toString(), true)).incrementAndGet();
	}

	private static AtomicLong revision(final URI uri) {
		IFile file = getFile(uri);
		if (file == null && !uri.isPlatformResource()) return new AtomicLong();
		URI key = file == null ? uri : URI.createPlatformResourceURI(file.getFullPath().toString(), true);
		return revisions.computeIfAbsent(key, ignored -> new AtomicLong());
	}

	/** Capture this validation's permission for asynchronous documentation and UI publication. */
	public static BooleanSupplier publicationGuard(final URI uri) {
		Request request = current.get();
		if (request != null) return request.valid;
		return guard(uri, () -> isEnabled(uri));
	}

	private static BooleanSupplier guard(final URI uri, final BooleanSupplier permission) {
		if (uri == null) return () -> true;
		AtomicLong revision = revision(uri);
		long version = revision.get();
		boolean allowed = permission.getAsBoolean();
		return () -> allowed && revision.get() == version;
	}

	public static Request begin(final URI uri, final boolean explicit) {
		return new Request(guard(uri, () -> explicit || isEnabled(uri)));
	}

	public static Request begin(final IFile file, final boolean explicit) {
		URI uri = URI.createPlatformResourceURI(file.getFullPath().toString(), true);
		return new Request(guard(uri, () -> explicit || isEnabled(file)));
	}

	public static final class Request implements AutoCloseable {
		private final Request previous;
		private final BooleanSupplier valid;

		private Request(final BooleanSupplier valid) {
			previous = current.get();
			this.valid = valid;
			current.set(this);
		}

		public boolean isValid() { return valid.getAsBoolean(); }

		@Override
		public void close() {
			if (previous == null) {
				current.remove();
			} else {
				current.set(previous);
			}
		}
	}
}
