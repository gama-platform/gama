sed -i 's/gl.glEnableClientState(GLPointerFunc.GL_VERTEX_ARRAY);/\/\/ removed glEnableClientState/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/renderer/caches/GeometryCache.java
sed -i 's/if (!gl.glIsEnabled(state)) { gl.glEnableClientState(state); }/\/\/ removed glEnableClientState/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/OpenGL.java
sed -i 's/if (gl.glIsEnabled(state)) { gl.glDisableClientState(state); }/\/\/ removed glDisableClientState/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/OpenGL.java
