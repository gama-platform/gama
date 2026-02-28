/*******************************************************************************************************
 *
 * BasicShaders.java, in gama.ui.display.opengl, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.display.opengl.renderer.shaders;

public class BasicShaders {

	public static final String VERTEX_SHADER = """
		#version 330 core
		layout (location = 0) in vec3 aPos;
		layout (location = 1) in vec4 aColor;
		layout (location = 2) in vec2 aTexCoord;

		out vec4 fragColor;
		out vec2 texCoord;

		uniform mat4 projection;
		uniform mat4 modelView;

		void main()
		{
		    gl_Position = projection * modelView * vec4(aPos, 1.0);
		    fragColor = aColor;
		    texCoord = aTexCoord;
		}
		""";

	public static final String FRAGMENT_SHADER = """
		#version 330 core
		in vec4 fragColor;
		in vec2 texCoord;

		out vec4 FragColor;

		uniform sampler2D texture1;
		uniform int useTexture;
		uniform vec4 globalColor;

		void main()
		{
		    if (useTexture == 1) {
		        vec4 texColor = texture(texture1, texCoord);
		        FragColor = texColor * fragColor * globalColor;
		    } else {
		        FragColor = fragColor * globalColor;
		    }
		}
		""";

}
