#version 150

uniform sampler2D Sampler0;
uniform float Time;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    float n = texture(Sampler0, vec2(texCoord0.y * 6.0 + Time * 0.15, texCoord0.x * 0.4 - Time * 0.3)).r;
    float band = sin(clamp(texCoord0.x, 0.0, 1.0) * 3.14159);
    float a = band * smoothstep(0.2, 0.7, n) * vertexColor.a;
    if (a < 0.005) discard;
    fragColor = vec4(vertexColor.rgb * (0.6 + 0.8 * n), a);
}
