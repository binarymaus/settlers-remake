package jsettlers.shaders.compile;

import java.util.Properties;
import java.util.function.Supplier;

public class CompilerOptions {

	private final ShaderType shaderType;
	private final String fileName;

	private CompilerOutputType compilerOutputType;
	private String entryPoint;
	private boolean saveOutput;
	private boolean copySource;
	private GLSLStandard targeEnv;

	public static final String ENTRY_POINT_KEY = "entryPoint";
	public static final String COMPILER_OUTPUT_KEY = "compilerOutput";
	public static final String SAVE_OUTPUT_KEY = "saveOutput";
	public static final String COPY_SOURCE_KEY = "copySource";
	private static final String TARGET_API_KEY = "targetApi";

	public static final String BOOLEAN_TRUE = "true";
	public static final String BOOLEAN_FALSE = "false";

	public CompilerOptions(String fileName, ShaderType shaderType) {
		this.shaderType = shaderType;
		this.fileName = fileName;
	}

	public static boolean getBoolean(Properties properties, String key) {
		String value = getString(properties, key);

		if(BOOLEAN_TRUE.equalsIgnoreCase(value)) return true;
		if(BOOLEAN_FALSE.equalsIgnoreCase(value)) return false;

		throw new IllegalArgumentException(key + " must be either true or false!");
	}

	public static String getString(Properties properties, String key) {
		String value = properties.getProperty(key);

		if(value == null) throw new IllegalStateException(key + " must be defined!");

		return value;
	}

	public static <T extends Enum<?>> T getEnum(Properties properties, String key, Supplier<T[]> values) {
		String value = getString(properties, key);

		for(T possibleValue : values.get()) {
			if(possibleValue.name().equalsIgnoreCase(value)) {
				return possibleValue;
			}
		}

		throw new IllegalStateException(key + " has an unknown value!");
	}

	public static CompilerOptions from(Properties localShaderInfo, String name, ShaderType type) {
		CompilerOptions options = new CompilerOptions(name, type);

		options.entryPoint = getString(localShaderInfo, ENTRY_POINT_KEY);
		options.compilerOutputType = getEnum(localShaderInfo, COMPILER_OUTPUT_KEY, CompilerOutputType::values);

		options.saveOutput = getBoolean(localShaderInfo, SAVE_OUTPUT_KEY);
		options.copySource = getBoolean(localShaderInfo, COPY_SOURCE_KEY);

		options.targeEnv = getEnum(localShaderInfo, TARGET_API_KEY, GLSLStandard::values);

		return options;
	}

	public ShaderType getShaderType() {
		return shaderType;
	}

	public String getFileName() {
		return fileName;
	}

	public String getEntryPoint() {
		return entryPoint;
	}

	public CompilerOutputType getOutputType() {
		return compilerOutputType;
	}

	public boolean copySource() {
		return copySource;
	}

	public boolean saveOutput() {
		return saveOutput;
	}

	public GLSLStandard getTargeEnv() {
		return targeEnv;
	}
}
