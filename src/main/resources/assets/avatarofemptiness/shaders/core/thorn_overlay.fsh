#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float OutlinePx;
uniform vec4 EdgeColor;

out vec4 fragColor;

float alphaAt(vec2 p, ivec2 size) {
    ivec2 q = clamp(ivec2(p), ivec2(0), size - 1);
    return texelFetch(Sampler0, q, 0).a;
}

void main() {
    ivec2 size = textureSize(Sampler0, 0);
    vec2 p = gl_FragCoord.xy;
    vec4 base = texelFetch(Sampler0, ivec2(p), 0);
    if (base.a <= 0.0) discard;

    float off = base.a <= 0.7 ? 1.0 : 0.0;
    float l = alphaAt(p + vec2(-OutlinePx, 0.0), size) + off;
    float r = alphaAt(p + vec2(OutlinePx, 0.0), size) + off;
    float t = alphaAt(p + vec2(0.0, OutlinePx), size) + off;
    float b = alphaAt(p + vec2(0.0, -OutlinePx), size) + off;
    float edge = clamp(step(l, 0.0) + step(r, 0.0) + step(t, 0.0) + step(b, 0.0), 0.0, 1.0);

    vec4 c = base + edge * EdgeColor;
    fragColor = clamp(c, 0.0, 1.0);
    gl_FragDepth = texelFetch(Sampler1, ivec2(p), 0).r;
}
