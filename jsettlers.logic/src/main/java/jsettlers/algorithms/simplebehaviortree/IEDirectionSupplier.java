package jsettlers.algorithms.simplebehaviortree;

import java.io.Serializable;

import java.util.function.Function;
import jsettlers.common.movable.EDirection;

@FunctionalInterface
public interface IEDirectionSupplier<T> extends Function<T, EDirection>, Serializable {
}
