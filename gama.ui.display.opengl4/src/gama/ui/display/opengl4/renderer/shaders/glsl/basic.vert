#version 410 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec4 aColor;
layout (location = 2) in vec2 aTexCoord;
layout (location = 3) in vec3 aNormal;
layout (location = 4) in mat4 instanceModelView;
layout (location = 8) in vec4 instanceColor;
layout (location = 9) in vec3 lineEnd;
layout (location = 10) in float ribbonWidth;
layout (location = 11) in float vertexLighting;

out vec4 vertexColor;
out vec2 TexCoord;
out vec3 frontMaterialColor;
out vec3 backMaterialColor;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform mat3 normalMatrix;
uniform bool useLighting;
uniform bool picking;
uniform bool instanced;
uniform bool mapBatch;
uniform vec2 framebufferSize;
uniform vec3 ambientColor;
uniform int lightCount;

const int MAX_LIGHTS = 8;

struct Light {
    vec4 position;
    vec3 color;
    vec3 attenuation;
    vec3 spotDirection;
    float spotCutoff;
};

uniform Light lights[MAX_LIGHTS];

void main()
{
    // The renderer supplies its combined model-view matrix as model.
    mat4 modelView = instanced ? instanceModelView : view * model;
    vec4 color = instanced ? instanceColor : aColor;
    vec4 eyePos = modelView * vec4(aPos, 1.0);
    gl_Position = projection * eyePos;
    if (mapBatch && ribbonWidth != 0.0) {
        vec4 endClip = projection * modelView * vec4(lineEnd, 1.0);
        // Do not expand a segment across the eye plane; clipping still handles the original positions.
        if (gl_Position.w > 0.0 && endClip.w > 0.0) {
            vec2 direction = (endClip.xy / endClip.w - gl_Position.xy / gl_Position.w) * framebufferSize;
            float lengthSquared = dot(direction, direction);
            if (lengthSquared > 0.0) {
                vec2 perpendicular = vec2(-direction.y, direction.x) * inversesqrt(lengthSquared);
                gl_Position.xy += perpendicular * ribbonWidth / framebufferSize * gl_Position.w;
            }
        }
    }
    vertexColor = color;
    TexCoord = aTexCoord;
    frontMaterialColor = color.rgb;
    backMaterialColor = color.rgb;
    if (useLighting && !picking && (!mapBatch || vertexLighting > 0.5)) {
        vec3 norm = normalize((instanced ? transpose(inverse(mat3(modelView))) : normalMatrix) * aNormal);
        vec3 frontIllumination = ambientColor;
        vec3 backIllumination = ambientColor;
        for (int i = 0; i < lightCount; i++) {
            vec3 toLight = lights[i].position.xyz - vec3(eyePos) * lights[i].position.w;
            float distanceToLight = length(toLight);
            vec3 lightDir = distanceToLight > 0.0 ? toLight / distanceToLight : vec3(0.0);
            float attenuation = 1.0;
            if (lights[i].position.w != 0.0) {
                vec3 distanceTerms = vec3(1.0, distanceToLight, distanceToLight * distanceToLight);
                attenuation = 1.0 / max(dot(lights[i].attenuation, distanceTerms), 0.000001);
                if (lights[i].spotCutoff > -1.0
                        && dot(-lightDir, normalize(lights[i].spotDirection)) < lights[i].spotCutoff) {
                    attenuation = 0.0;
                }
            }
            float diffuse = dot(norm, lightDir);
            vec3 intensity = lights[i].color * attenuation;
            frontIllumination += max(diffuse, 0.0) * intensity;
            backIllumination += max(-diffuse, 0.0) * intensity;
        }
        // Fixed-function lighting clamps vertex material colors before interpolation and texturing.
        frontMaterialColor = clamp(color.rgb * frontIllumination, 0.0, 1.0);
        backMaterialColor = clamp(color.rgb * backIllumination, 0.0, 1.0);
    }
}
