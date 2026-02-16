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
import gama.api.compilation.documentation.GamlConstantDocumentation;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.file.GenericFile;
import gama.api.types.file.IGamaFile;
import gama.api.types.misc.IContainer;
import gama.api.utils.GamlProperties;

/**
 * @author drogoul
 *
 */
public class ParametricFileType extends ParametricType {

	/**
	 * Gets the generic instance.
	 *
	 * @return the generic instance
	 */
	public static ParametricFileType getGenericFileType() {
		if (genericInstance == null) {
			genericInstance =
					new ParametricFileType("generic_file", IGamaFile.class, (s, o) -> new GenericFile(s, (String) o[0]),
							Types.LIST, Types.NO_TYPE, Types.NO_TYPE, GamaFileType.provideNewIndex());
		}
		return genericInstance;
	}

	/** The id. */
	int id;

	/** The support. */
	@SuppressWarnings ("rawtypes") final Class<IGamaFile> support;

	/** The buffer type. */
	final IContainerType<?> bufferType;

	/** The builder. */
	final IGamaGetter<IGamaFile<?, ?>> builder;

	/** The alias. */
	final String alias;

	/** The plugin. */
	String plugin;

	/** The getters. */
	private Map<String, IArtefactProto.Operator> getters;

	/** The generic instance. */
	static volatile ParametricFileType genericInstance;

	/**
	 * Instantiates a new parametric file type.
	 *
	 * @param name
	 *            the name
	 * @param class1
	 *            the class 1
	 * @param helper
	 *            the helper
	 * @param buffer
	 *            the buffer
	 * @param kt
	 *            the kt
	 * @param ct
	 *            the ct
	 */
	protected ParametricFileType(final String name, final Class<IGamaFile> class1,
			final IGamaGetter<IGamaFile<?, ?>> helper, final IType<?> buffer, final IType<?> kt, final IType<?> ct,
			final int id) {
		super(Types.builtInTypes, Types.FILE, kt, ct);
		support = class1;
		bufferType = (IContainerType<?>) buffer;
		builder = helper;
		alias = name;
		this.id = id;
	}

	@Override
	public int getNumberOfParameters() { return bufferType.getNumberOfParameters(); }

	@Override
	public boolean isDrawable() { return support != null && IGamaFile.Drawable.class.isAssignableFrom(support); }

	/**
	 * Gets the builder.
	 *
	 * @return the builder
	 */
	public IGamaGetter<IGamaFile<?, ?>> getBuilder() { return builder; }

	@Override
	public int hashCode() {
		return id;
	}

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
	 * Document constructors.
	 *
	 * @param result
	 *            the result
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
	 * Gets the support name.
	 *
	 * @return the support name
	 */
	public String getWrappedName() { return "stores its contents as a  " + bufferType.getTitle(); }

	/**
	 * Gets the support name.
	 *
	 * @return the support name
	 */
	public String getSupportName() { return "wraps files of Java class " + support.getSimpleName(); }

	@Override
	public boolean equals(final Object c) {
		if (c instanceof ParametricFileType) return ((ParametricFileType) c).id() == id();
		return super.equals(c);
	}

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
	@Override
	public Class toClass() {
		return support;
	}

	@Override
	public int getVarKind() { return ISymbolKind.Variable.CONTAINER; }

	@Override
	public int id() {
		return id;
	}

	@Override
	public void setDefiningPlugin(final String plugin) { this.plugin = plugin; }

	@Override
	public String getDefiningPlugin() { return plugin; }

	@Override
	public String toString() {
		return alias;
	}

	@Override
	public void collectMetaInformation(final GamlProperties meta) {
		if (plugin != null) {
			meta.put(GamlProperties.PLUGINS, this.plugin);
			meta.put(GamlProperties.TYPES, this.getName());
		}
	}

	/**
	 * Creates the file.
	 *
	 * @param scope
	 *            the scope
	 * @param path
	 *            the path
	 * @param contents
	 *            the contents
	 * @return the i gama file
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

	@Override
	public IType<?> getWrappedType() { return bufferType; }

	@Override
	public boolean isAssignableFrom(final IType<?> l) {
		return l == this;
	}

	@Override
	public IContainerType<?> typeIfCasting(final IExpression exp) {
		return this;
	}

	/**
	 * Sets the field getters.
	 *
	 * @param map
	 *            the map
	 */
	// ====== COPIED FROM GAMATYPE FOR NOW ON
	@Override
	public void setFieldGetters(final Map<String, IArtefactProto.Operator> map) {
		map.replaceAll((final String key, final IArtefactProto.Operator each) -> each.copyWithSignature(this));
		getters = map;
	}

	@Override
	public Map<String, IArtefactProto.Operator> getFieldGetters() {
		return getters == null ? Collections.EMPTY_MAP : getters;
	}

	@Override
	public IArtefactProto getGetter(final String field) {
		if (getters == null) return null;
		return getters.get(field);
	}

	@Override
	public void documentFields(final IGamlDocumentation result) {
		if (getters != null) {
			// sb.append("<b><br/>Fields :</b><ul>");
			for (final IArtefactProto.Operator f : getters.values()) { getFieldDocumentation(result, f); }

			result.append("</ul>");
		}
	}

	/**
	 * Gets the field documentation.
	 *
	 * @param prototype
	 *            the prototype
	 * @return the field documentation
	 */
	void getFieldDocumentation(final IGamlDocumentation sb, final IArtefactProto.Operator prototype) {

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