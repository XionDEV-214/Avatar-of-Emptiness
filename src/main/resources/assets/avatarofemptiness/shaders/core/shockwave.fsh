#version 150

uniform float Time;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    float v = texCoord0.y;
    float a = smoothstep(0.0, 0.06, v) * pow(1.0 - v, 1.8);
    float shimmer = 0.85 + 0.15 * sin(texCoord0.x * 400.0 + Time * 25.0);
    fragColor = vec4(vec3(1.1), a * vertexColor.a * shimmer);
}
