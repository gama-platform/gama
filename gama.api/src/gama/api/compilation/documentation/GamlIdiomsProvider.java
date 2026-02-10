/*******************************************************************************************************
 *
 * GamlIdiomsProvider.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.api.compilation.documentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import gama.api.GAMA;
import gama.api.additions.registries.ArtefactProtoRegistry;
import gama.api.additions.registries.GamaSkillRegistry;
import gama.api.compilation.descriptions.IActionDescription;
import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.descriptions.ISkillDescription;
import gama.api.compilation.descriptions.ISpeciesDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.utils.StringUtils;
import gama.api.utils.files.IGamlFileInfo;

/**
 * The Class GamlIdiomsProvider.
 *
 * @param <T>
 *            the generic type
 */
public class GamlIdiomsProvider<T extends IGamlDescription> {

	/**
	 * Indicates that a String search operation yielded no results.
	 */
	public static final int NOT_FOUND = -1;

	/** The providers. */
	static List<GamlIdiomsProvider<?>> PROVIDERS;

	/**
	 * Gets the providers.
	 *
	 * @return the providers
	 */
	public static List<GamlIdiomsProvider<?>> getProviders() {
		if (PROVIDERS == null) {

			GamlIdiomsProvider<IGamlFileInfo> FILES = new GamlIdiomsProvider<>("models", "Models (title & tags)",
					GAMA.getGui().getModelsManager().getAllModels());

			GamlIdiomsProvider<ISpeciesDescription> SPECIES =
					new GamlIdiomsProvider<>("species", "Built-in species", Types.getBuiltInSpecies().values());

			GamlIdiomsProvider<IVariableDescription> SPECIES_ATTRIBUTES =
					new GamlIdiomsProvider<>("variables", "Built-in species attribute", Iterables.concat(Iterables
							.transform(Types.getBuiltInSpecies().values(), ISpeciesDescription::getOwnAttributes)));

			GamlIdiomsProvider<IActionDescription> SPECIES_ACTIONS =
					new GamlIdiomsProvider<>("actions", "Built-in species action", Iterables.concat(Iterables
							.transform(Types.getBuiltInSpecies().values(), ISpeciesDescription::getOwnActions)));

			GamlIdiomsProvider<ISkillDescription> SKILLS =
					new GamlIdiomsProvider<>("skills", "Skill", GamaSkillRegistry.INSTANCE.getRegisteredSkills());

			GamlIdiomsProvider<IVariableDescription> SKILLS_ATTRIBUTES = new GamlIdiomsProvider<>("variables",
					"Skill Attribute", GamaSkillRegistry.INSTANCE.getRegisteredSkillsAttributes());

			GamlIdiomsProvider<IActionDescription> SKILLS_ACTIONS = new GamlIdiomsProvider<>("actions", "Skill Action",
					GamaSkillRegistry.INSTANCE.getRegisteredSkillsActions());

			GamlIdiomsProvider<IArtefactProto> STATEMENTS =
					new GamlIdiomsProvider<>("statements", "Statements", ArtefactProtoRegistry.getStatementProtos());

			GamlIdiomsProvider<IExpression> CONSTANTS =
					new GamlIdiomsProvider<>("constant", "Constant & Units", GAML.getUnits().values());

			GamlIdiomsProvider<IArtefactProto> OPERATORS = new GamlIdiomsProvider<>("operators", "Operators",
					Iterables.concat(Iterables.transform(GAML.OPERATORS.values(), Map::values)));

			GamlIdiomsProvider<IType<?>> TYPES =
					new GamlIdiomsProvider<>("types", "Types", Types.builtInTypes.getAllTypes());

			GamlIdiomsProvider<IArtefactProto.Facet> FACETS =
					new GamlIdiomsProvider<>("facets", "Facets", ArtefactProtoRegistry.getFacetsProtos());

			GamlIdiomsProvider<IArtefactProto> FIELDS =
					new GamlIdiomsProvider<>("attributes", "Fields", Types.getAllFields());

			/** The Constant PROVIDERS. */
			PROVIDERS = new ArrayList<>(Arrays.asList(FILES, SPECIES, SPECIES_ATTRIBUTES, SPECIES_ACTIONS, SKILLS,
					SKILLS_ATTRIBUTES, SKILLS_ACTIONS, STATEMENTS, CONSTANTS, OPERATORS, TYPES, FACETS, FIELDS));
		}

		return PROVIDERS;
	}

	/** The search. */
	public final String name, search;

	/** The elements. */
	public final Iterable<? extends T> elements;

	/** The titles. */
	public final Map<T, String> titles;

	/** The sorted elements. */
	public IGamlDescription[] sortedElements;

	/** The by name. */
	public Multimap<String, ? extends T> byName;

	/** The documenter. */
	// default
	public Function<T, IGamlDocumentation> documenter = IGamlDescription::getDocumentation;

	/**
	 * Instantiates a new gaml idioms provider.
	 *
	 * @param search
	 *            the search
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @param elmts
	 *            the elmts
	 */
	public GamlIdiomsProvider(final String search, final String name, final Iterable<? extends T> elmts) {
		this(search, name, elmts, null);
	}

	/**
	 * Instantiates a new gaml idioms provider.
	 *
	 * @param search
	 *            the search
	 * @param id
	 *            the id
	 * @param name
	 *            the name
	 * @param elmts
	 *            the elmts
	 * @param titles
	 *            the titles
	 */
	public GamlIdiomsProvider(final String search, final String name, final Iterable<? extends T> elmts,
			final Map<T, String> titles) {
		this.search = search;
		this.name = name;
		this.elements = elmts;
		this.titles = titles;
	}

	/**
	 * Document.
	 *
	 * @param element
	 *            the element
	 * @return the string
	 */
	@SuppressWarnings ("unchecked")
	public String document(final IGamlDescription element) {
		return documenter.apply((T) element).toString();
	}

	/**
	 * Gets the search category.
	 *
	 * @return the search category
	 */
	public String getSearchCategory() { return search; }

	/**
	 * Gets the.
	 *
	 * @param name
	 *            the name
	 * @return the collection<? extends t>
	 */
	public Collection<? extends T> get(final String name) {
		if (byName == null) { init(); }
		return byName.get(name);
	}

	/**
	 * Gets the sorted elements.
	 *
	 * @return the sorted elements
	 */
	public IGamlDescription[] getSortedElements() {
		if (sortedElements == null) { init(); }
		return sortedElements;
	}

	/**
	 * Inits the.
	 */
	private void init() {

		sortedElements = Iterables.toArray(elements, IGamlDescription.class);
		if (titles == null) {
			Arrays.sort(sortedElements, Comparator.comparing(IGamlDescription::getTitle));
		} else {
			Arrays.sort(sortedElements, (e1, e2) -> titles.get(e1).compareTo(titles.get(e2)));
		}
		byName = Multimaps.index(elements, IGamlDescription::getName);

	}

	/**
	 * For name.
	 *
	 * @param name
	 *            the name
	 * @return the multimap
	 */
	public static Multimap<GamlIdiomsProvider<?>, IGamlDescription> forName(final String name) {
		final Multimap<GamlIdiomsProvider<?>, IGamlDescription> result = ArrayListMultimap.create();
		for (final GamlIdiomsProvider<?> p : PROVIDERS) { result.replaceValues(p, p.get(name)); }
		return result;
	}

	/**
	 * Gets the documentation on.
	 *
	 * @param query
	 *            the query
	 * @return the documentation on
	 */
	public static String getDocumentationOn(final String query) {
		final String keyword = org.apache.commons.lang3.StringUtils
				.removeEnd(org.apache.commons.lang3.StringUtils.removeStart(query.trim(), "#"), ":");
		final Multimap<GamlIdiomsProvider<?>, IGamlDescription> results = forName(keyword);
		if (results.isEmpty()) return "No result found";
		final StringBuilder sb = new StringBuilder();
		final int max = results.keySet().stream().mapToInt(each -> each.name.length()).max().getAsInt();
		final String separator = org.apache.commons.lang3.StringUtils.repeat("—", max + 6).concat(StringUtils.LN);
		results.asMap().forEach((provider, list) -> {
			sb.append("").append(separator).append("|| ");
			sb.append(org.apache.commons.lang3.StringUtils.rightPad(provider.name, max));
			sb.append(" ||").append(StringUtils.LN).append(separator);
			for (final IGamlDescription d : list) {
				sb.append("== ").append(toText(d.getTitle())).append(StringUtils.LN)
						.append(toText(provider.document(d))).append(StringUtils.LN);
			}
		});

		return sb.toString();

		//
	}

	/** The html tags. */
	private static String[] HTML_TAGS =
			{ "<br/>", "<br>", "<b>", "</b>", "<i>", "</i>", "<ul>", "</ul>", "<li>", "</li>" };

	/** The replacements. */
	private static String[] REPLACEMENTS =
			{ StringUtils.LN, StringUtils.LN, "", "", "", "", "", "", StringUtils.LN + "- ", "" };

	/**
	 * To text.
	 *
	 * @param s
	 *            the s
	 * @return the string
	 */
	public static String toText(final String s) {
		if (s == null) return "";
		return breakStringToLines(org.apache.commons.lang3.StringUtils.replaceEach(s, HTML_TAGS, REPLACEMENTS), 120,
				StringUtils.LN);
	}

	/**
	 * Version of lastIndexOf that uses regular expressions for searching. By Tomer Godinger.
	 *
	 * @param str
	 *            String in which to search for the pattern.
	 * @param toFind
	 *            Pattern to locate.
	 * @return The index of the requested pattern, if found; NOT_FOUND (-1) otherwise.
	 */
	public static int lastIndexOfRegex(final String str, final String toFind) {
		final Pattern pattern = Pattern.compile(toFind);
		final Matcher matcher = pattern.matcher(str);

		// Default to the NOT_FOUND constant
		int lastIndex = NOT_FOUND;

		// Search for the given pattern
		while (matcher.find()) { lastIndex = matcher.start(); }

		return lastIndex;
	}

	/**
	 * Finds the last index of the given regular expression pattern in the given string, starting from the given index
	 * (and conceptually going backwards). By Tomer Godinger.
	 *
	 * @param str
	 *            String in which to search for the pattern.
	 * @param toFind
	 *            Pattern to locate.
	 * @param fromIndex
	 *            Maximum allowed index.
	 * @return The index of the requested pattern, if found; NOT_FOUND (-1) otherwise.
	 */
	public static int lastIndexOfRegex(final String str, final String toFind, final int fromIndex) {
		// Limit the search by searching on a suitable substring
		return lastIndexOfRegex(str.substring(0, fromIndex), toFind);
	}

	/**
	 * Breaks the given string into lines as best possible, each of which no longer than <code>maxLength</code>
	 * characters. By Tomer Godinger.
	 *
	 * @param str
	 *            The string to break into lines.
	 * @param maxLength
	 *            Maximum length of each line.
	 * @param newLineString
	 *            The string to use for line breaking.
	 * @return The resulting multi-line string.
	 */
	public static String breakStringToLines(final String s, final int maxLength, final String newLineString) {
		String str = s;
		final StringBuilder result = new StringBuilder();
		while (str.length() > maxLength) {
			// Attempt to break on whitespace first,
			int breakingIndex = lastIndexOfRegex(str, "\\s", maxLength);

			// Then on other non-alphanumeric characters,
			if (breakingIndex == NOT_FOUND) { breakingIndex = lastIndexOfRegex(str, "[^a-zA-Z0-9]", maxLength); }

			// And if all else fails, break in the middle of the word
			if (breakingIndex == NOT_FOUND) { breakingIndex = maxLength; }

			// Append each prepared line to the builder
			result.append(str.substring(0, breakingIndex + 1));
			result.append(newLineString);

			// And start the next line
			str = str.substring(breakingIndex + 1);
		}

		// Check if there are any residual characters left
		if (str.length() > 0) { result.append(str); }

		// Return the resulting string
		return result.toString();
	}

}
