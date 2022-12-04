package jsettlers.common.statistics;

import java.util.function.Supplier;

public class IntervalTimeRateCalculator {
	private final long interval;

	private final TimeRateCalculator[] calculators;

	private int index = 0;

	private long prevTime;

	public IntervalTimeRateCalculator(long interval, int calculatorCount, Supplier<Long> getTargetTime) {
		this.interval = interval;

		calculators = new TimeRateCalculator[calculatorCount];
		for(int i = 0; i < calculatorCount; i++) {
			calculators[i] = new TimeRateCalculator(getTargetTime);
		}

		prevTime = System.nanoTime();
	}

	public void tick() {
		long newTime = System.nanoTime();

		if(prevTime + interval > newTime) return;
		prevTime = newTime;

		index++;
		index %= calculators.length;

		calculators[index].tick();
	}

	public float getRate() {
		float average = 0;

		for(int i = 0; i < calculators.length; i++) {
			if(i == index) continue;

			average += calculators[i].getRate();
		}

		return average / (calculators.length-1);
	}
}
