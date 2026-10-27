package gama.extension.geopackage;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.geotools.api.data.DataStore;
import org.geotools.geopkg.GeoPkgDataStoreFactory;
import org.geotools.util.factory.GeoTools;

/**
 * Opens GeoPackage data stores outside GeoTools' SPI lookup, which does not work across OSGi bundles.
 */
public final class GeoPackageStores {

	static {
		// GeoTools discovers GeoPackage extensions through SPI, which needs this bundle's class loader in OSGi
		GeoTools.addClassLoader(GeoPkgDataStoreFactory.class.getClassLoader());
	}

	private GeoPackageStores() {}

	/**
	 * Opens the given GeoPackage file.
	 *
	 * @param file
	 *            the .gpkg file
	 * @return the data store, never null
	 * @throws IOException
	 *             if the file cannot be opened
	 */
	public static DataStore open(final File file) throws IOException {
		final DataStore store = new GeoPkgDataStoreFactory().createDataStore(Map.of("dbtype", "geopkg", "database", file));
		if (store == null) throw new IOException("Unable to open GeoPackage " + file);
		return store;
	}
}
