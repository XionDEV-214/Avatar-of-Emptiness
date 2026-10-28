#version 150

uniform sampler2D Sampler0;
uniform float Time;
uniform vec4 Tint;
uniform float StaticMix;
uniform float Glow;

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

void main() {
    vec4 t = texture(Sampler0, texCoord0);
    if (t.a < 0.1) discard;
    vec3 c = t.rgb * vertexColor.rgb * Tint.rgb;
    c *= 1.0 + Glow * (0.6 + 0.4 * sin(Time * 6.0));
    float s = hash(floor(gl_FragCoord.xy * 0.5), floor(Time * 24.0));
    c = mix(c, vec3(s), StaticMix);
    fragColor = vec4(c, Tint.a);
}
