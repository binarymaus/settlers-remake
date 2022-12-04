package jsettlers.graphics.image.reader.shadowmap;

public class ShadowMapping22 implements ShadowMapping {
	@Override
	public int getShadowIndex(int settlerIndex) {
		if(settlerIndex >= 8 && settlerIndex <= 13 || settlerIndex == 1) {
			return -1;
		} else if(settlerIndex == 0) {
			return 19;
		} else if (settlerIndex < 14) {
			return settlerIndex + 18;
		} else {
			return settlerIndex - 14;
		}
	}
}
