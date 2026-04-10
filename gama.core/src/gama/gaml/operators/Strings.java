/*******************************************************************************************************
 *
 * Strings.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.operators;

import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.test;
import gama.annotations.usage;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.Cast;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;
import gama.api.utils.StringUtils;
import gama.api.utils.files.CompressionUtils;

/**
 * {@code Strings} provides GAML language operators for string manipulation, organized into the following operator
 * families:
 *
 * <ul>
 * <li><b>Concatenation:</b> {@code +} (binary), {@code concatenate} (list &rarr; string, with optional separator)</li>
 * <li><b>Substring / access:</b> {@code copy_between}, {@code at} ({@code @}), {@code reverse}, {@code first},
 * {@code last}</li>
 * <li><b>Search:</b> {@code in}, {@code contains}, {@code contains_any}, {@code contains_all}, {@code index_of},
 * {@code last_index_of}, {@code starts_with}, {@code ends_with}</li>
 * <li><b>Case conversion:</b> {@code upper_case}, {@code lower_case}, {@code capitalize}</li>
 * <li><b>Splitting:</b> {@code split_with} / {@code tokenize}, {@code tokenize_regex}</li>
 * <li><b>Replacement:</b> {@code replace}, {@code replace_regex}</li>
 * <li><b>Information:</b> {@code length}, {@code empty}, {@code is_number}, {@code char}</li>
 * <li><b>Compression:</b> {@code compress} / {@code zip}, {@code uncompress} / {@code unzip}</li>
 * </ul>
 *
 * <h3>Index convention</h3>
 * <p>
 * All character and substring indices are <b>0-based</b>, consistent with Java's {@link String} API. For example,
 * {@code 'abc' at 0} returns {@code 'a'}.
 * </p>
 *
 * <h3>Unicode awareness</h3>
 * <p>
 * All string comparisons and substring operations delegate to Java's {@link String} internals and are therefore
 * Unicode-aware (UTF-16 code units).
 * </p>
 *
 * <h3>Empty string</h3>
 * <p>
 * The empty string {@code ''} is a valid operand in all operators unless otherwise stated in the individual operator
 * documentation.
 * </p>
 *
 * @author Alexis Drogoul (original), GAMA development team
 * @see gama.api.utils.StringUtils
 * @see gama.gaml.operators.Cast
 */

/**
 * The Class Strings.
 */
@SuppressWarnings ({ "rawtypes" })
public class Strings {

	/**
	 * Op plus.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the string
	 */

	/**
	 * Op plus.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the string
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING },
			doc = @doc ("Concatenates the two string operands"))
	@doc (
			usages = @usage (
					value = "if the left-hand and right-hand operand are a string, returns the concatenation of the two operands",
					examples = @example (
							value = "\"hello \" + \"World\"",
							equals = "\"hello World\"")))

	@test ("'a'+'b'='ab'")
	@test ("''+'' = ''")
	@test ("string a <- 'a'; a + '' = a")
	@test ("'hello' + '' = 'hello'")
	@test ("'' + 'world' = 'world'")
	public static String opPlus(final String a, final String b) {
		return a + b;
	}

	/**
	 * Op plus.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the string
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Op plus.
	 *
	 * @param scope
	 *            the scope
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the string
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = IKeyword.PLUS,
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Concatenates the string with the string representation of any GAML object",
			returns = "a {@code string} concatenation",
			special_cases = { "If the right operand is nil, it is converted to the string 'nil'." },
			usages = @usage (
					value = "if the left-hand operand is a string, returns the concatenation of the two operands (the left-hand one beind casted into a string)",
					examples = @example (
							value = "\"hello \" + 12",
							equals = "\"hello 12\"")))
	@test ("'a' + 1 = 'a1'")
	@test ("'a' + 1.5 = 'a1.5'")
	@test ("'a' + true = 'atrue'")
	public static String opPlus(final IScope scope, final String a, final Object b) throws GamaRuntimeException {
		return a + Cast.asString(scope, b);
	}

	/**
	 * Op concatenate.
	 *
	 * @param scope
	 *            the scope
	 * @param strings
	 *            the strings
	 * @return the string
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Op concatenate.
	 *
	 * @param scope
	 *            the scope
	 * @param strings
	 *            the strings
	 * @return the string
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "concatenate",
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			masterDoc = true,
			returns = "a {@code string} that is the concatenation of all elements in order.",
			special_cases = { "If the list is empty, returns the empty string ''.",
					"nil elements are converted to 'nil'." },
			usages = @usage (
					value = "concatenates a list of string into a string. More efficient than looping over the list and adding the strings individually",
					examples = @example (
							value = "concatenate(['a','bc'])",
							equals = "'abc'")))
	@test ("concatenate([]) = ''")
	@test ("concatenate(['a']) = 'a'")
	@test ("concatenate(['a','b','c']) = 'abc'")
	public static String opConcatenate(final IScope scope, final IList<String> strings) throws GamaRuntimeException {
		StringBuilder sb = new StringBuilder();
		for (String s : strings) { sb.append(s); }
		return sb.toString();
	}

	/**
	 * Op concatenate sep.
	 *
	 * @param scope
	 *            the scope
	 * @param strings
	 *            the strings
	 * @param separator
	 *            the separator
	 * @return the string
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */

	/**
	 * Op concatenate sep.
	 *
	 * @param scope
	 *            the scope
	 * @param strings
	 *            the strings
	 * @param separator
	 *            the separator
	 * @return the string
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@operator (
			value = "concatenate",
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			returns = "a {@code string} with all elements joined by the separator.",
			special_cases = { "If the list is empty, returns the empty string ''.",
					"If the separator is empty, equivalent to concatenate/1." },
			usages = @usage (
					value = "concatenates a list of string into a string, inserting the separator between each. More efficient than looping over the list and adding the strings individually",
					examples = @example (
							value = "concatenate(['a','bc', 'cd'], '--')",
							equals = "'a--bc--cd'")))
	@test ("concatenate([], '--') = ''")
	@test ("concatenate(['a'], '--') = 'a'")
	@test ("concatenate(['a','b'], '') = 'ab'")
	public static String opConcatenateSep(final IScope scope, final IList<String> strings, final String separator)
			throws GamaRuntimeException {
		StringJoiner sj = new StringJoiner(separator);
		for (String s : strings) { sj.add(s); }
		return sj.toString();
	}

	/**
	 * Op in.
	 *
	 * @param pattern
	 *            the pattern
	 * @param target
	 *            the target
	 * @return the boolean
	 */

	/**
	 * Op in.
	 *
	 * @param pattern
	 *            the pattern
	 * @param target
	 *            the target
	 * @return the boolean
	 */
	@operator (
			value = "in",
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Returns true if the left-hand string is contained in the right-hand string.",
			returns = "a {@code bool}.",
			special_cases = {
					"If the left operand is the empty string, returns true (the empty string is contained in any string).",
					"The test is case-sensitive." },
			usages =

			@usage (
					value = "if both operands are strings, returns true if the left-hand operand patterns is included in to the right-hand string;"),
			examples = @example (
					value = " 'bc' in 'abcded'",
					equals = "true"))
	@test ("'bc' in 'abcd'")
	@test ("'' in 'abc'")
	@test ("'abc' in 'abc'")
	@test ("!('BC' in 'abcd')")
	@test ("!('xyz' in 'abcd')")
	public static Boolean opIn(final String pattern, final String target) {
		return target.contains(pattern);
	}

	/**
	 * Op contains.
	 *
	 * @param target
	 *            the target
	 * @param pattern
	 *            the pattern
	 * @return the boolean
	 */

	/**
	 * Op contains.
	 *
	 * @param target
	 *            the target
	 * @param pattern
	 *            the pattern
	 * @return the boolean
	 */
	@operator (
			value = "contains",
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Returns true if the left-hand string contains the right-hand string.",
			returns = "a {@code bool}.",
			special_cases = { "If the right operand is the empty string, returns true." },
			usages = @usage (
					value = "if both operands are strings, returns true if the right-hand operand contains the right-hand pattern;"),
			examples = @example (
					value = "'abcded' contains 'bc'",
					equals = "true"))
	@test ("'abcd' contains 'bc'")
	@test ("'abcd' contains ''")
	@test ("!('abcd' contains 'xyz')")
	public static Boolean opContains(final String target, final String pattern) {
		return opIn(pattern, target);
	}

	/**
	 * Op contains any.
	 *
	 * @param target
	 *            the target
	 * @param l
	 *            the l
	 * @return the boolean
	 */

	/**
	 * Op contains any.
	 *
	 * @param target
	 *            the target
	 * @param l
	 *            the l
	 * @return the boolean
	 */
	@operator (
			value = "contains_any",
			can_be_const = true,
			expected_content_type = { IType.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Returns true if the string contains at least one element of the list.",
			returns = "a {@code bool}.",
			special_cases = { "If the list is empty, returns false.",
					"Non-string elements in the list are ignored." },
			examples = @example (
					value = "\"abcabcabc\" contains_any [\"ca\",\"xy\"]",
					equals = "true"))
	@test ("'abc' contains_any ['a', 'z']")
	@test ("!('abc' contains_any ['x','y','z'])")
	@test ("!('abc' contains_any [])")
	public static Boolean opContainsAny(final String target, final IList l) {
		for (final Object o : l) { if (o instanceof String && opContains(target, (String) o)) return true; }
		return false;
	}

	/**
	 * Op contains all.
	 *
	 * @param target
	 *            the target
	 * @param l
	 *            the l
	 * @return the boolean
	 */

	/**
	 * Op contains all.
	 *
	 * @param target
	 *            the target
	 * @param l
	 *            the l
	 * @return the boolean
	 */
	@operator (
			value = "contains_all",
			can_be_const = true,
			expected_content_type = { IType.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Returns true if the string contains all the elements of the list.",
			returns = "a {@code bool}.",
			special_cases = { "If the list is empty, returns true (vacuously true).",
					"Non-string elements in the list cause the method to return false." },
			usages = @usage (
					value = "if the left-operand is a string, test whether the string contains all the element of the list;",
					examples = @example (
							value = "\"abcabcabc\" contains_all [\"ca\",\"xy\"]",
							equals = "false")))
	@test ("'abcabcabc' contains_all ['ab', 'bc']")
	@test ("!('abc' contains_all ['ab','xyz'])")
	@test ("'abc' contains_all []")
	public static Boolean opContainsAll(final String target, final IList l) {
		for (final Object o : l) { if (!(o instanceof String) || !opContains(target, (String) o)) return false; }
		return true;
	}

	/**
	 * Op index of.
	 *
	 * @param target
	 *            the target
	 * @param pattern
	 *            the pattern
	 * @return the integer
	 */

	/**
	 * Op index of.
	 *
	 * @param target
	 *            the target
	 * @param pattern
	 *            the pattern
	 * @return the integer
	 */
	@operator (
			value = "index_of",
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Returns the index of the first occurrence of the right-hand string in the left-hand string. The index is 0-based.",
			returns = "an {@code int} &ge; 0 if found, or -1 if not found.",
			special_cases = { "If the pattern is not found, returns -1.",
					"If the pattern is the empty string, returns 0." },
			usages = @usage (
					value = "if both operands are strings, returns the index within the left-hand string of the first occurrence of the given right-hand string",
					examples = @example (
							value = "\"abcabcabc\" index_of \"ca\"",
							equals = "2")))
	@test ("'abcabcabc' index_of 'ca' = 2")
	@test ("'abcabcabc' index_of 'x' = -1")
	@test ("'abcabcabc' index_of '' = 0")
	public static Integer opIndexOf(final String target, final String pattern) {
		return target.indexOf(pattern);
	}

	/**
	 * Op last index of.
	 *
	 * @param target
	 *            the target
	 * @param pattern
	 *            the pattern
	 * @return the integer
	 */

	/**
	 * Op last index of.
	 *
	 * @param target
	 *            the target
	 * @param pattern
	 *            the pattern
	 * @return the integer
	 */
	@operator (
			value = "last_index_of",
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			usages = @usage (
					value = "if both operands are strings, returns the index within the left-hand string of the rightmost occurrence of the given right-hand string",
					examples = @example (
							value = "\"abcabcabc\" last_index_of \"ca\"",
							equals = "5")))
	@test ("'abcabcabc' last_index_of 'x' = -1")
	@test ("'abcabcabc' last_index_of 'ca' = 5")
	public static Integer opLastIndexOf(final String target, final String pattern) {
		return target.lastIndexOf(pattern);
	}

	/**
	 * Op copy.
	 *
	 * @param target
	 *            the target
	 * @param beginIndex
	 *            the begin index
	 * @param endIndex
	 *            the end index
	 * @return the string
	 */

	/**
	 * Op copy.
	 *
	 * @param target
	 *            the target
	 * @param beginIndex
	 *            the begin index
	 * @param endIndex
	 *            the end index
	 * @return the string
	 */
	@operator (
			value = { "copy_between" /* , "copy" */ },
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			examples = @example (
					value = "copy_between(\"abcabcabc\", 2,6)",
					equals = "\"cabc\""))
	public static String opCopy(final String target, final Integer beginIndex, final Integer endIndex) {
		final int bIndex = beginIndex < 0 ? 0 : beginIndex;
		final int eIndex = endIndex > target.length() ? target.length() : endIndex;
		if (bIndex >= eIndex) return "";
		return target.substring(bIndex, eIndex);
	}

	/**
	 * Op tokenize.
	 *
	 * @param scope
	 *            the scope
	 * @param target
	 *            the target
	 * @param pattern
	 *            the pattern
	 * @return the i list
	 */

	/**
	 * Op tokenize.
	 *
	 * @param scope
	 *            the scope
	 * @param target
	 *            the target
	 * @param pattern
	 *            the pattern
	 * @return the i list
	 */
	@operator (
			value = { "split_with", "tokenize" },
			content_type = IType.STRING,
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Returns a list containing the sub-strings (tokens) of the left-hand operand delimited by each of the characters of the right-hand operand.",
			masterDoc = true,
			comment = "Delimiters themselves are excluded from the resulting list.",
			examples = @example (
					value = "'to be or not to be,that is the question' split_with ' ,'",
					equals = "['to','be','or','not','to','be','that','is','the','question']"))
	@test ("split_with('a,b,c', ',') = ['a','b','c']")
	@test ("split_with('', ',') = []")
	public static IList opTokenize(final IScope scope, final String target, final String pattern) {
		return opTokenize(scope, target, pattern, false);
	}

	/**
	 * Op tokenize.
	 *
	 * @param scope
	 *            the scope
	 * @param target
	 *            the target
	 * @param pattern
	 *            the pattern
	 * @param completeSep
	 *            the complete sep
	 * @return the i list
	 */

	/**
	 * Op tokenize.
	 *
	 * @param scope
	 *            the scope
	 * @param target
	 *            the target
	 * @param pattern
	 *            the pattern
	 * @param completeSep
	 *            the complete sep
	 * @return the i list
	 */
	@operator (
			value = { "split_with", "tokenize" },
			content_type = IType.STRING,
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Returns a list containing the sub-strings (tokens) of the left-hand operand delimited either by each of the characters of the right-hand operand (false) or by the whole right-hand operand (true).",
			usages = @usage (
					value = "when used  with an  additional boolean operand, it returns a list containing the sub-strings (tokens) of the left-hand operand delimited either by each of the characters of the right-hand operand (false) or by the whole right-hand operand (true)."),
			examples = { @example (
					value = "'aa::bb:cc' split_with ('::', true)",
					equals = "['aa','bb:cc']"),
					@example (
							value = "'aa::bb:cc' split_with ('::', false)",
							equals = "['aa','bb','cc']") })
	public static IList opTokenize(final IScope scope, final String target, final String pattern,
			final Boolean completeSep) {
		if (completeSep) return GamaListFactory.create(scope, Types.STRING, target.split(pattern));
		final StringTokenizer st = new StringTokenizer(target, pattern);
		return GamaListFactory.create(scope, Types.STRING, st);
	}

	/**
	 * Op replace.
	 *
	 * @param target
	 *            the target
	 * @param pattern
	 *            the pattern
	 * @param replacement
	 *            the replacement
	 * @return the string
	 */

	/**
	 * Op replace.
	 *
	 * @param target
	 *            the target
	 * @param pattern
	 *            the pattern
	 * @param replacement
	 *            the replacement
	 * @return the string
	 */
	@operator (
			value = { "replace" },
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Returns the string obtained by replacing by the third operand, in the first operand, all the sub-strings equal to the second operand",
			examples = @example (
					value = "replace('to be or not to be,that is the question','to', 'do')",
					equals = "'do be or not do be,that is the question'"),
			see = { "replace_regex" })
	@test ("replace('hello world', 'world', 'GAMA') = 'hello GAMA'")
	@test ("replace('', 'x', 'y') = ''")
	public static String opReplace(final String target, final String pattern, final String replacement) {
		return target.replace(pattern, replacement);
	}

	/**
	 * Op replace regex.
	 *
	 * @param target
	 *            the target
	 * @param pattern
	 *            the pattern
	 * @param replacement
	 *            the replacement
	 * @return the string
	 */

	/**
	 * Op replace regex.
	 *
	 * @param target
	 *            the target
	 * @param pattern
	 *            the pattern
	 * @param replacement
	 *            the replacement
	 * @return the string
	 */
	@operator (
			value = { "replace_regex" },
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Returns the string obtained by replacing by the third operand, in the first operand, all the sub-strings that match the regular expression of the second operand",
			examples = @example (
					value = "replace_regex(\"colour, color\", \"colou?r\", \"col\")",
					equals = "'col, col'"),
			see = { "replace" })
	public static String opReplaceRegex(final String target, final String pattern, final String replacement) {
		// DEBUG.OUT("String pattern = " + pattern);
		return target.replaceAll(pattern, replacement);
	}

	/**
	 * Op regex matches.
	 *
	 * @param target
	 *            the target
	 * @param pattern
	 *            the pattern
	 * @return the i list
	 */

	/**
	 * Op regex matches.
	 *
	 * @param target
	 *            the target
	 * @param pattern
	 *            the pattern
	 * @return the i list
	 */
	@operator (
			value = { "regex_matches" },
			can_be_const = true,
			content_type = IType.STRING,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Returns the list of sub-strings of the first operand that match the regular expression provided in the second operand",
			examples = @example (
					value = "regex_matches(\"colour, color\", \"colou?r\")",
					equals = "['colour','color']"),
			see = { "replace_regex" })
	public static IList<String> opRegexMatches(final String target, final String pattern) {
		if (pattern == null || pattern.isEmpty()) return GamaListFactory.create();
		Pattern p;
		try {
			p = Pattern.compile(pattern);
		} catch (PatternSyntaxException e) {
			return target.contains(pattern) ? GamaListFactory.createWithoutCasting(Types.STRING, pattern)
					: GamaListFactory.create();
		}
		return GamaListFactory.wrap(Types.STRING, p.matcher(target).results().map(MatchResult::group).toList());
	}

	/**
	 * Checks if is gama number.
	 *
	 * @param s
	 *            the s
	 * @return the boolean
	 */

	/**
	 * Checks if is gama number.
	 *
	 * @param s
	 *            the s
	 * @return the boolean
	 */
	@operator (
			value = "is_number",
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "tests whether the operand represents a numerical value",
			comment = "Note that the symbol . should be used for a float value (a string with , will not be considered as a numeric value). "
					+ "Symbols e and E are also accepted. A hexadecimal value should begin with #.",
			examples = { @example (
					value = "is_number(\"test\")",
					equals = "false"),
					@example (
							value = "is_number(\"123.56\")",
							equals = "true"),
					@example (
							value = "is_number(\"-1.2e5\")",
							equals = "true"),
					@example (
							value = "is_number(\"1,2\")",
							equals = "false"),
					@example (
							value = "is_number(\"#12FA\")",
							equals = "true") })
	@test ("is_number('42')")
	@test ("is_number('3.14')")
	@test ("!is_number('abc')")
	@test ("!is_number('')")
	public static Boolean isGamaNumber(final String s) {
		return StringUtils.isGamaNumber(s);
	}

	/**
	 * Reverse.
	 *
	 * @param s
	 *            the s
	 * @return the string
	 */

	/**
	 * Reverse.
	 *
	 * @param s
	 *            the s
	 * @return the string
	 */
	@operator (
			value = "reverse",
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			usages = @usage (
					value = "if it is a string, reverse returns a new string with characters in the reversed order",
					examples = @example (
							value = "reverse ('abcd')",
							equals = "'dcba'")))
	@test ("reverse('') = ''")
	@test ("reverse('abc') = 'cba'")
	@test ("reverse('a') = 'a'")
	static public String reverse(final String s) {
		final StringBuilder buf = new StringBuilder(s);
		buf.reverse();
		return buf.toString();
	}

	/**
	 * Checks if is empty.
	 *
	 * @param s
	 *            the s
	 * @return the boolean
	 */

	/**
	 * Checks if is empty.
	 *
	 * @param s
	 *            the s
	 * @return the boolean
	 */
	@operator (
			value = "empty",
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			usages = @usage (
					value = "if it is a string, empty returns true if the string does not contain any character, and false otherwise",
					examples = @example (
							value = "empty ('abced')",
							equals = "false")))
	static public Boolean isEmpty(final String s) {
		return s != null && s.isEmpty();
	}

	/**
	 * First.
	 *
	 * @param s
	 *            the s
	 * @return the string
	 */

	/**
	 * First.
	 *
	 * @param s
	 *            the s
	 * @return the string
	 */
	@operator (
			value = "first",
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			usages = @usage (
					value = "if it is a string, first returns a string composed of its first character",
					examples = @example (
							value = "first ('abce')",
							equals = "'a'")))
	static public String first(final String s) {
		if (s == null || s.isEmpty()) return "";
		return String.valueOf(s.charAt(0));
	}

	/**
	 * Last.
	 *
	 * @param s
	 *            the s
	 * @return the string
	 */

	/**
	 * Last.
	 *
	 * @param s
	 *            the s
	 * @return the string
	 */
	@operator (
			value = "last",
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			usages = @usage (
					value = "if it is a string, last returns a string composed of its last character, or an empty string if the operand is empty",
					examples = @example (
							value = "last ('abce')",
							equals = "'e'")))
	static public String last(final String s) {
		if (s == null || s.isEmpty()) return "";
		return String.valueOf(s.charAt(s.length() - 1));
	}

	/**
	 * Length.
	 *
	 * @param s
	 *            the s
	 * @return the integer
	 */

	/**
	 * Length.
	 *
	 * @param s
	 *            the s
	 * @return the integer
	 */
	@operator (
			value = "length",
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			usages = @usage (
					value = "if it is a string, length returns the number of characters",
					examples = @example (
							value = "length (\"I am an agent\")",
							equals = "13")))
	@test ("length('') = 0")
	@test ("length('abc') = 3")
	static public Integer length(final String s) {
		if (s == null) return 0;
		return s.length();
	}

	/**
	 * Gets the.
	 *
	 * @param lv
	 *            the lv
	 * @param rv
	 *            the rv
	 * @return the string
	 */

	/**
	 * Gets the.
	 *
	 * @param lv
	 *            the lv
	 * @param rv
	 *            the rv
	 * @return the string
	 */
	@operator (
			value = { IKeyword.AT, "@" },
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			examples = @example (
					value = "'abcdef' at 0",
					equals = "'a'"))
	public static String get(final String lv, final int rv) {
		return rv < lv.length() && rv >= 0 ? lv.substring(rv, rv + 1) : "";
	}

	/**
	 * As char.
	 *
	 * @param s
	 *            the s
	 * @return the string
	 */

	/**
	 * As char.
	 *
	 * @param s
	 *            the s
	 * @return the string
	 */
	@operator (
			value = "char",
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			usages = @usage (
					value = "converts ACSII integer value to character",
					examples = @example (
							value = "char (34)",
							equals = "'\"'")))
	static public String asChar(final Integer s) {
		if (s == null) return "";
		return Character.toString((char) s.byteValue());
	}

	/**
	 * Indent.
	 *
	 * @param s
	 *            the s
	 * @param nb
	 *            the nb
	 * @return the string
	 */

	/**
	 * Indent.
	 *
	 * @param s
	 *            the s
	 * @param nb
	 *            the nb
	 * @return the string
	 */
	@operator (
			value = "indented_by",
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Converts a (possibly multiline) string by indenting it by a number -- specified by the second operand -- of tabulations to the right",
			examples = @example (
					value = "\"my\" + indented_by(\"text\", 1)",
					equals = "\"my	text\""))
	static public String indent(final String s, final int nb) {
		if (nb <= 0) return s;
		final StringBuilder sb = new StringBuilder(nb);
		for (int i = 0; i < nb; i++) { sb.append(StringUtils.TAB); }
		final String t = sb.toString();
		return s.replaceAll("(?m)^", t);
	}

	/**
	 * To lower case.
	 *
	 * @param s
	 *            the s
	 * @return the string
	 */

	/**
	 * To lower case.
	 *
	 * @param s
	 *            the s
	 * @return the string
	 */
	@operator (
			value = "lower_case",
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Converts all of the characters in the string operand to lower case",
			examples = @example (
					value = "lower_case(\"Abc\")",
					equals = "'abc'"),
			see = { "upper_case" })
	@test ("lower_case('') = ''")
	@test ("lower_case('HELLO') = 'hello'")
	static public String toLowerCase(final String s) {
		if (s == null) return s;
		return s.toLowerCase();
	}

	/**
	 * To upper case.
	 *
	 * @param s
	 *            the s
	 * @return the string
	 */

	/**
	 * To upper case.
	 *
	 * @param s
	 *            the s
	 * @return the string
	 */
	@operator (
			value = "upper_case",
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Converts all of the characters in the string operand to upper case",
			examples = @example (
					value = "upper_case(\"Abc\")",
					equals = "'ABC'"),
			see = { "lower_case" })
	@test ("upper_case('') = ''")
	@test ("upper_case('hello') = 'HELLO'")
	static public String toUpperCase(final String s) {
		if (s == null) return s;
		return s.toUpperCase();
	}

	/**
	 * Capitalize.
	 *
	 * @param str
	 *            the str
	 * @return the string
	 */

	/**
	 * Capitalize.
	 *
	 * @param scope
	 *            the scope
	 * @param str
	 *            the str
	 * @return the string
	 */
	@operator (
			value = "capitalize",
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Returns a string where the first letter is capitalized",
			examples = @example (
					value = "capitalize(\"abc\")",
					equals = "'Abc'"),
			see = { "lower_case", "upper_case" })
	public static String capitalize(final IScope scope, final String str) {
		if (str == null) throw GamaRuntimeException.error("String cannot be null", scope);
		if (str.isEmpty()) return str;
		return str.substring(0, 1).toUpperCase().concat(str.substring(1));
	}

	/**
	 * Zip.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param str
	 *            the str
	 * @return the string
	 * @date 28 oct. 2023
	 */

	/**
	 * Zip.
	 *
	 * @param scope
	 *            the scope
	 * @param str
	 *            the str
	 * @return the string
	 */
	@operator (
			value = { "compress", "zip" },
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Returns a string that represents the compressed form (using gzip) of the argument",
			see = { "uncompress" })
	@no_test
	public static String zip(final IScope scope, final String str) {
		if (str == null) throw GamaRuntimeException.error("String cannot be null", scope);
		if (str.isEmpty()) return str;
		return new String(CompressionUtils.zip(str.getBytes()), StandardCharsets.ISO_8859_1);
	}

	/**
	 * Unzip.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param scope
	 *            the scope
	 * @param str
	 *            the str
	 * @return the string
	 * @date 28 oct. 2023
	 */

	/**
	 * Unzip.
	 *
	 * @param scope
	 *            the scope
	 * @param str
	 *            the str
	 * @return the string
	 */
	@operator (
			value = { "uncompress", "decompress", "unzip" },
			can_be_const = true,
			category = { IOperatorCategory.STRING },
			concept = { IConcept.STRING })
	@doc (
			value = "Returns a string that represents the uncompressed form (using gzip) of the argument",
			see = { "compress" })
	@no_test
	public static String unzip(final IScope scope, final String str) {
		if (str == null) throw GamaRuntimeException.error("String cannot be null", scope);
		if (str.isEmpty()) return str;
		return new String(CompressionUtils.unzip(str.getBytes(StandardCharsets.ISO_8859_1)),
				StandardCharsets.ISO_8859_1);
	}

}
