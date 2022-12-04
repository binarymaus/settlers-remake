package jsettlers.graphics.image.reader.imageindex;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.IntBuffer;

public interface ImageConverter {
	void convert(DataInputStream dis, int count, IntBuffer output) throws IOException;
}
