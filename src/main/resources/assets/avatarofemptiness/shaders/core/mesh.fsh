#version 150

uniform sampler2D Sampler0;
uniform float Time;
uniform vec4 Tint;
uniform float StaticMix;
uniform float Glow;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec4 t = texture(Sampler0, texCoord0);
    if (t.a < 0.1) discard;
    vec3 c = t.rgb * vertexColor.rgb * Tint.rgb;
    c *= 1.0 + Glow * (0.6 + 0.4 * sin(Time * 6.0));
    float s = hash(floor(gl_FragCoord.xy * 0.5) + floor(Time * 24.0) * vec2(3.1, 7.7));
    c = mix(c, vec3(s), StaticMix);
    fragColor = vec4(c, Tint.a);
}
