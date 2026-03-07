/*******************************************************************************************************
 *
 * Constants.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor;

import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.type.TypeMirror;

import gama.annotations.action;
import gama.annotations.constant;
import gama.annotations.display;
import gama.annotations.doc;
import gama.annotations.experiment;
import gama.annotations.file;
import gama.annotations.listener;
import gama.annotations.operator;
import gama.annotations.skill;
import gama.annotations.species;
import gama.annotations.symbol;
import gama.annotations.tests;
import gama.annotations.type;
import gama.annotations.vars;
import gama.annotations.constants.IKeyword;
import gama.processor.doc.DocProcessor;
import gama.processor.elements.ActionProcessor;
import gama.processor.elements.ConstantProcessor;
import gama.processor.elements.DisplayProcessor;
import gama.processor.elements.ExperimentProcessor;
import gama.processor.elements.FileProcessor;
import gama.processor.elements.ListenerProcessor;
import gama.processor.elements.OperatorProcessor;
import gama.processor.elements.SkillProcessor;
import gama.processor.elements.SpeciesProcessor;
import gama.processor.elements.SymbolProcessor;
import gama.processor.elements.TypeProcessor;
import gama.processor.elements.VarsProcessor;
import gama.processor.tests.TestProcessor;

/**
 * The Interface Constants.
 */
public interface Constants {

	/**
	 *
	 */
	String IVarAndActionSupportClassName = "gama.api.compilation.IVarAndActionSupport";

	/**
	 *
	 */
	String ITypeClassName = "gama.api.gaml.types.IType";

	/**
	 *
	 */
	String IExpressionClassName = "gama.api.gaml.expressions.IExpression";

	/**
	 *
	 */
	String IScopeClassName = "gama.api.runtime.scope.IScope";

	/**
	 *
	 */
	String ISkillClassName = "gama.api.kernel.skill.ISkill";

	/** The I display surface class name. */
	String IDisplaySurfaceClassName = "gama.api.ui.displays.IDisplaySurface";

	/** The I experiment agent class name. */
	String IExperimentAgentClassName = "gama.api.kernel.simulation.IExperimentAgent";

	/** The I gama file class name. */
	String IGamaFileClassName = "gama.api.types.file.IGamaFile";

	/** The I agent class name. */
	String IAgentClassName = "gama.api.kernel.agent.IAgent";

	/** The I symbol class name. */
	String ISymbolClassName = "gama.api.gaml.symbols.ISymbol";

	/** The Constant PRODUCES_DOC. */
	boolean PRODUCES_DOC = true;

	/** The Constant CHARSET. */
	Charset CHARSET = Charset.forName("UTF-8");

	/** The Constant ADDITIONS_PACKAGE_BASE. */
	String ADDITIONS_PACKAGE_BASE = "gaml.additions";

	/** The Constant ADDITIONS_CLASS_NAME. */
	String ADDITIONS_CLASS_NAME = "GamlAdditions";

	/** The Constant PRODUCES_WARNING. */
	boolean PRODUCES_WARNING = true;

	/**
	 * Gets the i skill.
	 *
	 * @return the i skill
	 */
	default TypeMirror getISkill() { return getType(ISkillClassName); }

	/**
	 * Gets the i scope.
	 *
	 * @return the i scope
	 */
	default TypeMirror getIScope() { return getType(IScopeClassName); }

	/**
	 * Gets the string.
	 *
	 * @return the string
	 */
	default TypeMirror getString() { return getType(String.class.getName()); }

	/**
	 * Gets the i expression.
	 *
	 * @return the i expression
	 */
	default TypeMirror getIExpression() { return getType(IExpressionClassName); }

	/**
	 * Gets the string.
	 *
	 * @return the string
	 */
	default TypeMirror getIType() { return getType(ITypeClassName); }

	/**
	 * Gets the i var and action support.
	 *
	 * @return the i var and action support
	 */
	default TypeMirror getIVarAndActionSupport() { return getType(IVarAndActionSupportClassName); }

	/**
	 * Gets the i agent.
	 *
	 * @return the i agent
	 */
	default TypeMirror getIAgent() { return getType(IAgentClassName); }

	/**
	 * @param iagentclassname2
	 * @return
	 */
	TypeMirror getType(String classQualifiedName);

	/**
	 * Gets the i symbol.
	 *
	 * @return the i symbol
	 */
	default TypeMirror getISymbol() { return getType(ISymbolClassName); }

	/**
	 * Capitalize first letter.
	 *
	 * @param original
	 *            the original
	 * @return the string
	 */
	static String capitalizeFirstLetter(final String original) {
		if (original == null || original.length() == 0) return original;
		return original.substring(0, 1).toUpperCase() + original.substring(1);
	}

	/**
	 * Capitalize all words.
	 *
	 * @param str
	 *            the str
	 * @return the string
	 */
	static String capitalizeAllWords(final String str) {
		if (str == null || str.length() == 0) return str;
		final int strLen = str.length();
		final StringBuilder buffer = new StringBuilder(strLen);
		boolean capitalizeNext = true;
		for (int i = 0; i < strLen; i++) {
			final char ch = str.charAt(i);
			if (' ' == ch) {
				buffer.append(ch);
				capitalizeNext = true;
			} else if (capitalizeNext) {
				buffer.append(Character.toTitleCase(ch));
				capitalizeNext = false;
			} else {
				buffer.append(ch);
			}
		}
		return buffer.toString();

	}

	/**
	 * Gets the alphabet order.
	 *
	 * @param name
	 *            the name
	 * @return the alphabet order
	 */
	static String getAlphabetOrder(final String name) {
		String order = "";
		final String lastChar = "z";

		for (int i = 0; i < cuttingLettersOperatorDoc.length; i++) {
			final Character previousChar = i == 0 ? 'a' : cuttingLettersOperatorDoc[i - 1];
			final Character c = cuttingLettersOperatorDoc[i];

			if (i == 0 && name.toLowerCase().compareTo(c.toString().toLowerCase()) < 0
					|| name.toLowerCase().compareTo(previousChar.toString().toLowerCase()) >= 0
							&& name.toLowerCase().compareTo(c.toString().toLowerCase()) < 0) {
				order = previousChar.toString() + Character.toString(Character.toChars(c - 1)[0]);
			}
		}
		if ("".equals(order)) {
			order = cuttingLettersOperatorDoc[cuttingLettersOperatorDoc.length - 1].toString() + lastChar;
		}

		return order;
	}

	/** The basic skill. */
	String BASIC_SKILL = "gama.gaml.skills.Skill";

	/** The cutting letters operator doc. */
	Character[] cuttingLettersOperatorDoc = { 'b', 'd', 'i', 'n', 's' };

	/** The doc sep. */
	String DOC_SEP = "~";

	/** The ln. */
	String ln = "\n";

	/** The tab. */
	String tab = "\t";

	/** The in. */
	String in = ln;

	/** The boolean. */
	String IAGENT = "IAgent", IPOPULATION = "IPopulation", ISIMULATION = "ISimulation", ISKILL = "ISkill",
			ISYMBOL = "ISymbol", IDESC = "IDescription", ISCOPE = "IScope", OBJECT = "Object", IVALUE = "IValue",
			IEXPRESSION = "IExpression", INTEGER = "Integer", DOUBLE = "Double", BOOLEAN = "Boolean";

	/** The explicit imports. */
	String[] INDIVIDUAL_IMPORTS = {};

	/**
	 * Fallback package imports - only used if import tracking completely fails (should never happen). Since we now
	 * track ALL class imports automatically, this is just a safety net. No longer reads from gama-api-packages.txt
	 * file.
	 */
	Set<String> COLLECTIVE_IMPORTS = Stream.of("java.util.", "java.lang.").collect(Collectors.toSet());

	/** The static star imports. */
	Set<String> STATIC_COLLECTIVE_IMPORTS = Stream.of("gama.api.gaml.types.Cast", IKeyword.class.getName())
			.map(s -> s + ".").collect(Collectors.toSet());

	/** The ss 1. */
	List<String> ss1 = Arrays.asList(IKeyword.CONST, IKeyword.TRUE, IKeyword.FALSE, IKeyword.NAME, IKeyword.TYPE);

	/** The ss 2. */
	List<String> ss2 = ss1.stream().map(String::toUpperCase).collect(Collectors.toList());

	/** The class names. */
	Map<String, String> CLASS_NAMES = new HashMap<>() {
		{
			put("IAgent", "IA");
			put("IGamlAgent", "IG");
			put("IColor", "GC");
			put("IPair", "GP");
			put("GamaShape", "GS");
			put("Object", "O");
			put("Integer", "I");
			put("Double", "D");
			put("Boolean", "B");
			put("IExpression", "IE");
			put("IShape", "IS");
			put("IMap", "GM");
			put("IContainer", "IC");
			put("IPoint", "IL");
			put("IMatrix", "IM");
			put("String", "S");
			put("IPoint", "P");
			put("IGamaFile", "GF");
			put("IPath", "IP");
			put("IList", "LI");
			put("ITopology", "IT");
			put("GamlAgent", "GA");
			put("ISpecies", "SP");
			put("IScope", "SC");
			put("IDate", "GD");
			put("PlatformAgent", "PA");
			put("double", "d");
			put("int", "i");
			put("boolean", "b");

		}
	};

	/** The return when null. */
	Map<String, String> RETURN_WHEN_NULL = new HashMap<>() {
		{
			put(DOUBLE, " 0d");
			put(INTEGER, " 0");
			put(BOOLEAN, " false");
		}
	};

	/** The check prim. */
	Map<String, String> CHECK_PRIM = new HashMap<>() {
		{
			put("int", INTEGER);
			put("short", INTEGER);
			put("long", INTEGER);
			put("double", DOUBLE);
			put("float", DOUBLE);
			put("boolean", BOOLEAN);
		}
	};

	/** The package name. */
	String PACKAGE_NAME = "gaml.additions";

	/** The processors. */
	Map<Class<? extends Annotation>, IProcessor<?>> processors = new LinkedHashMap<>() {
		{
			// Order is important
			put(type.class, new TypeProcessor());
			// Doc built first, so that test generation can happen subsequently
			put(doc.class, new DocProcessor());
			// Then all the processors for specific annotations
			put(species.class, new SpeciesProcessor());
			put(symbol.class, new SymbolProcessor());
			put(vars.class, new VarsProcessor());
			put(listener.class, new ListenerProcessor());
			put(operator.class, new OperatorProcessor());
			put(file.class, new FileProcessor());
			put(action.class, new ActionProcessor());
			put(skill.class, new SkillProcessor());
			put(display.class, new DisplayProcessor());
			put(experiment.class, new ExperimentProcessor());
			put(constant.class, new ConstantProcessor());
			// TestProcessor actually processes both @tests and @test annotations
			put(tests.class, new TestProcessor());
		}
	};

}
