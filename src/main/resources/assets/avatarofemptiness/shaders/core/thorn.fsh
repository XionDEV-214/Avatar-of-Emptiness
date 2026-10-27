#version 150

uniform float ClipY;
uniform float GroundFade;

in float worldY;
out vec4 fragColor;

void main() {
    float h = worldY - ClipY;
    if (h <= 0.0) discard;
    fragColor = vec4(0.0, 0.0, 0.0, smoothstep(0.0, GroundFade, h));
}
