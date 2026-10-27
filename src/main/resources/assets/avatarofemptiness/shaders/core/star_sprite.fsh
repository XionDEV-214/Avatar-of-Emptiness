#version 150

uniform sampler2D Sampler0;
uniform float Time;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    vec4 t = texture(Sampler0, texCoord0);
    float flicker = 0.9 + 0.1 * sin(Time * 30.0);
    fragColor = vec4(t.rgb * vertexColor.rgb * flicker, t.a * vertexColor.a);
    if (fragColor.a < 0.003) discard;
}
