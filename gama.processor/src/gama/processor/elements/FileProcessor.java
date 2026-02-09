/*******************************************************************************************************
 *
 * FileProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor.elements;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import gama.annotations.file;
import gama.annotations.support.ITypeProvider;
import gama.processor.Constants;

/**
 * The FileProcessor is responsible for processing {@code @file} annotations during the annotation processing phase.
 *
 * <p>
 * File processors handle the registration of custom file type implementations that can be used to read, write, and
 * manipulate files in GAMA simulations. File types define how specific file formats are loaded, processed, and
 * converted into usable data structures within the simulation environment.
 *
 * <p>
 * This processor handles:
 * <ul>
 * <li><strong>File Type Registration:</strong> Registering file types with the GAMA runtime system</li>
 * <li><strong>Constructor Processing:</strong> Generating appropriate constructors for file creation</li>
 * <li><strong>Extension Association:</strong> Linking file extensions with their corresponding file types</li>
 * <li><strong>Buffer Configuration:</strong> Setting up buffer types and content handling for file operations</li>
 * <li><strong>Verification Operators:</strong> Creating operators to verify file type compatibility</li>
 * </ul>
 *
 * <h3>File Type Structure:</h3>
 * <p>
 * A file type definition includes:
 * <ul>
 * <li>A unique name for the file type</li>
 * <li>Supported file extensions</li>
 * <li>Buffer type and content configuration</li>
 * <li>Constructor methods for file instantiation</li>
 * <li>Type verification logic</li>
 * </ul>
 *
 * <h3>Example usage:</h3>
 *
 * <pre>{@code
 * @file (
 * 		name = "shapefile",
 * 		extensions = { "shp" },
 * 		buffer_type = IType.LIST,
 * 		buffer_content = IType.GEOMETRY)
 * public class ShapeFileType extends GamaFile<IList<IShape>, IShape> {
 * 
 * 	public ShapeFileType(IScope scope, String pathName) {
 * 		super(scope, pathName);
 * 	}
 * 
 * 	// File implementation methods
 * }
 * }</pre>
 *
 * @author GAMA Development Team
 * @since 1.0
 * @see file
 * @see ElementProcessor
 */
public class FileProcessor extends ElementProcessor<file> {

	/** The Constant STRING_ARRAY. */
	final static String[] STRING_ARRAY = { "String" };

	/** Constant for file type verification template to optimize string operations. */
	private static final String FILE_TYPE_VERIFICATION_TEMPLATE =
			"),null,\"Returns true if the parameter is a %s file\",I(0),B,true,3,0,0,0,"
					+ "(s,o)-> { return %s.verifyExtension(%s,%s);}, false);";

	@Override
	public void createElement(final StringBuilder sb, final Element e, final file f) {
		final String name = f.name();
		final String clazz = rawNameOf(e.asType());

		// Register the package of this class for import so we can use simple names
		registerPackageForImport(extractPackageFromClassName(clazz));

		verifyDoc(e, "file " + name, f);

		// Create main file declaration
		createFileDeclaration(sb, name, clazz, f);

		// Create file type verification operator
		createFileTypeVerificationOperator(sb, name);

		// Process constructors
		processConstructors(sb, e, name, clazz, f);
	}

	@Override
	protected Class<file> getAnnotationClass() { return file.class; }

	/**
	 * Creates the main file declaration with buffer type, index, content and extensions.
	 *
	 * @param sb
	 *            the StringBuilder to append to
	 * @param name
	 *            the file name
	 * @param clazz
	 *            the class name
	 * @param f
	 *            the file annotation
	 */
	private void createFileDeclaration(final StringBuilder sb, final String name, final String clazz, final file f) {
		sb.append(in).append("_file(").append(toJavaString(name)).append(',').append(toClassObject(clazz)).append(',');
		buildUnaryFileConstructor(sb, clazz);
		sb.append(",").append(f.buffer_type()).append(",").append(f.buffer_index()).append(",")
				.append(f.buffer_content()).append(",");
		toArrayOfStrings(f.extensions(), sb).append(");");
	}

	/**
	 * Creates the file type verification operator.
	 *
	 * @param sb
	 *            the StringBuilder to append to
	 * @param name
	 *            the file name
	 */
	private void createFileTypeVerificationOperator(final StringBuilder sb, final String name) {
		// Generate proper static method calls
		String gamaFileTypeRef = getClassName("gama.api.gaml.types.GamaFileType");
		String castMethodCall = generateStaticMethodCall("gama.api.gaml.types.Cast", "asString", "s, o[0]");

		sb.append(in).append("_operator(S(").append(toJavaString("is_" + name)).append(String
				.format(FILE_TYPE_VERIFICATION_TEMPLATE, name, gamaFileTypeRef, toJavaString(name), castMethodCall));
	}

	/**
	 * Processes all constructors of the file class to create appropriate operators.
	 *
	 * @param sb
	 *            the StringBuilder to append to
	 * @param e
	 *            the element representing the file class
	 * @param name
	 *            the file name
	 * @param clazz
	 *            the class name
	 * @param f
	 *            the file annotation
	 */
	private void processConstructors(final StringBuilder sb, final Element e, final String name, final String clazz,
			final file f) {
		// Pre-filter constructors to avoid processing non-constructors
		e.getEnclosedElements().stream().filter(m -> m.getKind() == ElementKind.CONSTRUCTOR)
				.forEach(constructor -> processConstructor(sb, constructor, name, clazz, f));
	}

	/**
	 * Processes a single constructor to create an operator if it's valid for GAML.
	 *
	 * @param sb
	 *            the StringBuilder to append to
	 * @param constructor
	 *            the constructor element
	 * @param name
	 *            the file name
	 * @param clazz
	 *            the class name
	 * @param f
	 *            the file annotation
	 */
	private void processConstructor(final StringBuilder sb, final Element constructor, final String name,
			final String clazz, final file f) {
		final List<? extends VariableElement> argParams = ((ExecutableElement) constructor).getParameters();
		final int n = argParams.size();
		if (n <= 1) return;

		// If the first parameter is not IScope, we consider it is not a constructor usable in GAML
		final String scope = rawNameOf(argParams.get(0).asType());
		if (!scope.contains("IScope")) return;

		verifyDoc(constructor, "constructor of " + name, null);

		final String[] args = new String[n - 1];
		int indexOfIType = -1;

		// Process constructor parameters with optimized type checking
		for (int i = 1; i < n; i++) {
			TypeMirror type = argParams.get(i).asType();
			if (context.isIType(type)) { indexOfIType = i; }
			args[i - 1] = rawNameOf(type);
		}

		final int content =
				indexOfIType == -1 ? f.buffer_content() : ITypeProvider.DENOTED_TYPE_AT_INDEX + indexOfIType;
		writeCreateFileOperator(sb, name, clazz, args, content, f.buffer_index());
	}

	/**
	 * Write create file operator.
	 *
	 * @param sb
	 *            the StringBuilder to append to
	 * @param name
	 *            the file name
	 * @param clazz
	 *            the class name
	 * @param names
	 *            the parameter types
	 * @param forcedContent
	 *            the forced content value
	 * @param index
	 *            the buffer index
	 */
	private void writeCreateFileOperator(final StringBuilder sb, final String name, final String clazz,
			final String[] names, final int forcedContent, final int index) {
		// Optimize string operations by chaining StringBuilder operations
		sb.append(in).append("_operator(S(").append(toJavaString(name + "_file")).append("),")
				.append(toClassObject(clazz)).append(".getConstructor(").append(toClassObject(ISCOPE));

		// More efficient parameter class appending
		for (final String classe : names) { sb.append(',').append(toClassObject(classe)); }

		sb.append("),").append(forcedContent).append(",I(0),GF,false,").append(toJavaString(name)).append(',');
		buildFileConstructor(sb, names, clazz);
		sb.append(");");
	}

	/**
	 * Builds the unary file constructor.
	 *
	 * @param sb
	 *            the sb
	 * @param className
	 *            the class name
	 */
	protected void buildUnaryFileConstructor(final StringBuilder sb, final String className) {
		sb.append("(s,o)-> {return new ").append(getClassName(className)).append("(s");
		sb.append(',');
		param(sb, "String", "o[0]");
		sb.append(");}");
	}

	/**
	 * Builds the file constructor.
	 *
	 * @param sb
	 *            the sb
	 * @param classes
	 *            the classes
	 * @param className
	 *            the class name
	 */
	protected void buildFileConstructor(final StringBuilder sb, final String[] classes, final String className) {
		sb.append("(s,o)-> {return new ").append(getClassName(className)).append("(s");
		for (int i = 0; i < classes.length; i++) {
			sb.append(',');
			param(sb, classes[i], "o[" + i + "]");
		}
		sb.append(");}");
	}

	@Override
	public String getExceptions() { return "throws SecurityException, NoSuchMethodException"; }

	/**
	 * Validate element.
	 *
	 * @param context
	 *            the context
	 * @param e
	 *            the e
	 * @return true, if successful
	 */
	@Override
	protected boolean validateElement(final Element e) {
		boolean result = assertClassExtends(true, (TypeElement) e, context.getType(Constants.IGamaFileClassName));
		return result;
	}

	/**
	 * Extracts the package name from a fully qualified class name.
	 *
	 * @param className
	 *            the fully qualified class name
	 * @return the package name, or null if no package can be extracted
	 */
	private String extractPackageFromClassName(final String className) {
		if (className == null) return null;
		int lastDotIndex = className.lastIndexOf('.');
		if (lastDotIndex > 0) return className.substring(0, lastDotIndex);
		return null;
	}

}
