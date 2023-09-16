/*******************************************************************************
 * Copyright (c) 2015
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
 *******************************************************************************/
package jsettlers.graphics.image;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import go.graphics.EPrimitiveType;
import go.graphics.GLDrawContext;
import go.graphics.ManagedUnifiedDrawHandle;

import java.awt.image.BufferedImage;

import jsettlers.common.Color;
import jsettlers.graphics.image.reader.ImageMetadata;
import go.graphics.ImageData;
import jsettlers.graphics.image.reader.translator.ImageDataProducer;

/**
 * This is the base for all images that are directly loaded from the image file.
 * <p>
 * This class interprets the image data in 4-4-4-4-Format. To change the interpretation, it is possible to subclass this class.
 *
 * @author Michael Zangl
 */
public class SingleImage extends Image implements ImageDataPrivider {

	protected final ImageDataProducer data;
	protected final int width;
	protected final int height;
	protected int twidth, theight, toffsetX, toffsetY;
	protected final int offsetX;
	protected final int offsetY;
	protected String name;

	protected ManagedUnifiedDrawHandle geometryIndex = null;

	/**
	 * Creates a new image by the given buffer.
	 *
	 * @param data
	 * 		The data buffer for the image with an unspecified color format.
	 * @param width
	 * 		The width.
	 * @param height
	 * 		The height.
	 * @param offsetX
	 * 		The x offset of the image.
	 * @param offsetY
	 * 		The y offset of the image.
	 */
	protected SingleImage(ImageDataProducer data, int width, int height, int offsetX,
			int offsetY, String name) {
		this.data = data;
		this.width = twidth = width;
		this.height = theight = height;
		this.offsetX = toffsetX = offsetX;
		this.offsetY = toffsetY = offsetY;
		this.name = name;
	}

	protected SingleImage(IntBuffer data, int width, int height, int offsetX,
						  int offsetY, String name) {
		this(() -> ImageData.of(data, width, height), width, height, offsetX, offsetY, name);
	}

	/**
	 * Creates a new image by linking this images data to the data of the provider.
	 *
	 * @param metadata
	 * 		The mata data to use.
	 * @param data
	 * 		The data to use.
	 */
	public SingleImage(ImageMetadata metadata, int[] data, String name) {
		this(wrap(data), metadata.width, metadata.height, metadata.offsetX, metadata.offsetY, name);
	}


	/**
	 * Creates a new image by linking this images data to the data of the provider.
	 *
	 * @param metadata
	 * 		The mata data to use.
	 * @param data
	 * 		The data to use.
	 */
	public SingleImage(ImageMetadata metadata, ImageDataProducer data, String name) {
		this(data, metadata.width, metadata.height, metadata.offsetX, metadata.offsetY, name);
	}

	private static IntBuffer wrap(int[] data) {
		IntBuffer bfr = ByteBuffer.allocateDirect(data.length*4).order(ByteOrder.nativeOrder()).asIntBuffer();
		bfr.put(data);
		bfr.rewind();
		return bfr;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public int getOffsetX() {
		return this.offsetX;
	}

	@Override
	public int getOffsetY() {
		return this.offsetY;
	}

	@Override
	public void drawImageAtRect(GLDrawContext gl, float x, float y, float width, float height, float intensity) {
		checkHandles(gl);

		// dark magic
		float sx = width/(float)twidth;
		float sy = height/(float)theight;
		float tx = x - offsetX*sx;
		float ty = y + height + offsetY*sy;
		geometryIndex.drawSimple(EPrimitiveType.Quad, tx, ty, 0, sx, sy, null, intensity);
	}

	@Override
	public ImageData getData() {
		return this.data.produceData();
	}

	@Override
	public void drawOnlyImageAt(GLDrawContext gl, float x, float y, float z, Color torsoColor, float fow) {
		checkHandles(gl);
		geometryIndex.drawSimple(EPrimitiveType.Quad, x, y, z, 1, 1, null, fow);
	}

	protected void checkHandles(GLDrawContext gl) {
		if(geometryIndex == null || !geometryIndex.isValid()) {
			ImageData texture = generateTextureData();
			geometryIndex = gl.createManagedUnifiedDrawCall(texture, toffsetX, toffsetY, twidth, theight);
		}
	}

	protected ImageData generateTextureData() {
		return getData();
	}


	@Override
	public void drawOnlyImageWithProgressAt(GLDrawContext gl, float x, float y, float z, float u1, float v1, float u2, float v2, float fow, boolean triangle) {
		checkHandles(gl);

		BuildingProgressDrawer.drawOnlyImageWithProgressAt(gl,
				x, y, z, u1, v1, u2, v2,
				geometryIndex.texX,
				geometryIndex.texWidth,
				geometryIndex.texY,
				geometryIndex.texHeight,
				geometryIndex.texture,
				geometryIndex.texture2,
				toffsetX,
				toffsetY,
				theight,
				twidth,
				fow, triangle);
	}

	public BufferedImage convertToBufferedImage() {
		if (width <= 0 || height <= 0) {
			return null;
		}

		BufferedImage rendered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		IntBuffer data = getData().convert(width, height).getReadData32();
		data.rewind();

		int[] rgbArray = new int[data.remaining()];
		for (int i = 0; i < rgbArray.length; i++) {
			int value = data.get();
			rgbArray[i] = (value>>8)|((value&0xFF)<<24);
		}

		rendered.setRGB(0, 0, width, height, rgbArray, 0, width);
		return rendered;
	}

	public Long hash() {
		IntBuffer data = getData().getReadData32().duplicate();
		data.rewind();
		long hashCode = 1L;
		long multiplier = 1L;
		while (data.hasRemaining()) {
			multiplier *= 31L;
			hashCode += (data.get() + 27L) * multiplier;
		}
		return hashCode;
	}
}