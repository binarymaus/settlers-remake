package jsettlers.graphics.image.reader.imageindex;

public class GRAY8Converter extends AlphaBitImageConverter {
	protected GRAY8Converter() {
		super(1);
	}

	@Override
	protected int readPixel(byte[] bfr, int offset, boolean alpha) {
		int value = bfr[offset]&0xFF;
		return value<<24 | value<<16 | value<<8 | (alpha?0xFF:0);
	}
}
