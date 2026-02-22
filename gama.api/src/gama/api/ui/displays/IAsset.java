/*******************************************************************************************************
 *
 * IAsset.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.ui.displays;

/**
 * A tagging interface for graphical assets in GAMA.
 * 
 * <p>This interface marks objects that represent graphical resources used in displays,
 * such as files, icons, images, textures, 3D models, and other visual elements. Assets
 * are identified by unique IDs and can be cached, shared, and reused across displays.</p>
 * 
 * <h2>Asset Types:</h2>
 * <p>Common asset types in GAMA include:</p>
 * <ul>
 *   <li><strong>Images:</strong> PNG, JPEG, GIF, SVG image files</li>
 *   <li><strong>3D Models:</strong> OBJ, DAE, 3DS model files</li>
 *   <li><strong>Icons:</strong> Small images used for UI elements</li>
 *   <li><strong>Textures:</strong> Images used to texture 3D objects</li>
 *   <li><strong>Files:</strong> Generic file resources</li>
 * </ul>
 * 
 * <h2>Asset Management:</h2>
 * <p>Assets are typically:</p>
 * <ul>
 *   <li>Loaded from files on demand</li>
 *   <li>Cached to avoid repeated loading</li>
 *   <li>Shared between multiple uses</li>
 *   <li>Released when no longer needed</li>
 * </ul>
 * 
 * <h2>Identification:</h2>
 * <p>Each asset has a unique ID (usually its file path or URL) that is used for
 * caching and reference purposes. This allows the same asset to be shared efficiently
 * across multiple display elements.</p>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * IAsset imageAsset = new ImageAsset("path/to/image.png");
 * String id = imageAsset.getId();  // "path/to/image.png"
 * // Asset can now be cached and reused by ID
 * }</pre>
 *
 * @author The GAMA Development Team
 * @since GAMA 1.0
 */
public interface IAsset {

	/**
	 * Gets the unique identifier for this asset.
	 * 
	 * <p>The ID is typically the file path, URL, or other unique reference to the
	 * asset resource. It should be consistent and suitable for use as a cache key.</p>
	 * 
	 * <p>Examples of valid IDs:</p>
	 * <ul>
	 *   <li>"models/images/agent.png"</li>
	 *   <li>"http://example.com/texture.jpg"</li>
	 *   <li>"resources/models/building.obj"</li>
	 * </ul>
	 *
	 * @return the unique identifier for this asset
	 */
	String getId();

}
