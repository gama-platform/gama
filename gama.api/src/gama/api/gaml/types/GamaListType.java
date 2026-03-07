/*******************************************************************************************************
 *
 * GamaListType.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

import gama.annotations.doc;
import gama.annotations.type;
import gama.annotations.constants.IKeyword;
import gama.annotations.support.IConcept;
import gama.annotations.support.ISymbolKind;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.runtime.scope.IScope;
import gama.api.types.list.GamaListFactory;
import gama.api.types.list.IList;

/**
 * Type representing ordered collections (lists) in GAML. Lists are one of the fundamental container types in GAMA,
 * providing indexed access to elements.
 * 
 * <p>
 * Lists in GAML:
 * </p>
 * <ul>
 * <li>Are ordered collections with integer indices (starting at 0)</li>
 * <li>Allow duplicate elements</li>
 * <li>Support dynamic resizing (add/remove operations)</li>
 * <li>Can be parameterized with a content type (e.g., {@code list<int>})</li>
 * <li>Support iteration, filtering, mapping, and other functional operations</li>
 * </ul>
 * 
 * <p>
 * List literals in GAML:
 * </p>
 * 
 * <pre>
 * // Empty list
 * list<int> numbers <- [];
 * 
 * // List with initial values
 * list<string> names <- ["Alice", "Bob", "Charlie"];
 * 
 * // Parameterized list type
 * list<float> coordinates <- [1.0, 2.5, 3.7];
 * 
 * // List operations
 * add 5 to: numbers;
 * remove 0 from: numbers;
 * int first <- numbers[0];
 * </pre>
 * 
 * <p>
 * Type conversion to list:
 * </p>
 * <ul>
 * <li><b>From container:</b> Creates a list copy of the container's elements</li>
 * <li><b>From point:</b> Extracts [x, y, z] coordinates as a list<float></li>
 * <li><b>From color:</b> Extracts [r, g, b, alpha] components as a list<int></li>
 * <li><b>From date:</b> Extracts [year, month, day, hour, minute, second] as a list<int></li>
 * <li><b>From agent:</b> Creates a single-element list containing the agent</li>
 * </ul>
 * 
 * <p>
 * Lists are implemented using {@link IList} interface and created via {@link GamaListFactory}.
 * </p>
 * 
 * @author drogoul
 * @since GAMA 1.0
 * 
 * @see IList
 * @see GamaListFactory
 * @see GamaContainerType
 * @see IContainerType
 */
@type (
		name = IKeyword.LIST,
		id = IType.LIST,
		wraps = { IList.class },
		kind = ISymbolKind.CONTAINER,
		concept = { IConcept.TYPE, IConcept.CONTAINER, IConcept.LIST },
		doc = @doc ("Ordered collection of values or agents"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaListType extends GamaContainerType<IList> {

	/**
	 * Constructs a new list type with the specified types manager.
	 * 
	 * <p>
	 * The list type is a built-in parametric type that can be specialized with a content type (e.g., {@code list<int>}
	 * ).
	 * </p>
	 *
	 * @param typesManager
	 *            the types manager that owns this type
	 */
	public GamaListType(final ITypesManager typesManager) {
		super(typesManager);
	}

	/**
	 * Casts an object to a GAMA list with the specified content type.
	 * 
	 * <p>
	 * This method handles various conversion scenarios:
	 * </p>
	 * <ul>
	 * <li><b>Container → List:</b> Copies elements to a new list</li>
	 * <li><b>Point → List:</b> Returns [x, y, z] as list<float></li>
	 * <li><b>Color → List:</b> Returns [r, g, b, alpha] as list<int></li>
	 * <li><b>Date → List:</b> Returns [year, month, day, hour, minute, second] as list<int></li>
	 * <li><b>Single value → List:</b> Creates a single-element list</li>
	 * <li><b>String → List:</b> Splits string into list of characters</li>
	 * </ul>
	 * 
	 * <p>
	 * Examples:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * // Cast point to list
	 * IList coords = listType.cast(scope, point(5.0, 10.0), null, null, Types.FLOAT, false);
	 * // Returns [5.0, 10.0, 0.0]
	 * 
	 * // Cast color to list
	 * IList rgba = listType.cast(scope, rgb(255, 128, 0), null, null, Types.INT, false);
	 * // Returns [255, 128, 0, 255]
	 * 
	 * // Cast single value to list
	 * IList singleElement = listType.cast(scope, 42, null, null, Types.INT, false);
	 * // Returns [42]
	 * }
	 * </pre>
	 *
	 * @param scope
	 *            the current execution scope
	 * @param obj
	 *            the object to cast to a list
	 * @param param
	 *            optional parameter (not used for lists)
	 * @param keyType
	 *            the key type (ignored for lists, always int)
	 * @param contentsType
	 *            the content type for the list elements
	 * @param copy
	 *            if true, creates a defensive copy of the list
	 * @return the object cast to an IList
	 * @throws GamaRuntimeException
	 *             if the cast fails
	 * 
	 * @see GamaListFactory#castToList(IScope, Object, IType, boolean)
	 */
	@Override
	public IList cast(final IScope scope, final Object obj, final Object param, final IType keyType,
			final IType contentsType, final boolean copy) throws GamaRuntimeException {
		return GamaListFactory.castToList(scope, obj, contentsType, copy);
	}

	/**
	 * Returns the key type for lists, which is always integer (for indexing).
	 * 
	 * <p>
	 * Lists use integer indices starting from 0, so the key type is always int regardless of the content type.
	 * </p>
	 *
	 * @return the integer type (Types.INT)
	 */
	@Override
	public IType getKeyType() { return Types.get(INT); }

	/**
	 * Determines the content type when casting a specific expression to a list.
	 * 
	 * <p>
	 * This method provides special handling for compound types that decompose into lists with specific element types:
	 * </p>
	 * <ul>
	 * <li><b>Point:</b> Decomposes to list<float> (coordinates)</li>
	 * <li><b>Color:</b> Decomposes to list<int> (RGBA values 0-255)</li>
	 * <li><b>Date:</b> Decomposes to list<int> (year, month, day, hour, minute, second)</li>
	 * <li><b>Other types:</b> Uses default content type inference</li>
	 * </ul>
	 * 
	 * <p>
	 * Examples:
	 * </p>
	 * 
	 * <pre>
	 * {@code
	 * // For point expression: list(myPoint)
	 * // Returns Types.FLOAT because points decompose to float coordinates
	 * 
	 * // For color expression: list(myColor)
	 * // Returns Types.INT because colors decompose to int RGBA values
	 * 
	 * // For agent expression: list(myAgent)
	 * // Returns the agent's type (default behavior)
	 * }
	 * </pre>
	 *
	 * @param expr
	 *            the expression being cast to a list
	 * @return the content type for the resulting list
	 */
	@Override
	public IType contentsTypeIfCasting(final IExpression expr) {
		switch (expr.getGamlType().id()) {
			case COLOR:
			case DATE:
				return Types.get(INT);
			case POINT:
				return Types.get(FLOAT);
		}
		return super.contentsTypeIfCasting(expr);
	}

	/**
	 * Indicates that lists can be cast to constant expressions.
	 * 
	 * <p>
	 * List literals (e.g., {@code [1, 2, 3]}) can be evaluated at compile time and used in constant contexts.
	 * </p>
	 *
	 * @return true, lists support constant casting
	 */
	@Override
	public boolean canCastToConst() {
		return true;
	}
}
