package jsettlers.graphics.image.reader.shadowmap;

public class ShadowMapping6 implements ShadowMapping {
	@Override
	public int getShadowIndex(int settlerIndex) {
		// fix donkey shadows
		if(settlerIndex >= 15) {
			return settlerIndex - 8;
		} else {
			return settlerIndex;
		}
	}
}
