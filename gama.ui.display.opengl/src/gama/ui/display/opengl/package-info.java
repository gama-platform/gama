/**
 * Shared OpenGL display infrastructure and the default fixed-function backend.
 *
 * <p>The scene graph, drawing objects, camera, canvas, animator and renderer lifecycle are shared with
 * {@code gama.ui.display.opengl4}. They use {@link gama.ui.display.opengl.IOpenGL} rather than assuming GL2.
 * The core-profile plugin specializes {@link gama.ui.display.opengl.renderer.JOGLRenderer}'s backend factories
 * and {@link gama.ui.display.opengl.view.SWTOpenGLDisplaySurface#createRenderer()} without duplicating that
 * infrastructure. Its view retains its own Eclipse registration and focus/input behaviour.</p>
 *
 * <p>Shader loading and framebuffer management are shared too, while the shader programs and their resources
 * remain in their respective backends. Resource lookup is relative to the concrete shader subclass, and GL4
 * opts out of the legacy shader loader's blend-state initialization.</p>
 *
 * <p>Native drawing, lighting, picking, keystone rendering and GPU caches remain backend-specific. In particular,
 * GL2 display lists store drawing commands, whereas GL4 cached drawings replay their Java drawing operations.
 * Shared layers therefore wait for textures before recording a GL2 display list, but not before caching a GL4
 * drawing. GPU resources always belong to their backend and context.</p>
 */
package gama.ui.display.opengl;
