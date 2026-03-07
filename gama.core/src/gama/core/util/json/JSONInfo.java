/*******************************************************************************************************
 *
 * JSONInfo.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.json;

import org.eclipse.core.resources.IFile;

import gama.annotations.constants.IKeyword;
import gama.api.GAMA;
import gama.api.compilation.documentation.GamlRegularDocumentation;
import gama.api.compilation.documentation.IGamlDocumentation;
import gama.api.utils.StringUtils;
import gama.api.utils.files.AbstractFileMetaData;
import gama.api.utils.geometry.GamaEnvelopeFactory;
import gama.api.utils.geometry.IEnvelope;
import gama.api.utils.json.IJsonValue;
import gama.dev.DEBUG;

/**
 * The Class JSONInfo.
 */
public class JSONInfo extends AbstractFileMetaData {

	/** The item count. */
	private int itemCount;

	/** The is geo json. */
	private boolean isGeoJson;

	/** The type. */
	private String type;

	/** The crs. */
	private String crs;

	/** The width. */
	private double width;

	/** The height. */
	private double height;

	/**
	 * Instantiates a new JSON info.
	 *
	 * @param file
	 *            the file
	 */
	public JSONInfo(final IFile file) {
		super(file);

		int itemCount = 0;
		boolean isGeoJson = false;
		String type = "Unknown";
		String crs = null;
		double width = 0;
		double height = 0;
		try (var reader = new java.io.InputStreamReader(file.getContents())) {
			IJsonValue value = GAMA.getJsonEncoder().parse(reader);
			if (value.isArray()) {
				type = "Array";
				itemCount = value.asArray().size();
			} else if (value.isObject()) {
				type = "Object";
				itemCount = value.asObject().size();
				if (value.asObject().get(IKeyword.TYPE) != null) {
					String t = value.asObject().get(IKeyword.TYPE).asString();
					if ("FeatureCollection".equals(t) || "Feature".equals(t) || "GeometryCollection".equals(t)) {
						isGeoJson = true;
						IEnvelope env = GamaEnvelopeFactory.of(0, 0, 0, 0, 0, 0);
						switch (t) {
							case "FeatureCollection": {
								IJsonValue features = value.asObject().get("features");
								if (features != null && features.isArray()) {
									itemCount = features.asArray().size();
									for (IJsonValue v : features.asArray()) {
										if (v.isObject()) {
											IJsonValue geom = v.asObject().get("geometry");
											if (geom != null && geom.isObject()) { computeEnvelope(geom, env); }
										}
									}
								}
								break;
							}
							case "Feature": {
								IJsonValue geom = value.asObject().get("geometry");
								if (geom != null && geom.isObject()) { computeEnvelope(geom, env); }
								break;
							}
							case "GeometryCollection": {
								IJsonValue geometries = value.asObject().get("geometries");
								if (geometries != null && geometries.isArray()) {
									for (IJsonValue v : geometries.asArray()) {
										if (v.isObject()) { computeEnvelope(v, env); }
									}
								}
								break;
							}
							case null:
							default:
								break;
						}
						width = env.getWidth();
						height = env.getHeight();
						IJsonValue crsVal = value.asObject().get("crs");
						if (crsVal != null && crsVal.isObject()) {
							IJsonValue props = crsVal.asObject().get("properties");
							if (props != null && props.isObject()) {
								IJsonValue name = props.asObject().get(IKeyword.NAME);
								if (name != null && name.isString()) { crs = name.asString(); }
							}
						}
					}
				}
			}
		} catch (Exception e) {
			DEBUG.ERR("Error reading JSON metadata for " + file.getName() + ": " + e.getMessage());
		}
		createFrom(itemCount, isGeoJson, type, crs, width, height);

	}

	/**
	 * Compute envelope.
	 *
	 * @param geom
	 *            the geom
	 * @param env
	 *            the env
	 */
	private void computeEnvelope(final IJsonValue geom, final IEnvelope env) {
		IJsonValue coords = geom.asObject().get("coordinates");
		if (coords == null) return;
		String type = geom.asObject().get(IKeyword.TYPE).asString();
		switch (type) {
			case "Point":
				expandEnvelope(coords, env);
				break;
			case "LineString":
			case "MultiPoint":
				for (IJsonValue v : coords.asArray()) { expandEnvelope(v, env); }
				break;
			case "Polygon":
			case "MultiLineString":
				for (IJsonValue v : coords.asArray()) { for (IJsonValue v2 : v.asArray()) { expandEnvelope(v2, env); } }
				break;
			case "MultiPolygon":
				for (IJsonValue v : coords.asArray()) {
					for (IJsonValue v2 : v.asArray()) {
						for (IJsonValue v3 : v2.asArray()) { expandEnvelope(v3, env); }
					}
				}
				break;
			case null:
			default:
				break;
		}
	}

	/**
	 * Expand envelope.
	 *
	 * @param coord
	 *            the coord
	 * @param env
	 *            the env
	 */
	private void expandEnvelope(final IJsonValue coord, final IEnvelope env) {
		if (coord.isArray() && coord.asArray().size() >= 2) {
			double x = coord.asArray().get(0).asDouble();
			double y = coord.asArray().get(1).asDouble();
			if (env.getWidth() == 0 && env.getHeight() == 0 && env.getMinX() == 0 && env.getMinY() == 0) {
				env.init(x, x, y, y, 0, 0);
			} else {
				env.expandToInclude(x, y, 0);
			}
		}
	}

	/**
	 * Instantiates a new JSON info.
	 *
	 * @param modificationStamp
	 *            the modification stamp
	 * @param itemCount
	 *            the item count
	 * @param isGeoJson
	 *            the is geo json
	 * @param type
	 *            the type
	 * @param crs
	 *            the crs
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public void createFrom(final int itemCount, final boolean isGeoJson, final String type, final String crs,
			final double width, final double height) {
		this.itemCount = itemCount;
		this.isGeoJson = isGeoJson;
		this.type = type;
		// GeoJSON files are using WGS84 by default
		this.crs = crs == null || crs.isEmpty() ? isGeoJson ? "WGS84" : "" : crs;
		this.width = width;
		this.height = height;
	}

	/**
	 * Instantiates a new JSON info.
	 *
	 * @param propertyString
	 *            the property string
	 */
	public JSONInfo(final String propertyString) {
		super(propertyString);
		final String[] segments = split(propertyString);
		itemCount = Integer.parseInt(segments[1]);
		isGeoJson = Boolean.parseBoolean(segments[2]);
		type = segments.length > 3 ? segments[3] : "Unknown";
		crs = segments.length > 4 ? segments[4] : null;
		width = segments.length > 5 ? Double.parseDouble(segments[5]) : 0;
		height = segments.length > 6 ? Double.parseDouble(segments[6]) : 0;
	}

	@Override
	public void appendSuffix(final StringBuilder sb) {
		sb.append(type).append(" [").append(itemCount).append(isGeoJson ? " features" : " items").append("]");
		if (isGeoJson) {
			sb.append(SUFFIX_DEL).append(crs);
			sb.append(SUFFIX_DEL).append(Math.round(width)).append("m x ").append(Math.round(height)).append("m");
			sb.append(SUFFIX_DEL).append("GeoJSON");
		} else {
			sb.append(SUFFIX_DEL).append("JSON");
		}
	}

	@Override
	public IGamlDocumentation getDocumentation() {
		final GamlRegularDocumentation sb = new GamlRegularDocumentation();
		sb.append(isGeoJson ? "GeoJSON File" : "JSON File").append(StringUtils.LN);
		sb.append("Type: ").append(type).append(StringUtils.LN);
		sb.append("Contains: ").append(itemCount + "").append(isGeoJson ? " features" : " items")
				.append(StringUtils.LN);
		sb.append("CRS: ").append(crs).append(StringUtils.LN);
		if (isGeoJson && width > 0 && height > 0) {
			sb.append("Dimensions: ").append(Math.round(width) + "m x " + Math.round(height) + "m")
					.append(StringUtils.LN);
		}
		return sb;
	}

	@Override
	public String toPropertyString() {
		return super.toPropertyString() + DELIMITER + itemCount + DELIMITER + isGeoJson + DELIMITER + type + DELIMITER
				+ crs + DELIMITER + width + DELIMITER + height;
	}

	/**
	 * Gets the item count.
	 *
	 * @return the item count
	 */
	public int getItemCount() { return itemCount; }

	/**
	 * Checks if is geo json.
	 *
	 * @return true, if is geo json
	 */
	public boolean isGeoJson() { return isGeoJson; }

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() { return type; }

}
