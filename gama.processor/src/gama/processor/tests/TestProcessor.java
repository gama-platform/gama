/*******************************************************************************************************
 *
 * TestProcessor.java, in gama.processor, is part of the source code of the
 * GAMA modeling and simulation platform .
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

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.constant;
import gama.annotations.precompiler.GamlAnnotations.file;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.species;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.test;
import gama.annotations.precompiler.GamlAnnotations.tests;
import gama.annotations.precompiler.GamlAnnotations.type;
import gama.processor.ElementProcessor;
import gama.processor.ProcessorContext;

/**
 * The Class TestProcessor.
 */
public class TestProcessor extends ElementProcessor<tests> {

	@Override
	public void serialize(final ProcessorContext context, final Collection<StringBuilder> elements,
			final StringBuilder sb) {}

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
					createElement(sb, context, e, createFrom(e.getAnnotation(test.class)));
				} catch (final Exception exception) {
					context.emitError("Exception in processor: " + exception.getMessage(), e);
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
	 * Creates the from.
	 *
	 * @param test the test
	 * @return the tests
	 */
	private tests createFrom(final test test) {
		return new tests() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return tests.class;
			}

			@Override
			public test[] value() {
				return new test[] { test };
			}
		};
	}

	@Override
	public void createElement(final StringBuilder sb, final ProcessorContext context, final Element e,
			final tests tests) {
		final String name = getTestName(determineName(context, e, tests));
		sb.append(ln).append(tab).append("test ").append(name).append(" {");
		for (final test test : tests.value()) {
			final String[] lines = determineText(context, test).split(";");
			for (final String line : lines) {
				if (!line.isEmpty()) {
					sb.append(ln).append(tab).append(tab).append(line).append(';');
				}
			}
		}
		// Output the footer
		sb.append(ln).append(tab).append("}").append(ln);
	}

	/**
	 * Write tests.
	 *
	 * @param context the context
	 * @param sb the sb
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeTests(final ProcessorContext context, final Writer sb) throws IOException {
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
	 * @param context the context
	 * @param test the test
	 * @return the string
	 */
	private String determineText(final ProcessorContext context, final test test) {
		String text = test.value().trim();
		final int lastSemiColon = text.lastIndexOf(';');
		String lastAssert = text.substring(lastSemiColon + 1);
		text = text.substring(0, lastSemiColon + 1);
		if (lastAssert.isEmpty()) { return text; }
		if (test.warning()) {
			lastAssert += " warning: true";
		}
		return text + "assert " + lastAssert + ";";
	}

	/**
	 * Determine name.
	 *
	 * @param context the context
	 * @param e the e
	 * @param tests the tests
	 * @return the string
	 */
	private String determineName(final ProcessorContext context, final Element e, final tests tests) {
		String testName = null;
		// Looking for named tests and concatenating their individual names
		for (final test test : tests.value()) {
			final String individualName = test.name();
			if (!individualName.isEmpty()) {
				if (testName == null) {
					testName = individualName;
				} else {
					testName += " and " + individualName;
				}
			}
		}
		// No named tests, proceed by inferring the name from the GAML artefact (if present)
		if (testName == null) {
			for (final Annotation a : context.getUsefulAnnotationsOn(e)) {
				switch (a.annotationType().getSimpleName()) {
					case "operator":
						testName = "Operator " + ((operator) a).value()[0];
						break;
					case "constant":
						testName = "Constant " + ((constant) a).value();
						break;
					case "symbol":
						testName = ((symbol) a).name()[0];
						break;
					case "type":
						testName = "Type " + ((type) a).name();
						break;
					case "skill":
						testName = "Skill " + ((skill) a).name();
						break;
					case "species":
						testName = "Species " + ((species) a).name();
						break;
					case "file":
						testName = ((file) a).name() + " File";
						break;
					case "action":
						testName = "Action " + ((action) a).name();
						break;
					case "getter":
						testName = "Getting " + ((getter) a).value();
						break;
					case "setter":
						testName = "Setting " + ((setter) a).value();
				}
				if (testName != null) {
					break;
				}
			}
		}
		// No named tests and no GAML artefact present; grab the name of the Java element as a last call
		if (testName == null) {
			testName = e.getSimpleName().toString();
		}
		return testName;
	}

	@Override
	protected Class<tests> getAnnotationClass() {
		return tests.class;
	}

	/** The names already used. */
	final Map<String, Integer> namesAlreadyUsed = new HashMap<>();

	/**
	 * Gets the test name.
	 *
	 * @param name the name
	 * @return the test name
	 */
	private String getTestName(final String name) {
		String result = name;
		Integer number = namesAlreadyUsed.get(name);
		if (number != null) {
			number++;
			namesAlreadyUsed.put(name, number);
			result = name + " (" + number + ")";
		} else {
			namesAlreadyUsed.put(name, 0);
		}
		return toJavaString(result);
	}

}
