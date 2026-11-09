#version 410 core

out vec4 FragColor;

in vec4 vertexColor;
in vec2 TexCoord;
in vec3 frontMaterialColor;
in vec3 backMaterialColor;

uniform sampler2D texture1;
uniform bool useTexture;
// Legacy OpenGL blends textured objects with GL_ONE, so object transparency does not dim the texture.
uniform bool premultiplyTexture;
uniform bool picking;
uniform vec4 pickingColor;
uniform float pickingAlpha;

void main()
{
    vec4 texel = useTexture ? texture(texture1, TexCoord) : vec4(1.0);
    vec4 baseColor = texel * vertexColor;
    // Match the legacy alpha test, including transparent mesh colors.
    if (baseColor.a * (picking ? pickingAlpha : 1.0) <= 0.01) discard;
    if (picking) {
        if (pickingColor.a == 0.0) discard;
        FragColor = vec4(pickingColor.rgb, 1.0);
        return;
    }

    float colorScale = useTexture && premultiplyTexture ? texel.a : 1.0;

    vec3 materialColor = gl_FrontFacing ? frontMaterialColor : backMaterialColor;
    vec3 result = texel.rgb * materialColor;
    FragColor = vec4(result * colorScale, baseColor.a);
}
