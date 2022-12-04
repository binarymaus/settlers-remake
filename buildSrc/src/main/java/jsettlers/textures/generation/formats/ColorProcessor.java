package jsettlers.textures.generation.formats;

import java.io.DataOutputStream;
import jsettlers.textures.generation.Color;

public abstract class ColorProcessor implements ColorReader {

	protected final DataOutputStream dos;

	protected ColorProcessor(DataOutputStream dos) {
		this.dos = dos;
	}
}
