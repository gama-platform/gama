#version 410 core

out vec4 FragColor;

in vec4 vertexColor;
in vec2 TexCoord;

uniform sampler2D texture1;
uniform bool useTexture;

void main()
{
    if(useTexture) {
        FragColor = texture(texture1, TexCoord) * vertexColor;
    } else {
        FragColor = vertexColor;
    }
}
