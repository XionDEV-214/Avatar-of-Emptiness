#version 150

uniform sampler2D Sampler0;
uniform float Time;
uniform float Progress;
uniform float Trail;
uniform float Flash;
uniform float Margin;
uniform vec2 Size;
uniform int Mode;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

float hash(vec2 p, float f) {
    uvec3 q = uvec3(uvec2(ivec2(p)), uint(int(f)));
    uint h = q.x * 1597334677u ^ q.y * 3812015801u ^ q.z * 2654435761u;
    h ^= h >> 15u;
    h *= 2246822519u;
    h ^= h >> 13u;
    h *= 3266489917u;
    h ^= h >> 16u;
    return float(h) * (1.0 / 4294967295.0);
}

void palette(out vec3 dark, out vec3 bright, out vec3 edge) {
    if (Mode == 0) {
        dark = vec3(0.16, 0.01, 0.20);
        bright = vec3(0.86, 0.18, 0.70);
        edge = vec3(1.0, 0.62, 1.0);
    } else if (Mode == 1) {
        dark = vec3(0.32, 0.0, 0.06);
        bright = vec3(1.0, 0.16, 0.30);
        edge = vec3(1.0, 0.78, 0.84);
    } else if (Mode == 2) {
        dark = vec3(0.22, 0.0, 0.0);
        bright = vec3(1.0, 0.06, 0.04);
        edge = vec3(1.0, 0.62, 0.30);
    } else {
        dark = vec3(0.0);
        bright = vec3(1.0);
        edge = vec3(1.0);
    }
}

void main() {
    vec3 dark, bright, edge;
    palette(dark, bright, edge);

    vec2 p = texCoord0 * Size;
    vec2 lo = vec2(Margin);
    vec2 hi = Size - Margin;
    vec2 q = max(lo - p, p - hi);
    float outside = length(max(q, 0.0));
    float pulse = 0.75 + 0.25 * sin(Time * 3.0);

    if (q.x > 0.0 || q.y > 0.0) {
        float g = exp(-outside * 0.8) * 0.5 * pulse + Flash * exp(-outside * 0.5) * 0.6;
        vec3 gc = Mode == 3 ? vec3(hash(floor(p * 0.5), floor(Time * 24.0))) : edge;
        if (g < 0.004) discard;
        fragColor = vec4(gc, g);
        return;
    }

    vec2 b = (p - lo) / (hi - lo);
    float w = hi.x - lo.x;
    float rim = min(min(p.x - lo.x, hi.x - p.x), min(p.y - lo.y, hi.y - p.y));

    float bgN = texture(Sampler0, vec2(p.x * 0.01 + Time * 0.02, p.y * 0.05)).r;
    vec3 col = dark * 0.25 + vec3(0.02, 0.0, 0.03) + dark * bgN * 0.3;

    if (b.x < Progress) {
        if (Mode == 3) {
            float s = hash(floor(p * 0.75), floor(Time * 24.0));
            float scan = 0.8 + 0.2 * sin(p.y * 3.0 + Time * 30.0);
            col = vec3(s * scan);
        } else {
            float n1 = texture(Sampler0, vec2(p.x * 0.015 - Time * 0.18, p.y * 0.05 + Time * 0.03)).r;
            float n2 = texture(Sampler0, vec2(p.x * 0.031 - Time * 0.33 + n1 * 0.2, p.y * 0.09 - Time * 0.05)).r;
            float n = clamp((n1 - 0.36) * 1.6 + (n2 - 0.36) * 1.2 + 0.5, 0.0, 1.0);
            col = mix(dark, bright, n);
            col += bright * 0.35 * smoothstep(0.45, 0.0, b.y);
            col *= 0.8 + 0.2 * smoothstep(1.0, 0.6, b.y);
            float lead = (Progress - b.x) * w;
            col += edge * exp(-lead * 0.35) * 1.1;
        }
    } else if (b.x < Trail) {
        float f = 0.65 + 0.2 * sin(Time * 12.0);
        col = mix(col, Mode == 3 ? vec3(0.8) : mix(edge, vec3(1.0), 0.5), f);
    }

    for (int k = 1; k < 6; k++) {
        float nx = abs(b.x - float(k) / 6.0) * w;
        if (nx < 0.6) col *= 0.35;
    }

    if (rim < 1.0) col = mix(edge * 0.55, vec3(0.0), 0.35);
    col += Flash * 0.35 * edge;
    fragColor = vec4(col, 1.0);
}
