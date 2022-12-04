package jsettlers.graphics.image.reader.imageindex;

public class GRAY1Converter extends AlphaBitImageConverter {

	protected GRAY1Converter() {
		super(0);
	}

	@Override
	protected int readPixel(byte[] bfr, int offset, boolean alpha) {
		return alpha?136:0;
	}
}
