package jsettlers.graphics.image.reader.imageindex;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.IntBuffer;

public abstract class AlphaBitImageConverter implements ImageConverter {

	private final int dataPerPixel;

	protected AlphaBitImageConverter(int dataPerPixel) {
		this.dataPerPixel = dataPerPixel;
	}

	@Override
	public final void convert(DataInputStream dis, int count, IntBuffer output) throws IOException {
		int blocks = count / 8;

		int[] blockData = new int[8];
		for(int i = 0; i < blocks; i++) {
			readSingleBlock(dis, blockData);
			output.put(blockData);
		}

		int remaining = count % 8;
		int[] restBlock = new int[remaining];
		readSingleBlock(dis, restBlock);
		output.put(restBlock);
		output.rewind();
	}

	private void readSingleBlock(DataInputStream dis, int[] data) throws IOException {
		int blockSize = data.length*dataPerPixel+1;
		byte[] bfr = dis.readNBytes(blockSize);
		if(blockSize != bfr.length) {
			throw new IOException("Invalid block data!");
		}

		byte alphaBits = bfr[bfr.length-1];

		for(int i = 0; i < data.length; i++) {
			boolean alphaBit = (alphaBits & (1 << data.length-i-1)) > 0;

			data[i] = readPixel(bfr, i*dataPerPixel, alphaBit);
		}
	}

	protected abstract int readPixel(byte[] bfr, int offset, boolean alpha);
}
