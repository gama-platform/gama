/*******************************************************************************************************
 *
 * TestProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor.tests;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;

import gama.annotations.action;
import gama.annotations.constant;
import gama.annotations.file;
import gama.annotations.getter;
import gama.annotations.operator;
import gama.annotations.setter;
import gama.annotations.skill;
import gama.annotations.species;
import gama.annotations.symbol;
import gama.annotations.test;
import gama.annotations.tests;
import gama.annotations.type;
import gama.processor.ProcessorContext;
import gama.processor.elements.ElementProcessor;

/**
 * Generates one independent GAML test statement per {@link test} annotation.
 */
public class TestProcessor extends ElementProcessor<tests> {

	@Override
	public void serialize(final Collection<StringBuilder> elements, final StringBuilder sb) {}

	/**
	 * Process.
	 */
	@Override
	public void process(final ProcessorContext context) {
		// Processes tests annotations
		super.process(context);
		// Special case for lone test annotations
		final Map<String, List<Element>> elements = context.groupElements(test.class);
		for (final Map.Entry<String, List<Element>> entry : elements.entrySet()) {
			final StringBuilder sb = serializedElements.getOrDefault(entry.getKey(), new StringBuilder());
			for (final Element e : entry.getValue()) {
				try {
					createTest(sb, e, e.getAnnotation(test.class));
				} catch (final Exception exception) {
					context.emitError("Exception in processor: " + exception.getMessage(), exception, e);
				}

			}
			serializedElements.put(entry.getKey(), sb);
		}
	}

	@Override
	public boolean outputToJava() {
		return false;
	}

	/**
	 * Creates the element.
	 *
	 * @param sb
	 *            the sb
	 * @param e
	 *            the e
	 * @param tests
	 *            the tests
	 */
	@Override
	public void createElement(final StringBuilder sb, final Element e, final tests tests) {
		for (final test test : tests.value()) { createTest(sb, e, test); }
	}

	private void createTest(final StringBuilder sb, final Element e, final test test) {
		sb.append(ln).append(tab).append("test ").append(getTestName(determineName(e, test))).append(" {");
		for (final String line : determineText(test).split(";")) {
			if (!line.isEmpty()) { sb.append(ln).append(tab).append(tab).append(line).append(';'); }
		}
		sb.append(ln).append(tab).append("}").append(ln);
	}

	/**
	 * Write tests.
	 *
	 * @param sb
	 *            the sb
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void writeTests(final Writer sb) throws IOException {
		sb.append("experiment ").append(toJavaString("Tests for " + context.currentPlugin)).append(" type: test {");
		for (final StringBuilder tests : serializedElements.values()) {
			sb.append(ln);
			sb.append(tests);
		}
		sb.append(ln).append('}');
		namesAlreadyUsed.clear();
	}

	/**
	 * Determine text.
	 *
	 * @param test
	 *            the test
	 * @return the string
	 */
	private String determineText(final test test) {
		String text = test.value().trim();
		final int lastSemiColon = text.lastIndexOf(';');
		String lastAssert = text.substring(lastSemiColon + 1);
		text = text.substring(0, lastSemiColon + 1);
		if (lastAssert.isEmpty()) return text;
		if (test.warning()) { lastAssert += " warning: true"; }
		return text + "assert " + lastAssert + ";";
	}

	/**
	 * Determine name.
	 *
	 * @param e
	 *            the e
	 * @param test
	 *            the test
	 * @return the string
	 */
	private String determineName(final Element e, final test test) {
		if (!test.name().isEmpty()) return test.name();
		for (final Annotation a : context.getUsefulAnnotationsOn(e)) {
			final String name = switch (a) {
				case operator o -> "Operator " + o.value()[0];
				case constant c -> "Constant " + c.value();
				case symbol s -> s.name()[0];
				case type t -> "Type " + t.name();
				case skill s -> "Skill " + s.name();
				case species s -> "Species " + s.name();
				case file f -> f.name() + " File";
				case action act -> "Action " + act.name();
				case getter g -> "Getting " + g.value();
				case setter s -> "Setting " + s.value();
				default -> null;
			};
			if (name != null) return name;
		}
		return e.getSimpleName().toString();
	}

	@Override
	protected Class<tests> getAnnotationClass() { return tests.class; }

	/** The names already used. */
	final Map<String, Integer> namesAlreadyUsed = new HashMap<>();

	/**
	 * Gets the test name.
	 *
	 * @param name
	 *            the name
	 * @return the test name
	 */
	private String getTestName(final String name) {
		final int number = namesAlreadyUsed.merge(name, 1, Integer::sum) - 1;
		return toJavaString(number == 0 ? name : name + " (" + number + ")");
	}

}
