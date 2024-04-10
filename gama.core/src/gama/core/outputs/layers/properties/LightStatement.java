/*******************************************************************************************************
 *
 * LightStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.properties;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.core.common.interfaces.IKeyword;
import gama.core.outputs.LayeredDisplayOutput;
import gama.core.outputs.layers.AbstractLayerStatement;
import gama.core.outputs.layers.properties.LightStatement.LightStatementValidator;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.IExpressionDescription;
import gama.gaml.descriptions.StatementDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.types.IType;

/**
 * The Class LightStatement.
 */
@symbol (
		name = "light",
		kind = ISymbolKind.LAYER,
		with_sequence = true,
		concept = { IConcept.LIGHT, IConcept.THREED })
@inside (
		symbols = IKeyword.DISPLAY)
@validator (LightStatementValidator.class)
@facets (
		omissible = IKeyword.NAME,
		value = { @facet (
				name = IKeyword.NAME,
				type = IType.STRING,
				optional = false,
				doc = @doc ("The name of the light source, must be unique (otherwise the last definition prevails). "
						+ "Will be used to populate a menu where light sources can be easily turned on and off. Special names can be used:"
						+ "Using the special constant #ambient will allow to redefine or control the ambient light intensity and presence"
						+ "Using the special constant #default will replace the default directional light of the surrounding display")),
				@facet (
						name = IKeyword.LOCATION,
						type = IType.POINT,
						optional = true,
						doc = @doc ("the location of the light (only for point and spot light) in model coordinates. Default is {0,0,20}")),
				@facet (
						name = IKeyword.TYPE,
						type = IType.STRING,
						optional = true,
						doc = @doc ("the type of light to create. A value among {#point, #direction, #spot}")),
				@facet (
						name = IKeyword.DIRECTION,
						type = IType.POINT,
						optional = true,
						doc = @doc ("the direction of the light (only for direction and spot light). (default value : {0.5,0.5,-1})")),
				@facet (
						name = "angle",
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("the angle of the spot light in degree (only for spot light). (default value : 45)")),
				@facet (
						name = IKeyword.LINEAR_ATTENUATION,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("the linear attenuation of the positionnal light. (default value : 0)")),
				@facet (
						name = IKeyword.QUADRATIC_ATTENUATION,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("the quadratic attenuation of the positionnal light. (default value : 0)")),
				@facet (
						name = "active",
						type = IType.BOOL,
						optional = true,
						doc = @doc ("a boolean expression telling if the light is on or off. (default value if not specified : true)")),
				@facet (
						name = IKeyword.INTENSITY,
						type = { IType.INT, IType.COLOR },
						optional = true,
						doc = @doc ("an int / rgb / rgba value to specify either the color+intensity of the light or simply its intensity. (default value if not specified can be set in the Preferences. If not, it is equal to: (160,160,160,255) ).")),
				@facet (
						name = "show",
						type = { IType.BOOL },
						optional = true,
						doc = @doc ("If true, draws the light source. (default value if not specified : false).")),
				@facet (
						name = IKeyword.DYNAMIC,
						type = { IType.BOOL },
						optional = true,
						doc = @doc ("specify if the parameters of the light need to be updated every cycle or treated as constants. (default value : true).")) })
@doc (
		value = "`light` allows to define diffusion lights in your 3D display. They must be given a name, which will help track them in the UI. Two names have however special meanings: #ambient, "
				+ "which designates the ambient luminosity and color of the scene (with a default intensity of (160,160,160,255) or the value set in the Preferences) and #default, "
				+ "which designates the default directional light applied to a scene (with a default medium intensity of (160,160,160,255) or the value set in the Preferences in the direction given by (0.5,0.5,1)). Redefining a light named #ambient or #regular "
				+ "will then modify these default lights (for example changing their color or deactivating them). To be more precise, and given all the default values of the facets, the existence of these two lights is effectively equivalent to redefining:"
				+ "light #ambient intensity: gama.pref_display_light_intensity; light #default type: #direction intensity: gama.pref_display_light_intensity direction: {0.5,0.5,-1};",
		usages = { @usage (
				value = "The general syntax is:",
				examples = { @example (
						value = "light 1 type:point location:{20,20,20} color:255, linear_attenuation:0.01 quadratic_attenuation:0.0001 show:true dynamic:false;",
						isExecutable = false),
						@example (
								value = "light 'spot1' type: #spot location:{20,20,20} direction:{0,0,-1} color:255 angle:25 linear_attenuation:0.01 quadratic_attenuation:0.0001 draw:true dynamic: false;",
								isExecutable = false),
						@example (
								value = "light 'point2' type: #point direction:{1,1,-1} color:255 draw:true dynamic: false;",
								isExecutable = false) }) },
		see = { IKeyword.DISPLAY })

public class LightStatement extends AbstractLayerStatement {

	/**
	 * The Class LightStatementValidator.
	 */
	public static class LightStatementValidator extends OpenGLSpecificLayerValidator {

		/**
		 * Method validate()
		 *
		 * @see gama.gaml.compilation.IDescriptionValidator#validate(gama.gaml.descriptions.IDescription)
		 */
		@Override
		public void validate(final StatementDescription desc) {
			super.validate(desc);

			final IExpressionDescription position = desc.getFacet(IKeyword.LOCATION);
			final IExpressionDescription direction = desc.getFacet(IKeyword.DIRECTION);
			final IExpressionDescription spotAngle = desc.getFacet(IKeyword.ANGLE);
			final IExpressionDescription linearAttenuation = desc.getFacet(IKeyword.LINEAR_ATTENUATION);
			final IExpressionDescription quadraticAttenuation = desc.getFacet(IKeyword.QUADRATIC_ATTENUATION);

			final IExpression spec = desc.getFacetExpr(IKeyword.TYPE);
			if (spec != null && spec.isConst()) {
				final String type = spec.literalValue();
				// light type direction
				switch (type) {
					case ILightDefinition.point: {
						if (direction != null) {
							desc.warning("a point light has no direction (only a position)", GENERAL);
						}
						if (spotAngle != null) {
							desc.warning("a point light has no spot angle (only a spot light does !)", GENERAL);
						}
						break;
					}
					case ILightDefinition.spot:
						break;
					case ILightDefinition.direction: {
						if (position != null) {
							desc.warning("a direction light has no position (only a direction)", GENERAL);
						}
						if (linearAttenuation != null) { desc.error("a direction light has no attenuation", GENERAL); }
						if (quadraticAttenuation != null) {
							desc.warning("a direction light has no attenuation", GENERAL);
						}
						break;
					}
					case ILightDefinition.ambient:
						break;
				}

			}
		}

	}

	/** The definition. */
	final LightDefinition definition;

	/**
	 * Instantiates a new light statement.
	 *
	 * @param desc
	 *            the desc
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public LightStatement(final IDescription desc) throws GamaRuntimeException {
		super(desc);
		definition = new LightDefinition(this);
	}

	/**
	 * Inits the.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	@Override
	protected boolean _init(final IScope scope) {
		definition.refresh(scope);
		return true;
	}

	/**
	 * Gets the type.
	 *
	 * @param output
	 *            the output
	 * @return the type
	 */
	@Override
	public LayerType getType(final LayeredDisplayOutput output) {
		return LayerType.LIGHT;
	}

	/**
	 * Step.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	@Override
	protected boolean _step(final IScope scope) {
		definition.refresh(scope);
		return true;
	}

	/**
	 * Gets the definition.
	 *
	 * @return the definition
	 */
	public LightDefinition getDefinition() { return definition; }

}
