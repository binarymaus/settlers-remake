#version 330

in float frag_color;
in vec2 frag_texcoord;

out vec4 fragColor;

uniform sampler2D texHandle;

void main() {
	fragColor = texture(texHandle, frag_texcoord);
	fragColor.rgb *= frag_color;
}
