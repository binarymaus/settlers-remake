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
package jsettlers.graphics.image.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class IntArrayWriter implements ImageArrayProvider {
	private static final short TRANSPARENT = 0;
	private IntBuffer array;
	private int width;
	private int line;

	@Override
	public void startImage(int width, int height) throws IOException {
		if (width == 0 && height == 0) {
			array = ByteBuffer.allocateDirect(4).asIntBuffer();
		}
		this.width = width;
		array = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
	}

	@Override
	public void writeLine(int[] data, int linelength) throws IOException {
		int offset = line * width;
		for (int i = 0; i < linelength; i++) {
			array.put(offset + i, data[i]);
		}
		for (int i = linelength; i < width; i++) {
			array.put(offset + i, TRANSPARENT);
		}

		line++;
	}

	public IntBuffer getArray() {
		return array;
	}

}
