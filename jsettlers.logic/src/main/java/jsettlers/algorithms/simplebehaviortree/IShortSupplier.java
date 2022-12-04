package jsettlers.algorithms.simplebehaviortree;

import java.io.Serializable;

import java.util.function.Function;

@FunctionalInterface
public interface IShortSupplier<T> extends Function<T, Short>, Serializable {}
