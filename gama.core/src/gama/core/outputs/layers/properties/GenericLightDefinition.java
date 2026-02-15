/*******************************************************************************************************
 *
 * GenericLightDefinition.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.outputs.layers.properties;

import gama.api.data.objects.IColor;
import gama.api.ui.layers.ILightDefinition;
import gama.api.utils.color.GamaColorFactory;

/**
 * The Class AmbientLightDefinition.
 */
public class GenericLightDefinition implements ILightDefinition {

	/** The name. */
	final String name;

	/** The id. */
	int id;

	/** The intensity. */
	final IColor intensity;

	/**
	 * Instantiates a new generic light definition.
	 *
	 * @param name
	 *            the name.
	 * @param id
	 *            the id.
	 */
	public GenericLightDefinition(final String name, final int id, final int intensity) {
		this(name, id, GamaColorFactory.createWithRGBA(intensity, intensity, intensity, 255));
	}

	/**
	 * Instantiates a new generic light definition.
	 *
	 * @param name
	 *            the name.
	 * @param id
	 *            the id.
	 * @param intensity
	 *            the intensity.
	 */
	public GenericLightDefinition(final String name, final int id, final IColor intensity) {
		this.name = name;
		this.id = id;
		this.intensity = intensity;
	}

	@Override
	public String getName() { return name; }

	@Override
	public int getId() { return id; }

	@Override
	public IColor getIntensity() { return intensity; }

	@Override
	public void setId(final int index) { id = index; }

}
