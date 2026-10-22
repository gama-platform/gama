/*******************************************************************************************************
 *
 * NavigatorSearchControl.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.view;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.swt.IFocusService;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import gama.api.GAMA;
import gama.api.constants.GamlFileExtension;
import gama.dev.DEBUG;
import gama.ui.navigator.view.contents.VirtualContent;
import gama.ui.navigator.view.contents.WrappedGamaFile;
import gama.ui.shared.controls.SwitchButton;
import gama.ui.shared.resources.GamaColors;
import gama.ui.shared.resources.IGamaColors;
import gama.ui.shared.views.toolbar.GamaToolbarSimple;

/**
 * The class EditToolbarFindControls.
 *
 * @author drogoul
 * @since 5 déc. 2014
 *
 */
public class NavigatorSearchControl {

	/**
	 * Should select.
	 *
	 * @param o
	 *            the o
	 * @return true, if successful
	 */
	boolean shouldSelect(final Object o) {
		if (appliedBody) {
			if (!(o instanceof WrappedGamaFile file)) return false;
			final IFile resource = file.getResource();
			final Long stamp = bodyMatches.get(resource);
			return stamp != null && resource.isAccessible() && stamp == resource.getModificationStamp();
		}
		if (!(o instanceof VirtualContent<?> content)) return false;
		return switch (content.getType()) {
			case FILE, FOLDER, PROJECT, VIRTUAL_FOLDER ->
				content.getName().toLowerCase(Locale.ROOT).contains(appliedPattern)
						|| content instanceof WrappedGamaFile file && file.hasTag(appliedPattern);
			default -> false;
		};
	}

	/**
	 * The Class NamePatternFilter.
	 */
	protected class NamePatternFilter extends ViewerFilter {

		/** The already selected. */
		Set<VirtualContent<?>> alreadySelected = new HashSet<>();

		/**
		 * Reset.
		 */
		public void reset() {
			alreadySelected.clear();
		}

		/**
		 * Instantiates a new name pattern filter.
		 */
		public NamePatternFilter() {}

		@Override
		public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
			return select((VirtualContent<?>) element, true);
		}

		/**
		 * Select.
		 *
		 * @param element
		 *            the element
		 * @param b
		 *            the b
		 * @return true, if successful
		 */
		private boolean select(final VirtualContent<?> element, final boolean b) {
			if (appliedBody && element instanceof WrappedGamaFile) return shouldSelect(element);
			if (alreadySelected.contains(element)) return true;
			if (internalSelect(element, b)) {
				alreadySelected.add(element);
				return true;
			}
			return false;
		}

		/**
		 * Internal select.
		 *
		 * @param element
		 *            the element
		 * @param considerVirtualContent
		 *            the consider virtual content
		 * @return true, if successful
		 */
		private boolean internalSelect(final VirtualContent<?> element, final boolean considerVirtualContent) {
			if (appliedPattern.isEmpty()) return true;
			if (!appliedBody) {
				if (shouldSelect(element)) return true;
				for (VirtualContent<?> parent = element.getParent(); parent != null; parent = parent.getParent()) {
					switch (parent.getType()) {
						case FOLDER, PROJECT, VIRTUAL_FOLDER:
							if (shouldSelect(parent)) return true;
							break;
						default:
							break;
					}
				}
			}
			switch (element.getType()) {
				case FILE:
					if (shouldSelect(element)) return true;
					if (!appliedBody) {
						for (final Object child : element.getNavigatorChildren()) {
							if (child instanceof VirtualContent<?> content
									&& content.getType() == VirtualContent.VirtualContentType.FILE
									&& select(content, false)) return true;
						}
					}
					return false;
				case CATEGORY:
				case FILE_REFERENCE:
				case GAML_ELEMENT:
					return considerVirtualContent;
				case FOLDER:
				case PROJECT:
				case ROOT:
				case VIRTUAL_FOLDER:
				default:
					final Object[] children = element.getNavigatorChildren();
					for (final Object element2 : children) {
						if (select((VirtualContent<?>) element2, false)) return true;
					}
					return false;
			}
		}
	}

	/** The find. */
	Text find;

	/** The Constant EMPTY. */
	private static final String EMPTY = "Search..."; //$NON-NLS-1$

	/** The pattern. */
	String pattern = "";

	/** The applied pattern. */
	private String appliedPattern = "";

	/** The search body. */
	private boolean searchBody;

	/** The applied body. */
	private boolean appliedBody;

	/** The mode. */
	private SwitchButton mode;

	/** The search generation. */
	private volatile long searchGeneration;

	/** The pending search. */
	private Job pendingSearch;

	/** The body matches. */
	private Map<IFile, Long> bodyMatches = Map.of();

	/**
	 * The Record SourceText.
	 *
	 * @param stamp
	 *            the stamp
	 * @param charset
	 *            the charset
	 * @param text
	 *            the text
	 */
	private record SourceText(long stamp, String charset, String text) {}

	/** The source cache. */
	// Charge a minimum per entry as well as the text size, bounding both memory and the number of files.
	private final Cache<IFile, SourceText> sourceCache = CacheBuilder.newBuilder().maximumWeight(4 * 1024 * 1024)
			.weigher((final IFile file, final SourceText source) -> 1024 + source.text().length() * 2).build();

	/** The navigator. */
	GamaNavigator navigator;

	/** The tree viewer. */
	CommonViewer treeViewer;

	/**
	 * List of expanded elements at the start of a search used internally to restore the state before search
	 **/
	private Object[] expandedElementsBeforeSearch = null;

	/** The filter. */
	final NamePatternFilter filter = new NamePatternFilter();

	/**
	 * Instantiates a new navigator search control.
	 *
	 * @param navigator
	 *            the navigator
	 */
	public NavigatorSearchControl(final GamaNavigator navigator) {
		this.navigator = navigator;
	}

	/**
	 * Initialize.
	 */
	public void initialize() {
		treeViewer = navigator.getCommonViewer();
	}

	/**
	 * Fill.
	 *
	 * @param toolbar
	 *            the toolbar
	 * @return the navigator search control
	 */
	public NavigatorSearchControl fill(final GamaToolbarSimple toolbar) {

		final Composite parent = new Composite(toolbar, SWT.NONE);
		final Color c = toolbar.getBackground();
		final GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		// layout.horizontalSpacing = 4;
		parent.setLayout(layout);
		parent.setBackgroundMode(SWT.INHERIT_FORCE);
		GamaColors.setBackground(c, parent);
		find = new Text(parent, SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		find.setBackground(IGamaColors.NICE.color());
		final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.widthHint = 120;
		find.setLayoutData(data);
		mode = new SwitchButton(parent, SWT.CHECK, IGamaColors.NICE.color(), IGamaColors.NICE.color(),
				new String[] { "Title", "Body" });
		mode.setBackground(c);
		mode.setSelection(true);
		mode.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		mode.setToolTipText("Title: search file, folder, group and project names, and model tags. "
				+ "Body: search saved model source, including comments "
				+ "(case-insensitive; unsaved editor changes are not searched).");
		mode.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			searchBody = !mode.getSelection();
			scheduleSearch(200);
		}));
		toolbar.control(parent, SWT.DEFAULT);

		final IFocusService focusService = navigator.getSite().getService(IFocusService.class);
		focusService.addFocusTracker(find, "search");

		find.setMessage(EMPTY);
		find.setToolTipText("Search file and folder names and model tags (Title) or saved model source (Body).");

		find.addModifyListener(modifyListener);
		find.addSelectionListener(SelectionListener.widgetDefaultSelectedAdapter(e -> {
			if (e.detail == SWT.ICON_CANCEL) { find.setText(""); }
		}));
		find.addDisposeListener(e -> {
			cancelSearch();
			sourceCache.invalidateAll();
		});
		find.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(final KeyEvent e) {}

			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.character == SWT.ESC) {
					find.setText("");
					navigator.setFocus();
				}
			}
		});

		return this;
	}

	/** The modify listener. */
	private final ModifyListener modifyListener = e -> {
		pattern = ((Text) e.widget).getText().toLowerCase(Locale.ROOT);
		scheduleSearch(200);
	};

	/**
	 * Cancel search.
	 */
	private void cancelSearch() {
		++searchGeneration;
		if (pendingSearch != null) {
			pendingSearch.cancel();
			pendingSearch = null;
		}
	}

	/**
	 * Schedule search.
	 *
	 * @param delay
	 *            the delay
	 */
	private void scheduleSearch(final long delay) {
		cancelSearch();
		if (pattern.isEmpty()) {
			resetSearch();
			return;
		}
		final long generation = searchGeneration;
		final String query = pattern;
		final boolean body = searchBody;
		final var display = find.getDisplay();
		if (body) {
			pendingSearch = new Job("Search saved model source") {
				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					final Map<IFile, Long> matches = new HashMap<>();
					try {
						// Traverse workspace resources, not the wrapper cache: collapsed models may not be cached.
						GAMA.getWorkspaceManager().getRoot().accept(proxy -> {
							if (monitor.isCanceled() || generation != searchGeneration)
								throw new OperationCanceledException();
							if (!proxy.isAccessible()) return false;
							if (proxy.getType() == IResource.FILE) {
								if (GamlFileExtension.isAny(proxy.getName())) {
									final IFile file = (IFile) proxy.requestResource();
									final SourceText source = readSource(file, monitor, generation);
									if (source != null && source.text().contains(query)) {
										matches.put(file, source.stamp());
									}
								}
								return false;
							}
							return true;
						}, IResource.NONE);
					} catch (final OperationCanceledException e) {
						return Status.CANCEL_STATUS;
					} catch (final CoreException e) {
						DEBUG.ERR("Unable to search model source in the workspace", e);
					}
					if (monitor.isCanceled() || generation != searchGeneration) return Status.CANCEL_STATUS;
					if (!display.isDisposed()) {
						display.asyncExec(() -> applySearch(query, true, matches, generation));
					}
					return Status.OK_STATUS;
				}
			};
		} else {
			pendingSearch = new UIJob(display, "Search navigator titles") {
				@Override
				public IStatus runInUIThread(final IProgressMonitor monitor) {
					if (!monitor.isCanceled()) { applySearch(query, false, Map.of(), generation); }
					return Status.OK_STATUS;
				}
			};
		}
		pendingSearch.setSystem(true);
		pendingSearch.schedule(delay);
	}

	/**
	 * Read source.
	 *
	 * @param file
	 *            the file
	 * @param monitor
	 *            the monitor
	 * @param generation
	 *            the generation
	 * @return the source text
	 */
	private SourceText readSource(final IFile file, final IProgressMonitor monitor, final long generation) {
		if (monitor.isCanceled() || generation != searchGeneration) return null;
		try {
			final long stamp = file.getModificationStamp();
			final String charset = file.getCharset();
			final SourceText cached = sourceCache.getIfPresent(file);
			if (stamp != IResource.NULL_STAMP && cached != null && cached.stamp() == stamp
					&& cached.charset().equals(charset))
				return cached;
			sourceCache.invalidate(file);
			final StringBuilder text = new StringBuilder();
			try (final var reader = new InputStreamReader(file.getContents(), charset)) {
				final char[] buffer = new char[8192];
				int count;
				while ((count = reader.read(buffer)) != -1) {
					if (monitor.isCanceled() || generation != searchGeneration) return null;
					text.append(buffer, 0, count);
				}
			}
			if (monitor.isCanceled() || generation != searchGeneration || !file.isAccessible()
					|| stamp != file.getModificationStamp() || !charset.equals(file.getCharset()))
				return null;
			final SourceText source = new SourceText(stamp, charset, text.toString().toLowerCase(Locale.ROOT));
			if (stamp != IResource.NULL_STAMP) { sourceCache.put(file, source); }
			return source;
		} catch (final CoreException | IOException e) {
			sourceCache.invalidate(file);
			DEBUG.ERR("Unable to search saved source of " + file.getFullPath(), e);
			return null;
		}
	}

	/**
	 * Apply search.
	 *
	 * @param query
	 *            the query
	 * @param body
	 *            the body
	 * @param matches
	 *            the matches
	 * @param generation
	 *            the generation
	 */
	private void applySearch(final String query, final boolean body, final Map<IFile, Long> matches,
			final long generation) {
		if (generation != searchGeneration || find.isDisposed() || treeViewer == null
				|| treeViewer.getControl().isDisposed())
			return;
		appliedPattern = query;
		appliedBody = body;
		bodyMatches = matches;
		refreshSearch();
	}

	/**
	 * Do search.
	 */
	public void doSearch() {
		scheduleSearch(0);
	}

	/**
	 * Refresh search.
	 */
	private void refreshSearch() {
		if (expandedElementsBeforeSearch == null) { expandedElementsBeforeSearch = treeViewer.getExpandedElements(); }
		treeViewer.getControl().setRedraw(false);
		try {
			filter.reset();
			if (!Arrays.asList(treeViewer.getFilters()).contains(filter)) {
				treeViewer.addFilter(filter);
			} else {
				treeViewer.refresh(false);
			}
			treeViewer.expandAll();
		} finally {
			treeViewer.getControl().setRedraw(true);
		}
	}

	/**
	 * Reset search.
	 */
	public void resetSearch() {
		cancelSearch();
		appliedPattern = "";
		bodyMatches = Map.of();
		filter.alreadySelected.clear();
		if (treeViewer == null || treeViewer.getControl().isDisposed()) return;
		treeViewer.getControl().setRedraw(false);
		try {
			if (Arrays.asList(treeViewer.getFilters()).contains(filter)) {
				treeViewer.removeFilter(filter);
			} else {
				treeViewer.refresh(false);
			}
			if (expandedElementsBeforeSearch != null) {
				treeViewer.collapseAll();
				treeViewer.setExpandedElements(expandedElementsBeforeSearch);
				expandedElementsBeforeSearch = null;
			}
		} finally {
			treeViewer.getControl().setRedraw(true);
		}
	}

	/**
	 * Search for.
	 *
	 * @param name
	 *            the name
	 */
	public void searchFor(final String name) {
		// Tags in the navigator always initiate a title/tag search, even after a Body search.
		searchBody = false;
		mode.setSelection(true);
		find.setText(name);
		pattern = name.toLowerCase(Locale.ROOT);
		doSearch();

	}

}
