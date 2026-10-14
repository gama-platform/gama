/*******************************************************************************************************
 *
 * GamlDocStyleHelper.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.ui.shared.access;

import com.google.common.base.Strings;

import gama.ui.application.workbench.ThemeHelper;

/**
 * The Class GamlDocStyleHelper.
 *
 * <p>
 * Provides modern styling, CSS stylesheets, and HTML structure for GAML documentation presented in editor hover
 * popups and the GAML Reference search view. Distinct colors and accents are assigned to each language category
 * (statements, facets, variables, actions, types, operators, literals, etc.) and section headers.
 * </p>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since 2025-03
 */
public class GamlDocStyleHelper {

	/**
	 * Returns the CSS class modifier for a documentation header card based on the element's title.
	 *
	 * @param title
	 *            the title string (e.g. "statement parameter", "variable speed", "action move")
	 * @return the CSS class name
	 */
	public static String getHeaderCssClass(final String title) {
		if (title == null) return "gaml-header-default";
		String lower = title.toLowerCase();
		if (lower.startsWith("statement")) return "gaml-header-statement";
		if (lower.startsWith("facet")) return "gaml-header-facet";
		if (lower.startsWith("action") || lower.startsWith("primitive") || lower.startsWith("argument"))
			return "gaml-header-action";
		if (lower.startsWith("operator") || lower.startsWith("function")) return "gaml-header-operator";
		if (lower.startsWith("variable") || lower.startsWith("attribute") || lower.startsWith("field"))
			return "gaml-header-variable";
		if (lower.startsWith("type") || lower.startsWith("data type") || lower.startsWith("species")
				|| lower.startsWith("skill"))
			return "gaml-header-type";
		if (lower.startsWith("unit") || lower.startsWith("constant") || lower.contains("literal"))
			return "gaml-header-constant";
		return "gaml-header-default";
	}

	/**
	 * Returns the RGB color associated with a category name or title to match the hover documentation header accent.
	 *
	 * @param categoryOrTitle
	 *            the category or title (e.g. "Statements", "Facets", "Operators", etc.)
	 * @return the SWT RGB color
	 */
	public static org.eclipse.swt.graphics.RGB getCategoryRGB(final String categoryOrTitle) {
		final boolean dark = ThemeHelper.isDark();
		if (categoryOrTitle == null) return dark ? new org.eclipse.swt.graphics.RGB(52, 152, 219)
				: new org.eclipse.swt.graphics.RGB(41, 128, 185);
		String lower = categoryOrTitle.toLowerCase();
		if (lower.contains("statement")) return dark ? new org.eclipse.swt.graphics.RGB(163, 113, 247)
				: new org.eclipse.swt.graphics.RGB(142, 68, 173); // Purple
		if (lower.contains("facet")) return dark ? new org.eclipse.swt.graphics.RGB(210, 123, 207)
				: new org.eclipse.swt.graphics.RGB(155, 89, 182); // Magenta
		if (lower.contains("action") || lower.contains("argument") || lower.contains("primitive"))
			return dark ? new org.eclipse.swt.graphics.RGB(88, 166, 255)
					: new org.eclipse.swt.graphics.RGB(41, 128, 185); // Blue
		if (lower.contains("operator") || lower.contains("function") || lower.contains("constructor"))
			return dark ? new org.eclipse.swt.graphics.RGB(240, 136, 62)
					: new org.eclipse.swt.graphics.RGB(230, 126, 34); // Orange
		if (lower.contains("variable") || lower.contains("attribute") || lower.contains("field"))
			return dark ? new org.eclipse.swt.graphics.RGB(63, 185, 80)
					: new org.eclipse.swt.graphics.RGB(39, 174, 96); // Green
		if (lower.contains("type") || lower.contains("species") || lower.contains("skill"))
			return dark ? new org.eclipse.swt.graphics.RGB(57, 197, 187)
					: new org.eclipse.swt.graphics.RGB(22, 160, 133); // Teal
		if (lower.contains("unit") || lower.contains("constant") || lower.contains("model"))
			return dark ? new org.eclipse.swt.graphics.RGB(224, 108, 117)
					: new org.eclipse.swt.graphics.RGB(192, 57, 43); // Coral / Red
		return dark ? new org.eclipse.swt.graphics.RGB(88, 166, 255)
				: new org.eclipse.swt.graphics.RGB(41, 128, 185);
	}

	/**
	 * Returns the CSS class modifier for a documentation section banner based on the section header text.
	 *
	 * @param header
	 *            the section header string (e.g. "Possible facets:", "Arguments accepted:")
	 * @return the CSS class name
	 */
	public static String getSectionCssClass(final String header) {
		if (header == null) return "gaml-section-default";
		String lower = header.toLowerCase();
		if (lower.contains("facet")) return "gaml-section-facets";
		if (lower.contains("argument") || lower.contains("parameter")) return "gaml-section-arguments";
		if (lower.contains("attribute") || lower.contains("field")) return "gaml-section-attributes";
		if (lower.contains("action")) return "gaml-section-actions";
		if (lower.contains("return")) return "gaml-section-returns";
		if (lower.contains("constructor")) return "gaml-section-constructors";
		if (lower.contains("example") || lower.contains("usage")) return "gaml-section-examples";
		return "gaml-section-default";
	}

	/**
	 * Wraps the given title, optional comment, and documentation body into a modern HTML card structure.
	 *
	 * @param title
	 *            the title of the element (may be null or empty)
	 * @param comment
	 *            an optional documentation or code comment (may be null or empty)
	 * @param documentation
	 *            the main documentation body HTML
	 * @return the complete HTML card string
	 */
	public static String wrapWithCard(final String title, final String comment, final String documentation) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<div class=\"gaml-hover\">");
		if (!Strings.isNullOrEmpty(title)) {
			String headerClass = getHeaderCssClass(title);
			buffer.append("<div class=\"gaml-hover-header ").append(headerClass).append("\">").append(title)
					.append("</div>");
		}
		if (!Strings.isNullOrEmpty(comment)) {
			buffer.append("<div class=\"gaml-hover-comment\">").append(comment).append("</div>");
		}
		if (!Strings.isNullOrEmpty(documentation)) {
			buffer.append("<div class=\"gaml-hover-content\">").append(documentation).append("</div>");
		}
		buffer.append("</div>");
		return buffer.toString();
	}

	/**
	 * Returns the complete CSS stylesheet adapting dynamically to light or dark theme.
	 *
	 * @return the CSS string
	 */
	public static String getDocCss() {
		final boolean dark = ThemeHelper.isDark();
		StringBuilder css = new StringBuilder();
		css.append("\n/* GAML Modern Documentation Styles */\n");
		css.append(
				"body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 11px; line-height: 1.5; padding: 6px 8px; margin: 0; }\n");

		// Base card structures
		css.append(
				".gaml-hover-header { padding: 5px 8px; margin-bottom: 8px; border-radius: 3px; font-weight: 600; font-size: 1.05em; border-left-width: 4px; border-left-style: solid; }\n");
		css.append(
				".gaml-hover-section { padding: 3px 6px; margin: 10px 0 4px 0; border-radius: 2px; font-weight: 600; font-size: 0.95em; border-left-width: 3px; border-left-style: solid; }\n");
		css.append(
				".gaml-hover-banner { padding: 4px 8px; margin-bottom: 8px; border-radius: 3px; font-size: 0.9em; border-left-width: 3px; border-left-style: solid; }\n");
		css.append(
				".gaml-hover-comment { padding: 4px 8px; margin-bottom: 8px; border-radius: 2px; font-style: italic; font-size: 0.95em; border-left-width: 3px; border-left-style: solid; }\n");
		css.append(".gaml-hover-content { margin-top: 4px; }\n");
		css.append(".gaml-hover-content p { margin: 4px 0 6px 0; }\n");

		if (dark) {
			css.append("body { background-color: #1e2227; color: #abb2bf; }\n");
			css.append("hr { border: none; height: 1px; background-color: #333a44; margin: 8px 0; }\n");

			// Category-specific header colors (Dark mode)
			css.append(
					".gaml-header-statement, .gaml-section-facets { border-left-color: #a371f7; background-color: #271f33; color: #d6b8ff; }\n");
			css.append(
					".gaml-header-facet { border-left-color: #d27bcf; background-color: #2f1d33; color: #f2b8ee; }\n");
			css.append(
					".gaml-header-action, .gaml-section-arguments, .gaml-section-actions { border-left-color: #58a6ff; background-color: #192736; color: #a5d6ff; }\n");
			css.append(
					".gaml-header-operator, .gaml-section-constructors { border-left-color: #f0883e; background-color: #2e2216; color: #ffa657; }\n");
			css.append(
					".gaml-header-variable, .gaml-section-attributes { border-left-color: #3fb950; background-color: #1a2a1f; color: #7ee787; }\n");
			css.append(
					".gaml-header-type, .gaml-section-returns { border-left-color: #39c5bb; background-color: #162b27; color: #79e5dc; }\n");
			css.append(
					".gaml-header-constant { border-left-color: #e06c75; background-color: #2d1c22; color: #f7a8b0; }\n");
			css.append(
					".gaml-section-examples { border-left-color: #8b949e; background-color: #22262c; color: #c9d1d9; }\n");
			css.append(
					".gaml-header-default, .gaml-section-default { border-left-color: #3498db; background-color: #212833; color: #8faec9; }\n");

			// Banners and comments
			css.append(
					".gaml-hover-banner { border-left-color: #d29922; background-color: #2d2618; color: #e5c07b; }\n");
			css.append(
					".gaml-hover-comment { border-left-color: #7f8c8d; background-color: #252a31; color: #8b9eb3; }\n");

			// Code, links, lists, tables
			css.append(
					"code { font-family: ui-monospace, 'SF Mono', Menlo, Monaco, Consolas, monospace; font-size: 0.92em; background-color: #282c34; color: #79c0ff; padding: 1px 5px; border-radius: 3px; border: 1px solid #3e4451; }\n");
			css.append(
					"pre { font-family: ui-monospace, 'SF Mono', Menlo, Monaco, Consolas, monospace; font-size: 0.92em; background-color: #1b1e23; color: #abb2bf; padding: 6px 10px; border-radius: 4px; border: 1px solid #333a44; border-left: 3px solid #3498db; overflow-x: auto; margin: 6px 0; line-height: 1.4; }\n");
			css.append("pre code { background: transparent; padding: 0; border: none; color: inherit; }\n");
			css.append("a:link, a:visited { color: #58a6ff; text-decoration: none; font-weight: 500; }\n");
			css.append("a:hover { color: #79c0ff; text-decoration: underline; }\n");
			css.append("ul, ol { margin: 4px 0 6px 0; padding-left: 18px; }\n");
			css.append("li { margin-bottom: 3px; }\n");
			css.append("li b, dt b { color: #d4e4f7; }\n");
			css.append("table { border-collapse: collapse; width: 100%; margin: 6px 0; font-size: 0.95em; }\n");
			css.append("th, td { padding: 4px 8px; text-align: left; border-bottom: 1px solid #333a44; }\n");
			css.append("th { background-color: #252d38; color: #8faec9; font-weight: 600; }\n");
		} else {
			css.append("body { background-color: #ffffff; color: #24292f; }\n");
			css.append("hr { border: none; height: 1px; background-color: #e1e4e8; margin: 8px 0; }\n");

			// Category-specific header colors (Light mode)
			css.append(
					".gaml-header-statement, .gaml-section-facets { border-left-color: #8e44ad; background-color: #f6f0fb; color: #4a235a; }\n");
			css.append(
					".gaml-header-facet { border-left-color: #9b59b6; background-color: #faf0f9; color: #5b2c6f; }\n");
			css.append(
					".gaml-header-action, .gaml-section-arguments, .gaml-section-actions { border-left-color: #2980b9; background-color: #edf7fd; color: #1b4f72; }\n");
			css.append(
					".gaml-header-operator, .gaml-section-constructors { border-left-color: #e67e22; background-color: #fef5ec; color: #7e3b04; }\n");
			css.append(
					".gaml-header-variable, .gaml-section-attributes { border-left-color: #27ae60; background-color: #edfbf1; color: #196f3d; }\n");
			css.append(
					".gaml-header-type, .gaml-section-returns { border-left-color: #16a085; background-color: #edfbf8; color: #0e6251; }\n");
			css.append(
					".gaml-header-constant { border-left-color: #c0392b; background-color: #fdf2f2; color: #78281f; }\n");
			css.append(
					".gaml-section-examples { border-left-color: #57606a; background-color: #f4f6f8; color: #24292f; }\n");
			css.append(
					".gaml-header-default, .gaml-section-default { border-left-color: #3498db; background-color: #f0f4f8; color: #2c3e50; }\n");

			// Banners and comments
			css.append(
					".gaml-hover-banner { border-left-color: #f39c12; background-color: #fef9e7; color: #996500; }\n");
			css.append(
					".gaml-hover-comment { border-left-color: #95a5a6; background-color: #f6f8fa; color: #57606a; }\n");

			// Code, links, lists, tables
			css.append(
					"code { font-family: ui-monospace, 'SF Mono', Menlo, Monaco, Consolas, monospace; font-size: 0.92em; background-color: #f1f5f9; color: #0969da; padding: 1px 5px; border-radius: 3px; border: 1px solid #e2e8f0; }\n");
			css.append(
					"pre { font-family: ui-monospace, 'SF Mono', Menlo, Monaco, Consolas, monospace; font-size: 0.92em; background-color: #f8fafc; color: #1e293b; padding: 6px 10px; border-radius: 4px; border: 1px solid #e2e8f0; border-left: 3px solid #3498db; overflow-x: auto; margin: 6px 0; line-height: 1.4; }\n");
			css.append("pre code { background: transparent; padding: 0; border: none; color: inherit; }\n");
			css.append("a:link, a:visited { color: #0969da; text-decoration: none; font-weight: 500; }\n");
			css.append("a:hover { color: #0550ae; text-decoration: underline; }\n");
			css.append("ul, ol { margin: 4px 0 6px 0; padding-left: 18px; }\n");
			css.append("li { margin-bottom: 3px; }\n");
			css.append("li b, dt b { color: #1e293b; }\n");
			css.append("table { border-collapse: collapse; width: 100%; margin: 6px 0; font-size: 0.95em; }\n");
			css.append("th, td { padding: 4px 8px; text-align: left; border-bottom: 1px solid #e1e4e8; }\n");
			css.append("th { background-color: #f0f4f8; color: #2c3e50; font-weight: 600; }\n");
		}
		return css.toString();
	}
}
