package jsettlers.algorithms.simplebehaviortree.nodes;

import jsettlers.algorithms.simplebehaviortree.IBooleanConditionFunction;
import jsettlers.algorithms.simplebehaviortree.NodeStatus;
import jsettlers.algorithms.simplebehaviortree.Leaf;
import jsettlers.algorithms.simplebehaviortree.Tick;

public class Condition<T> extends Leaf<T> {
	private static final long serialVersionUID = -5811980322685099119L;

	private final IBooleanConditionFunction<T> condition;

	public Condition(IBooleanConditionFunction<T> condition) {
		this.condition = condition;
	}

	@Override
	protected NodeStatus onTick(Tick<T> tick) {
		return condition.test(tick.target) ? NodeStatus.SUCCESS : NodeStatus.FAILURE;
	}
}
