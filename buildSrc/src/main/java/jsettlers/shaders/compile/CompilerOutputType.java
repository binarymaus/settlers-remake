package jsettlers.shaders.compile;

import org.lwjgl.util.shaderc.Shaderc;

public enum CompilerOutputType {
	SPIRV_BINARY(Shaderc::shaderc_compile_into_spv),
	SPIRV_TEXT(Shaderc::shaderc_compile_into_spv_assembly),
	PREPROCESSED_TEXT(Shaderc::shaderc_compile_into_preprocessed_text),
	DO_NOT_COMPILE(null),
	;

	private final ShadercInterface compileFunction;

	CompilerOutputType(ShadercInterface compileFunction) {
		this.compileFunction = compileFunction;
	}

	public ShadercInterface getCompileFunction() {
		return compileFunction;
	}

	public interface ShadercInterface {
		long compile(long compiler,
					 CharSequence source_text,
					 int shader_kind,
					 CharSequence input_file_name,
					 CharSequence entry_point_name,
					 long additional_options);
	}
}
