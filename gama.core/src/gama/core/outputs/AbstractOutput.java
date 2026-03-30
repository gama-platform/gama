/*******************************************************************************************************
 *
 * AbstractOutput.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs;

import java.util.Collections;
import java.util.List;

import gama.annotations.inside;
import gama.annotations.constants.IKeyword;
import gama.api.GAMA;
import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.descriptions.IModelDescription;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.symbols.ISymbol;
import gama.api.gaml.symbols.Symbol;
import gama.api.gaml.types.Cast;
import gama.api.kernel.simulation.IExperimentAgent;
import gama.api.runtime.scope.IScope;
import gama.api.ui.IGamaView;
import gama.api.ui.IOutput;

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
		view = getScope().getGui().showView(getScope(), getViewId(), isUnique() ? null : getName(),
				viewOpenMode()); // IWorkbenchPage.VIEW_ACTIVATE = 1, VIEW_CREATE = 2
		if (view == null) return;
		view.addOutput(AbstractOutput.this);
	};

	/**
	 * Returns the workbench view-open mode constant to use when opening this output's view.
	 *
	 * <p>
	 * The default is {@code 1} ({@code IWorkbenchPage.VIEW_ACTIVATE}), which immediately activates and renders the view
	 * as soon as it is opened. Subclasses that prefer to defer activation (e.g. to avoid intermediate layout passes
	 * while multiple displays are being opened in sequence) should return {@code 2}
	 * ({@code IWorkbenchPage.VIEW_CREATE}), which creates the part silently without activating it.
	 * </p>
	 *
	 * @return the integer open-mode constant passed to {@link gama.api.ui.IGui#showView}
	 */
	protected int viewOpenMode() { return 1; }

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
			refresh = GAML.getExpressionFactory().getTrue();
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
		if (shouldOpenView()) {
			// Run synchronously if already on the UI/display thread (e.g. inside the openAndApplyLayout syncExec),
			// so the caller can immediately call applyLayoutNow() on the same thread.
			// Run as a UIJob (asynchronous=true) in the classic command-thread path.
			final boolean async = !GAMA.getGui().isInDisplayThread();
			GAMA.getGui().run("Opening " + getName(), opener, async);
		}
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
			final IModelDescription micro = this.getDescription().getModelDescription();
			final IModelDescription main = scope.getModel().getDescription();
			final boolean fromMicroModel = main.getMicroModel(micro.getAlias()) != null;
			if (fromMicroModel) {
				final IExperimentAgent exp = (IExperimentAgent) scope.getRoot()
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

	/**
	 * Gets the view.
	 *
	 * @return the view
	 */
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
