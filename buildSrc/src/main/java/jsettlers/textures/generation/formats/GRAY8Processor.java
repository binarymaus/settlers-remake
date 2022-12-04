package jsettlers.textures.generation.formats;

import java.io.DataOutputStream;
import java.io.IOException;
import jsettlers.textures.generation.Color;

public class GRAY8Processor extends AlphaBitProcessor {

	protected GRAY8Processor(DataOutputStream dos) {
		super(dos);
	}

	@Override
	protected void processColor(Color color) throws IOException {
		int sum = color.getRed8() + color.getGreen8() + color.getBlue8();

		dos.write((sum / 3) & 0xFF);
	}
}
