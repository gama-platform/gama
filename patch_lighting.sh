sed -i 's/getGL().glLightModelfv(GL4.GL_LIGHT_MODEL_AMBIENT, array, 0);/\/\/ removed glLightfv/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/renderer/helpers/LightHelper.java
sed -i 's/getGL().glLightModelf(GL4.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);/\/\/ removed glLightModelf/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/renderer/helpers/LightHelper.java
sed -i 's/openGL.getGL().glLightfv(id, GLLightingFunc.GL_DIFFUSE, color, 0);/\/\/ removed glLightfv/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/renderer/helpers/LightHelper.java
sed -i 's/gl.glLightfv(id, GLLightingFunc.GL_POSITION, lightPosition, 0);/\/\/ removed glLightfv/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/renderer/helpers/LightHelper.java
sed -i 's/gl.glLightf(id, GLLightingFunc.GL_CONSTANT_ATTENUATION, (float) ca);/\/\/ removed glLightf/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/renderer/helpers/LightHelper.java
sed -i 's/gl.glLightf(id, GLLightingFunc.GL_LINEAR_ATTENUATION, (float) l);/\/\/ removed glLightf/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/renderer/helpers/LightHelper.java
sed -i 's/gl.glLightf(id, GLLightingFunc.GL_QUADRATIC_ATTENUATION, (float) q);/\/\/ removed glLightf/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/renderer/helpers/LightHelper.java
sed -i 's/gl.glLightfv(id, GLLightingFunc.GL_SPOT_DIRECTION, spotLight, 0);/\/\/ removed glLightfv/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/renderer/helpers/LightHelper.java
sed -i 's/gl.glLightf(id, GLLightingFunc.GL_SPOT_CUTOFF, (float) spotAngle);/\/\/ removed glLightf/' gama.ui.display.opengl4/src/gama/ui/display/opengl4/renderer/helpers/LightHelper.java
