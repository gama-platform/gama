/*******************************************************************************************************
 *
 * FacetProto.java, in gaml.compiler, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compiler.gaml.prototypes;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import gama.annotations.doc;
import gama.annotations.facet;
import gama.annotations.facets;
import gama.api.additions.registries.ArtefactProtoRegistry;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;

/**
 * The Class FacetProto.
 */
public class FacetProto implements IArtefactProto.Facet, Comparable<FacetProto> {

	/** The Constant NO_DOC. */
	private static final String NO_DOC = "No documentation yet";

	/** The name. */
	private final String name;

	/** The deprecated. */
	private String deprecated = null;

	/** The types. */
	private final IType<?>[] types;

	/** The types describers. */
	private final int[] typesDescribers;

	/** The content type. */
	private final IType<?> contentType;

	/** The key type. */
	private final IType<?> keyType;

	/** The optional. */
	private final boolean optional, internal, isLabel, isId, isRemote, isNewTemp;

	/** The values. */
	private final Set<String> values;

	/** The owner. */
	private String owner;

	/** The support. */
	private Class<?> support;

	/** The main doc. */
	private String mainDoc = null;

	/**
	 * Instantiates a new facet proto.
	 *
	 * @param name
	 *            the name
	 * @param types
	 *            the types
	 * @param ct
	 *            the ct
	 * @param kt
	 *            the kt
	 * @param values
	 *            the values
	 * @param optional
	 *            the optional
	 * @param internal
	 *            the internal
	 * @param isRemote
	 *            the is remote
	 */
	public FacetProto(final String name, final int[] types, final int ct, final int kt, final String[] values,
			final boolean optional, final boolean internal, final boolean isRemote) {
		this.name = name;
		this.typesDescribers = types;
		isNewTemp = typesDescribers[0] == IType.NEW_TEMP_ID;
		this.types = new IType[types.length];
		for (int i = 0; i < types.length; i++) { this.types[i] = Types.get(types[i]); }
		this.contentType = Types.get(ct);
		this.keyType = Types.get(kt);
		this.optional = optional;
		this.internal = internal;
		this.isRemote = isRemote;
		isLabel = ArtefactProtoRegistry.ID_FACETS.contains(types[0]);
		isId = isLabel && types[0] != IType.LABEL;
		this.values = values.length == 0 ? null : ImmutableSet.copyOf(values);
	}

	/**
	 * Checks if is label.
	 *
	 * @return true, if is label
	 */
	@Override
	public boolean isLabel() { return isLabel; }

	/**
	 * Sets the owner.
	 *
	 * @param s
	 *            the new owner
	 */
	@Override
	public void setOwner(final String s) { owner = s; }

	/**
	 * Gets the defining plugin.
	 *
	 * @return the defining plugin
	 */
	@Override
	public String getDefiningPlugin() {
		// returns null as facets cannot be defined alone (the symbol already
		// carries this information)
		return null;
	}

	/**
	 * Checks if is id.
	 *
	 * @return true, if is id
	 */
	@Override
	public boolean isId() { return isId; }

	/**
	 * Method getTitle()
	 *
	 * @see gama.api.compilation.descriptions.IGamlDescription#getTitle()
	 */
	@Override
	public String getTitle() {
		final String p = owner == null ? "" : " of " + owner;
		return "Facet " + name + p;
	}

	/**
	 * Types to string.
	 *
	 * @return the string
	 */
	public String typesToString() {
		final StringBuilder s = new StringBuilder(30);
		s.append(types.length < 2 ? " " : " any type in [");
		for (int i = 0; i < types.length; i++) {
			switch (typesDescribers[i]) {
				case IType.ID -> s.append("an identifier");
				case IType.LABEL -> s.append("a label");
				case IType.NEW_TEMP_ID, IType.NEW_VAR_ID -> s.append("a new identifier");
				case IType.TYPE_ID -> s.append("a datatype identifier");
				case IType.NONE -> s.append("any type");
				default -> // TODO AD 2/16 Document the types with the new possibility to
							// include of and index
						s.append(types[i]);
			}

			if (i != types.length - 1) { s.append(", "); }
		}
		if (types.length >= 2) { s.append("]"); }
		return s.toString();
	}

	/**
	 * Method getDocumentation()
	 *
	 * @see gama.api.compilation.descriptions.IGamlDescription#getDocumentation()
	 */
	@Override
	public IGamlDocumentation getDocumentation() {
		final IGamlDocumentation sb = new GamlRegularDocumentation();
		sb.append(getDeprecated() != null ? "Deprecated" : optional ? "Optional" : "Required").append(", expects ")
				.append(typesToString());
		if (values != null && values.size() > 0) {
			sb.append(", takes values in ").append(values.toString()).append(". ");
		}
		if (getMainDoc() != null && getMainDoc().length() > 0) { sb.append(" - ").append(getMainDoc()); }
		if (getDeprecated() != null) {
			sb.append(" <b>[");
			sb.append(getDeprecated());
			sb.append("]</b>");
		}
		return sb;
	}

	/**
	 * Method getName()
	 *
	 * @see gama.api.compilation.descriptions.IGamlDescription#getName()
	 */
	@Override
	public String getName() { return name; }

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the new name
	 */
	@Override
	public void setName(final String name) {
		// Nothing
	}

	/**
	 * Method compareTo()
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final FacetProto o) {
		return getName().compareTo(o.getName());
	}

	/**
	 * Method serialize()
	 *
	 * @see gama.api.utils.IGamlable#serializeToGaml(boolean)
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		if (getDeprecated() != null || ArtefactProtoRegistry.NON_SERIALIZABLE_FACETS.contains(name)) return "";
		return name + (optional ? ": optional" : ": required") + " ("
				+ (types.length < 2 ? typesToString().substring(1) : typesToString()) + ")";
	}

	/**
	 * Builds the doc.
	 */
	private void buildDoc() {
		if (mainDoc == null) {
			final facets facets = support.getAnnotation(facets.class);
			if (facets != null) {
				final facet[] array = facets.value();
				for (final facet f : array) {
					if (name.equals(f.name())) {
						final doc[] docs = f.doc();
						if (docs != null && docs.length > 0) {
							final doc d = docs[0];
							mainDoc = d.value();
							deprecated = d.deprecated();
							if (deprecated != null && deprecated.length() == 0) { deprecated = null; }
						}
					}
				}
			}
			if (mainDoc == null) { mainDoc = NO_DOC; }
		}

	}

	/**
	 * Sets the class.
	 *
	 * @param c
	 *            the new class
	 */
	@Override
	public void setClass(final Class c) { support = c; }

	/**
	 * Gets the doc.
	 *
	 * @return the doc
	 */
	@Override
	public String getMainDoc() {
		buildDoc();
		return mainDoc;
	}

	/**
	 * Gets the deprecated.
	 *
	 * @return the deprecated
	 */
	@Override
	public String getDeprecated() {
		buildDoc();
		return deprecated;
	}

	/**
	 * Gets the types.
	 *
	 * @return the types
	 */
	@Override
	public IType<?>[] getTypes() { return types; }

	/**
	 * Gets the types describers.
	 *
	 * @return the types describers
	 */
	@Override
	public int[] getTypesDescribers() { return typesDescribers; }

	/**
	 * Gets the content type.
	 *
	 * @return the content type
	 */
	@Override
	public IType<?> getContentType() { return contentType; }

	/**
	 * Gets the key type.
	 *
	 * @return the key type
	 */
	@Override
	public IType<?> getKeyType() { return keyType; }

	/**
	 * Checks if is optional.
	 *
	 * @return the optional
	 */
	@Override
	public boolean isOptional() { return optional; }

	/**
	 * Checks if is optional.
	 *
	 * @return the optional
	 */
	@Override
	public boolean isInternal() { return internal; }

	/**
	 * Checks if is remote.
	 *
	 * @return true, if is remote
	 */
	@Override
	public boolean isRemote() { return isRemote; }

	/**
	 * Checks if is new temp.
	 *
	 * @return true, if is new temp
	 */
	@Override
	public boolean isNewTemp() { return isNewTemp; }

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	@Override
	public Set<String> getValues() { return values; }

	/**
	 * Gets the owner.
	 *
	 * @return the owner
	 */
	@Override
	public String getOwner() { return owner; }

	/**
	 * Gets the support.
	 *
	 * @return the support
	 */
	@Override
	public Class<?> getSupport() { return support; }

}