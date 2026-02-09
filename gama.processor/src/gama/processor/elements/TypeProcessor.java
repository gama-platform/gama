/*******************************************************************************************************
 *
 * TypeProcessor.java, in gama.processor, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.processor.elements;

import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

import gama.annotations.type;
import gama.processor.doc.TypeConverter;

/**
 * The TypeProcessor is responsible for processing {@code @type} annotations during the annotation processing phase.
 * 
 * <p>Types in GAMA represent data types that can be used in GAML models - these include primitive types,
 * complex types, and user-defined types. Each type defines how values are stored, manipulated, and converted
 * within the GAMA system. The type processor handles the registration of these types with the GAMA runtime.
 * 
 * <p>This processor handles:
 * <ul>
 * <li><strong>Type Registration:</strong> Registering types with the GAMA type system</li>
 * <li><strong>Type Mapping:</strong> Mapping GAMA types to Java classes</li>
 * <li><strong>Casting Operations:</strong> Processing and validating casting methods</li>
 * <li><strong>Type Hierarchy:</strong> Establishing type relationships and inheritance</li>
 * <li><strong>Documentation Validation:</strong> Ensuring proper documentation for types and casting operators</li>
 * </ul>
 * 
 * <h3>Type Definition Structure:</h3>
 * <p>A typical type definition includes:
 * <ul>
 * <li>A unique name for the type</li>
 * <li>A numeric ID for efficient type identification</li>
 * <li>The kind of type (primitive, complex, etc.)</li>
 * <li>Java classes that this type wraps or represents</li>
 * <li>Casting operations for type conversion</li>
 * </ul>
 * 
 * <h3>Example usage:</h3>
 * <pre>{@code
 * @type(
 *     name = "point", 
 *     id = IType.POINT, 
 *     kind = TYPE_KIND.DATATYPE,
 *     wraps = {GamaPoint.class, ILocation.class}
 * )
 * public class PointType extends GamaType<ILocation> {
 *     
 *     @operator(value = "point", can_be_const = true)
 *     public static ILocation cast(IScope scope, Object o, IType<?> type, boolean copy) {
 *         // Casting implementation
 *     }
 * }
 * }</pre>
 * 
 * @author GAMA Development Team
 * @since 1.0
 * @see type
 * @see ElementProcessor
 * @see TypeConverter
 */
public class TypeProcessor extends ElementProcessor<type> {

	/**
	 * Creates the element code for a type annotation.
	 * 
	 * <p>This method generates the runtime registration code for a GAMA type. The process involves:
	 * <ol>
	 * <li>Extracting wrapped Java types from the annotation (handling reflection complications)</li>
	 * <li>Validating type documentation</li>
	 * <li>Searching for and validating cast methods within the type class</li>
	 * <li>Generating type registration code with all metadata</li>
	 * <li>Registering type mappings for documentation generation</li>
	 * </ol>
	 * 
	 * <p>The method handles the complexity of annotation processing where class references
	 * in annotations are available only through {@link MirroredTypeException} or 
	 * {@link MirroredTypesException}.
	 * 
	 * <p>Cast methods are automatically discovered and validated. A proper cast method should:
	 * <ul>
	 * <li>Be named "cast"</li>
	 * <li>Take exactly 4 parameters (scope, value, type, copy flag)</li>
	 * <li>Have proper documentation if it's the primary casting operator</li>
	 * </ul>
	 * 
	 * @param sb the StringBuilder to append the generated registration code to
	 * @param e the class element annotated with @type
	 * @param t the type annotation containing the type metadata
	 */
	@Override
	public void createElement(final StringBuilder sb, final Element e, final type t) {
		List<? extends TypeMirror> types = Collections.emptyList();
		// Trick to obtain the names of the classes...
		try {
			t.wraps();
		} catch (final MirroredTypesException ex) {
			try {
				types = ex.getTypeMirrors();
			} catch (final MirroredTypeException ex2) {
				types = Collections.singletonList(ex2.getTypeMirror());
			}
		}
		verifyDoc(e, "type " + t.name(), t);
		for (final Element m : e.getEnclosedElements()) {
			if (m.getKind() == ElementKind.METHOD && m.getSimpleName().contentEquals("cast")) {
				final ExecutableElement ee = (ExecutableElement) m;
				if (ee.getParameters().size() == 4) { verifyDoc(m, "the casting operator of " + t.name(), null); }
			}
		}
		
		final String typeName = t.name();
		final String rawTypeName = rawNameOf(e.asType());
		
		sb.append(in).append("_type(").append(toJavaString(typeName)).append(",new ").append(rawTypeName)
				.append("(),").append(t.id()).append(',').append(t.kind());
		types.stream().map(this::rawNameOf).forEach(s -> {
			sb.append(',').append(toClassObject(s));
			TypeConverter.registerType(s, typeName, t.id());
		});
		sb.append(");");

	}

	/**
	 * Returns the annotation class that this processor handles.
	 * 
	 * @return the {@link type} annotation class
	 */
	@Override
	protected Class<type> getAnnotationClass() { return type.class; }

	/**
	 * Validates that a type element meets the requirements for type processing.
	 * 
	 * <p>This method ensures that the type class properly extends the IType interface,
	 * which is required for all type implementations in GAMA. The IType interface provides:
	 * <ul>
	 * <li>Type identity and metadata access</li>
	 * <li>Value validation and conversion operations</li>
	 * <li>Casting and coercion behavior</li>
	 * <li>Integration with the GAMA type system</li>
	 * </ul>
	 * 
	 * @param e the element to validate (should be a class annotated with @type)
	 * @return {@code true} if the element extends IType, {@code false} otherwise
	 */
	@Override
	protected boolean validateElement(final Element e) {
		boolean result = assertClassExtends(true, (TypeElement) e, context.getIType());
		return result;
	}

}
