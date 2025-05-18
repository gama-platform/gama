/*******************************************************************************************************
 *
 * AbstractOutput.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs;

import java.util.Collections;
import java.util.List;

import gama.annotations.precompiler.GamlAnnotations.inside;
import gama.core.common.interfaces.IGamaView;
import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.experiment.ExperimentAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.compilation.ISymbol;
import gama.gaml.compilation.Symbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.ModelDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.expressions.IExpressionFactory;
import gama.gaml.operators.Cast;

/**
 * The Class AbstractOutput.
 *
 * @author drogoul
 */
@inside (
		symbols = IKeyword.OUTPUT)
public abstract class AbstractOutput extends Symbol implements IOutput {

	/** The output scope. */
	private IScope outputScope;

	/** The permanent. */
	volatile boolean paused, open, permanent, disposed = false;

	/** The is user created. */
	private boolean isUserCreated = true;

	/** The refresh. */
	final IExpression refresh;

	/** The original name. */
	final String originalName;

	/** The virtual. */
	final boolean virtual;

	/** The view. */
	protected IGamaView view;

	/** The opener. */
	final Runnable opener = () -> {
		view = getScope().getGui().showView(getScope(), getViewId(), isUnique() ? null : getName(), 1); // IWorkbenchPage.VIEW_ACTIVATE
		if (view == null) return;
		view.addOutput(AbstractOutput.this);
	};

	/**
	 * Instantiates a new abstract output.
	 *
	 * @param desc
	 *            the desc
	 */
	public AbstractOutput(final IDescription desc) {
		super(desc);
		virtual = IKeyword.TRUE.equals(getLiteral(IKeyword.VIRTUAL, null));
		if (hasFacet(IKeyword.REFRESH)) {
			refresh = this.getFacet(IKeyword.REFRESH);
		} else {
			refresh = IExpressionFactory.TRUE_EXPR;
		}
		if (desc != null) { name = desc.getName(); }
		originalName = name;
		if (name != null) {
			name = name.replace(':', '_').replace('/', '_').replace('\\', '_');
			if (name.length() == 0) { name = "output"; }
		}
	}

	@Override
	public String getOriginalName() { return originalName; }

	/**
	 * Checks if is user created.
	 *
	 * @return true, if is user created
	 */
	// @Override
	final boolean isUserCreated() { return isUserCreated; }

	// @Override
	@Override
	public final void setUserCreated(final boolean isUserCreated) { this.isUserCreated = isUserCreated; }

	@Override
	public boolean init(final IScope scope) {
		setScope(buildScopeFrom(scope));
		// getScope().setCurrentSymbol(this);
		return true;
	}

	@Override
	public void update() throws GamaRuntimeException {
		if (view != null) {
			// DEBUG.OUT("Output asking view to update");
			view.update(this);
		}
	}

	/**
	 * Builds the scope from.
	 *
	 * @param scope
	 *            the scope
	 * @return the i scope
	 */
	protected IScope buildScopeFrom(final IScope scope) {
		String desc = description == null ? " " : description.getKeyword() + " ";
		return scope.copy("of " + desc + getName());
	}

	@Override
	public void close() {
		setPaused(true);
		setOpen(false);
	}

	@Override
	public boolean isOpen() { return open; }

	@Override
	public boolean isPaused() { return paused; }

	@Override
	public void open() {
		setOpen(true);
		if (shouldOpenView()) { GAMA.getGui().run("Opening " + getName(), opener, true); }
	}

	// @Override
	@Override
	public boolean isRefreshable() {
		if (!isOpen() || isPaused()) return false;
		final IScope scope = getScope();
		if (scope == null || scope.interrupted()) return false;
		return Cast.asBool(scope, refresh.value(scope));
	}

	@Override
	public abstract boolean step(IScope scope);

	/**
	 * Sets the open.
	 *
	 * @param open
	 *            the new open
	 */
	void setOpen(final boolean open) { this.open = open; }

	@Override
	public void setPaused(final boolean suspended) {
		paused = suspended;
		if (view != null) { view.updateToolbarState(); }
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {

	}

	/**
	 * Gets the children.
	 *
	 * @return the children
	 */
	public List<? extends ISymbol> getChildren() { return Collections.EMPTY_LIST; }

	@Override
	public String getId() {
		IDescription desc = this.getDescription();
		final String cName = desc == null ? null : desc.getModelDescription().getAlias();
		if (cName != null && !"".equals(cName) && !getName().contains("#"))
			return isUnique() ? getViewId() : getViewId() + getName() + "#" + cName;
		return isUnique() ? getViewId() : getViewId() + getName();
	}

	/**
	 * Sets the scope.
	 *
	 * @param scope
	 *            the new scope
	 */
	public void setScope(final IScope scope) {
		if (this.outputScope != null) { GAMA.releaseScope(this.outputScope); }
		if (scope.getModel() != null) {
			final ModelDescription micro = this.getDescription().getModelDescription();
			final ModelDescription main = scope.getModel().getDescription();
			final boolean fromMicroModel = main.getMicroModel(micro.getAlias()) != null;
			if (fromMicroModel) {
				final ExperimentAgent exp = (ExperimentAgent) scope.getRoot()
						.getExternMicroPopulationFor(micro.getAlias() + "." + this.getDescription().getOriginName())
						.getAgent(0);
				this.outputScope = exp.getSimulation().getScope();
			} else {
				this.outputScope = scope;
			}
		} else {
			this.outputScope = scope;
		}
	}

	@Override
	public IScope getScope() { return outputScope; }

	/**
	 * Sets the permanent.
	 */
	void setPermanent() {
		permanent = true;
	}

	/**
	 * Checks if is permanent.
	 *
	 * @return true, if is permanent
	 */
	public boolean isPermanent() { return permanent; }

	@Override
	public boolean isUnique() { return false; }

	@Override
	public boolean isVirtual() { return virtual; }

	/**
	 * Should open view.
	 *
	 * @return true, if successful
	 */
	protected boolean shouldOpenView() {
		return true;
	}

	@Override
	public IGamaView getView() { return view; }

	@Override
	public void dispose() {
		if (disposed) return;
		disposed = true;
		if (view != null) {
			view.removeOutput(this);
			view = null;
		}
		if (getScope() != null) { GAMA.releaseScope(getScope()); }
	}

}
