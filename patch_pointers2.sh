sed -i 's/ogl.glNormalPointer(GL_DOUBLE, 0, sideNormalBuffer);/\/\/ removed glNormalPointer/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/scene/text/TextDrawer.java
sed -i 's/ogl.glNormalPointer(GL4.GL_DOUBLE, 0, 0);/\/\/ removed glNormalPointer/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/scene/mesh/MeshDrawer.java
sed -i 's/ogl.glNormalPointer(GL4.GL_DOUBLE, 0, normalBuffer);/\/\/ removed glNormalPointer/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/scene/mesh/MeshDrawer.java
