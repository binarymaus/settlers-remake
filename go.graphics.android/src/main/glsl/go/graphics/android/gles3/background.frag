#version 300 es

precision mediump float;

in float frag_color;
in vec2 frag_texcoord;

uniform sampler2D texHandle;

out vec4 fragColor;

void main() {
	fragColor = texture(texHandle, frag_texcoord);
	fragColor.rgb *= frag_color;
}
