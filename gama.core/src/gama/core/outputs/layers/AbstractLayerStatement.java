/*******************************************************************************************************
 *
 * AbstractLayerStatement.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers;

import com.google.common.primitives.Longs;

import gama.annotations.inside;
import gama.annotations.constants.IKeyword;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IDescriptionValidator;
import gama.api.compilation.descriptions.IStatementDescription;
import gama.api.constants.IGamlIssue;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.symbols.Symbol;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IOutput;
import gama.api.ui.displays.IDisplayData;
import gama.api.ui.layers.ILayerStatement;
import gama.api.utils.prefs.GamaPreferences;
import gama.core.outputs.LayeredDisplayOutput;
import one.util.streamex.StreamEx;

/**
 * Written by drogoul Modified on 9 nov. 2009
 *
 * GAML statement to define the properties of a layer in a display
 *
 * @todo Description
 *
 */
@inside (
		symbols = IKeyword.DISPLAY)
public abstract class AbstractLayerStatement extends Symbol implements ILayerStatement {

	/**
	 * The Class SpeciesLayerValidator.
	 */
	public static class OpenGLSpecificLayerValidator implements IDescriptionValidator<IStatementDescription> {

		/**
		 * Warn if not open GL.
		 *
		 * @param description
		 *            the description
		 */
		void warnIfNotOpenGL(final IStatementDescription layer) {
			IDescription d = layer.getEnclosingDescription();
			if (!isOpenGL(d)) {
				layer.warning(layer.getKeyword() + " layers can only be used in OpenGL displays",
						IGamlIssue.WRONG_TYPE);
			}
		}

		/**
		 * Checks if is open GL.
		 *
		 * @param d
		 *            the d
		 * @return true, if is open GL
		 */
		private boolean isOpenGL(final IDescription d) {
			// Are we in OpenGL world ?
			IDescription display = d;
			final String type = display.getLitteral(TYPE);

			if (type != null) return IKeyword._3D.equals(type) || IKeyword.OPENGL.equals(type);
			final String parent = display.getLitteral(PARENT);
			final boolean isOpenGLDefault = !GamaPreferences.Displays.CORE_DISPLAY.getValue();
			if (parent == null) return isOpenGLDefault;
			display = StreamEx.of(display.getEnclosingDescription().getChildrenWithKeyword(DISPLAY).iterator())
					.findFirst(dspl -> dspl.getName().equals(parent)).get();
			if (display == null) return isOpenGLDefault;
			return isOpenGL(display);
		}

		@Override
		public void validate(final IStatementDescription description) {
			warnIfNotOpenGL(description);
		}

	}

	/** The output. */
	LayeredDisplayOutput output;

	/**
	 * Checks if is to create.
	 *
	 * @return true, if is to create
	 */
	@Override
	public boolean isToCreate() { return true; }

	/**
	 * Instantiates a new abstract layer statement.
	 *
	 * @param desc
	 *            the desc
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public AbstractLayerStatement(final IDescription desc) throws GamaRuntimeException {
		super(desc);
		setName(desc.getName());
	}

	@Override
	public IExpression getRefreshFacet() { return getFacet(IKeyword.REFRESH); }

	@Override
	public int compareTo(final ILayerStatement o) {
		return Longs.compare(getOrder(), o.getOrder());
	}

	@Override
	public final boolean init(final IScope scope) {
		return _init(scope);
	}

	/**
	 * Inits the.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	protected abstract boolean _init(IScope scope);

	@Override
	public void setDisplayOutput(final IOutput out) { output = (LayeredDisplayOutput) out; }

	/**
	 * Gets the display output.
	 *
	 * @return the display output
	 */
	public LayeredDisplayOutput getDisplayOutput() { return output; }

	/**
	 * Gets the layered display data.
	 *
	 * @return the layered display data
	 */
	public IDisplayData getLayeredDisplayData() {
		if (output == null) return null;
		return output.getData();
	}

	@Override
	public final boolean step(final IScope scope) throws GamaRuntimeException {
		if (!scope.interrupted()) return _step(scope);
		return false;
	}

	/**
	 * Step.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	protected abstract boolean _step(IScope scope);

	@Override
	public void setChildren(final Iterable<? extends ISymbol> children) {}

}
