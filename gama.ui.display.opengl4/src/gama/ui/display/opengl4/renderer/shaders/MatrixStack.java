package gama.ui.display.opengl4.renderer.shaders;

import org.joml.Matrix4f;

import java.util.Stack;

public class MatrixStack {
    private final Stack<Matrix4f> stack = new Stack<>();
    private Matrix4f current = new Matrix4f();

    public MatrixStack() {
        current.identity();
    }

    public void pushMatrix() {
        stack.push(new Matrix4f(current));
    }

    public void popMatrix() {
        if (!stack.isEmpty()) {
            current = stack.pop();
        }
    }

    public void loadIdentity() {
        current.identity();
    }

    public void translate(float x, float y, float z) {
        current.translate(x, y, z);
    }

    public void translate(double x, double y, double z) {
        current.translate((float)x, (float)y, (float)z);
    }

    public void rotate(float angleDegrees, float x, float y, float z) {
        current.rotate((float) Math.toRadians(angleDegrees), x, y, z);
    }

    public void rotate(double angleDegrees, double x, double y, double z) {
        current.rotate((float) Math.toRadians(angleDegrees), (float)x, (float)y, (float)z);
    }

    public void scale(float x, float y, float z) {
        current.scale(x, y, z);
    }

    public void scale(double x, double y, double z) {
        current.scale((float)x, (float)y, (float)z);
    }

    public void multMatrix(Matrix4f matrix) {
        current.mul(matrix);
    }

    public void ortho(double left, double right, double bottom, double top, double zNear, double zFar) {
	current.ortho((float)left, (float)right, (float)bottom, (float)top, (float)zNear, (float)zFar);
    }

    public void perspective(double fovy, double aspect, double zNear, double zFar) {
	current.perspective((float)Math.toRadians(fovy), (float)aspect, (float)zNear, (float)zFar);
    }

    public void frustum(double left, double right, double bottom, double top, double zNear, double zFar) {
        current.frustum((float)left, (float)right, (float)bottom, (float)top, (float)zNear, (float)zFar);
    }

    public Matrix4f getCurrentMatrix() {
        return current;
    }
}
