package jsettlers.textures.generation.formats;

import java.io.DataOutputStream;
import java.io.IOException;
import jsettlers.textures.generation.Color;

public abstract class AlphaBitProcessor extends ColorProcessor {

	protected AlphaBitProcessor(DataOutputStream dos) {
		super(dos);
		data = 1;
	}

	private int data;

	protected abstract void processColor(Color color) throws IOException;

	@Override
	public final void readColor(Color color) throws IOException {
		processColor(color);

		data <<= 1;
		data |= color.getAlpha1()?1:0;
		if((data&(1<<8))>0) {
			dos.writeByte(data&0xFF);
			data = 1;
		}
	}

	public void flush() throws IOException {
		dos.writeByte(data&0xFF);
	}
}
