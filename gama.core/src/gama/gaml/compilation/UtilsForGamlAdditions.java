/*******************************************************************************************************
 *
 * UtilsForGamlAdditions.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.gaml.compilation;

import java.util.Arrays;
import java.util.Collections;

import gama.core.common.interfaces.IKeyword;
import gama.core.kernel.experiment.ExperimentAgent;
import gama.core.kernel.root.PlatformAgent;
import gama.core.kernel.simulation.SimulationAgent;
import gama.core.metamodel.agent.GamlAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.GamaShape;
import gama.core.metamodel.shape.IShape;
import gama.core.metamodel.topology.ITopology;
import gama.core.runtime.IScope;
import gama.core.util.GamaColor;
import gama.core.util.GamaPair;
import gama.core.util.IContainer;
import gama.core.util.IDate;
import gama.core.util.file.IGamaFile;
import gama.core.util.graph.IGraph;
import gama.core.util.list.IList;
import gama.core.util.map.IMap;
import gama.core.util.matrix.IMatrix;
import gama.core.util.path.IPath;
import gama.gaml.descriptions.FacetProto;
import gama.gaml.descriptions.IDescription;
import gama.gaml.expressions.IExpression;
import gama.gaml.factories.DescriptionFactory;
import gama.gaml.skills.GridSkill;
import gama.gaml.skills.MovingSkill;
import gama.gaml.species.ISpecies;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 *
 */
public abstract class UtilsForGamlAdditions {

	/** The ai. */
	protected int[] AI = {};

	/** The as. */
	protected String[] AS = {};

	/** The f. */
	protected boolean F = false;

	/** The t. */
	protected boolean T = true;

	/** The prim. */
	protected String PRIM = IKeyword.PRIMITIVE;

	/** The ia. */
	protected Class<?> IA = IAgent.class;

	/** The it. */
	protected Class<?> IT = ITopology.class;

	/** The sp. */
	protected Class<?> SP = ISpecies.class;

	/** The ga. */
	protected Class<?> GA = GamlAgent.class;

	/** The gc. */
	protected Class<?> GC = GamaColor.class;

	/** The gp. */
	protected Class<?> GP = GamaPair.class;

	/** The gs. */
	protected Class<?> GS = GamaShape.class;

	/** The o. */
	protected Class<?> O = Object.class;

	/** The b. */
	protected Class<?> B = Boolean.class;

	/** The i. */
	protected Class<?> I = Integer.class;

	/** The d. */
	protected Class<?> D = Double.class;

	/** The s. */
	protected Class<?> S = String.class;

	/** The ie. */
	protected Class<?> IE = IExpression.class;

	/** The is. */
	protected Class<?> IS = IShape.class;

	/** The gm. */
	protected Class<?> GM = IMap.class;

	/** The p. */
	// public final static Class<?> GL = GamaList.class;
	protected Class<?> P = GamaPoint.class;

	/** The ic. */
	protected Class<?> IC = IContainer.class;

	/** The il. */
	protected Class<?> IL = GamaPoint.class;

	/** The li. */
	protected Class<?> LI = IList.class;

	/** The im. */
	protected Class<?> IM = IMatrix.class;

	/** The gr. */
	protected Class<?> GR = IGraph.class;

	/** The ip. */
	protected Class<?> IP = IPath.class;

	/** The gf. */
	protected Class<?> GF = IGamaFile.class;

	/** The msk. */
	protected Class<?> MSK = MovingSkill.class;

	/** The gsk. */
	protected Class<?> GSK = GridSkill.class;

	/** The sc. */
	protected Class<?> SC = IScope.class;

	/** The gd. */
	protected Class<?> GD = IDate.class;

	/** The sa. */
	protected Class<?> SA = SimulationAgent.class;

	/** The ea. */
	protected Class<?> EA = ExperimentAgent.class;

	/** The pa. */
	protected Class<?> PA = PlatformAgent.class;

	/** The i. */
	protected Class<?> i = int.class;

	/** The d. */
	protected Class<?> d = double.class;

	/** The b. */
	protected Class<?> b = boolean.class;

	/**
	 * The Class Children.
	 */
	public static class Children {

		/** The children. */
		private final Iterable<IDescription> children;

		/**
		 * Instantiates a new children.
		 *
		 * @param descs
		 *            the descs
		 */
		public Children(final IDescription... descs) {
			if (descs == null || descs.length == 0) {
				children = Collections.emptyList();
			} else {
				children = Arrays.asList(descs);
			}
		}

		/**
		 * Gets the children.
		 *
		 * @return the children
		 */
		public Iterable<IDescription> getChildren() { return children; }

	}

	/**
	 * Desc.
	 *
	 * @param keyword
	 *            the keyword
	 * @param children
	 *            the children
	 * @param facets
	 *            the facets
	 * @return the i description
	 */
	protected IDescription desc(final String keyword, final Children children, final String... facets) {
		return DescriptionFactory.create(keyword, null, children.getChildren(), facets);
	}

	/**
	 * Desc.
	 *
	 * @param keyword
	 *            the keyword
	 * @param facets
	 *            the facets
	 * @return the i description
	 */
	protected IDescription desc(final String keyword, final String... facets) {
		return DescriptionFactory.create(keyword, facets);
	}

	/**
	 * Creates a VariableDescription
	 *
	 * @param keyword
	 * @param facets
	 * @return
	 */
	protected IDescription desc(final int keyword, final String... facets) {
		final IType t = Types.get(keyword);
		if (t == null) throw new RuntimeException("Types not defined");
		return desc(t.toString(), facets);
	}

	/**
	 * S.
	 *
	 * @param strings
	 *            the strings
	 * @return the string[]
	 */
	protected String[] S(final String... strings) {
		return strings;
	}

	/**
	 * I.
	 *
	 * @param integers
	 *            the integers
	 * @return the int[]
	 */
	protected int[] I(final int... integers) {
		return integers;
	}

	/**
	 * P.
	 *
	 * @param protos
	 *            the protos
	 * @return the facet proto[]
	 */
	protected FacetProto[] P(final FacetProto... protos) {
		return protos;
	}

	/**
	 * C.
	 *
	 * @param classes
	 *            the classes
	 * @return the class[]
	 */
	protected Class[] C(final Class... classes) {
		return classes;
	}

	/**
	 * T.
	 *
	 * @param c
	 *            the c
	 * @return the i type
	 */
	protected IType<?> T(final Class<?> c) {
		return Types.get(c);
	}

	/**
	 * Ti.
	 *
	 * @param c
	 *            the c
	 * @return the string
	 */
	protected String Ti(final Class<?> c) {
		return String.valueOf(Types.get(c).id());
	}

	/**
	 * Ts.
	 *
	 * @param c
	 *            the c
	 * @return the string
	 */
	protected String Ts(final Class<?> c) {
		return Types.get(c).toString();
	}

	/**
	 * T.
	 *
	 * @param c
	 *            the c
	 * @return the i type
	 */
	protected IType T(final String c) {
		return Types.get(c);
	}

	/**
	 * T.
	 *
	 * @param c
	 *            the c
	 * @return the i type
	 */
	protected IType T(final int c) {
		return Types.get(c);
	}

}
