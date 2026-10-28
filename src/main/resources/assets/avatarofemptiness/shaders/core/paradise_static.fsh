#version 150

uniform float Time;

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
    vec2 g = floor(texCoord0);
    float s = hash(g, floor(Time * 24.0));
    float scan = 0.8 + 0.2 * sin(texCoord0.y * 3.14159 + Time * 30.0);
    fragColor = vec4(vec3(s * scan), vertexColor.a);
}
