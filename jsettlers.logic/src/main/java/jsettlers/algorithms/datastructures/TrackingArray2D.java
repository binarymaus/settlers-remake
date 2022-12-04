package jsettlers.algorithms.datastructures;

import java.util.function.IntFunction;
import java.util.function.Predicate;

public class TrackingArray2D<T> {

	private final TrackingArray<T> array;
	private final int width;

	public TrackingArray2D(IntFunction<T[]> create, int width, int height, Predicate<T> condition) {
		this.array = new TrackingArray<>(create, width*height, condition);
		this.width = width;
	}

	public void set(int x, int y, T value) {
		array.set(x+y*width, value);
	}

	public T get(int x, int y) {
		return array.get(x+y*width);
	}

	public int getTrackedCount() {
		return array.getTrackedCount();
	}
}
