package jsettlers.graphics.image.reader.custom.graphics;

import jsettlers.graphics.image.Image;
import jsettlers.graphics.image.reader.DatFileReader;
import jsettlers.graphics.image.reader.EmptyDatFile;
import jsettlers.graphics.image.sequence.ArraySequence;
import jsettlers.graphics.image.sequence.Sequence;
import jsettlers.graphics.image.sequence.SequenceList;
import jsettlers.graphics.map.draw.DrawConstants;
import jsettlers.graphics.map.draw.ImageProvider;

public abstract class CustomDatFile extends EmptyDatFile {

	private final DatFileReader fallback;
	protected final ImageProvider imageProvider;

	CustomDatFile(DatFileReader fallback, ImageProvider imageProvider) {
		this.fallback = fallback;
		this.imageProvider = imageProvider;
	}

	@Override
	public SequenceList<Image> getSettlers() {
		return new SequenceList<Image>() {

			private SequenceList<Image> fallbackSequence = fallback.getSettlers();

			@Override
			public int size() {
				return Math.max(30, fallbackSequence.size());
			}

			@Override
			public Sequence<Image> get(int index) {
				if(!DrawConstants.FORCE_ORIGINAL) {
					Sequence<Image> custom = getCustom(index);
					if(custom != null) {
						return custom;
					}
				}

				if (index < fallbackSequence.size()) {
					Sequence<Image> sequence = fallbackSequence.get(index);

					if (sequence != null) {
						return sequence;
					}
				}
				return ArraySequence.getNullSequence();
			}
		};
	}

	protected abstract Sequence<Image> getCustom(int index);
}
