package jsettlers.textures.generation.formats;

import java.io.IOException;
import jsettlers.textures.generation.Color;

public interface ColorReader {

	void readColor(Color color) throws IOException;

	default void flush() throws IOException {}
}
