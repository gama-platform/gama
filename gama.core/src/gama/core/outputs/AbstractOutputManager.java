/*******************************************************************************************************
 *
 * AbstractOutputManager.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2024-06).
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import gama.core.common.interfaces.IKeyword;
import gama.core.common.preferences.GamaPreferences;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaMapFactory;
import gama.core.util.IMap;
import gama.dev.DEBUG;
import gama.gaml.compilation.ISymbol;
import gama.gaml.compilation.Symbol;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.operators.Cast;
import gama.gaml.types.Types;

/**
 * Class AbstractOutputManager.
 *
 * @author drogoul
 * @since 9 juin 2013
 *
 */
public abstract class AbstractOutputManager extends Symbol implements IOutputManager {

    public static class ConcurrentLinkedReducedHashSet<E> implements Iterable<E> {
	/**
	 * A tuple holding our value and its insertion order.
	 */
	private record InsertionOrder<T>(T value, long order) {
	}

	/**
	 * Contains a mapping from our element to its insertionOrder.
	 */
	private final Map<E, InsertionOrder<E>> elements = new ConcurrentHashMap<>();
	/**
	 * The insertion order maintained in a ConcurrentSkipListSet, so that we
	 * iterate in the correct order.
	 */
	private final Set<InsertionOrder<E>> elementsOrderedByInsertion = new ConcurrentSkipListSet<>(
		Comparator.comparingLong(InsertionOrder::order));
	/**
	 * AtomicLong for generating the next insertion order.
	 */
	private final AtomicLong nextOrder = new AtomicLong();

	public boolean add(E e) {
	    var added = new AtomicBoolean(false);
	    elements.computeIfAbsent(e, key -> {
		var holder = new InsertionOrder<>(e, nextOrder.getAndIncrement());
		elementsOrderedByInsertion.add(holder);
		added.set(true);
		return holder;
	    });
	    return added.get();
	}

	public boolean remove(Object e) {
	    var removed = new AtomicBoolean(false);
	    elements.computeIfPresent((E) e, (key, holder) -> {
		elementsOrderedByInsertion.remove(holder);
		removed.set(true);
		return null; // will remove the entry
	    });
	    return removed.get();
	}

	public boolean contains(E e) {
	    return elements.containsKey(e);
	}

	public Stream<E> stream() {
	    return elementsOrderedByInsertion.stream().map(InsertionOrder::value);
	}

	public void clear() {
	    // slow, but ensures we remove all entries in both collections
	    stream().forEach(this::remove);
	}

	@Override
	public Iterator<E> iterator() {
	    return stream().iterator();
	}

	@Override
	public String toString() {
	    return stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]"));
	}
    }

    static class OutputMap implements Map<String, IOutput> {
	// an internal data structure that preserves concurrency and the
	// definition order of outputs
	int monitorOrder;
	final Map<String, IOutput> actualOutputs = new ConcurrentHashMap<>();
	final ConcurrentLinkedReducedHashSet<String> outputNames = new ConcurrentLinkedReducedHashSet<>();
	final IMap<String, IOutput> virtualOutputs = GamaMapFactory.create(); // Necessary
									      // ?

	@Override
	public int size() {
	    return actualOutputs.size();
	}

	@Override
	public boolean isEmpty() {
	    return actualOutputs.isEmpty();
	}

	@Override
	public boolean containsKey(final Object key) {
	    return actualOutputs.containsKey(key);
	}

	@Override
	public boolean containsValue(final Object value) {
	    return actualOutputs.containsValue(value);
	}

	@Override
	public IOutput get(final Object key) {
	    return actualOutputs.get(key);
	}

	@Override
	public IOutput put(final String key, final IOutput value) {
	    if (value == null) {
		return null;
	    }
	    if (value.isVirtual()) {
		return virtualOutputs.put(key, value);
	    }
	    if (value instanceof MonitorOutput mo) {
		mo.setOrder(monitorOrder++);
	    }
	    if (actualOutputs.containsKey(key)) {
		outputNames.remove(key);
	    }
	    outputNames.add(key);
	    return actualOutputs.put(key, value);
	}

	@Override
	public IOutput remove(final Object key) {
	    return outputNames.remove(key) ? actualOutputs.remove(key) : null;
	}

	@Override
	public void putAll(final Map<? extends String, ? extends IOutput> m) {
	    m.forEach(this::put);
	}

	@Override
	public void clear() {
	    outputNames.clear();
	    actualOutputs.clear();
	}

	@Override
	public Set<String> keySet() {
	    return Sets.newConcurrentHashSet(outputNames);
	}

	@Override
	public Collection<IOutput> values() {
	    List<IOutput> result = new ArrayList<>();
	    outputNames.forEach(n -> {
		result.add(actualOutputs.get(n));
	    });
	    return result;
	}

	@Override
	public Set<Entry<String, IOutput>> entrySet() {
	    return actualOutputs.entrySet();
	}

	@Override
	public void forEach(final BiConsumer<? super String, ? super IOutput> action) {
	    outputNames.forEach(n -> {
		action.accept(n, actualOutputs.get(n));
	    });
	}

	public IOutput findOutputWith(final Predicate<? super IOutput> condition) {
	    for (IOutput o : actualOutputs.values()) {
		if (condition.test(o)) {
		    return o;
		}
	    }
	    return null;
	}

    }

    static {
	DEBUG.OFF();
    }

    /** The autosave. */
    final IExpression autosave;

    /** The in init phase. */
    volatile boolean inInitPhase;

    /**
     * Properties
     */

    /** The layout. */
    LayoutStatement layout;

    /** The outputs. */
    protected final OutputMap outputs = new OutputMap();

    /** The virtual outputs. */

    /** The display index. */
    protected int displayIndex;

    /** The has monitors. */
    protected boolean hasMonitors;

    /**
     * Instantiates a new abstract output manager.
     *
     * @param desc
     *            the desc
     */
    public AbstractOutputManager(final IDescription desc) {
	super(desc);
	autosave = desc.getFacetExpr(IKeyword.AUTOSAVE);
	boolean sync = GamaPreferences.Runtime.CORE_SYNC.getValue() || "true".equals(desc.getLitteral("synchronized"))
		|| desc.hasFacet(IKeyword.AUTOSAVE) && !"false".equals(desc.getLitteral(IKeyword.AUTOSAVE));
	if (sync) {
	    GAMA.synchronizeFrontmostExperiment();
	}
    }

    // @Override
    // public Map<String, ? extends IOutput> getOutputs() { return outputs; }

    @Override
    public Iterator<IOutput> iterator() {
	return outputs.values().iterator();
    }

    @Override
    public IOutput getOutputWithId(final String id) {
	return outputs.get(id);
    }

    @Override
    public void putAll(final Map<String, IOutput> mm) {
	outputs.putAll(mm);
    }

    @Override
    public IOutput getOutputWithOriginalName(final String s) {
	return outputs.findOutputWith(each -> each.getOriginalName().equals(s));
    }

    @Override
    public void add(final IOutput output) {
	hasMonitors |= output instanceof MonitorOutput;
	// if (output.isVirtual()) {
	// virtualOutputs.put(output.getId(), output);
	// } else {
	// synchronized (outputs) {
	outputs.put(output.getId(), output);
	// }
	// }
    }

    @Override
    public synchronized void dispose() {
	super.dispose();
	try {
	    GAMA.desynchronizeFrontmostExperiment();
	    // synchronized (outputs) {
	    for (final IOutput output : outputs.values()) {
		output.dispose();
	    }
	    // }
	    clear();
	} catch (final Exception e) {
	    e.printStackTrace();
	}
    }

    @Override
    // hqnghi
    // for instant, multi-simulation cannot have their owns outputs display at
    // same time.
    public void clear() {
	// synchronized (outputs) {
	outputs.clear();
	// }
    }

    @Override
    public void remove(final IOutput o) {
	if (!(o instanceof AbstractOutput)) {
	    return;
	}
	if (((AbstractOutput) o).isUserCreated()) {
	    o.dispose();
	    outputs.remove(o.getName());
	} else {
	    o.setPaused(true);
	}
    }

    @Override
    public void setChildren(final Iterable<? extends ISymbol> commands) {
	for (final ISymbol s : commands) {
	    if (s instanceof LayoutStatement) {
		layout = (LayoutStatement) s;
	    } else if (s instanceof IOutput o) {
		if (o.isAutoSave()) {
		    GAMA.synchronizeFrontmostExperiment();
		}
		add(o);
		o.setUserCreated(false);
		if (o instanceof LayeredDisplayOutput ldo) {
		    ldo.setIndex(displayIndex++);
		}
	    }
	}
    }

    @Override
    public void forceUpdateOutputs() {
	outputs.forEach((n, o) -> o.update());
    }

    @Override
    public void pause() {
	outputs.forEach((n, o) -> o.setPaused(true));
    }

    @Override
    public void resume() {
	outputs.forEach((n, o) -> o.setPaused(false));
    }

    @Override
    public void close() {
	outputs.forEach((n, o) -> o.close());
    }

    @Override
    public Collection<MonitorOutput> getMonitors() {
	return Lists.newArrayList(Iterables.filter(outputs.values(), MonitorOutput.class));
    }

    @Override
    public String toString() {
	return name;
    }

    @Override
    public boolean init(final IScope scope) {
	name = scope.getRoot().getName();
	for (final IOutput output : ImmutableList.copyOf(outputs.values())) {
	    if (!open(scope, output)) {
		return false;
	    }
	}
	evaluateAutoSave(scope);
	return true;
    }

    /**
     * Evaluate auto save.
     *
     * @param scope
     *            the scope
     * @return true, if successful
     * @throws GamaRuntimeException
     *             the gama runtime exception
     */
    private boolean evaluateAutoSave(final IScope scope) throws GamaRuntimeException {
	boolean isAutosaving = false;
	if (autosave != null) {
	    String path = null;
	    if (autosave.getGamlType().equals(Types.STRING)) {
		path = Cast.asString(scope, autosave.value(scope));
		isAutosaving = path != null && !path.isBlank();
	    } else {
		isAutosaving = Cast.asBool(scope, autosave.value(scope));
	    }
	    if (isAutosaving) {
		scope.getGui().getSnapshotMaker().takeAndSaveScreenshot(scope, path);
	    }
	}
	return isAutosaving;
    }

    /**
     * Sets the layout.
     *
     * @param layout
     *            the new layout
     */
    public void setLayout(final LayoutStatement layout) {
	this.layout = layout;
    }

    /**
     * Gets the layout.
     *
     * @return the layout
     */
    public LayoutStatement getLayout() {
	return layout;
    }

    @Override
    public boolean open(final IScope scope, final IOutput output) {
	if (scope.init(output).passed()) {
	    output.setPaused(false);
	    if (initialStep(scope, output)) {
		try {
		    output.open();
		    // DEBUG.OUT("Updating the output");
		    output.update();
		} catch (final RuntimeException e) {
		    e.printStackTrace();
		    return false;
		}
	    }
	}
	return true;
    }

    /**
     * Initial step.
     *
     * @param scope
     *            the scope
     * @param output
     *            the output
     * @return true, if successful
     */
    protected boolean initialStep(final IScope scope, final IOutput output) {
	inInitPhase = true;
	boolean result = false;
	try {
	    result = scope.step(output).passed();
	} finally {
	    inInitPhase = false;
	}
	return result;
    }

    @Override
    public boolean step(final IScope scope) {
	outputs.forEach((name, each) -> {
	    if (each instanceof LayeredDisplayOutput ldo) {
		ldo.linkScopeWithGraphics();
	    }
	    if (each.isRefreshable() && each.getScope().step(each).passed()) {
		each.update();
	    }
	});

	evaluateAutoSave(scope);
	return true;
    }

    @Override
    public boolean hasMonitors() {
	return hasMonitors;
    }

    @Override
    public boolean isEmpty() {
	return outputs.isEmpty();
    }

    @Override
    public void forEach(final BiConsumer<? super String, ? super IOutput> action) {
	outputs.forEach(action);
    }
}