package jsettlers.ai.highlevel;

import jsettlers.common.landscape.EResourceType;

import java.util.Arrays;

public class AiPartitionResources {
	final long[] resourceCount = new long[EResourceType.VALUES.length];
	long grassCount;
	long stoneCount;
	long usableSwampCount;

	public void clear() {
		Arrays.fill(resourceCount, 0);

		grassCount = 0;
		stoneCount = 0;
		usableSwampCount = 0;
	}
}
