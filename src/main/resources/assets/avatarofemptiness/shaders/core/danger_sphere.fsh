#version 150

uniform float Time;

in vec4 vertexColor;
in vec3 viewNormal;
in vec3 viewPos;
out vec4 fragColor;

void main() {
    vec3 n = normalize(viewNormal);
    vec3 v = normalize(-viewPos);
    float f = 1.0 - abs(dot(n, v));
    float rim = pow(f, 2.2);
    float wave = 0.85 + 0.15 * sin(Time * 6.0 + f * 10.0);
    float a = vertexColor.a * (0.35 + 1.6 * rim) * wave;
    fragColor = vec4(vertexColor.rgb * (0.7 + 0.8 * rim), clamp(a, 0.0, 1.0));
}
