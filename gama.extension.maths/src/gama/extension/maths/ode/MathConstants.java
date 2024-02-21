/*******************************************************************************************************
 *
 * MathConstants.java, in gaml.extensions.maths, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.maths.ode;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IConstantCategory;
import gama.annotations.precompiler.GamlAnnotations.constant;
import gama.annotations.precompiler.GamlAnnotations.doc;

/**
 * The Interface MathConstants.
 */
public interface MathConstants {

	/** The rk 4. */
	@constant (
			value = "rk4",
			category = { IConstantCategory.MATH },
			concept = { IConcept.EQUATION, IConcept.CONSTANT },
			doc = @doc ("rk4 solver")) String rk4 = "rk4";

	/** The Euler. */
	@constant (
			value = "Euler",
			category = { IConstantCategory.MATH },
			concept = { IConcept.EQUATION, IConcept.CONSTANT },
			doc = @doc ("Euler solver")) String Euler = "Euler";

	/** The Three eighthes. */
	@constant (
			value = "ThreeEighthes",
			category = { IConstantCategory.MATH },
			concept = { IConcept.EQUATION, IConcept.CONSTANT },
			doc = @doc ("ThreeEighthes solver")) String ThreeEighthes = "ThreeEighthes";

	/** The Midpoint. */
	@constant (
			value = "Midpoint",
			category = { IConstantCategory.MATH },
			concept = { IConcept.EQUATION, IConcept.CONSTANT },
			doc = @doc ("Midpoint solver")) String Midpoint = "Midpoint";

	/** The Gill. */
	@constant (
			value = "Gill",
			category = { IConstantCategory.MATH },
			concept = { IConcept.EQUATION, IConcept.CONSTANT },
			doc = @doc ("Gill solver")) String Gill = "Gill";

	/** The Luther. */
	@constant (
			value = "Luther",
			category = { IConstantCategory.MATH },
			concept = { IConcept.EQUATION, IConcept.CONSTANT },
			doc = @doc ("Luther solver")) String Luther = "Luther";

	/** The dp 853. */
	@constant (
			value = "dp853",
			category = { IConstantCategory.MATH },
			concept = { IConcept.EQUATION, IConcept.CONSTANT },
			doc = @doc ("dp853 solver")) String dp853 = "dp853";

	/** The Adams bashforth. */
	@constant (
			value = "AdamsBashforth",
			category = { IConstantCategory.MATH },
			concept = { IConcept.EQUATION, IConcept.CONSTANT },
			doc = @doc ("AdamsBashforth solver")) String AdamsBashforth = "AdamsBashforth";

	/** The Adams moulton. */
	@constant (
			value = "AdamsMoulton",
			category = { IConstantCategory.MATH },
			concept = { IConcept.EQUATION, IConcept.CONSTANT },
			doc = @doc ("AdamsMoulton solver")) String AdamsMoulton = "AdamsMoulton";

	/** The Dormand prince 54. */
	@constant (
			value = "DormandPrince54",
			category = { IConstantCategory.MATH },
			concept = { IConcept.EQUATION, IConcept.CONSTANT },
			doc = @doc ("DormandPrince54 solver")) String DormandPrince54 = "DormandPrince54";

	/** The Gragg bulirsch stoer. */
	@constant (
			value = "GraggBulirschStoer",
			category = { IConstantCategory.MATH },
			concept = { IConcept.EQUATION, IConcept.CONSTANT },
			doc = @doc ("GraggBulirschStoer solver")) String GraggBulirschStoer = "GraggBulirschStoer";

	/** The Higham hall 54. */
	@constant (
			value = "HighamHall54",
			category = { IConstantCategory.MATH },
			concept = { IConcept.EQUATION, IConcept.CONSTANT },
			doc = @doc ("HighamHall54 solver")) String HighamHall54 = "HighamHall54";

}
