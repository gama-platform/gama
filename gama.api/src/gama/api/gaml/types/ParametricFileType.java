/*******************************************************************************************************
 *
 * ParametricFileType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;

import gama.annotations.doc;
import gama.annotations.file;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.support.ISymbolKind;
import gama.api.additions.IGamaGetter;
import gama.api.compilation.artefacts.IArtefact;
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.file.GenericFile;
import gama.api.types.file.IGamaFile;
import gama.api.types.misc.IContainer;
import gama.api.utils.GamlProperties;

/**
 * Parametric file type - specialized file types for specific file formats.
 * <p>
 * ParametricFileType represents specific file format implementations in GAMA's extensible file type system. Each
 * parametric file type is associated with particular file extensions, a buffer type for storing contents, and
 * format-specific parsing and serialization logic. This class enables GAMA to support numerous file formats through a
 * plugin-based architecture.
 * </p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 * <li>Format-specific file handling</li>
 * <li>Extension-based type identification</li>
 * <li>Parametric content types</li>
 * <li>Custom file builders/parsers</li>
 * <li>Plugin-based registration</li>
 * <li>Type-safe file operations</li>
 * <li>Automatic documentation generation</li>
 * </ul>
 *
 * <h2>Type Parameters:</h2>
 * <p>
 * Parametric file types have three type parameters defining their structure:
 * <ul>
 * <li><b>Buffer Type</b> - container type for file contents (list, matrix, etc.)</li>
 * <li><b>Key Type</b> - type for indexing file contents</li>
 * <li><b>Content Type</b> - type of individual elements in the file</li>
 * </ul>
 * </p>
 *
 * <h2>Examples of Parametric File Types:</h2>
 *
 * <pre>
 * CSV File:
 *   - Buffer: matrix
 *   - Key: point
 *   - Content: unknown (mixed types)
 *   - Extensions: .csv
 *
 * Shapefile:
 *   - Buffer: list
 *   - Key: int
 *   - Content: geometry
 *   - Extensions: .shp
 *
 * Image File:
 *   - Buffer: matrix
 *   - Key: point
 *   - Content: rgb color
 *   - Extensions: .jpg, .png, .gif, etc.
 *
 * Text File:
 *   - Buffer: list
 *   - Key: int
 *   - Content: string
 *   - Extensions: .txt, .text
 * </pre>
 *
 * <h2>Registration and Extension:</h2>
 * <p>
 * New file types are registered through {@link GamaFileType#addFileTypeDefinition}:
 * </p>
 *
 * <pre>
 * {@code
 * GamaFileType.addFileTypeDefinition("myformat", // alias
 * 		Types.LIST, // buffer type
 * 		Types.INT, // key type
 * 		Types.STRING, // content type
 * 		MyFileClass.class, // implementation class
 * 		builder, // file instance builder
 * 		new String[] { "myext" }, // extensions
 * 		"plugin.id" // plugin ID
 * );
 * }
 * </pre>
 *
 * <h2>File Creation:</h2>
 * <p>
 * Parametric file types provide builders for creating file instances with proper type information and format-specific
 * initialization.
 * </p>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @see ParametricType
 * @see GamaFileType
 * @see IGamaFile
 * @since GAMA 1.0
 */
public class ParametricFileType extends ParametricType {

	/**
	 * Gets the generic file type instance.
	 * <p>
	 * Returns a singleton generic file type used as fallback when no specific file type matches an extension.
	 * </p>
	 *
	 * @return the generic file type instance
	 */
	public static ParametricFileType getGenericFileType() {
		if (genericInstance == null) {
			genericInstance =
					new ParametricFileType("generic_file", IGamaFile.class, (s, o) -> new GenericFile(s, (String) o[0]),
							Types.LIST, Types.NO_TYPE, Types.NO_TYPE, GamaFileType.provideNewIndex());
		}
		return genericInstance;
	}

	/** Unique type identifier for this file type. */
	int id;

	/** The Java class implementing this file type. */
	@SuppressWarnings ("rawtypes") final Class<IGamaFile> support;

	/** The container type used to store file contents. */
	final IContainerType<?> bufferType;

	/** Factory/builder for creating file instances. */
	final IGamaGetter<IGamaFile<?, ?>> builder;

	/** The type alias used in GAML (e.g., "csv", "shapefile"). */
	final String alias;

	/** The plugin that defines this file type. */
	String plugin;

	/** Field getters for accessing file attributes. */
	private Map<String, IArtefact.Operator> getters;

	/** Singleton instance of the generic file type. */
	static volatile ParametricFileType genericInstance;

	/**
	 * Constructs a new parametric file type.
	 *
	 * @param name
	 *            the type name (typically alias + "_file")
	 * @param class1
	 *            the Java class implementing this file type
	 * @param helper
	 *            the builder/factory for creating file instances
	 * @param buffer
	 *            the container type for file contents
	 * @param kt
	 *            the key type for indexing file contents
	 * @param ct
	 *            the content type for file elements
	 * @param id
	 *            the unique type identifier
	 */
	protected ParametricFileType(final String name, final Class<IGamaFile> class1,
			final IGamaGetter<IGamaFile<?, ?>> helper, final IType<?> buffer, final IType<?> kt, final IType<?> ct,
			final int id) {
		super(null, Types.FILE, kt, ct);
		support = class1;
		bufferType = (IContainerType<?>) buffer;
		builder = helper;
		alias = name;
		this.id = id;
	}

	/**
	 * Returns the number of type parameters for this file type.
	 * <p>
	 * Delegates to the buffer type's parameter count.
	 * </p>
	 *
	 * @return the number of type parameters (typically 2: key and content)
	 */
	@Override
	public int getNumberOfParameters() { return bufferType.getNumberOfParameters(); }

	/**
	 * Indicates whether files of this type can be drawn/visualized.
	 * <p>
	 * Files are drawable if they implement the IGamaFile.Drawable interface.
	 * </p>
	 *
	 * @return true if the file type implements drawable interface
	 */
	@Override
	public boolean isDrawable() { return support != null && IGamaFile.Drawable.class.isAssignableFrom(support); }

	/**
	 * Returns the builder/factory for creating file instances.
	 *
	 * @return the file instance builder
	 */
	public IGamaGetter<IGamaFile<?, ?>> getBuilder() { return builder; }

	/**
	 * Computes hash code based on the unique type ID.
	 *
	 * @return the type ID
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * Generates documentation for this file type.
	 * <p>
	 * Creates comprehensive documentation including:
	 * <ul>
	 * <li>File type description from annotations</li>
	 * <li>Supported constructors and their parameters</li>
	 * <li>Accessible fields and attributes</li>
	 * </ul>
	 * </p>
	 *
	 * @return the documentation for this file type
	 */
	@Override
	public IGamlDocumentation getDocumentation() {
		IGamlDocumentation result = new GamlRegularDocumentation();
		doc documentation = support.getAnnotation(doc.class);
		if (documentation == null) {
			final file t = support.getAnnotation(file.class);
			if (t != null) {
				final doc[] docs = t.doc();
				if (docs != null && docs.length > 0) { documentation = docs[0]; }
			}
		}
		if (documentation == null) {
			result.append("File type " + getName() + getSupportName());
		} else {
			result.append("File type ").append(getName()).append(", ").append(getSupportName()).append(", ")
					.append(getWrappedName()).append("<br/>").append(documentation.value());
		}
		documentConstructors(result);
		getGamlType().documentFields(result);
		return result;
	}

	/**
	 * Documents the constructors available for this file type.
	 *
	 * @param result
	 *            the documentation object to append to
	 */
	private void documentConstructors(final IGamlDocumentation result) {
		Constructor[] constructors = support.getConstructors();
		if (constructors.length == 0) return;
		result.append("<br/>").append("File constructors:").append("<br/><ul>");
		for (Constructor<?> c : constructors) {
			doc annotation = c.getAnnotation(doc.class);
			String signature =
					"(" + new Signature(c.getParameterTypes()).asPattern(false).replace("unknown,", "") + ")";
			String doc = annotation == null ? "" : annotation.value();
			result.set("File constructors:", alias + signature, new GamlConstantDocumentation(doc));
		}
	}

	/**
	 * Returns a description of how this file type wraps its contents.
	 *
	 * @return description of the buffer type
	 */
	public String getWrappedName() { return "stores its contents as a  " + bufferType.getTitle(); }

	/**
	 * Returns the name of the Java class supporting this file type.
	 *
	 * @return description of the support class
	 */
	public String getSupportName() { return "wraps files of Java class " + support.getSimpleName(); }

	/**
	 * Checks equality with another object based on type ID.
	 *
	 * @param c
	 *            the object to compare
	 * @return true if c is a ParametricFileType with the same ID
	 */
	@Override
	public boolean equals(final Object c) {
		if (c instanceof ParametricFileType) return ((ParametricFileType) c).id() == id();
		return super.equals(c);
	}

	/**
	 * Casts an object to a file of this specific type.
	 * <p>
	 * Supports casting from:
	 * <ul>
	 * <li>IGamaFile of compatible type - returns the file</li>
	 * <li>IGamaFile of different type - re-casts using the file's path</li>
	 * <li>String - creates a new file from the path</li>
	 * <li>String with container parameter - creates and initializes the file</li>
	 * </ul>
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to a file
	 * @param param
	 *            optional parameter (can be initial contents)
	 * @param keyType
	 *            the key type (not used)
	 * @param contentType
	 *            the content type (not used)
	 * @param copy
	 *            whether to create a copy
	 * @return the file instance, or null if casting fails
	 */
	@Override
	public IGamaFile<?, ?> cast(final IScope scope, final Object obj, final Object param, final IType<?> keyType,
			final IType<?> contentType, final boolean copy) {
		if (obj == null) return null;
		if (obj instanceof IGamaFile) {
			if (support.isInstance(obj)) return (IGamaFile<?, ?>) obj;
			return cast(scope, ((IGamaFile<?, ?>) obj).getPath(scope), param, keyType, contentType, copy);
		}
		if (obj instanceof String) {
			if (param == null) return createFile(scope, (String) obj, null);
			if (param instanceof IContainer.ToSet)
				return createFile(scope, (String) obj, (IContainer.Modifiable<?, ?, ?, ?>) param);
		}
		return null;
	}

	@SuppressWarnings ({ "unchecked", "rawtypes" })
	/**
	 * Returns the Java class implementing this file type.
	 *
	 * @return the support class
	 */
	@Override
	public Class toClass() {
		return support;
	}

	/**
	 * Returns the variable kind for files.
	 *
	 * @return container variable kind
	 */
	@Override
	public ISymbolKind getVarKind() { return ISymbolKind.CONTAINER; }

	/**
	 * Returns the unique type identifier.
	 *
	 * @return the type ID
	 */
	@Override
	public int id() {
		return id;
	}

	/**
	 * Sets the plugin that defines this file type.
	 *
	 * @param plugin
	 *            the plugin identifier
	 */
	@Override
	public void setDefiningPlugin(final String plugin) { this.plugin = plugin; }

	/**
	 * Returns the plugin that defines this file type.
	 *
	 * @return the plugin identifier
	 */
	@Override
	public String getDefiningPlugin() { return plugin; }

	/**
	 * Returns the string representation of this file type (its alias).
	 *
	 * @return the type alias
	 */
	@Override
	public String toString() {
		return alias;
	}

	/**
	 * Collects meta-information about this file type for documentation.
	 *
	 * @param meta
	 *            the properties object to populate
	 */
	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		if (plugin != null) {
			meta.put(GamlProperties.PLUGINS, this.plugin);
			meta.put(GamlProperties.TYPES, this.getName());
		}
	}

	/**
	 * Creates a new file instance of this type.
	 * <p>
	 * Uses the builder to create the file and optionally initializes it with contents.
	 * </p>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param path
	 *            the file path
	 * @param contents
	 *            optional initial contents for the file
	 * @return the created file instance
	 */
	@SuppressWarnings ({ "rawtypes", "unchecked" })
	public IGamaFile createFile(final IScope scope, final String path, final IContainer.Modifiable contents) {
		final IGamaFile file = builder.get(scope, path);
		if (contents != null) {
			file.setWritable(scope, true);
			file.setContents(file.ensureContentsIsCompatible(contents));
		}
		return file;
	}

	/**
	 * Returns the buffer/container type used to store file contents.
	 *
	 * @return the buffer type
	 */
	@Override
	public IType<?> getWrappedType() { return bufferType; }

	/**
	 * Checks if this file type can be assigned from another type.
	 * <p>
	 * Only exact type matches are assignable for parametric file types.
	 * </p>
	 *
	 * @param l
	 *            the type to check
	 * @return true if l is the same type
	 */
	@Override
	public boolean isAssignableFrom(final IType<?> l) {
		return l == this;
	}

	/**
	 * Determines the specific file type when casting an expression.
	 * <p>
	 * Always returns this specific parametric file type.
	 * </p>
	 *
	 * @param exp
	 *            the expression being cast
	 * @return this parametric file type
	 */
	@Override
	public IContainerType<?> typeIfCasting(final IExpression exp) {
		return this;
	}

	/**
	 * Sets the field getters for accessing file attributes.
	 * <p>
	 * Registers operators that can access file fields/attributes.
	 * </p>
	 *
	 * @param map
	 *            the map of field names to getter operators
	 */
	@Override
	public void setFieldGetters(final Map<String, IArtefact.Operator> map) {
		map.replaceAll((final String key, final IArtefact.Operator each) -> each.copyWithSignature(this));
		getters = map;
	}

	/**
	 * Returns the field getters for this file type.
	 *
	 * @return map of field names to getter operators
	 */
	@Override
	public Map<String, IArtefact.Operator> getFieldGetters() {
		return getters == null ? Collections.EMPTY_MAP : getters;
	}

	/**
	 * Returns the getter operator for a specific field.
	 *
	 * @param field
	 *            the field name
	 * @return the getter operator, or null if not found
	 */
	@Override
	public IArtefact getGetter(final String field) {
		if (getters == null) return null;
		return getters.get(field);
	}

	/**
	 * Documents all accessible fields for this file type.
	 *
	 * @param result
	 *            the documentation object to append to
	 */
	@Override
	public void documentFields(final IGamlDocumentation result) {
		if (getters != null) {
			// sb.append("<b><br/>Fields :</b><ul>");
			for (final IArtefact.Operator f : getters.values()) { getFieldDocumentation(result, f); }

			result.append("</ul>");
		}
	}

	/**
	 * Generates documentation for a specific field.
	 *
	 * @param sb
	 *            the documentation object to append to
	 * @param prototype
	 *            the field's getter operator
	 */
	void getFieldDocumentation(final IGamlDocumentation sb, final IArtefact.Operator prototype) {

		final vars annot = prototype.getJavaBase().getAnnotation(vars.class);
		if (annot != null) {
			final variable[] allVars = annot.value();
			for (final variable v : allVars) {
				if (v.name().equals(prototype.getName())) {
					if (v.doc().length > 0) {
						sb.set("Accessible fields: ", v.name(), new GamlConstantDocumentation(v.doc()[0].value()));
					}
					break;
				}
			}
		}
	}
	// ===============

}