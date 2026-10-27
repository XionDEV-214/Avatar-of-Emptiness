#version 150

uniform sampler2D Sampler0;
uniform float Time;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    vec2 p = texCoord0 * 2.0 - 1.0;
    float r = length(p);
    float n0 = texture(Sampler0, p * 0.35 + vec2(vertexColor.r, Time * 0.05)).r;
    float edge = 0.86 + 0.1 * (n0 - 0.5) + 0.03 * sin(atan(p.y, p.x) * 7.0 + Time * 3.0);
    if (r > edge) discard;

    float z = sqrt(max(0.0, 1.0 - (r / edge) * (r / edge)));
    vec2 sp = p / (1.0 + z);
    float n = texture(Sampler0, sp * 0.8 + vec2(Time * 0.07, vertexColor.r)).r;
    float veins = smoothstep(0.35, 0.55, n);
    vec3 flesh = mix(vec3(0.25, 0.0, 0.02), vec3(0.65, 0.04, 0.06), clamp(n * 2.2, 0.0, 1.0));
    flesh = mix(flesh, vec3(0.08, 0.0, 0.0), veins * 0.7);
    float light = 0.45 + 0.55 * z;
    float rim = pow(1.0 - z, 3.0);
    fragColor = vec4(flesh * light + vec3(0.9, 0.1, 0.1) * rim * 0.6, 1.0);
}
