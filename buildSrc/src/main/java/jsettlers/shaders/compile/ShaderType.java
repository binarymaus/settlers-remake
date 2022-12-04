package jsettlers.shaders.compile;

import org.lwjgl.util.shaderc.Shaderc;

public enum ShaderType {
	VERTEX_SHADER("vert", Shaderc.shaderc_vertex_shader),
	FRAGMENT_SHADER("frag", Shaderc.shaderc_fragment_shader),
	;

	private final String fileType;
	private final int shadercValue;

	ShaderType(String fileType, int shadercValue) {
		this.fileType = fileType;
		this.shadercValue = shadercValue;
	}

	public String getFileType() {
		return fileType;
	}

	public int getShadercValue() {
		return shadercValue;
	}

	public String getConfig(String subKey) {
		return fileType + "." + subKey;
	}

	public static ShaderType getByFileType(String fileType) {
		for(ShaderType type : values()) {
			if(type.getFileType().equals(fileType)) {
				return type;
			}
		}

		return null;
	}
}
