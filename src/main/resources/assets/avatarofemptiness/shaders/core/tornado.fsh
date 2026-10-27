#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float Time;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    vec2 coords = texCoord0;
    vec2 base = texCoord0;
    float cosAngle = clamp(coords.x, -1.0, 1.0);
    float spin = vertexColor.r > 0.5 ? 1.0 : -1.0;

    coords.x = acos(cosAngle) / 3.141;
    coords.x += (coords.y * spin * -4.4 - spin * Time * 0.9 * 2.3) * 0.87;
    coords.y += coords.x * spin * -0.6 - Time * 0.6 * 0.98;

    float warp = texture(Sampler1, coords).r * 16.0 + coords.y * 20.0;
    coords += vec2(cos(warp), sin(warp)) * 0.03;
    coords *= 0.6;

    float n = sqrt(texture(Sampler0, coords).r * texture(Sampler0, coords * 0.8).r);
    n -= smoothstep(0.2, 0.0, base.y) * 0.5 + smoothstep(0.9, 1.0, base.y) * 0.45;

    float opacity = smoothstep(1.0, 0.67, abs(cosAngle)) * smoothstep(0.1, 0.12, n);
    float glow = 1.0 + smoothstep(0.75, 0.1, abs(cosAngle)) * 0.8;
    vec3 baseColor = vec3(0.65, 0.92, 1.0);
    vec3 backColor = vec3(0.05, 0.25, 0.6);
    float threshold = 0.35;
    vec3 col = mix(baseColor, backColor * 0.6, smoothstep(threshold + 0.2, threshold, n)) * glow;
    fragColor = vec4(col, opacity * vertexColor.a);
    if (fragColor.a < 0.01) discard;
}
