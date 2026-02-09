/*******************************************************************************************************
 *
 * IArtefactProto.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.prototypes;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import gama.annotations.usage;
import gama.annotations.support.ISymbolKind;
import gama.api.additions.IGamaGetter;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IGamlDescription;
import gama.api.compilation.descriptions.IVarDescriptionUser;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Signature;

/**
 *
 */
public interface IArtefactProto extends IGamlDescription {

	/**
	 * The Interface Statement.
	 */
	interface Symbol extends IArtefactProto {

		/**
		 * @return
		 */
		String getOmissible();

		/**
		 * @param a
		 * @return
		 */
		boolean isLabel(String a);

		/**
		 * @param key
		 * @return
		 */
		boolean isId(String key);

		/**
		 * Gets the possible facets.
		 *
		 * @return the possible facets
		 */
		Map<String, IArtefactProto.Facet> getPossibleFacets();

		/**
		 * Checks for args.
		 *
		 * @return true, if successful
		 */
		boolean hasArgs();

		/**
		 * Checks if is primitive.
		 *
		 * @return true, if is primitive
		 */
		boolean isPrimitive();

		/**
		 * Checks for sequence.
		 *
		 * @return true, if successful
		 */
		boolean hasSequence();

		/**
		 * Checks if is remote context.
		 *
		 * @return true, if is remote context
		 */
		boolean isRemoteContext();

		/**
		 * Checks if is top level.
		 *
		 * @return true, if is top level
		 */
		boolean isTopLevel();

		/**
		 * @param s
		 * @return
		 */
		boolean shouldBeDefinedIn(String s);

	}

	/**
	 * The Interface Facet.
	 */
	interface Facet extends IArtefactProto {

		/**
		 * Gets the kind.
		 *
		 * @return the kind
		 */
		@Override
		default int getKind() { return ISymbolKind.FACET; }

		/**
		 * Gets the support.
		 *
		 * @return the support
		 */
		Class<?> getSupport();

		/**
		 * Gets the owner.
		 *
		 * @return the owner
		 */
		String getOwner();

		/**
		 * Gets the values.
		 *
		 * @return the values
		 */
		Set<String> getValues();

		/**
		 * Checks if is new temp.
		 *
		 * @return true, if is new temp
		 */
		boolean isNewTemp();

		/**
		 * Checks if is remote.
		 *
		 * @return true, if is remote
		 */
		boolean isRemote();

		/**
		 * Checks if is optional.
		 *
		 * @return the optional
		 */
		boolean isInternal();

		/**
		 * Checks if is optional.
		 *
		 * @return the optional
		 */
		boolean isOptional();

		/**
		 * Gets the key type.
		 *
		 * @return the key type
		 */
		IType<?> getKeyType();

		/**
		 * Gets the content type.
		 *
		 * @return the content type
		 */
		IType<?> getContentType();

		/**
		 * Gets the types describers.
		 *
		 * @return the types describers
		 */
		int[] getTypesDescribers();

		/**
		 * Gets the types.
		 *
		 * @return the types
		 */
		IType<?>[] getTypes();

		/**
		 * Sets the class.
		 *
		 * @param c
		 *            the new class
		 */
		void setClass(final Class c);

		/**
		 * Checks if is id.
		 *
		 * @return true, if is id
		 */
		boolean isId();

		/**
		 * Sets the owner.
		 *
		 * @param s
		 *            the new owner
		 */
		void setOwner(final String s);

		/**
		 * Checks if is label.
		 *
		 * @return true, if is label
		 */
		boolean isLabel();

		/**
		 * The Interface Operator.
		 */
	}

	/**
	 * The Interface Operator.
	 */
	interface Operator extends IArtefactProto, IVarDescriptionUser {

		/**
		 * @return
		 */
		IType getReturnType();

		/**
		 * @return
		 */
		boolean canBeConst();

		/**
		 * @return
		 */
		Signature getSignature();

		/**
		 * @return
		 */
		boolean[] getLazyness();

		/**
		 * @return
		 */
		boolean isIterator();

		/**
		 * @param context
		 * @param gamlType
		 */
		void verifyExpectedTypes(IDescription context, IType<?> gamlType);

		/**
		 * @return
		 */
		int getTypeProvider();

		/**
		 * @return
		 */
		int getContentTypeContentTypeProvider();

		/**
		 * @return
		 */
		int getContentTypeProvider();

		/**
		 * @return
		 */
		int getKeyTypeProvider();

		/**
		 * @return
		 */
		String getCategory();

		/**
		 * @param b
		 * @return
		 */
		String getPattern(boolean b);

		/**
		 * Copy with signature.
		 *
		 * @param gamaType
		 *            the gama type
		 * @return the operator
		 */
		Operator copyWithSignature(final IType gamaType);

		/**
		 * @return
		 */
		AnnotatedElement getJavaBase();

		/**
		 * Gets the deprecated.
		 *
		 * @return the deprecated
		 */

		IGamaGetter getHelper();
	}

	/**
	 * @return
	 */
	String getDeprecated()
	/**
	 * Gets the main doc.
	 *
	 * @return the main doc
	 */
	;

	/**
	 * @return
	 */
	String getMainDoc()
	/**
	 * Gets the usages.
	 *
	 * @return the usages
	 */
	;

	/**
	 * @return
	 */
	default Iterable<usage> getUsages() {
		return Collections.EMPTY_LIST;
		/**
		 * Gets the kind.
		 *
		 * @return the kind
		 */
	}

	/**
	 * @return
	 */
	int getKind();

}