/*******************************************************************************************************
 *
 * FileMetaDataProvider.java, in gama.ui.navigator, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.navigator.metadata;

import static gama.dev.DEBUG.TIMER_WITH_EXCEPTIONS;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
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
import org.eclipse.swt.graphics.ImageData;

import gama.core.common.GamlFileExtension;
import gama.core.util.file.GamaCSVFile;
import gama.core.util.file.GamaCSVFile.CSVInfo;
import gama.core.util.file.GamaFileMetaData;
import gama.core.util.file.GamaOsmFile;
import gama.core.util.file.GamaOsmFile.OSMInfo;
import gama.core.util.file.GamaShapeFile;
import gama.core.util.file.GamaShapeFile.ShapeInfo;
import gama.core.util.file.GamlFileInfo;
import gama.core.util.file.IFileMetaDataProvider;
import gama.core.util.file.IGamaFileMetaData;
import gama.dev.DEBUG;
import gama.dev.THREADS;
import gama.gaml.compilation.GAML;
import gama.gaml.interfaces.IGamlDescription.ConstantDoc;
import gama.gaml.interfaces.IGamlDescription.Doc;
import gama.gaml.operators.Strings;

/**
 * Class FileMetaDataProvider.
 *
 * @author drogoul
 * @since 11 févr. 2015
 *
 */
public class FileMetaDataProvider implements IFileMetaDataProvider {

	/** The processing. */
	private static volatile Set<Object> processing = Collections.<Object> synchronizedSet(new HashSet<>());

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

	/** The Constant CSV_CT_ID. */
	public static final String CSV_CT_ID = "gama.ui.csv.type";

	/** The Constant IMAGE_CT_ID. */
	public static final String IMAGE_CT_ID = "gama.ui.images.type";

	/** The Constant GAML_CT_ID. */
	public static final String GAML_CT_ID = "gama.ui.gaml.type";

	/** The Constant SHAPEFILE_CT_ID. */
	public static final String SHAPEFILE_CT_ID = "gama.ui.shapefile.type";

	/** The Constant OSM_CT_ID. */
	public static final String OSM_CT_ID = "gama.ui.osm.type";

	/** The Constant SHAPEFILE_SUPPORT_CT_ID. */
	public static final String SHAPEFILE_SUPPORT_CT_ID = "gama.ui.shapefile.support.type";

	/** The Constant GSIM_CT_ID. */
	public static final String GSIM_CT_ID = "gama.ui.gsim.type";

	/** The Constant instance. */
	private final static FileMetaDataProvider instance = new FileMetaDataProvider();

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
		}
	};

	/**
	 * The Class GenericFileInfo.
	 */
	public static class GenericFileInfo extends GamaFileMetaData {

		/** The suffix. */
		final String suffix;

		/**
		 * Instantiates a new generic file info.
		 *
		 * @param stamp
		 *            the stamp
		 * @param suffix
		 *            the suffix
		 */
		public GenericFileInfo(final long stamp, final String suffix) {
			super(stamp);
			this.suffix = suffix;
		}

		/**
		 * Instantiates a new generic file info.
		 *
		 * @param propertiesString
		 *            the properties string
		 */
		public GenericFileInfo(final String propertiesString) { // NO_UCD (unused code)
			super(propertiesString);
			final String[] segments = split(propertiesString);
			suffix = segments[1];
		}

		@Override
		public String getSuffix() { return suffix; }

		@Override
		public void appendSuffix(final StringBuilder sb) {
			if (suffix != null) { sb.append(suffix); }
		}

		@Override
		public String toPropertyString() {
			return super.toPropertyString() + DELIMITER + suffix;
		}

		@Override
		public Doc getDocumentation() { return new ConstantDoc(suffix); }
	}

	/**
	 * The Class ProjectInfo.
	 */
	public static class ProjectInfo extends GamaFileMetaData {

		/** The comment. */
		final String comment;

		/**
		 * Instantiates a new project info.
		 *
		 * @param project
		 *            the project
		 * @throws CoreException
		 *             the core exception
		 */
		public ProjectInfo(final IProject project) throws CoreException {
			super(project.getModificationStamp());
			final IProjectDescription desc = project.getDescription();
			comment = desc.getComment();
		}

		/**
		 * Instantiates a new project info.
		 *
		 * @param propertiesString
		 *            the properties string
		 */
		public ProjectInfo(final String propertiesString) { // NO_UCD (unused code)
			super(propertiesString);
			final String[] segments = split(propertiesString);
			comment = segments[1];
		}

		@Override
		public String getSuffix() {
			if (comment == null || comment.isEmpty()) return "";
			return comment;
		}

		@Override
		public void appendSuffix(final StringBuilder sb) {
			if (comment != null && !comment.isEmpty()) { sb.append(comment); }
		}

		@Override
		public String toPropertyString() {
			return super.toPropertyString() + DELIMITER + comment;
		}

		@Override
		public Doc getDocumentation() { return new ConstantDoc(comment); }
	}

	/** The Constant CLASSES. */
	public static final Map<String, Class<? extends GamaFileMetaData>> CLASSES = new HashMap<>() {

		{
			put(CSV_CT_ID, CSVInfo.class);
			put(IMAGE_CT_ID, ImageInfo.class);
			put(GAML_CT_ID, GamlFileInfo.class);
			put(SHAPEFILE_CT_ID, ShapeInfo.class);
			put(OSM_CT_ID, OSMInfo.class);
			put(SHAPEFILE_SUPPORT_CT_ID, GenericFileInfo.class);
			put("project", ProjectInfo.class);
			// BEN put(GSIM_CT_ID, SavedSimulationInfo.class);
		}
	};

	/** The executor. */
	ExecutorService executor = Executors.newCachedThreadPool();

	/** The started. */
	volatile boolean started;

	/**
	 * Instantiates a new file meta data provider.
	 */
	private FileMetaDataProvider() {
		ResourcesPlugin.getWorkspace().getSynchronizer().add(CACHE_KEY);
	}

	/**
	 * Gets the meta data.
	 *
	 * @param project
	 *            the project
	 * @param includeOutdated
	 *            the include outdated
	 * @return the meta data
	 */
	private IGamaFileMetaData getMetaData(final IProject project, final boolean includeOutdated) {
		if (!project.isAccessible()) return null;
		final String ct = "project";
		final Class<? extends GamaFileMetaData> infoClass = CLASSES.get(ct);
		if (infoClass == null) return null;
		final IGamaFileMetaData data = readMetadata(project, infoClass, includeOutdated);
		if (data == null) {
			try {
				storeMetaData(project, new ProjectInfo(project), false);
			} catch (final CoreException e) {
				e.printStackTrace();
				return null;
			}
		}
		return data;
	}

	/**
	 * Method getMetaData()
	 *
	 * @see gama.gui.navigator.IFileMetaDataProvider#getMetaData(org.eclipse.core.resources.IFile)
	 */
	@Override
	public IGamaFileMetaData getMetaData(final Object element, final boolean includeOutdated,
			final boolean immediately) {
		startup();
		if (processing.contains(element)) {
			while (processing.contains(element)) { THREADS.WAIT(100); }
			return getMetaData(element, includeOutdated, immediately);

		}

		try {
			if (element instanceof IProject) return getMetaData((IProject) element, includeOutdated);
			IFile file = adaptTo(element, IFile.class, IFile.class);

			if (file == null) {
				if (element instanceof java.io.File) {
					final IPath p = Path.fromOSString(((java.io.File) element).getAbsolutePath());
					file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(p);
				}
				if (file == null || !file.exists()) return null;
			} else if (!file.isAccessible()) return null;
			final String ct = getContentTypeId(file);
			final Class<? extends GamaFileMetaData> infoClass = CLASSES.get(ct);
			if (infoClass == null) return null;
			final IGamaFileMetaData[] data = { readMetadata(file, infoClass, includeOutdated) };
			if (data[0] == null) {
				processing.add(element);
				final IFile theFile = file;
				final Runnable create = () -> {
					try {
						switch (ct) {
							case SHAPEFILE_CT_ID:
								data[0] = createShapeFileMetaData(theFile);
								break;
							case OSM_CT_ID:
								data[0] = createOSMMetaData(theFile);
								break;
							case IMAGE_CT_ID:
								data[0] = createImageFileMetaData(theFile);
								break;
							case CSV_CT_ID:
								data[0] = createCSVFileMetaData(theFile);
								break;
							case GAML_CT_ID:
								data[0] = createGamlFileMetaData(theFile);
								break;
							case SHAPEFILE_SUPPORT_CT_ID:
								data[0] = createShapeFileSupportMetaData(theFile);
								break;
						}
						// Last chance: we generate a generic info
						if (data[0] == null) { data[0] = createGenericFileMetaData(theFile); }

						// System.out
						// .println("Storing the metadata just created (or
						// recreated) while reading it for " + theFile);
						storeMetaData(theFile, data[0], immediately);
						try {

							theFile.refreshLocal(IResource.DEPTH_ZERO, null);
						} catch (final CoreException e) {
							e.printStackTrace();
						}
						// GAMA.getGui().updateDecorator("gama.ui.application.decorator");
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
			return data[0];
		} finally {

		}
	}

	/**
	 * Read metadata.
	 *
	 * @param <T>
	 *            the generic type
	 * @param file
	 *            the file
	 * @param clazz
	 *            the clazz
	 * @param includeOutdated
	 *            the include outdated
	 * @return the t
	 */
	private <T extends IGamaFileMetaData> T readMetadata(final IResource file, final Class<T> clazz,
			final boolean includeOutdated) {
		T result = null;
		final long modificationStamp = file.getModificationStamp();
		try {
			final String s = (String) file.getSessionProperty(CACHE_KEY);
			if (s != null) {
				// s = GZIP.decompress(s);
				result = GamaFileMetaData.from(s, modificationStamp, clazz, includeOutdated);
			}
			if (!clazz.isInstance(result)) return null;
		} catch (final Exception ignore) {
			DEBUG.ERR("Error loading metadata for " + file.getName() + " : " + ignore.getMessage());
		}
		return result;
	}

	@Override
	public void storeMetaData(final IResource file, final IGamaFileMetaData data, final boolean immediately) {
		startup();
		if (!file.isAccessible()) return;
		try {
			// DEBUG.LOG("Writing back metadata to " + file);
			if (ResourcesPlugin.getWorkspace().isTreeLocked()) // DEBUG.LOG("Canceled: Resources are locked");
				return;

			if (data != null) { data.setModificationStamp(file.getModificationStamp()); }

			final Runnable runnable = () -> {
				try {
					file.setSessionProperty(CACHE_KEY, data == null ? null : data.toPropertyString());
					file.setSessionProperty(CHANGE_KEY, true);
				} catch (final Exception e) {
					e.printStackTrace();
				}
				// DEBUG.LOG("Success: sync info written");
			};
			// WorkspaceModifyOperation
			if (!immediately) {
				executor.submit(runnable);
			} else {
				runnable.run();
			}

		} catch (final Exception ignore) {
			DEBUG.ERR("Error storing metadata for " + file.getName() + " : " + ignore.getMessage());
			ignore.printStackTrace();

		}
	}

	/**
	 * @param file
	 */
	private GamlFileInfo createGamlFileMetaData(final IFile file) {
		return GAML.getInfo(URI.createPlatformResourceURI(file.getFullPath().toOSString(), true),
				file.getModificationStamp());
	}

	/**
	 * Creates the CSV file meta data.
	 *
	 * @param file
	 *            the file
	 * @return the gama CSV file. CSV info
	 */
	private GamaCSVFile.CSVInfo createCSVFileMetaData(final IFile file) {
		return new CSVInfo(file.getLocation().toOSString(), file.getModificationStamp(), null);
	}

	/**
	 * Creates the image file meta data.
	 *
	 * @param file
	 *            the file
	 * @return the image info
	 */
	private ImageInfo createImageFileMetaData(final IFile file) {
		int type = -1, width = -1, height = -1;
		ImageData imageData = ImageDataLoader.getImageData(file);
		if (imageData != null) {
			width = imageData.width;
			height = imageData.height;
			type = imageData.type;
		} else {
			width = -1;
			height = -1;
			type = -1;
		}
		return new ImageInfo(file.getModificationStamp(), type, width, height);

	}

	/**
	 * @param file
	 * @return
	 */
	private GamaShapeFile.ShapeInfo createShapeFileMetaData(final IFile file) {
		ShapeInfo info = null;
		try {
			info = new ShapeInfo(null, file.getLocationURI().toURL(), file.getModificationStamp());
		} catch (final MalformedURLException e) {
			e.printStackTrace();
		}
		return info;

	}

	/**
	 * Creates the OSM meta data.
	 *
	 * @param file
	 *            the file
	 * @return the gama osm file. OSM info
	 */
	private GamaOsmFile.OSMInfo createOSMMetaData(final IFile file) {
		OSMInfo info = null;
		try {
			info = new OSMInfo(file.getLocationURI().toURL(), file.getModificationStamp());
		} catch (final MalformedURLException e) {
			e.printStackTrace();
		}
		return info;

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
		return new GenericFileInfo(file.getModificationStamp(), "" + type + " for '" + r.getName() + "'");
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
		if (ext == null) return new GenericFileInfo(file.getModificationStamp(), "Generic file");
		ext = ext.toUpperCase();
		return new GenericFileInfo(file.getModificationStamp(), "Generic " + ext + " file");
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
			fileName = fileName.replace(extension, "shp");
		}
		return r.getParent().findMember(fileName);
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
	 * Gets the single instance of FileMetaDataProvider.
	 *
	 * @return single instance of FileMetaDataProvider
	 */
	public static FileMetaDataProvider getInstance() { return instance; }

	/**
	 * Startup.
	 */
	private void startup() {
		if (started) return;
		started = true;
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		DEBUG.TIMER("GAMA", "Retrieving workspace metadata", "done in", () -> {
			try {
				workspace.getRoot().accept(resource -> {
					if (resource.isAccessible()) {
						String toRead = resource.getPersistentProperty(CACHE_KEY);
						if (toRead != null) { resource.setSessionProperty(CACHE_KEY, Strings.unzip(null, toRead)); }
					}
					return true;
				});
			} catch (final CoreException e) {}
		});

		try {
			workspace.addSaveParticipant("gama.ui.shared.modeling", getSaveParticipant());
		} catch (final CoreException e) {
			e.printStackTrace();
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

				TIMER_WITH_EXCEPTIONS("GAMA", "workspace metadata ", "saved in", () -> {
					ResourcesPlugin.getWorkspace().getRoot().accept(resource -> {
						String toSave = null;
						try {

							if (resource.isAccessible()) {
								toSave = (String) resource.getSessionProperty(CACHE_KEY);
								if (toSave != null) {
									resource.setPersistentProperty(CACHE_KEY, Strings.zip(null, toSave));
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
		if (f == null || !SHAPEFILE_CT_ID.equals(getContentTypeId(f))) return Collections.EMPTY_LIST;
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
		return r instanceof IFile && SHAPEFILE_CT_ID.equals(getContentTypeId((IFile) r));
	}

}
