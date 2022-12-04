package jsettlers.textures.generation.formats;

import java.io.DataOutputStream;
import java.util.function.Function;

public enum ImageFormat {

	RGB8(1, RGB8Processor::new),
	GRAY8(2, GRAY8Processor::new),
	GRAY1(3, GRAY1Processor::new)
	;

	private final int magic;

	private final Function<DataOutputStream, ColorReader> createProcessor;

	ImageFormat(int magic, Function<DataOutputStream, ColorReader> createProcessor) {
		this.magic = magic;
		this.createProcessor = createProcessor;
	}

	public int getMagic() {
		return magic;
	}

	public ColorReader createProcessor(DataOutputStream out) {
		return createProcessor.apply(out);
	}
}
