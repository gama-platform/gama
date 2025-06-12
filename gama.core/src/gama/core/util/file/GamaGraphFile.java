/*******************************************************************************************************
 *
 * GamaGraphFile.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.file;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.nio.GraphImporter;
import org.jgrapht.util.SupplierUtil;

import gama.core.common.geometry.Envelope3D;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.graph.GamaGraph;
import gama.core.util.graph.IGraph;
import gama.core.util.graph.loader.GraphImporters;
import gama.gaml.species.ISpecies;

/**
 * The Class GamaGraphFile.
 */
public abstract class GamaGraphFile extends GamaFile<IGraph<?, ?>, Object> {

	/** The node S. */
	ISpecies nodeS = null;

	/** The edge S. */
	ISpecies edgeS = null;

	/**
	 * Instantiates a new gama graph file.
	 *
	 * @param scope
	 *            the scope
	 * @param pn
	 *            the pn
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public GamaGraphFile(final IScope scope, final String pn) throws GamaRuntimeException {
		super(scope, pn);
	}

	/**
	 * Instantiates a new gama graph file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param nodeSpecies
	 *            the node species
	 */
	public GamaGraphFile(final IScope scope, final String pathName, final ISpecies nodeSpecies) {
		super(scope, pathName);
		nodeS = nodeSpecies;
	}

	/**
	 * Instantiates a new gama graph file.
	 *
	 * @param scope
	 *            the scope
	 * @param pathName
	 *            the path name
	 * @param nodeSpecies
	 *            the node species
	 * @param edgeSpecies
	 *            the edge species
	 */
	public GamaGraphFile(final IScope scope, final String pathName, final ISpecies nodeSpecies,
			final ISpecies edgeSpecies) {
		super(scope, pathName);
		nodeS = nodeSpecies;
		edgeS = edgeSpecies;
	}

	@Override
	public Envelope3D computeEnvelope(final IScope scope) {
		return null;
	}

	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) return;
		GraphImporter<String, DefaultEdge> parser = GraphImporters.getGraphImporter(getFileType());
		DirectedMultigraph<String, DefaultEdge> graph =
				new DirectedMultigraph<>(SupplierUtil.createStringSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, true);

		parser.importGraph(graph, this.getFile(scope));
		setBuffer(new GamaGraph<>(scope, graph, nodeS, edgeS));
	}

	/**
	 * Gets the file type.
	 *
	 * @return the file type
	 */
	abstract protected String getFileType();

}
