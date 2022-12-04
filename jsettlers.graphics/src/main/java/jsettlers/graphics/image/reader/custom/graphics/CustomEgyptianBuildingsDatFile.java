package jsettlers.graphics.image.reader.custom.graphics;

import jsettlers.common.images.AnimationSequence;
import jsettlers.graphics.image.Image;
import jsettlers.graphics.image.reader.DatFileReader;
import jsettlers.graphics.image.reader.EmptyDatFile;
import jsettlers.graphics.image.reader.WrappedAnimation;
import jsettlers.graphics.image.sequence.ArraySequence;
import jsettlers.graphics.image.sequence.Sequence;
import jsettlers.graphics.image.sequence.SequenceList;
import jsettlers.graphics.map.draw.DrawConstants;
import jsettlers.graphics.map.draw.ImageProvider;

public class CustomEgyptianBuildingsDatFile extends CustomDatFile {

	CustomEgyptianBuildingsDatFile(DatFileReader fallback, ImageProvider imageProvider) {
		super(fallback, imageProvider);
	}

	@Override
	protected Sequence<Image> getCustom(int index) {
		if (index == 1) {
			return new WrappedAnimation(imageProvider, new AnimationSequence("egyptian_sawmill_sawmill", 0, 2));
		}
		if(index == 3) {
			return new WrappedAnimation(imageProvider, new AnimationSequence("egyptian_stonecutter_stonecutter", 0, 2));
		}
		return null;
	}

}
