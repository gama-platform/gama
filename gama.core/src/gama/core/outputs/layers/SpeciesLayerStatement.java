/*******************************************************************************************************
 *
 * SpeciesLayerStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import java.util.ArrayList;
import java.util.List;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IKeyword;
import gama.core.outputs.LayeredDisplayOutput;
import gama.core.outputs.layers.SpeciesLayerStatement.SpeciesLayerSerializer;
import gama.core.outputs.layers.SpeciesLayerStatement.SpeciesLayerValidator;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.compilation.IDescriptionValidator;
import gama.gaml.compilation.ISymbol;
import gama.gaml.compilation.annotations.serializer;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.IExpressionDescription;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.descriptions.StatementDescription;
import gama.gaml.descriptions.SymbolDescription;
import gama.gaml.descriptions.SymbolSerializer;
import gama.gaml.descriptions.TypeDescription;
import gama.gaml.factories.DescriptionFactory;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.operators.Cast;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.AspectStatement;
import gama.gaml.statements.IExecutable;
import gama.gaml.statements.IStatement;
import gama.gaml.types.IType;

/**
 * Written by drogoul Modified on 9 nov. 2009
 *
 * @todo Description
 *
 */
@symbol (
		name = IKeyword.SPECIES_LAYER,
		kind = ISymbolKind.LAYER,
		with_sequence = true,
		remote_context = true,
		concept = { IConcept.DISPLAY, IConcept.SPECIES })
@inside (
		symbols = { IKeyword.DISPLAY, IKeyword.SPECIES_LAYER })
@facets (
		value = { @facet (
				name = IKeyword.POSITION,
				type = IType.POINT,
				optional = true,
				doc = @doc ("position of the upper-left corner of the layer. Note that if coordinates are in [0,1[, the position is relative to the size of the environment (e.g. {0.5,0.5} refers to the middle of the display) whereas it is absolute when coordinates are greater than 1 for x and y. The z-ordinate can only be defined between 0 and 1. The position can only be a 3D point {0.5, 0.5, 0.5}, the last coordinate specifying the elevation of the layer. In case of negative value OpenGl will position the layer out of the environment.")),
				@facet (
						name = IKeyword.ROTATE,
						type = { IType.FLOAT },
						optional = true,
						doc = @doc ("Defines the angle of rotation of this layer, in degrees, around the z-axis.")),
				@facet (
						name = IKeyword.SELECTABLE,
						type = { IType.BOOL },
						optional = true,
						doc = @doc ("Indicates whether the agents present on this layer are selectable by the user. Default is true")),
				@facet (
						name = IKeyword.SIZE,
						type = IType.POINT,
						optional = true,
						doc = @doc ("extent of the layer in the screen from its position. Coordinates in [0,1[ are treated as percentages of the total surface, while coordinates > 1 are treated as absolute sizes in model units (i.e. considering the model occupies the entire view). Like in 'position', an elevation can be provided with the z coordinate, allowing to scale the layer in the 3 directions ")),
				@facet (
						name = IKeyword.TRANSPARENCY,
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("the transparency level of the layer (between 0 -- opaque -- and 1 -- fully transparent)")),
				@facet (
						name = IKeyword.TRACE,
						type = { IType.BOOL, IType.INT },
						optional = true,
						doc = @doc ("Allows to aggregate the visualization of agents at each timestep on the display. Default is false. If set to an int value, only the last n-th steps will be visualized. If set to true, no limit of timesteps is applied. ")),
				@facet (
						name = IKeyword.FADING,
						type = { IType.BOOL },
						optional = true,
						doc = @doc ("Used in conjunction with 'trace:', allows to apply a fading effect to the previous traces. Default is false")),
				@facet (
						name = IKeyword.VISIBLE,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Defines whether this layer is visible or not")),
				@facet (
						name = IKeyword.SPECIES,
						type = IType.SPECIES,
						optional = false,
						doc = @doc ("the species to be displayed")),
				@facet (
						name = IKeyword.ASPECT,
						type = IType.ID,
						optional = true,
						doc = @doc ("the name of the aspect that should be used to display the species")),
				@facet (
						name = IKeyword.REFRESH,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("(openGL only) specify whether the display of the species is refreshed. (true by default, usefull in case of agents that do not move)")) },
		omissible = IKeyword.SPECIES)
@doc (
		value = "The `" + IKeyword.SPECIES_LAYER
				+ "` statement is used using the `species keyword`. It allows modeler to display all the agent of a given species in the current display. In particular, modeler can choose the aspect used to display them.",
		usages = { @usage (
				value = "The general syntax is:",
				examples = { @example (
						value = "display my_display {",
						isExecutable = false),
						@example (
								value = "   species species_name [additional options];",
								isExecutable = false),
						@example (
								value = "}",
								isExecutable = false) }),
				@usage (
						value = "Species can be superposed on the same plan (be careful with the order, the last one will be above all the others):",
						examples = { @example (
								value = "display my_display {",
								isExecutable = false),
								@example (
										value = "   species agent1 aspect: base;",
										isExecutable = false),
								@example (
										value = "   species agent2 aspect: base;",
										isExecutable = false),
								@example (
										value = "   species agent3 aspect: base;",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }),
				@usage (
						value = "Each species layer can be placed at a different z value using the opengl display. position:{0,0,0} means the layer will be placed on the ground and position:{0,0,1} means it will be placed at an height equal to the maximum size of the environment.",
						examples = { @example (
								value = "display my_display type: opengl{",
								isExecutable = false),
								@example (
										value = "   species agent1 aspect: base ;",
										isExecutable = false),
								@example (
										value = "   species agent2 aspect: base position:{0,0,0.5};",
										isExecutable = false),
								@example (
										value = "   species agent3 aspect: base position:{0,0,1};",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }) },
		see = { IKeyword.DISPLAY, IKeyword.AGENTS, IKeyword.CHART, IKeyword.EVENT, "graphics", IKeyword.GRID_LAYER,
				IKeyword.IMAGE, IKeyword.OVERLAY })
@serializer (SpeciesLayerSerializer.class)
@validator (SpeciesLayerValidator.class)
public class SpeciesLayerStatement extends AgentLayerStatement {

	/**
	 * The Class SpeciesLayerSerializer.
	 */
	public static class SpeciesLayerSerializer extends SymbolSerializer<StatementDescription> {

		@Override
		protected void serializeKeyword(final SymbolDescription desc, final StringBuilder sb,
				final boolean includingBuiltIn) {
			sb.append("species ");
		}

	}

	/**
	 * The Class SpeciesLayerValidator.
	 */
	public static class SpeciesLayerValidator implements IDescriptionValidator<StatementDescription> {

		@Override
		public void validate(final StatementDescription description) {
			TypeDescription target = description.getGamlType().getDenotedSpecies();
			if (!(target instanceof SpeciesDescription sd)) // Already caught by the type checking
				return;
			final IExpressionDescription ed = description.getFacet(ASPECT);
			if (ed != null) {
				final String a = description.getLitteral(ASPECT);
				if (sd.getAspect(a) != null) {
					ed.compileAsLabel();
				} else {
					description.error(a + " is not the name of an aspect of " + target.getName(), IGamlIssue.GENERAL,
							ed.getTarget());
				}

			}
		}

	}

	/** The aspect. */
	private IExecutable aspect;

	/** The host species. */
	protected ISpecies hostSpecies;

	/** The species. */
	protected ISpecies species;

	/** The micro species layers. */
	protected List<SpeciesLayerStatement> microSpeciesLayers;

	/**
	 * Instantiates a new species layer statement.
	 *
	 * @param desc
	 *            the desc
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public SpeciesLayerStatement(final IDescription desc) throws GamaRuntimeException {
		super(desc);
		setName(getFacet(IKeyword.SPECIES).literalValue());
	}

	@Override
	public boolean _init(final IScope scope) throws GamaRuntimeException {
		// top level species layer is a direct micro-species of "world_species"
		// for sure
		if (hostSpecies == null && scope.getSimulation() != null) { hostSpecies = scope.getSimulation().getSpecies(); }
		species = Cast.asSpecies(scope, getFacet(IKeyword.SPECIES).value(scope));
		if (species == null && hostSpecies != null) { species = hostSpecies.getMicroSpecies(getName()); }
		if (species == null) throw GamaRuntimeException.error("not a suitable species to display: " + getName(), scope);
		if (super._init(scope) && microSpeciesLayers != null) {
			for (final SpeciesLayerStatement microLayer : microSpeciesLayers) {
				microLayer.hostSpecies = species;
				if (!scope.init(microLayer).passed()) return false;
			}
		}
		return true;
	}

	@Override
	public boolean _step(final IScope scope) throws GamaRuntimeException {
		return true;
	}

	@Override
	public LayerType getType(final LayeredDisplayOutput output) {
		return LayerType.SPECIES;
	}

	/**
	 * Gets the aspects.
	 *
	 * @return the aspects
	 */
	public List<String> getAspects() { return species.getAspectNames(); }

	@Override
	public void setAspect(final String currentAspect) {
		super.setAspect(currentAspect);
		aspect = species.getAspect(constantAspectName);
	}

	@Override
	public void computeAspectName(final IScope sim) throws GamaRuntimeException {
		if (aspect != null) return;
		super.computeAspectName(sim);
		aspect = species.getAspect(constantAspectName);
	}

	@Override
	public IExecutable getAspect() { return aspect; }

	/**
	 * Gets the species.
	 *
	 * @return the species
	 */
	public ISpecies getSpecies() { return species; }

	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {
		final List<IStatement> aspectStatements = new ArrayList<>();

		for (final ISymbol c : commands) {
			if (c instanceof SpeciesLayerStatement) {
				if (microSpeciesLayers == null) { microSpeciesLayers = new ArrayList<>(); }
				microSpeciesLayers.add((SpeciesLayerStatement) c);
			} else {
				aspectStatements.add((IStatement) c);
			}
		}
		if (!aspectStatements.isEmpty()) {
			constantAspectName = "inline";
			final IDescription d =
					DescriptionFactory.create(IKeyword.ASPECT, getDescription(), IKeyword.NAME, "inline");
			aspect = new AspectStatement(d);
			((AspectStatement) aspect).setChildren(aspectStatements);
		}
	}

	/**
	 * Returns a list of micro-species layers declared as sub-layers.
	 *
	 * @return
	 */
	public List<SpeciesLayerStatement> getMicroSpeciesLayers() { return microSpeciesLayers; }

	@Override
	public String toString() {
		return "SpeciesDisplayLayer species: " + getName();
	}
}
