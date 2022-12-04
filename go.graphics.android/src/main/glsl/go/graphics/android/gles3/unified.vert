#version 300 es

precision mediump float;

in vec2 vertex; //attribute
in vec2 texcoord; //attribute

uniform mat4 globalTransform;
uniform vec3 transform[2];
uniform mat4 projection;

uniform lowp int mode;

out vec2 frag_texcoord;

void main() {
	vec4 transformed = vec4(vertex, 0, 1);
	transformed.xyz = (transformed.xyz*transform[1])+transform[0];
	gl_Position = projection * globalTransform * transformed;

	if(mode != 0) frag_texcoord = texcoord;
}
