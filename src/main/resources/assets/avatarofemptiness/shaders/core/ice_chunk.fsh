#version 150

uniform float Time;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    vec2 p = texCoord0 * 2.0 - 1.0;
    float ang = vertexColor.r * 6.2831 + Time * 3.0;
    float c = cos(ang), s = sin(ang);
    p = vec2(c * p.x - s * p.y, s * p.x + c * p.y);
    float shape = abs(p.x) * 1.7 + abs(p.y) * 1.05;
    float body = step(shape, 0.9);
    float facet = (p.x * p.y > 0.0) ? 0.82 : 1.0;
    vec3 col = mix(vec3(0.55, 0.85, 1.0), vec3(0.95, 1.0, 1.0), smoothstep(0.9, 0.2, shape)) * facet;
    float glow = exp(-max(shape - 0.9, 0.0) * 5.0) * (1.0 - body) * 0.6;
    fragColor = vec4(col * body + vec3(0.5, 0.85, 1.0) * glow, max(body, glow) * vertexColor.a);
    if (fragColor.a < 0.01) discard;
}
