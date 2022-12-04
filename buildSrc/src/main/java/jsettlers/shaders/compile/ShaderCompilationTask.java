package jsettlers.shaders.compile;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.lwjgl.util.shaderc.Shaderc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ShaderCompilationTask extends DefaultTask {

	private Path inputDirectory;
	private Path generatedSourcesDirectory;
	private Path generatedResourcesDirectory;

	private static final String SHADER_INFO_FILE_NAME = "shaders.txt";
	private static final String SPIRV_SUFFIX = ".spv";

	private ShaderCompiler compiler;

	static {
		Shaderc.getLibrary();
	}

	@TaskAction
	public void compileShaders() throws IOException {
		if (generatedSourcesDirectory == null) {
			throw new RuntimeException("please use generatedSourcesDirectory=\"...\"");
		}

		if (generatedResourcesDirectory == null) {
			throw new RuntimeException("please use generatedResourcesDirectory=\"...\"");
		}

		if (inputDirectory == null) {
			throw new RuntimeException("please use resourceDirectory=\"...\"");
		}

		compiler = new ShaderCompiler();

		try {
			processFiles(inputDirectory);
		} finally {
			compiler.destroy();
		}
	}

	private void processFiles(Path sourceDirectory) throws IOException {
		final Path shaderInfoFile = sourceDirectory.resolve(SHADER_INFO_FILE_NAME);


		List<Path> directoryContent = Files.walk(sourceDirectory, 1).collect(Collectors.toList());

		directoryContent.remove(sourceDirectory);

		List<Path> files = new ArrayList<>();

		for(Path file : directoryContent) {
			if (Files.isDirectory(file)) {
				processFiles(file);
			} else if (Files.isRegularFile(file)) {
				files.add(file);
			} else {
				throw new IllegalStateException("Unknown directory entry: " + file);
			}
		}

		if(!Files.exists(shaderInfoFile) && !files.isEmpty()) {
			throw new IllegalStateException("All shader directories must contain a " + SHADER_INFO_FILE_NAME + " file!");
		}
		files.remove(shaderInfoFile);

		Properties shaderInfo = new Properties();

		if(!files.isEmpty()) {
			shaderInfo.load(Files.newInputStream(shaderInfoFile));

			Files.createDirectories(getOutputPath(sourceDirectory, generatedResourcesDirectory));
		}

		for (Path file : files) {
			processSingleFile(shaderInfo, file);
		}
	}

	private void processSingleFile(Properties localShaderInfo, Path file) throws IOException {
		String fileName = file.getFileName().toString();

		if(SHADER_INFO_FILE_NAME.equals(fileName)) return;

		if(!fileName.contains(".")) return;

		String[] fileParts = fileName.split("\\.", 2);

		ShaderType type = ShaderType.getByFileType(fileParts[1]);

		if(type == null) {
			getLogger().info("Ignoring " + file);
		} else {
			CompilerOptions options = CompilerOptions.from(localShaderInfo, file.toString(), type);
			compileShader(options, file, type);
		}
	}

	private void compileShader(CompilerOptions options, Path file, ShaderType type) throws IOException {
		String sourceCode = String.join("\n", Files.readAllLines(file));

		ByteBuffer output = null;

		if(options.getOutputType().getCompileFunction() != null) {
			output = compiler.compileShader(options, sourceCode, getLogger());
		}

		if(output != null && options.saveOutput()) {
			Path baseOutputFile = getOutputPath(file, generatedResourcesDirectory);
			Path parent = baseOutputFile.getParent();
			String sourceName = baseOutputFile.getFileName().toString();
			Path outputFile = parent.resolve(sourceName + SPIRV_SUFFIX);

			byte[] outputArray = new byte[output.remaining()];
			output.get(outputArray);

			Files.write(outputFile, outputArray);
		}

		if(options.copySource()) {
			Path outputSourceFile = getOutputPath(file, generatedResourcesDirectory);
			Files.copy(file, outputSourceFile, StandardCopyOption.REPLACE_EXISTING);
		}

	}

	public Path getOutputPath(Path inputPath, Path outputDir) {
		Path relative = inputDirectory.relativize(inputPath);

		return outputDir.resolve(relative);
	}

	@InputDirectory
	public Path getInputDirectory() {
		return inputDirectory;
	}

	public void setInputDirectory(Path inputDirectory) {
		this.inputDirectory = inputDirectory;
	}

	@OutputDirectory
	public Path getGeneratedSourcesDirectory() {
		return generatedSourcesDirectory;
	}

	public void setGeneratedSourcesDirectory(Path generatedSourcesDirectory) {
		this.generatedSourcesDirectory = generatedSourcesDirectory;
	}

	@OutputDirectory
	public Path getGeneratedResourcesDirectory() {
		return generatedResourcesDirectory;
	}

	public void setGeneratedResourcesDirectory(Path generatedResourcesDirectory) {
		this.generatedResourcesDirectory = generatedResourcesDirectory;
	}
}
