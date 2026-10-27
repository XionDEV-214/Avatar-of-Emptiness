#version 150

uniform sampler2D Sampler0;
uniform float Time;
uniform int Mode;
uniform vec3 PalA;
uniform vec3 PalB;
uniform vec3 PalC;
uniform vec3 PalD;
uniform float Intensity;

in vec3 dir;
out vec4 fragColor;

vec3 palette(float t) {
    t = clamp(t, 0.0, 1.0) * 3.0;
    if (t < 1.0) return mix(PalA, PalB, t);
    if (t < 2.0) return mix(PalB, PalC, t - 1.0);
    return mix(PalC, PalD, t - 2.0);
}

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

float noise(vec2 uv) {
    return texture(Sampler0, uv).r;
}

void main() {
    vec3 n = normalize(dir);
    float u = atan(n.z, n.x) / 6.2831853 + 0.5;
    float v = acos(clamp(n.y, -1.0, 1.0)) / 3.14159265;
    vec2 uv = vec2(u, v);
    vec3 col;

    if (Mode == 0) {
        float a = noise(uv * vec2(4.0, 2.0) + vec2(Time * 0.004, Time * 0.002));
        float b = noise(uv * vec2(8.0, 4.0) + vec2(-Time * 0.007, Time * 0.003) + a * 0.15);
        col = palette(0.4 + (a - 0.36) * 2.2 + (b - 0.36) * 1.6) * (0.55 + 1.2 * b);
        float vein = smoothstep(0.55, 0.70, noise(uv * vec2(12.0, 6.0) + vec2(Time * 0.01, 0.0)));
        col += PalD * vein * 0.6;
    } else if (Mode == 1) {
        float swirl = u + Time * 0.035 + v * 1.6;
        vec2 p = vec2(swirl * 3.0, v * 2.0 - Time * 0.06);
        float a = noise(p);
        float b = noise(p * 2.1 + vec2(a * 0.3, Time * 0.02));
        col = palette(0.3 + (a - 0.22) * 2.0 + (b - 0.22) * 1.2) * (0.6 + 1.2 * b);
        col *= 0.75 + 0.25 * sin(v * 40.0 - Time * 2.0 + a * 6.0);
    } else if (Mode == 2) {
        float c = noise(uv * vec2(3.0, 1.5));
        float c2 = noise(uv * vec2(7.0, 3.5) + 0.31);
        float crack = clamp(smoothstep(0.82, 0.62, c) + smoothstep(0.75, 0.55, c2) * 0.6, 0.0, 1.0);
        float pulse = 0.92 + 0.08 * sin(Time * 3.0);
        col = mix(PalA * pulse, PalB, crack);
        col += PalC * smoothstep(0.90, 0.84, c) * (1.0 - smoothstep(0.84, 0.74, c)) * 0.9;
    } else if (Mode == 3) {
        vec2 p = uv * vec2(3.0, 2.0) + vec2(Time * 0.06, 0.0);
        float a = noise(p);
        float b = noise(p * vec2(2.0, 5.0) + vec2(Time * 0.14, 0.0) + a * 0.25);
        col = mix(PalA, PalB, a) * (0.35 + 0.9 * b);
        col += PalC * pow(b, 5.0) * 1.2;
        col = mix(col, PalD, smoothstep(0.85, 1.0, b) * 0.4);
    } else if (Mode == 4) {
        float a = noise(uv * vec2(6.0, 3.0));
        float b = noise(uv * vec2(13.0, 6.5) + 0.37);
        float c = noise(uv * vec2(25.0, 12.5) + 0.71);
        float stars = pow(a, 3.0) * 2.0 + pow(b, 4.0) * 1.5 + pow(c, 5.0) * 1.2;
        float twinkle = 0.7 + 0.3 * sin(Time * 4.0 + a * 50.0);
        vec3 tint = mix(PalD, PalC, b);
        col = PalA + mix(PalB, tint, 0.35) * stars * twinkle;
        col += PalB * 0.04 * smoothstep(0.4, 0.9, a);
    } else {
        vec2 g = floor(uv * vec2(1024.0, 512.0));
        float s = hash(g + floor(Time * 30.0) * vec2(1.7, 9.2));
        float scan = 0.85 + 0.15 * sin(v * 900.0 + Time * 40.0);
        col = vec3(s * scan);
    }

    fragColor = vec4(col * Intensity, 1.0);
}
