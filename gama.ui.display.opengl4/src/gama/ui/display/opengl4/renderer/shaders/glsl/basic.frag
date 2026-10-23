#version 410 core

out vec4 FragColor;

in vec4 vertexColor;
in vec2 TexCoord;
in vec3 fragNormal;
in vec3 fragPos;

uniform sampler2D texture1;
uniform bool useTexture;
// Legacy OpenGL blends textured objects with GL_ONE, so object transparency does not dim the texture.
uniform bool premultiplyTexture;

const int MAX_LIGHTS = 8;

struct Light {
    vec4 position;              // eye-space position (w=1) or direction towards the light (w=0)
    vec3 color;
    vec3 attenuation;           // constant, linear, quadratic
    vec3 spotDirection;         // eye-space direction away from the light
    float spotCutoff;           // cosine of cutoff angle, -1 for non-spot lights
};

uniform bool useLighting;
uniform vec3 ambientColor;
uniform int lightCount;
uniform Light lights[MAX_LIGHTS];

void main()
{
    vec4 texel = useTexture ? texture(texture1, TexCoord) : vec4(1.0);
    vec4 baseColor = texel * vertexColor;

    float colorScale = useTexture && premultiplyTexture ? texel.a : 1.0;

    if (!useLighting) {
        FragColor = vec4(baseColor.rgb * colorScale, baseColor.a);
        return;
    }

    vec3 norm = normalize(fragNormal);
    if (!gl_FrontFacing) norm = -norm;

    vec3 illumination = ambientColor;
    for (int i = 0; i < lightCount; i++) {
        vec3 toLight = lights[i].position.xyz - fragPos * lights[i].position.w;
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
        illumination += max(dot(norm, lightDir), 0.0) * lights[i].color * attenuation;
    }

    // Fixed-function OpenGL clamps the lit material color before modulating the texture.
    vec3 materialColor = clamp(vertexColor.rgb * illumination, 0.0, 1.0);
    vec3 result = texel.rgb * materialColor;
    FragColor = vec4(result * colorScale, baseColor.a);
}
