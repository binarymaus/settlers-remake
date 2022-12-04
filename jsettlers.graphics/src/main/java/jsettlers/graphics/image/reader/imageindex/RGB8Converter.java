package jsettlers.graphics.image.reader.imageindex;

public class RGB8Converter extends AlphaBitImageConverter {
	protected RGB8Converter() {
		super(3);
	}

	@Override
	protected int readPixel(byte[] bfr, int offset, boolean alpha) {
		return (bfr[offset]&0xFF) << 24 | (bfr[offset+1]&0xFF) << 16 | (bfr[offset+2]&0xFF) << 8 | (alpha?0xFF : 0);
	}
}
