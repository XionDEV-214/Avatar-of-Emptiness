#version 150

in vec3 Position;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec3 viewNormal;
out vec3 viewPos;

void main() {
    vec4 vp = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * vp;
    vertexColor = Color;
    viewNormal = mat3(ModelViewMat) * Normal;
    viewPos = vp.xyz;
}
