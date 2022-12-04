package jsettlers.graphics.image.reader.shadowmap;

public class ShadowMapping1 implements ShadowMapping {
	@Override
	public int getShadowIndex(int settlerIndex) {
		if(settlerIndex == 26) {
			return -1;
		} else if(settlerIndex > 26) {
			settlerIndex--;
		}

		if(settlerIndex == 32) {
			return -1;
		} else if(settlerIndex > 32) {
			settlerIndex--;
		}
		return settlerIndex;
	}
}
