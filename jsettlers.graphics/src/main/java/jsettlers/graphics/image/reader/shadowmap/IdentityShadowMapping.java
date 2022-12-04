package jsettlers.graphics.image.reader.shadowmap;

public class IdentityShadowMapping implements ShadowMapping {

	@Override
	public int getShadowIndex(int settlerIndex) {
		return settlerIndex;
	}
}
