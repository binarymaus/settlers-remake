#version 330

in vec3 vertex; //attribute
in vec2 texcoord; //attribute
in float color; //attribute

uniform mat4 globalTransform;
uniform mat4 projection;
uniform mat4 height;

out float frag_color;
out vec2 frag_texcoord;

void main() {
	vec4 transformed = height * vec4(vertex, 1);
	transformed.z = -.1;
	gl_Position = projection * globalTransform * transformed;

	frag_color = color;
	frag_texcoord = texcoord;
}
