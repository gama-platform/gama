/*******************************************************************************************************
 *
 * UtilsForGamlAdditions.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.additions;

import java.util.Arrays;
import java.util.Collections;

import gama.api.compilation.descriptions.IDescription;
import gama.api.compilation.prototypes.IArtefactProto;
import gama.api.constants.IKeyword;
import gama.api.data.objects.IColor;
import gama.api.data.objects.IContainer;
import gama.api.data.objects.IDate;
import gama.api.data.objects.IGraph;
import gama.api.data.objects.IList;
import gama.api.data.objects.IMap;
import gama.api.data.objects.IMatrix;
import gama.api.data.objects.IPair;
import gama.api.data.objects.IPath;
import gama.api.data.objects.IPoint;
import gama.api.data.objects.IShape;
import gama.api.gaml.GAML;
import gama.api.gaml.expressions.IExpression;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.ITypesManager;
import gama.api.gaml.types.Types;
import gama.api.kernel.PlatformAgent;
import gama.api.kernel.agent.IAgent;
import gama.api.kernel.species.ISpecies;
import gama.api.kernel.topology.ITopology;
import gama.api.runtime.scope.IScope;
import gama.api.utils.files.IGamaFile;

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

	/** The pa. */
	protected Class<?> PA = PlatformAgent.class;

	/** The it. */
	protected Class<?> IT = ITopology.class;

	/** The sp. */
	protected Class<?> SP = ISpecies.class;

	/** The gc. */
	protected Class<?> GC = IColor.class;

	/** The gp. */
	protected Class<?> GP = IPair.class;

	/** The gs. */
	protected Class<?> GS = IShape.class;

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
	protected Class<?> P = IPoint.class;

	/** The ic. */
	protected Class<?> IC = IContainer.class;

	/** The il. */
	protected Class<?> IL = IPoint.class;

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

	/** The sc. */
	protected Class<?> SC = IScope.class;

	/** The gd. */
	protected Class<?> GD = IDate.class;

	/** The i. */
	protected Class<?> i = int.class;

	/** The d. */
	protected Class<?> d = double.class;

	/** The b. */
	protected Class<?> b = boolean.class;

	/** The tm. */
	protected ITypesManager TM = Types.builtInTypes;

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
		return GAML.getDescriptionFactory().create(keyword, null, children.getChildren(), facets);
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
		return GAML.getDescriptionFactory().create(keyword, facets);
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
	protected IArtefactProto.Facet[] P(final IArtefactProto.Facet... protos) {
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
