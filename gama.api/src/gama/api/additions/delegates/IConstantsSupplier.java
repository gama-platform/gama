/*******************************************************************************************************
 *
 * IConstantsSupplier.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions.delegates;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import gama.annotations.constant;
import gama.annotations.doc;
import gama.annotations.support.IConstantCategory;
import gama.api.additions.IConstantAcceptor;
import gama.api.utils.StringUtils;

/**
 * Interface for classes that can supply constants to the GAMA platform.
 * 
 * <p>This interface is used by the GAMA platform to collect and register constants
 * from various sources (e.g., Java classes with annotated static fields). Constants
 * are made available in the GAML language and can be used in models.</p>
 * 
 * <p>Implementations typically scan for static fields annotated with {@link gama.annotations.constant}
 * and provide them to an {@link IConstantAcceptor} for registration.</p>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * public class MyConstantsSupplier implements IConstantsSupplier {
 *     @Override
 *     public void supplyConstantsTo(IConstantAcceptor acceptor) {
 *         browse(MyConstants.class, acceptor);
 *     }
 * }
 * 
 * public class MyConstants {
 *     @constant(value = "pi", category = IConstantCategory.MATH, 
 *               doc = @doc("The mathematical constant pi"))
 *     public static final double PI = 3.14159;
 * }
 * }</pre>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * @see IConstantAcceptor
 * @see gama.annotations.constant
 */
public interface IConstantsSupplier {

	/**
	 * Supplies all constants from this source to the specified acceptor.
	 * 
	 * <p>This is the main entry point called by the GAMA platform during initialization
	 * to gather all constants provided by this supplier. Implementations typically call
	 * the {@link #browse(Class, IConstantAcceptor)} method to scan annotated classes.</p>
	 *
	 * @param acceptor the acceptor that will receive and register the constants
	 */
	void supplyConstantsTo(IConstantAcceptor acceptor);

	/**
	 * Browses a class for constant declarations and supplies them to the acceptor.
	 * 
	 * <p>This default implementation scans all static fields of the given class that are
	 * annotated with {@link gama.annotations.constant}. For each annotated field, it extracts
	 * the constant value, documentation, and metadata (such as alternative names and category),
	 * then passes them to the acceptor for registration.</p>
	 * 
	 * <p>The method handles:</p>
	 * <ul>
	 *   <li>Extraction of constant values from static fields</li>
	 *   <li>Processing of {@code @doc} annotations for documentation</li>
	 *   <li>Handling of alternative names via {@code altNames}</li>
	 *   <li>Detection of time-related constants</li>
	 *   <li>Deprecation notices</li>
	 * </ul>
	 *
	 * @param theClass the class to scan for constant declarations
	 * @param acceptor the acceptor that will receive the discovered constants
	 */
	default void browse(final Class theClass, final IConstantAcceptor acceptor) {
		String[] names = null;
		boolean isTime = false;
		String deprecated = null;
		Object value = null;
		for (final Field field : theClass.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers())) {
				try {
					value = field.get(theClass);
				} catch (SecurityException | IllegalArgumentException | IllegalAccessException e1) {
					e1.printStackTrace();
					continue;
				}
				final constant annotation = field.getAnnotation(constant.class);
				if (annotation != null) {
					names = annotation.altNames();
					StringBuilder documentation = new StringBuilder();
					final doc[] ds = annotation.doc();
					if (ds != null && ds.length > 0) {
						final doc d = ds[0];
						documentation.append(d.value());
						deprecated = d.deprecated();
						if (deprecated.isEmpty()) { deprecated = null; }
						isTime = Arrays.asList(annotation.category()).contains(IConstantCategory.TIME);
					}
					documentation.append(". Its internal value is <b>").append(StringUtils.toGaml(value, false))
							.append(". </b><p/>");
					acceptor.accept(annotation.value(), value, documentation.toString(), deprecated, isTime, names);
				}
			}
		}
	}

}
