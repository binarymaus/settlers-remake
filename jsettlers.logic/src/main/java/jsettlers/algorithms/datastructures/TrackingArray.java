package jsettlers.algorithms.datastructures;

import java.util.function.IntFunction;
import java.util.function.Predicate;

public class TrackingArray<T> {

	private final T[] array;
	private final Predicate<T> condition;
	private int count;

	public TrackingArray(IntFunction<T[]> create, int size, Predicate<T> condition) {
		this.array = create.apply(size);
		this.condition = condition;
	}

	public void set(int index, T value) {
		if(condition.test(array[index])) {
			count--;
		}

		if(condition.test(value)) {
			count++;
		}

		array[index] = value;
	}

	public T get(int index) {
		return array[index];
	}

	public int getTrackedCount() {
		return count;
	}
}
