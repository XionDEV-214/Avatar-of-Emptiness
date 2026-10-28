#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float Time;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    vec2 c = texCoord0 - 0.5;
    float d = length(c) * 2.0;
    float ang = atan(c.y, c.x) / 6.2831853 + 0.5;

    vec2 pa = vec2(d, fract(ang + Time * 0.10));
    vec2 pb = vec2(d, fract(ang - Time * 0.10));
    float na = texture(Sampler0, pa * vec2(1.2, 1.0) + vec2(-Time * 0.12, 0.0)).r;
    float nb = texture(Sampler1, pb * vec2(1.6, 1.0) + vec2(-Time * 0.08, 0.0)).r;
    float combined = dot(vec2(na, nb), vec2(0.582, 0.813)) - 0.40;

    float vanish = 1.0 - vertexColor.a;
    float baseRadius = 0.45;
    float e = d / baseRadius;
    e += pow(e, 2.0) * combined * clamp(0.15 / max(d, 0.001), 0.0, 1.0) * 1.4 * (1.0 - vanish);
    e += vanish * 1.6;

    vec3 red = vertexColor.rgb;
    float inside = step(e, 1.0);
    float edge = smoothstep(0.72, 0.99, e) * inside;
    vec3 core = red * edge * 2.4;

    float outside = 1.0 - inside;
    float halo = exp(-max(e - 1.0, 0.0) * 2.5) * 0.75;
    float veinNoise = texture(Sampler1, vec2(ang * 2.0 + Time * 0.01, d * 0.35 - Time * 0.03)).r;
    float veins = smoothstep(0.55, 0.75, veinNoise) * smoothstep(1.0, 0.5, d) * 1.4;
    float glow = clamp((halo + veins) * outside * (1.0 - vanish), 0.0, 1.0);
    glow *= smoothstep(1.0, 0.72, d);

    vec3 col = core * inside + red * outside;
    float alpha = max(inside, glow);
    if (alpha < 0.004) discard;
    fragColor = vec4(col, alpha);
}
