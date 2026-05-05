/*******************************************************************************************************
 *
 * SaveOptions.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils.files;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import gama.api.gaml.expressions.IExpression;
import gama.api.utils.files.BufferingUtils.BufferingStrategies;

/**
 * The Class SaveOptions.
 */
public record SaveOptions(String code, boolean addHeader, String type, Object attributesToSave,
		BufferingStrategies bufferingStrategy, boolean rewrite, Double noData, Charset writeCharset) {

	/**
	 * Instantiates a new save options.
	 *
	 * @param code2
	 *            the code 2
	 * @param addHeader2
	 *            the add header 2
	 * @param type2
	 *            the type 2
	 * @param attributesFacet
	 *            the attributes facet
	 * @param strategy
	 *            the strategy
	 * @param rewrite2
	 *            the rewrite 2
	 */
	public SaveOptions(final String code2, final boolean addHeader2, final String type2,
			final IExpression attributesFacet, final BufferingStrategies strategy, final boolean rewrite2) {
		this(code2, addHeader2, type2, attributesFacet, strategy, rewrite2, null, StandardCharsets.UTF_8);
	}

	/**
	 * Instantiates a new save options.
	 *
	 * @param code2
	 *            the code 2
	 * @param addHeader2
	 *            the add header 2
	 * @param type2
	 *            the type 2
	 * @param attributesFacet
	 *            the attributes facet
	 * @param strategy
	 *            the strategy
	 * @param rewrite2
	 *            the rewrite 2
	 * @param noData2
	 *            the optional no-data value to persist when supported by the target format
	 */
	public SaveOptions(final String code2, final boolean addHeader2, final String type2,
			final IExpression attributesFacet, final BufferingStrategies strategy, final boolean rewrite2,
			final Double noData2) {
		this(code2, addHeader2, type2, attributesFacet, strategy, rewrite2, noData2, StandardCharsets.UTF_8);
	}

	/**
	 * With charset.
	 *
	 * @param c
	 *            the c
	 * @return the save options
	 */
	public SaveOptions withCharset(final Charset c) {
		return new SaveOptions(code, addHeader, type, attributesToSave, bufferingStrategy, rewrite, noData, c);
	}

}
