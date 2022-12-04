package jsettlers.common.statistics;

import java.util.function.Supplier;

public class TimeRateCalculator {
	private final Supplier<Long> getTargetTime;

	private long prevTargetTime;
	private long prevRealTime;

	private float rate;

	public TimeRateCalculator(Supplier<Long> getTargetTime) {
		this.getTargetTime = getTargetTime;

		prevRealTime = System.nanoTime();
		prevTargetTime = getTargetTime.get();
	}

	public void tick() {
		long newRealTime = System.nanoTime();
		long newTargetTime = getTargetTime.get();

		float deltaRealTime = newRealTime - prevRealTime;
		float deltaTargetTime = newTargetTime - prevTargetTime;

		rate = deltaTargetTime/deltaRealTime;

		prevRealTime = newRealTime;
		prevTargetTime = newTargetTime;
	}

	public float getRate() {
		return rate;
	}
}
