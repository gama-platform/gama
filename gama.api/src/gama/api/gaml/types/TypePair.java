/*******************************************************************************************************
 *
 * TypePair.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.gaml.types;

/**
 * Immutable key class for caching binary type relationships. Used to cache results of operations like isAssignableFrom,
 * findCommonSupertypeWith, etc.
 *
 * For parametric types, we use identity comparison since parametric types with different content/key types are
 * different instances and should be cached separately.
 *
 * @author drogoul
 */
final class TypePair {

	/** Pre-computed hash code for performance */
	private final int hashCode;

	/** The first type in the pair */
	private final IType<?> type1;

	/** The second type in the pair */
	private final IType<?> type2;

	/**
	 * Creates a new TypePair for caching binary type operations.
	 *
	 * @param type1
	 *            the first type (e.g., the type being checked for assignability)
	 * @param type2
	 *            the second type (e.g., the target type)
	 */
	TypePair(final IType<?> type1, final IType<?> type2) {
		this.type1 = type1;
		this.type2 = type2;
		// Pre-compute hash code for better performance
		this.hashCode = 31 * type1.hashCode() + type2.hashCode();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof final TypePair other)) return false;
		return other.type1 == type1 && other.type2 == type2;
		// return other.hashCode == hashCode;
	}

	@Override
	public String toString() {
		return "(" + type1 + ", " + type2 + ")";
	}

	/**
	 * Gets the first type in the pair.
	 *
	 * @return the first type
	 */
	IType<?> getType1() { return type1; }

	/**
	 * Gets the second type in the pair.
	 *
	 * @return the second type
	 */
	IType<?> getType2() { return type2; }
}
