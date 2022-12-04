package go.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class ImageData {

	private ShortBuffer data16;
	private IntBuffer data32;
	private final int width;
	private final int height;

	public ImageData(int width, int height) {
		this(ByteBuffer.allocateDirect(width*height*4)
				.order(ByteOrder.nativeOrder())
				.asIntBuffer(),
				width,
				height);
	}

	private ImageData(IntBuffer data, int width, int height) {
		this.data32 = data;
		this.width = width;
		this.height = height;
	}

	public ShortBuffer getReadData16() {
		convertTo16();
		return data16;
	}

	public IntBuffer getReadData32() {
		convertTo32();
		return data32;
	}

	public IntBuffer getWriteData32() {
		convertTo32();
		return data32;
	}

	private void convertTo16() {
		data16 = ByteBuffer.allocateDirect(width*height*2).order(ByteOrder.nativeOrder()).asShortBuffer();

		while(data32.hasRemaining()) {
			int color = data32.get();
			int c1 = ((color>>24));
			int c2 = ((color>>16));
			int c3 = ((color>>8));
			int c4 = (color&0xFF);

			data16.put((short) (cnv8to4(c1) << 12 | cnv8to4(c2) << 8 | cnv8to4(c3) << 4 | cnv8to4(c4)));
		}
		data32.rewind();
		data16.rewind();
	}

	protected final int cnv8to4(int c8bit) {
		int unsigned = c8bit&0xFF;
		return (int) (unsigned/255f*15f);
	}

	private void convertTo32() {
		if(data32 != null) return;

		data32 = ByteBuffer.allocateDirect(width*height*4).order(ByteOrder.nativeOrder()).asIntBuffer();

		while(data16.hasRemaining()) {
			data32.put(Color.convertTo32Bit(data16.get()));
		}
		data32.rewind();
		data16 = null;
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

		ImageData newImage = new ImageData(newWidth, newHeight);
		IntBuffer newData = newImage.getWriteData32();
		IntBuffer oldData = getReadData32();

		for(int y = 0; y < newHeight; y++) {
			for(int x = 0; x < newWidth; x++) {
				int ox = (int) (x*width/(float)newWidth);
				int oy = (int) (y*height/(float)newHeight);

				newData.put(oldData.get(ox+oy*width));
			}
		}
		newData.rewind();
		return newImage;
	}

	public static ImageData of(IntBuffer data, int width, int height) {
		return new ImageData(data, width, height);
	}
}
