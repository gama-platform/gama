/*******************************************************************************************************
 *
 * TestSelection.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.tests;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.emf.common.util.URI;

import gama.api.GAMA;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.kernel.species.IExperimentSpecies;
import gama.api.kernel.species.IModelSpecies;
import gama.api.runtime.scope.IScope;

/**
 * Identifies a single {@code test} declaration by the absolute location of its file and its EMF fragment, so that it
 * can be matched against a freshly compiled model (in this JVM or in an isolated worker) without relying on names.
 */
public record TestSelection(String location, String fragment) {

	private static final Map<IExperimentSpecies, TestSelection> ACTIVE =
			Collections.synchronizedMap(new IdentityHashMap<>());

	public TestSelection {
		Objects.requireNonNull(location, "location");
		Objects.requireNonNull(fragment, "fragment");
		if (location.isEmpty() || fragment.isEmpty()) throw new IllegalArgumentException("Incomplete test selection");
	}

	/** Builds a selection from the URI of a test declaration (including its fragment). */
	public static TestSelection of(final URI declaration) {
		if (declaration == null || !declaration.hasFragment())
			throw new IllegalArgumentException("Not a test declaration: " + declaration);
		return new TestSelection(location(declaration), declaration.fragment());
	}

	/** Absolute, normalized location of the resource designated by the URI (fragment ignored). */
	public static String location(final URI uri) {
		final URI resource = uri.trimFragment();
		String path = null;
		if (resource.isPlatformResource()) {
			final var file = GAMA.getWorkspaceManager().getRoot()
					.getFile(new org.eclipse.core.runtime.Path(resource.toPlatformString(true)));
			if (file.getLocation() != null) { path = file.getLocation().toOSString(); }
		} else if (resource.isFile()) { path = resource.toFileString(); }
		if (path == null) return resource.toString();
		final Path absolute = Path.of(path).toAbsolutePath().normalize();
		try {
			return absolute.toRealPath().toString();
		} catch (final IOException e) {
			return absolute.toString();
		}
	}

	public boolean matches(final URI uri) {
		return uri != null && fragment.equals(uri.fragment()) && location.equals(location(uri));
	}

	public boolean matches(final WithTestSummary<?> test) {
		return test != null && matches(test.getURI());
	}

	/** Tests that the experiment would run: those of the model's species plus those of the experiment itself. */
	public static List<WithTestSummary<?>> testsOf(final IModelSpecies model, final String experiment) {
		final Set<WithTestSummary<?>> result = Collections.newSetFromMap(new IdentityHashMap<>());
		final List<WithTestSummary<?>> ordered = new ArrayList<>();
		model.getAllTests().forEach(t -> { if (result.add(t)) { ordered.add(t); } });
		final IExperimentSpecies plan = model.getExperiment(experiment);
		if (plan != null) {
			plan.getBehaviors().forEach(s -> {
				if (s instanceof WithTestSummary<?> t && result.add(t)) { ordered.add(t); }
			});
		}
		return ordered;
	}

	/** Whether exactly one test run by this experiment corresponds to this selection. */
	public boolean isRunBy(final IModelSpecies model, final String experiment) {
		final IExperimentSpecies plan = model.getExperiment(experiment);
		return plan != null && plan.isTest() && testsOf(model, experiment).stream().filter(this::matches).count() == 1;
	}

	/** Fails explicitly rather than silently running every test when the selection cannot be resolved. */
	public void validate(final IModelSpecies model, final String experiment) {
		final long count = testsOf(model, experiment).stream().filter(this::matches).count();
		if (count == 0)
			throw new IllegalArgumentException("The selected test is not run by experiment " + experiment);
		if (count > 1) throw new IllegalArgumentException("The selected test is ambiguous in experiment " + experiment);
	}

	/** Names of the test experiments of the model that run this test. */
	public List<String> experimentsIn(final IModelSpecies model) {
		final Set<String> names = new LinkedHashSet<>();
		for (final String name : model.getDescription().getExperimentNames()) {
			if (isRunBy(model, name)) { names.add(name); }
		}
		return new ArrayList<>(names);
	}

	/** Restricts the next execution of this experiment to the selection; {@code null} restores running all tests. */
	public static void activate(final IExperimentSpecies experiment, final TestSelection selection) {
		if (experiment == null) return;
		if (selection == null) {
			ACTIVE.remove(experiment);
		} else {
			ACTIVE.put(experiment, selection);
		}
	}

	public static TestSelection forExperiment(final IExperimentSpecies experiment) {
		if (experiment == null || ACTIVE.isEmpty()) return null;
		return ACTIVE.get(experiment);
	}

	/** The selection restricting the experiment running in this scope, or null if all its tests run. */
	public static TestSelection active(final IScope scope) {
		if (ACTIVE.isEmpty() || scope == null) return null;
		final IExperimentAgent agent = scope.getExperiment();
		return agent == null ? null : forExperiment(agent.getSpecies());
	}

}
