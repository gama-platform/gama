sed -i 's/gl.glTexEnvi(GL4.GL_TEXTURE_ENV, GL4.GL_TEXTURE_ENV_MODE, GL4.GL_DECAL);/\/\/ removed glTexEnvi/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/OpenGL.java
sed -i 's/gl.glTexEnvi(GL4.GL_TEXTURE_ENV, GL4.GL_TEXTURE_ENV_MODE, GL4.GL_MODULATE);/\/\/ removed glTexEnvi/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/OpenGL.java
sed -i 's/gl.glHint(GL4.GL_PERSPECTIVE_CORRECTION_HINT, hint);/\/\/ removed glHint/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/OpenGL.java
sed -i 's/gl.glHint(GL4.GL_POINT_SMOOTH_HINT, hint);/\/\/ removed glHint/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/OpenGL.java
sed -i 's/gl.glAlphaFunc/\/\/ removed glAlphaFunc/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/OpenGL.java
