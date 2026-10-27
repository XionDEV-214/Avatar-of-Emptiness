#version 150

uniform sampler2D Sampler0;
uniform float Time;
uniform int Mode;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    float across = abs(texCoord0.y - 0.5) * 2.0;
    if (Mode == 0) {
        float flow = texture(Sampler0, vec2(texCoord0.x * 0.5 - Time * 3.0, texCoord0.y * 0.5)).r;
        float edge = smoothstep(0.55, 0.75, across + (flow - 0.5) * 0.12);
        vec3 col = mix(vec3(0.0), vec3(1.0, 0.0, 0.0), edge);
        col += vec3(1.0, 0.2, 0.1) * pow(flow, 6.0) * edge;
        float a = 1.0 - smoothstep(0.92, 1.0, across);
        fragColor = vec4(col, a * vertexColor.a);
    } else {
        float dash = 0.6 + 0.4 * sin(texCoord0.x * 2.0 - Time * 12.0);
        float a = pow(1.0 - across, 1.5) * dash * vertexColor.a;
        fragColor = vec4(vertexColor.rgb, a);
    }
    if (fragColor.a < 0.004) discard;
}
