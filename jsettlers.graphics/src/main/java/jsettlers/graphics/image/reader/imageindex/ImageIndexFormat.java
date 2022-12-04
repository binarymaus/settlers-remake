package jsettlers.graphics.image.reader.imageindex;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.function.Supplier;

public enum ImageIndexFormat {

	RGB8(1, RGB8Converter::new),
	GRAY8(2, GRAY8Converter::new),
	GRAY1(3, GRAY1Converter::new)
	;

	private final int magic;

	private final Supplier<ImageConverter> converter;

	ImageIndexFormat(int magic, Supplier<ImageConverter> converter) {
		this.magic = magic;
		this.converter = converter;
	}

	public static ImageIndexFormat byMagic(int magic) {
		for(ImageIndexFormat format : values()) {
			if(format.getMagic() == magic) {
				return format;
			}
		}

		return null;
	}

	public int getMagic() {
		return magic;
	}

	public void convert(DataInputStream dis, int count, IntBuffer output) throws IOException {
		converter.get().convert(dis, count, output);
	}
}
