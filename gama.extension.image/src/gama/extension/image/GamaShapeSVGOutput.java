package gama.extension.image;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;

import com.github.weisj.jsvg.renderer.Output;
import com.github.weisj.jsvg.util.Provider;
import com.github.weisj.jsvg.util.ShapeUtil;

import gama.core.common.geometry.GeometryUtils;
import gama.core.metamodel.shape.GamaShapeFactory;
import gama.core.metamodel.shape.IShape;
import gama.core.util.GamaListFactory;

class GamaShapeSVGOutput implements Output, Iterable<IShape> {

	List<IShape> shapes = GamaListFactory.create();

	private AffineTransform currentTransform;
	private Stroke currentStroke;
	private Shape currentClip;

	public GamaShapeSVGOutput() {
		currentStroke = new BasicStroke();
		currentTransform = new AffineTransform();
		currentClip = null;
	}

	private GamaShapeSVGOutput(final GamaShapeSVGOutput parent) {
		shapes = parent.shapes;
		currentStroke = parent.currentStroke;
		currentTransform = new AffineTransform(parent.currentTransform);
		currentClip = parent.currentClip != null ? new Area(parent.currentClip) : null;
	}

	private void addShape(final Shape shape) {
		// NOTE: ShapeUtil.transformShape always returns a new shape hence we can safely modify shape.
		Shape s = currentClip != null ? ShapeUtil.intersect(currentClip, shape, true, false) : shape;
		Geometry g = ShapeReader.read(s.getPathIterator(null, 1.0), GeometryUtils.GEOMETRY_FACTORY);
		addShape(g);
	}

	private void addShape(final Geometry g) {
		int n = g.getNumGeometries();
		if (n == 1) {
			if (g instanceof GeometryCollection gc) {
				addShape(gc.getGeometryN(0));
			} else {
				shapes.add(GamaShapeFactory.createFrom(g));
			}
		} else {
			for (int i = 0; i < n; i++) { addShape(g.getGeometryN(i)); }
		}
	}

	private void append(final Shape shape, final AffineTransform transform) {
		AffineTransform at = new AffineTransform(currentTransform);
		at.concatenate(transform);
		addShape(ShapeUtil.transformShape(shape, at));
	}

	private void append(final Shape shape) {
		addShape(ShapeUtil.transformShape(shape, currentTransform));
	}

	@Override
	public void fillShape(final Shape shape) {
		append(shape);
	}

	@Override
	public void drawShape(final Shape shape) {
		append(currentStroke.createStrokedShape(shape));
	}

	@Override
	public void drawImage(final BufferedImage image) {
		append(new Rectangle2D.Float(0, 0, image.getWidth(), image.getHeight()));
	}

	@Override
	public void drawImage(final Image image, final ImageObserver observer) {
		append(new Rectangle2D.Float(0, 0, image.getWidth(null), image.getHeight(null)));
	}

	@Override
	public void drawImage(final Image image, final AffineTransform at, final ImageObserver observer) {
		append(new Rectangle2D.Float(0, 0, image.getWidth(null), image.getHeight(null)), at);
	}

	@Override
	public void setPaint(final Paint paint) {
		// Not supported. Do nothing
	}

	@Override
	public void setPaint(final Provider<Paint> paintProvider) {
		// Not supported. Do nothing
	}

	@Override
	public void setStroke(final Stroke stroke) { currentStroke = stroke; }

	@Override
	public Stroke stroke() {
		return currentStroke;
	}

	@Override
	public void applyClip(final Shape clipShape) {
		Shape transformedShape = ShapeUtil.transformShape(clipShape, currentTransform);
		if (currentClip != null) {
			currentClip = ShapeUtil.intersect(currentClip, transformedShape, true, false);
		} else {
			currentClip = transformedShape;
		}
	}

	@Override
	public void setClip(final Shape shape) {
		currentClip = shape != null ? ShapeUtil.transformShape(shape, currentTransform) : null;
	}

	@Override
	public Optional<Float> contextFontSize() {
		return Optional.empty();
	}

	@Override
	public Output createChild() {
		return new GamaShapeSVGOutput(this);
	}

	@Override
	public void dispose() {
		// No action needed
	}

	@Override
	public void debugPaint(final Consumer<Graphics2D> painter) {
		// Not supported. Do nothing
	}

	@Override
	public Rectangle2D clipBounds() {
		float veryLargeNumber = Float.MAX_VALUE / 4;
		return currentClip != null ? currentClip.getBounds2D()
				: new Rectangle2D.Float(-veryLargeNumber, -veryLargeNumber, 2 * veryLargeNumber, 2 * veryLargeNumber);
	}

	@Override
	public RenderingHints renderingHints() {
		return null;
	}

	@Override
	public Object renderingHint(final RenderingHints.Key key) {
		return null;
	}

	@Override
	public void setRenderingHint(final RenderingHints.Key key, final Object value) {
		// Not supported. Do nothing
	}

	@Override
	public AffineTransform transform() {
		return new AffineTransform(currentTransform);
	}

	@Override
	public void setTransform(final AffineTransform affineTransform) {
		currentTransform = new AffineTransform(affineTransform);
	}

	@Override
	public void applyTransform(final AffineTransform transform) {
		currentTransform.concatenate(transform);
	}

	@Override
	public void rotate(final double angle) {
		currentTransform.rotate(angle);
	}

	@Override
	public void scale(final double sx, final double sy) {
		currentTransform.scale(sx, sy);
	}

	@Override
	public void translate(final double dx, final double dy) {
		currentTransform.translate(dx, dy);
	}

	@Override
	public float currentOpacity() {
		return 1;
	}

	@Override
	public void applyOpacity(final float opacity) {
		// Not supported. Do nothing
	}

	@Override
	public SafeState safeState() {
		return new ShapeOutputSafeState(this);
	}

	@Override
	public boolean supportsFilters() {
		return false;
	}

	@Override
	public boolean supportsColors() {
		return false;
	}

	@Override
	public boolean isSoftClippingEnabled() {
		// Not needed here. Always return false
		return false;
	}

	private static class ShapeOutputSafeState implements SafeState {
		private final GamaShapeSVGOutput shapeOutput;
		private final Stroke oldStroke;
		private final AffineTransform oldTransform;
		private final Area oldClip;

		private ShapeOutputSafeState(final GamaShapeSVGOutput shapeOutput) {
			this.shapeOutput = shapeOutput;
			this.oldStroke = shapeOutput.stroke();
			this.oldTransform = shapeOutput.transform();
			this.oldClip = shapeOutput.currentClip != null ? new Area(shapeOutput.currentClip) : null;
		}

		@Override
		public void restore() {
			shapeOutput.currentStroke = oldStroke;
			shapeOutput.currentTransform = oldTransform;
			shapeOutput.currentClip = oldClip;
		}
	}

	@Override
	public Iterator<IShape> iterator() {
		return shapes.iterator();
	}

}