#version 410 core

out vec4 FragColor;

in vec4 vertexColor;
in vec2 TexCoord;
in vec3 fragNormal;
in vec3 fragPos;

uniform sampler2D texture1;
uniform bool useTexture;

// Lighting uniforms
uniform bool  useLighting;
uniform vec3  ambientColor;      // ambient light colour (RGB)
uniform vec3  lightPosition;     // world-space position of the primary light
uniform vec3  lightColor;        // diffuse+specular colour of the primary light
uniform vec3  viewPos;           // world-space camera position
uniform float shininess;         // specular shininess exponent (default 32)

void main()
{
    vec4 baseColor;
    if (useTexture) {
        baseColor = texture(texture1, TexCoord) * vertexColor;
    } else {
        baseColor = vertexColor;
    }

    if (!useLighting) {
        FragColor = baseColor;
        return;
    }

    // --- Phong lighting ---
    vec3 norm    = normalize(fragNormal);
    vec3 lightDir = normalize(lightPosition - fragPos);

    // Ambient
    vec3 ambient = ambientColor * baseColor.rgb;

    // Diffuse
    float diff   = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor * baseColor.rgb;

    // Specular (Blinn-Phong half-vector)
    vec3 viewDir    = normalize(viewPos - fragPos);
    vec3 halfDir    = normalize(lightDir + viewDir);
    float spec      = pow(max(dot(norm, halfDir), 0.0), max(shininess, 1.0));
    vec3 specular   = spec * lightColor * 0.3;   // scale specular contribution

    vec3 result = ambient + diffuse + specular;
    FragColor   = vec4(result, baseColor.a);
}
