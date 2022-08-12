package go.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class ImageData {

	private final ShortBuffer data;
	private final int width;
	private final int height;

	public ImageData(ShortBuffer data, int width, int height) {
		this.data = data;
		this.width = width;
		this.height = height;
	}

	public ShortBuffer getData() {
		return data;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public ImageData convert(int newWidth, int newHeight) {
		if(width == newWidth && height == newHeight) {
			return this;
		}

		ShortBuffer newData = ByteBuffer.allocateDirect(newWidth*newHeight*2)
				.order(ByteOrder.nativeOrder())
				.asShortBuffer();
		for(int y = 0; y < newHeight; y++) {
			for(int x = 0; x < newWidth; x++) {
				int ox = (int) (x*width/(float)newWidth);
				int oy = (int) (y*height/(float)newHeight);

				newData.put(data.get(ox+oy*width));
			}
		}
		newData.rewind();
		return of(newData, newWidth, newHeight);
	}

	public static ImageData of(ShortBuffer data, int width, int height) {
		return new ImageData(data, width, height);
	}
}
