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
    float ang = atan(c.y, c.x);
    float n = texture(Sampler1, vec2(ang / 6.2831853 + Time * 0.25, d * 1.2 - Time * 0.6)).r;

    float rot = (1.0 - clamp(d, 0.0, 1.0)) * 0.6 * sin(Time * 1.7) + (n - 0.5) * 0.12;
    float cr = cos(rot), sr = sin(rot);
    vec2 rc = vec2(cr * c.x - sr * c.y, sr * c.x + cr * c.y) + 0.5;
    vec4 tex = texture(Sampler0, rc);

    float lum = max(tex.r, max(tex.g, tex.b));
    float core = (1.0 - lum) * tex.a;
    vec3 veins = vertexColor.rgb * (0.5 + 0.9 * lum);
    vec3 col = mix(veins, vec3(0.0), core);

    float swirl = pow(texture(Sampler1, vec2(ang / 6.2831853 * 2.0 - Time * 0.5 + d * 1.5, d - Time * 0.3)).r, 5.0);
    col += vertexColor.rgb * swirl * core * 0.8;

    fragColor = vec4(col, tex.a * vertexColor.a);
    if (fragColor.a < 0.01) discard;
}
