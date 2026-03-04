sed -i 's/gl.getGL().glCallList(i);/\/\/ Removed display list glCallList/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/OpenGL.java
sed -i 's/gl.getGL().glNewList(index, GL4.GL_COMPILE);/\/\/ Removed display list glNewList/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/OpenGL.java
sed -i 's/gl.getGL().glEndList();/\/\/ Removed display list glEndList/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/OpenGL.java
sed -i 's/gl.getGL().glGenLists(1);/0; \/\/ Removed display list glGenLists/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/OpenGL.java
sed -i 's/gl.getGL().glDeleteLists(i, 1);/\/\/ Removed display list glDeleteLists/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/OpenGL.java
