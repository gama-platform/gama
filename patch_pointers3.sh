sed -i 's/gl.getGL().glTexCoordPointer(2, GL_DOUBLE, 0, faceTextureBuffer);/\/\/ removed glTexCoordPointer/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/scene/text/TextDrawer.java
sed -i 's/ogl.glTexCoordPointer(2, GL4.GL_DOUBLE, 0, 0);/\/\/ removed glTexCoordPointer/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/scene/mesh/MeshDrawer.java
sed -i 's/if (outputsTextures) { ogl.glTexCoordPointer(2, GL4.GL_DOUBLE, 0, texBuffer); }/\/\/ removed glTexCoordPointer/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/scene/mesh/MeshDrawer.java
