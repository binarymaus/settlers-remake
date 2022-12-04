package jsettlers.algorithms.simplebehaviortree.nodes;

import jsettlers.algorithms.simplebehaviortree.Decorator;
import jsettlers.algorithms.simplebehaviortree.IBooleanConditionFunction;
import jsettlers.algorithms.simplebehaviortree.Node;
import jsettlers.algorithms.simplebehaviortree.NodeStatus;
import jsettlers.algorithms.simplebehaviortree.Tick;

public class Guard<T> extends Decorator<T> {
	private static final long serialVersionUID = -4675927057210755053L;

	protected final IBooleanConditionFunction<T> condition;
	protected final boolean                      value;

	public Guard(IBooleanConditionFunction<T> condition, Node<T> child) {
		super(child);
		this.condition = condition;
		value = true;
	}

	public Guard(IBooleanConditionFunction<T> condition, boolean shouldBe, Node<T> child) {
		super(child);
		this.condition = condition;
		value = shouldBe;
	}

	@Override
	protected NodeStatus onTick(Tick<T> tick) {
		boolean result = condition.test(tick.target);
		if (result == value) {
			return child.execute(tick);
		} else {
			return NodeStatus.FAILURE;
		}
	}

	public boolean checkGuardCondition(Tick<T> tick) {
		return condition.test(tick.target);
	}
}
