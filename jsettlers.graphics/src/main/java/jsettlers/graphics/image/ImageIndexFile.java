/*******************************************************************************
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
 *******************************************************************************/
package jsettlers.graphics.image;

import java.util.List;
import jsettlers.common.images.TextureMap;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import jsettlers.graphics.image.reader.ImageMetadata;
import jsettlers.graphics.image.reader.translator.ImageDataProducer;

/**
 * This class loads the image index from a file.
 *
 * @author Michael Zangl
 */
public class ImageIndexFile {
	private List<SingleImage> images = null;

	/**
	 * Gets the image reference with the given index.
	 *
	 * @param index
	 * 		The index of the image in the file.
	 * @return The image.
	 */
	public Image getImage(int index) {
		if (images == null) {
			try {
				load();
			} catch (IOException e) {
				e.printStackTrace();
				images = new ArrayList<>();
			}
		}
		if (index < images.size()) {
			return images.get(index);
		} else {
			return NullImage.getInstance();
		}
	}

	private void load() throws IOException {
		final DataInputStream in = new DataInputStream(new BufferedInputStream(getResource("texturemap")));

		byte[] header = new byte[4];
		in.read(header);
		if (header[0] != 'T' || header[1] != 'E' || header[2] != 'X' || header[3] != '2') {
			throw new IOException("Texture file has wrong version.");
		}

		images = new ArrayList<>();
		while (in.available() > 0) {
			images.add(readNextImage(in));
		}
	}

	private SingleImage readNextImage(final DataInputStream in) throws IOException {
		int offsetX = -in.readShort();
		int offsetY = -in.readShort();
		short width = in.readShort();
		short height = in.readShort();
		short textureFileNumber = in.readShort();
		int torsoIndex = in.readInt();
		int shadowIndex = in.readInt();
		String name = in.readUTF();

		ImageMetadata metadata = new ImageMetadata();
		metadata.height = height;
		metadata.width = width;
		metadata.offsetX = offsetX;
		metadata.offsetY = offsetY;

		ImageDataProducer prod = new ImageIndexImageProducer(textureFileNumber);

		if(torsoIndex >= 0 || shadowIndex >= 0) {
			SettlerImage img = new SettlerImage(metadata, prod, name);
			img.setTorso(getImageNegSafe(torsoIndex));
			img.setShadow(getImageNegSafe(shadowIndex));
			return img;
		} else {
			return new SingleImage(metadata, prod, name);
		}
	}

	private SingleImage getImageNegSafe(int index) {
		if(index >= 0) {
			return images.get(index);
		}

		return null;
	}

	public static InputStream getResource(String string) {
		return TextureMap.class.getResourceAsStream(string);
	}
}
