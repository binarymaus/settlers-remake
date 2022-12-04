package jsettlers.graphics.image;

import java.io.DataInputStream;
import java.io.IOException;

import go.graphics.ImageData;
import jsettlers.graphics.image.reader.imageindex.ImageIndexFormat;
import jsettlers.graphics.image.reader.translator.ImageDataProducer;

public class ImageIndexImageProducer implements ImageDataProducer {

	private final int textureNumber;

	public ImageIndexImageProducer(int textureNumber) {
		this.textureNumber = textureNumber;
	}

	@Override
	public ImageData produceData() {
		try(DataInputStream dis = new DataInputStream(ImageIndexFile.getResource("images_" + textureNumber))) {
			int magic = dis.readInt();

			ImageIndexFormat format = ImageIndexFormat.byMagic(magic);
			if(format == null) {
				throw new IllegalStateException("unknown image magic " + magic + "!");
			}

			int width = dis.readShort();
			int height = dis.readShort();

			ImageData output = new ImageData(width, height);

			format.convert(dis, width*height, output.getWriteData32());

			return output;
		} catch (IOException e) {
			throw new IllegalStateException("failed to load imageIndexImage", e);
		}
	}
}
