package jsettlers.textures.generation.formats;

import java.io.DataOutputStream;
import jsettlers.textures.generation.Color;

public class GRAY1Processor extends AlphaBitProcessor {

	protected GRAY1Processor(DataOutputStream dos) {
		super(dos);
	}

	@Override
	protected void processColor(Color color) {
	}
}
