#version 150

uniform sampler2D Sampler0;
uniform float Time;
uniform float CamAngle;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

const float R = 0.33;
const float DISK_MINOR = 0.16;
const vec3 DISK_SCALE = vec3(1.05, 0.30, 1.05);
const vec3 DISK_COLOR = vec3(1.0, 0.93, 0.86);

float hash13(vec3 p) {
    return fract(sin(dot(p, vec3(12.9898, 78.233, 51.9852))) * 30000.0);
}

float torusDistance(vec3 p, vec2 t) {
    vec2 q = vec2(length(p.xz) - t.x, p.y);
    return length(q) - t.y;
}

vec4 sampleDisk(vec3 pos) {
    vec3 o = pos / DISK_SCALE;
    float inside = -torusDistance(o, vec2(0.75, DISK_MINOR));
    float g = pow(max(0.0, inside / DISK_MINOR), 0.9);
    vec2 radial = vec2(atan(o.x, o.z) / 6.283 + 0.5, length(o));
    g *= texture(Sampler0, radial * vec2(3.0, 3.5) + Time * vec2(0.6, -0.2)).r;
    return vec4(DISK_COLOR, 1.0) * g * 0.75;
}

vec3 rotX(vec3 v, float a) {
    float c = cos(a), s = sin(a);
    return vec3(v.x, c * v.y - s * v.z, s * v.y + c * v.z);
}

void main() {
    vec2 uv = texCoord0 * 2.0 - 1.0;
    uv.y = -uv.y;
    vec3 p = rotX(vec3(uv, -1.2), CamAngle);
    vec3 stepDir = rotX(vec3(0.0, 0.0, 1.0), CamAngle);
    p += stepDir * hash13(p * 10.0 + Time) * 0.02;

    vec4 result = vec4(0.0);
    float captured = 0.0;
    for (int i = 0; i < 56; i++) {
        float dist = length(p);
        captured = smoothstep(0.01, -0.1, dist - R);
        float distortion = clamp(0.005 / (dist * dist), 0.0, 0.1) * R * 0.8;
        p -= normalize(p) * distortion;
        p += stepDir * (1.0 - captured) * 0.045;
        result += sampleDisk(p) * 1.8;
    }

    float d0 = length(uv);
    result.rgb += DISK_COLOR * clamp(0.02 / pow(max(d0 - R, 0.01), 1.5), 0.0, 2.0) * smoothstep(1.0, 0.4, d0) * 0.25;

    float alpha = clamp(max(captured, max(result.r, max(result.g, result.b))), 0.0, 1.0);
    fragColor = vec4(result.rgb * (1.0 - captured), alpha * vertexColor.a);
    if (fragColor.a < 0.005) discard;
}
