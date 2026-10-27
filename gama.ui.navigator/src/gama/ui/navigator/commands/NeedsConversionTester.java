/*******************************************************************************************************
 *
 * NeedsConversionTester.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.commands;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;

import gaml.compiler.transition.GamlFileProcessor;

/**
 * Tells whether a resource contains (or is) a GAML file that the GAMA 2026 conversion would modify, i.e. a file
 * suspected to use the old syntax. Results are cached per file and invalidated when the file is modified.
 */
public class NeedsConversionTester extends PropertyTester {

	private record Entry(long lastModified, boolean result) {}

	private static final Map<Path, Entry> CACHE = new ConcurrentHashMap<>();
	private static final GamlFileProcessor PROCESSOR = new GamlFileProcessor();

	@Override
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expected) {
		if (!(receiver instanceof final IResource r) || r.getLocation() == null) return false;
		final Path p = r.getLocation().toFile().toPath();
		if (r.getType() == IResource.FILE) return check(p);
		try {
			return PROCESSOR.hasFilesToConvert(p, NeedsConversionTester::check);
		} catch (final IOException e) {
			return false;
		}
	}

	private static boolean check(final Path file) {
		final long modified = file.toFile().lastModified();
		final Entry e = CACHE.get(file);
		if (e != null && e.lastModified() == modified) return e.result();
		final boolean result = PROCESSOR.needsConversion(file);
		CACHE.put(file, new Entry(modified, result));
		return result;
	}
}
