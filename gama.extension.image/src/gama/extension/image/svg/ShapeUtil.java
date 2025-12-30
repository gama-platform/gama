/*******************************************************************************************************
 *
 * ShapeUtil.java, in gama.extension.image, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.image.svg;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 * The Class ShapeUtil. A copy of com.github.weisj.jsvg.util.ShapeUtil with minor modifications. For an unknown reason,
 * the original class wouldn't load as an Ecplise plugin
 */
public final class ShapeUtil {

	/** The Constant NON_RECTILINEAR_TRANSFORM_MASK. */
	private static final int NON_RECTILINEAR_TRANSFORM_MASK =
			AffineTransform.TYPE_GENERAL_TRANSFORM | AffineTransform.TYPE_GENERAL_ROTATION;

	/**
	 * Instantiates a new shape util.
	 */
	private ShapeUtil() {}

	/**
	 * Checks if is invalid area.
	 *
	 * @param area
	 *            the area
	 * @return true, if is invalid area
	 */
	public static boolean isInvalidArea(final Rectangle2D area) {
		return area.isEmpty() || Double.isNaN(area.getWidth()) || Double.isNaN(area.getHeight());
	}

	/*
	 * Intersect two Shapes by the simplest method, attempting to produce a simplified result. The boolean arguments
	 * keep1 and keep2 specify whether or not the first or second shapes can be modified during the operation or whether
	 * that shape must be "kept" unmodified.
	 */

	/**
	 * Intersect.
	 *
	 * @param s1
	 *            the s 1
	 * @param s2
	 *            the s 2
	 * @param keep1
	 *            the keep 1
	 * @param keep2
	 *            the keep 2
	 * @return the shape
	 */
	public static Shape intersect(final Shape s1, final Shape s2, final boolean keep1, final boolean keep2) {
		if (s1 instanceof Rectangle && s2 instanceof Rectangle) return ((Rectangle) s1).intersection((Rectangle) s2);
		if (s1 instanceof Rectangle2D) return intersectRectShape((Rectangle2D) s1, s2, keep1, keep2);
		if (s2 instanceof Rectangle2D) return intersectRectShape((Rectangle2D) s2, s1, keep2, keep1);
		return intersectByArea(s1, s2, keep1, keep2);
	}

	/*
	 * Intersect a Rectangle with a Shape by the simplest method, attempting to produce a simplified result. The boolean
	 * arguments keep1 and keep2 specify whether or not the first or second shapes can be modified during the operation
	 * or whether that shape must be "kept" unmodified.
	 */

	/**
	 * Intersect rect shape.
	 *
	 * @param r
	 *            the r
	 * @param s
	 *            the s
	 * @param keep1
	 *            the keep 1
	 * @param keep2
	 *            the keep 2
	 * @return the shape
	 */
	private static Shape intersectRectShape(final Rectangle2D r, Shape s, final boolean keep1, final boolean keep2) {
		if (s instanceof Rectangle2D r2) {
			Rectangle2D outputRect;
			if (!keep1) {
				outputRect = r;
			} else if (!keep2) {
				outputRect = r2;
			} else {
				outputRect = new Rectangle2D.Float();
			}
			double x1 = Math.max(r.getX(), r2.getX());
			double x2 = Math.min(r.getX() + r.getWidth(), r2.getX() + r2.getWidth());
			double y1 = Math.max(r.getY(), r2.getY());
			double y2 = Math.min(r.getY() + r.getHeight(), r2.getY() + r2.getHeight());

			if (x2 - x1 < 0 || y2 - y1 < 0) {
				// Width or height is negative. No intersection.
				outputRect.setFrameFromDiagonal(0, 0, 0, 0);
			} else {
				outputRect.setFrameFromDiagonal(x1, y1, x2, y2);
			}
			return outputRect;
		}
		if (r.contains(s.getBounds2D())) {
			if (keep2) { s = cloneShape(s); }
			return s;
		}
		return intersectByArea(r, s, keep1, keep2);
	}

	/*
	 * Intersect two Shapes using the Area class. Presumably other attempts at simpler intersection methods proved
	 * fruitless. The boolean arguments keep1 and keep2 specify whether or not the first or second shapes can be
	 * modified during the operation or whether that shape must be "kept" unmodified.
	 */

	/**
	 * Intersect by area.
	 *
	 * @param s1
	 *            the s 1
	 * @param s2
	 *            the s 2
	 * @param keep1
	 *            the keep 1
	 * @param keep2
	 *            the keep 2
	 * @return the shape
	 */
	private static Shape intersectByArea(final Shape s1, Shape s2, final boolean keep1, final boolean keep2) {
		Area a1;
		Area a2;

		// First see if we can find an overwrite-able source shape
		// to use as our destination area to avoid duplication.
		if (!keep1 && s1 instanceof Area) {
			a1 = (Area) s1;
		} else if (!keep2 && s2 instanceof Area) {
			a1 = (Area) s2;
			s2 = s1;
		} else {
			a1 = new Area(s1);
		}

		if (s2 instanceof Area) {
			a2 = (Area) s2;
		} else {
			a2 = new Area(s2);
		}

		a1.intersect(a2);
		if (a1.isRectangular()) return a1.getBounds2D();

		return a1;
	}

	/**
	 * Transform shape.
	 *
	 * @param s
	 *            the s
	 * @param transform
	 *            the transform
	 * @return the shape
	 */
	public static Shape transformShape(final Shape s, final AffineTransform transform) {
		if (transform.getType() > AffineTransform.TYPE_TRANSLATION) return transformShape(transform, s);
		return transformShape(transform.getTranslateX(), transform.getTranslateY(), s);
	}

	/**
	 * Transform shape.
	 *
	 * @param tx
	 *            the tx
	 * @param shape
	 *            the shape
	 * @return the shape
	 */
	private static Shape transformShape(final AffineTransform tx, final Shape shape) {
		if (shape instanceof Rectangle2D rect && (tx.getType() & NON_RECTILINEAR_TRANSFORM_MASK) == 0) {
			double[] matrix = new double[4];
			matrix[0] = rect.getX();
			matrix[1] = rect.getY();
			matrix[2] = matrix[0] + rect.getWidth();
			matrix[3] = matrix[1] + rect.getHeight();
			tx.transform(matrix, 0, matrix, 0, 2);
			fixRectangleOrientation(matrix, rect);
			return new Rectangle2D.Double(matrix[0], matrix[1], matrix[2] - matrix[0], matrix[3] - matrix[1]);
		}

		if (tx.isIdentity()) return cloneShape(shape);

		return tx.createTransformedShape(shape);
	}

	/**
	 * Fix rectangle orientation.
	 *
	 * @param m
	 *            the m
	 * @param r
	 *            the r
	 */
	private static void fixRectangleOrientation(final double[] m, final Rectangle2D r) {
		if (r.getWidth() > 0 != m[2] - m[0] > 0) {
			double t = m[0];
			m[0] = m[2];
			m[2] = t;
		}
		if (r.getHeight() > 0 != m[3] - m[1] > 0) {
			double t = m[1];
			m[1] = m[3];
			m[3] = t;
		}
	}

	/**
	 * Transform shape.
	 *
	 * @param tx
	 *            the tx
	 * @param ty
	 *            the ty
	 * @param s
	 *            the s
	 * @return the shape
	 */
	private static Shape transformShape(final double tx, final double ty, final Shape s) {
		if (s instanceof Rectangle2D rect)
			return new Rectangle2D.Double(rect.getX() + tx, rect.getY() + ty, rect.getWidth(), rect.getHeight());

		if (tx == 0 && ty == 0) return ShapeUtil.cloneShape(s);

		AffineTransform mat = AffineTransform.getTranslateInstance(tx, ty);
		return mat.createTransformedShape(s);
	}

	/**
	 * Clone shape.
	 *
	 * @param s
	 *            the s
	 * @return the shape
	 */
	private static Shape cloneShape(final Shape s) {
		return new GeneralPath(s);
	}
}
