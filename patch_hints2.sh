sed -i 's/gl.glHint(GL4.GL_MULTISAMPLE_FILTER_HINT_NV, hint);/\/\/ removed glHint/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/OpenGL.java
sed -i 's/gl.glPolygonMode(GL.GL_FRONT_AND_BACK, currentPolygonMode);/\/\/ gl.glPolygonMode(GL.GL_FRONT_AND_BACK, currentPolygonMode);/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/OpenGL.java
sed -i 's/gl.glPolygonMode(GL.GL_FRONT_AND_BACK, mode);/\/\/ gl.glPolygonMode(GL.GL_FRONT_AND_BACK, mode);/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/OpenGL.java
