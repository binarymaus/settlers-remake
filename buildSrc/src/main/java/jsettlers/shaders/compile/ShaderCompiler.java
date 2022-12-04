package jsettlers.shaders.compile;

import org.gradle.api.logging.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.shaderc.Shaderc;

import java.nio.ByteBuffer;

public class ShaderCompiler {
	/**
	 * shaderc_compiler_t
	 */
	private final long shadercHandle;

	/**
	 *
	 */
	private final long shadercOptions;

	public ShaderCompiler() {
		shadercHandle = Shaderc.shaderc_compiler_initialize();
		if(shadercHandle == 0) throw new IllegalStateException("Could not initialize shader compiler!");

		shadercOptions = Shaderc.shaderc_compile_options_initialize();
		if(shadercOptions == 0) throw new IllegalStateException("Could not initialize shader options!");
	}

	public void destroy() {
		Shaderc.shaderc_compile_options_release(shadercOptions);
		Shaderc.shaderc_compiler_release(shadercHandle);
	}

	public ByteBuffer compileShader(CompilerOptions options, String sourceCode, Logger logger) {
		Shaderc.shaderc_compile_options_set_target_env(shadercOptions, options.getTargeEnv().getShadercEnv(), 0);

		ShaderType type = options.getShaderType();

		long result = options.getOutputType()
				.getCompileFunction()
				.compile(shadercHandle,
						sourceCode,
						type.getShadercValue(),
						options.getFileName(),
						options.getEntryPoint(),
						shadercOptions);

		if(result == 0) {
			throw new IllegalStateException("Shader compiler is out of memory!");
		}

		int status = Shaderc.shaderc_result_get_compilation_status(result);
		long warnings = Shaderc.shaderc_result_get_num_warnings(result);
		long errors = Shaderc.shaderc_result_get_num_errors(result);

		String errorMessage = Shaderc.shaderc_result_get_error_message(result);


		ByteBuffer resultData = null;

		if(status == Shaderc.shaderc_compilation_status_success) {
			ByteBuffer tmpResultData = Shaderc.shaderc_result_get_bytes(result);
			resultData = BufferUtils.createByteBuffer(tmpResultData.capacity());
			resultData.put(tmpResultData);
			resultData.rewind();
		}

		Shaderc.shaderc_result_release(result);


		if(warnings != 0) {
			logger.warn(options.getFileName() + " has " + warnings + " warnings!");
		}

		if(errors != 0) {
			logger.error(options.getFileName() + " has " + errors + " errors!");
		}

		if(errorMessage != null && !errorMessage.isEmpty()) {
			logger.warn("Compiling " + options.getFileName() + " resulted in: " + errorMessage);
		}

		if(status != Shaderc.shaderc_compilation_status_success) {
			logger.error(options.getFileName() + ": Could not compile shader!");
			throw new RuntimeException();
		}



		return resultData;
	}
}
