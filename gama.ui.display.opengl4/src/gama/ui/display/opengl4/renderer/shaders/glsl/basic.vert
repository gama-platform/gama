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
    vec4 worldPos = model * vec4(aPos, 1.0);
    gl_Position = projection * view * worldPos;
    vertexColor = aColor;
    TexCoord = aTexCoord;
    // Transform normal into world space using the normal matrix (transpose of inverse of model)
    // For uniform scaling the model matrix upper-left 3x3 is sufficient.
    fragNormal = normalize(mat3(transpose(inverse(model))) * aNormal);
    fragPos = vec3(worldPos);
}
