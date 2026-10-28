#version 410 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec4 aColor;
layout (location = 2) in vec2 aTexCoord;
layout (location = 3) in vec3 aNormal;

out vec4 vertexColor;
out vec2 TexCoord;
out vec3 fragNormal;
out vec3 fragPos;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main()
{
    // The renderer supplies its combined model-view matrix as model.
    mat4 modelView = view * model;
    vec4 eyePos = modelView * vec4(aPos, 1.0);
    gl_Position = projection * eyePos;
    vertexColor = aColor;
    TexCoord = aTexCoord;
    fragNormal = normalize(mat3(transpose(inverse(modelView))) * aNormal);
    fragPos = vec3(eyePos);
}
