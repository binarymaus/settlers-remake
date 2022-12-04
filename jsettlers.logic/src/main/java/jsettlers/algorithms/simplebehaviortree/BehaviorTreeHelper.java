package jsettlers.algorithms.simplebehaviortree;

import jsettlers.algorithms.simplebehaviortree.nodes.DynamicGuardSelector;
import jsettlers.algorithms.simplebehaviortree.nodes.IGetPropertyProducer;
import jsettlers.algorithms.simplebehaviortree.nodes.ISetPropertyConsumer;
import jsettlers.algorithms.simplebehaviortree.nodes.Property;
import jsettlers.algorithms.simplebehaviortree.nodes.RepeatCount;
import jsettlers.algorithms.simplebehaviortree.nodes.ResetAfter;
import jsettlers.algorithms.simplebehaviortree.nodes.Sleep;
import jsettlers.algorithms.simplebehaviortree.nodes.Action;
import jsettlers.algorithms.simplebehaviortree.nodes.AlwaysFail;
import jsettlers.algorithms.simplebehaviortree.nodes.AlwaysRunning;
import jsettlers.algorithms.simplebehaviortree.nodes.AlwaysSucceed;
import jsettlers.algorithms.simplebehaviortree.nodes.Condition;
import jsettlers.algorithms.simplebehaviortree.nodes.Debug;
import jsettlers.algorithms.simplebehaviortree.nodes.Guard;
import jsettlers.algorithms.simplebehaviortree.nodes.Inverter;
import jsettlers.algorithms.simplebehaviortree.nodes.Selector;
import jsettlers.algorithms.simplebehaviortree.nodes.Sequence;
import jsettlers.algorithms.simplebehaviortree.nodes.Parallel;
import jsettlers.algorithms.simplebehaviortree.nodes.Repeat;
import jsettlers.algorithms.simplebehaviortree.nodes.Wait;

import java.io.Serializable;
import jsettlers.logic.constants.MatchConstants;

public final class BehaviorTreeHelper {

	/* --- Node Factory --- */

	public static <T> Action<T> action(INodeStatusActionConsumer<T> action) {
		return new Action<>(action);
	}

	public static <T> Node<T> action(String debugMessage, INodeStatusActionConsumer<T> action) {
		return debug(debugMessage, action(action));
	}

	public static <T> Action<T> action2(INodeStatusActionFunction<T> action) {
		return new Action<>(action);
	}

	public static <T> Node<T> action2(String debugMessage, INodeStatusActionFunction<T> action) {
		return debug(debugMessage, action2(action));
	}

	public static <T> Condition<T> condition(IBooleanConditionFunction<T> condition) {
		return new Condition<>(condition);
	}

	public static <T> Node<T> condition(String debugMessage, IBooleanConditionFunction<T> condition) {
		return debug(debugMessage, condition(condition));
	}

	public static <T> AlwaysFail<T> alwaysFail() {
		return new AlwaysFail<>();
	}

	public static <T> AlwaysRunning<T> alwaysRunning() {
		return new AlwaysRunning<>();
	}

	public static <T> AlwaysSucceed<T> alwaysSucceed() {
		return new AlwaysSucceed<>();
	}

	public static <T> Guard<T> guard(IBooleanConditionFunction<T> condition, Node<T> child) {
		return guard(condition, true, child);
	}

	public static <T> Node<T> guard(String debugMessage, IBooleanConditionFunction<T> condition, Node<T> child) {
		return guard(debugMessage, condition, true, child);
	}

	public static <T> Guard<T> guard(IBooleanConditionFunction<T> condition, boolean shouldBe, Node<T> child) {
		return new Guard<>(condition, shouldBe, child);
	}

	public static <T> Node<T> guard(String debugMessage, IBooleanConditionFunction<T> condition, boolean shouldBe, Node<T> child) {
		return debug(debugMessage, guard(condition, shouldBe, child));
	}

	public static <T> Inverter<T> inverter(Node<T> child) {
		return new Inverter<>(child);
	}

	public static <T> Node<T> ignoreFailure(Node<T> child) {
		return selector(child,
				alwaysSucceed());
	}

	public static <T> ResetAfter<T> resetAfter(INodeStatusActionConsumer<T> reset, Node<T> child) {
		return new ResetAfter<>(reset, child);
	}

	@SafeVarargs
	public static <T> Parallel<T> parallel(Parallel.Policy successPolicy, boolean preemptive, Node<T>... children) {
		return new Parallel<>(successPolicy, preemptive, children);
	}

	public static <T> Repeat<T> repeat(Repeat.Policy policy, IBooleanConditionFunction<T> condition, Node<T> child) {
		return new Repeat<>(policy, condition, child);
	}

	public static <T> Repeat<T> repeat(IBooleanConditionFunction<T> condition, Node<T> child) {
		return new Repeat<>(condition, child);
	}

	public static <T> RepeatCount<T> repeatLoop(IIntegerSupplier<T> times, Node<T> child) {
		return new RepeatCount<>(times, child);

	}

	public static <T> RepeatCount<T> repeatLoop(int times, Node<T> child) {
		return new RepeatCount<>(c -> times, child);
	}

	@SafeVarargs
	public static <T> Selector<T> selector(Node<T>... children) {
		return new Selector<>(children);
	}

	@SafeVarargs
	public static <T> Node<T> selector(String debugMessage, Node<T>... children) {
		return debug(debugMessage, new Selector<>(children));
	}

	@SafeVarargs
	public static <T> Node<T> guardSelector(Guard<T>... children) {
		return new DynamicGuardSelector<>(children);
	}

	@SafeVarargs
	public static <T> Node<T> guardSelector(String debugMessage, Guard<T>... children) {
		return debug(debugMessage, guardSelector(children));
	}

	@SafeVarargs
	public static <T> Sequence<T> sequence(Node<T>... children) {
		return new Sequence<>(children);
	}

	@SafeVarargs
	public static <T> Node<T> sequence(String debugMessage, Node<T>... children) {
		return debug(debugMessage, sequence(children));
	}

	public static <T> Wait<T> waitFor(Node<T> condition) {
		return new Wait<>(condition);
	}

	public static <T> Node<T> alwaysSucceed(Node<T> child) {
		return new Selector<>((Node<T>[])new Node[] {child, new AlwaysSucceed<>()});
	}

	public static <T> Sleep<T> sleep(IIntegerSupplier<T> delaySupplier) {
		return new Sleep<>(delaySupplier);
	}

	public static <T> Sleep<T> sleep(int delay) {
		return new Sleep<>(c->delay);
	}

	public static <T> Debug<T> debug(String msg, Node<T> child) {
		return new Debug<>(msg, child);
	}

	public static <T> Debug<T> debug(String msg) {
		return new Debug<>(msg);
	}

	public static <T, I extends Serializable> Property<T, I> property(ISetPropertyConsumer<T, I> setter, IGetPropertyProducer<T, I> getter, I value, Node<T> child) {
		return new Property<>(setter, getter, value, child);
	}

	public static <T> Node<T> doRandomly(float chance, Node<T> child) {
		return sequence(
				condition(mov -> MatchConstants.random().nextFloat() <= chance),
				child
		);
	}
}
