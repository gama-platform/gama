/*******************************************************************************************************
 *
 * FileMetaDataProvider.java, in gama.workspace, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.workspace.metadata;

import static gama.dev.DEBUG.TIMER_WITH_EXCEPTIONS;
import static org.eclipse.core.runtime.Path.fromOSString;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.emf.common.util.URI;

import gama.api.GAMA;
import gama.api.constants.GamlFileExtension;
import gama.api.ui.IStatusMessage;
import gama.api.utils.files.AbstractFileMetaData;
import gama.api.utils.files.CompressionUtils;
import gama.api.utils.files.FileUtils;
import gama.api.utils.files.IFileMetadataProvider;
import gama.api.utils.files.IGamaFileMetaData;
import gama.api.utils.files.IGamlFileInfo;
import gama.dev.BANNER_CATEGORY;
import gama.dev.DEBUG;
import gama.dev.THREADS;
import gama.workspace.manager.WorkspaceManager;

/**
 * FileMetaDataProvider is responsible for managing metadata for files and projects in the GAMA workspace.
 * It provides caching mechanisms, serialization/deserialization of metadata, and efficient retrieval
 * of file information through both session properties (runtime) and persistent properties (disk storage).
 * 
 * <p>The provider supports various file types including shapefiles, GAML files, images, and generic files.
 * Metadata is compressed when stored to disk to save space and decompressed when loaded.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 * <li>Asynchronous metadata processing using a thread pool executor</li>
 * <li>Two-level caching (session and persistent properties)</li>
 * <li>Automatic compression/decompression of persistent metadata</li>
 * <li>Thread-safe operations with proper synchronization</li>
 * <li>Graceful degradation for corrupted or outdated metadata</li>
 * </ul>
 * 
 * @author drogoul
 * @since 11 févr. 2015
 * @version 2.0 - Improved thread safety and compression handling
 */
public class FileMetaDataProvider implements IFileMetadataProvider {

	/** The metadata builders. */
	Map<String, Function<IFile, IGamaFileMetaData>> metadataBuilders = new HashMap<>();

	/** The metadata retrievers. */
	Map<String, Function<String, IGamaFileMetaData>> metadataDecoders = new HashMap<>();

	/**
	 * Register metadata class.
	 *
	 * @param contentType
	 *            the content type
	 * @param clazz
	 *            the clazz
	 */
	@Override
	public void registerMetadataClass(String contentType, Class<? extends IGamaFileMetaData> clazz) {
		Constructor<? extends IGamaFileMetaData> iFileConstructor;
		try {
			iFileConstructor = clazz.getDeclaredConstructor(IFile.class);
		} catch (NoSuchMethodException e) {
			return;
		}
		Function<IFile, IGamaFileMetaData> builder = file -> {
			try {
				return iFileConstructor.newInstance(file);
			} catch (Exception e) {
				return null;
			}
		};
		registerMetadataBuilder(contentType, builder); 
		Constructor<? extends IGamaFileMetaData> propertiesConstructor;
		try {
			propertiesConstructor = clazz.getDeclaredConstructor(String.class);
		} catch (NoSuchMethodException e) {
			return;
		}
		Function<String, IGamaFileMetaData> retriever = properties -> {
			try {
				return propertiesConstructor.newInstance(properties);
			} catch (Exception e) {
				return null; 
			}
		};
		registerMetadataDecoder(contentType, retriever);
	}

	/**
	 * Register metadata builder.
	 *
	 * @param contentType
	 *            the content type
	 * @param builder
	 *            the builder
	 */
	public void registerMetadataBuilder(final String contentType, final Function<IFile, IGamaFileMetaData> builder) {
		metadataBuilders.put(contentType, builder);
	}

	/**
	 * Register metadata retriever.
	 *
	 * @param contentType
	 *            the content type
	 * @param retriever
	 *            the retriever
	 */
	public void registerMetadataDecoder(final String contentType, final Function<String, IGamaFileMetaData> retriever) {
		metadataDecoders.put(contentType, retriever);
	}

	/** The processing. */
	private static volatile Set<Object> processing = Collections.synchronizedSet(new HashSet<>());

	/** The metadata retrieval lock for preventing concurrent access. */
	private final Object metadataLock = new Object();

	/**
	 * Adapt the specific object to the specified classes, supporting the IAdaptable interface as well.
	 *
	 * @param o
	 *            the object.
	 * @param actualType
	 *            the actual type that must be returned.
	 * @param adapterType
	 *            the adapter type to check for.
	 */
	private <T> T adaptTo(final Object o, final Class<T> actualType, final Class<?> adapterType) {
		if (actualType.isInstance(o)) return actualType.cast(o);
		if (o instanceof IAdaptable) {
			final Object o2 = ((IAdaptable) o).getAdapter(adapterType);
			if (actualType.isInstance(o2)) return actualType.cast(o2);
		}
		return null;
	}

	/** The Constant CACHE_KEY. */
	public static final QualifiedName CACHE_KEY = new QualifiedName("gama.ui.application", "metadata");

	/** The Constant CHANGE_KEY. */
	public static final QualifiedName CHANGE_KEY = new QualifiedName("gama.ui.application", "changed");

	/** The Constant INSTANCE. */
	private static FileMetaDataProvider INSTANCE;

	/** The Constant OSMExt. */
	public static final ArrayList<String> OSMExt = new ArrayList<>() {

		{
			add("osm");
			add("gz");
			add("pbf");
			add("bz2");
		}
	};

	/** The Constant longNames. */
	public static final HashMap<String, String> longNames = new HashMap<>() {

		{
			put("prj", "Projection data");
			put("shx", "Index data");
			put("dbf", "Attribute data");
			put("xml", "Metadata");
			put("sbn", "Query data");
			put("sbx", "Query data");
			put("qix", "Query data");
			put("qpj", "QGis project");
			put("fix", "Feature index");
			put("cpg", "Character set codepage");
			put("qml", "Style information");
			put("gfs", "GML Feature Store");
			put("xsd", "XML Schema Definition");
		}
	};

	/** The executor. */
	ExecutorService executor = Executors.newFixedThreadPool(
		Math.max(1, Runtime.getRuntime().availableProcessors() / 2),
		r -> {
			Thread t = new Thread(r, "MetadataProvider-" + System.nanoTime());
			t.setDaemon(true);
			t.setPriority(Thread.NORM_PRIORITY - 1);
			return t;
		}
	);

	/** The started. */
	volatile boolean started;

	/**
	 * Instantiates a new file meta data provider with default metadata decoders and builders.
	 */
	private FileMetaDataProvider() {
		registerMetadataDecoder("project", (Function<String, IGamaFileMetaData>) ProjectInfo::new);
		registerMetadataBuilder(SHAPEFILE_SUPPORT_CT_ID, f -> this.createShapeFileSupportMetaData(f));
		registerMetadataDecoder(SHAPEFILE_SUPPORT_CT_ID, GenericFileInfo::new);
	}

	/**
	 * Shutdown the metadata provider and clean up resources.
	 * This method should be called when the workspace is being closed.
	 */
	public void shutdown() {
		if (executor != null && !executor.isShutdown()) {
			executor.shutdown();
			try {
				if (!executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				executor.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
		processing.clear();
		metadataBuilders.clear();
		metadataDecoders.clear();
		started = false;
	}

	/**
	 * Gets the meta data for a project.
	 *
	 * @param project the project
	 * @param includeOutdated whether to include outdated metadata
	 * @return the metadata or null if not available
	 */
	private IGamaFileMetaData getMetaData(final IProject project, final boolean includeOutdated) {
		if (!project.isAccessible()) return null;
		final IGamaFileMetaData data = readMetadata("project", project, includeOutdated);
		if (data == null) {
			try {
				storeMetaData(project, new ProjectInfo(project), false);
			} catch (final CoreException e) {
				DEBUG.ERR("Error creating project metadata for " + project.getName() + ": " + e.getMessage());
				return null;
			}
		}
		return data;
	}

	/**
	 * Gets the meta data for a given element. Handles various types including files, URIs, and projects.
	 * Uses a caching mechanism with session and persistent properties.
	 *
	 * @param element the element to get metadata for
	 * @param includeOutdated whether to include outdated metadata in results  
	 * @param immediately whether to process metadata immediately or asynchronously
	 * @return the metadata or null if not available
	 */
	@Override
	public IGamaFileMetaData getMetaData(final Object element, final boolean includeOutdated,
			final boolean immediately) {
		startup();
		
		if (element == null) {
			return null;
		}
		
		// Wait for processing with timeout to avoid infinite blocking
		int waitCount = 0;
		while (processing.contains(element) && waitCount < 50) {
			THREADS.WAIT(100);
			waitCount++;
		}

		try {
			switch (element) {
				case java.io.File f -> {
					return getMetaData(
							GAMA.getWorkspaceManager().getRoot().getFileForLocation(fromOSString(f.getAbsolutePath())),
							includeOutdated, immediately);
				}
				case URI u -> {
					return getMetaData(FileUtils.getWorkspaceFile(u), includeOutdated, immediately);
				}
				case IProject p -> {
					return getMetaData(p, includeOutdated);
				}
				default -> {
				}
			}
			
			final IFile file = adaptTo(element, IFile.class, IFile.class);
			if (file == null || !file.isAccessible()) return null;
			
			final String ct = getContentTypeId(file);
			if (ct == null) return null;
			
			final IGamaFileMetaData[] data = { readMetadata(ct, file, includeOutdated) };
			
			if (data[0] == null) {
				synchronized (metadataLock) {
					// Double-check pattern to avoid race conditions
					data[0] = readMetadata(ct, file, includeOutdated);
					if (data[0] == null) {
						processing.add(element);
						
						final Runnable create = () -> {
							try {
								Function<IFile, IGamaFileMetaData> metadata = metadataBuilders.get(ct);
								if (data[0] == null && metadata != null) { 
									data[0] = metadata.apply(file); 
								}
								if (data[0] == null) { 
									data[0] = createGenericFileMetaData(file); 
								}
								storeMetaData(file, data[0], immediately);
								try {
									file.refreshLocal(IResource.DEPTH_ZERO, null);
								} catch (final CoreException e) {
									DEBUG.ERR("Error refreshing file " + file.getName() + ": " + e.getMessage());
								}
							} catch (Exception e) {
								DEBUG.ERR("Error in processing " + file.getName() + ": " + e.getMessage());
							} finally {
								processing.remove(element);
							}
						};

						if (immediately) {
							create.run();
						} else {
							executor.submit(create);
						}
					}
				}
			}
			return data[0];
		} catch (Exception e) {
			DEBUG.ERR("Error in getMetaData for " + element + ": " + e.getMessage());
			processing.remove(element);
			return null;
		}
	}

	/**
	 * Read metadata from either session properties (runtime) or persistent properties (startup).
	 * Handles both compressed and uncompressed data formats for backward compatibility.
	 *
	 * @param contentType the content type identifier for metadata decoding
	 * @param file the resource file containing the metadata
	 * @param includeOutdated whether to include outdated metadata in results
	 * @return the decoded metadata or null if not available or invalid
	 */
	private IGamaFileMetaData readMetadata(String contentType, final IResource file, final boolean includeOutdated) {
		if (file == null || !file.isAccessible() || contentType == null) {
			return null;
		}
		
		IGamaFileMetaData result = null;
		final long modificationStamp = file.getModificationStamp();
		
		try {
			// First try to get from session properties (runtime cache)
			String metadataString = (String) file.getSessionProperty(CACHE_KEY);
			
			// If not in session, try persistent properties (saved to disk)
			if (metadataString == null) {
				String persistentData = file.getPersistentProperty(CACHE_KEY);
				if (persistentData != null) {
					try {
						// Try to decompress - the persistent data is compressed
						metadataString = CompressionUtils.unzip(persistentData);
						// Cache it in session for faster access
						file.setSessionProperty(CACHE_KEY, metadataString);
					} catch (Exception e) {
						// Fallback: treat as uncompressed (backward compatibility)
						metadataString = persistentData;
						DEBUG.LOG("Fallback to uncompressed metadata for " + file.getName());
					}
				}
			}
			
			if (metadataString != null) {
				final Function<String, IGamaFileMetaData> decoder = metadataDecoders.get(contentType);
				if (decoder != null) {
					try {
						result = decoder.apply(metadataString);
						if (result != null) {
							final boolean hasFailed = result.hasFailed();
							// Return null if metadata is outdated and we don't want outdated data
							if (!hasFailed && !includeOutdated && result.getModificationStamp() != modificationStamp) {
								return null;
							}
						}
					} catch (final Exception e) {
						DEBUG.ERR("Error decoding metadata " + metadataString + " : " + e.getClass().getSimpleName() + ":" + e.getMessage());
						if (e instanceof InvocationTargetException && e.getCause() != null) {
							e.getCause().printStackTrace();
						}
						// Clear corrupted cache
						try {
							file.setSessionProperty(CACHE_KEY, null);
						} catch (CoreException ce) {
							// Ignore cleanup errors
						}
					}
				}
			}
		} catch (final Exception e) {
			DEBUG.ERR("Error loading metadata for " + file.getName() + " : " + e.getMessage());
		}
		return result;
	}

	/**
	 * Stores metadata for the given resource. The metadata is stored in session properties
	 * for immediate access and will be persisted to disk during workspace save operations.
	 *
	 * @param file the resource to store metadata for
	 * @param data the metadata to store, or null to clear existing metadata
	 * @param immediately whether to store synchronously or asynchronously
	 */
	@Override
	public void storeMetaData(final IResource file, final IGamaFileMetaData data, final boolean immediately) {
		startup();
		if (!file.isAccessible()) return;
		try {
			if (GAMA.getWorkspaceManager().getWorkspace().isTreeLocked()) return;

			if (data != null) { data.setModificationStamp(file.getModificationStamp()); }

			final Runnable runnable = () -> {
				try {
					file.setSessionProperty(CACHE_KEY, data == null ? null : data.toPropertyString());
					file.setSessionProperty(CHANGE_KEY, true);
				} catch (final Exception e) {
					DEBUG.ERR("Error setting session properties for " + file.getName() + ": " + e.getMessage());
				}
			};
			if (!immediately) {
				executor.submit(runnable);
			} else {
				runnable.run();
			}

		} catch (final Exception e) {
			DEBUG.ERR("Error storing metadata for " + file.getName() + " : " + e.getMessage());
		}
	}

	/**
	 * Creates the shape file support meta data.
	 *
	 * @param file
	 *            the file
	 * @return the generic file info
	 */
	private GenericFileInfo createShapeFileSupportMetaData(final IFile file) {
		final IResource r = shapeFileSupportedBy(file);
		if (r == null) return null;
		final String ext = file.getFileExtension();
		final String type = longNames.containsKey(ext) ? longNames.get(ext) : "Data";
		return new GenericFileInfo(file, "" + type + " for '" + r.getName() + "'");
	}

	/**
	 * Creates the generic file meta data.
	 *
	 * @param file
	 *            the file
	 * @return the generic file info
	 */
	private GenericFileInfo createGenericFileMetaData(final IFile file) {
		String ext = file.getFileExtension();
		if (ext == null) return new GenericFileInfo(file, "Generic file");
		ext = ext.toUpperCase();
		return new GenericFileInfo(file, "Generic " + ext + " file");
	}

	/**
	 * Gets the content type id.
	 *
	 * @param p
	 *            the p
	 * @return the content type id
	 */
	public static String getContentTypeId(final IFile p) {
		final IContentType ct = Platform.getContentTypeManager().findContentTypeFor(p.getFullPath().toOSString());
		if (ct != null) return ct.getId();
		if (GamlFileExtension.isAny(p.getName())) return GAML_CT_ID;
		final String ext = p.getFileExtension();
		if ("shp".equals(ext)) return SHAPEFILE_CT_ID;
		if (OSMExt.contains(ext)) return OSM_CT_ID;
		if (longNames.containsKey(ext)) return SHAPEFILE_SUPPORT_CT_ID;
		if ("gsim".equals(ext)) return GSIM_CT_ID;
		return "";
	}

	/**
	 * Shape file supported by.
	 *
	 * @param r
	 *            the r
	 * @return the i resource
	 */
	public static IResource shapeFileSupportedBy(final IFile r) {
		String fileName = r.getName();
		// Special case for these odd files
		if (fileName.endsWith(".shp.xml")) {
			fileName = fileName.replace(".xml", "");
		} else {
			final String extension = r.getFileExtension();
			if (!longNames.containsKey(extension)) return null;
			fileName = fileName.substring(0, fileName.length() - extension.length() - 1);
		}
		final IContainer parent = r.getParent();
		IResource result = parent.findMember(fileName + ".shp");
		if (result != null && result.exists()) return result;
		result = parent.findMember(fileName + ".asc");
		if (result != null && result.exists()) return result;
		result = parent.findMember(fileName + ".gml");
		if (result != null && result.exists()) return result;
		return null;
	}

	/**
	 * Checks if is support.
	 *
	 * @param shapefile
	 *            the shapefile
	 * @param other
	 *            the other
	 * @return true, if is support
	 */
	public static boolean isSupport(final IFile shapefile, final IFile other) {
		final IResource r = shapeFileSupportedBy(other);
		return shapefile.equals(r);
	}

	/**
	 * Gets the single INSTANCE of FileMetaDataProvider.
	 *
	 * @return single INSTANCE of FileMetaDataProvider
	 */
	public static FileMetaDataProvider getInstance() {
		if (INSTANCE == null) { INSTANCE = new FileMetaDataProvider(); }
		return INSTANCE;
	}

	/**
	 * Initialize the metadata provider by setting up workspace synchronization,
	 * loading cached metadata from persistent properties, and registering save participants.
	 * This method is thread-safe and idempotent.
	 */
	private void startup() {
		if (started) return;
		synchronized (this) {
			if (started) return; // Double-check pattern
			
			IWorkspace workspace = GAMA.getWorkspaceManager().getWorkspace();
			workspace.getSynchronizer().add(CACHE_KEY);
			started = true;
			
			DEBUG.TIMER(BANNER_CATEGORY.GAMA, "Retrieving workspace metadata", "done in", () -> {
				try {
					workspace.getRoot().accept(resource -> {
						if (resource.isAccessible()) {
							String toRead = resource.getPersistentProperty(CACHE_KEY);
							if (toRead != null) {
								try {
									// Decompress the persistent data
									String decompressed = CompressionUtils.unzip(toRead);
									resource.setSessionProperty(CACHE_KEY, decompressed);
								} catch (Exception e) {
									// Handle legacy uncompressed data
									DEBUG.LOG("Using legacy uncompressed metadata for " + resource.getName());
									resource.setSessionProperty(CACHE_KEY, toRead);
								}
							}
						}
						return true;
					});
				} catch (final CoreException e) {
					DEBUG.ERR("Error loading persistent metadata during startup: " + e.getMessage());
				}
			});

			try {
				workspace.addSaveParticipant("gama.ui.shared.modeling", getSaveParticipant());
			} catch (final CoreException e) {
				DEBUG.ERR("Error registering save participant: " + e.getMessage());
			}
		}
	}

	/**
	 * Gets the save participant.
	 *
	 * @return the save participant
	 */
	private ISaveParticipant getSaveParticipant() {
		return new ISaveParticipant() {

			@Override
			public void saving(final ISaveContext context) throws CoreException {
				if (context.getKind() != ISaveContext.FULL_SAVE) return;

				TIMER_WITH_EXCEPTIONS(BANNER_CATEGORY.GAMA, "workspace metadata ", "saved in", () -> {
					GAMA.getWorkspaceManager().getRoot().accept(resource -> {
						String toSave = null;
						try {

							if (resource.isAccessible()) {
								toSave = (String) resource.getSessionProperty(CACHE_KEY);
								if (toSave != null) {
									resource.setPersistentProperty(CACHE_KEY, CompressionUtils.zip(toSave));
								}
							}
							return true;
						} catch (final Exception e) {
							DEBUG.ERR("Error when saving metadata of " + resource.getName() + ": " + e.getMessage());
							if (toSave != null) { DEBUG.ERR("Trying to save " + toSave); }
							return true;
						}

					});
				});

			}

			@Override
			public void rollback(final ISaveContext context) {}

			@Override
			public void prepareToSave(final ISaveContext context) throws CoreException {}

			@Override
			public void doneSaving(final ISaveContext context) {}
		};
	}

	/**
	 * Gets the support files of.
	 *
	 * @param f
	 *            the f
	 * @return the support files of
	 */
	public List<IFile> getSupportFilesOf(final IFile f) {
		if (f == null) return Collections.EMPTY_LIST;
		String ct = getContentTypeId(f);
		if (!SHAPEFILE_CT_ID.equals(ct) && !GML_CT_ID.equals(ct)
				&& (!IMAGE_CT_ID.equals(ct) || !"asc".equals(f.getFileExtension())))
			return Collections.EMPTY_LIST;
		final IContainer c = f.getParent();
		final List<IFile> result = new ArrayList<>();
		try {
			for (final IResource r : c.members()) {
				if (r instanceof IFile && isSupport(f, (IFile) r)) { result.add((IFile) r); }
			}
		} catch (final CoreException e) {}
		return result;
	}

	/**
	 * Checks for support files.
	 *
	 * @param r
	 *            the r
	 * @return true, if successful
	 */
	public boolean hasSupportFiles(final IResource r) {
		if (!(r instanceof IFile)) return false;
		String ct = getContentTypeId((IFile) r);
		return SHAPEFILE_CT_ID.equals(ct) || GML_CT_ID.equals(ct)
				|| IMAGE_CT_ID.equals(ct) && "asc".equals(r.getFileExtension());
	}

	@Override
	public void refreshAllMetaData() {
		try {
			GAMA.getGui().getStatus().informStatus("Refreshing metadata of files in workspace",
					IStatusMessage.COMPILE_ICON);
			GAMA.getWorkspaceManager().getRoot().accept(resource -> {
				if (resource.isAccessible()) {
					storeMetaData(resource, null, true);
					getMetaData(resource, false, true);
				}
				return true;
			});
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}
