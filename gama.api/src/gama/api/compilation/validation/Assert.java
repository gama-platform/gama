/*******************************************************************************************************
 *
 * Assert.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.compilation.validation;

import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import gama.annotations.constants.IKeyword;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IVariableDescription;
import gama.api.constants.IGamlIssue;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.expressions.IExpressionDescription;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.gaml.types.Types;

/**
 * Validation utility class providing assertion methods for type compatibility and name validation.
 *
 * <p>
 * This class serves as a centralized location for validation logic used throughout the GAML compilation process. It
 * provides static utility methods to verify:
 * </p>
 *
 * <ul>
 * <li><strong>Type Compatibility:</strong> Ensuring assigned values can be safely converted to receiver types</li>
 * <li><strong>Name Validity:</strong> Checking that identifiers don't conflict with reserved keywords or type
 * names</li>
 * <li><strong>Container Type Compliance:</strong> Validating content types for container assignments (lists, maps)</li>
 * <li><strong>Species Compatibility:</strong> Ensuring species hierarchies are respected in assignments</li>
 * </ul>
 *
 * <h2>Type System Validation</h2>
 *
 * <p>
 * The class handles several special cases in the GAML type system:
 * </p>
 * <ul>
 * <li><strong>Int/Float Conversions:</strong> Automatic casting with warnings (Issue #590)</li>
 * <li><strong>Empty Containers:</strong> Accepting empty lists/maps for any container type</li>
 * <li><strong>Species Inheritance:</strong> Verifying sub-species relationships</li>
 * <li><strong>Map/List-of-Pairs:</strong> Special conversion handling (Issue #846)</li>
 * <li><strong>Nil Values:</strong> Checking against default value availability</li>
 * </ul>
 *
 * <h2>Reserved Keywords</h2>
 *
 * <p>
 * The following keywords are reserved and cannot be used as variable or entity names:
 * </p>
 * <ul>
 * <li>{@code false} - Boolean literal</li>
 * <li>{@code true} - Boolean literal</li>
 * <li>{@code null} - Null literal</li>
 * <li>{@code myself} - Special agent reference</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Validate assignment type compatibility
 * Assert.typesAreCompatibleForAssignment("value", // facet name
 * 		context, // description context
 * 		"variable x", // receiver description
 * 		Types.FLOAT, // expected type
 * 		assignedExpression // value being assigned
 * );
 *
 * // Validate name is not reserved
 * if (!Assert.nameIsValid(variableDescription)) {
 * 	// Name validation failed, errors already reported
 * 	return;
 * }
 * }</pre>
 *
 * <h2>Error Reporting</h2>
 *
 * <p>
 * All validation methods report issues directly to the description context using appropriate issue codes from
 * {@link IGamlIssue}:
 * </p>
 * <ul>
 * <li>{@link IGamlIssue#SHOULD_CAST} - Type conversion warnings</li>
 * <li>{@link IGamlIssue#WRONG_TYPE} - Incompatible type errors</li>
 * <li>{@link IGamlIssue#IS_RESERVED} - Reserved keyword usage</li>
 * <li>{@link IGamlIssue#IS_A_TYPE} - Type name conflicts</li>
 * <li>{@link IGamlIssue#MISSING_NAME} - Missing name attribute</li>
 * </ul>
 *
 * @author Alexis Drogoul
 * @since GAMA 1.0
 * @version 2025-03
 *
 * @see IDescription
 * @see IType
 * @see Types
 * @see IGamlIssue
 */
public class Assert implements gama.annotations.constants.IKeyword {

	/**
	 * Validates that an assigned value is type-compatible with the receiver variable.
	 *
	 * <p>
	 * This method performs comprehensive type checking for assignments, ensuring that the assigned expression can be
	 * safely converted to the receiver type. It handles multiple special cases in the GAML type system and emits
	 * appropriate warnings or errors.
	 * </p>
	 *
	 * <h3>Validation Process:</h3>
	 * <ol>
	 * <li><strong>Null Checks:</strong> Returns early if the assigned expression is null</li>
	 * <li><strong>Base Type Validation:</strong> Checks if assigned type is translatable to receiver type</li>
	 * <li><strong>Container Content Validation:</strong> For containers, validates content type compatibility</li>
	 * <li><strong>Special Cases:</strong> Handles int/float conversions, empty containers, and species hierarchies</li>
	 * </ol>
	 *
	 * <h3>Special Cases Handled:</h3>
	 * <ul>
	 * <li><strong>Int/Float Casting:</strong> Automatic conversion with warning (Issue #590)</li>
	 * <li><strong>Empty Containers:</strong> Empty lists/maps are accepted for any container type</li>
	 * <li><strong>Species Incompatibility:</strong> Validates sub-species relationships (see
	 * {@link #speciesAreIncompatible})</li>
	 * <li><strong>Map/List-of-Pairs:</strong> Special handling for list&lt;pair&gt; to map conversion (Issue #846)</li>
	 * </ul>
	 *
	 * <h3>Error/Warning Conditions:</h3>
	 * <ul>
	 * <li>{@link IGamlIssue#SHOULD_CAST} - Warning when implicit casting is required</li>
	 * <li>{@link IGamlIssue#WRONG_TYPE} - Error when types are incompatible for species</li>
	 * </ul>
	 *
	 * @param facetName
	 *            the name of the facet being assigned (e.g., "value", "init")
	 * @param context
	 *            the description context where the assignment occurs (for error reporting)
	 * @param receiverDescription
	 *            a human-readable description of what receives the value (e.g., "variable x")
	 * @param receiverType
	 *            the expected type of the receiver
	 * @param assigned
	 *            the expression description being assigned (may be null)
	 *
	 * @see #verifyIntFloatAndEmptyContainers
	 * @see #speciesAreIncompatible
	 * @see #emitCastingWarning
	 */
	public static void typesAreCompatibleForAssignment(final String facetName, final IDescription context,
			final String receiverDescription, final IType<?> receiverType, final IExpressionDescription assigned) {
		if (assigned == null) return;
		final IExpression value = assigned.getExpression();
		if (value == null) return;
		final IType assignedType = value.getGamlType();
		verifyIntFloatAndEmptyContainers(facetName, context, receiverDescription, receiverType, assigned, value,
				assignedType);
		// Contents Type
		if (receiverType.isContainer() && assignedType.isContainer()) {
			final IType receiverContentType = receiverType.getContentType();
			IType<?> contentType = assignedType.getContentType();
			// Special cases for the empty lists and maps
			if (Types.isEmptyContainerCase(receiverType, value)
					|| speciesAreIncompatible(context, receiverType, assigned, value, receiverContentType, contentType))
				return;
			// Special case for maps and lists of pairs (Issue 846)
			if (receiverType.id() == IType.MAP && assignedType.id() == IType.LIST && contentType.id() == IType.PAIR) {
				contentType = contentType.getContentType();
			}
			if (!contentType.isTranslatableInto(receiverContentType)) {
				emitCastingWarning(facetName, context, receiverDescription, receiverType, assigned, receiverContentType,
						contentType);
			}
		}
	}

	/**
	 * Verifies and warns about int/float conversions and empty container assignments.
	 *
	 * <p>
	 * This method handles two special cases in the GAML type system:
	 * </p>
	 *
	 * <h3>1. Int/Float Conversion (Issue #590):</h3>
	 * <p>
	 * When an integer value is assigned to a float variable (or vice versa), GAML performs an automatic cast but emits
	 * a warning to alert the developer of the potential precision loss or conversion.
	 * </p>
	 *
	 * <h3>2. Empty Container Assignment:</h3>
	 * <p>
	 * Empty lists and maps (e.g., {@code []}, {@code map([])}) are accepted for any container type assignment, as they
	 * don't carry type-specific content that could cause issues.
	 * </p>
	 *
	 * <h3>Warning Generated:</h3>
	 * <p>
	 * When a conversion is needed, the method emits a {@link IGamlIssue#SHOULD_CAST} warning with the message:
	 * "{receiver} of type {receiverType} is assigned a value of type {assignedType}, which will be casted to
	 * {receiverType}"
	 * </p>
	 *
	 * @param facetName
	 *            the name of the facet being assigned
	 * @param context
	 *            the description context for error reporting
	 * @param receiverDescription
	 *            human-readable description of the receiver
	 * @param receiverType
	 *            the expected type
	 * @param assigned
	 *            the expression description being assigned
	 * @param value
	 *            the actual expression value
	 * @param assignedType
	 *            the type of the assigned value
	 *
	 * @see Types#intFloatCase
	 * @see Types#isEmptyContainerCase
	 */
	private static void verifyIntFloatAndEmptyContainers(final String facetName, final IDescription context,
			final String receiverDescription, final IType<?> receiverType, final IExpressionDescription assigned,
			final IExpression value, final IType assignedType) {
		// AD: 6/9/13 special case for int and float (see Issue 590) and for
		// empty lists and maps
		if ((value != GAML.getExpressionFactory().getNil()
				&& !assignedType.getGamlType().isTranslatableInto(receiverType.getGamlType())
				|| Types.intFloatCase(receiverType, assignedType))
				&& !Types.isEmptyContainerCase(receiverType, value)) {
			final EObject target = assigned.getTarget();
			final String msg =
					receiverDescription + " of type " + receiverType.getGamlType() + " is assigned a value of type "
							+ assignedType.getGamlType() + ", which will be casted to " + receiverType.getGamlType();
			if (target == null) {
				context.warning(msg, IGamlIssue.SHOULD_CAST, facetName, receiverType.toString());
			} else {
				context.warning(msg, IGamlIssue.SHOULD_CAST, target, receiverType.toString());
			}
		}
	}

	/**
	 * Emits a casting warning for incompatible container content types.
	 *
	 * <p>
	 * This method generates a {@link IGamlIssue#SHOULD_CAST} warning when the content type of a container being
	 * assigned doesn't match the expected content type. For example, assigning a {@code list<int>} to a
	 * {@code list<float>} variable.
	 * </p>
	 *
	 * <p>
	 * The warning message indicates which elements will be automatically cast and helps developers understand potential
	 * type conversions happening in their code.
	 * </p>
	 *
	 * <h3>Warning Format:</h3>
	 * <p>
	 * "Elements of {receiver} are of type {receiverContentType} but are assigned elements of type {contentType}, which
	 * will be casted to {receiverContentType}"
	 * </p>
	 *
	 * @param facetName
	 *            the facet name being assigned
	 * @param context
	 *            the description context for error reporting
	 * @param receiverDescription
	 *            human-readable description of the receiver
	 * @param receiverType
	 *            the full receiver type (including content type)
	 * @param assigned
	 *            the expression description being assigned
	 * @param receiverContentType
	 *            the expected content type of the container
	 * @param contentType
	 *            the actual content type being assigned
	 */
	private static void emitCastingWarning(final String facetName, final IDescription context,
			final String receiverDescription, final IType<?> receiverType, final IExpressionDescription assigned,
			final IType receiverContentType, final IType<?> contentType) {
		final EObject target = assigned.getTarget();
		if (target == null) {
			context.warning("Elements of " + receiverDescription + " are of type " + receiverContentType
					+ " but are assigned elements of type " + contentType + ", which will be casted to "
					+ receiverContentType, IGamlIssue.SHOULD_CAST, facetName, receiverType.toString());
		} else {
			context.warning("Elements of " + receiverDescription + " are of type " + receiverContentType
					+ " but are assigned elements of type " + contentType + ", which will be casted to "
					+ receiverContentType, IGamlIssue.SHOULD_CAST, target, receiverType.toString());
		}
	}

	/**
	 * Checks if species types are incompatible in an assignment and reports errors if so.
	 *
	 * <p>
	 * This method validates species hierarchy constraints when assigning species values. In GAML, you can only assign a
	 * species to a variable of type {@code species<X>} if the assigned species is X itself or a sub-species of X.
	 * </p>
	 *
	 * <h3>Validation Rule:</h3>
	 * <p>
	 * For assignments to {@code species<Parent>} variables:
	 * </p>
	 * <ul>
	 * <li>✓ {@code species<Parent>} - Direct match (valid)</li>
	 * <li>✓ {@code species<Child>} where Child extends Parent (valid)</li>
	 * <li>✗ {@code species<Other>} where Other is not related to Parent (invalid)</li>
	 * <li>✓ {@code nil} - Null value (handled elsewhere)</li>
	 * </ul>
	 *
	 * <h3>Error Condition:</h3>
	 * <p>
	 * When the assigned species is not a sub-species of the receiver species, emits error: "Impossible assignment:
	 * {assignedSpecies} is not a sub-species of {receiverSpecies}" with issue code {@link IGamlIssue#WRONG_TYPE}.
	 * </p>
	 *
	 * @param context
	 *            the description context for error reporting
	 * @param receiverType
	 *            the full receiver type (must be {@code species<X>})
	 * @param assigned
	 *            the expression description being assigned
	 * @param expr2
	 *            the actual expression value
	 * @param receiverContentType
	 *            the expected species type (X in {@code species<X>})
	 * @param contentType
	 *            the assigned species type
	 * @return true if species are incompatible (error reported), false if compatible or not a species case
	 */
	private static boolean speciesAreIncompatible(final IDescription context, final IType<?> receiverType,
			final IExpressionDescription assigned, final IExpression expr2, final IType receiverContentType,
			final IType<?> contentType) {
		// AD: 28/4/14 special case for variables of type species<xxx>
		if (expr2 != GAML.getExpressionFactory().getNil() && receiverType.getGamlType().id() == IType.SPECIES
				&& !contentType.isTranslatableInto(receiverContentType)) {
			context.error("Impossible assignment: " + contentType.getSpeciesName() + " is not a sub-species of "
					+ receiverContentType.getSpeciesName(), IGamlIssue.WRONG_TYPE, assigned.getTarget());
			return true;
		}
		return false;
	}

	/**
	 * Immutable set of reserved keywords that cannot be used as identifiers in GAML.
	 *
	 * <p>
	 * These keywords have special meaning in the GAML language and are reserved for language constructs. Attempting to
	 * use them as variable names, action names, species names, etc., will result in a compilation error.
	 * </p>
	 *
	 * <p>
	 * Reserved keywords:
	 * </p>
	 * <ul>
	 * <li>{@code false} - Boolean false literal</li>
	 * <li>{@code true} - Boolean true literal</li>
	 * <li>{@code null} - Null/nil literal</li>
	 * <li>{@code myself} - Special reference to the current agent</li>
	 * </ul>
	 *
	 * @see #nameIsValid
	 */
	public static Set<String> RESERVED = Set.of(IKeyword.FALSE, IKeyword.TRUE, IKeyword.NULL, IKeyword.MYSELF);

	/**
	 * Validates that a description's name is valid and not reserved or conflicting with type names.
	 *
	 * <p>
	 * This method performs three critical validations on identifier names:
	 * </p>
	 *
	 * <h3>Validation Checks:</h3>
	 * <ol>
	 * <li><strong>Name Presence:</strong> The name attribute must exist and be non-null</li>
	 * <li><strong>Reserved Keywords:</strong> Name cannot be a reserved keyword (see {@link #RESERVED})</li>
	 * <li><strong>Type Name Conflicts:</strong> Name cannot conflict with existing type or species names</li>
	 * </ol>
	 *
	 * <h3>Error Conditions:</h3>
	 * <ul>
	 * <li><strong>Missing Name ({@link IGamlIssue#MISSING_NAME}):</strong> The 'name' attribute is missing</li>
	 * <li><strong>Reserved Keyword ({@link IGamlIssue#IS_RESERVED}):</strong> Name matches a reserved keyword</li>
	 * <li><strong>Type Conflict ({@link IGamlIssue#IS_A_TYPE}):</strong> Name conflicts with type or species</li>
	 * </ul>
	 *
	 * <h3>Context-Aware Messaging:</h3>
	 * <p>
	 * Error messages adapt based on the description type:
	 * </p>
	 * <ul>
	 * <li>For {@link IVariableDescription}: "...cannot be used as a variable name"</li>
	 * <li>For other descriptions: "...cannot be used as a {keyword} name"</li>
	 * </ul>
	 *
	 * <h3>Usage Example:</h3>
	 *
	 * <pre>{@code
	 * @Override
	 * public void validate(IDescription desc) {
	 * 	if (!Assert.nameIsValid(desc)) {
	 * 		return; // Validation failed, error already reported
	 * 	}
	 * 	// Continue with additional validation...
	 * }
	 * }</pre>
	 *
	 * @param cd
	 *            the description to validate (must have a name attribute)
	 * @return true if the name is valid, false if it's missing, reserved, or conflicts with a type
	 *
	 * @see #RESERVED
	 * @see ITypesManager#containsType
	 */
	public static boolean nameIsValid(final IDescription cd) {
		final String name = cd.getName();
		if (name == null) {
			cd.error("The attribute 'name' is missing", IGamlIssue.MISSING_NAME);
			return false;
		}
		if (RESERVED.contains(name)) {
			final String type = "It cannot be used as a "
					+ (cd instanceof IVariableDescription ? "variable" : cd.getKeyword()) + " name.";
			cd.error(name + " is a reserved keyword. " + type + " Reserved keywords are: " + RESERVED,
					IGamlIssue.IS_RESERVED, NAME, name);
			return false;
		}
		final ITypesManager manager = Types.findTypesManager(cd);
		if (!manager.containsType(name)) return true;
		final String type = "It cannot be used as a "
				+ (cd instanceof IVariableDescription ? "variable" : cd.getKeyword()) + " name.";
		final String species = manager.get(name).isAgentType() ? "species" : IKeyword.TYPE;
		cd.error(name + " is a " + species + " name. " + type, IGamlIssue.IS_A_TYPE, NAME, name);
		return false;
	}

}