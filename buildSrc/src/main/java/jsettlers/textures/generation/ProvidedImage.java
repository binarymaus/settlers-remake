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
package jsettlers.textures.generation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import jsettlers.textures.generation.formats.ColorReader;

public class ProvidedImage {

	private final BufferedImage image;
	private final int[] offsets;
	private final int[] imageSize;

	public ProvidedImage(BufferedImage image, int[] offsets, int[] imageSize) {
		this.image = image;
		this.offsets = offsets;
		this.imageSize = imageSize;
	}

	public void getData(ColorReader reader) throws IOException {
		for(int y = 0; y < image.getHeight(); y++) {
			for(int x = 0; x < image.getWidth(); x++) {
				Color color = new Color(image.getRGB(x, y));
				reader.readColor(color);
			}
		}
	}

	public int getTextureWidth() {
		return image.getWidth();
	}

	public int getTextureHeight() {
		return image.getHeight();
	}

	public int getImageWidth() {
		return imageSize[0];
	}

	public int getImageHeight() {
		return imageSize[1];
	}

	public int getOffsetX() {
		return offsets[0];
	}

	public int getOffsetY() {
		return offsets[1];
	}
}
