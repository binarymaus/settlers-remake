package jsettlers.common.map.partition;

public interface ISingleProfessionLimit {

	int getCurrentCount();

	int getTargetCount();

	float getCurrentRatio();

	float getTargetRatio();

	boolean isRelative();
}
