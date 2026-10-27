#version 150

uniform float Time;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec2 g = floor(texCoord0);
    float s = hash(g + floor(Time * 24.0) * vec2(1.7, 9.2));
    float scan = 0.8 + 0.2 * sin(texCoord0.y * 3.14159 + Time * 30.0);
    fragColor = vec4(vec3(s * scan), vertexColor.a);
}
