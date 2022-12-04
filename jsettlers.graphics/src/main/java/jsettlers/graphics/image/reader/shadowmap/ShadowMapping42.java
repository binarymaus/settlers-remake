package jsettlers.graphics.image.reader.shadowmap;

public class ShadowMapping42 implements ShadowMapping {
	@Override
	public int getShadowIndex(int settlerIndex) {
		if(settlerIndex <= 6) {
			return settlerIndex + 19;
		} else {
			return settlerIndex - 6;
		}
	}
}
