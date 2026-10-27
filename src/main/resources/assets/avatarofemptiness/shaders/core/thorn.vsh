#version 150

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 ModelMat;

out float worldY;

void main() {
    vec4 world = ModelMat * vec4(Position, 1.0);
    worldY = world.y;
    gl_Position = ProjMat * ModelViewMat * world;
}
