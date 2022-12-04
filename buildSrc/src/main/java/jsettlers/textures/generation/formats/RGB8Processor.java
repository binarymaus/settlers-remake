package jsettlers.textures.generation.formats;

import java.io.DataOutputStream;
import java.io.IOException;
import jsettlers.textures.generation.Color;

public class RGB8Processor extends AlphaBitProcessor {

	protected RGB8Processor(DataOutputStream dos) {
		super(dos);
	}

	@Override
	public void processColor(Color color) throws IOException {
		dos.write(color.getRed8());
		dos.write(color.getGreen8());
		dos.write(color.getBlue8());
	}
}
