sed -i 's/ogl.glVertexPointer(3, GL_DOUBLE, 0, sideQuadsBuffer);/\/\/ removed glVertexPointer/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/scene/text/TextDrawer.java
sed -i 's/olg.glVertexPointer(3, GL_DOUBLE, depth == 0 ? 0 : 6 \* Double.SIZE \/ Byte.SIZE, sideQuadsBuffer);/\/\/ removed glVertexPointer/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/scene/text/TextDrawer.java
sed -i 's/gl.getGL().glVertexPointer(3, GL_DOUBLE, 0, faceVertexBuffer);/\/\/ removed glVertexPointer/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/scene/text/TextDrawer.java
sed -i 's/ogl.glVertexPointer(3, GL4.GL_DOUBLE, 0, 0);/\/\/ removed glVertexPointer/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/scene/mesh/MeshDrawer.java
sed -i 's/ogl.glVertexPointer(3, GL4.GL_DOUBLE, 0, vertexBuffer);/\/\/ removed glVertexPointer/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/scene/mesh/MeshDrawer.java
sed -i 's/gl.glVertexPointer(2, GL4.GL_DOUBLE, 0, db);/\/\/ removed glVertexPointer/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/renderer/caches/GeometryCache.java
