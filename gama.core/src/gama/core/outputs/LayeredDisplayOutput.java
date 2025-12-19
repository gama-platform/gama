/*******************************************************************************************************
 *
 * LayeredDisplayOutput.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Iterables;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.facet;
import gama.annotations.precompiler.GamlAnnotations.facets;
import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.annotations.precompiler.GamlAnnotations.symbol;
import gama.annotations.precompiler.GamlAnnotations.usage;
import gama.annotations.precompiler.GamlProperties;
import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.ISymbolKind;
import gama.core.common.interfaces.IDisplayCreator.DisplayDescription;
import gama.core.common.interfaces.IDisplaySurface;
import gama.core.common.interfaces.IGamaView;
import gama.core.common.interfaces.IGamaView.Display;
import gama.core.common.interfaces.IGui;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.preferences.GamaPreferences;
import gama.core.outputs.LayeredDisplayOutput.DisplaySerializer;
import gama.core.outputs.LayeredDisplayOutput.DisplayValidator;
import gama.core.outputs.layers.AbstractLayerStatement;
import gama.core.outputs.layers.ILayerStatement;
import gama.core.outputs.layers.properties.CameraStatement;
import gama.core.outputs.layers.properties.LightStatement;
import gama.core.outputs.layers.properties.RotationStatement;
import gama.core.runtime.IScope;
import gama.core.runtime.IScope.IGraphicsScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.dev.DEBUG;
import gama.gaml.compilation.IDescriptionValidator;
import gama.gaml.compilation.ISymbol;
import gama.gaml.compilation.annotations.serializer;
import gama.gaml.compilation.annotations.validator;
import gama.gaml.descriptions.ConstantExpressionDescription;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.IExpressionDescription;
import gama.gaml.descriptions.SymbolDescription;
import gama.gaml.descriptions.SymbolSerializer;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.IExpressionFactory;
import gama.gaml.interfaces.IGamlIssue;
import gama.gaml.statements.Facets;
import gama.gaml.types.IType;

/**
 * The Class LayerDisplayOutput.
 *
 * @author drogoul
 */
@symbol (
		name = { IKeyword.DISPLAY },
		kind = ISymbolKind.OUTPUT,
		with_sequence = true,
		concept = { IConcept.DISPLAY })
@facets (
		value = { @facet (
				name = IKeyword.VIRTUAL,
				type = IType.BOOL,
				optional = true,
				doc = @doc ("Declaring a display as virtual makes it invisible on screen, and only usable for display inheritance")),
				@facet (
						name = IKeyword.PARENT,
						type = IType.ID,
						optional = true,
						doc = @doc ("Declares that this display inherits its layers and attributes from the parent display named as the argument. Expects the identifier of the parent display or a string if the name of the parent contains spaces")),
				@facet (
						name = IKeyword.BACKGROUND,
						type = IType.COLOR,
						optional = true,
						doc = @doc ("Allows to fill the background of the display and its toolbar with a specific color. Beware that this color, used in the UI, will not be affected by the light used in the display.")),
				@facet (
						name = IKeyword.NAME,
						type = IType.LABEL,
						optional = false,
						doc = @doc ("the identifier of the display")),
				// WARNING VALIDER EN VERIFIANT LE TYPE DU DISPLAY
				@facet (
						name = IKeyword.TYPE,
						type = IType.LABEL,
						optional = true,
						doc = @doc ("Allows to use either Java2D (for planar models) or OpenGL (for 3D models) as the rendering subsystem")),
				@facet (
						name = "antialias",
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Indicates whether to use advanced antialiasing for the display or not. The default value is the one indicated in the preferences of GAMA ('false' is its factory default). Antialising produces smoother outputs, but comes with a cost in terms of speed and memory used. ")),

				@facet (
						name = IKeyword.REFRESH,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Indicates the condition under which this output should be refreshed (default is true)")),
				@facet (
						name = IKeyword.TOOLBAR,
						type = { IType.BOOL, IType.COLOR },
						optional = true,
						doc = @doc ("Indicates whether the top toolbar of the display view should be initially visible or not. If a color is passed, then the background of the toolbar takes this color")),
				@facet (
						name = IKeyword.FULLSCREEN,
						type = { IType.BOOL, IType.INT },
						optional = true,
						doc = @doc ("Indicates, when using a boolean value, whether or not the display should cover the whole screen (default is false). If an integer is passed, specifies also the screen to use: 0 for the primary monitor, 1 for the secondary one, and so on and so forth. If the monitor is not available, the first one is used")),
				@facet (
						name = "show_fps",
						internal = true,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Allows to enable/disable the drawing of the number of frames per second")),
				@facet (
						name = "axes",
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Allows to enable/disable the drawing of the world shape and the ordinate axes. Default can be configured in Preferences")),
				@facet (
						name = IKeyword.ORTHOGRAPHIC_PROJECTION,
						internal = true,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Allows to enable/disable the orthographic projection. Default can be configured in Preferences")),

				/// LIGHT FACETS
				@facet (
						name = IKeyword.IS_LIGHT_ON,
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Allows to enable/disable the light at once. Default is true")),
				@facet (
						name = "locked",
						type = IType.BOOL,
						optional = true,
						doc = @doc ("Allows to lock/unlock a 2D display when it opens. For 3D displays please use the `camera` statement")),
				@facet (
						name = "draw_diffuse_light",
						type = IType.BOOL,
						optional = true,
						doc = @doc (
								deprecated = "Define it in the various 'light' statements instead with 'show: true/false'",
								value = "Allows to show/hide a representation of the lights. Default is false.")),

				/// CAMERA FACETS
				@facet (
						name = IKeyword.CAMERA,
						type = IType.STRING,
						optional = true,
						doc = @doc ("""
								Allows to define the name of the camera to use. Default value is 'default'. \
								Accepted values are (1) the name of one of the cameras defined using the 'camera' statement or \
								(2) one of the preset cameras, accessible using constants: #from_above, #from_left, #from_right, \
								#from_up_left, #from_up_right, #from_front, #from_up_front, #isometric""")),
				/// END CAMERA FACETS

				@facet (
						name = IKeyword.KEYSTONE,
						type = IType.CONTAINER,
						optional = true,
						doc = @doc ("Set the position of the 4 corners of your screen ([topLeft,topRight,botLeft,botRight]), in (x,y) coordinate ( the (0,0) position is the top left corner, while the (1,1) position is the bottom right corner). The default value is : [{0,0},{1,0},{0,1},{1,1}]")),
				@facet (
						name = "z_near",
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("Set the distances to the near depth clipping planes. Must be positive.")),
				@facet (
						name = "z_far",
						type = IType.FLOAT,
						optional = true,
						doc = @doc ("Set the distances to the far depth clipping planes. Must be positive.")),
				@facet (
						name = IKeyword.AUTOSAVE,
						type = { IType.BOOL, IType.POINT, IType.STRING },
						optional = true,
						doc = @doc ("""
								Allows to save this display on disk. This facet accepts bool, point or string values. \
								If it is false or nil, nothing happens. 'true' will save it at a resolution of 500x500 with a standard name \
								(containing the name of the model, display, resolution, cycle and time). A non-nil point will change that resolution. A non-nil string will keep 500x500 and change the filename \
								(if it is not dynamically built, the previous file will be erased). Note that setting autosave to true in a display will synchronize all the displays defined in the experiment""")), },
		omissible = IKeyword.NAME)
@inside (
		symbols = { IKeyword.OUTPUT, IKeyword.PERMANENT })
@validator (DisplayValidator.class)
@serializer (DisplaySerializer.class)
@doc (
		value = "A display refers to an independent and mobile part of the interface that can display species, images, texts or charts.",
		usages = { @usage (
				value = "The general syntax is:",
				examples = @example (
						value = "display my_display [additional options] { ... }",
						isExecutable = false)),
				@usage (
						value = "Each display can include different layers (like in a GIS).",
						examples = { @example (
								value = "display gridWithElevationTriangulated type: opengl {",
								isExecutable = false),
								@example (
										value = "	grid cell elevation: true triangulation: true;",
										isExecutable = false),
								@example (
										value = "	species people aspect: base;",
										isExecutable = false),
								@example (
										value = "}",
										isExecutable = false) }) })
public class LayeredDisplayOutput extends AbstractOutput {

	static {
		DEBUG.OFF();
	}

	/** The layers. */
	private final List<AbstractLayerStatement> layers;

	/** The cameras. */
	private final List<CameraStatement> cameras;

	/** The lights. */
	private final List<LightStatement> lights;

	/** The rotation. */
	private RotationStatement rotation;

	/** The surface. */
	protected IDisplaySurface surface;

	/** The index. */
	private int index;

	/** The data. */
	private final LayeredDisplayData data = new LayeredDisplayData();

	/**
	 * The Class DisplaySerializer.
	 */
	public static class DisplaySerializer extends SymbolSerializer<SymbolDescription> {

		/**
		 * Method collectPluginsInFacetValue()
		 *
		 * @see gama.gaml.descriptions.SymbolSerializer#collectPluginsInFacetValue(gama.gaml.descriptions.SymbolDescription,
		 *      java.lang.String, java.util.Set)
		 */
		@Override
		protected void collectMetaInformationInFacetValue(final SymbolDescription desc, final String key,
				final GamlProperties plugins) {
			super.collectMetaInformationInFacetValue(desc, key, plugins);
			if (TYPE.equals(key)) {
				final IExpressionDescription exp = desc.getFacet(TYPE);
				if (exp.getExpression() != null) {
					final String type = exp.getExpression().literalValue();
					final DisplayDescription dd = gama.core.runtime.GAMA.getGui().getDisplayDescriptionFor(type);
					if (dd != null) { plugins.put(GamlProperties.PLUGINS, dd.getDefiningPlugin()); }
				}
			}
		}

	}

	/**
	 * The Class InfoValidator.
	 */
	public static class DisplayValidator implements IDescriptionValidator<IDescription> {

		/**
		 * Method validate()
		 *
		 * @see gama.gaml.compilation.IDescriptionValidator#validate(gama.gaml.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription d) {

			handleInheritance(d);

			final IExpressionDescription auto = d.getFacet(AUTOSAVE);
			if (auto != null && auto.getExpression().isConst() && TRUE.equals(auto.getExpression().literalValue())) {
				d.info("With autosave enabled, GAMA must remain the frontmost window and the display must not be covered or obscured by other windows",
						IGamlIssue.GENERAL, auto.getTarget(), AUTOSAVE);
			}
			// Are we in OpenGL world ?
			IExpressionDescription type = d.getFacet(TYPE);
			final boolean isOpenGLDefault = !IKeyword._2D.equals(GamaPreferences.Displays.CORE_DISPLAY.getValue());
			if (type == null) {
				type = ConstantExpressionDescription.create(isOpenGLDefault ? IKeyword._3D : IKeyword._2D);
				d.setFacetExprDescription(TYPE, type);
			}
			String cand = "";
			// Addresses and fixes Issue 833.
			final String s = type.getExpression().literalValue();
			if (!IGui.DISPLAYS.containsKey(s) && !gama.core.runtime.GAMA.isInHeadLessMode()) {
				// In headless mode, all displays should be accepted
				cand = IGui.DISPLAYS.keySet().stream().findFirst().get();

				d.warning(
						s + " is not a valid display type. Valid types are:" + IGui.DISPLAYS.keySet()
								+ ". Gama will fallback to first valid type (" + cand + ")",
						IGamlIssue.UNKNOWN_KEYWORD, TYPE);

				d.setFacet(TYPE, ConstantExpressionDescription.create(cand));
			}

			// Addressing problems with charts in OpenGL
			if (IKeyword._3D.equals(type.getExpression().literalValue()) && !Iterables.isEmpty(d.getOwnChildren())
					&& d.visitOwnChildren(c -> (CHART.equals(c.getKeyword()) || EVENT.equals(c.getKeyword())))) {
				d.warning("Consider switching to a 2d display if you only display charts", CONFLICTING_FACETS, TYPE);
			}
		}

		/**
		 * @param d
		 */
		private void handleInheritance(final IDescription d) {
			final IExpressionDescription parentExp = d.getFacet(PARENT);
			if (parentExp == null) return;
			if (!parentExp.getExpression().isConst()) {
				d.error("The parent display name must be a constant");
				return;
			}
			final String string = parentExp.getExpression().literalValue();
			handleInheritance(d, string);
		}

		/**
		 * Handle inheritance.
		 *
		 * @param d
		 *            the d
		 * @param string
		 *            the string
		 */
		private void handleInheritance(final IDescription d, final String string) {
			final IDescription output = d.getEnclosingDescription();
			for (final IDescription parentDisplay : output.getChildrenWithKeyword(DISPLAY)) {
				if (parentDisplay.getName().equals(string)) {
					handleInheritance(parentDisplay);
					handleInheritanceBetween(d, parentDisplay);
					return;
				}
			}
			d.error("No parent display named '" + string + "' found");
		}

		/**
		 * Handle inheritance.
		 *
		 * @param child
		 *            the child
		 * @param parent
		 *            the parent
		 */
		private void handleInheritanceBetween(final IDescription child, final IDescription parent) {
			final Facets childFacets = child.getFacets();
			final boolean hasVirtual = childFacets.containsKey(VIRTUAL);
			final Facets parentFacets = parent.getFacets();
			childFacets.complementWith(parentFacets);
			if (!hasVirtual) { childFacets.remove(VIRTUAL); }
			child.replaceChildrenWith(Iterables.concat(parent.getOwnChildren(), child.getOwnChildren()));

		}

	}

	/**
	 * Instantiates a new layered display output.
	 *
	 * @param desc
	 *            the desc
	 */
	public LayeredDisplayOutput(final IDescription desc) {
		super(desc);
		if (hasFacet(IKeyword.TYPE)) { getData().setDisplayType(getLiteral(IKeyword.TYPE)); }
		layers = new CopyOnWriteArrayList<>();
		cameras = new ArrayList<>();
		lights = new ArrayList<>();
	}

	@Override
	public boolean init(final IScope scope) throws GamaRuntimeException {
		final boolean result = super.init(scope);
		if (!result) return false;
		if (rotation != null) {
			getData().setRotation(rotation.getDefinition());
			rotation.init(scope);
		}
		for (CameraStatement s : cameras) {
			getData().addCameraDefinition(s.getDefinition());
			s.init(scope);
		}
		for (LightStatement s : lights) {
			getData().addLightDefinition(s.getDefinition());
			s.init(scope);
		}
		getData().initWith(getScope(), description);

		for (final ILayerStatement layer : getLayers()) {
			layer.setDisplayOutput(this);
			if (!getScope().init(layer).passed()) return false;
		}
		//
		// final IExpression sync = getFacet("synchronized");
		// if (sync != null) { setSynchronized(Cast.asBool(getScope(),
		// sync.value(getScope()))); }

		createSurface(getScope());
		return true;
	}

	@Override
	public boolean step(final IScope scope) throws GamaRuntimeException {
		for (final ILayerStatement layer : getLayers()) {
			try {
				getScope().setCurrentSymbol(layer);
				getScope().step(layer);
			} finally {
				scope.setCurrentSymbol(null);
			}
		}
		return true;
	}

	@Override
	public void update() throws GamaRuntimeException {

		if (surface == null) return;
		// DEBUG.OUT("Entering update of the output");
		getData().update(getScope(), description.getFacets());

		super.update();
		// See #3696
		// if (!surface.shouldWaitToBecomeRendered()) { setRendered(true); }
	}

	@Override
	public void dispose() {
		if (disposed) return;
		// setSynchronized(false);
		super.dispose();
		if (surface != null) { surface.dispose(); }
		surface = null;
		getLayers().clear();
		getData().dispose();
	}

	/**
	 * Creates the surface.
	 *
	 * @param scope
	 *            the scope
	 */
	protected void createSurface(final IScope scope) {
		if (surface != null) {
			surface.outputReloaded();
			return;
		}
		if (scope.getExperiment().isHeadless()) {
			// If in headless mode, we need to get the 'image' surface
			getData().setDisplayType(IKeyword.IMAGE);
		} else if (getData().is3D()) return;
		surface = scope.getGui().createDisplaySurfaceFor(this);
	}

	@Override
	public String getViewId() {
		if (getData().isWeb()) return IGui.GL_LAYER_VIEW_ID3;
		if (getData().isOpenGL2()) return IGui.GL_LAYER_VIEW_ID2;
		if (getData().is3D()) return IGui.GL_LAYER_VIEW_ID;
		return IGui.LAYER_VIEW_ID;
	}

	/**
	 * Gets the surface.
	 *
	 * @return the surface
	 */
	public IDisplaySurface getSurface() { return surface; }

	@Override
	public List<? extends ISymbol> getChildren() { return getLayers(); }

	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {
		final List<AbstractLayerStatement> list = new ArrayList<>();
		for (final ISymbol s : commands) {
			switch (s) {
				case CameraStatement cs -> cameras.add(cs);
				case RotationStatement rs -> rotation = rs;
				case LightStatement ls -> lights.add(ls);
				case null, default -> {
					list.add((AbstractLayerStatement) s);
				}
			}

		}
		setLayers(list);

	}

	/**
	 * Sets the surface.
	 *
	 * @param surface
	 *            the new surface
	 */
	public void setSurface(final IDisplaySurface surface) {
		this.surface = surface;
		if (surface == null) { view = null; }
	}

	/**
	 * Gets the image.
	 *
	 * @return the image
	 */
	public BufferedImage getImage() {
		return surface == null ? null : surface.getImage(surface.getWidth(), surface.getHeight());
	}

	/**
	 * Gets the image.
	 *
	 * @param w
	 *            the w
	 * @param h
	 *            the h
	 * @return the image
	 */
	public BufferedImage getImage(final int w, final int h) {
		return surface == null ? null : surface.getImage(w, h);
	}

	/**
	 * Sets the layers.
	 *
	 * @param layers
	 *            the new layers
	 */
	public void setLayers(final List<AbstractLayerStatement> layers) {
		this.layers.clear();
		this.layers.addAll(layers);
	}

	/**
	 * Gets the layers.
	 *
	 * @return the layers
	 */
	public List<AbstractLayerStatement> getLayers() { return layers; }

	@Override
	public void setPaused(final boolean paused) {
		final boolean wasPaused = isPaused();
		super.setPaused(paused);
		if (surface == null) return;
		if (getData().is3D()) { ((IDisplaySurface.OpenGL) surface).setPaused(paused); }
		if (wasPaused && !paused) { surface.updateDisplay(false, null); }
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	public LayeredDisplayData getData() {
		return data; // .get();
	}

	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public int getIndex() { return index; }

	/**
	 * Sets the index.
	 *
	 * @param index
	 *            the new index
	 */
	public void setIndex(final int index) { this.index = index; }

	@Override
	public boolean isAutoSave() {
		final IExpression e = getFacet(IKeyword.AUTOSAVE);
		if (e == null || e == IExpressionFactory.FALSE_EXPR) return false;
		return true;
	}

	/**
	 * Zoom.
	 *
	 * @param mode
	 *            the mode
	 */
	public void zoom(final int mode) {
		if (mode < 0) {
			surface.zoomOut();
		} else if (mode == 0) {
			surface.zoomFit();
		} else {
			surface.zoomIn();
		}
	}

	@Override
	public IGamaView.Display getView() { return (Display) super.getView(); }

	/**
	 * Release view.
	 */
	public void releaseView() {
		view = null;
	}

	@Override
	protected IGraphicsScope buildScopeFrom(final IScope scope) {
		return scope.copyForGraphics("of " + getDescription().getKeyword() + " " + getName());
	}

	@Override
	public IGraphicsScope getScope() { return (IGraphicsScope) super.getScope(); }

	@Override
	protected boolean shouldOpenView() {
		return !IKeyword.IMAGE.equals(getData().getDisplayType());
	}

	/**
	 * Link scope with graphics.
	 */
	public void linkScopeWithGraphics() {
		IGraphicsScope scope = getScope();
		IDisplaySurface surface = getSurface();
		if (scope == null || surface == null) return;
		scope.setGraphics(surface.getIGraphics());
	}

	// @Override
	// public void setRendered(final boolean b) { rendered = b; }
	//
	// @Override
	// public boolean isRendered() {
	// if (view != null && !view.isVisible()) return true;
	// if (!this.isRefreshable() || !this.isOpen() || this.isPaused()) return
	// true;
	// return rendered;
	// }
}
