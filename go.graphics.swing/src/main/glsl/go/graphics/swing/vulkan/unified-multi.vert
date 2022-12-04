#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location=0) in vec3 position;
layout(location=1) in vec2 scale;
layout(location=2) in vec4 color;
layout(location=3) in vec3 additional; // intensity,index,mode

layout(constant_id=0) const int MAX_GLOBALTRANS_COUNT = 4;
layout(constant_id=1) const int MAX_GEOMETRY_DATA_QUAD_COUNT = 256;

layout(set=0, binding=0) uniform GlobalData {
    mat4 projection;
    mat4 globalTrans[MAX_GLOBALTRANS_COUNT];
} global;

layout(set=2, binding=0) uniform GeometryData {
    vec4 geometryData[4*MAX_GEOMETRY_DATA_QUAD_COUNT];
} geomtryBuffer;

layout(push_constant) uniform UnifiedPerCall {
    int globalTransIndex;
} local;

layout (location=0) out vec2 frag_texcoord;
layout (location=1) flat out int frag_mode;
layout (location=2) out float frag_intensity;
layout (location=3) out vec4 frag_color;

void main() {
    int mode = int(additional.z);
    frag_mode = mode;
    frag_intensity = additional.x;
    frag_color = color;

    int index = gl_VertexIndex+(int(additional.y));

    frag_texcoord = geomtryBuffer.geometryData[index].zw;
    vec2 local_vert = geomtryBuffer.geometryData[index].xy;

    vec4 transformed = vec4(local_vert*scale, 0, 1);
    transformed.xyz += position;

    gl_Position = global.projection * global.globalTrans[local.globalTransIndex] * transformed;
}