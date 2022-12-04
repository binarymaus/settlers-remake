package jsettlers.shaders.compile;

import org.lwjgl.util.shaderc.Shaderc;

public enum GLSLStandard {
	VULKAN(Shaderc.shaderc_target_env_vulkan),
	MODERN_OPENGL(Shaderc.shaderc_target_env_opengl),
	LEGACY_OPENGL(Shaderc.shaderc_target_env_opengl_compat),
	;

	private final int shadercEnv;

	GLSLStandard(int shadercEnv) {
		this.shadercEnv = shadercEnv;
	}

	public int getShadercEnv() {
		return shadercEnv;
	}
}
