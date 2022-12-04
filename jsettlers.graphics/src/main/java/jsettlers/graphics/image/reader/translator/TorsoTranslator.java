/*
 * Copyright (c) 2015 - 2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package jsettlers.graphics.image.reader.translator;

import java.io.IOException;

import jsettlers.graphics.image.SingleImage;
import jsettlers.graphics.image.reader.bytereader.ByteReader;
import jsettlers.graphics.image.reader.ImageMetadata;

/**
 * This class reads the torso image. That image is always a grayscale image.
 * 
 * @author Michael Zangl
 *
 */
public class TorsoTranslator implements DatBitmapTranslator<SingleImage> {
	private static final int TORSO_BITS = 0x1f;

	@Override
	public int getTransparentColor() {
		return 0;
	}

	@Override
	public int readUntransparentColor(ByteReader reader) throws IOException {
		int read = (int) ((reader.read8() & TORSO_BITS) * 255f/31f); // convert 5->8 bits
		return (short) (read << 24 | read << 16 | read << 8 | 0xff); // convert to 8888
	}

	@Override
	public HeaderType getHeaderType() {
		return HeaderType.DISPLACED;
	}

	@Override
	public SingleImage createImage(ImageMetadata metadata, ImageDataProducer array, String name) {
		return new SingleImage(metadata, array, name);
	}
}
